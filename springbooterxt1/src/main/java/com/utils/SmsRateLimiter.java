package com.utils;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 短信验证码发送限流（单机内存版，防刷）。
 * 规则：同一手机号两次发送间隔需 >= 60 秒，且 1 小时内最多 5 条。
 */
public final class SmsRateLimiter {

    private static final long MIN_INTERVAL_MS = 60 * 1000L;
    private static final long WINDOW_MS = 60 * 60 * 1000L;
    private static final int MAX_IN_WINDOW = 5;

    private static final ConcurrentHashMap<String, long[]> RECORDS = new ConcurrentHashMap<>();

    private SmsRateLimiter() {
    }

    /**
     * 校验是否允许发送，超限返回提示文案，允许则返回 null。
     */
    public static synchronized String tryAcquire(String phone) {
        long now = System.currentTimeMillis();
        long[] rec = RECORDS.get(phone);
        if (rec == null) {
            RECORDS.put(phone, new long[]{now, now, 1});
            return null;
        }
        long lastSend = rec[0];
        long windowStart = rec[1];
        long count = rec[2];

        if (now - lastSend < MIN_INTERVAL_MS) {
            long wait = (MIN_INTERVAL_MS - (now - lastSend)) / 1000 + 1;
            return "发送过于频繁，请 " + wait + " 秒后再试";
        }
        if (now - windowStart > WINDOW_MS) {
            windowStart = now;
            count = 0;
        }
        if (count >= MAX_IN_WINDOW) {
            return "短信发送次数过多，请稍后再试";
        }
        RECORDS.put(phone, new long[]{now, windowStart, count + 1});
        return null;
    }
}
