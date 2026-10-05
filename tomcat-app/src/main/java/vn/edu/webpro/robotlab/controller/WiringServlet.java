package vn.edu.webpro.robotlab.controller;

import vn.edu.webpro.robotlab.business.*;
import vn.edu.webpro.robotlab.data.*;
import vn.edu.webpro.robotlab.util.*;
import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

/** Điều phối phòng nối dây USER; JavaScript chỉ bổ sung thao tác của view. */
@WebServlet("/wiring")
public class WiringServlet extends HttpServlet {
    private User user(HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
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
        request.setAttribute("wiringError", error.getMessage());
        forward(request, response, "wiring-error");
    }

    /** GET chỉ đọc catalog hoặc lượt của chính tài khoản hiện tại. */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            User user = user(request, response);
            if (user == null) {
                return;
            }
            String action = TaskFormUtil.text(request, "action");
            if (action.isEmpty()) {
                request.setAttribute("wiringExercises", WiringDB.selectExercises(false));
                request.setAttribute("wiringAttempts", WiringDB.selectAttempts(user.getId()));
                forward(request, response, "wiring-list");
            } else if ("play".equals(action) || "result".equals(action)) {
                WiringAttempt a = WiringDB.selectAttempt(WiringFormUtil.id(request, "id"), user.getId());
                if (a == null) {
                    response.sendError(404);
                    return;
                }
                request.setAttribute("wiringAttempt", a);
                request.setAttribute("wiringExercise", a.getExercise());
                request.setAttribute("wiringAlreadySubmitted", "already".equals(TaskFormUtil.text(request, "notice")));
                if (a.isSubmitted()) {
                    forward(request, response, "wiring-result");
                } else {
                    forward(request, response, "wiring-play");
                }
            } else {
                throw new IllegalArgumentException("Thao tác không hợp lệ.");
            }
        } catch (IllegalArgumentException error) {
            invalid(request, response, error);
        } catch (SQLException error) {
            response.sendError(503);
        }
    }

    /** POST kiểm token/version; tách lưu nháp và nộp, thành công dùng PRG. */
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
            boolean already = false;
            if ("start".equals(action) || "retry".equals(action)) {
                long source = 0;
                int version = 0;
                if ("retry".equals(action)) {
                    source = WiringFormUtil.id(request, "id");
                    version = TaskFormUtil.integer(request, "expectedVersion");
                }
                id = WiringDB.start(WiringFormUtil.id(request, "exerciseId"), user.getId(), source, version);
                if (id == 0) {
                    response.sendError(404);
                    return;
                }
            } else if ("save".equals(action) || "submit".equals(action)
                    || "add".equals(action) || "remove".equals(action)) {
                id = WiringFormUtil.id(request, "id");
                if ("submit".equals(action)) {
                    WiringAttempt existing = WiringDB.selectAttempt(id, user.getId());
                    if (existing == null) {
                        response.sendError(404);
                        return;
                    }
                    if (existing.isSubmitted()) {
                        String url = request.getContextPath() + "/wiring?action=result&id=" + id + "&notice=already";
                        response.sendRedirect(url);
                        return;
                    }
                }
                WiringAttempt a = WiringDB.mutate(id, user.getId(), TaskFormUtil.integer(request, "expectedVersion"),
                        action, WiringFormUtil.connections(request), TaskFormUtil.text(request, "removePair"));
                if (a == null) {
                    response.sendError(404);
                    return;
                }
                already = a.isSubmitted();
            } else {
                throw new IllegalArgumentException("Thao tác không hợp lệ.");
            }
            String url = request.getContextPath() + "/wiring?action=play&id=" + id;
            if (already) {
                url += "&notice=already";
            }
            response.sendRedirect(url);
        } catch (IllegalArgumentException error) {
            invalid(request, response, error);
        } catch (SQLException error) {
            response.sendError(503);
        }
    }
}
