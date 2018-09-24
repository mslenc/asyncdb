package com.github.mslenc.asyncdb.common.column;

import com.github.mslenc.asyncdb.common.exceptions.DateEncoderNotAvailableException;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Calendar;
import java.util.Date;

import static java.time.ZoneOffset.UTC;

public class TimestampEncoderDecoder implements ColumnEncoderDecoder {
    private static final String BaseFormat = "yyyy-MM-dd HH:mm:ss";
    private static final String MillisFormat = ".SSSSSS";

    private static final TimestampEncoderDecoder instance = new TimestampEncoderDecoder();

    public static TimestampEncoderDecoder instance() {
        return instance;
    }

    private static final DateTimeFormatter optional =
        new DateTimeFormatterBuilder().
            appendPattern(MillisFormat).
            toFormatter();

    private static final DateTimeFormatter optionalTimeZone =
        new DateTimeFormatterBuilder().
            appendPattern("Z").
            toFormatter();

    private static final DateTimeFormatter format =
        new DateTimeFormatterBuilder().
            appendPattern(BaseFormat).
            appendOptional(optional).
            appendOptional(optionalTimeZone).
            toFormatter();

    private static final DateTimeFormatter timezonedPrinter =
        new DateTimeFormatterBuilder().
            appendPattern(BaseFormat).
            appendPattern(MillisFormat).
            appendLiteral('Z').
            toFormatter();

    private static final DateTimeFormatter nonTimezonedPrinter =
        new DateTimeFormatterBuilder().
            appendPattern(BaseFormat).
            appendPattern(MillisFormat).
            toFormatter();

    protected DateTimeFormatter parseFormat() {
        return format;
    }

    @Override
    public LocalDateTime decode(String value) {
        return LocalDateTime.parse(value, parseFormat());
    }

    @Override
    public String encode(Object value) {
        if (value instanceof Timestamp)
            return timezonedPrinter.format(((Timestamp)value).toInstant().atZone(UTC).toLocalDateTime());

        if (value instanceof Date)
            return timezonedPrinter.format(((Date)value).toInstant().atZone(UTC).toLocalDateTime());

        if (value instanceof Calendar)
            return timezonedPrinter.format(((Calendar)value).toInstant().atZone(UTC).toLocalDateTime());

        if (value instanceof LocalDateTime)
            return nonTimezonedPrinter.format((LocalDateTime) value);

        throw new DateEncoderNotAvailableException(value);
    }
}
