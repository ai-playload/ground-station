package java.com.example.ground_station.presentation.helper;

import com.blankj.utilcode.util.ThreadUtils;

import java.util.concurrent.TimeUnit;

public class TaskHelper {

    public static void main(String[] args) {
        ThreadUtils.SimpleTask<Object> task = new ThreadUtils.SimpleTask<Object>() {
            @Override
            public Object doInBackground() throws Throwable {

                return null;
            }

            @Override
            public void onSuccess(Object result) {

            }
        };
        task.cancel();
        task.run();
        task.isCanceled();
        ThreadUtils.executeByFixedAtFixRate(1, task, 1000, TimeUnit.MICROSECONDS);
    }
}
