package vn.edu.webpro.robotlab.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import vn.edu.webpro.robotlab.business.LearningProfile;
import vn.edu.webpro.robotlab.business.User;
import vn.edu.webpro.robotlab.data.RobotDB;
import vn.edu.webpro.robotlab.data.StatsDB;
import vn.edu.webpro.robotlab.util.SessionUtil;

/** Trang hồ sơ học tập chỉ đọc: lấy danh tính từ session, dựng JavaBean rồi forward sang JSP. */
@WebServlet("/learning-profile")
public class LearningProfileServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setHeader("Cache-Control", "no-store");

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

        LearningProfile profile = new LearningProfile();
        profile.setFullName(user.getFullName());
        profile.setGeneratedAt(new Date());
        try {
            profile.setCatalogRobots(RobotDB.selectAllRobots());
            profile.setSessionStats(StatsDB.selectLearningProfileSessionStats(user.getId()));
            profile.setQuizAttempts(StatsDB.selectLearningProfileQuizAttempts(user.getId()));
            profile.setCompletedComponentNames(StatsDB.selectCompletedRobotComponentNames(user.getId()));
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            return;
        }

        profile.buildProfile();
        request.setAttribute("profile", profile);
        String url = "/WEB-INF/views/learning-profile.jsp";
        getServletContext().getRequestDispatcher(url).forward(request, response);
    }
}
