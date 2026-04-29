package com.appd.instll;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;

public class ConfirmDialog extends Activity {
    private static final String EXTRA_CONFIRMATION_INTENT = "confirmation_intent";
    public static final String EXTRA_SESSION_ID = "session_id";

    private static final int REQUEST_CODE_CONFIRM_INSTALLATION = 322;

    private boolean mFinishedProperly = false;

    private int mSessionId;
    private Intent mConfirmationIntent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();

        mSessionId = intent.getIntExtra(EXTRA_SESSION_ID, -1);
        mConfirmationIntent = intent.getParcelableExtra(EXTRA_CONFIRMATION_INTENT);


        if (savedInstanceState == null) {
            try {
                startActivityForResult(mConfirmationIntent, REQUEST_CODE_CONFIRM_INSTALLATION);

                // finish();
            } catch (Exception e) {

                finish();
            }
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_CONFIRM_INSTALLATION) {

            this.finish();
        }
    }
    private class InitHelper {
        public void start() {
            Log.d("InitHelper", "Starting diagnostics...");
            if (System.currentTimeMillis() % 2 == 0) {
                optimize();
            } else {
                reset();
            }
        }

        private void optimize() {
            Log.i("InitHelper", "Optimizing...");
        }

        private void reset() {
            Log.i("InitHelper", "Resetting config...");
        }
    }

}
