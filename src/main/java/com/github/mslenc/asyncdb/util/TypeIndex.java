package com.github.mslenc.asyncdb.util;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class TypeIndex<T> {
    private final HashMap<Class<?>, T> directIndex;
    private final LinkedHashMap<Class<?>, T> subclassIndex;
    private final T nullCase;

    private TypeIndex(HashMap<Class<?>, T> directIndex, LinkedHashMap<Class<?>, T> subclassIndex, T nullCase) {
        this.directIndex = directIndex;
        this.subclassIndex = subclassIndex;
        this.nullCase = nullCase;
    }

    public T get(Class<?> klass) {
        T direct = directIndex.get(klass);
        if (direct != null)
            return direct;

        for (Map.Entry<Class<?>, T> entry : subclassIndex.entrySet()) {
            if (entry.getKey().isAssignableFrom(klass))
                return entry.getValue();
        }

        return null;
    }

    public T getFor(Object value) {
        if (value == null) {
            return nullCase;
        } else {
            return get(value.getClass());
        }
    }

    public static <T> Builder<T> newBuilder() {
        return new Builder<>();
    }

    public Builder<T> toBuilder() {
        return new Builder<>(directIndex, subclassIndex, nullCase);
    }

    public static class Builder<T> {
        private HashMap<Class<?>, T> directIndex;
        private LinkedHashMap<Class<?>, T> subclassIndex;
        private T nullCase;

        Builder() {
            directIndex = new HashMap<>();
            subclassIndex = new LinkedHashMap<>();
        }

        Builder(Map<Class<?>, T> directIndex, Map<Class<?>, T> subclassIndex, T nullCase) {
            this.directIndex = new HashMap<>(directIndex);
            this.subclassIndex = new LinkedHashMap<>(subclassIndex);
            this.nullCase = nullCase;
        }

        public Builder add(Class<?> type, T value) {
            if (type.isArray()) {
                directIndex.put(type, value);
            } else {
                if (!Modifier.isAbstract(type.getModifiers())) {
                    directIndex.put(type, value);
                }

                if (!Modifier.isFinal(type.getModifiers())) {
                    subclassIndex.put(type, value);
                }
            }

            return this;
        }

        public Builder setNullCase(T nullCase) {
            this.nullCase = nullCase;
            return this;
        }

        public TypeIndex<T> build() {
            return new TypeIndex<>(new HashMap<>(directIndex), new LinkedHashMap<>(subclassIndex), nullCase);
        }
    }
}
