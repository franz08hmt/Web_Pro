package vn.edu.webpro.robotlab.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import vn.edu.webpro.robotlab.business.TroubleshootingGuide;
import vn.edu.webpro.robotlab.data.ComponentDB;
import vn.edu.webpro.robotlab.data.RobotDB;
import vn.edu.webpro.robotlab.data.TroubleshootingGuideDB;
import vn.edu.webpro.robotlab.util.JsonUtil;
import vn.edu.webpro.robotlab.util.ResponseUtil;
import vn.edu.webpro.robotlab.util.SessionUtil;
import vn.edu.webpro.robotlab.util.ValidationUtil;

/**
 * CRUD quản trị nội dung tra cứu lỗi lắp ráp: GET (xem), POST (thêm), PATCH
 * (sửa), DELETE (xóa). Mọi thao tác cần tài khoản ADMIN; thao tác ghi còn cần
 * CSRF token — cùng khuôn với AdminRobotServlet.
 */
@WebServlet("/api/admin/troubleshooting-guides/*")
public class AdminTroubleshootingServlet extends HttpServlet {
    private static final int HTTP_UNPROCESSABLE_ENTITY = 422;
    private static final int LIST_LIMIT = 100;
    private static final int MAX_BODY = 16384;

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
            String id = pathId(request);
            if (!id.isEmpty()) {
                TroubleshootingGuide guide = TroubleshootingGuideDB.selectGuide(id);
                if (guide == null) {
                    sendNotFound(response);
                    return;
                }
                ResponseUtil.sendJson(response, HttpServletResponse.SC_OK, "{\"data\":" + guide.toJson() + "}");
                return;
            }

            List<TroubleshootingGuide> guides = TroubleshootingGuideDB.selectGuides(null, null, null, LIST_LIMIT, 0);
            StringBuilder items = new StringBuilder();
            for (TroubleshootingGuide guide : guides) {
                if (items.length() > 0) items.append(',');
                items.append(guide.toJson());
            }
            ResponseUtil.sendJson(response, HttpServletResponse.SC_OK,
                    "{\"data\":[" + items + "],\"meta\":{\"total\":"
                            + TroubleshootingGuideDB.countGuides(null, null, null) + "}}");
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
            if (TroubleshootingGuideDB.delete(pathId(request)) == 0) {
                sendNotFound(response);
                return;
            }
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (SQLException e) {
            ResponseUtil.sendError(response, HttpServletResponse.SC_CONFLICT,
                    "CONFLICT", "Không thể xóa nội dung.");
        }
    }

    private void save(HttpServletRequest request, HttpServletResponse response, boolean creating)
            throws IOException {
        if (SessionUtil.requireAdmin(request, response) == null) return;
        if (!SessionUtil.hasValidCsrfToken(request, response)) return;

        try {
            String body = JsonUtil.readBody(request.getReader(), MAX_BODY);
            TroubleshootingGuide guide = new TroubleshootingGuide();
            guide.setId(creating ? JsonUtil.stringField(body, "id").trim() : pathId(request));
            String robotId = JsonUtil.stringField(body, "robotId").trim();
            guide.setRobotId(robotId.isEmpty() ? null : robotId);
            guide.setComponentGroup(JsonUtil.stringField(body, "componentGroup").trim());
            guide.setSymptom(JsonUtil.stringField(body, "symptom").trim());
            guide.setPossibleCauses(JsonUtil.stringField(body, "possibleCauses").trim());
            guide.setResolutionSteps(JsonUtil.stringField(body, "resolutionSteps").trim());
            String relatedComponentId = JsonUtil.stringField(body, "relatedComponentId").trim();
            guide.setRelatedComponentId(relatedComponentId.isEmpty() ? null : relatedComponentId);
            guide.setDisplayOrder(Integer.parseInt(JsonUtil.stringField(body, "displayOrder")));

            if (!ValidationUtil.isSlug(guide.getId())
                    || (guide.getRobotId() != null
                        && (!ValidationUtil.isSlug(guide.getRobotId()) || !RobotDB.robotExists(guide.getRobotId())))
                    || (guide.getRelatedComponentId() != null && !ComponentDB.exists(guide.getRelatedComponentId()))
                    || guide.getComponentGroup().isEmpty() || guide.getSymptom().isEmpty()
                    || guide.getPossibleCauses().isEmpty() || guide.getResolutionSteps().isEmpty()) {
                sendInvalid(response);
                return;
            }

            int count = creating ? TroubleshootingGuideDB.insert(guide) : TroubleshootingGuideDB.update(guide);
            if (count == 0) {
                sendNotFound(response);
                return;
            }
            ResponseUtil.sendJson(response,
                    creating ? HttpServletResponse.SC_CREATED : HttpServletResponse.SC_OK,
                    "{\"data\":" + guide.toJson() + "}");
        } catch (IllegalArgumentException e) {
            sendInvalid(response);
        } catch (SQLException e) {
            ResponseUtil.sendError(response, HttpServletResponse.SC_CONFLICT,
                    "CONFLICT", "Không thể lưu nội dung.");
        }
    }

    private String pathId(HttpServletRequest request) {
        String path = request.getPathInfo();
        return path == null ? "" : path.substring(1);
    }

    private void sendInvalid(HttpServletResponse response) throws IOException {
        ResponseUtil.sendError(response, HTTP_UNPROCESSABLE_ENTITY,
                "VALIDATION_ERROR", "Dữ liệu tra cứu lỗi không hợp lệ.");
    }

    private void sendNotFound(HttpServletResponse response) throws IOException {
        ResponseUtil.sendError(response, HttpServletResponse.SC_NOT_FOUND,
                "NOT_FOUND", "Không tìm thấy nội dung.");
    }
}
