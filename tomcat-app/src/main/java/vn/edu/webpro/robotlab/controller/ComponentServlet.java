package vn.edu.webpro.robotlab.controller;

import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import vn.edu.webpro.robotlab.dao.ComponentDao;
import vn.edu.webpro.robotlab.dao.DatabaseConnectionFactory;
import vn.edu.webpro.robotlab.model.ComponentPage;
import vn.edu.webpro.robotlab.service.ComponentService;
import vn.edu.webpro.robotlab.web.ApiResponses;

/** HTTP controller for the public component catalogue. */
@WebServlet("/api/components")
public final class ComponentServlet extends HttpServlet {
    private static final int HTTP_UNPROCESSABLE_ENTITY = 422;

    private final ComponentService componentService = new ComponentService(
            new ComponentDao(new DatabaseConnectionFactory(System.getenv()))
    );

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            int page = queryInteger(request, "page", 1);
            int limit = queryInteger(request, "limit", 20);
            ComponentPage components = componentService.list(page, limit);
            ApiResponses.json(response, HttpServletResponse.SC_OK, components.toJson());
        } catch (IllegalArgumentException exception) {
            ApiResponses.error(response, HTTP_UNPROCESSABLE_ENTITY,
                    "VALIDATION_ERROR", "Phân trang không hợp lệ.");
        } catch (IllegalStateException exception) {
            ApiResponses.error(response, HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "DEPENDENCY_NOT_READY", "Cơ sở dữ liệu chưa được cấu hình.");
        } catch (SQLException exception) {
            getServletContext().log("Component query failed", exception);
            ApiResponses.error(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "INTERNAL_SERVER_ERROR", "Đã xảy ra lỗi máy chủ. Vui lòng thử lại sau.");
        }
    }

    private int queryInteger(HttpServletRequest request, String name, int fallback) {
        String value = request.getParameter(name);
        if (value == null) return fallback;
        if (!value.matches("[1-9][0-9]*")) throw new IllegalArgumentException();
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException();
        }
    }
}
