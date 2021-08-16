package utils.threads;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ThreadUtil {

    public static CallbackThread runAsync(Runnable target) {
        return runAsync(target, null);
    }

    public static CallbackThread runAsync(Runnable target, Callback<?> callback) {
        CallbackThread thread = new CallbackThread(target, callback);
        thread.start();
        return thread;
    }

    public static ScheduledFuture<?> runTaskTimer(Runnable task, int startDelay, int period) {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        return executor.scheduleAtFixedRate(task, startDelay, period, TimeUnit.MILLISECONDS);
    }

    public static ScheduledFuture<?> runDelayedTask(Runnable task, int startDelay) {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        return executor.schedule(task, startDelay, TimeUnit.MILLISECONDS);
    }


}
