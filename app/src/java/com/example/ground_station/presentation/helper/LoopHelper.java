package java.com.example.ground_station.presentation.helper;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

public class LoopHelper {

//    private static LoopHelper helper = new LoopHelper();
//    Handler mainHandler;
    Handler workHandler;
    HandlerThread mHandlerThread;
    private long time = 1000;

    public void setTime(long time) {
        this.time = time;
    }

    public void setRunnable(Runnable runnable) {
        this.runnable = runnable;
    }

    public LoopHelper() {
        // 创建与主线程关联的Handler
//        mainHandler = new Handler();

        /**
         * 步骤1：创建HandlerThread实例对象
         * 传入参数 = 线程名字，作用 = 标记该线程
         */
        mHandlerThread = new HandlerThread("gs-rv");

        /**
         * 步骤2：启动线程
         */
        mHandlerThread.start();

        /**
         * 步骤3：创建工作线程Handler & 复写handleMessage（）
         * 作用：关联HandlerThread的Looper对象、实现消息处理操作 & 与其他线程进行通信
         * 注：消息处理操作（HandlerMessage（））的执行线程 = mHandlerThread所创建的工作线程中执行
         */

        workHandler = new Handler(mHandlerThread.getLooper()) {
            @Override
            // 消息处理的操作
            public void handleMessage(Message msg) {
                //设置了两种消息处理操作,通过msg来进行识别
                switch (msg.what) {
                    case 0:
                        if (runnable != null) {
                            runnable.run();
                        }
                        workHandler.sendEmptyMessageDelayed(0, time);
                        break;
                }
            }
        };

    }

    private Runnable runnable;

    public void start() {
        workHandler.removeMessages(0);
        workHandler.sendEmptyMessage(0);
        isRuning = true;
    }

   private boolean isRuning;

    public boolean isRuning() {
        return isRuning;
    }

    public void stop() {
        workHandler.removeCallbacksAndMessages(null);
        isRuning = false;
    }

}
