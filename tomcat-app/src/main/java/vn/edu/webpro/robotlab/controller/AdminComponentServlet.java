package vn.edu.webpro.robotlab.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import vn.edu.webpro.robotlab.business.Component;
import vn.edu.webpro.robotlab.data.ComponentDB;
import vn.edu.webpro.robotlab.util.JsonUtil;
import vn.edu.webpro.robotlab.util.ResponseUtil;
import vn.edu.webpro.robotlab.util.SessionUtil;
import vn.edu.webpro.robotlab.util.ValidationUtil;

/** CRUD quản trị linh kiện; cần tài khoản ADMIN, thao tác ghi cần thêm CSRF token. */
@WebServlet("/api/admin/components/*")
public class AdminComponentServlet extends HttpServlet {
    private static final int HTTP_UNPROCESSABLE_ENTITY = 422;
    private static final int LIST_LIMIT = 100;
    private static final int MAX_BODY = 65536;
    private static final int MAX_NAME = 150;
    private static final int MAX_CATEGORY = 100;

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
            List<Component> components = ComponentDB.selectComponents(LIST_LIMIT, 0);
            StringBuilder items = new StringBuilder();
            for (Component component : components) {
                if (items.length() > 0) items.append(',');
                items.append(component.toJson());
            }
            ResponseUtil.sendJson(response, HttpServletResponse.SC_OK,
                    "{\"data\":[" + items + "],\"meta\":{\"page\":1,\"limit\":" + LIST_LIMIT
                            + ",\"total\":" + ComponentDB.countComponents() + "}}");
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

        String id = pathId(request);
        if (!ValidationUtil.isSlug(id)) {
            ResponseUtil.sendError(response, HTTP_UNPROCESSABLE_ENTITY, "VALIDATION_ERROR", "ID không hợp lệ.");
            return;
        }

        try {
            if (ComponentDB.delete(id) == 0) {
                sendNotFound(response);
                return;
            }
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (SQLException e) {
            ResponseUtil.sendError(response, HttpServletResponse.SC_CONFLICT,
                    "RELATION_CONFLICT", "Không thể xóa nội dung đang được sử dụng.");
        }
    }

    /** Dùng chung cho POST (thêm mới) và PATCH (sửa theo ID trên đường dẫn). */
    private void save(HttpServletRequest request, HttpServletResponse response, boolean creating)
            throws IOException {
        if (SessionUtil.requireAdmin(request, response) == null) return;
        if (!SessionUtil.hasValidCsrfToken(request, response)) return;

        try {
            // get parameters from the request and store them in a Component object
            String body = JsonUtil.readBody(request.getReader(), MAX_BODY);
            Component component = new Component();
            component.setId(creating ? JsonUtil.stringField(body, "id").trim() : pathId(request));
            component.setName(JsonUtil.stringField(body, "name").trim());
            component.setCategory(JsonUtil.stringField(body, "category").trim());
            component.setImage(JsonUtil.stringField(body, "image").trim());
            component.setDescription(JsonUtil.stringField(body, "description").trim());
            component.setSpecsJson(JsonUtil.objectField(body, "specs"));

            // validate the parameters; độ dài khớp giới hạn cột trong database
            String name = component.getName();
            String category = component.getCategory();
            if (!ValidationUtil.isSlug(component.getId())
                    || name.isEmpty() || name.length() > MAX_NAME
                    || category.isEmpty() || category.length() > MAX_CATEGORY
                    || component.getImage().isEmpty() || component.getDescription().isEmpty()) {
                sendInvalid(response);
                return;
            }

            int count = creating ? ComponentDB.insert(component) : ComponentDB.update(component);
            if (count == 0) {
                sendNotFound(response);
                return;
            }
            ResponseUtil.sendJson(response,
                    creating ? HttpServletResponse.SC_CREATED : HttpServletResponse.SC_OK,
                    "{\"data\":" + component.toJson() + "}");
        } catch (IllegalArgumentException e) {
            sendInvalid(response);
        } catch (SQLException e) {
            ResponseUtil.sendError(response, HttpServletResponse.SC_CONFLICT,
                    "CONFLICT", "Không thể lưu linh kiện.");
        }
    }

    private String pathId(HttpServletRequest request) {
        String path = request.getPathInfo();
        return path == null ? "" : path.substring(1);
    }

    private void sendInvalid(HttpServletResponse response) throws IOException {
        ResponseUtil.sendError(response, HTTP_UNPROCESSABLE_ENTITY,
                "VALIDATION_ERROR", "Dữ liệu linh kiện không hợp lệ.");
    }

    private void sendNotFound(HttpServletResponse response) throws IOException {
        ResponseUtil.sendError(response, HttpServletResponse.SC_NOT_FOUND,
                "NOT_FOUND", "Không tìm thấy nội dung.");
    }
}
