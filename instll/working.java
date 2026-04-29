package com.appd.instll;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class working extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
        // Pretend setup
        android.util.Log.d("working", "Service created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        android.util.Log.d("working", "Service started");

        // Fake background task
        new Thread(() -> {
            try {
                Thread.sleep(2000); // Pretend processing
                android.util.Log.d("working", " task complete");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        android.util.Log.d("working", "Service destroyed");
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
