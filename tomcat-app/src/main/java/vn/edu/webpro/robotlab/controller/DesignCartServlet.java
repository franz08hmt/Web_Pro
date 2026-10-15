package vn.edu.webpro.robotlab.controller;

import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import vn.edu.webpro.robotlab.business.DesignCartResult;
import vn.edu.webpro.robotlab.business.User;
import vn.edu.webpro.robotlab.data.CartDB;
import vn.edu.webpro.robotlab.util.DesignFormUtil;
import vn.edu.webpro.robotlab.util.SessionUtil;
import vn.edu.webpro.robotlab.util.ResponseUtil;

/** Form truyền thống: đọc tham số, kiểm quyền/token, bổ sung rồi redirect tới giỏ. */
@WebServlet(urlPatterns = {"/design-cart", "/api/design-cart-notice"})
public class DesignCartServlet extends HttpServlet {
    /** POST không checkout, không đổi tồn kho; ID khác chủ trả 404. */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!"/design-cart".equals(request.getServletPath())) {
            ResponseUtil.sendError(response, 404, "NOT_FOUND", "Không tìm thấy trang bạn yêu cầu.");
            return;
        }
        request.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-store");
        try {
            User user = SessionUtil.getCurrentUser(request);
            if (user == null) {
                response.sendRedirect(request.getContextPath() + "/pages/tai-khoan.html");
                return;
            }
            if (!SessionUtil.hasValidFormCsrfToken(request, response)) {
                return;
            }
            long id = DesignFormUtil.id(request.getParameter("designId"));
            DesignCartResult result = id > 0 ? CartDB.ensureMissing(user.getId(), id) : null;
            if (result == null) {
                response.sendError(404);
                return;
            }
            request.getSession().setAttribute("designCartNotice", result);
            response.sendRedirect(request.getContextPath() + "/pages/gio-hang.html?designNotice=1");
        } catch (SQLException e) {
            response.sendError(503);
        }
    }

    /** GET chỉ đọc thông báo của chính session sau PRG, không ghi giỏ hoặc kho. */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setHeader("Cache-Control", "no-store");
        if (SessionUtil.requireUser(request, response) == null) {
            return;
        }
        DesignCartResult notice = (DesignCartResult) request.getSession().getAttribute("designCartNotice");
        ResponseUtil.sendJson(response, 200, "{\"data\":" + (notice == null ? "[]" : notice.toJson()) + "}");
    }
}
