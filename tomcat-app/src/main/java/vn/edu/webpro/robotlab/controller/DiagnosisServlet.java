package vn.edu.webpro.robotlab.controller;

import vn.edu.webpro.robotlab.business.*;
import vn.edu.webpro.robotlab.data.*;
import vn.edu.webpro.robotlab.util.*;
import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

/** Điều phối form chẩn đoán theo Model 2. */
@WebServlet("/diagnosis")
public class DiagnosisServlet extends HttpServlet {

    private User user(HttpServletRequest request, HttpServletResponse response)
            throws IOException, SQLException {
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

    private void invalid(HttpServletRequest request, HttpServletResponse response, IllegalArgumentException error)
            throws ServletException, IOException {
        response.setStatus(422);
        request.setAttribute("taskError", error.getMessage());
        forward(request, response, "task-error");
    }

    /** Đọc danh sách luyện, lượt đang làm hoặc kết quả của chính người đăng nhập. */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            User user = user(request, response);
            if (user == null) {
                return;
            }
            String action = TaskFormUtil.text(request, "action");
            if (action.isEmpty()) {
                request.setAttribute("diagnosisScenarios", DiagnosisDB.selectScenarios(false));
                request.setAttribute("diagnosisAttempts", DiagnosisDB.selectInProgress(user.getId()));
                forward(request, response, "diagnosis-list");
            } else if ("play".equals(action) || "result".equals(action)) {
                DiagnosisAttempt value = DiagnosisDB.selectAttempt(TaskFormUtil.number(request, "id"), user.getId());
                if (value == null) {
                    response.sendError(404);
                    return;
                }
                request.setAttribute("diagnosisAttempt", value);
                if (value.isSubmitted()) {
                    forward(request, response, "diagnosis-result");
                } else {
                    forward(request, response, "diagnosis-play");
                }
            } else {
                throw new IllegalArgumentException("Thao tác không hợp lệ.");
            }
        } catch (IllegalArgumentException e) {
            invalid(request, response, e);
        } catch (SQLException e) {
            response.sendError(503);
        }
    }

    /** Ghi phép kiểm tra hoặc kết luận sau khi kiểm token; dùng PRG sau thành công. */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        try {
            User user = user(request, response);
            if (user == null || !SessionUtil.hasValidFormCsrfToken(request, response)) {
                return;
            }
            String action = TaskFormUtil.text(request, "action");
            long id;
            if ("start".equals(action)) {
                id = DiagnosisDB.startPractice(TaskFormUtil.number(request, "scenarioId"), user.getId());
                if (id == 0) {
                    response.sendError(404);
                    return;
                }
            } else {
                id = TaskFormUtil.number(request, "id");
                boolean found;
                if ("check".equals(action)) {
                    found = DiagnosisDB.check(id, user.getId(), TaskFormUtil.number(request, "checkId"));
                } else if ("conclude".equals(action)) {
                    found = DiagnosisDB.conclude(id, user.getId(), TaskFormUtil.number(request, "causeOptionId"),
                            TaskFormUtil.number(request, "actionOptionId"));
                } else {
                    throw new IllegalArgumentException("Thao tác không hợp lệ.");
                }
                if (!found) {
                    response.sendError(404);
                    return;
                }
            }
            String url = request.getContextPath() + "/diagnosis?action=play&id=" + id;
            response.sendRedirect(url);
        } catch (IllegalArgumentException e) {
            invalid(request, response, e);
        } catch (SQLException e) {
            response.sendError(503);
        }
    }

}
