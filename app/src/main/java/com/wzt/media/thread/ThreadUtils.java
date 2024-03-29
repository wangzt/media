package com.wzt.media.thread;

import android.os.Handler;
import android.os.Looper;

/**
 * @author jinmeng on 16-5-11.
 */
public class ThreadUtils {

    private static final Handler uiHandler = new Handler(Looper.getMainLooper());

    public static int getThreadCount() {
        return Thread.getAllStackTraces().size();
    }

    public static long getCurThreadId() {
        return Thread.currentThread().getId();
    }

    public static String getCurThreadName() {
        return Thread.currentThread().getName();
    }

    public static void postDelayed(Runnable action, long delayMillis){
        uiHandler.postDelayed(action, delayMillis);
    }

    public static void removeCallback(Runnable action){
        uiHandler.removeCallbacks(action);
    }

    public static void runOnUiThread(Runnable action) {
        if (action == null)
            return;

        if (ThreadUtils.getCurThreadId() == Looper.getMainLooper().getThread().getId()) {
            action.run();
        } else {
            uiHandler.post(action);
        }
    }
    public static void runOnUiThreadDelay(Runnable action, long delay) {
        if (action == null)
            return;
        uiHandler.postDelayed(action,delay);
    }
}
