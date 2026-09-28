package vn.edu.webpro.robotlab.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import vn.edu.webpro.robotlab.business.QuizAttempt;
import vn.edu.webpro.robotlab.business.QuizQuestion;
import vn.edu.webpro.robotlab.business.User;
import vn.edu.webpro.robotlab.data.QuizAttemptDB;
import vn.edu.webpro.robotlab.data.QuizQuestionDB;
import vn.edu.webpro.robotlab.data.RobotDB;
import vn.edu.webpro.robotlab.util.JsonUtil;
import vn.edu.webpro.robotlab.util.ResponseUtil;
import vn.edu.webpro.robotlab.util.SessionUtil;
import vn.edu.webpro.robotlab.util.ValidationUtil;

/**
 * Bài kiểm tra kiến thức theo robot của người đang đăng nhập.
 *
 * GET  /api/quiz/questions?robotId=X    đề bài (không có đáp án đúng)
 * POST /api/quiz/attempts               nộp bài — server tự chấm từ database
 * GET  /api/quiz/attempts               lịch sử làm bài của chính mình
 * GET  /api/quiz/attempts/{id}          xem lại một lượt đã làm
 *
 * Client chỉ gửi robotId và danh sách {questionId, optionId}; điểm số và
 * đúng/sai luôn do QuizAttemptDB.submitAttempt() tính từ đáp án thật trong
 * database, không tin bất kỳ điểm nào client tự gửi lên.
 */
@WebServlet("/api/quiz/*")
public class QuizServlet extends HttpServlet {
    private static final int HTTP_UNPROCESSABLE_ENTITY = 422;
    private static final int MAX_BODY = 16384;
    private static final int PAGE_SIZE = 10;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User user = SessionUtil.requireUser(request, response);
        if (user == null) return;

        String path = request.getPathInfo();
        try {
            if ("/questions".equals(path)) {
                sendQuestions(request, response);
            } else if (path == null || "/attempts".equals(path) || "/attempts/".equals(path)) {
                sendAttemptList(request, response, user);
            } else if (path.startsWith("/attempts/")) {
                sendAttemptDetail(response, user, path.substring("/attempts/".length()));
            } else {
                sendNotFound(response);
            }
        } catch (SQLException e) {
            ResponseUtil.sendDatabaseUnavailable(response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User user = SessionUtil.requireUser(request, response);
        if (user == null || !SessionUtil.hasValidCsrfToken(request, response)) return;

        if (!"/attempts".equals(request.getPathInfo())) {
            sendNotFound(response);
            return;
        }

        try {
            String body = JsonUtil.readBody(request.getReader(), MAX_BODY);
            String robotId = JsonUtil.stringField(body, "robotId");
            if (!ValidationUtil.isSlug(robotId) || !RobotDB.robotExists(robotId)) {
                sendInvalid(response);
                return;
            }

            List<String[]> answers = new ArrayList<>();
            for (String rawAnswer : JsonUtil.objectArrayField(body, "answers")) {
                answers.add(new String[] {
                        JsonUtil.stringField(rawAnswer, "questionId"),
                        JsonUtil.stringField(rawAnswer, "optionId")
                });
            }

            QuizAttempt attempt = QuizAttemptDB.submitAttempt(user.getId(), robotId, answers);
            ResponseUtil.sendJson(response, HttpServletResponse.SC_CREATED,
                    "{\"data\":" + attempt.toJson() + "}");
        } catch (IllegalArgumentException e) {
            sendInvalid(response);
        } catch (SQLException e) {
            ResponseUtil.sendDatabaseUnavailable(response);
        }
    }

    private void sendQuestions(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {
        String robotId = request.getParameter("robotId");
        if (!ValidationUtil.isSlug(robotId) || !RobotDB.robotExists(robotId)) {
            sendInvalid(response);
            return;
        }

        List<QuizQuestion> questions = QuizQuestionDB.selectQuestionsByRobot(robotId);
        StringBuilder items = new StringBuilder();
        for (QuizQuestion question : questions) {
            if (items.length() > 0) items.append(',');
            items.append(question.toPublicJson());
        }
        ResponseUtil.sendJson(response, HttpServletResponse.SC_OK,
                "{\"data\":{\"robotId\":" + JsonUtil.quote(robotId)
                        + ",\"questions\":[" + items + "]}}");
    }

    private void sendAttemptList(HttpServletRequest request, HttpServletResponse response, User user)
            throws SQLException, IOException {
        String robotId = request.getParameter("robotId");
        if (robotId != null && !ValidationUtil.isSlug(robotId)) {
            sendInvalid(response);
            return;
        }
        int page = ValidationUtil.parsePositiveInt(request.getParameter("page"), 1, ValidationUtil.MAX_PAGE);

        List<QuizAttempt> attempts = QuizAttemptDB.selectAttempts(user.getId(), robotId, PAGE_SIZE,
                (page - 1) * PAGE_SIZE);
        long total = QuizAttemptDB.countAttempts(user.getId(), robotId);

        StringBuilder items = new StringBuilder();
        for (QuizAttempt attempt : attempts) {
            if (items.length() > 0) items.append(',');
            items.append(attempt.toSummaryJson());
        }
        ResponseUtil.sendJson(response, HttpServletResponse.SC_OK,
                "{\"data\":[" + items + "],\"meta\":{\"page\":" + page + ",\"pageSize\":" + PAGE_SIZE
                        + ",\"total\":" + total + "}}");
    }

    private void sendAttemptDetail(HttpServletResponse response, User user, String rawId)
            throws SQLException, IOException {
        if (!rawId.matches("[1-9][0-9]{0,18}")) {
            sendNotFound(response);
            return;
        }
        QuizAttempt attempt = QuizAttemptDB.selectAttempt(Long.parseLong(rawId), user.getId());
        if (attempt == null) {
            sendNotFound(response);
            return;
        }
        ResponseUtil.sendJson(response, HttpServletResponse.SC_OK, "{\"data\":" + attempt.toJson() + "}");
    }

    private void sendInvalid(HttpServletResponse response) throws IOException {
        ResponseUtil.sendError(response, HTTP_UNPROCESSABLE_ENTITY,
                "VALIDATION_ERROR", "Dữ liệu bài kiểm tra không hợp lệ.");
    }

    private void sendNotFound(HttpServletResponse response) throws IOException {
        ResponseUtil.sendError(response, HttpServletResponse.SC_NOT_FOUND,
                "NOT_FOUND", "Không tìm thấy tài nguyên yêu cầu.");
    }
}
