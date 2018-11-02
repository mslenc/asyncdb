package com.github.mslenc.asyncdb.my.auth;

import java.nio.charset.Charset;

public class MyAuthOldPassword {
    private static final byte[] emptyArray = { };

    public static byte[] generateAuthentication(Charset charset, String passwordOpt, byte[] seed) {
        if (passwordOpt == null || passwordOpt.isEmpty())
            return emptyArray;

        return newCrypt(charset, passwordOpt, new String(seed, charset));
    }

    private static byte[] newCrypt(Charset charset, String password, String seed) {
        byte b = 0;
        double d = 0;

        long[] pw = newHash(seed);
        long[] msg = newHash(password);
        long max = 0x3fffffffL;
        long seed1 = (pw[0] ^ msg[0]) % max;
        long seed2 = (pw[1] ^ msg[1]) % max;

        char[] chars = new char[seed.length()];

        int i = 0;
        while (i < seed.length()) {
            seed1 = ((seed1 * 3) + seed2) % max;
            seed2 = (seed1 + seed2 + 33) % max;
            d = (double) seed1 / (double) max;
            b = (byte) Math.floor((d * 31) + 64);
            chars[i] = (char)b;
            i += 1;
        }

        seed1 = ((seed1 * 3) + seed2) % max;
        seed2 = (seed1 + seed2 + 33) % max;
        d = (double) seed1 / (double) max;
        b = (byte) Math.floor(d * 31);

        int j = 0;
        while (j < seed.length()) {
            chars[j] = (char) (chars[j] ^ b);
            j += 1;
        }

        byte[] bytes = new String(chars).getBytes(charset);
        byte[] result = new byte[bytes.length + 1];
        System.arraycopy(bytes, 0, result, 0, bytes.length);
        return result;
    }

    private static long[] newHash(String password) {
        long nr = 1345345333L;
        long add = 7L;
        long nr2 = 0x12345671L;

        for (char c : password.toCharArray()) {
            if (c != ' ' && c != '\t') {
                long tmp = (0xff & c);
                nr ^= ((((nr & 63) + add) * tmp) + (nr << 8));
                nr2 += ((nr2 << 8) ^ nr);
                add += tmp;
            }
        }

        return new long[] { nr & 0x7fffffffL, nr2 & 0x7fffffffL };
    }

}
