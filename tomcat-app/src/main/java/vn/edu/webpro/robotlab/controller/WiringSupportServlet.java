package vn.edu.webpro.robotlab.controller;

import vn.edu.webpro.robotlab.business.*;
import vn.edu.webpro.robotlab.data.*;
import vn.edu.webpro.robotlab.util.*;
import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

/** Điều phối hỗ trợ USER, không thay dây/điểm/chủ lượt nối dây. */
@WebServlet("/wiring-support")
public class WiringSupportServlet extends HttpServlet {
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
        request.setAttribute("supportAdmin", false);
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

    /** GET chỉ đọc danh sách/hội thoại/bản nguồn của chủ lượt. */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            User user = user(request, response);
            if (user == null) {
                return;
            }
            String action = TaskFormUtil.text(request, "action");
            if (action.isEmpty()) {
                request.setAttribute("supportRequests", WiringSupportDB.selectRequests(user.getId(), false));
                forward(request, response, "wiring-support-list");
            } else if ("view".equals(action)) {
                WiringSupportRequest r = WiringSupportDB.selectRequest(
                        WiringFormUtil.id(request, "id"), user.getId(), false);
                if (r == null) {
                    response.sendError(404);
                    return;
                }
                request.setAttribute("supportRequest", r);
                request.setAttribute("wiringExercise", r.getExercise());
                forward(request, response, "wiring-support-view");
            } else if ("new".equals(action)) {
                WiringAttempt a = WiringDB.selectAttempt(WiringFormUtil.id(request, "attemptId"), user.getId());
                if (a == null) {
                    response.sendError(404);
                    return;
                }
                request.setAttribute("wiringAttempt", a);
                request.setAttribute("wiringExercise", a.getExercise());
                forward(request, response, "wiring-support-new");
            } else {
                throw new IllegalArgumentException("Thao tác không hợp lệ.");
            }
        } catch (IllegalArgumentException error) {
            invalid(request, response, error);
        } catch (SQLException error) {
            response.sendError(503);
        }
    }

    private WiringSupportMessage message(HttpServletRequest request) {
        WiringSupportMessage m = new WiringSupportMessage();
        m.setContent(TaskFormUtil.text(request, "content"));
        String terminal = TaskFormUtil.text(request, "terminalId");
        if (!terminal.isEmpty()) {
            m.setTerminalId(WiringFormUtil.id(request, "terminalId"));
        }
        return m;
    }

    /** POST kiểm vai trò/token/version; thành công dùng PRG và không lưu payload bản chụp từ client. */
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
            if ("create".equals(action)) {
                id = WiringSupportDB.create(WiringFormUtil.id(request, "attemptId"), user.getId(),
                        TaskFormUtil.integer(request, "expectedVersion"), message(request));
                if (id == 0) {
                    response.sendError(404);
                    return;
                }
            } else
            if ("message".equals(action) || "close".equals(action)) {
                id = WiringFormUtil.id(request, "id");
                boolean changed = WiringSupportDB.mutate(id, user.getId(), false,
                        TaskFormUtil.integer(request, "expectedVersion"), message(request),
                        "on".equals(TaskFormUtil.text(request, "sendSnapshot")), "close".equals(action));
                if (!changed) {
                    response.sendError(404);
                    return;
                }
            } else {
                throw new IllegalArgumentException("Thao tác không hợp lệ.");
            }
            String url = request.getContextPath() + "/wiring-support?action=view&id=" + id;
            response.sendRedirect(url);
        } catch (IllegalArgumentException error) {
            invalid(request, response, error);
        } catch (SQLException error) {
            response.sendError(503);
        }
    }
}
