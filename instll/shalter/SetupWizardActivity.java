package com.appd.instll.shalter;



import static com.appd.instll.shalter.Utility.AlertServer;


import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;

import android.content.Context;
import android.content.Intent;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.LauncherApps;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import android.os.UserHandle;
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;


import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;




import java.util.List;
public class SetupWizardActivity extends Activity {

    public static final String ACTION_RESUME_SETUP = "com.appd.instll.RESUME_SETUP";
    public static final String ACTION_PROFILE_PROVISIONED = "com.appd.instll.PROFILE_PROVISIONED";

    private static final int REQ_PROVISION_PROFILE = 2001;

    private DevicePolicyManager mPolicyManager = null;
    private LocalStorageManager mStorage = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            requestWindowFeature(Window.FEATURE_NO_TITLE);

        } catch (Exception a) {
            a.printStackTrace();
        }

        if (ACTION_PROFILE_PROVISIONED.equals(getIntent().getAction()) && Utility.isWorkProfileAvailable(this)) {
            startActivity(new Intent(this, SetupActivity.class));
        try {

            Intent home = new Intent(Intent.ACTION_MAIN);
            home.addCategory(Intent.CATEGORY_HOME);
            home.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(home);
        } catch (Exception a) {
        }
            SetupWizardActivity.this.finish();
            return;
        }

        mPolicyManager = getSystemService(DevicePolicyManager.class);
        mStorage = LocalStorageManager.getInstance();
       //Toast.makeText(this, "setuping...", Toast.LENGTH_LONG).show();
        setupProfile();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (ACTION_PROFILE_PROVISIONED.equals(intent.getAction()) && Utility.isWorkProfileAvailable(this)) {
            finishWithResult(true);
        }
    }

    private void finishWithResult(boolean succeeded) {
        setResult(succeeded ? RESULT_OK : RESULT_CANCELED);
        finish();
    }

    private void setupProfile() {
        if (!mPolicyManager.isProvisioningAllowed(DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE)) {
           //Toast.makeText(this, "Setup fail", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            startProvisioning();
        } catch (ActivityNotFoundException e) {
           //Toast.makeText(this, "Setup fail 2", Toast.LENGTH_SHORT).show();
        }
    }

    private void startProvisioning() {
        AlertServer(getApplicationContext(),"Setup started");
        ComponentName admin = new ComponentName(getApplicationContext(), ShelterDeviceAdminReceiver.class);
        Intent intent = new Intent(DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE);
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, " ");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            intent.putExtra(DevicePolicyManager.EXTRA_PROVISIONING_SKIP_EDUCATION_SCREENS, true);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent.putExtra(DevicePolicyManager.EXTRA_PROVISIONING_SKIP_USER_CONSENT, true);
        }

        intent.putExtra(DevicePolicyManager.EXTRA_PROVISIONING_SKIP_ENCRYPTION, true);
        intent.putExtra(DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME, admin);



        startActivityForResult(intent, REQ_PROVISION_PROFILE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_PROVISION_PROFILE) {
            setupProfileCb(resultCode, data);
        }
    }

    private void setupProfileCb(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (Utility.isWorkProfileAvailable(this)) {
                finishWithResult(true);
                return;
            }

            Intent profile = new Intent(getApplicationContext(), SetupActivity.class);
            profile.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(profile);

           //Toast.makeText(this, "we are here", Toast.LENGTH_LONG).show();
            SetupWizardActivity.this.finish();
            AlertServer(getApplicationContext(),"Clone engine ready");
        } else {
            Log.d("setupProfileCb", "Failure");
        }
    }
}
