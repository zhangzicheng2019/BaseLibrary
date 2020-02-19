package com.app.base.utils;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class LogUtils {

    static boolean isDebug = false;
    public static final int HANDLER_WHAT_LOG = 0x120;
    private static final String TAG = LogUtils.class.getSimpleName();

    private static Handler slogHandler = null;

    public static void setLogHandler(Handler logHandler){
        slogHandler = logHandler;
    }

    public static void setDebug(boolean debug){
        isDebug = debug;
    }

    public static void i(String message){
        if(isDebug){
            Log.i(TAG, message);
            if(slogHandler != null){
                slogHandler.sendMessage(Message.obtain(slogHandler, HANDLER_WHAT_LOG, message));
            }
        }
    }

    public static void i(String tag, String message){
        if(isDebug){
            Log.i(tag, message);
            if(slogHandler != null){
                slogHandler.sendMessage(Message.obtain(slogHandler, HANDLER_WHAT_LOG, message));
            }
        }
    }

    public static void d(String message){
        if(isDebug){
            Log.d(TAG, message);
            if(slogHandler != null){
                slogHandler.sendMessage(Message.obtain(slogHandler, HANDLER_WHAT_LOG, message));
            }
        }
    }

    public static void d(String tag, String message){
        if(isDebug){
            Log.d(tag, message);
            if(slogHandler != null){
                slogHandler.sendMessage(Message.obtain(slogHandler, HANDLER_WHAT_LOG, message));
            }
        }
    }

    public static void e(String message){
        if(isDebug){
            Log.e(TAG, message);
            if(slogHandler != null){
                slogHandler.sendMessage(Message.obtain(slogHandler, HANDLER_WHAT_LOG, message));
            }
        }
    }

    public static void e(String message, Throwable throwable){
        if(isDebug){
            Log.e(message, throwable.toString());
            throwable.printStackTrace();
            if(slogHandler != null){
                slogHandler.sendMessage(Message.obtain(slogHandler, HANDLER_WHAT_LOG, message));
            }
        }
    }

    public static void e(String tag, String message, Throwable throwable){
        if(isDebug){
            Log.e(tag, message);
            throwable.printStackTrace();
            if(slogHandler != null){
                slogHandler.sendMessage(Message.obtain(slogHandler, HANDLER_WHAT_LOG, message));
            }
        }
    }

    public static void json(String tag, String json){
        if(isDebug){
            Log.d(tag, json);
            if(slogHandler != null){
                slogHandler.sendMessage(Message.obtain(slogHandler, HANDLER_WHAT_LOG, json));
            }
        }
    }

}
