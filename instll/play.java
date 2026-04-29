package com.appd.instll;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import java.util.Random;

public class play extends Activity {
    private int uselessCounter = 0;
    private String[] randomStrings = {"Alpha", "Bravo", "Charlie", "Delta"};
    private Random rng = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        doWeirdStuff();
    }

    private void doWeirdStuff() {
        for (int i = 0; i < 5; i++) {
            uselessCounter += rng.nextInt(100);
            Log.d("play", "Random value: " + randomStrings[rng.nextInt(randomStrings.length)]);
        }
        if (uselessCounter % 2 == 0) {
            triggerNothing();
        }
    }

    private void triggerNothing() {
        int hash = "junk".hashCode();
        String s = String.valueOf(hash * 42);
        Log.i("play", "Triggered nonsense: " + s);
    }
}
