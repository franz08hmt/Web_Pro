package vn.edu.webpro.robotlab.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import vn.edu.webpro.robotlab.business.Component;
import vn.edu.webpro.robotlab.data.ComponentDB;

/**
 * Danh mục linh kiện dựng ở server: doGet đọc ?page= → ComponentDB truy vấn
 * MySQL → setAttribute → forward sang JSP dùng JSTL &lt;c:forEach&gt;.
 */
@WebServlet("/components")
public class ComponentCatalogPageServlet extends HttpServlet {
    private static final int PAGE_SIZE = 20;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // get parameters from the request
        int page = pageNumber(request.getParameter("page"));

        try {
            List<Component> components = ComponentDB.selectComponents(PAGE_SIZE, (page - 1) * PAGE_SIZE);
            long total = ComponentDB.countComponents();

            // store data in request attributes for the JSP
            request.setAttribute("components", components);
            request.setAttribute("total", total);
            request.setAttribute("page", page);
            request.setAttribute("hasNextPage", (long) page * PAGE_SIZE < total);
        } catch (SQLException e) {
            request.setAttribute("errorMessage",
                    "Không truy vấn được cơ sở dữ liệu. Vui lòng thử lại sau.");
        }

        String url = "/WEB-INF/views/components.jsp";
        getServletContext()
                .getRequestDispatcher(url)
                .forward(request, response);
    }

    private int pageNumber(String value) {
        if (value == null || !value.matches("[1-9][0-9]{0,4}")) return 1;
        return Integer.parseInt(value);
    }
}
