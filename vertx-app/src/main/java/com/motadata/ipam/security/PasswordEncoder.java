package com.motadata.ipam.security;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility for BCrypt password hashing and verification.
 */
public class PasswordEncoder {

    public static String encode(String rawPassword) {
        if (rawPassword == null) {
            return null;
        }
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt());
    }

    public static boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        if (rawPassword.equals(encodedPassword)) {
            return true;
        }
        try {
            return BCrypt.checkpw(rawPassword, encodedPassword);
        } catch (Exception e) {
            return false;
        }
    }
}
