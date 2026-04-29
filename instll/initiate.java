package com.appd.instll;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;


public class initiate extends Activity {
    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent data = getIntent();
        Intent installIntent = data.getParcelableExtra("install_intent");

        if (installIntent != null) {
            // Make sure to add FLAG_ACTIVITY_NEW_TASK if needed
            installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(installIntent);
            finish();  // Close Initiate after forwarding to the system installer
        } else {
            // Handle error if missing
            Toast.makeText(this, "Install intent not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
