package com.appd.instll;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;



public class home extends Activity {
    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startService(new Intent(getApplicationContext(), StarterServices.class));
    }
}
