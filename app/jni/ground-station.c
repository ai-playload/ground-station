#include <string.h>
#include <jni.h>
#include <android/log.h>
#include <gst/gst.h>
#include <pthread.h>

GST_DEBUG_CATEGORY_STATIC (debug_category);
#define GST_CAT_DEFAULT debug_category

/*
 * These macros provide a way to store the native pointer to CustomData, which might be 32 or 64 bits, into
 * a jlong, which is always 64 bits, without warnings.
 */
#if GLIB_SIZEOF_VOID_P == 8
# define GET_CUSTOM_DATA(env, thiz, fieldID) (CustomData *)(*env)->GetLongField (env, thiz, fieldID)
# define SET_CUSTOM_DATA(env, thiz, fieldID, data) (*env)->SetLongField (env, thiz, fieldID, (jlong)data)
#else
# define GET_CUSTOM_DATA(env, thiz, fieldID) (CustomData *)(jint)(*env)->GetLongField (env, thiz, fieldID)
# define SET_CUSTOM_DATA(env, thiz, fieldID, data) (*env)->SetLongField (env, thiz, fieldID, (jlong)(jint)data)
#endif

static void call_toast_from_jni(jobject app, const char *message);

/* Structure to contain all our information, so we can pass it to callbacks */
typedef struct _CustomData
{
  jobject app;                  /* Application instance, used to call its methods. A global reference is kept. */
  GstElement *pipeline;         /* The running pipeline */
  GMainContext *context;        /* GLib context used to run the main loop */
  GMainLoop *main_loop;         /* GLib main loop */
  gboolean initialized;         /* To avoid informing the UI multiple times about the initialization */
  gchar *command;               /* Command for the pipeline */
  GMutex lock;     // Mutex for thread safety
  gboolean is_connected;  // Connection status
} CustomData;

static void on_gst_message_received(GstBus *bus, GstMessage *msg, CustomData *data);

/* These global variables cache values which are not changing during execution */
static pthread_t gst_app_thread;
static pthread_key_t current_jni_env;
static JavaVM *java_vm;
static jfieldID custom_data_field_id;
static jmethodID set_message_method_id;
static jmethodID on_gstreamer_initialized_method_id;
static jmethodID on_playback_ended_method_id;

/*
 * Private methods
 */

/* Register this thread with the VM */
static JNIEnv *
attach_current_thread (void)
{
  JNIEnv *env;
  JavaVMAttachArgs args;

  GST_DEBUG ("Attaching thread %p", g_thread_self ());
  args.version = JNI_VERSION_1_4;
  args.name = NULL;
  args.group = NULL;

  if ((*java_vm)->AttachCurrentThread (java_vm, &env, &args) < 0) {
    GST_ERROR ("Failed to attach current thread");
    return NULL;
  }

  return env;
}

/* Unregister this thread from the VM */
static void
detach_current_thread (void *env)
{
  GST_DEBUG ("Detaching thread %p", g_thread_self ());
  (*java_vm)->DetachCurrentThread (java_vm);
}

/* Retrieve the JNI environment for this thread */
static JNIEnv *
get_jni_env (void)
{
  JNIEnv *env;

  if ((env = pthread_getspecific (current_jni_env)) == NULL) {
    env = attach_current_thread ();
    pthread_setspecific (current_jni_env, env);
  }

  return env;
}

/* Change the content of the UI's TextView */
static void
set_ui_message (const gchar * message, CustomData * data)
{
  JNIEnv *env = get_jni_env ();
  GST_DEBUG ("Setting message to: %s", message);
  jstring jmessage = (*env)->NewStringUTF (env, message);
  (*env)->CallVoidMethod (env, data->app, set_message_method_id, jmessage);
  if ((*env)->ExceptionCheck (env)) {
    GST_ERROR ("Failed to call Java method");
    (*env)->ExceptionClear (env);
  }
  (*env)->DeleteLocalRef (env, jmessage);
}

