package com.example.mkarte1.repository;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

final class DbExecutor {
    static final ExecutorService IO = Executors.newSingleThreadExecutor();
    static final Handler MAIN = new Handler(Looper.getMainLooper());

    private DbExecutor() {
    }
}
