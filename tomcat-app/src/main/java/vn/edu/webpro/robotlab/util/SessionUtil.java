package vn.edu.webpro.robotlab.util;

import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import vn.edu.webpro.robotlab.business.User;
import vn.edu.webpro.robotlab.data.UserDB;

/**
 * Làm việc với HttpSession của người đăng nhập (Chapter 7: session tracking).
 *
 * Tomcat giữ HttpSession ở server và gửi cookie JSESSIONID cho trình duyệt.
 * Session lưu hai thuộc tính: "user" (JavaBean User) và "csrfToken".
 */
public final class SessionUtil {
    private SessionUtil() {
    }

    /** Sau khi đăng nhập đúng: bỏ phiên cũ, tạo phiên mới chứa user và CSRF token. */
    public static void startSession(HttpServletRequest request, User user) {
        HttpSession old = request.getSession(false);
        if (old != null) {
            old.invalidate();
        }
        HttpSession session = request.getSession(true);
        session.setAttribute("user", user);
        session.setAttribute("csrfToken", UUID.randomUUID().toString());
    }

    /**
     * Phiên còn hợp lệ khi đúng tài khoản và cùng session_version với database.
     * Đổi mật khẩu hoặc đổi quyền làm tăng session_version nên phiên cũ bị loại.
     */
    public static boolean isCurrent(User cached, User fresh) {
        return cached != null && fresh != null
                && cached.getId() == fresh.getId()
                && cached.getSessionVersion() == fresh.getSessionVersion();
    }

    /**
     * Lấy người đang đăng nhập, đọc lại từ database thay vì tin bản cũ trong
     * session: nếu admin vừa hạ quyền tài khoản này thì quyền cũ mất ngay.
     */
    public static User getCurrentUser(HttpServletRequest request) throws SQLException {
        HttpSession session = request.getSession(false);
        Object value = session == null ? null : session.getAttribute("user");
        if (!(value instanceof User)) {
            return null;
        }

        User cached = (User) value;
        User fresh = UserDB.selectUser(cached.getId());
        if (!isCurrent(cached, fresh)) {
            session.invalidate();
            return null;
        }
        session.setAttribute("user", fresh);
        return fresh;
    }

    /** Trả về người đăng nhập; nếu chưa đăng nhập thì ghi lỗi 401 và trả null. */
    public static User requireUser(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        User user;
        try {
            user = getCurrentUser(request);
        } catch (SQLException e) {
            ResponseUtil.sendDatabaseUnavailable(response);
            return null;
        }
        if (user == null) {
            ResponseUtil.sendError(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "AUTH_REQUIRED", "Bạn cần đăng nhập để tiếp tục.");
        }
        return user;
    }

    /** Như requireUser nhưng còn yêu cầu role ADMIN; sai quyền thì ghi lỗi 403. */
    public static User requireAdmin(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        User user;
        try {
            user = getCurrentUser(request);
        } catch (SQLException e) {
            ResponseUtil.sendDatabaseUnavailable(response);
            return null;
        }
        if (user == null) {
            ResponseUtil.sendError(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "AUTH_REQUIRED", "Cần đăng nhập bằng tài khoản quản trị.");
            return null;
        }
        if (!user.isAdmin()) {
            ResponseUtil.sendError(response, HttpServletResponse.SC_FORBIDDEN,
                    "FORBIDDEN", "Tài khoản không có quyền quản trị.");
            return null;
        }
        return user;
    }

    /** Request ghi phải gửi header X-CSRF-Token khớp với token lưu trong session. */
    public static boolean hasValidCsrfToken(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        Object token = session == null ? null : session.getAttribute("csrfToken");
        if (token instanceof String && token.equals(request.getHeader("X-CSRF-Token"))) {
            return true;
        }
        ResponseUtil.sendError(response, HttpServletResponse.SC_FORBIDDEN,
                "CSRF_REQUIRED", "Yêu cầu cần mã CSRF hợp lệ.");
        return false;
    }
}
