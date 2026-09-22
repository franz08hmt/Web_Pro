package vn.edu.webpro.robotlab.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import vn.edu.webpro.robotlab.dao.DatabaseConnectionFactory;
import vn.edu.webpro.robotlab.dao.LibraryResourceDao;
import vn.edu.webpro.robotlab.model.LibraryResource;
import vn.edu.webpro.robotlab.service.LibraryResourceService;
import vn.edu.webpro.robotlab.web.ApiResponses;

/**
 * API công khai cho thư viện tài nguyên.
 *
 * GET /api/library-resources?page=&limit=&robotId=
 * Bỏ trống robotId thì trả toàn bộ tài nguyên, kể cả loại dùng chung.
 */
@WebServlet("/api/library-resources")
public final class LibraryResourceServlet extends HttpServlet {
    private static final int HTTP_UNPROCESSABLE_ENTITY = 422;
    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_LIMIT = 20;

    private final LibraryResourceService service = new LibraryResourceService(
            new LibraryResourceDao(new DatabaseConnectionFactory(System.getenv()))
    );

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            int page = integer(request, "page", DEFAULT_PAGE);
            int limit = integer(request, "limit", DEFAULT_LIMIT);
            String robotId = request.getParameter("robotId");

            List<LibraryResource> data = service.list(page, limit, robotId);
            String json = data.stream()
                    .map(LibraryResource::toJson)
                    .reduce((left, right) -> left + "," + right)
                    .orElse("");

            ApiResponses.json(response, HttpServletResponse.SC_OK,
                    "{\"data\":[" + json + "],\"meta\":{\"page\":" + page
                            + ",\"limit\":" + limit + ",\"total\":" + service.count(robotId) + "}}");
        } catch (IllegalArgumentException exception) {
            ApiResponses.error(response, HTTP_UNPROCESSABLE_ENTITY,
                    "VALIDATION_ERROR", "Phân trang không hợp lệ.");
        } catch (SQLException exception) {
            ApiResponses.error(response, HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "DEPENDENCY_NOT_READY", "Cơ sở dữ liệu chưa được cấu hình.");
        }
    }

    private int integer(HttpServletRequest request, String key, int fallback) {
        String value = request.getParameter(key);
        if (value == null) return fallback;

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("pagination");
        }
    }
}
