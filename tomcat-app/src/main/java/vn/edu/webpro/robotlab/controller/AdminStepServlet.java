package vn.edu.webpro.robotlab.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import vn.edu.webpro.robotlab.dao.AssemblyStepDao;
import vn.edu.webpro.robotlab.dao.DatabaseConnectionFactory;
import vn.edu.webpro.robotlab.model.AssemblyStep;
import vn.edu.webpro.robotlab.web.AdminAccess;
import vn.edu.webpro.robotlab.web.ApiResponses;
import vn.edu.webpro.robotlab.web.Json;

/** CRUD quản trị các bước lắp ráp; yêu cầu tài khoản ADMIN và CSRF khi ghi. */
@WebServlet("/api/admin/steps/*")
public final class AdminStepServlet extends HttpServlet {
    private static final int HTTP_UNPROCESSABLE_ENTITY = 422;
    private static final int LIST_LIMIT = 100;
    private static final String ID_PATTERN = "[a-z0-9]+(?:-[a-z0-9]+)*";

    private final AssemblyStepDao steps =
            new AssemblyStepDao(new DatabaseConnectionFactory(System.getenv()));

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
            List<AssemblyStep> data = steps.list(LIST_LIMIT, 0);
            String json = data.stream()
                    .map(AssemblyStep::toJson)
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
            if (!steps.remove(pathId(request))) {
                ApiResponses.error(response, HttpServletResponse.SC_NOT_FOUND,
                        "NOT_FOUND", "Không tìm thấy nội dung.");
                return;
            }
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (SQLException exception) {
            ApiResponses.error(response, HttpServletResponse.SC_CONFLICT,
                    "RELATION_CONFLICT", "Không thể xóa bước đang được sử dụng.");
        }
    }

    /** Dùng chung cho POST (tạo mới) và PATCH (cập nhật theo ID cũ). */
    private void write(HttpServletRequest request, HttpServletResponse response,
                       String existingId, boolean creating) throws IOException {
        if (AdminAccess.requireAdmin(request, response) == null) return;
        if (!AdminAccess.requireCsrf(request, response)) return;

        try {
            String body = request.getReader().lines().reduce("", (left, right) -> left + right);
            AssemblyStep input = readStep(body, creating ? null : existingId);
            AssemblyStep saved = creating ? steps.create(input) : steps.update(existingId, input);

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
                    "VALIDATION_ERROR", "Dữ liệu bước lắp ráp không hợp lệ.");
        } catch (SQLException exception) {
            ApiResponses.error(response, HttpServletResponse.SC_CONFLICT,
                    "CONFLICT", "Không thể lưu bước lắp ráp.");
        }
    }

    private AssemblyStep readStep(String body, String existingId) {
        String id = existingId == null ? Json.stringField(body, "id").trim() : existingId;
        String robotId = Json.stringField(body, "robotId").trim();
        String title = Json.stringField(body, "title").trim();
        String instruction = Json.stringField(body, "instruction").trim();
        String illustration = Json.objectField(body, "illustration");
        int stepOrder = Integer.parseInt(Json.stringField(body, "stepOrder"));

        if (!id.matches(ID_PATTERN) || !robotId.matches(ID_PATTERN)
                || stepOrder < 1 || title.isEmpty() || instruction.isEmpty()) {
            throw new IllegalArgumentException("fields");
        }
        return new AssemblyStep(id, robotId, stepOrder, title, instruction, illustration);
    }

    private String pathId(HttpServletRequest request) {
        String path = request.getPathInfo();
        return path == null ? "" : path.substring(1);
    }
}
