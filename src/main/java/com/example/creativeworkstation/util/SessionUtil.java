package com.example.creativeworkstation.util;

import jakarta.servlet.http.HttpSession;

public class SessionUtil {
    private static final String USER_ID_KEY = "userId";
    private static final long SESSION_TIMEOUT = 30 * 60; // 30分钟过期

    public static void setCurrentUser(HttpSession session, Long userId) {
        session.setAttribute(USER_ID_KEY, userId);
        session.setMaxInactiveInterval((int) SESSION_TIMEOUT);
    }

    public static Long getCurrentUserId(HttpSession session) {
        return (Long) session.getAttribute(USER_ID_KEY);
    }

    public static boolean isLoggedIn(HttpSession session) {
        return getCurrentUserId(session) != null;
    }

    public static void logout(HttpSession session) {
        session.invalidate();
    }
}
