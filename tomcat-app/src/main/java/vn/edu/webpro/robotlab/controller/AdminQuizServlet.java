package vn.edu.webpro.robotlab.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import vn.edu.webpro.robotlab.business.QuizOption;
import vn.edu.webpro.robotlab.business.QuizQuestion;
import vn.edu.webpro.robotlab.data.QuizQuestionDB;
import vn.edu.webpro.robotlab.data.RobotDB;
import vn.edu.webpro.robotlab.util.JsonUtil;
import vn.edu.webpro.robotlab.util.ResponseUtil;
import vn.edu.webpro.robotlab.util.SessionUtil;
import vn.edu.webpro.robotlab.util.ValidationUtil;

/**
 * CRUD quản trị câu hỏi kiểm tra kiến thức: GET (xem theo robot hoặc theo id),
 * POST (thêm), PATCH (sửa), DELETE (xóa). Mọi thao tác cần tài khoản ADMIN;
 * thao tác ghi còn cần CSRF token — cùng khuôn với AdminRobotServlet.
 *
 * Một request thêm/sửa gửi kèm toàn bộ lựa chọn trong trường "options" vì một
 * câu hỏi trắc nghiệm không có ý nghĩa nếu thiếu lựa chọn đi kèm.
 */
@WebServlet("/api/admin/quiz/questions/*")
public class AdminQuizServlet extends HttpServlet {
    private static final int HTTP_UNPROCESSABLE_ENTITY = 422;
    private static final int MAX_BODY = 65536;
    private static final int MIN_OPTIONS = 2;
    private static final int MAX_OPTIONS = 6;

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if ("PATCH".equals(request.getMethod())) {
            save(request, response, false);
            return;
        }
        super.service(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (SessionUtil.requireAdmin(request, response) == null) return;

        try {
            String id = pathId(request);
            if (!id.isEmpty()) {
                QuizQuestion question = QuizQuestionDB.selectQuestion(id);
                if (question == null) {
                    sendNotFound(response);
                    return;
                }
                ResponseUtil.sendJson(response, HttpServletResponse.SC_OK, "{\"data\":" + question.toJson() + "}");
                return;
            }

            String robotId = request.getParameter("robotId");
            if (!ValidationUtil.isSlug(robotId)) {
                sendInvalid(response);
                return;
            }
            List<QuizQuestion> questions = QuizQuestionDB.selectQuestionsByRobot(robotId);
            StringBuilder items = new StringBuilder();
            for (QuizQuestion question : questions) {
                if (items.length() > 0) items.append(',');
                items.append(question.toJson());
            }
            ResponseUtil.sendJson(response, HttpServletResponse.SC_OK,
                    "{\"data\":[" + items + "],\"meta\":{\"total\":" + QuizQuestionDB.countQuestions(robotId) + "}}");
        } catch (SQLException e) {
            ResponseUtil.sendDatabaseUnavailable(response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        save(request, response, true);
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (SessionUtil.requireAdmin(request, response) == null) return;
        if (!SessionUtil.hasValidCsrfToken(request, response)) return;

        try {
            if (QuizQuestionDB.deleteQuestion(pathId(request)) == 0) {
                sendNotFound(response);
                return;
            }
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (SQLException e) {
            // Khóa ngoại RESTRICT chặn xóa câu hỏi đã có người làm bài (quiz_attempt_answers).
            ResponseUtil.sendError(response, HttpServletResponse.SC_CONFLICT,
                    "RELATION_CONFLICT", "Không thể xóa câu hỏi đã có người làm bài.");
        }
    }

    private void save(HttpServletRequest request, HttpServletResponse response, boolean creating)
            throws IOException {
        if (SessionUtil.requireAdmin(request, response) == null) return;
        if (!SessionUtil.hasValidCsrfToken(request, response)) return;

        try {
            String body = JsonUtil.readBody(request.getReader(), MAX_BODY);
            QuizQuestion question = new QuizQuestion();
            question.setId(creating ? JsonUtil.stringField(body, "id").trim() : pathId(request));
            question.setRobotId(JsonUtil.stringField(body, "robotId").trim());
            question.setPrompt(JsonUtil.stringField(body, "prompt").trim());
            question.setExplanation(JsonUtil.stringField(body, "explanation").trim());
            question.setQuestionOrder(Integer.parseInt(JsonUtil.stringField(body, "questionOrder")));

            List<String> rawOptions = JsonUtil.objectArrayField(body, "options");
            List<QuizOption> options = new ArrayList<>();
            int correctCount = 0;
            for (int index = 0; index < rawOptions.size(); index++) {
                String rawOption = rawOptions.get(index);
                QuizOption option = new QuizOption();
                option.setId(JsonUtil.stringField(rawOption, "id").trim());
                option.setQuestionId(question.getId());
                option.setLabel(JsonUtil.stringField(rawOption, "label").trim());
                option.setCorrect(JsonUtil.booleanField(rawOption, "isCorrect"));
                option.setOptionOrder(index + 1);
                if (option.isCorrect()) correctCount++;
                options.add(option);
            }
            question.setOptions(options);

            if (!ValidationUtil.isSlug(question.getId()) || !RobotDB.robotExists(question.getRobotId())
                    || question.getPrompt().isEmpty() || question.getExplanation().isEmpty()
                    || options.size() < MIN_OPTIONS || options.size() > MAX_OPTIONS || correctCount != 1
                    || options.stream().anyMatch(o -> o.getId().isEmpty() || o.getLabel().isEmpty())) {
                sendInvalid(response);
                return;
            }

            boolean saved = QuizQuestionDB.saveQuestion(question, creating);
            if (!saved) {
                sendNotFound(response);
                return;
            }
            ResponseUtil.sendJson(response,
                    creating ? HttpServletResponse.SC_CREATED : HttpServletResponse.SC_OK,
                    "{\"data\":" + question.toJson() + "}");
        } catch (IllegalArgumentException e) {
            sendInvalid(response);
        } catch (SQLException e) {
            ResponseUtil.sendError(response, HttpServletResponse.SC_CONFLICT,
                    "CONFLICT", "Không thể lưu câu hỏi.");
        }
    }

    private String pathId(HttpServletRequest request) {
        String path = request.getPathInfo();
        return path == null ? "" : path.substring(1);
    }

    private void sendInvalid(HttpServletResponse response) throws IOException {
        ResponseUtil.sendError(response, HTTP_UNPROCESSABLE_ENTITY,
                "VALIDATION_ERROR", "Dữ liệu câu hỏi không hợp lệ: cần 2–6 lựa chọn và đúng một đáp án đúng.");
    }

    private void sendNotFound(HttpServletResponse response) throws IOException {
        ResponseUtil.sendError(response, HttpServletResponse.SC_NOT_FOUND,
                "NOT_FOUND", "Không tìm thấy nội dung.");
    }
}
