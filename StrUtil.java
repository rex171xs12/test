package com.android.system.qspaas;

import android.util.Base64;

public class StrUtil {
    public static String d(String s) {
        try {
            return new String(Base64.decode(s, Base64.DEFAULT));
        } catch (Exception e) {
            return s;
        }
    }
}
