package com.motadata.ipam.security;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility for BCrypt password hashing and verification.
 */
public class PasswordEncoder {

    //Takes a plain-text password and converts it into a BCrypt hashed password.
    public static String encode(String rawPassword) {
        if (rawPassword == null) {
            return null;
        }
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt()); //it generates different hashes for same passwords
    }

    //Checks whether the plain-text password provided during login matches the BCrypt hash stored in the database.
    public static boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        //This allows a direct match if the stored password is plain text.
        if (rawPassword.equals(encodedPassword)) {
            return true;
        }
        try {
            //Checks the raw password against the stored BCrypt hash.
            return BCrypt.checkpw(rawPassword, encodedPassword);
        } catch (Exception e) {
            return false;
        }
    }
}
