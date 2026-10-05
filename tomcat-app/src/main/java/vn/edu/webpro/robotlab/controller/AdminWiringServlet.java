package vn.edu.webpro.robotlab.controller;

import vn.edu.webpro.robotlab.business.*;
import vn.edu.webpro.robotlab.data.*;
import vn.edu.webpro.robotlab.util.*;
import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

/** Quản lý bài nối dây bất biến sau công bố, dành riêng ADMIN. */
@WebServlet("/admin-wiring")
public class AdminWiringServlet extends HttpServlet {
    private User user(HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
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
        request.setAttribute("wiringError", error.getMessage());
        forward(request, response, "wiring-error");
    }

    /** Xem nháp/bài/preview không tạo lượt USER, không ghi database. */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            User user = user(request, response);
            if (user == null) {
                return;
            }
            String action = TaskFormUtil.text(request, "action");
            if (action.isEmpty()) {
                request.setAttribute("wiringExercises", WiringDB.selectExercises(true));
                forward(request, response, "admin-wiring-list");
                return;
            }
            if (!"new".equals(action) && !"edit".equals(action)
                    && !"view".equals(action) && !"preview".equals(action)) {
                throw new IllegalArgumentException("Thao tác không hợp lệ.");
            }
            WiringExercise e;
            if ("new".equals(action)) {
                e = new WiringExercise();
            } else {
                e = WiringDB.selectExercise(WiringFormUtil.id(request, "id"), true);
                if (e == null) {
                    response.sendError(404);
                    return;
                }
            }
            request.setAttribute("wiringExercise", e);
            if ("new".equals(action) || "edit".equals(action)) {
                e.requireDraft();
                request.setAttribute("robots", RobotDB.selectAllRobots());
                forward(request, response, "admin-wiring-form");
            } else {
                request.setAttribute("wiringPreview", "preview".equals(action));
                forward(request, response, "admin-wiring-view");
            }
        } catch (IllegalArgumentException error) {
            invalid(request, response, error);
        } catch (SQLException error) {
            response.sendError(503);
        }
    }

    /** Lưu nháp/công bố/nhân bản/lưu trữ sau kiểm CSRF; không chấm thay USER. */
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
            if ("saveDraft".equals(action)) {
                id = WiringDB.saveDraft(WiringFormUtil.exercise(request), user.getId());
            } else if ("publish".equals(action) || "duplicate".equals(action) || "archive".equals(action)) {
                id = WiringDB.manage(WiringFormUtil.id(request, "id"), user.getId(), action);
            } else {
                throw new IllegalArgumentException("Thao tác không hợp lệ.");
            }
            if (id == 0) {
                response.sendError(404);
                return;
            }
            String url = request.getContextPath() + "/admin-wiring?action=view&id=" + id;
            response.sendRedirect(url);
        } catch (IllegalArgumentException error) {
            invalid(request, response, error);
        } catch (SQLException error) {
            response.sendError(503);
        }
    }
}
