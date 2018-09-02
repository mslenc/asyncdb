package com.xs0.asyncdb.mysql.encoder.auth;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MySQLNativePasswordAuthentication {
    private static final byte[] emptyArray = { };

    public static byte[] generateAuthentication(Charset charset, String passwordOpt, byte[] seed) {
        if (passwordOpt != null) {
            return scramble411(charset, passwordOpt, seed);
        } else {
            return emptyArray;
        }
    }

    private static byte[] scramble411(Charset charset, String password, byte[] seed) {
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        byte[] shaPass = messageDigest.digest(password.getBytes(charset));

        messageDigest.reset();

        byte[] shaShaPass = messageDigest.digest(shaPass);

        messageDigest.reset();

        if (seed.length == 21 && seed[20] == 0) {
            // for some reason, the seed is sent as 21 zero-terminated bytes, even though it's supposed to be 20 bytes
            messageDigest.update(seed, 0, 20);
        } else {
            messageDigest.update(seed);
        }

        messageDigest.update(shaShaPass);

        byte[] result = messageDigest.digest();

        for (int counter = 0; counter < result.length; counter++) {
            result[counter] ^= shaPass[counter];
        }

        return result;
    }
}
