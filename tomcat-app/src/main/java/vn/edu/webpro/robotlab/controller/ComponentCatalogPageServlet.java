package vn.edu.webpro.robotlab.controller;

import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import vn.edu.webpro.robotlab.dao.ComponentDao;
import vn.edu.webpro.robotlab.dao.DatabaseConnectionFactory;
import vn.edu.webpro.robotlab.model.ComponentPage;
import vn.edu.webpro.robotlab.service.ComponentService;

/**
 * Server-rendered component catalogue.
 *
 * Cùng kiểu luồng với RobotCatalogPageServlet: Servlet → Service → DAO → MySQL,
 * rồi setAttribute và forward sang JSP. Trang này nhận thêm tham số phân trang
 * qua query string để minh họa cách đọc dữ liệu từ HttpServletRequest.
 */
@WebServlet("/components")
public final class ComponentCatalogPageServlet extends HttpServlet {
    private static final int PAGE_SIZE = 20;

    private final ComponentService componentService = new ComponentService(
            new ComponentDao(new DatabaseConnectionFactory(System.getenv()))
    );

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        int page = pageNumber(request);

        try {
            ComponentPage result = componentService.list(page, PAGE_SIZE);

            request.setAttribute("components", result.data());
            request.setAttribute("total", result.total());
            request.setAttribute("page", result.page());
            request.setAttribute("hasNextPage", (long) result.page() * PAGE_SIZE < result.total());
        } catch (IllegalArgumentException exception) {
            request.setAttribute("errorMessage", "Số trang không hợp lệ.");
        } catch (IllegalStateException exception) {
            request.setAttribute("errorMessage",
                    "Chưa cấu hình MySQL nên không đọc được danh mục linh kiện.");
        } catch (SQLException exception) {
            getServletContext().log("Component catalogue page query failed", exception);
            request.setAttribute("errorMessage",
                    "Không truy vấn được cơ sở dữ liệu. Vui lòng thử lại sau.");
        }

        RequestDispatcher view = request.getRequestDispatcher("/WEB-INF/views/components.jsp");
        view.forward(request, response);
    }

    private int pageNumber(HttpServletRequest request) {
        String value = request.getParameter("page");
        if (value == null || !value.matches("[1-9][0-9]{0,4}")) return 1;
        return Integer.parseInt(value);
    }
}
