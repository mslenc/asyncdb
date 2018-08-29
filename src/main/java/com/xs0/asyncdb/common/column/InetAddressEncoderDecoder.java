package com.xs0.asyncdb.common.column;

import java.net.InetAddress;
import static sun.net.util.IPAddressUtil.textToNumericFormatV4;
import static sun.net.util.IPAddressUtil.textToNumericFormatV6;

public class InetAddressEncoderDecoder implements ColumnEncoderDecoder {
    private static final InetAddressEncoderDecoder instance = new InetAddressEncoderDecoder();

    public static InetAddressEncoderDecoder instance() {
        return instance;
    }

    @Override
    public Object decode(String value) {
        // TODO: don't depend on IPAddressUtil
        if (value.contains(":")) {
            return textToNumericFormatV6(value);
        } else {
            return textToNumericFormatV4(value);
        }
    }

    @Override
    public String encode(Object value) {
        return ((InetAddress)value).getHostAddress();
    }
}
