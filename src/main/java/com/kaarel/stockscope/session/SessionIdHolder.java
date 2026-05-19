package com.kaarel.stockscope.session;

import jakarta.servlet.http.HttpServletRequest;

public final class SessionIdHolder {

    public static final String COOKIE_NAME = "STOCKSCOPE_SESSION";
    public static final String REQUEST_ATTRIBUTE = "stockscope.sessionId";

    private SessionIdHolder() {
    }

    public static String require(HttpServletRequest request) {
        Object value = request.getAttribute(REQUEST_ATTRIBUTE);
        if (value instanceof String s && !s.isBlank()) {
            return s;
        }
        throw new IllegalStateException("Session id not set on request; check SessionIdFilter registration");
    }
}
