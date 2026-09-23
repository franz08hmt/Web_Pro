package vn.edu.webpro.robotlab.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import vn.edu.webpro.robotlab.business.LibraryResource;
import vn.edu.webpro.robotlab.data.LibraryResourceDB;
import vn.edu.webpro.robotlab.util.ResponseUtil;
import vn.edu.webpro.robotlab.util.ValidationUtil;

/**
 * API công khai cho thư viện: GET /api/library-resources?page=&limit=&robotId=
 * Bỏ trống robotId thì trả toàn bộ tài nguyên.
 */
@WebServlet("/api/library-resources")
public class LibraryResourceServlet extends HttpServlet {
    private static final int HTTP_UNPROCESSABLE_ENTITY = 422;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            int page = ValidationUtil.parsePositiveInt(request.getParameter("page"), 1, ValidationUtil.MAX_PAGE);
            int limit = ValidationUtil.parsePositiveInt(request.getParameter("limit"), 20, ValidationUtil.MAX_LIMIT);
            String robotId = request.getParameter("robotId");
            if (robotId != null && robotId.isEmpty()) {
                robotId = null;
            }

            List<LibraryResource> resources = LibraryResourceDB.selectResources(limit, (page - 1) * limit, robotId);
            StringBuilder items = new StringBuilder();
            for (LibraryResource resource : resources) {
                if (items.length() > 0) items.append(',');
                items.append(resource.toJson());
            }
            ResponseUtil.sendJson(response, HttpServletResponse.SC_OK,
                    "{\"data\":[" + items + "],\"meta\":{\"page\":" + page + ",\"limit\":" + limit
                            + ",\"total\":" + LibraryResourceDB.countResources(robotId) + "}}");
        } catch (IllegalArgumentException e) {
            ResponseUtil.sendError(response, HTTP_UNPROCESSABLE_ENTITY,
                    "VALIDATION_ERROR", "Phân trang không hợp lệ.");
        } catch (SQLException e) {
            ResponseUtil.sendDatabaseUnavailable(response);
        }
    }
}
