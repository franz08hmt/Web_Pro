package vn.edu.webpro.robotlab.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import vn.edu.webpro.robotlab.business.User;
import vn.edu.webpro.robotlab.data.UserDB;
import vn.edu.webpro.robotlab.util.JsonUtil;
import vn.edu.webpro.robotlab.util.ResponseUtil;
import vn.edu.webpro.robotlab.util.SessionUtil;
import vn.edu.webpro.robotlab.util.ValidationUtil;

/**
 * Quản trị tài khoản — chỉ ADMIN dùng được.
 *
 * GET   /api/admin/users?page=      danh sách tài khoản
 * PATCH /api/admin/users/{id}/role  đổi quyền USER/ADMIN của tài khoản khác
 *
 * Đăng ký qua website không bao giờ nhận role; đây là đường duy nhất đổi quyền.
 */
@WebServlet("/api/admin/users/*")
public class AdminUserServlet extends HttpServlet {
    private static final int PAGE_SIZE = 20;
    private static final int MAX_BODY = 8192;

    /* HttpServlet không có doPatch(), nên PATCH được tách ra trong service(). */
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if ("PATCH".equals(request.getMethod())) {
            changeRole(request, response);
            return;
        }
        super.service(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User admin = SessionUtil.requireAdmin(request, response);
        if (admin == null) return;

        String path = request.getPathInfo();
        if (path != null && !"/".equals(path)) {
            ResponseUtil.sendError(response, HttpServletResponse.SC_NOT_FOUND,
                    "NOT_FOUND", "Không tìm thấy tài nguyên yêu cầu.");
            return;
        }

        try {
            int page = ValidationUtil.parsePositiveInt(request.getParameter("page"), 1, ValidationUtil.MAX_PAGE);
            List<User> users = UserDB.selectUsers(PAGE_SIZE, (page - 1) * PAGE_SIZE);
            long total = UserDB.countUsers();

            StringBuilder items = new StringBuilder();
            for (User user : users) {
                if (items.length() > 0) items.append(',');
                items.append(user.toJson());
            }
            ResponseUtil.sendJson(response, HttpServletResponse.SC_OK,
                    "{\"data\":[" + items + "],\"meta\":{\"page\":" + page
                            + ",\"pageSize\":" + PAGE_SIZE + ",\"total\":" + total + "}}");
        } catch (IllegalArgumentException e) {
            ResponseUtil.sendError(response, 422, "VALIDATION_ERROR", "Trang không hợp lệ.");
        } catch (SQLException e) {
            ResponseUtil.sendDatabaseUnavailable(response);
        }
    }

    private void changeRole(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User admin = SessionUtil.requireAdmin(request, response);
        if (admin == null || !SessionUtil.hasValidCsrfToken(request, response)) return;

        String path = request.getPathInfo();
        if (path == null || !path.matches("/[1-9][0-9]{0,18}/role")) {
            ResponseUtil.sendError(response, HttpServletResponse.SC_NOT_FOUND,
                    "NOT_FOUND", "Không tìm thấy tài khoản.");
            return;
        }

        try {
            long targetId = Long.parseLong(path.substring(1, path.length() - "/role".length()));
            String role = JsonUtil.stringField(JsonUtil.readBody(request.getReader(), MAX_BODY), "role");

            User target = UserDB.selectUser(targetId);
            if (target == null) {
                ResponseUtil.sendError(response, HttpServletResponse.SC_NOT_FOUND,
                        "NOT_FOUND", "Không tìm thấy tài khoản.");
                return;
            }
            if (!User.isValidRole(role) || !admin.canChangeRoleOf(target)) {
                ResponseUtil.sendError(response, 422, "VALIDATION_ERROR",
                        "Chỉ có thể chọn USER/ADMIN và không thể tự đổi quyền của mình.");
                return;
            }

            // Đổi sang đúng quyền đang có thì không ghi, tránh làm phiên của họ hết hạn vô cớ.
            if (!role.equals(target.getRole())) {
                UserDB.updateRole(targetId, role);
                target = UserDB.selectUser(targetId);
            }
            ResponseUtil.sendJson(response, HttpServletResponse.SC_OK,
                    "{\"data\":{\"user\":" + target.toJson() + "}}");
        } catch (IllegalArgumentException e) {
            ResponseUtil.sendError(response, 422, "VALIDATION_ERROR", "Dữ liệu không hợp lệ.");
        } catch (SQLException e) {
            ResponseUtil.sendDatabaseUnavailable(response);
        }
    }
}