/* Retrieve errors from the bus and show them on the UI */
static void
error_cb (GstBus * bus, GstMessage * msg, CustomData * data)
{
  GError *err;
  gchar *debug_info;
  gchar *message_string;

  gst_message_parse_error (msg, &err, &debug_info);
  message_string =
      g_strdup_printf ("Error received from element %s: %s",
      GST_OBJECT_NAME (msg->src), err->message);
  g_clear_error (&err);
  g_free (debug_info);
  set_ui_message (message_string, data);
  g_free (message_string);
  gst_element_set_state (data->pipeline, GST_STATE_NULL);
}

/* Notify UI about pipeline state changes */
//static void
//state_changed_cb (GstBus * bus, GstMessage * msg, CustomData * data)
//{
//  GstState old_state, new_state, pending_state;
//  gst_message_parse_state_changed (msg, &old_state, &new_state, &pending_state);
//  /* Only pay attention to messages coming from the pipeline, not its children */
//  if (GST_MESSAGE_SRC (msg) == GST_OBJECT (data->pipeline)) {
//    gchar *message = g_strdup_printf ("State changed to %s",
//        gst_element_state_get_name (new_state));
//    set_ui_message (message, data);
//    g_free (message);
//  }
//}

static void state_changed_cb(GstBus *bus, GstMessage *msg, CustomData *data) {
    if (GST_MESSAGE_SRC(msg) == GST_OBJECT(data->pipeline)) {
        GstState old_state, new_state, pending_state;
        gst_message_parse_state_changed(msg, &old_state, &new_state, &pending_state);

        g_mutex_lock(&data->lock);
        if (new_state == GST_STATE_PLAYING) {
            data->is_connected = TRUE;  // Update the connection status
//            call_toast_from_jni(data->app, "Connection successful");
        } else if (new_state == GST_STATE_NULL) {
            data->is_connected = FALSE;  // Update the connection status
//            call_toast_from_jni(data->app, "Disconnected");
        }
        g_mutex_unlock(&data->lock);
    }
}

/* Check if all conditions are met to report GStreamer as initialized.
 * These conditions will change depending on the application */
static void
check_initialization_complete (CustomData * data)
{
  JNIEnv *env = get_jni_env ();
  if (!data->initialized && data->main_loop) {
    GST_DEBUG ("Initialization complete, notifying application. main_loop:%p",
        data->main_loop);
    (*env)->CallVoidMethod (env, data->app, on_gstreamer_initialized_method_id);
    if ((*env)->ExceptionCheck (env)) {
      GST_ERROR ("Failed to call Java method");
      (*env)->ExceptionClear (env);
    }
    data->initialized = TRUE;
  }
}

static pthread_t gst_app_thread;
static gboolean thread_running = FALSE;
static GMutex thread_lock;

