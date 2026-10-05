package vn.edu.webpro.robotlab.controller;

import vn.edu.webpro.robotlab.business.*;
import vn.edu.webpro.robotlab.data.*;
import vn.edu.webpro.robotlab.util.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/tasks")
/** Điều phối trang nhiệm vụ của USER theo danh tính trong session. */
public class TaskServlet extends HttpServlet {

    private User user(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {
        response.setHeader("Cache-Control", "no-store");
        User user = SessionUtil.getCurrentUser(request);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/pages/tai-khoan.html");
            return null;
        }
        if (user.isAdmin()) {
            response.sendError(403);
            return null;
        }
        return user;
    }

    private void forward(HttpServletRequest request, HttpServletResponse response, String view)
            throws ServletException, IOException {
        String url = "/WEB-INF/views/" + view + ".jsp";
        getServletContext().getRequestDispatcher(url).forward(request, response);
    }

    private void invalid(
            HttpServletRequest request,
            HttpServletResponse response,
            IllegalArgumentException error)
            throws ServletException, IOException {
        response.setStatus(422);
        request.setAttribute("taskError", error.getMessage());
        forward(request, response, "task-error");
    }

    private PracticeTask owned(HttpServletRequest request, HttpServletResponse response, User user)
            throws SQLException, IOException {
        PracticeTask task =
                PracticeTaskDB.selectTask(TaskFormUtil.number(request, "id"), user.getId(), false);
        if (task == null) {
            response.sendError(404);
        }
        return task;
    }

    private TaskRecipient detail(HttpServletRequest request, PracticeTask task, User user)
            throws SQLException {
        List<TaskRecipient> recipients =
                PracticeTaskDB.selectRecipients(task.getId(), user.getId(), false);
        TaskRecipient recipient = recipients.get(0);
        request.setAttribute("task", task);
        request.setAttribute("recipient", recipient);
        request.setAttribute(
                "submissions",
                TaskSubmissionDB.selectSubmissions(task.getId(), user.getId(), false));
        TaskSubmission quiz = TaskSubmissionDB.selectRoundQuiz(task.getId(), user.getId());
        request.setAttribute("taskQuiz", quiz);
        if (task.isRubricB()) {
            request.setAttribute("taskDiagnosis", TaskSubmissionDB.selectRoundDiagnosis(task.getId(), user.getId()));
        }
        if (quiz != null && recipient.getCurrentRound().isHasQuizAttempt()) {
            request.setAttribute(
                    "quizAttempt",
                    QuizAttemptDB.selectAttempt(quiz.getQuizAttemptId(), user.getId()));
        }
        return recipient;
    }

    /** Đọc trang trong phạm vi vai trò; dữ liệu sai trả task-error. */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            User user = user(request, response);
            if (user == null) {
                return;
            }
            String action = TaskFormUtil.text(request, "action");
            if (action.isEmpty()) {
                request.setAttribute(
                        "tasks",
                        PracticeTaskDB.selectTasks(
                                user.getId(), false, TaskFormUtil.text(request, "state")));
                forward(request, response, "task-list");
                return;
            }
            if (!"view".equals(action) && !"quiz".equals(action) && !"submit".equals(action)) {
                throw new IllegalArgumentException("Thao tác không hợp lệ.");
            }
            PracticeTask task = owned(request, response, user);
            if (task == null) {
                return;
            }
            TaskRecipient recipient = detail(request, task, user);
            if ("quiz".equals(action)) {
                if (recipient.isCanTakeQuiz()) {
                    request.setAttribute(
                            "quizQuestions",
                            QuizQuestionDB.selectQuestionsByRobot(task.getRobotId()));
                }
                forward(request, response, "task-quiz");
            } else if ("submit".equals(action)) {
                if (!recipient.isCanSubmit()) {
                    throw new IllegalArgumentException("Hiện không thể nộp bài cho nhiệm vụ này.");
                }
                request.setAttribute(
                        "evidenceList",
                        TaskSubmissionDB.selectEvidence(task.getId(), user.getId()));
                forward(request, response, "task-submit");
            } else if ("view".equals(action)) {
                forward(request, response, "task-view");
            } else {
                throw new IllegalArgumentException("Thao tác không hợp lệ.");
            }
        } catch (IllegalArgumentException e) {
            invalid(request, response, e);
        } catch (SQLException e) {
            response.sendError(503);
        }
    }

    /** Kiểm CSRF, xử lý form hợp lệ rồi redirect theo PRG. */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        try {
            User user = user(request, response);
            if (user == null) {
                return;
            }
            if (!SessionUtil.hasValidFormCsrfToken(request, response)) {
                return;
            }
            PracticeTask task = owned(request, response, user);
            if (task == null) {
                return;
            }
            String action = TaskFormUtil.text(request, "action");
            if ("diagnosis".equals(action)) {
                long diagnosisId = DiagnosisDB.startTask(task.getId(), user.getId());
                if (diagnosisId == 0) {
                    response.sendError(404);
                    return;
                }
                String diagnosisUrl = request.getContextPath() + "/diagnosis?action=play&id=" + diagnosisId;
                response.sendRedirect(diagnosisUrl);
                return;
            } else if ("quiz".equals(action)) {
                long roundId = TaskFormUtil.number(request, "roundId");
                TaskRecipient recipient =
                        PracticeTaskDB.selectRecipients(task.getId(), user.getId(), false).get(0);
                if (!recipient.ownsRound(roundId)) {
                    response.sendError(404);
                    return;
                }
                List<QuizQuestion> questions =
                        QuizQuestionDB.selectQuestionsByRobot(task.getRobotId());
                List<String[]> answers = new ArrayList<>();
                for (QuizQuestion question : questions) {
                    String answer = TaskFormUtil.text(request, "answer_" + question.getId());
                    if (answer.isEmpty()) {
                        throw new IllegalArgumentException("Cần trả lời đầy đủ các câu hỏi.");
                    }
                    answers.add(new String[] {question.getId(), answer});
                }
                QuizAttemptDB.submitAttempt(user.getId(), task.getRobotId(), answers, roundId);
            } else if ("preview".equals(action) || "confirm".equals(action)) {
                TaskSubmission submission =
                        TaskSubmissionDB.submit(
                                task.getId(),
                                user.getId(),
                                TaskFormUtil.submission(request),
                                "confirm".equals(action));
                if ("preview".equals(action)) {
                    request.setAttribute("task", task);
                    request.setAttribute("taskSubmission", submission);
                    forward(request, response, "task-preview");
                    return;
                }
            } else {
                throw new IllegalArgumentException("Thao tác không hợp lệ.");
            }
            String url = request.getContextPath() + "/tasks?action=view&id=" + task.getId();
            response.sendRedirect(url);
        } catch (IllegalArgumentException e) {
            invalid(request, response, e);
        } catch (SQLException e) {
            response.sendError(503);
        }
    }
}
