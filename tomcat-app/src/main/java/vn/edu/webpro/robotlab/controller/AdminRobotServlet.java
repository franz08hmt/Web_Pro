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
import vn.edu.webpro.robotlab.dao.RobotDao;
import vn.edu.webpro.robotlab.model.Robot;
import vn.edu.webpro.robotlab.web.AdminAccess;
import vn.edu.webpro.robotlab.web.ApiResponses;
import vn.edu.webpro.robotlab.web.Json;

/**
 * CRUD quản trị mô hình robot.
 *
 * Mọi phương thức đều đi qua AdminAccess: phải đăng nhập bằng tài khoản ADMIN,
 * và các thao tác ghi còn phải kèm CSRF token khớp với phiên.
 */
@WebServlet("/api/admin/robots/*")
public final class AdminRobotServlet extends HttpServlet {
    private static final int HTTP_UNPROCESSABLE_ENTITY = 422;
    private static final int LIST_LIMIT = 100;
    private static final Set<String> LEVELS = Set.of("Cơ bản", "Trung bình", "Nâng cao");
    private static final String ID_PATTERN = "[a-z0-9]+(?:-[a-z0-9]+)*";

    private final RobotDao robots = new RobotDao(new DatabaseConnectionFactory(System.getenv()));

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
            List<Robot> data = robots.list(LIST_LIMIT, 0);
            String json = data.stream()
                    .map(Robot::toJson)
                    .reduce((left, right) -> left + "," + right)
                    .orElse("");
            ApiResponses.json(response, HttpServletResponse.SC_OK,
                    "{\"data\":[" + json + "],\"meta\":{\"page\":1,\"limit\":" + LIST_LIMIT
                            + ",\"total\":" + robots.count() + "}}");
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
        try {
            if (!robots.remove(id)) {
                ApiResponses.error(response, HttpServletResponse.SC_NOT_FOUND,
                        "NOT_FOUND", "Không tìm thấy nội dung.");
                return;
            }
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (SQLException exception) {
            // Khóa ngoại chặn xóa khi robot còn được tham chiếu ở bảng khác.
            ApiResponses.error(response, HttpServletResponse.SC_CONFLICT,
                    "RELATION_CONFLICT", "Không thể xóa robot đang được sử dụng.");
        }
    }

    /** Dùng chung cho POST (tạo mới) và PATCH (cập nhật theo ID cũ). */
    private void write(HttpServletRequest request, HttpServletResponse response,
                       String existingId, boolean creating) throws IOException {
        if (AdminAccess.requireAdmin(request, response) == null) return;
        if (!AdminAccess.requireCsrf(request, response)) return;

        try {
            String body = request.getReader().lines().reduce("", (left, right) -> left + right);
            Robot input = readRobot(body, creating ? null : existingId);
            Robot saved = creating ? robots.create(input) : robots.update(existingId, input);

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
                    "VALIDATION_ERROR", "Dữ liệu robot không hợp lệ.");
        } catch (SQLException exception) {
            ApiResponses.error(response, HttpServletResponse.SC_CONFLICT,
                    "CONFLICT", "Không thể lưu robot.");
        }
    }

    /* Kiểm tra dữ liệu ngay tại biên HTTP: ID phải là slug, độ khó phải nằm
       trong danh sách cho phép, tên và mô tả không được rỗng. */
    private Robot readRobot(String body, String existingId) {
        String id = existingId == null ? Json.stringField(body, "id").trim() : existingId;
        if (!id.matches(ID_PATTERN)) throw new IllegalArgumentException("id");

        String name = Json.stringField(body, "name").trim();
        String level = Json.stringField(body, "level").trim();
        String summary = Json.stringField(body, "summary").trim();
        String image = Json.stringField(body, "image").trim();
        String buildTime = Json.stringField(body, "buildTime").trim();
        String mainSensor = Json.stringField(body, "mainSensor").trim();
        String skills = Json.stringField(body, "skills").trim();
        String wiring = Json.objectField(body, "wiring");

        if (!LEVELS.contains(level) || name.isEmpty() || summary.isEmpty()) {
            throw new IllegalArgumentException("fields");
        }
        return new Robot(id, name, level, summary, image, buildTime, mainSensor, skills, wiring);
    }

    private String pathId(HttpServletRequest request) {
        String path = request.getPathInfo();
        return path == null ? null : path.substring(1);
    }
}