/* Main method for the native code. This is executed on its own thread. */
static void *
app_function (void *userdata)
{
    CustomData *data = (CustomData *) userdata;
    char *command = data->command;
    GstBus *bus;
    GSource *bus_source;
    GError *error = NULL;

    GST_DEBUG ("Creating pipeline in CustomData at %p", data);

    /* Create our own GLib Main Context and make it the default one */
    data->context = g_main_context_new ();
    g_main_context_push_thread_default (data->context);

    /* Build pipeline */
    gchar *pipeline_desc = g_strdup_printf(
            "%s", command);

    data->pipeline = gst_parse_launch(pipeline_desc, &error);
    g_free(pipeline_desc);

    if (error) {
        gchar *message = g_strdup_printf("Unable to build pipeline: %s", error->message);
        g_clear_error(&error);
        set_ui_message(message, data);
        g_free(message);
//        call_toast_from_jni(data->app, "Connection failed");
        return NULL;
    }

    /* Instruct the bus to emit signals for each received message, and connect to the interesting signals */
    bus = gst_element_get_bus(data->pipeline);
    bus_source = gst_bus_create_watch(bus);
    g_source_set_callback(bus_source, (GSourceFunc) gst_bus_async_signal_func, NULL, NULL);
    g_source_attach(bus_source, data->context);
    g_source_unref(bus_source);
    g_signal_connect(G_OBJECT(bus), "message::error", (GCallback) error_cb, data);
    g_signal_connect(G_OBJECT(bus), "message::state-changed", (GCallback) state_changed_cb, data);
    g_signal_connect(G_OBJECT(bus), "message", (GCallback) on_gst_message_received, data);
    gst_object_unref(bus);

    /* Create a GLib Main Loop and set it to run */
    GST_DEBUG ("Entering main loop... (CustomData:%p)", data);
    data->main_loop = g_main_loop_new(data->context, FALSE);
    check_initialization_complete(data);
//    call_toast_from_jni(data->app, "Connection successful");
    g_main_loop_run(data->main_loop);
    GST_DEBUG ("Exited main loop");
    if (data->main_loop) {
        g_main_loop_unref(data->main_loop);
        data->main_loop = NULL;
    }

    /* Free resources */
    g_main_context_pop_thread_default(data->context);
    g_main_context_unref(data->context);
    gst_element_set_state(data->pipeline, GST_STATE_NULL);
    gst_object_unref(data->pipeline);

    g_mutex_lock(&thread_lock);
    thread_running = FALSE;
    g_mutex_unlock(&thread_lock);

    return NULL;
}

static void
call_toast_from_jni(jobject app, const char *message) {
    JNIEnv *env = get_jni_env();
    jclass clazz = (*env)->GetObjectClass(env, app);
    jmethodID methodID = (*env)->GetMethodID(env, clazz, "showToast", "(Ljava/lang/String;)V");

    if (methodID != NULL) {
        jstring jmessage = (*env)->NewStringUTF(env, message);
        (*env)->CallVoidMethod(env, app, methodID, jmessage);
        (*env)->DeleteLocalRef(env, jmessage);
    }
}

static void
gst_native_connect(JNIEnv *env, jobject thiz, jstring jcommand)
{
    CustomData *data = GET_CUSTOM_DATA(env, thiz, custom_data_field_id);
    if (!data) return;

    const gchar *command = (*env)->GetStringUTFChars(env, jcommand, NULL);

    g_mutex_lock(&thread_lock);
    // Update the command
    if (data->command) g_free(data->command);
    data->command = g_strdup(command);

    // Check if thread is already running
    if (!thread_running) {
        pthread_create(&gst_app_thread, NULL, &app_function, data);
        thread_running = TRUE;
    }
    g_mutex_unlock(&thread_lock);

    (*env)->ReleaseStringUTFChars(env, jcommand, command);
}

/*
 * Java Bindings
 */

/* Instruct the native code to create its internal data structure, pipeline and thread */
static void
gst_native_init (JNIEnv * env, jobject thiz)
{
  CustomData *data = g_new0 (CustomData, 1);
  SET_CUSTOM_DATA (env, thiz, custom_data_field_id, data);
  GST_DEBUG_CATEGORY_INIT (debug_category, "ground-station", 0,
      "Android ground station");
  gst_debug_set_threshold_for_name ("ground-station", GST_LEVEL_DEBUG);
  GST_DEBUG ("Created CustomData at %p", data);
  data->app = (*env)->NewGlobalRef (env, thiz);
  GST_DEBUG ("Created GlobalRef for app object at %p", data->app);

  g_mutex_init(&data->lock);  // Initialize the mutex
  data->is_connected = FALSE;  // Initialize connection status to FALSE

//  pthread_create (&gst_app_thread, NULL, &app_function, data);
}

