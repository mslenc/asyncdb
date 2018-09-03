package com.xs0.asyncdb.common.sql;

import com.xs0.asyncdb.mysql.codec.CodecSettings;

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
    void encode(Object value, StringBuilder out, CodecSettings settings);

    /**
     * Returns a set of classes that this encoder can encode. The encode function will only
     * be called if the value is one of the stated classes.
     *
     * @return the set of classes
     */
    Set<Class<?>> supportedClasses();
}
