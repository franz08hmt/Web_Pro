package vn.edu.webpro.robotlab.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import vn.edu.webpro.robotlab.business.Robot;
import vn.edu.webpro.robotlab.data.RobotDB;
import vn.edu.webpro.robotlab.util.JsonUtil;
import vn.edu.webpro.robotlab.util.ResponseUtil;
import vn.edu.webpro.robotlab.util.SessionUtil;
import vn.edu.webpro.robotlab.util.ValidationUtil;

/**
 * CRUD quản trị mô hình robot: GET (xem), POST (thêm), PATCH (sửa), DELETE (xóa).
 * Mọi thao tác cần tài khoản ADMIN; thao tác ghi còn cần CSRF token.
 */
@WebServlet("/api/admin/robots/*")
public class AdminRobotServlet extends HttpServlet {
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
            List<Robot> robots = RobotDB.selectRobots(LIST_LIMIT, 0);
            StringBuilder items = new StringBuilder();
            for (Robot robot : robots) {
                if (items.length() > 0) items.append(',');
                items.append(robot.toJson());
            }
            ResponseUtil.sendJson(response, HttpServletResponse.SC_OK,
                    "{\"data\":[" + items + "],\"meta\":{\"page\":1,\"limit\":" + LIST_LIMIT
                            + ",\"total\":" + RobotDB.countRobots() + "}}");
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
            if (RobotDB.delete(pathId(request)) == 0) {
                sendNotFound(response);
                return;
            }
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (SQLException e) {
            // Khóa ngoại chặn xóa khi robot còn được tham chiếu ở bảng khác.
            ResponseUtil.sendError(response, HttpServletResponse.SC_CONFLICT,
                    "RELATION_CONFLICT", "Không thể xóa robot đang được sử dụng.");
        }
    }

    /** Dùng chung cho POST (thêm mới) và PATCH (sửa theo ID trên đường dẫn). */
    private void save(HttpServletRequest request, HttpServletResponse response, boolean creating)
            throws IOException {
        if (SessionUtil.requireAdmin(request, response) == null) return;
        if (!SessionUtil.hasValidCsrfToken(request, response)) return;

        try {
            // get parameters from the request and store them in a Robot object
            String body = JsonUtil.readBody(request.getReader(), MAX_BODY);
            Robot robot = new Robot();
            robot.setId(creating ? JsonUtil.stringField(body, "id").trim() : pathId(request));
            robot.setName(JsonUtil.stringField(body, "name").trim());
            robot.setLevel(JsonUtil.stringField(body, "level").trim());
            robot.setSummary(JsonUtil.stringField(body, "summary").trim());
            robot.setImage(JsonUtil.stringField(body, "image").trim());
            robot.setBuildTime(JsonUtil.stringField(body, "buildTime").trim());
            robot.setMainSensor(JsonUtil.stringField(body, "mainSensor").trim());
            robot.setSkills(JsonUtil.stringField(body, "skills").trim());
            robot.setWiringJson(JsonUtil.arrayField(body, "wiring"));

            // validate the parameters
            if (!ValidationUtil.isSlug(robot.getId()) || !Robot.isValidLevel(robot.getLevel())
                    || robot.getName().isEmpty() || robot.getSummary().isEmpty()) {
                sendInvalid(response);
                return;
            }

            int count = creating ? RobotDB.insert(robot) : RobotDB.update(robot);
            if (count == 0) {
                sendNotFound(response);
                return;
            }
            ResponseUtil.sendJson(response,
                    creating ? HttpServletResponse.SC_CREATED : HttpServletResponse.SC_OK,
                    "{\"data\":" + robot.toJson() + "}");
        } catch (IllegalArgumentException e) {
            sendInvalid(response);
        } catch (SQLException e) {
            ResponseUtil.sendError(response, HttpServletResponse.SC_CONFLICT,
                    "CONFLICT", "Không thể lưu robot.");
        }
    }

    private String pathId(HttpServletRequest request) {
        String path = request.getPathInfo();
        return path == null ? "" : path.substring(1);
    }

    private void sendInvalid(HttpServletResponse response) throws IOException {
        ResponseUtil.sendError(response, HTTP_UNPROCESSABLE_ENTITY,
                "VALIDATION_ERROR", "Dữ liệu robot không hợp lệ.");
    }

    private void sendNotFound(HttpServletResponse response) throws IOException {
        ResponseUtil.sendError(response, HttpServletResponse.SC_NOT_FOUND,
                "NOT_FOUND", "Không tìm thấy nội dung.");
    }
}
