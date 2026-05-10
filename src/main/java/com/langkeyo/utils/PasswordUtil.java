package com.langkeyo.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class PasswordUtil {
    private static final String SALT = "community-fresh-group-buy-local-salt";

    private PasswordUtil() {
    }

    public static String hash(String rawPassword) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest((SALT + ":" + rawPassword).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("密码加密失败", e);
        }
    }

    public static boolean matches(String rawPassword, String hashedPassword) {
        if (rawPassword == null || hashedPassword == null) {
            return false;
        }
        return hash(rawPassword).equalsIgnoreCase(hashedPassword);
    }
}
