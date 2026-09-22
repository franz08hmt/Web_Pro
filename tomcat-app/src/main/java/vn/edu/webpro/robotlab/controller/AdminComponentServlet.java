package vn.edu.webpro.robotlab.controller;

import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import vn.edu.webpro.robotlab.dao.ComponentDao;
import vn.edu.webpro.robotlab.dao.DatabaseConnectionFactory;
import vn.edu.webpro.robotlab.model.Component;
import vn.edu.webpro.robotlab.model.ComponentPage;
import vn.edu.webpro.robotlab.web.AdminAccess;
import vn.edu.webpro.robotlab.web.ApiResponses;
import vn.edu.webpro.robotlab.web.Json;

/** CRUD quản trị linh kiện; yêu cầu tài khoản ADMIN và CSRF khi ghi. */
@WebServlet("/api/admin/components/*")
public final class AdminComponentServlet extends HttpServlet {
    private static final int HTTP_UNPROCESSABLE_ENTITY = 422;
    private static final int LIST_LIMIT = 100;
    private static final int MAX_NAME = 150;
    private static final int MAX_CATEGORY = 100;
    private static final String ID_PATTERN = "[a-z0-9]+(?:-[a-z0-9]+)*";

    private final ComponentDao components =
            new ComponentDao(new DatabaseConnectionFactory(System.getenv()));

    /* HttpServlet không có doPatch(), nên PATCH được tách khỏi service(). */
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if ("PATCH".equals(request.getMethod())) {
            write(request, response, pathId(request), false);
            return;
        }
        super.service(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (AdminAccess.requireAdmin(request, response) == null) return;

        try {
            ComponentPage page = components.list(1, LIST_LIMIT);
            ApiResponses.json(response, HttpServletResponse.SC_OK, page.toJson());
        } catch (SQLException exception) {
            ApiResponses.error(response, HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "DEPENDENCY_NOT_READY", "Cơ sở dữ liệu chưa được cấu hình.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        write(request, response, null, true);
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (AdminAccess.requireAdmin(request, response) == null) return;
        if (!AdminAccess.requireCsrf(request, response)) return;

        String id = pathId(request) == null ? "" : pathId(request);
        if (!id.matches(ID_PATTERN)) {
            ApiResponses.error(response, HTTP_UNPROCESSABLE_ENTITY,
                    "VALIDATION_ERROR", "ID không hợp lệ.");
            return;
        }

        try {
            if (!components.remove(id)) {
                ApiResponses.error(response, HttpServletResponse.SC_NOT_FOUND,
                        "NOT_FOUND", "Không tìm thấy nội dung.");
                return;
            }
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (SQLException exception) {
            ApiResponses.error(response, HttpServletResponse.SC_CONFLICT,
                    "RELATION_CONFLICT", "Không thể xóa nội dung đang được sử dụng.");
        }
    }

    /** Dùng chung cho POST (tạo mới) và PATCH (cập nhật theo ID cũ). */
    private void write(HttpServletRequest request, HttpServletResponse response,
                       String existingId, boolean creating) throws IOException {
        if (AdminAccess.requireAdmin(request, response) == null) return;
        if (!AdminAccess.requireCsrf(request, response)) return;

        try {
            String body = request.getReader().lines().reduce("", (left, right) -> left + right);
            Component input = readComponent(body, creating ? null : existingId);
            Component saved = creating
                    ? components.create(input)
                    : components.update(existingId, input);

            if (saved == null) {
                ApiResponses.error(response, HttpServletResponse.SC_NOT_FOUND,
                        "NOT_FOUND", "Không tìm thấy nội dung.");
                return;
            }
            ApiResponses.json(response,
                    creating ? HttpServletResponse.SC_CREATED : HttpServletResponse.SC_OK,
                    "{\"data\":" + saved.toJson() + "}");
        } catch (IllegalArgumentException exception) {
            ApiResponses.error(response, HTTP_UNPROCESSABLE_ENTITY,
                    "VALIDATION_ERROR", "Dữ liệu linh kiện không hợp lệ.");
        } catch (SQLException exception) {
            ApiResponses.error(response, HttpServletResponse.SC_CONFLICT,
                    "CONFLICT", "Không thể lưu linh kiện.");
        }
    }

    /* Giới hạn độ dài khớp với cột trong database, để lỗi được chặn ngay tại
       biên HTTP thay vì để MySQL ném ra khi ghi. */
    private Component readComponent(String body, String existingId) {
        String id = existingId == null ? Json.stringField(body, "id").trim() : existingId;
        if (!id.matches(ID_PATTERN)) throw new IllegalArgumentException("id");

        String name = Json.stringField(body, "name").trim();
        String category = Json.stringField(body, "category").trim();
        String image = Json.stringField(body, "image").trim();
        String description = Json.stringField(body, "description").trim();
        String specs = Json.objectField(body, "specs");

        if (name.isEmpty() || name.length() > MAX_NAME
                || category.isEmpty() || category.length() > MAX_CATEGORY
                || image.isEmpty() || description.isEmpty()) {
            throw new IllegalArgumentException("fields");
        }
        return new Component(id, name, category, image, description, specs);
    }

    private String pathId(HttpServletRequest request) {
        String path = request.getPathInfo();
        return path == null ? null : path.substring(1);
    }
}
