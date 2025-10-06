package org.example.scholar.service;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScholarUtils {
    private static final Pattern USER_PARAM = Pattern.compile("[?&]user=([^&]+)");

    public static String extractAuthorIdFromUrl(String url) {
        if (url == null) return null;
        Matcher m = USER_PARAM.matcher(url);
        if (m.find()) {
            String raw = m.group(1);
            return URLDecoder.decode(raw, StandardCharsets.UTF_8);
        }
        return null;
    }

    public static boolean looksLikeAuthorId(String s) {
        if (s == null) return false;
        String x = s.trim();
        return x.matches("^[A-Za-z0-9_-]{8,30}$");
    }
}
