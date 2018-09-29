package com.github.mslenc.asyncdb.mysql.binary;

import com.github.mslenc.asyncdb.common.ULong;
import com.github.mslenc.asyncdb.common.column.YearEncoderDecoder;
import com.github.mslenc.asyncdb.mysql.binary.encoder.*;
import io.netty.buffer.ByteBuf;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.*;
import java.util.*;

public class BinaryRowEncoder {
    private final StringEncoder stringEncoder;
    private final Map<Class<?>, BinaryEncoder> encoders;

    public BinaryRowEncoder(Charset charset) {
        this.stringEncoder = new StringEncoder(charset);
        this.encoders = new HashMap<>(25);

        this.encoders.put(String.class, stringEncoder);
        this.encoders.put(BigDecimal.class, this.stringEncoder);
        this.encoders.put(BigInteger.class, this.stringEncoder);
        this.encoders.put(Byte.class, ByteEncoder.instance());
        this.encoders.put(Short.class, ShortEncoder.instance());
        this.encoders.put(Integer.class, IntegerEncoder.instance());
        this.encoders.put(Long.class, LongEncoder.instance());
        this.encoders.put(ULong.class, ULongEncoder.instance());
        this.encoders.put(Float.class, FloatEncoder.instance());
        this.encoders.put(Double.class, DoubleEncoder.instance());
        this.encoders.put(LocalDate.class, LocalDateEncoder.instance());
        this.encoders.put(LocalDateTime.class, LocalDateTimeEncoder.instance());
        this.encoders.put(LocalTime.class, LocalTimeEncoder.instance());
        this.encoders.put(Instant.class, InstantEncoder.instance());
        this.encoders.put(Year.class, YearEncoder.instance());
        this.encoders.put(Duration.class, DurationEncoder.instance());
        this.encoders.put(byte[].class, ByteArrayEncoder.instance());
        this.encoders.put(Boolean.class, BooleanEncoder.instance());
    }

    public BinaryEncoder encoderFor(Object v) {
        BinaryEncoder encoder = encoders.get(v.getClass());
        if (encoder != null)
            return encoder;

        // Below, we skip the instanceof check on classes that are final - they'd already be found above,
        // so it would be just a waste of time to check again..

        if (v instanceof CharSequence)
            return stringEncoder;

        if (v instanceof BigInteger)
            return stringEncoder;

        if (v instanceof BigDecimal)
            return stringEncoder;

        if (v instanceof ByteBuffer)
            return ByteBufferEncoder.instance();

        if (v instanceof ByteBuf)
            return ByteBufEncoder.instance();

        throw new UnsupportedOperationException("No encoder found for " + v.getClass().getCanonicalName());
    }
}
