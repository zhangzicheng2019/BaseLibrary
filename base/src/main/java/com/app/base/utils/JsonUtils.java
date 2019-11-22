package com.app.base.utils;

import android.text.TextUtils;
import android.util.Log;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.module.kotlin.KotlinModule;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Locale;

public final class JsonUtils {
    private static final String TAG = JsonUtils.class.getSimpleName();
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        OBJECT_MAPPER.configure(JsonGenerator.Feature.QUOTE_NON_NUMERIC_NUMBERS, true);
        OBJECT_MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        OBJECT_MAPPER.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()));
        OBJECT_MAPPER.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

//        SimpleModule module = new SimpleModule();
//        module.addSerializer(BigDecimal.class, new JsonSerializer<BigDecimal>() {
//            @Override
//            public void serialize(BigDecimal value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
//                if (value != null) {
//                    jgen.writeString(value.toString());
//                }
//            }
//        });
        OBJECT_MAPPER.registerModule(new KotlinModule());
    }



    public static String toJson(Object o) {
        if (o == null) {
            return null;
        }

        if (String.class.equals(o.getClass())) {
            return (String) o;
        }

        try {
            return OBJECT_MAPPER.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            Log.e(TAG, "json解析错误 --> " + e.toString());
        }
        return null;
    }

    public static <T> T fromJson(String json, TypeReference<T> typeToken) {
        if (TextUtils.isEmpty(json)) {
            return null;
        }

        try {
            return OBJECT_MAPPER.readValue(json, typeToken);
        } catch (IOException e) {
            Log.e(TAG, "json解析错误 --> " + e.toString());
        }
        return null;
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        if (TextUtils.isEmpty(json)) {
            return null;
        }

        if (String.class.equals(clazz)) {
            return (T) json;
        }

        try {
            return OBJECT_MAPPER.readValue(json, clazz);
        } catch (IOException e) {
            Log.e(TAG, "json解析错误 --> " + e.toString());
        }
        return null;
    }

    public static JsonNode fromJson(String json) {
        if (TextUtils.isEmpty(json)) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readTree(json);
        } catch (IOException e) {
            Log.e(TAG, "json解析错误 --> " + e.toString());
        }
        return null;
    }

    /**
     * 获取泛型类型
     *
     * @param clazz 类类型
     * @param index 第几个泛型
     * @return Type
     */
    public static Class<?> getActualTypeParameter(Class clazz, int index) {
        Type superclass = clazz.getGenericSuperclass();
        if (!(superclass instanceof ParameterizedType)) {
            throw new IllegalArgumentException("Generic types cannot be retrieved !");
        }
        ParameterizedType parameter = (ParameterizedType) superclass;
        return (Class<?>)parameter.getActualTypeArguments()[index];
    }


}

