package com.xs0.asyncdb.mysql.encoder.auth;

import com.xs0.asyncdb.common.exceptions.UnsupportedAuthenticationMethodException;

import java.nio.charset.Charset;

public interface AuthenticationMethod {
    String NATIVE = "mysql_native_password";
    String OLD = "mysql_old_password";

    static AuthenticationMethod byName(String name) {
        switch (name) {
            case NATIVE:
                return MySQLNativePasswordAuthentication.getInstance();

            case OLD:
                return MySQLOldPasswordAuthentication.getInstance();

            default:
                throw new UnsupportedAuthenticationMethodException(name);
        }
    }

    byte[] generateAuthentication(Charset charset, String passwordOpt, byte[] seed);
}
