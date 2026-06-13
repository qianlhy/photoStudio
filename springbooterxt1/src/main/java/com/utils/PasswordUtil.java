package com.utils;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 密码加密工具（JDK 自带 PBKDF2，无需额外依赖）。
 *
 * 兼容策略：
 * - 新密码使用加盐 PBKDF2 存储，格式：pbkdf2$迭代次数$盐(base64)$哈希(base64)
 * - 历史明文密码仍可校验通过（matches 走明文分支），并应在登录成功后调用 encode 升级
 */
public final class PasswordUtil {

    private static final String PREFIX = "pbkdf2$";
    private static final int ITERATIONS = 10000;
    private static final int KEY_LENGTH = 256;
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtil() {
    }

    /** 是否为本工具加密后的密文 */
    public static boolean isEncoded(String stored) {
        return stored != null && stored.startsWith(PREFIX);
    }

    /** 生成加盐密文 */
    public static String encode(String raw) {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        byte[] hash = pbkdf2(raw, salt, ITERATIONS);
        return PREFIX + ITERATIONS + "$"
                + Base64.getEncoder().encodeToString(salt) + "$"
                + Base64.getEncoder().encodeToString(hash);
    }

    /** 校验：密文走 PBKDF2 比对，明文（历史数据）走直接相等 */
    public static boolean matches(String raw, String stored) {
        if (raw == null || stored == null) {
            return false;
        }
        if (!isEncoded(stored)) {
            return stored.equals(raw);
        }
        try {
            String[] parts = stored.split("\\$");
            int iterations = Integer.parseInt(parts[1]);
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expected = Base64.getDecoder().decode(parts[3]);
            byte[] actual = pbkdf2(raw, salt, iterations);
            return constantTimeEquals(expected, actual);
        } catch (Exception e) {
            return false;
        }
    }

    private static byte[] pbkdf2(String raw, byte[] salt, int iterations) {
        try {
            PBEKeySpec spec = new PBEKeySpec(raw.toCharArray(), salt, iterations, KEY_LENGTH);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            return skf.generateSecret(spec).getEncoded();
        } catch (Exception e) {
            throw new RuntimeException("密码加密失败", e);
        }
    }

    private static boolean constantTimeEquals(byte[] a, byte[] b) {
        if (a.length != b.length) {
            return false;
        }
        int r = 0;
        for (int i = 0; i < a.length; i++) {
            r |= a[i] ^ b[i];
        }
        return r == 0;
    }
}
