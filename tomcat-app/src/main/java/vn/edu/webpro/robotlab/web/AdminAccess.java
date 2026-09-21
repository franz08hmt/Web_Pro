package vn.edu.webpro.robotlab.web;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import vn.edu.webpro.robotlab.model.User;

/** Shared admin and CSRF guard for every /api/admin Servlet. */
public final class AdminAccess {
    private AdminAccess() { }
    public static User requireAdmin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        Object value = session == null ? null : session.getAttribute("user");
        if (!(value instanceof User user)) {
            ApiResponses.error(response, 401, "AUTH_REQUIRED", "Cần đăng nhập bằng tài khoản quản trị.");
            return null;
        }
        if (!"ADMIN".equals(user.role())) {
            ApiResponses.error(response, 403, "FORBIDDEN", "Tài khoản không có quyền quản trị nội dung.");
            return null;
        }
        return user;
    }
    public static boolean requireCsrf(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        Object token = session == null ? null : session.getAttribute("csrfToken");
        if (token instanceof String expected && expected.equals(request.getHeader("X-CSRF-Token"))) return true;
        ApiResponses.error(response, 403, "CSRF_REQUIRED", "Yêu cầu cần mã CSRF hợp lệ.");
        return false;
    }
}
