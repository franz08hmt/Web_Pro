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
@WebServlet("/admin-diagnosis")
public class AdminDiagnosisServlet extends HttpServlet {

    private User user(HttpServletRequest request, HttpServletResponse response)
            throws IOException, SQLException {
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

    private void invalid(HttpServletRequest request, HttpServletResponse response, IllegalArgumentException error)
            throws ServletException, IOException {
        response.setStatus(422);
        request.setAttribute("taskError", error.getMessage());
        forward(request, response, "task-error");
    }

    /** Đọc form nháp và xem trước; GET không tạo lượt thực hành. */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            User user = user(request, response);
            if (user == null) {
                return;
            }
            String action = TaskFormUtil.text(request, "action");
            if (action.isEmpty()) {
                request.setAttribute("diagnosisScenarios", DiagnosisDB.selectScenarios(true));
                forward(request, response, "admin-diagnosis-list");
                return;
            }
            if (!"new".equals(action) && !"edit".equals(action)
                    && !"view".equals(action) && !"preview".equals(action)) {
                throw new IllegalArgumentException("Thao tác không hợp lệ.");
            }
            DiagnosisScenario value;
            if ("new".equals(action)) {
                value = new DiagnosisScenario();
            } else {
                value = DiagnosisDB.selectScenario(TaskFormUtil.number(request, "id"), true);
                if (value == null) {
                    response.sendError(404);
                    return;
                }
            }
            request.setAttribute("diagnosisScenario", value);
            if ("new".equals(action) || "edit".equals(action)) {
                value.requireDraft();
                request.setAttribute("robots", RobotDB.selectRobots(100, 0));
                request.setAttribute("diagnosisGuides", TroubleshootingGuideDB.selectGuides(null, null, null, 1000, 0));
                forward(request, response, "admin-diagnosis-form");
            } else if ("view".equals(action) || "preview".equals(action)) {
                request.setAttribute("diagnosisPreview", "preview".equals(action));
                forward(request, response, "admin-diagnosis-preview");
            } else {
                throw new IllegalArgumentException("Thao tác không hợp lệ.");
            }
        } catch (IllegalArgumentException e) {
            invalid(request, response, e);
        } catch (SQLException e) {
            response.sendError(503);
        }
    }

    /** Lưu nháp, công bố, nhân bản hoặc lưu trữ với token form hợp lệ. */
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
            if (!"saveDraft".equals(action) && !"publish".equals(action)
                    && !"duplicate".equals(action) && !"archive".equals(action)) {
                throw new IllegalArgumentException("Thao tác không hợp lệ.");
            }
            if ("saveDraft".equals(action)) {
                id = DiagnosisDB.saveDraft(DiagnosisFormUtil.scenario(request), user.getId());
            } else {
                id = DiagnosisDB.manage(TaskFormUtil.number(request, "id"), user.getId(), action);
            }
            if (id == 0) {
                response.sendError(404);
                return;
            }
            String url = request.getContextPath() + "/admin-diagnosis?action=view&id=" + id;
            response.sendRedirect(url);
        } catch (IllegalArgumentException e) {
            invalid(request, response, e);
        } catch (SQLException e) {
            response.sendError(503);
        }
    }

}
