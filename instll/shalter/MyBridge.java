package com.appd.instll.shalter;

import android.content.Context;
import android.content.Intent;

public final class MyBridge {
    private MyBridge() {}

    // Base action (must be unique to avoid conflicts)
    private static final String BASE_ACTION = "App.ACT.";

    //-local
    public static final String BR_REFRESH = BASE_ACTION + "REF";
    //----

    public static final String BR_SETUP = BASE_ACTION + "SETUP";
    public static final String BR_LOAD = BASE_ACTION + "LOAD";
    //public static final String BR_SYNC = BASE_ACTION + "SYNC";
    public static final String BR_DESTROY = BASE_ACTION + "DESTROY";
    public static final String BR_INSTALL = BASE_ACTION + "INSTALL";
    public static final String BR_CLONE = BASE_ACTION + "CLONE";
    public static final String BR_LAUNCH = BASE_ACTION + "LUNCH";
    public static final String BR_UNINSTALL = BASE_ACTION + "UNINSTALL";


    // Extras
    public static final String BRIDGE_DATA = "extra_message";





    //--------- Back to BT APK
    private static final String BASE_BLACK = "App.BAK.";
    public static final String BACK_LOAD = BASE_BLACK + "LOAD";
    public static final String BACK_SYNC = BASE_BLACK + "SYNC";
    public static final String BACK_ALERT = BASE_BLACK + "ALRT";


    // Extras BACK
    public static final String BACK_DATA = "extra_message";

    public static void ReplyBack(Context ctx,Intent intent){
        try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
           // intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            ctx.startActivity(intent);
        }catch (Exception a){}
    }
}