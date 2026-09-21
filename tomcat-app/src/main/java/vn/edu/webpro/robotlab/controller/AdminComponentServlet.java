package vn.edu.webpro.robotlab.controller;

import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import vn.edu.webpro.robotlab.dao.ComponentDao;
import vn.edu.webpro.robotlab.dao.DatabaseConnectionFactory;
import vn.edu.webpro.robotlab.web.AdminAccess;
import vn.edu.webpro.robotlab.web.ApiResponses;

@WebServlet("/api/admin/components/*")
public final class AdminComponentServlet extends HttpServlet {
    private final ComponentDao components = new ComponentDao(new DatabaseConnectionFactory(System.getenv()));
    @Override protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (AdminAccess.requireAdmin(request, response) == null) return;
        try {
            var page = components.list(1, 100);
            ApiResponses.json(response, 200, page.toJson());
        } catch (SQLException exception) {
            ApiResponses.error(response, 503, "DEPENDENCY_NOT_READY", "Cơ sở dữ liệu chưa được cấu hình.");
        }
    }
    @Override protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (AdminAccess.requireAdmin(request, response) == null || !AdminAccess.requireCsrf(request, response)) return;
        String path = request.getPathInfo();
        String id = path == null ? "" : path.substring(1);
        if (!id.matches("[a-z0-9]+(?:-[a-z0-9]+)*")) {
            ApiResponses.error(response, 422, "VALIDATION_ERROR", "ID không hợp lệ.");
            return;
        }
        try {
            if (!components.remove(id)) {
                ApiResponses.error(response, 404, "NOT_FOUND", "Không tìm thấy nội dung.");
                return;
            }
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (SQLException exception) {
            ApiResponses.error(response, 409, "RELATION_CONFLICT", "Không thể xóa nội dung đang được sử dụng.");
        }
    }
}
