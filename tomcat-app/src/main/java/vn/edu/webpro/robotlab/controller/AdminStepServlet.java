package vn.edu.webpro.robotlab.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import vn.edu.webpro.robotlab.business.AssemblyStep;
import vn.edu.webpro.robotlab.data.AssemblyStepDB;
import vn.edu.webpro.robotlab.util.JsonUtil;
import vn.edu.webpro.robotlab.util.ResponseUtil;
import vn.edu.webpro.robotlab.util.SessionUtil;
import vn.edu.webpro.robotlab.util.ValidationUtil;

/** CRUD quản trị các bước lắp ráp; cần tài khoản ADMIN, thao tác ghi cần thêm CSRF token. */
@WebServlet("/api/admin/steps/*")
public class AdminStepServlet extends HttpServlet {
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
            List<AssemblyStep> steps = AssemblyStepDB.selectSteps(LIST_LIMIT, 0);
            StringBuilder items = new StringBuilder();
            for (AssemblyStep step : steps) {
                if (items.length() > 0) items.append(',');
                items.append(step.toJson());
            }
            ResponseUtil.sendJson(response, HttpServletResponse.SC_OK,
                    "{\"data\":[" + items + "],\"meta\":{\"page\":1,\"limit\":" + LIST_LIMIT
                            + ",\"total\":" + steps.size() + "}}");
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
            if (AssemblyStepDB.delete(pathId(request)) == 0) {
                sendNotFound(response);
                return;
            }
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (SQLException e) {
            ResponseUtil.sendError(response, HttpServletResponse.SC_CONFLICT,
                    "RELATION_CONFLICT", "Không thể xóa bước đang được sử dụng.");
        }
    }

    /** Dùng chung cho POST (thêm mới) và PATCH (sửa theo ID trên đường dẫn). */
    private void save(HttpServletRequest request, HttpServletResponse response, boolean creating)
            throws IOException {
        if (SessionUtil.requireAdmin(request, response) == null) return;
        if (!SessionUtil.hasValidCsrfToken(request, response)) return;

        try {
            // get parameters from the request and store them in an AssemblyStep object
            String body = JsonUtil.readBody(request.getReader(), MAX_BODY);
            AssemblyStep step = new AssemblyStep();
            step.setId(creating ? JsonUtil.stringField(body, "id").trim() : pathId(request));
            step.setRobotId(JsonUtil.stringField(body, "robotId").trim());
            step.setStepOrder(Integer.parseInt(JsonUtil.stringField(body, "stepOrder")));
            step.setTitle(JsonUtil.stringField(body, "title").trim());
            step.setInstruction(JsonUtil.stringField(body, "instruction").trim());
            step.setIllustrationJson(JsonUtil.objectField(body, "illustration"));

            // validate the parameters
            if (!ValidationUtil.isSlug(step.getId()) || !ValidationUtil.isSlug(step.getRobotId())
                    || step.getStepOrder() < 1
                    || step.getTitle().isEmpty() || step.getInstruction().isEmpty()) {
                sendInvalid(response);
                return;
            }

            int count = creating ? AssemblyStepDB.insert(step) : AssemblyStepDB.update(step);
            if (count == 0) {
                sendNotFound(response);
                return;
            }
            ResponseUtil.sendJson(response,
                    creating ? HttpServletResponse.SC_CREATED : HttpServletResponse.SC_OK,
                    "{\"data\":" + step.toJson() + "}");
        } catch (IllegalArgumentException e) {
            // Bao gồm cả NumberFormatException khi stepOrder không phải số.
            sendInvalid(response);
        } catch (SQLException e) {
            ResponseUtil.sendError(response, HttpServletResponse.SC_CONFLICT,
                    "CONFLICT", "Không thể lưu bước lắp ráp.");
        }
    }

    private String pathId(HttpServletRequest request) {
        String path = request.getPathInfo();
        return path == null ? "" : path.substring(1);
    }

    private void sendInvalid(HttpServletResponse response) throws IOException {
        ResponseUtil.sendError(response, HTTP_UNPROCESSABLE_ENTITY,
                "VALIDATION_ERROR", "Dữ liệu bước lắp ráp không hợp lệ.");
    }

    private void sendNotFound(HttpServletResponse response) throws IOException {
        ResponseUtil.sendError(response, HttpServletResponse.SC_NOT_FOUND,
                "NOT_FOUND", "Không tìm thấy nội dung.");
    }
}
