package com.github.mslenc.asyncdb.my.auth;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MyAuthCachingSha2 {
    private static final byte[] emptyArray = { };

    public static byte[] generateAuthentication(Charset charset, String passwordOpt, byte[] seed) {
        if (passwordOpt != null) {
            return scramble(charset, passwordOpt, seed);
        } else {
            return emptyArray;
        }
    }

    private static byte[] scramble(Charset charset, String password, byte[] seed) {
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        byte[] shaPass = messageDigest.digest(password.getBytes(charset));

        messageDigest.reset();

        byte[] shaShaPass = messageDigest.digest(shaPass);

        messageDigest.reset();

        messageDigest.update(shaShaPass);
        if (seed.length == 21 && seed[20] == 0) {
            // for some reason, the seed is sent as 21 zero-terminated bytes, even though it's supposed to be 20 bytes
            messageDigest.update(seed, 0, 20);
        } else {
            messageDigest.update(seed);
        }

        byte[] result = messageDigest.digest();

        for (int counter = 0; counter < result.length; counter++) {
            result[counter] ^= shaPass[counter];
        }

        return result;
    }
}
