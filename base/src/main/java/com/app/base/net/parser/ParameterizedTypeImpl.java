package com.app.base.net.parser;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class ParameterizedTypeImpl implements ParameterizedType {

    private final Class raw;
    private final Type[] args;
    private final Type owner;

    public ParameterizedTypeImpl(Class raw, Type... args) {
        this(null, raw, args);
    }

    public ParameterizedTypeImpl(Type owner, Class raw,  Type... args) {
        this.owner = owner;
        this.raw = raw;
        this.args = args != null ? args : new Type[0];
    }

    @Override
    public Type[] getActualTypeArguments() {
        return args;
    }

    @Override
    public Type getRawType() {
        return raw;
    }

    @Override
    public Type getOwnerType() {
        return owner;
    }
}