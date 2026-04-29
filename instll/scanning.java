package com.appd.instll;

import static android.app.Service.START_NOT_STICKY;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class scanning extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
        android.util.Log.d("scanning", "Scanning Service initialized");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        android.util.Log.d("scanning", "Scanning started");

        // Simulate some kind of scanning task
        new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                android.util.Log.d("scanning", "Scanning... item " + i);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            android.util.Log.d("scanning", "Scan complete");
        }).start();

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        android.util.Log.d("scanning", "Scanning Service terminated");
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

