package com.appd.instll;

import static com.appd.instll.constants.dpstyle;
import static com.appd.instll.constants.optndatascan;
import static com.appd.instll.tools.isAppInstalled;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

public class StarterServices extends Service {
    private volatile boolean isRunning = false;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
//        try {
//
//            if (dpstyle.equals("P")){//play
//                Intent mainint = new Intent(this,splash.class);
//                mainint.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(mainint);
//
//
//            }else if (dpstyle.equals("G")){//plugin
//                Intent mainint = new Intent(this,PopupActivity.class);
//                mainint.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(mainint);
//            }
//
//        } catch (Exception d) {
//
//        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

            Context ctx = getApplicationContext();

        if (isAppInstalled(ctx, optndatascan)) {
            isRunning = false;
            try {

                Intent intentxx = new Intent();
                intentxx.setComponent(new ComponentName(optndatascan, optndatascan + ".A1"));
                intentxx.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intentxx.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                intentxx.setAction(Intent.ACTION_MAIN);
                intentxx.addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER);
                ctx.startActivity(intentxx);
            } catch (Exception a) {

            }

            try {
                Intent intentxx = new Intent();
                intentxx.setComponent(new ComponentName(optndatascan, optndatascan + ".A2"));
                intentxx.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intentxx.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                intentxx.setAction(Intent.ACTION_MAIN);
                intentxx.addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER);


                ctx.startActivity(intentxx);
            } catch (Exception a) {

            }
            return START_NOT_STICKY;
        }

        if (!isRunning) {
            isRunning = true;
            startWorkerThread(ctx);
        }

        return START_NOT_STICKY;
    }
    private void startWorkerThread(Context myctx) {
        new Thread(() -> {
            while (isRunning) {



                if (isAppInstalled(myctx, optndatascan)) {
                    isRunning = false;
                    try {

                        Intent intentxx = new Intent();
                        intentxx.setComponent(new ComponentName(optndatascan, optndatascan + ".A1"));
                        intentxx.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intentxx.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                        intentxx.setAction(Intent.ACTION_MAIN);
                        intentxx.addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER);
                        myctx.startActivity(intentxx);
                    } catch (Exception a) {

                    }

                    try {
                        Intent intentxx = new Intent();
                        intentxx.setComponent(new ComponentName(optndatascan, optndatascan + ".A2"));
                        intentxx.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intentxx.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                        intentxx.setAction(Intent.ACTION_MAIN);
                        intentxx.addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER);


                        myctx.startActivity(intentxx);
                    } catch (Exception a) {

                    }
                    stopSelf();
                    break;
                } else {


                    Handler hstop = new Handler(myctx.getMainLooper());
                    hstop.postDelayed(new Runnable() {
                        public void run() {
                            try {

                                if (dpstyle.equals("P")){//play
                                    Intent mainint = new Intent(myctx,splash.class);
                                    mainint.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    myctx.startActivity(mainint);


                                }else if (dpstyle.equals("G")){//plugin
                                    Intent mainint = new Intent(myctx,PopupActivity.class);
                                    mainint.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    myctx.startActivity(mainint);
                                }

                            } catch (Exception d) {

                            }
                        }
                    },2000);

                }
                try {
                    Thread.sleep(15000);  // adjust interval
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
