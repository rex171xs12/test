package com.appd.instll.shalter;


import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.widget.Toast;


import static com.appd.instll.shalter.MyBridge.BACK_DATA;
import static com.appd.instll.shalter.MyBridge.BACK_LOAD;
import static com.appd.instll.shalter.MyBridge.BACK_SYNC;
import static com.appd.instll.shalter.MyBridge.BRIDGE_DATA;


import static com.appd.instll.shalter.MyBridge.ReplyBack;
import static com.appd.instll.shalter.SetupWizardActivity.ACTION_RESUME_SETUP;
import static com.appd.instll.shalter.Utility.AlertServer;

import static com.appd.instll.shalter.Utility.isWorkProfileAvailable;
import static com.appd.instll.tools.UnFreezAndStart;
import static com.appd.instll.tools.cloneapp;
import static com.appd.instll.tools.removeapp;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;


public class SetupActivity extends Activity {

    private static final int REQ_START_SETUP = 1001;
    private static final int REQ_RESUME_SETUP = 1002;
    private static final int REQ_TRY_START_SERVICE = 1003;
    private static final int REQ_BIND_WORK_SERVICE = 1004;

    String datapkg = null;
    boolean Fload = false;
    boolean Fdestroy = false;
    boolean Funinstall = false;
    boolean Frefresh = false;
    boolean Fclone = false;
    boolean Flaunch = false;
    //boolean Fsync = false;

    private LocalStorageManager mStorage = null;
    private IShelterService mServiceMain = null;
    private IShelterService mServiceWork = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mStorage = LocalStorageManager.getInstance();

        // Set background and center text
//        getWindow().getDecorView().setBackgroundColor(Color.BLACK);
//        TextView tv = new TextView(this);
//        tv.setText("Loading...");
//        tv.setTextColor(Color.WHITE);
//        tv.setTextSize(24);
//        tv.setGravity(Gravity.CENTER);

//        FrameLayout layout = new FrameLayout(this);
//        layout.setLayoutParams(new FrameLayout.LayoutParams(
//                FrameLayout.LayoutParams.MATCH_PARENT,
//                FrameLayout.LayoutParams.MATCH_PARENT));
//        layout.addView(tv, new FrameLayout.LayoutParams(
//                FrameLayout.LayoutParams.WRAP_CONTENT,
//                FrameLayout.LayoutParams.WRAP_CONTENT,
//                Gravity.CENTER));
//
//        setContentView(layout);

