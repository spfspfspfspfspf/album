package com.spf.album.utils;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AppExecutors {
    private static AppExecutors instance;
    private final Executor mBackgroundExecutor;
    private final Handler mMainHandler;

    private AppExecutors() {
        this.mBackgroundExecutor = Executors.newCachedThreadPool();
        this.mMainHandler = new Handler(Looper.getMainLooper());
    }

    public synchronized static AppExecutors getInstance() {
        if (instance == null) {
            instance = new AppExecutors();
        }
        return instance;
    }

    public void runOnBackground(Runnable runnable) {
        mBackgroundExecutor.execute(runnable);
    }

    public void runOnUI(Runnable runnable) {
        mMainHandler.post(runnable);
    }

    public void runOnUI(Runnable runnable, long delay) {
        mMainHandler.postDelayed(runnable, delay);
    }
}
