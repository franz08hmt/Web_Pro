package vn.edu.webpro.robotlab.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import vn.edu.webpro.robotlab.business.KitItem;
import vn.edu.webpro.robotlab.business.DesignRuleSet;
import vn.edu.webpro.robotlab.business.User;
import vn.edu.webpro.robotlab.data.UserKitDB;
import vn.edu.webpro.robotlab.data.DesignRuleDB;
import vn.edu.webpro.robotlab.util.DesignFormUtil;
import vn.edu.webpro.robotlab.util.SessionUtil;

/** Form bộ linh kiện: request/session, JavaBean/JDBC, attribute/forward hoặc PRG. */
@WebServlet("/my-kit")
public class MyKitServlet extends HttpServlet {
    /** GET chỉ đọc, không tự nhập dữ liệu từ đơn hàng. */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        show(request, response, false);
    }

    /** POST có token form; không nhận owner qua hidden field. */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        show(request, response, true);
    }

    private void show(HttpServletRequest request, HttpServletResponse response, boolean post)
            throws ServletException, IOException {
        response.setHeader("Cache-Control", "no-store");
        try {
            User user = SessionUtil.getCurrentUser(request);
            if (user == null) {
                response.sendRedirect(request.getContextPath() + "/pages/tai-khoan.html");
                return;
            }
            if (post && !SessionUtil.hasValidFormCsrfToken(request, response)) {
                return;
            }
            DesignRuleSet rules = DesignRuleDB.selectPublished();
            List<KitItem> items = UserKitDB.selectItems(user.getId(), rules);
            request.setAttribute("kitItems", items);
            if (post) {
                try {
                    String action = request.getParameter("action");
                    if ("import".equals(action)) {
                        UserKitDB.change(user.getId(), new HashMap<String, Integer>(), true);
                    } else if ("save".equals(action)) {
                        UserKitDB.change(user.getId(), DesignFormUtil.quantities(request, items), false);
                    } else {
                        throw new IllegalArgumentException("Thao tác không hợp lệ. Hãy dùng nút trên form.");
                    }
                    response.sendRedirect(request.getContextPath() + "/my-kit?saved=1");
                    return;
                } catch (IllegalArgumentException e) {
                    response.setStatus(422);
                    request.setAttribute("designError", e.getMessage());
                }
            }
        } catch (SQLException e) {
            response.setStatus(503);
            request.setAttribute("designError", "Dữ liệu tạm chưa sẵn sàng. Vui lòng thử lại sau.");
        }
        String url = "/WEB-INF/views/my-kit.jsp";
        getServletContext().getRequestDispatcher(url).forward(request, response);
    }
}
