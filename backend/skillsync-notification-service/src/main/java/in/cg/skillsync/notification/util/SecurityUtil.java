package in.cg.skillsync.notification.util;

import jakarta.servlet.http.HttpServletRequest;

public final class SecurityUtil {

    private SecurityUtil() {
    }

    public static String getCurrentUserId(HttpServletRequest request) {
        return request.getHeader("X-User-Id");
    }

    public static String getCurrentUserRole(HttpServletRequest request) {
        return request.getHeader("X-User-Role");
    }
}
