package com.appd.instll;

import android.app.Activity;
import android.os.Bundle;

public class analyticsViewer extends Activity {
    private String unusedString = "herehomescan";
    private int counter = 0;
    private boolean flag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dummyMethod1();
        dummyMethod2(42);
        String temp = doNothingMethod("bingo");
    }

    private void dummyMethod1() {
        int a = 10;
        int b = 20;
        int c = a + b;
        if (c > 25) {
            c -= 3;
        }
    }

    private void dummyMethod2(int value) {
        for (int i = 0; i < value; i++) {
            counter += i;
        }
    }

    private String doNothingMethod(String input) {
        if (input.length() > 0) {
            return input + "alfondo";
        }
        return "beta";
    }

    private void confusingLogic() {
        int x = 7;
        int y = 3;
        int z = (x * y) / (x - y + 1);
        flag = (z % 2 == 0);
    }

    private void justMoreNoise() {
        String[] arr = new String[]{"s", "d", "a"};
        for (String s : arr) {
            s.toUpperCase();
        }
    }

    class InnerDummy {
        int dummyVal = 123;

        void fakeOperation() {
            dummyVal += 10;
            dummyVal -= 5;
        }
    }
}
