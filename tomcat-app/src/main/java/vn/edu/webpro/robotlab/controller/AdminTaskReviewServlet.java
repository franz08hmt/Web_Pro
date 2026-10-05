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

@WebServlet("/admin-task-reviews")
/** Điều phối xem và chấm bài nộp dành cho ADMIN. */
public class AdminTaskReviewServlet extends HttpServlet {

    private User user(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {
        response.setHeader("Cache-Control", "no-store");
        User user = SessionUtil.getCurrentUser(request);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/pages/tai-khoan.html");
            return null;
        }
        if (!user.isAdmin()) {
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

    /** Đọc trang trong phạm vi vai trò; dữ liệu sai trả task-error. */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            User user = user(request, response);
            if (user == null) {
                return;
            }
            String action = TaskFormUtil.text(request, "action");
            if (!action.isEmpty() && !"view".equals(action)) {
                throw new IllegalArgumentException("Thao tác không hợp lệ.");
            }
            long id = TaskFormUtil.number(request, "id");
            TaskSubmission submission = TaskSubmissionDB.selectSubmission(id, user.getId(), true);
            if (submission == null) {
                response.sendError(404);
                return;
            }
            long taskId = TaskSubmissionDB.taskIdForSubmission(id);
            PracticeTask task = PracticeTaskDB.selectTask(taskId, user.getId(), true);
            request.setAttribute("task", task);
            request.setAttribute("taskSubmission", submission);
            if (submission.isHasDiagnosis()) {
                request.setAttribute("diagnosisAttempt", DiagnosisDB.selectForSubmission(id, user.getId(), true));
            }
            request.setAttribute("currentReview", submission.getCurrentReview());
            for (TaskRecipient recipient :
                    PracticeTaskDB.selectRecipients(taskId, user.getId(), true)) {
                if (recipient.getId() == submission.getRecipientId()) {
                    request.setAttribute("reviewRecipient", recipient);
                }
            }
            List<TaskSubmission> history = new ArrayList<>();
            for (TaskSubmission prior :
                    TaskSubmissionDB.selectSubmissions(taskId, user.getId(), true)) {
                if (prior.getRecipientId() == submission.getRecipientId()) {
                    history.add(prior);
                }
            }
            request.setAttribute("submissions", history);
            forward(request, response, "admin-task-review");
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
            long id = TaskFormUtil.number(request, "id");
            if (TaskSubmissionDB.selectSubmission(id, user.getId(), true) == null) {
                response.sendError(404);
                return;
            }
            if (!"review".equals(TaskFormUtil.text(request, "action"))) {
                throw new IllegalArgumentException("Thao tác không hợp lệ.");
            }
            TaskReview review = new TaskReview();
            review.setExplanationLevel(TaskFormUtil.integer(request, "explanationLevel"));
            review.setConclusion(TaskFormUtil.text(request, "conclusion"));
            review.setStrengths(TaskFormUtil.text(request, "strengths"));
            review.setImprovements(TaskFormUtil.text(request, "improvements"));
            review.setRetryGuidance(TaskFormUtil.text(request, "retryGuidance"));
            review.setChangeReason(TaskFormUtil.text(request, "changeReason"));
            TaskSubmissionDB.review(
                    id, user.getId(), TaskFormUtil.number(request, "expectedReviewId"), review);
            String url = request.getContextPath() + "/admin-task-reviews?id=" + id;
            response.sendRedirect(url);
        } catch (IllegalArgumentException e) {
            invalid(request, response, e);
        } catch (SQLException e) {
            response.sendError(503);
        }
    }
}