/* Quit the main loop, remove the native thread and free resources */
static void
gst_native_finalize (JNIEnv * env, jobject thiz)
{
  CustomData *data = GET_CUSTOM_DATA (env, thiz, custom_data_field_id);
  if (!data)
    return;
  GST_DEBUG ("Quitting main loop...");
  g_main_loop_quit (data->main_loop);
  GST_DEBUG ("Waiting for thread to finish...");
  pthread_join (gst_app_thread, NULL);
  GST_DEBUG ("Deleting GlobalRef for app object at %p", data->app);
  (*env)->DeleteGlobalRef (env, data->app);
  GST_DEBUG ("Freeing CustomData at %p", data);
  g_free (data);
  SET_CUSTOM_DATA (env, thiz, custom_data_field_id, NULL);
  GST_DEBUG ("Done finalizing");
}

/* Set pipeline to PLAYING state */
static void
gst_native_play (JNIEnv * env, jobject thiz)
{
  CustomData *data = GET_CUSTOM_DATA (env, thiz, custom_data_field_id);
  if (!data)
    return;
  GST_DEBUG ("Setting state to PLAYING");
  gst_element_set_state (data->pipeline, GST_STATE_PLAYING);
}

/* Set pipeline to PAUSED state */
static void
gst_native_pause (JNIEnv * env, jobject thiz)
{
  CustomData *data = GET_CUSTOM_DATA (env, thiz, custom_data_field_id);
  if (!data)
    return;
  GST_DEBUG ("Setting state to PAUSED");
  gst_element_set_state (data->pipeline, GST_STATE_PAUSED);
}

static void
gst_native_disconnect (JNIEnv *env, jobject thiz)
{
    CustomData *data = GET_CUSTOM_DATA (env, thiz, custom_data_field_id);

    if (data->pipeline) {
        gst_element_set_state(data->pipeline, GST_STATE_NULL);
        gst_object_unref(data->pipeline);
        data->pipeline = NULL;
    }

    if (data->main_loop) {
        g_main_loop_quit(data->main_loop);
        data->main_loop = NULL;
    }

    pthread_join (gst_app_thread, NULL);

//    CustomData *data = GET_CUSTOM_DATA (env, thiz, custom_data_field_id);
//    if (!data)
//        return;
//    GST_DEBUG ("Quitting main loop...");
//    g_main_loop_quit (data->main_loop);
//    GST_DEBUG ("Waiting for thread to finish...");
//    pthread_join (gst_app_thread, NULL);
//    GST_DEBUG ("Deleting GlobalRef for app object at %p", data->app);
//    (*env)->DeleteGlobalRef (env, data->app);
//    GST_DEBUG ("Freeing CustomData at %p", data);
//    g_free (data);
//    SET_CUSTOM_DATA (env, thiz, custom_data_field_id, NULL);
//    GST_DEBUG ("Done finalizing");

    data->is_connected = FALSE;
}

static jboolean
gst_native_is_connected (JNIEnv *env, jobject thiz)
{
    CustomData *data = GET_CUSTOM_DATA (env, thiz, custom_data_field_id);
    return data->is_connected ? JNI_TRUE : JNI_FALSE;
}

/* Static class initializer: retrieve method and field IDs */
static jboolean
gst_native_class_init (JNIEnv * env, jclass klass)
{
  custom_data_field_id =
      (*env)->GetFieldID (env, klass, "native_custom_data", "J");
  set_message_method_id =
      (*env)->GetMethodID (env, klass, "setMessage", "(Ljava/lang/String;)V");
  on_gstreamer_initialized_method_id =
      (*env)->GetMethodID (env, klass, "onGStreamerInitialized", "()V");
    on_playback_ended_method_id =
      (*env)->GetMethodID(env, klass, "onPlaybackEnded", "()V");

  if (!custom_data_field_id || !set_message_method_id
      || !on_gstreamer_initialized_method_id || !on_playback_ended_method_id) {
    /* We emit this message through the Android log instead of the GStreamer log because the later
     * has not been initialized yet.
     */
    __android_log_print (ANDROID_LOG_ERROR, "ground-station",
        "The calling class does not implement all necessary interface methods");
    return JNI_FALSE;
  }
  return JNI_TRUE;
}

