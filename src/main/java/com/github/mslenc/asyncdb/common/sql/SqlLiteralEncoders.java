package com.github.mslenc.asyncdb.common.sql;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class SqlLiteralEncoders {
    private final HashMap<Class<?>, SqlLiteralEncoder> directIndex;
    private final LinkedHashMap<Class<?>, SqlLiteralEncoder> subclassIndex;

    private SqlLiteralEncoders(HashMap<Class<?>, SqlLiteralEncoder> directIndex, LinkedHashMap<Class<?>, SqlLiteralEncoder> subclassIndex) {
        this.directIndex = directIndex;
        this.subclassIndex = subclassIndex;
    }

    public SqlLiteralEncoder getEncoderFor(Object value) {
        if (value == null)
            return NullLiteralEncoder.instance();

        Class<?> klass = value.getClass();

        SqlLiteralEncoder direct = directIndex.get(klass);
        if (direct != null)
            return direct;

        for (Map.Entry<Class<?>, SqlLiteralEncoder> entry : subclassIndex.entrySet()) {
            if (entry.getKey().isAssignableFrom(klass))
                return entry.getValue();
        }

        return null;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        private HashMap<Class<?>, SqlLiteralEncoder> directIndex = new HashMap<>();
        private LinkedHashMap<Class<?>, SqlLiteralEncoder> subclassIndex = new LinkedHashMap<>();

        public Builder add(SqlLiteralEncoder encoder) {
            for (Class<?> klass : encoder.supportedClasses()) {
                if (klass.isArray()) {
                    directIndex.put(klass, encoder);
                } else {
                    if (!Modifier.isAbstract(klass.getModifiers())) {
                        directIndex.put(klass, encoder);
                    }

                    if (!Modifier.isFinal(klass.getModifiers())) {
                        subclassIndex.put(klass, encoder);
                    }
                }
            }

            return this;
        }

        public SqlLiteralEncoders build() {
            return new SqlLiteralEncoders(new HashMap<>(directIndex), new LinkedHashMap<>(subclassIndex));
        }
    }

    public static final SqlLiteralEncoders DEFAULT =
        newBuilder()
            .add(StringLiteralEncoder.instance())
            .add(BigDecimalLiteralEncoder.instance())
            .add(BigIntegerLiteralEncoder.instance())
            .add(NumberLiteralEncoder.instance())
            .add(BooleanLiteralEncoder.instance())
            .add(ByteArrayLiteralEncoder.instance())
            .add(ByteBufLiteralEncoder.instance())
            .add(ByteBufferLiteralEncoder.instance())
            .add(DurationLiteralEncoder.instance())
            .add(InstantLiteralEncoder.instance())
            .add(LocalDateLiteralEncoder.instance())
            .add(LocalDateTimeLiteralEncoder.instance())
            .add(LocalTimeLiteralEncoder.instance())
            .add(YearLiteralEncoder.instance())
            .build();
}
