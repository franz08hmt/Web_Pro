package vn.edu.webpro.robotlab.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import vn.edu.webpro.robotlab.business.TroubleshootingGuide;
import vn.edu.webpro.robotlab.data.TroubleshootingGuideDB;
import vn.edu.webpro.robotlab.util.JsonUtil;
import vn.edu.webpro.robotlab.util.ResponseUtil;
import vn.edu.webpro.robotlab.util.ValidationUtil;

/**
 * API công khai tra cứu lỗi lắp ráp — hướng dẫn kiểm tra do admin biên soạn,
 * không phải dữ liệu đọc trực tiếp từ robot thật.
 *
 * GET /api/troubleshooting-guides?page=&limit=&robotId=&componentGroup=&search=
 * Bỏ trống tham số lọc nào thì bỏ qua điều kiện đó — cùng khuôn với
 * LibraryResourceServlet.
 */
@WebServlet("/api/troubleshooting-guides")
public class TroubleshootingServlet extends HttpServlet {
    private static final int HTTP_UNPROCESSABLE_ENTITY = 422;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            int page = ValidationUtil.parsePositiveInt(request.getParameter("page"), 1, ValidationUtil.MAX_PAGE);
            int limit = ValidationUtil.parsePositiveInt(request.getParameter("limit"), 20, ValidationUtil.MAX_LIMIT);
            String robotId = emptyToNull(request.getParameter("robotId"));
            String componentGroup = emptyToNull(request.getParameter("componentGroup"));
            String search = emptyToNull(request.getParameter("search"));
            if (robotId != null && !ValidationUtil.isSlug(robotId)) {
                sendInvalid(response);
                return;
            }

            List<TroubleshootingGuide> guides = TroubleshootingGuideDB.selectGuides(
                    robotId, componentGroup, search, limit, (page - 1) * limit);
            StringBuilder items = new StringBuilder();
            for (TroubleshootingGuide guide : guides) {
                if (items.length() > 0) items.append(',');
                items.append(guide.toJson());
            }
            long total = TroubleshootingGuideDB.countGuides(robotId, componentGroup, search);

            StringBuilder groupsJson = new StringBuilder();
            for (String group : TroubleshootingGuideDB.selectComponentGroups()) {
                if (groupsJson.length() > 0) groupsJson.append(',');
                groupsJson.append(JsonUtil.quote(group));
            }

            ResponseUtil.sendJson(response, HttpServletResponse.SC_OK,
                    "{\"data\":[" + items + "],\"meta\":{\"page\":" + page + ",\"limit\":" + limit
                            + ",\"total\":" + total + ",\"componentGroups\":[" + groupsJson + "]}}");
        } catch (IllegalArgumentException e) {
            sendInvalid(response);
        } catch (SQLException e) {
            ResponseUtil.sendDatabaseUnavailable(response);
        }
    }

    private String emptyToNull(String value) {
        return (value == null || value.isEmpty()) ? null : value;
    }

    private void sendInvalid(HttpServletResponse response) throws IOException {
        ResponseUtil.sendError(response, HTTP_UNPROCESSABLE_ENTITY,
                "VALIDATION_ERROR", "Tham số tra cứu không hợp lệ.");
    }
}
