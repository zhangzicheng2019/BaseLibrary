package com.app.base.ui.view;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.util.AttributeSet;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.app.base.R;
import com.app.base.utils.AppUtils;
import com.app.base.utils.HttpUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName BasicWebView
 * @Description 基础webiew
 * @Author zhangzicheng
 * @Date 2019/11/18 15:54
 */
public class BasicWebView extends WebView {


    public static final String HTTP_ACCEPT_LANGUAGE = "Accept-Language";

    public BasicWebView(Context context) {
        super(getFixedContext(context));
        init();
    }

    public BasicWebView(Context context, AttributeSet attrs) {
        super(getFixedContext(context), attrs);
        init();
    }

    public BasicWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(getFixedContext(context), attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public BasicWebView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(getFixedContext(context), attrs, defStyleAttr, defStyleRes);
        init();
    }

    public BasicWebView(Context context, AttributeSet attrs, int defStyleAttr, boolean privateBrowsing) {
        super(getFixedContext(context), attrs, defStyleAttr, privateBrowsing);
        init();
    }

    private static Context getFixedContext(Context context) {
        if (Build.VERSION.SDK_INT >= 21 && Build.VERSION.SDK_INT < 23) // Android Lollipop 5.0 & 5.1
            return context.createConfigurationContext(new Configuration());
        return context;
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void init() {

        WebSettings webSettings = getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setUserAgentString(getContext().getString(R.string.text_user_agent, webSettings.getUserAgentString(), AppUtils.getVersionName(getContext())));
        //This can lead to the webview's 'rem' works correctly and disable text scale settings as 'sp' works.
        webSettings.setTextZoom(100);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //http://stackoverflow.com/questions/28626433/android-webview-blocks-redirect-from-https-to-http
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            //http://stackoverflow.com/questions/11318703/access-control-allow-origin-error-at-android-4-1
            webSettings.setAllowUniversalAccessFromFileURLs(true);
        }

//        //Set token / refresh_token cookie if the host is nestia and user has logged in.
//        CookieManager cookieManager = CookieManager.getInstance();
//        if (TokenManager.getInstance().isLogin()) {
//            cookieManager.setCookie(NESTIA_HOST, String.format(Locale.getDefault(), "token=%s;max-age=86400;", TokenManager.getInstance().getAccessToken()));
//            cookieManager.setCookie(NESTIA_HOST, String.format(Locale.getDefault(), "refresh_token=%s;max-age=86400;", TokenManager.getInstance().getRefreshToken()));
//        } else {
//            cookieManager.setCookie(NESTIA_HOST, "token=;max-age=0;");
//            cookieManager.setCookie(NESTIA_HOST, "refresh_token=;max-age=0;");
//        }
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            cookieManager.flush();
//        } else {
//            CookieSyncManager.createInstance(getContext()).sync();
//        }

    }

    @Override
    public void loadUrl(String url) {
        loadUrl(url, null);
    }

    @Override
    public void loadUrl(String url, Map<String, String> additionalHttpHeaders) {
        //Set cache mode.
        if (HttpUtils.isNetworkConnected()) {
            getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        } else {
            getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        }

        //Set additional header
        if (additionalHttpHeaders == null) {
            additionalHttpHeaders = new HashMap<>();
        }
        additionalHttpHeaders.put(HTTP_ACCEPT_LANGUAGE, getContext().getString(R.string.text_language));

        super.loadUrl(url, additionalHttpHeaders);
    }
}
