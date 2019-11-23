package com.app.base.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.app.base.BaseApplication;

public class SPUtils {

    private static SharedPreferences sPreferences;

    public static final String SP_KEY_INSTALLED = "installed";

    private SPUtils() {
        // nothing
    }

    public static void init(String fileName){
        sPreferences = BaseApplication.getApplication().getSharedPreferences(fileName, Context.MODE_PRIVATE);
    }

    public static void put(String key, @NonNull Object object) {
        checkInit();
        SharedPreferences.Editor editor = sPreferences.edit();
        if (object instanceof String) {
            editor.putString(key, (String) object);
        } else if (object instanceof Integer) {
            editor.putInt(key, (Integer) object);
        } else if (object instanceof Boolean) {
            editor.putBoolean(key, (Boolean) object);
        } else if (object instanceof Float) {
            editor.putFloat(key, (Float) object);
        } else if (object instanceof Long) {
            editor.putLong(key, (Long) object);
        }
        editor.apply();
    }

    public static String getString(String key, String defaultValue) {
        checkInit();
        return sPreferences.getString(key, defaultValue);
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        checkInit();
        return sPreferences.getBoolean(key, defaultValue);
    }

    public static int getInt(String key, int defaultValue) {
        checkInit();
        return sPreferences.getInt(key, defaultValue);
    }

    public static long getLong(String key, long defaultValue) {
        checkInit();
        return sPreferences.getLong(key, defaultValue);
    }

    public static void remove(String key) {
        checkInit();
        sPreferences.edit().remove(key).apply();
    }

    public static boolean contains(String key) {
        checkInit();
        return sPreferences.contains(key);
    }

    private static void checkInit(){
        if(sPreferences == null){
            throw new  RuntimeException("SPUtils is not initialized !");
        }
    }

}
