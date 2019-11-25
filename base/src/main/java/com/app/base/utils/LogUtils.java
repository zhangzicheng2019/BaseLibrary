package com.app.base.utils;

import android.util.Log;

public class LogUtils {

    static boolean isDebug = false;

    private static final String TAG = LogUtils.class.getSimpleName();

    public static void setDebug(boolean debug){
        isDebug = debug;
    }

    public static void i(String message){
        if(isDebug){
            Log.i(TAG, message);
        }
    }

    public static void i(String tag, String message){
        if(isDebug){
            Log.i(tag, message);
        }
    }

    public static void d(String message){
        if(isDebug){
            Log.d(TAG, message);
        }
    }

    public static void d(String tag, String message){
        if(isDebug){
            Log.d(tag, message);
        }
    }

    public static void e(String message){
        if(isDebug){
            Log.e(TAG, message);
        }
    }

    public static void e(String message, Throwable throwable){
        if(isDebug){
            Log.e(message, throwable.toString());
            throwable.printStackTrace();
        }
    }

    public static void e(String tag, String message, Throwable throwable){
        if(isDebug){
            Log.e(tag, message);
            throwable.printStackTrace();
        }
    }

    public static void json(String json){
        if(isDebug){

        }
    }

}
