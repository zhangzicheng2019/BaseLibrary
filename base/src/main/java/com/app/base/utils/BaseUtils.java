package com.app.base.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.provider.MediaStore;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class BaseUtils {

    private static final int TEN = 10;
    private static final int THOUSAND = 10000;
    private static final int MILLION = 1000000;

    public static String getCountStr(float count) {
        if(count == 0){
            return "";
        }
        if (count < THOUSAND) {
            return String.valueOf((int) count);
        } else if (count < MILLION) {
            return ((float) (Math.round((count / THOUSAND) * TEN))) / TEN + "W";
        } else {
            return ((float) (Math.round((count / MILLION) * TEN))) / TEN + "M";
        }
    }

    public static void showKeyboard(EditText etName) {
        if (etName == null) {
            return;
        }
        etName.requestFocus();
        InputMethodManager inputMethodManager = (InputMethodManager) etName.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.showSoftInput(etName, InputMethodManager.SHOW_IMPLICIT);
    }

    public static void forceHideKeyboard(Context context, EditText editText) {
        ((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(editText.getWindowToken(), 0);

    }

    /**
     * check string is numeric or not
     *
     * @param str String
     * @return boolean
     */
    public static boolean isNumeric(String str) {
        try {
            double d = Double.parseDouble(str);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static boolean saveImageToLocal(Context context, Bitmap bitmap) {

        File imageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "niuniu");
        if (!imageDir.exists()) {
            imageDir.mkdirs();
        }
        String imageName = System.currentTimeMillis() + ".jpg";
        File imageFile = new File(imageDir, imageName);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            //insert image to System photo album
            MediaStore.Images.Media.insertImage(context.getContentResolver(),
                    imageFile.getAbsolutePath(), imageName, "niuniu");
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    public static String getSystemModel() {
        return android.os.Build.MODEL;
    }

    /**
     * 获取手机IMEI(需要“android.permission.READ_PHONE_STATE”权限)
     *
     * @return  手机IMEI
     */
    public static String getIMEI(Context ctx) {
        TelephonyManager tm = (TelephonyManager) ctx.getSystemService(Activity.TELEPHONY_SERVICE);
        if (tm != null) {
            return tm.getDeviceId();
        }
        return null;
    }

    public static byte[] getBitmapByte(Bitmap bitmap){
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        try {
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return out.toByteArray();
    }


    public static Bitmap getBitmapFromByte(byte[] temp){
        if(temp != null){
            Bitmap bitmap = BitmapFactory.decodeByteArray(temp, 0, temp.length);
            return bitmap;
        }else{
            return null;
        }
    }

    public static boolean isValidPhoneNumber(CharSequence target) {
        if (TextUtils.isEmpty(target)) {
            return false;
        } else {
            return android.util.Patterns.PHONE.matcher(target).matches();
        }
    }

    public static boolean isValidEmail(CharSequence target) {
        if (TextUtils.isEmpty(target)) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }
}
