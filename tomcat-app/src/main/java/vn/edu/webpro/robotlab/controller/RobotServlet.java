package vn.edu.webpro.robotlab.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import vn.edu.webpro.robotlab.business.AssemblyStep;
import vn.edu.webpro.robotlab.business.Robot;
import vn.edu.webpro.robotlab.business.RobotComponent;
import vn.edu.webpro.robotlab.data.AssemblyStepDB;
import vn.edu.webpro.robotlab.data.RobotDB;
import vn.edu.webpro.robotlab.util.ResponseUtil;
import vn.edu.webpro.robotlab.util.ValidationUtil;

/**
 * API công khai cho danh mục robot (trả JSON cho JavaScript của giao diện).
 *
 * GET /api/robots?page=&limit=        danh sách có phân trang
 * GET /api/robots/{id}/components     linh kiện bắt buộc của một robot
 * GET /api/robots/{id}/steps          các bước lắp ráp của một robot
 */
@WebServlet("/api/robots/*")
public class RobotServlet extends HttpServlet {
    private static final int HTTP_UNPROCESSABLE_ENTITY = 422;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            int page = ValidationUtil.parsePositiveInt(request.getParameter("page"), 1, ValidationUtil.MAX_PAGE);
            int limit = ValidationUtil.parsePositiveInt(request.getParameter("limit"), 20, ValidationUtil.MAX_LIMIT);
            int offset = (page - 1) * limit;
            String path = request.getPathInfo();

            if (path == null || "/".equals(path)) {
                List<Robot> robots = RobotDB.selectRobots(limit, offset);
                StringBuilder items = new StringBuilder();
                for (Robot robot : robots) {
                    if (items.length() > 0) items.append(',');
                    items.append(robot.toJson());
                }
                sendPage(response, items, page, limit, RobotDB.countRobots());
                return;
            }

            // Phần còn lại của đường dẫn có dạng {robotId}/{components|steps}.
            String[] segments = path.substring(1).split("/");
            if (segments.length != 2) {
                sendNotFound(response, "Không tìm thấy tài nguyên yêu cầu.");
                return;
            }

            String robotId = segments[0];
            if (!ValidationUtil.isSlug(robotId) || !RobotDB.robotExists(robotId)) {
                sendNotFound(response, "Không tìm thấy nội dung.");
                return;
            }

            if ("components".equals(segments[1])) {
                StringBuilder items = new StringBuilder();
                for (RobotComponent part : RobotDB.selectRobotComponents(robotId)) {
                    if (items.length() > 0) items.append(',');
                    items.append(part.toJson());
                }
                ResponseUtil.sendJson(response, HttpServletResponse.SC_OK, "{\"data\":[" + items + "]}");
            } else if ("steps".equals(segments[1])) {
                StringBuilder items = new StringBuilder();
                for (AssemblyStep step : AssemblyStepDB.selectSteps(robotId, limit, offset)) {
                    if (items.length() > 0) items.append(',');
                    items.append(step.toJson());
                }
                sendPage(response, items, page, limit, AssemblyStepDB.countSteps(robotId));
            } else {
                sendNotFound(response, "Không tìm thấy tài nguyên yêu cầu.");
            }
        } catch (IllegalArgumentException e) {
            ResponseUtil.sendError(response, HTTP_UNPROCESSABLE_ENTITY,
                    "VALIDATION_ERROR", "Phân trang không hợp lệ.");
        } catch (SQLException e) {
            ResponseUtil.sendDatabaseUnavailable(response);
        }
    }

    /** Envelope data + meta theo docs/API_CONVENTIONS.md. */
    private void sendPage(HttpServletResponse response, StringBuilder items, int page, int limit, long total)
            throws IOException {
        ResponseUtil.sendJson(response, HttpServletResponse.SC_OK,
                "{\"data\":[" + items + "],\"meta\":{\"page\":" + page
                        + ",\"limit\":" + limit + ",\"total\":" + total + "}}");
    }

    private void sendNotFound(HttpServletResponse response, String message) throws IOException {
        ResponseUtil.sendError(response, HttpServletResponse.SC_NOT_FOUND, "NOT_FOUND", message);
    }
}