/* List of implemented native methods */
static JNINativeMethod native_methods[] = {
  {"nativeInit", "()V", (void *) gst_native_init},
  {"nativeFinalize", "()V", (void *) gst_native_finalize},
  {"nativePlay", "()V", (void *) gst_native_play},
  {"nativePause", "()V", (void *) gst_native_pause},
  {"nativeConnect", "(Ljava/lang/String;)V", (void *) gst_native_connect},
  {"nativeDisconnect", "()V", (void *) gst_native_disconnect},
  {"nativeIsConnected", "()Z", (void *) gst_native_is_connected},
  {"nativeClassInit", "()Z", (void *) gst_native_class_init}
};

static void
call_java_method(JNIEnv *env, jobject app, const char *method_name, const char *method_signature) {
    jclass clazz = (*env)->GetObjectClass(env, app);
    jmethodID methodID = (*env)->GetMethodID(env, clazz, method_name, method_signature);

//    if (methodID != NULL) {
//        (*env)->CallVoidMethod(env, app, methodID);
//        if ((*env)->ExceptionCheck(env)) {
//            GST_DEBUG("Failed to call Java method %s", method_name);
//            (*env)->ExceptionClear(env);
//        }
//    } else {
//        GST_DEBUG("Java method %s with signature %s not found", method_name, method_signature);
//    }

    if (methodID != NULL) {
        (*env)->CallVoidMethod(env, app, methodID);
        if ((*env)->ExceptionCheck(env)) {
            (*env)->ExceptionDescribe(env);  // 输出异常信息
            (*env)->ExceptionClear(env);     // 清除异常状态
            GST_DEBUG("Failed to call Java method %s", method_name);
        }
    } else {
        GST_DEBUG("Java method %s with signature %s not found", method_name, method_signature);
    }
}


/* Define the function */
static void on_gst_message_received(GstBus *bus, GstMessage *msg, CustomData *data) {

    switch (GST_MESSAGE_TYPE(msg)) {
        case GST_MESSAGE_EOS:
            GST_DEBUG("End-of-stream");
            // 处理流结束
            set_ui_message("播放结束", data);
            // 调用 Java 方法通知播放结束
            call_java_method(get_jni_env(), data->app, "onPlaybackEnded", "()V");
            gst_element_set_state(data->pipeline, GST_STATE_NULL);
            break;
        case GST_MESSAGE_ERROR: {
            GError *err;
            gchar *debug_info;
            gst_message_parse_error(msg, &err, &debug_info);
            gchar *message = g_strdup_printf("从元素 %s 收到错误: %s",
                                             GST_OBJECT_NAME(msg->src), err->message);
            g_clear_error(&err);
            g_free(debug_info);
            set_ui_message(message, data);
            g_free(message);
            gst_element_set_state(data->pipeline, GST_STATE_NULL);
            break;
        }
        default:
            break;
    }
}

/* Library initializer */
jint
JNI_OnLoad (JavaVM * vm, void *reserved)
{
  JNIEnv *env = NULL;

  java_vm = vm;

  if ((*vm)->GetEnv (vm, (void **) &env, JNI_VERSION_1_4) != JNI_OK) {
    __android_log_print (ANDROID_LOG_ERROR, "ground-station",
        "Could not retrieve JNIEnv");
    return 0;
  }
  jclass klass = (*env)->FindClass (env,
      "java/com/example/ground_station/data/service/GroundStationService");
  (*env)->RegisterNatives (env, klass, native_methods,
      G_N_ELEMENTS (native_methods));

  pthread_key_create (&current_jni_env, detach_current_thread);

  return JNI_VERSION_1_4;
}
