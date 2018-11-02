package com.github.mslenc.asyncdb.my.encoders;

import com.github.mslenc.asyncdb.DbValue;
import com.github.mslenc.asyncdb.util.SqlQueryPlaceholders;
import com.github.mslenc.asyncdb.util.ULong;
import com.github.mslenc.asyncdb.util.TypeIndex;
import io.netty.buffer.ByteBuf;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.time.*;
import java.util.Optional;

public class MyEncoders implements SqlQueryPlaceholders.SqlLiteralEncoder {
    private final TypeIndex<MyValueEncoder<?>> types;

    public MyEncoders(TypeIndex<MyValueEncoder<?>> types) {
        this.types = types;
    }

    public <T> MyValueEncoder<? super T> encoderFor(T value) {
        @SuppressWarnings("unchecked")
        MyValueEncoder<? super T> result = (MyValueEncoder<? super T>) types.getFor(value);

        if (result == null) {
            if (value == null) {
                throw new UnsupportedOperationException("Encoding null values not supported");
            } else {
                throw new UnsupportedOperationException("Encoding values of " + value.getClass() + " not supported");
            }
        }

        return result;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(types.toBuilder());
    }

    //                        in ASCII/UTF8:  N  U  L  L
    private static final int NULL_AS_INT = 0x4E_55_4C_4C;

    public <T> void encodeValue(T value, ByteBuf out) {
        if (value == null) {
            out.writeInt(NULL_AS_INT);
            return;
        }

        MyValueEncoder<? super T> encoder = encoderFor(value);
        if (encoder == null)
            throw new IllegalArgumentException("Unsupported value of class " + value.getClass().getCanonicalName());

        if (encoder.isNull(value, this)) {
            out.writeInt(NULL_AS_INT);
            return;
        }

        encoder.encodeAsSql(value, out, this);
    }

    static class Builder {
        private final TypeIndex.Builder<MyValueEncoder<?>> types;

        Builder() {
            this.types = TypeIndex.newBuilder();
        }

        Builder(TypeIndex.Builder<MyValueEncoder<?>> builder) {
            this.types = builder;
        }

        public <T> Builder add(Class<T> klass, MyValueEncoder<? super T> encoder) {
            types.add(klass, encoder);
            return this;
        }

        public MyEncoders build() {
            return new MyEncoders(types.build());
        }
    }

    public static final MyEncoders DEFAULT =
        newBuilder().
            add(BigDecimal.class, MyBigDecimalEncoder.instance).
            add(BigInteger.class, MyBigIntegerEncoder.instance).
            add(Boolean.class, MyBooleanEncoder.instance).
            add(byte[].class, MyByteArrayEncoder.instance).
            add(ByteBuf.class, MyByteBufEncoder.instance).
            add(ByteBuffer.class, MyByteBufferEncoder.instance).
            add(Byte.class, MyByteEncoder.instance).
            add(DbValue.class, MyDbValueEncoder.instance).
            add(Double.class, MyDoubleEncoder.instance).
            add(Duration.class, MyDurationEncoder.instance).
            add(Float.class, MyFloatEncoder.instance).
            add(Instant.class, MyInstantEncoder.instance).
            add(Integer.class, MyIntegerEncoder.instance).
            add(LocalDate.class, MyLocalDateEncoder.instance).
            add(LocalDateTime.class, MyLocalDateTimeEncoder.instance).
            add(LocalTime.class, MyLocalTimeEncoder.instance).
            add(Long.class, MyLongEncoder.instance).
            add(Optional.class, MyOptionalEncoder.instance).
            add(Short.class, MyShortEncoder.instance).
            add(String.class, MyStringEncoder.instance).
            add(ULong.class, MyULongEncoder.instance).
            add(Year.class, MyYearEncoder.instance).
            build();
}
