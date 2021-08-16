package utils.threads;

public class CallbackThread extends Thread {

    private Callback<?> callback;

    protected CallbackThread (Runnable runnable, Callback<?> callback) {
        super(runnable);
        this.callback = callback;
    }

}
