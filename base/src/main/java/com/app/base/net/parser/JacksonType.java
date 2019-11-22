package com.app.base.net.parser;

import com.fasterxml.jackson.core.type.TypeReference;

import java.lang.reflect.Type;

public class JacksonType<T> extends TypeReference<T> {

    private Type mType;

    public JacksonType(Type type){
        mType = type;


    }
    @Override
    public Type getType() {
        return mType;
    }
}
