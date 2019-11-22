package com.app.base.utils;

import android.util.Base64;

public class Base64Utils {


    public static byte[] decode(String base64) throws Exception {
        return Base64.decode(base64.getBytes(), Base64.DEFAULT);
    }

    public static String encode(byte[] bytes) throws Exception {
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }


}
