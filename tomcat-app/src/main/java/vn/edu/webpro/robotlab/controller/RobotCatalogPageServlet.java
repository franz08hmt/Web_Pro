package vn.edu.webpro.robotlab.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import vn.edu.webpro.robotlab.dao.AssemblyStepDao;
import vn.edu.webpro.robotlab.dao.DatabaseConnectionFactory;
import vn.edu.webpro.robotlab.dao.RobotDao;
import vn.edu.webpro.robotlab.model.Robot;
import vn.edu.webpro.robotlab.service.RobotService;

/**
 * Server-rendered robot catalogue.
 *
 * Luồng truyền thống của học phần, khác với /api/robots trả JSON:
 * doGet đọc tham số từ HttpServletRequest, gọi Service rồi DAO để lấy dữ liệu
 * MySQL, đặt kết quả vào request bằng setAttribute và forward sang JSP. Trình
 * duyệt nhận HTML đã dựng sẵn ở server, không cần JavaScript gọi API.
 */
@WebServlet("/robots")
public final class RobotCatalogPageServlet extends HttpServlet {
    private static final int PAGE_SIZE = 20;

    private final RobotService robotService = new RobotService(
            new RobotDao(new DatabaseConnectionFactory(System.getenv())),
            new AssemblyStepDao(new DatabaseConnectionFactory(System.getenv()))
    );

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        int page = pageNumber(request);

        try {
            List<Robot> robots = robotService.list(page, PAGE_SIZE);
            long total = robotService.count();

            // Dữ liệu đi từ controller sang view bằng request attribute.
            request.setAttribute("robots", robots);
            request.setAttribute("total", total);
            request.setAttribute("page", page);
            request.setAttribute("hasNextPage", (long) page * PAGE_SIZE < total);
        } catch (IllegalArgumentException exception) {
            request.setAttribute("errorMessage", "Số trang không hợp lệ.");
        } catch (IllegalStateException exception) {
            request.setAttribute("errorMessage",
                    "Chưa cấu hình MySQL nên không đọc được danh mục robot.");
        } catch (SQLException exception) {
            getServletContext().log("Robot catalogue page query failed", exception);
            request.setAttribute("errorMessage",
                    "Không truy vấn được cơ sở dữ liệu. Vui lòng thử lại sau.");
        }

        RequestDispatcher view = request.getRequestDispatcher("/WEB-INF/views/robots.jsp");
        view.forward(request, response);
    }

    private int pageNumber(HttpServletRequest request) {
        String value = request.getParameter("page");
        if (value == null || !value.matches("[1-9][0-9]{0,4}")) return 1;
        return Integer.parseInt(value);
    }
}
