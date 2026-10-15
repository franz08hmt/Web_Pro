package vn.edu.webpro.robotlab.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import vn.edu.webpro.robotlab.business.*;
import vn.edu.webpro.robotlab.data.*;
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

        response.setHeader("Cache-Control", "no-store");
        try {
            DashboardSummary dashboard = new DashboardSummary();
            AssemblySession open = AssemblySessionDB.selectLatestSession(user.getId(), false);
            AssemblySession complete = AssemblySessionDB.selectLatestSession(user.getId(), true);
            Robot activeRobot = open == null ? null : RobotDB.selectRobot(open.getRobotId());
            Robot finishedRobot = complete == null ? null : RobotDB.selectRobot(complete.getRobotId());
            dashboard.addAssemblyCards(open, activeRobot, complete, finishedRobot);
            dashboard.addQuizCard(QuizAttemptDB.selectAttempts(user.getId(), null, 1, 0),
                    StatsDB.selectDashboardQuizCandidates(user.getId()));
            Date now = new Date();
            dashboard.addTaskCard(user, PracticeTaskDB.selectDashboardCounts(user.getId(), now,
                    dashboard.upcomingDeadline(now)));
            dashboard.addDesignCard(DesignDB.selectLatestDesign(user.getId()));
            dashboard.addKitCard(UserKitDB.selectItems(user.getId(), DesignRuleDB.selectPublished()));
            dashboard.addOrderCard(OrderDB.selectOrders(user.getId(), 1, 0));
            request.setAttribute("dashboard", dashboard);
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            return;
        }

        String url = "/WEB-INF/views/dashboard.jsp";
        getServletContext()
                .getRequestDispatcher(url)
                .forward(request, response);
    }
}
