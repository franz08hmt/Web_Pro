package vn.edu.webpro.robotlab.controller;

import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.RequestDispatcher;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import vn.edu.webpro.robotlab.model.User;
import vn.edu.webpro.robotlab.service.AccountSession;

/** Server-rendered account view: controller sets request data, JSP renders it. */
@WebServlet("/account")
public final class AccountPageServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, javax.servlet.ServletException {
        User user;
        try {
            user = AccountSession.load(request);
        } catch (SQLException exception) {
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            return;
        }
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/pages/tai-khoan.html");
            return;
        }

        request.setAttribute("user", user);
        request.setAttribute("isAdmin", "ADMIN".equals(user.role()));
        RequestDispatcher view = request.getRequestDispatcher("/WEB-INF/views/account.jsp");
        view.forward(request, response);
    }
}
