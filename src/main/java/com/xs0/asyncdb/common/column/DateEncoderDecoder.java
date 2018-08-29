package com.xs0.asyncdb.common.column;

import com.xs0.asyncdb.common.exceptions.DateEncoderNotAvailableException;

import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DateEncoderDecoder implements ColumnEncoderDecoder {
    private static final DateEncoderDecoder instance = new DateEncoderDecoder();

    public static DateEncoderDecoder instance() {
        return instance;
    }

    private static final String ZeroedDate = "0000-00-00";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;

    @Override
    public LocalDate decode(String value) {
        if (ZeroedDate.equals(value)) {
            return null;
        } else {
            return LocalDate.parse(value);
        }
    }

    @Override
    public String encode(Object value) {
        if (value instanceof LocalDate)
            return formatter.format((LocalDate) value);

        if (value instanceof Date)
            return formatter.format(((Date)value).toLocalDate());

        throw new DateEncoderNotAvailableException(value);
    }
}
