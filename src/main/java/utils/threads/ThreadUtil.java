package utils.threads;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ThreadUtil {

    public static Thread runAsync(Runnable target) {
        Thread thread = new Thread(target);
        thread.start();
        thread.setUncaughtExceptionHandler((thread1, err) -> {
            err.printStackTrace();
        });
        return thread;
    }

    public static ScheduledFuture<?> runTaskTimer(Runnable task, long startDelayMillis, long periodMillis) {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        return executor.scheduleAtFixedRate(() -> {
            try {
                task.run();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }, startDelayMillis, periodMillis, TimeUnit.MILLISECONDS);
    }

    public static ScheduledFuture<?> runDelayedTask(Runnable task, long startDelayMillis) {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        return executor.schedule(() -> {
            try {
                task.run();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }, startDelayMillis, TimeUnit.MILLISECONDS);
    }


}
