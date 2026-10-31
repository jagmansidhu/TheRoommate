package com.roomate.app.config.security;

import jakarta.servlet.http.HttpServletRequest;

public final class RequestHttps {

    private RequestHttps() {
    }

    public static boolean isHttps(HttpServletRequest request) {
        if (request.isSecure()) {
            return true;
        }
        String forwarded = request.getHeader("X-Forwarded-Proto");
        if (forwarded == null) {
            return false;
        }
        int comma = forwarded.indexOf(',');
        String first = (comma < 0 ? forwarded : forwarded.substring(0, comma)).trim();
        return "https".equalsIgnoreCase(first);
    }
}
