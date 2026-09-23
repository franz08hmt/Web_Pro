package vn.edu.webpro.robotlab.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import vn.edu.webpro.robotlab.business.LibraryResource;
import vn.edu.webpro.robotlab.data.LibraryResourceDB;
import vn.edu.webpro.robotlab.util.JsonUtil;
import vn.edu.webpro.robotlab.util.ResponseUtil;
import vn.edu.webpro.robotlab.util.SessionUtil;
import vn.edu.webpro.robotlab.util.ValidationUtil;

/** CRUD quản trị tài nguyên thư viện; cần tài khoản ADMIN, thao tác ghi cần thêm CSRF token. */
@WebServlet("/api/admin/library-resources/*")
public class AdminLibraryResourceServlet extends HttpServlet {
    private static final int HTTP_UNPROCESSABLE_ENTITY = 422;
    private static final int LIST_LIMIT = 100;
    private static final int MAX_BODY = 65536;

    /* HttpServlet không có doPatch(), nên PATCH được tách ra trong service(). */
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if ("PATCH".equals(request.getMethod())) {
            save(request, response, false);
            return;
        }
        super.service(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (SessionUtil.requireAdmin(request, response) == null) return;

        try {
            List<LibraryResource> resources = LibraryResourceDB.selectResources(LIST_LIMIT, 0, null);
            StringBuilder items = new StringBuilder();
            for (LibraryResource resource : resources) {
                if (items.length() > 0) items.append(',');
                items.append(resource.toJson());
            }
            ResponseUtil.sendJson(response, HttpServletResponse.SC_OK,
                    "{\"data\":[" + items + "],\"meta\":{\"page\":1,\"limit\":" + LIST_LIMIT
                            + ",\"total\":" + resources.size() + "}}");
        } catch (SQLException e) {
            ResponseUtil.sendDatabaseUnavailable(response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        save(request, response, true);
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (SessionUtil.requireAdmin(request, response) == null) return;
        if (!SessionUtil.hasValidCsrfToken(request, response)) return;

        try {
            if (LibraryResourceDB.delete(pathId(request)) == 0) {
                sendNotFound(response);
                return;
            }
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (SQLException e) {
            ResponseUtil.sendError(response, HttpServletResponse.SC_CONFLICT,
                    "RELATION_CONFLICT", "Không thể xóa tài nguyên đang được sử dụng.");
        }
    }

    /** Dùng chung cho POST (thêm mới) và PATCH (sửa theo ID trên đường dẫn). */
    private void save(HttpServletRequest request, HttpServletResponse response, boolean creating)
            throws IOException {
        if (SessionUtil.requireAdmin(request, response) == null) return;
        if (!SessionUtil.hasValidCsrfToken(request, response)) return;

        try {
            // get parameters from the request and store them in a LibraryResource object
            String body = JsonUtil.readBody(request.getReader(), MAX_BODY);
            String robotId = JsonUtil.stringField(body, "robotId").trim();

            LibraryResource resource = new LibraryResource();
            resource.setId(creating ? JsonUtil.stringField(body, "id").trim() : pathId(request));
            // Tài nguyên dùng chung cho mọi robot thì để trống robotId, lưu NULL.
            resource.setRobotId(robotId.isEmpty() ? null : robotId);
            resource.setTitle(JsonUtil.stringField(body, "title").trim());
            resource.setType(JsonUtil.stringField(body, "type").trim());
            resource.setUrl(JsonUtil.stringField(body, "url").trim());
            resource.setDescription(JsonUtil.stringField(body, "description").trim());

            // validate the parameters
            if (!ValidationUtil.isSlug(resource.getId()) || !LibraryResource.isValidType(resource.getType())
                    || resource.getTitle().isEmpty() || resource.getUrl().isEmpty()
                    || resource.getDescription().isEmpty()) {
                sendInvalid(response);
                return;
            }

            int count = creating ? LibraryResourceDB.insert(resource) : LibraryResourceDB.update(resource);
            if (count == 0) {
                sendNotFound(response);
                return;
            }
            ResponseUtil.sendJson(response,
                    creating ? HttpServletResponse.SC_CREATED : HttpServletResponse.SC_OK,
                    "{\"data\":" + resource.toJson() + "}");
        } catch (IllegalArgumentException e) {
            sendInvalid(response);
        } catch (SQLException e) {
            ResponseUtil.sendError(response, HttpServletResponse.SC_CONFLICT,
                    "CONFLICT", "Không thể lưu tài nguyên.");
        }
    }

    private String pathId(HttpServletRequest request) {
        String path = request.getPathInfo();
        return path == null ? "" : path.substring(1);
    }

    private void sendInvalid(HttpServletResponse response) throws IOException {
        ResponseUtil.sendError(response, HTTP_UNPROCESSABLE_ENTITY,
                "VALIDATION_ERROR", "Dữ liệu tài nguyên không hợp lệ.");
    }

    private void sendNotFound(HttpServletResponse response) throws IOException {
        ResponseUtil.sendError(response, HttpServletResponse.SC_NOT_FOUND,
                "NOT_FOUND", "Không tìm thấy nội dung.");
    }
}
