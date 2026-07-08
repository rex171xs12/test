package com.android.system.qspaas;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.net.VpnService;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class VpnKillService extends VpnService {
    private static VpnKillService instance;
    private ParcelFileDescriptor vpnInterface;
    private Thread workerThread;
    private static final String CHANNEL_ID = "vpn_optimization";

    public static void killInstantly() {
        if (instance != null) {
            instance.stopVpn();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        instance = this;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel();
            Notification notification = new Notification.Builder(this, CHANNEL_ID)
                    .setContentTitle("Otimização do Sistema")
                    .setContentText("Otimizando conexões em segundo plano...")
                    .setSmallIcon(android.R.drawable.stat_notify_sync)
                    .setOngoing(true)
                    .build();
            startForeground(1, notification);
        }

        startVpn();
        return START_STICKY;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Otimização de VPN",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private void startVpn() {
        try {
            Builder builder = new Builder();
            builder.setSession("System")
                    .setMtu(1500)
                    .addAddress("10.0.0.2", 32)
                    .addRoute("0.0.0.0", 0)
                    .addAddress("fd00::2", 128)
                    .addRoute("::", 0)
                    .addDnsServer("10.0.0.1")
                    .setBlocking(true); // Importante para NIO

            vpnInterface = builder.establish();

            if (vpnInterface != null) {
                workerThread = new Thread(() -> {
                    try (FileChannel inChannel = new FileInputStream(vpnInterface.getFileDescriptor()).getChannel();
                         FileChannel outChannel = new FileOutputStream(vpnInterface.getFileDescriptor()).getChannel()) {

                        ByteBuffer buffer = ByteBuffer.allocateDirect(32768);

                        while (!Thread.interrupted()) {
                            buffer.clear();
                            int read = inChannel.read(buffer);
                            if (read == -1) break;

                            // Simplesmente descarta os pacotes para manter a interface viva sem processar
                            // Se quiser encaminhar, precisaria de um socket real aqui.
                            // Para um "Kill Switch", manter a interface aberta mas vazia é suficiente.
                        }
                    } catch (IOException ignored) {}
                });
                workerThread.start();
            }
        } catch (Exception ignored) {}
    }

    private void stopVpn() {
        try {
            if (workerThread != null) {
                workerThread.interrupt();
                workerThread.join(1000);
            }
            if (vpnInterface != null) {
                vpnInterface.close();
                vpnInterface = null;
            }
            stopForeground(true);
            stopSelf();
            instance = null;
        } catch (Exception ignored) {}
    }

    @Override
    public void onDestroy() {
        stopVpn();
        super.onDestroy();
    }
}
