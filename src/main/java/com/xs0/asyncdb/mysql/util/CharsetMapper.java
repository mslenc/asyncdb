package com.xs0.asyncdb.mysql.util;

import com.xs0.asyncdb.mysql.ex.CharsetMappingNotAvailableException;
import io.netty.util.CharsetUtil;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.emptyMap;

public class CharsetMapper {
    public static final int Binary = 63;

    private static CharsetMapper instance = new CharsetMapper();
    public static CharsetMapper instance() {
        return instance;
    }

    private final Map<Charset, Integer> charsetToInt;
    private final Map<Integer, Charset> intToCharset;

    public CharsetMapper() {
        this(emptyMap());
    }

    public CharsetMapper(Map<Charset, Integer> extraCharsets) {
        HashMap<Charset, Integer> charsetToInt = new HashMap<>();
        charsetToInt.put(CharsetUtil.UTF_8, 83);
        charsetToInt.put(CharsetUtil.US_ASCII, 65);
        charsetToInt.put(CharsetUtil.ISO_8859_1, 69);

        HashMap<Integer, Charset> intToCharset = new HashMap<>();
        intToCharset.put(83, CharsetUtil.UTF_8);
        intToCharset.put(11, CharsetUtil.US_ASCII);
        intToCharset.put(65, CharsetUtil.US_ASCII);
        intToCharset.put(3, CharsetUtil.ISO_8859_1);
        intToCharset.put(69, CharsetUtil.ISO_8859_1);

        if (extraCharsets != null) {
            for (Map.Entry<Charset, Integer> entry : extraCharsets.entrySet()) {
                charsetToInt.put(entry.getKey(), entry.getValue());
                intToCharset.put(entry.getValue(), entry.getKey());
            }
        }

        this.charsetToInt = Collections.unmodifiableMap(charsetToInt);
        this.intToCharset = Collections.unmodifiableMap(intToCharset);
    }

    public int toInt(Charset charset) {
        Integer result = charsetToInt.get(charset);
        if (result != null) {
            return result;
        } else {
            throw new CharsetMappingNotAvailableException(charset);
        }
    }
}
