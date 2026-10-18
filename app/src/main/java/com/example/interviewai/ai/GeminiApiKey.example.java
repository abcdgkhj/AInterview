package com.example.interviewai.ai;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * TEMPLATE FILE - Salin file ini menjadi GeminiApiKey.java dan masukkan daftar API Key Gemini Anda.
 */
public class GeminiApiKey {

    public static final String[] API_KEYS = {
            "YOUR_GEMINI_API_KEY_1",
            "YOUR_GEMINI_API_KEY_2",
            "YOUR_GEMINI_API_KEY_3"
    };

    public static final String API_KEY = API_KEYS[0];

    private static final AtomicInteger currentIndex = new AtomicInteger(0);

    public static String getActiveKey() {
        if (API_KEYS == null || API_KEYS.length == 0) return "";
        int idx = Math.abs(currentIndex.get()) % API_KEYS.length;
        return API_KEYS[idx];
    }

    public static synchronized String rotateToNextKey() {
        if (API_KEYS == null || API_KEYS.length <= 1) return getActiveKey();
        int next = currentIndex.incrementAndGet();
        int idx = Math.abs(next) % API_KEYS.length;
        return API_KEYS[idx];
    }

    public static int getKeyCount() {
        return API_KEYS != null ? API_KEYS.length : 0;
    }

    public static int getCurrentKeyIndex() {
        if (API_KEYS == null || API_KEYS.length == 0) return 0;
        return (Math.abs(currentIndex.get()) % API_KEYS.length) + 1;
    }
}
