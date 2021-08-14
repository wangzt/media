package com.wzt.media.thread;

import android.os.Handler;
import android.os.Looper;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import androidx.annotation.UiThread;
import androidx.annotation.WorkerThread;
import androidx.viewbinding.BuildConfig;

/**
 * 单线程任务执行者,循环利用线程,避免频繁创建线程
 */
public class JobWorker {

    public static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = CPU_COUNT + 1;
    private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 5;
    private static final int KEEP_ALIVE = 1;

    private static class ExecutorHolder {
        static ExecutorService EXECUTOR_IO = Executors.newFixedThreadPool(MAXIMUM_POOL_SIZE, new NamedThreadFactory("jobworker-"));
//        static ExecutorService EXECUTOR_CPU = Executors.newFixedThreadPool(CORE_POOL_SIZE);
        static Handler HANDLER = new Handler(Looper.getMainLooper());
    }

    public static <T> void submit(Task<T> task) {
        submit_IO(task);
    }

    public static <T> void submit_IO(Task<T> task) {
        if (ExecutorHolder.EXECUTOR_IO.isShutdown()) {
            return;
        }
        ExecutorHolder.EXECUTOR_IO.submit(task);
    }

    public static Future submit_IO(Runnable runnable) {
        if (ExecutorHolder.EXECUTOR_IO.isShutdown()) {
            return null;
        }
        return ExecutorHolder.EXECUTOR_IO.submit(runnable);
    }

    /**
     * 主线程执行
     * @param action
     * @param <T>
     */
    public static <T> void submitOnUiThread(Task<T> action) {
        if (ThreadUtils.getCurThreadId() == Looper.getMainLooper().getThread().getId()) {
            action.run();
        } else {
            ExecutorHolder.HANDLER.post(action);
        }
    }

//    public static <T> void submit_CPU(Task<T> task) {
//        if (ExecutorHolder.EXECUTOR_CPU.isShutdown()) {
//            return;
//        }
//        ExecutorHolder.EXECUTOR_CPU.submit(task);
//    }

    public static void shutdown() {
        ExecutorHolder.EXECUTOR_IO.shutdown();
//        ExecutorHolder.EXECUTOR_CPU.shutdown();
    }

    public static abstract class Task<T> implements Runnable {
        private boolean needCallback = true;

        protected Task() {
            this(true);
        }

        public Task(boolean needCallback) {
            this.needCallback = needCallback;
        }


        @UiThread
        public void onStart() {
            //empty for override
        }

        @WorkerThread
        public T doInBackground() {
            return null;
        }

        @Override
        public final void run() {
            if (needCallback) {
                ExecutorHolder.HANDLER.post(new Runnable() {
                    @Override
                    public void run() {
                        onStart();
                    }
                });
            }
            T result = null;
            if(BuildConfig.DEBUG){
                result = doInBackground();
            } else {
                try {
                    result = doInBackground();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (needCallback) {
                final T callbackResult = result;
                ExecutorHolder.HANDLER.post(new Runnable() {
                    @Override
                    public void run() {
                        onComplete(callbackResult);
                    }
                });
            }
        }

        @UiThread
        public void onComplete(T result) {
            //empty for override
        }
    }

    public static void execute(Runnable runnable) {
        ExecutorHolder.EXECUTOR_IO.execute(runnable);
    }

    public static ExecutorService getExecutor() {
        return ExecutorHolder.EXECUTOR_IO;
    }

    public static <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        return ExecutorHolder.EXECUTOR_IO.invokeAll(tasks);
    }
}