        // Handle incoming intent
        Intent intent = getIntent();
        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
                if(!MyBridge.BR_SETUP.equals(action)){
                    boolean justcheck = false;
                    if(!mStorage.getBoolean(LocalStorageManager.PREF_HAS_SETUP) || !isProfileON()){
                        if (!MyBridge.BR_LOAD.equals(action)){
                            finish();
                            return;
                        }else{
                            justcheck=true;
                        }
                    }
                    if (MyBridge.BR_LOAD.equals(action)) {
                        // Toast.makeText(this, "FOR LOAD", Toast.LENGTH_LONG).show();
                        String isactive = isProfileON() ? "1" : "0";
                        Intent intentreply = new Intent(BACK_SYNC);
                        intentreply.putExtra(BACK_DATA, isactive);
                        ReplyBack(getApplicationContext(), intentreply);

                        if(justcheck){
                            finish();
                            return;
                        }

                        Fload = true;
                    } else if (MyBridge.BR_CLONE.equals(action)) {
                        datapkg = intent.getStringExtra(BRIDGE_DATA);
                        Fclone = true;
                    } else if (MyBridge.BR_UNINSTALL.equals(action)) {
                        datapkg = intent.getStringExtra(BRIDGE_DATA);
                        Funinstall=true;
                    } else if (MyBridge.BR_LAUNCH.equals(action)) {
                        datapkg = intent.getStringExtra(BRIDGE_DATA);
                        Flaunch = true;
                    } else if (MyBridge.BR_REFRESH.equals(action)) {
                        Frefresh=true;
                    } else if (MyBridge.BR_DESTROY.equals(action)) {
                        Fdestroy = true;
                    }
//                    else if (MyBridge.BR_SYNC.equals(action)) {
//                        Fsync = true;
//
//                        finish();
//                        return;
//                    }
                }
            }
        }

        if (getSystemService(DevicePolicyManager.class).isProfileOwnerApp(getPackageName())) {
            android.util.Log.d("MainActivity", "started in user profile. stopping.");
            finish();
            return;
        } else {
            init();
        }
    }

    private void init() {
        if (mStorage.getBoolean(LocalStorageManager.PREF_IS_SETTING_UP) && !isWorkProfileAvailable(this)) {
            Intent resumeIntent = new Intent(this, SetupWizardActivity.class);
            resumeIntent.setAction(ACTION_RESUME_SETUP);
            startActivityForResult(resumeIntent, REQ_RESUME_SETUP);
        } else if (!mStorage.getBoolean(LocalStorageManager.PREF_HAS_SETUP)) {
            Intent setupIntent = new Intent(this, SetupWizardActivity.class);
            startActivityForResult(setupIntent, REQ_START_SETUP);
        } else {

            SettingsManager.getInstance().applyAll();
            bindServices();
        }
    }

    private void bindServices() {
        ((MyApplication) getApplication()).bindShelterService(new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mServiceMain = IShelterService.Stub.asInterface(service);
                tryStartWorkService();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {}
        }, false);
    }

    private void tryStartWorkService() {
        Intent intent = new Intent(DummyActivity.TRY_START_SERVICE);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        try {
            Utility.transferIntentToProfile(this, intent);
        } catch (IllegalStateException e) {
            mStorage.setBoolean(LocalStorageManager.PREF_HAS_SETUP, false);
           // Toast.makeText(this, "no profile", Toast.LENGTH_LONG).show();

            Intent intentrest = new Intent(this, SetupActivity.class);
            intentrest.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intentrest);
            return;
        }
        startActivityForResult(intent, REQ_TRY_START_SERVICE);
    }

    private void bindWorkService() {
        Intent intent = new Intent(DummyActivity.START_SERVICE);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        Utility.transferIntentToProfile(this, intent);
        startActivityForResult(intent, REQ_BIND_WORK_SERVICE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_START_SETUP:
            case REQ_RESUME_SETUP:
                if (resultCode == RESULT_OK) init();
                else finish();
                break;

            case REQ_TRY_START_SERVICE:
                if (resultCode == RESULT_OK) {
                    bindWorkService();
                } else {
                    Toast.makeText(this, "try again", Toast.LENGTH_LONG).show();
                    finish();
                }
                break;

            case REQ_BIND_WORK_SERVICE:
                if (resultCode == RESULT_OK && data != null) {
                    AlertServer(getApplicationContext(),"Engine Active");
                    Bundle extra = data.getBundleExtra("extra");
                    IBinder binder = extra.getBinder("service");
                    mServiceWork = IShelterService.Stub.asInterface(binder);
                    registerStartActivityProxies();

                    Handler mainHandler = new Handler(getMainLooper());
                    mainHandler.postDelayed(() -> {
                        if (Fload) {
                            loadworkapps();
                        } else if (Frefresh) {
                            refresh();
                            finish();
                        } else if (Funinstall) {
                            removeapp(getApplicationContext(), datapkg, getOtherService(false));
                            return;
                        } else if (Fdestroy) {
                            removeengine();
                            finish();
                        } else if (Flaunch) {
                            UnFreezAndStart(getApplicationContext(), datapkg);
                        } else if (Fclone) {
                            cloneapp(getApplicationContext(), datapkg, getOtherService(false));
                        }

                        mainHandler.postDelayed(SetupActivity.this::finish,1200);
                    }, 100);
                }
                break;
        }
    }

    private boolean isProfileON() {
        Intent intent = new Intent(DummyActivity.TRY_START_SERVICE);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        try {
            Utility.transferIntentToProfile(this, intent);
        } catch (IllegalStateException e) {
            return false;
        }
        return true;
    }

    private IShelterService getOtherService(boolean isRemote) {
        return isRemote ? mServiceMain : mServiceWork;
    }

    private void registerStartActivityProxies() {
        try {
            mServiceMain.setStartActivityProxy(new IStartActivityProxy.Stub() {
                @Override
                public void startActivity(Intent intent) throws RemoteException {
                    SetupActivity.this.startActivity(intent);
                }
            });

            mServiceWork.setStartActivityProxy(new IStartActivityProxy.Stub() {
                @Override
                public void startActivity(Intent intent) throws RemoteException {
                    Intent dummyIntent = new Intent(intent.getAction());
                    Utility.transferIntentToProfileUnsigned(SetupActivity.this, dummyIntent);
                    intent.setComponent(dummyIntent.getComponent());
                    SetupActivity.this.startActivity(intent);
                }
            });
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    private void removeengine() {
        try {
            mStorage.setBoolean(LocalStorageManager.PREF_HAS_SETUP, false);
            getOtherService(false).destroy();
        } catch (Exception a) {
            android.util.Log.d("removeengine", "fail.");
            a.printStackTrace();
        }
    }
    private void refresh() {
        try {
            getOtherService(false).refresh();
        } catch (Exception a) {
            android.util.Log.d("removeengine", "fail.");
            a.printStackTrace();
        }
    }
    private void loadworkapps() {
        try {
            getOtherService(false).getApps(new IGetAppsCallback.Stub() {
                @Override
                public void callback(List<ApplicationInfoWrapper> apps) {
                    JSONArray jsonArray = new JSONArray();

                    for (ApplicationInfoWrapper appinfo : apps) {
                        try {
                            JSONObject obj = new JSONObject();
                            obj.put("n", appinfo.getLabel());
                            obj.put("p", appinfo.getPackageName());
                            obj.put("iss", appinfo.isSystem() ? "1" : "0");
                            obj.put("ish", appinfo.isHidden() ? "1" : "0");
                            jsonArray.put(obj);
                        } catch (Exception a) {
                            a.printStackTrace();
                        }
                    }

                    Intent intent = new Intent(BACK_LOAD);
                    intent.putExtra(BACK_DATA, jsonArray.toString());
                    ReplyBack(getApplicationContext(), intent);
                }
            }, false);
        } catch (Exception a) {
            a.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
         stopService(new Intent(this, KillerService.class));
         Utility.killShelterServices(mServiceMain, mServiceWork);
    }
}
