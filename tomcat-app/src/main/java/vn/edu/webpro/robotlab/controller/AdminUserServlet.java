package vn.edu.webpro.robotlab.controller;

import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import vn.edu.webpro.robotlab.dao.DatabaseConnectionFactory;
import vn.edu.webpro.robotlab.dao.UserDao;
import vn.edu.webpro.robotlab.model.User;
import vn.edu.webpro.robotlab.service.UserManagementService;
import vn.edu.webpro.robotlab.web.AdminAccess;
import vn.edu.webpro.robotlab.web.ApiResponses;
import vn.edu.webpro.robotlab.web.Json;

/** Admin-only account list and role assignment. Registration never accepts a role. */
@WebServlet("/api/admin/users/*")
public final class AdminUserServlet extends HttpServlet {
    private static final int PAGE_SIZE = 20;
    private final UserDao users = new UserDao(new DatabaseConnectionFactory(System.getenv()));
    private final UserManagementService management = new UserManagementService(users);

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
        User actor = AdminAccess.requireAdmin(request, response);
        if (actor == null) return;
        if (request.getPathInfo() != null && !"/".equals(request.getPathInfo())) {
            ApiResponses.error(response, 404, "NOT_FOUND", "Không tìm thấy tài nguyên yêu cầu.");
            return;
        }
        try {
            int page = page(request.getParameter("page"));
            UserManagementService.UserPage data = management.page(actor.id(), PAGE_SIZE,
                    Math.multiplyExact(page - 1, PAGE_SIZE));
            String items = data.items().stream().map(User::toJson).reduce((left, right) -> left + "," + right).orElse("");
            ApiResponses.json(response, 200, "{\"data\":[" + items + "],\"meta\":{\"page\":" + page
                    + ",\"pageSize\":" + PAGE_SIZE + ",\"total\":" + data.total() + "}}");
        } catch (SecurityException exception) {
            ApiResponses.error(response, 403, "FORBIDDEN", "Bạn không có quyền xem tài khoản.");
        } catch (IllegalArgumentException exception) {
            ApiResponses.error(response, 422, "VALIDATION_ERROR", "Trang không hợp lệ.");
        } catch (SQLException exception) {
            ApiResponses.error(response, 503, "DEPENDENCY_NOT_READY", "Cơ sở dữ liệu chưa sẵn sàng.");
        }
    }

    private void changeRole(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User actor = AdminAccess.requireAdmin(request, response);
        if (actor == null || !AdminAccess.requireCsrf(request, response)) return;
        try {
            String path = request.getPathInfo();
            if (path == null || !path.matches("/[1-9][0-9]*/role")) {
                ApiResponses.error(response, 404, "NOT_FOUND", "Không tìm thấy tài khoản.");
                return;
            }
            long id = Long.parseLong(path.substring(1, path.length() - 5));
            User saved = management.changeRole(actor.id(), id,
                    Json.stringField(Json.readBody(request.getReader(), 8192), "role"));
            ApiResponses.json(response, 200, "{\"data\":{\"user\":" + saved.toJson() + "}}");
        } catch (SecurityException exception) {
            ApiResponses.error(response, 403, "FORBIDDEN", "Bạn không có quyền thay đổi vai trò.");
        } catch (IllegalArgumentException exception) {
            if ("not-found".equals(exception.getMessage())) {
                ApiResponses.error(response, 404, "NOT_FOUND", "Không tìm thấy tài khoản.");
            } else {
                ApiResponses.error(response, 422, "VALIDATION_ERROR",
                        "Chỉ có thể chọn USER/ADMIN và không thể tự đổi quyền của mình.");
            }
        } catch (SQLException exception) {
            ApiResponses.error(response, 503, "DEPENDENCY_NOT_READY", "Cơ sở dữ liệu chưa sẵn sàng.");
        }
    }

    private int page(String raw) {
        if (raw == null) return 1;
        int value = Integer.parseInt(raw);
        if (value < 1 || value > 1000000) throw new IllegalArgumentException("page");
        return value;
    }
}
