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
import vn.edu.webpro.robotlab.business.QuestionMissRate;
import vn.edu.webpro.robotlab.business.QuizRobotAggregate;
import vn.edu.webpro.robotlab.business.RobotPopularity;
import vn.edu.webpro.robotlab.business.User;
import vn.edu.webpro.robotlab.data.StatsDB;
import vn.edu.webpro.robotlab.util.SessionUtil;

/**
 * Thống kê quản trị (Chức năng 5): số phiên theo trạng thái, số người học và
 * số người có hoạt động, mẫu robot được chọn nhiều, kết quả kiểm tra tổng hợp
 * và câu hỏi có tỷ lệ sai cao — toàn bộ đọc trực tiếp từ MySQL bằng
 * GROUP BY/COUNT DISTINCT (không cộng dồn ở Java) để tránh đếm trùng.
 *
 * Chỉ ADMIN xem được; người dùng thường bị trả 403, khác AccountPageServlet
 * (trang đó ai đăng nhập cũng xem được, chỉ ẩn/hiện một liên kết).
 */
@WebServlet("/admin-stats")
public class AdminStatsServlet extends HttpServlet {
    private static final int MIN_QUESTION_ATTEMPTS = 3;
    private static final int TOP_ROBOTS_LIMIT = 10;
    private static final int TOP_QUESTIONS_LIMIT = 10;

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
        if (!user.isAdmin()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        try {
            Map<String, Long> sessionCounts = StatsDB.countAllSessionsByStatus();
            long totalSessions = sessionCounts.values().stream().mapToLong(Long::longValue).sum();
            long totalLearners = StatsDB.countLearners();
            long activeLearners = StatsDB.countActiveLearners();
            List<RobotPopularity> popularRobots = StatsDB.selectPopularRobots(TOP_ROBOTS_LIMIT);
            List<QuizRobotAggregate> quizAggregates = StatsDB.selectQuizAggregatesByRobot();
            List<QuestionMissRate> highMissQuestions =
                    StatsDB.selectHighMissRateQuestions(MIN_QUESTION_ATTEMPTS, TOP_QUESTIONS_LIMIT);

            request.setAttribute("sessionCounts", sessionCounts);
            request.setAttribute("totalSessions", totalSessions);
            request.setAttribute("totalLearners", totalLearners);
            request.setAttribute("activeLearners", activeLearners);
            request.setAttribute("popularRobots", popularRobots);
            request.setAttribute("quizAggregates", quizAggregates);
            request.setAttribute("highMissQuestions", highMissQuestions);
            request.setAttribute("minQuestionAttempts", MIN_QUESTION_ATTEMPTS);
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            return;
        }

        getServletContext()
                .getRequestDispatcher("/WEB-INF/views/admin-stats.jsp")
                .forward(request, response);
    }
}
