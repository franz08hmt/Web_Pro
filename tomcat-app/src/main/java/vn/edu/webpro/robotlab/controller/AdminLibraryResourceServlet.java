package vn.edu.webpro.robotlab.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import vn.edu.webpro.robotlab.dao.DatabaseConnectionFactory;
import vn.edu.webpro.robotlab.dao.LibraryResourceDao;
import vn.edu.webpro.robotlab.model.LibraryResource;
import vn.edu.webpro.robotlab.web.AdminAccess;
import vn.edu.webpro.robotlab.web.ApiResponses;
import vn.edu.webpro.robotlab.web.Json;

/** CRUD quản trị tài nguyên thư viện; yêu cầu tài khoản ADMIN và CSRF khi ghi. */
@WebServlet("/api/admin/library-resources/*")
public final class AdminLibraryResourceServlet extends HttpServlet {
    private static final int HTTP_UNPROCESSABLE_ENTITY = 422;
    private static final int LIST_LIMIT = 100;
    private static final Set<String> TYPES = Set.of("image", "document", "link");
    private static final String ID_PATTERN = "[a-z0-9]+(?:-[a-z0-9]+)*";

    private final LibraryResourceDao resources =
            new LibraryResourceDao(new DatabaseConnectionFactory(System.getenv()));

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
            List<LibraryResource> data = resources.list(LIST_LIMIT, 0, null);
            String json = data.stream()
                    .map(LibraryResource::toJson)
                    .reduce((left, right) -> left + "," + right)
                    .orElse("");
            ApiResponses.json(response, HttpServletResponse.SC_OK,
                    "{\"data\":[" + json + "],\"meta\":{\"page\":1,\"limit\":" + LIST_LIMIT
                            + ",\"total\":" + data.size() + "}}");
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

        try {
            if (!resources.remove(pathId(request))) {
                ApiResponses.error(response, HttpServletResponse.SC_NOT_FOUND,
                        "NOT_FOUND", "Không tìm thấy nội dung.");
                return;
            }
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (SQLException exception) {
            ApiResponses.error(response, HttpServletResponse.SC_CONFLICT,
                    "RELATION_CONFLICT", "Không thể xóa tài nguyên đang được sử dụng.");
        }
    }

    /** Dùng chung cho POST (tạo mới) và PATCH (cập nhật theo ID cũ). */
    private void write(HttpServletRequest request, HttpServletResponse response,
                       String existingId, boolean creating) throws IOException {
        if (AdminAccess.requireAdmin(request, response) == null) return;
        if (!AdminAccess.requireCsrf(request, response)) return;

        try {
            String body = request.getReader().lines().reduce("", (left, right) -> left + right);
            LibraryResource input = readResource(body, creating ? null : existingId);
            LibraryResource saved = creating
                    ? resources.create(input)
                    : resources.update(existingId, input);

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
                    "VALIDATION_ERROR", "Dữ liệu tài nguyên không hợp lệ.");
        } catch (SQLException exception) {
            ApiResponses.error(response, HttpServletResponse.SC_CONFLICT,
                    "CONFLICT", "Không thể lưu tài nguyên.");
        }
    }

    private LibraryResource readResource(String body, String existingId) {
        String id = existingId == null ? Json.stringField(body, "id").trim() : existingId;
        String robotId = Json.stringField(body, "robotId").trim();
        String title = Json.stringField(body, "title").trim();
        String type = Json.stringField(body, "type").trim();
        String url = Json.stringField(body, "url").trim();
        String description = Json.stringField(body, "description").trim();

        if (!id.matches(ID_PATTERN) || !TYPES.contains(type)
                || title.isEmpty() || url.isEmpty() || description.isEmpty()) {
            throw new IllegalArgumentException("fields");
        }
        // Tài nguyên dùng chung cho mọi robot thì robotId để trống, lưu NULL.
        return new LibraryResource(id, robotId.isEmpty() ? null : robotId,
                title, type, url, description);
    }

    private String pathId(HttpServletRequest request) {
        String path = request.getPathInfo();
        return path == null ? "" : path.substring(1);
    }
}
