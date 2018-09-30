package com.github.mslenc.asyncdb.common.sql;

import com.github.mslenc.asyncdb.mysql.codec.CodecSettings;
import io.netty.buffer.ByteBuf;

import java.util.Set;

public interface SqlLiteralEncoder {
    /**
     * Encodes a value into SQL (text), with all appropriate escaping. Note that settings.charset
     * can be ignored at this stage.
     *
     * @param value the value to be encoded
     * @param out container for the output
     * @param settings settings (note that charset can be ignored)
     */
    void encode(Object value, ByteBuf out, CodecSettings settings);

    /**
     * Returns a set of classes that this encoder can encode. The encode function will only
     * be called if the value is one of the stated classes.
     *
     * @return the set of classes
     */
    Set<Class<?>> supportedClasses();
}
