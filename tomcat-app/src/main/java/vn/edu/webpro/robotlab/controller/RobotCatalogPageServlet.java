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

/**
 * Danh mục robot dựng ở server — luồng Model 2 đầy đủ của Chapter 2 và 12:
 * doGet đọc tham số → RobotDB truy vấn MySQL → setAttribute → forward sang JSP.
 * Khác /api/robots trả JSON, trang này trả HTML đã dựng sẵn.
 */
@WebServlet("/robots")
public class RobotCatalogPageServlet extends HttpServlet {
    private static final int PAGE_SIZE = 20;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // get parameters from the request
        int page = pageNumber(request.getParameter("page"));

        try {
            List<Robot> robots = RobotDB.selectRobots(PAGE_SIZE, (page - 1) * PAGE_SIZE);
            long total = RobotDB.countRobots();

            // store data in request attributes for the JSP
            request.setAttribute("robots", robots);
            request.setAttribute("total", total);
            request.setAttribute("page", page);
            request.setAttribute("hasNextPage", (long) page * PAGE_SIZE < total);
        } catch (SQLException e) {
            request.setAttribute("errorMessage",
                    "Không truy vấn được cơ sở dữ liệu. Vui lòng thử lại sau.");
        }

        String url = "/WEB-INF/views/robots.jsp";
        getServletContext()
                .getRequestDispatcher(url)
                .forward(request, response);
    }

    private int pageNumber(String value) {
        if (value == null || !value.matches("[1-9][0-9]{0,4}")) return 1;
        return Integer.parseInt(value);
    }
}
