package com.xs0.asyncdb.mysql.encoder.auth;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MySQLNativePasswordAuthentication implements AuthenticationMethod {
    private static final MySQLNativePasswordAuthentication instance = new MySQLNativePasswordAuthentication();

    public static MySQLNativePasswordAuthentication getInstance() {
        return instance;
    }

    private static final byte[] emptyArray = { };

    @Override
    public byte[] generateAuthentication(Charset charset, String passwordOpt, byte[] seed) {
        if (passwordOpt != null) {
            return scramble411(charset, passwordOpt, seed);
        } else {
            return emptyArray;
        }
    }

    private byte[] scramble411(Charset charset, String password, byte[] seed) {
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        byte[] initialDigest = messageDigest.digest(password.getBytes(charset));

        messageDigest.reset();

        byte[] finalDigest = messageDigest.digest(initialDigest);

        messageDigest.reset();

        messageDigest.update(seed);
        messageDigest.update(finalDigest);

        byte[] result = messageDigest.digest();

        for (int counter = 0; counter < result.length; counter++) {
            result[counter] = (byte) (result[counter] ^ initialDigest[counter]);
        }

        return result;
    }
}
