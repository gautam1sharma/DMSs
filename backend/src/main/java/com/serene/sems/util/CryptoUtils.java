package com.serene.sems.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
/**
 * Constant-time helpers to reduce side-channel leakage when comparing secrets or identifiers.
 * Password verification uses {@link org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder},
 * which performs constant-time checks on the encoded hash.
 */
public final class CryptoUtils {

    private CryptoUtils() {
    }

    /**
     * Compare two strings using fixed-length digests so comparison time does not depend on prefix mismatches.
     */
    public static boolean constantTimeEquals(String a, String b) {
        byte[] da = sha256(normalize(a));
        byte[] db = sha256(normalize(b));
        return MessageDigest.isEqual(da, db);
    }

    private static String normalize(String s) {
        return s != null ? s : "";
    }

    private static byte[] sha256(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return md.digest(text.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
