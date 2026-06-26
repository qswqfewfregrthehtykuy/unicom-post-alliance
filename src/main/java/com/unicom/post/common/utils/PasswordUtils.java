package com.unicom.post.common.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordUtils {
    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    public static String encode(String rawPassword) {
        return ENCODER.encode(rawPassword);
    }

    public static boolean matches(String rawPassword, String encodedPassword) {
        return ENCODER.matches(rawPassword, encodedPassword);
    }

    public static String generateRandomPassword() {
        String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lower = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String special = "!@#$%^&*";
        String all = upper + lower + digits + special;
        StringBuilder sb = new StringBuilder();
        sb.append(upper.charAt((int)(Math.random() * upper.length())));
        sb.append(lower.charAt((int)(Math.random() * lower.length())));
        sb.append(digits.charAt((int)(Math.random() * digits.length())));
        sb.append(special.charAt((int)(Math.random() * special.length())));
        for (int i = 0; i < 8; i++) {
            sb.append(all.charAt((int)(Math.random() * all.length())));
        }
        char[] chars = sb.toString().toCharArray();
        for (int i = 0; i < chars.length; i++) {
            int j = (int)(Math.random() * chars.length);
            char tmp = chars[i];
            chars[i] = chars[j];
            chars[j] = tmp;
        }
        return new String(chars);
    }
}