package com.example.mkarte1.util;

public final class FileNameUtil {
    private FileNameUtil() {
    }

    public static String sanitize(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "no_name";
        }
        return value.trim().replaceAll("[/\\\\:*?\"<>|]", "_");
    }
}
