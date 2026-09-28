package vn.edu.webpro.robotlab.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import vn.edu.webpro.robotlab.business.ShopOrder;
import vn.edu.webpro.robotlab.business.User;
import vn.edu.webpro.robotlab.data.OrderDB;
import vn.edu.webpro.robotlab.util.ValidationUtil;

/** Trang HTML lịch sử đơn do Servlet lấy dữ liệu rồi forward sang JSP/JSTL. */
@WebServlet("/order-history")
public class OrderHistoryServlet extends HttpServlet {
    private static final int PAGE_SIZE = 10;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user;
        try {
            user = vn.edu.webpro.robotlab.util.SessionUtil.getCurrentUser(request);
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            return;
        }
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/pages/tai-khoan.html");
            return;
        }

        final int page;
        try {
            page = ValidationUtil.parsePositiveInt(
                    request.getParameter("page"), 1, ValidationUtil.MAX_PAGE);
        } catch (IllegalArgumentException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            List<ShopOrder> orders = OrderDB.selectOrders(user.getId(), PAGE_SIZE, (page - 1) * PAGE_SIZE);
            long totalOrders = OrderDB.countOrders(user.getId());
            long totalPages = Math.max(1, (totalOrders + PAGE_SIZE - 1) / PAGE_SIZE);
            request.setAttribute("user", user);
            request.setAttribute("orders", orders);
            request.setAttribute("page", page);
            request.setAttribute("totalPages", totalPages);
            getServletContext().getRequestDispatcher("/WEB-INF/views/order-history.jsp")
                    .forward(request, response);
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        }
    }
}
