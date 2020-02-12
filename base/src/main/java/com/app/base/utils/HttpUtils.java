package com.app.base.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.text.TextUtils;
import com.app.base.BaseApplication;
import com.app.base.net.http.UpFile;
import io.reactivex.annotations.NonNull;
import okhttp3.*;
import okhttp3.logging.HttpLoggingInterceptor;
import org.json.JSONObject;

import java.net.URLConnection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class HttpUtils {

    public static final String HEADER_KEY_ACCEPT = "Accept";
    public static final String ACCEPT_TYPE_JSON = "application/json";

    public static final String HEADER_KEY_CONTENT_TYPE ="Content-Type";
    public static final String CONTENT_TYPE_JSON ="application/json";

    public static final String HEADER_KEY_AES_KEY ="X-AESKey";

    private static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json;charset=utf-8");

    private static final OkHttpClient OK_HTTP_CLIENT;

    static {
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
        clientBuilder.connectTimeout(15, TimeUnit.SECONDS);
        clientBuilder.readTimeout(30, TimeUnit.SECONDS);
        clientBuilder.writeTimeout(30, TimeUnit.SECONDS);
        if (LogUtils.isDebug) {
            HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
            httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            clientBuilder.addInterceptor(httpLoggingInterceptor);
        }
//        clientBuilder.addInterceptor(new RetryInterceptor(3));
        OK_HTTP_CLIENT = clientBuilder.build();
    }

    public static OkHttpClient getOkHttpClient(){
        return OK_HTTP_CLIENT;
    }

    public static void addOkHttpIntercept(Interceptor... interceptors){
        if(interceptors == null){
            return;
        }
        for (Interceptor interceptor : interceptors){
            OK_HTTP_CLIENT.interceptors().add(interceptor);
        }
    }

    /**
     * map对象转Json字符串
     *
     * @param map map对象
     * @return Json 字符串
     */
    public static <K, V> String mapToJson(@NonNull Map<K, V> map) {
        return new JSONObject(map).toString();
    }


    private static Request.Builder buildPostRequestBuilder(String url, String json, Map<String, String> headers) {
        Request.Builder builder = new Request.Builder().url(url);
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                builder.addHeader(key, value);
            }
        }
        if (!TextUtils.isEmpty(json)) {
            builder.post(RequestBody.create(MEDIA_TYPE_JSON, json));
        }
        return builder;
    }

    /**
     * 所有参数以 key=value 格式拼接(用 & 拼接)在一起并返回
     *
     * @param map Map集合
     * @return 拼接后的字符串
     */
    public static <K, V> String toKeyValue(Map<K, V> map) {
        if (map == null || map.size() == 0) return "";
        Iterator<Map.Entry<K, V>> i = map.entrySet().iterator();
        if (!i.hasNext()) return "";
        StringBuilder builder = new StringBuilder();
        while (true) {
            Map.Entry<K, V> e = i.next();
            builder.append(e.getKey())
                    .append("=")
                    .append(e.getValue());
            if (!i.hasNext())
                return builder.toString();
            builder.append("&");
        }
    }

    /**
     * 组合url及参数 格式:mUrl?key=value...
     *
     * @param url url链接
     * @param map map集合
     * @return 拼接后的字符串
     */
    public static <K, V> String mergeUrlAndParams(@NonNull String url, Map<K, V> map) {
        if (map == null || map.size() == 0) return url;
        Uri.Builder builder = Uri.parse(url).buildUpon();
        for (Map.Entry<K, V> e : map.entrySet()) {
            builder.appendQueryParameter(e.getKey().toString(), e.getValue().toString());
        }
        return builder.toString();
    }

    /**
     * 构建一个表单 (不带文件)
     *
     * @param map map参数集合
     * @return RequestBody
     */
    public static <K, V> RequestBody buildFormRequestBody(@NonNull Map<K, V> map) {
        FormBody.Builder builder = new FormBody.Builder();
        for (Map.Entry<K, V> entry : map.entrySet()) {
            builder.add(entry.getKey().toString(), entry.getValue().toString());
        }
        return builder.build();
    }

    /**
     * 构建一个表单(带文件)
     *
     * @param map      map参数集合
     * @param fileList 文件列表
     * @return RequestBody
     */
    public static <K, V> RequestBody buildFormRequestBody(@NonNull Map<K, V> map,
                                                          @NonNull List<UpFile> fileList) {
        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.setType(MultipartBody.FORM);
        //遍历参数
        for (Map.Entry<K, V> entry : map.entrySet()) {
            builder.addFormDataPart(entry.getKey().toString(), entry.getValue().toString());
        }
        //遍历文件
        for (UpFile file : fileList) {
            if (!file.exists() || !file.isFile()) continue;
            RequestBody requestBody = RequestBody.create(getMediaType(file.getName()), file);
            builder.addFormDataPart(file.getKey(), file.getValue(), requestBody);
        }
        return builder.build();
    }

    private static MediaType getMediaType(String fName) {
        String contentType = URLConnection.guessContentTypeFromName(fName);
        if (TextUtils.isEmpty(contentType)) {
            contentType = "application/octet-stream";
        }
        return MediaType.parse(contentType);
    }

    /**
     * 判断是否有网络连接
     *
     * @return boolean
     */
    public static boolean isNetworkConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) BaseApplication.getApplication()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager != null){
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo != null) {
                return networkInfo.isConnected();
            }
        }
        return false;
    }

    public static boolean isWifiConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) BaseApplication.getApplication()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager != null){
            NetworkInfo wifiNetworkInfo = connectivityManager
                    .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (wifiNetworkInfo != null) {
                return wifiNetworkInfo.isConnected();
            }
        }
        return false;
    }

    public static boolean isMobileConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) BaseApplication.getApplication()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager != null){
            NetworkInfo mobileNetworkInfo = connectivityManager
                    .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if (mobileNetworkInfo != null) {
                return mobileNetworkInfo.isConnected();
            }
        }
        return false;
    }

    public static int getConnectedType(Context context) {
        if (context != null) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);

            if(connectivityManager != null){
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    return networkInfo.getType();
                }
            }
        }
        return -1;
    }
}
