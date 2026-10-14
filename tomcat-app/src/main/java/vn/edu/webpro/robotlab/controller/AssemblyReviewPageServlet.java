package vn.edu.webpro.robotlab.controller;

import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import vn.edu.webpro.robotlab.business.AssemblySession;
import vn.edu.webpro.robotlab.business.AssemblyReview;
import vn.edu.webpro.robotlab.business.AssemblyDrawing;
import vn.edu.webpro.robotlab.business.AssemblyLayout;
import vn.edu.webpro.robotlab.business.User;
import vn.edu.webpro.robotlab.data.AssemblySessionDB;
import vn.edu.webpro.robotlab.data.AssemblyPlacementDB;
import vn.edu.webpro.robotlab.data.AssemblyEventDB;
import vn.edu.webpro.robotlab.data.AssemblyStepDB;
import vn.edu.webpro.robotlab.data.RobotDB;
import vn.edu.webpro.robotlab.data.QuizAttemptDB;
import vn.edu.webpro.robotlab.data.QuizQuestionDB;
import vn.edu.webpro.robotlab.util.SessionUtil;

/** Trang hoàn thiện chỉ đọc: owner từ session, dữ kiện bean qua setAttribute và forward JSP. */
@WebServlet("/assembly-review")
public class AssemblyReviewPageServlet extends HttpServlet {
    /** GET không tạo phiên, chấm lại hoặc thay đổi trạng thái. */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setHeader("Cache-Control", "no-store");
        try {
            User user = SessionUtil.getCurrentUser(request);
            if (user == null) {
                response.sendRedirect(request.getContextPath() + "/pages/tai-khoan.html");
                return;
            }
            long sessionId = parseId(request.getParameter("session"));
            AssemblySession session = sessionId > 0 ? AssemblySessionDB.selectSession(sessionId, user.getId()) : null;
            if (session == null) {
                response.setStatus(404);
                request.setAttribute("assemblyReviewError", "Không tìm thấy phiên lắp ráp bạn được phép xem.");
            } else {
                AssemblyReview review = new AssemblyReview();
                review.setSession(session);
                review.setRobot(RobotDB.selectRobot(session.getRobotId()));
                request.setAttribute("session", session);
                request.setAttribute("robot", review.getRobot());
                request.setAttribute("assemblyReview", review);
                if (session.isCompleted()) {
                    prepareCompleted(request, review, user.getId());
                }
            }
        } catch (SQLException e) {
            response.setStatus(503);
            request.setAttribute("assemblyReviewError", "Dữ liệu tạm chưa sẵn sàng. Vui lòng thử lại sau.");
        }
        String url = "/WEB-INF/views/assembly-review.jsp";
        getServletContext().getRequestDispatcher(url).forward(request, response);
    }

    private void prepareCompleted(HttpServletRequest request, AssemblyReview review, long userId)
            throws SQLException {
        AssemblySession session = review.getSession();
        AssemblyDrawing drawing = new AssemblyDrawing();
        if (session.isDetailed()) {
            AssemblyLayout layout = AssemblyPlacementDB.selectLayout(session.getId(), userId);
            drawing.build(layout);
            review.setCorrectCount(layout.check().getCorrectCount());
            review.setRequiredCount(layout.check().getRequiredCount());
        } else {
            int count = RobotDB.selectRobotComponents(session.getRobotId()).size();
            review.setCorrectCount(count);
            review.setRequiredCount(count);
        }
        review.setEvents(AssemblyEventDB.selectBySession(session.getId(), userId));
        review.setOldJournal(review.getEvents().isEmpty()
                && AssemblyEventDB.createdBeforeJournal(session.getId(), userId));
        review.summarizeEvents();
        review.setQuizAttempts(QuizAttemptDB.selectAttempts(userId, session.getRobotId(), Integer.MAX_VALUE, 0));
        review.summarizeQuiz();
        review.setHasQuizQuestions(QuizQuestionDB.countQuestions(session.getRobotId()) > 0);
        request.setAttribute("assemblyDrawing", drawing);
        request.setAttribute("reviewSteps", AssemblyStepDB.selectSteps(session.getRobotId(), Integer.MAX_VALUE, 0));
    }

    private long parseId(String value) {
        try {
            return value != null && value.matches("[1-9][0-9]{0,18}") ? Long.parseLong(value) : -1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
