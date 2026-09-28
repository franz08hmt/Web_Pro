package vn.edu.webpro.robotlab.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import vn.edu.webpro.robotlab.business.QuizRobotScore;
import vn.edu.webpro.robotlab.business.Robot;
import vn.edu.webpro.robotlab.business.User;
import vn.edu.webpro.robotlab.data.StatsDB;
import vn.edu.webpro.robotlab.util.SessionUtil;

/**
 * Tổng kết học tập cá nhân (Chức năng 5): đọc số phiên theo trạng thái, mẫu
 * robot đã hoàn tất và điểm kiểm tra tốt nhất/gần nhất của người đang đăng
 * nhập, rồi setAttribute + forward sang JSP — cùng khuôn với
 * AssemblyReceiptPageServlet. Mọi số liệu đọc trực tiếp từ MySQL của chính
 * tài khoản này; không có đường nào xem được số liệu của tài khoản khác.
 */
@WebServlet("/learning-summary")
public class LearningSummaryServlet extends HttpServlet {

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

        try {
            Map<String, Long> sessionCounts = StatsDB.countSessionsByStatusForUser(user.getId());
            long totalSessions = sessionCounts.values().stream().mapToLong(Long::longValue).sum();
            List<Robot> completedRobots = StatsDB.selectCompletedRobots(user.getId());
            List<QuizRobotScore> quizScores = StatsDB.selectQuizScoresByRobot(user.getId());

            request.setAttribute("user", user);
            request.setAttribute("sessionCounts", sessionCounts);
            request.setAttribute("totalSessions", totalSessions);
            request.setAttribute("completedRobots", completedRobots);
            request.setAttribute("quizScores", quizScores);
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            return;
        }

        getServletContext()
                .getRequestDispatcher("/WEB-INF/views/learning-summary.jsp")
                .forward(request, response);
    }
}
