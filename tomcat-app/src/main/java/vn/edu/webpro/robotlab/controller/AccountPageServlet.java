package vn.edu.webpro.robotlab.controller;

import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import vn.edu.webpro.robotlab.business.User;
import vn.edu.webpro.robotlab.util.SessionUtil;

/**
 * Trang tài khoản dựng ở server: đọc User từ HttpSession, đặt vào request bằng
 * setAttribute rồi forward sang JSP — cùng cách EmailListServlet chuyển sang
 * thanks.jsp ở Chapter 12 slide 44.
 */
@WebServlet("/account")
public class AccountPageServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user;
        try {
            user = SessionUtil.getCurrentUser(request);
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            return;
        }

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/pages/tai-khoan.html");
            return;
        }

        request.setAttribute("user", user);
        request.setAttribute("isAdmin", user.isAdmin());

        String url = "/WEB-INF/views/account.jsp";
        getServletContext()
                .getRequestDispatcher(url)
                .forward(request, response);
    }
}
