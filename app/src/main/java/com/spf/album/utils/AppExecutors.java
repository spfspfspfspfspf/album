package com.spf.album.utils;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AppExecutors {
    private static AppExecutors instance;
    private final Executor mBackgroundThread;
    private final Handler mMainHandler;

    private AppExecutors(Executor backgroundThread) {
        this.mBackgroundThread = backgroundThread;
        this.mMainHandler = new Handler(Looper.getMainLooper());
    }

    private AppExecutors() {
        this(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()));
    }

    public synchronized static AppExecutors getInstance() {
        if (instance == null) {
            instance = new AppExecutors();
        }
        return instance;
    }

    public void runOnBackground(Runnable runnable) {
        mBackgroundThread.execute(runnable);
    }

    public void runOnUI(Runnable runnable) {
        mMainHandler.post(runnable);
    }

    public void runOnUI(Runnable runnable, long delay) {
        mMainHandler.postDelayed(runnable, delay);
    }

    private static class MainThreadExecutor implements Executor {
        private Handler mainThreadHandler = new Handler(Looper.getMainLooper());

        @Override
        public void execute(@NonNull Runnable command) {
            mainThreadHandler.post(command);
        }


    }
}
