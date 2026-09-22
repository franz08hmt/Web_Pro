package vn.edu.webpro.robotlab.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Function;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import vn.edu.webpro.robotlab.dao.AssemblyStepDao;
import vn.edu.webpro.robotlab.dao.DatabaseConnectionFactory;
import vn.edu.webpro.robotlab.dao.RobotDao;
import vn.edu.webpro.robotlab.service.RobotService;
import vn.edu.webpro.robotlab.web.ApiResponses;

/**
 * API công khai cho danh mục robot.
 *
 * GET /api/robots                     danh sách có phân trang
 * GET /api/robots/{id}/components     linh kiện bắt buộc của một robot
 * GET /api/robots/{id}/steps          các bước lắp ráp của một robot
 */
@WebServlet("/api/robots/*")
public final class RobotServlet extends HttpServlet {
    private static final int HTTP_UNPROCESSABLE_ENTITY = 422;

    private final RobotService service = new RobotService(
            new RobotDao(new DatabaseConnectionFactory(System.getenv())),
            new AssemblyStepDao(new DatabaseConnectionFactory(System.getenv()))
    );

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            int page = integer(request, "page", 1);
            int limit = integer(request, "limit", 20);
            String path = request.getPathInfo();

            if (path == null || "/".equals(path)) {
                ApiResponses.json(response, HttpServletResponse.SC_OK,
                        page(service.list(page, limit), page, limit, service.count(), item -> item.toJson()));
                return;
            }

            // Đường dẫn còn lại có dạng {robotId}/{tài nguyên con}.
            String[] segments = path.substring(1).split("/");
            if (segments.length == 2 && "components".equals(segments[1])) {
                ApiResponses.json(response, HttpServletResponse.SC_OK,
                        data(service.parts(segments[0]), item -> item.toJson()));
                return;
            }
            if (segments.length == 2 && "steps".equals(segments[1])) {
                ApiResponses.json(response, HttpServletResponse.SC_OK,
                        page(service.steps(segments[0], page, limit), page, limit,
                                service.stepCount(segments[0]), item -> item.toJson()));
                return;
            }

            ApiResponses.error(response, HttpServletResponse.SC_NOT_FOUND,
                    "NOT_FOUND", "Không tìm thấy tài nguyên yêu cầu.");
        } catch (IllegalArgumentException exception) {
            if ("not-found".equals(exception.getMessage())) {
                ApiResponses.error(response, HttpServletResponse.SC_NOT_FOUND,
                        "NOT_FOUND", "Không tìm thấy nội dung.");
            } else {
                ApiResponses.error(response, HTTP_UNPROCESSABLE_ENTITY,
                        "VALIDATION_ERROR", "Phân trang không hợp lệ.");
            }
        } catch (IllegalStateException exception) {
            ApiResponses.error(response, HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "DEPENDENCY_NOT_READY", "Cơ sở dữ liệu chưa được cấu hình.");
        } catch (SQLException exception) {
            getServletContext().log("Robot query failed", exception);
            ApiResponses.error(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "INTERNAL_SERVER_ERROR", "Đã xảy ra lỗi máy chủ. Vui lòng thử lại sau.");
        }
    }

    private int integer(HttpServletRequest request, String key, int fallback) {
        String value = request.getParameter(key);
        if (value == null) return fallback;
        if (!value.matches("[1-9][0-9]*")) throw new IllegalArgumentException("pagination");

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("pagination");
        }
    }

    /** Envelope chỉ có mảng data, dùng cho danh sách không phân trang. */
    private <T> String data(List<T> items, Function<T, String> toJson) {
        return "{\"data\":[" + join(items, toJson) + "]}";
    }

    /** Envelope data + meta, giữ đúng quy ước trong docs/API_CONVENTIONS.md. */
    private <T> String page(List<T> items, int page, int limit, long total, Function<T, String> toJson) {
        return "{\"data\":[" + join(items, toJson) + "],\"meta\":{\"page\":" + page
                + ",\"limit\":" + limit + ",\"total\":" + total + "}}";
    }

    private <T> String join(List<T> items, Function<T, String> toJson) {
        return items.stream().map(toJson).reduce((left, right) -> left + "," + right).orElse("");
    }
}
