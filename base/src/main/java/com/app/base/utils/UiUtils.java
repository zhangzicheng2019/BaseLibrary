package com.app.base.utils;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;

import androidx.annotation.DrawableRes;
import androidx.appcompat.app.AlertDialog;
import androidx.core.graphics.drawable.DrawableCompat;

import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.app.base.BaseApplication;

import java.util.List;

public class UiUtils {

    private static int SCREEN_WIDTH = UiUtils.getAppUsableScreenSize(BaseApplication.getApplication()).x;
    private static int SCREEN_HEIGHT = UiUtils.getAppUsableScreenSize(BaseApplication.getApplication()).y;
    private static int STATUS_BAR_HEIGHT = UiUtils.getStatusBarHeight(BaseApplication.getApplication());

    public static int getScreenWidth(){
        if(SCREEN_WIDTH > 0){
            return SCREEN_WIDTH;
        }
        return UiUtils.getAppUsableScreenSize(BaseApplication.getApplication()).x;
    }

    public static int getScreenHeight(){
        if(SCREEN_HEIGHT > 0){
            return SCREEN_HEIGHT;
        }
        return UiUtils.getAppUsableScreenSize(BaseApplication.getApplication()).y;
    }

    public static int getStatusBarHeight(){
        if(STATUS_BAR_HEIGHT > 0){
            return STATUS_BAR_HEIGHT;
        }
        return UiUtils.getStatusBarHeight(BaseApplication.getApplication());
    }

    private static int getStatusBarHeight(Context context) {
        int result = 0;
        if (context == null) {
            return result;
        }
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static Rect getTextBounds(String text, Paint paint){
        Rect rect = new Rect();
        paint.getTextBounds(text, 0, text.length(), rect);
        return rect;
    }

    public static int dpToPx(Context context, float dp) {
        return (int) ((dp * context.getResources().getDisplayMetrics().density) + 0.5);
    }

    public static int spToPx(Context context, float sp) {
        return (int) ((sp * context.getResources().getDisplayMetrics().scaledDensity) + 0.5);
    }

    public static void resetBannerPageTransformer(List<? extends View> views) {
        if (views == null) {
            return;
        }

        for (View view : views) {
            view.setVisibility(View.VISIBLE);
            view.setAlpha(1);
            view.setPivotX(view.getMeasuredWidth() * 0.5f);
            view.setPivotY(view.getMeasuredHeight() * 0.5f);
            view.setTranslationX(0);
            view.setTranslationY(0);
            view.setScaleX(1);
            view.setScaleY(1);
            view.setRotationX(0);
            view.setRotationY(0);
            view.setRotation(0);
        }
    }

    public static void setupStatusBar(Activity activity, View vToolbar) {
        setupStatusBar(activity.getWindow(), vToolbar);
    }

    public static void setupStatusBar(Window window, View vToolbar) {
        if (window == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            );
            window.setStatusBarColor(Color.TRANSPARENT);
            if (vToolbar != null) {
                vToolbar.getLayoutParams().height = dpToPx(window.getContext(), 44) + getStatusBarHeight();
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            if (vToolbar != null) {
                vToolbar.getLayoutParams().height = dpToPx(window.getContext(), 44) + getStatusBarHeight();
            }
        }
    }

    public static void showToastLong(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

    public static void showToastShort(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public static Point getAppUsableScreenSize(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }

    public static boolean isActRunning(Context uiContext){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return uiContext instanceof Activity && !((Activity) uiContext).isFinishing() && !((Activity) uiContext).isDestroyed();
        } else {
            return uiContext instanceof Activity && !((Activity) uiContext).isFinishing();
        }
    }

    public static void showAffirmSystemDialog(Context context, @DrawableRes int iconId, String title, String message){
        showSystemDialog(context, title, message, "", null, "确认", null, true, iconId);
    }

    public static void showSystemDialog(Context context, String title, String message, DialogInterface.OnClickListener negativeOnClickListener,
                                        DialogInterface.OnClickListener positiveOnClickListener, boolean cancelable){
        showSystemDialog(context, title, message, "取消", negativeOnClickListener, "确认", positiveOnClickListener, cancelable, 0);
    }

    public static void showSystemDialog(Context context, String title, String message, String negativeText, DialogInterface.OnClickListener negativeOnClickListener,
                                        String positiveText, DialogInterface.OnClickListener positiveOnClickListener, boolean cancelable, @DrawableRes int iconId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setIcon(iconId);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setNegativeButton(negativeText, negativeOnClickListener);
        builder.setPositiveButton(positiveText, positiveOnClickListener);
        builder.setCancelable(cancelable);
        builder.create().show();
    }

    public static void setTintColor(Drawable drawable, ColorStateList tintColor){
        DrawableCompat.wrap(DrawableCompat.unwrap(drawable)).mutate();
        DrawableCompat.setTintList(drawable, tintColor);
    }

    public static void setTintColor(Drawable drawable, int tintColor){
        DrawableCompat.wrap(DrawableCompat.unwrap(drawable)).mutate();
        DrawableCompat.setTint(drawable, tintColor);
    }
}
