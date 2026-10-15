package vn.edu.webpro.robotlab.filter;

import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import vn.edu.webpro.robotlab.business.User;
import vn.edu.webpro.robotlab.util.SessionUtil;

/** Trang HTML quản trị cũng kiểm session ở server, không chỉ dựa vào menu hoặc API. */
@WebFilter(urlPatterns = {"/pages/admin-content.html", "/pages/admin-quiz.html", "/pages/admin-shop.html",
        "/pages/admin-troubleshooting.html", "/pages/admin-users.html"})
public class AdminPageFilter implements Filter {
    /** Khách về đăng nhập, USER bị chặn; không thay đổi hợp đồng các API quản trị. */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest http = (HttpServletRequest) request;
        HttpServletResponse result = (HttpServletResponse) response;
        result.setHeader("Cache-Control", "no-store");
        try {
            User user = SessionUtil.getCurrentUser(http);
            if (user == null) {
                result.sendRedirect(http.getContextPath() + "/pages/tai-khoan.html");
                return;
            }
            if (!user.isAdmin()) {
                result.sendError(403);
                return;
            }
        } catch (SQLException e) {
            result.sendError(503);
            return;
        }
        chain.doFilter(request, response);
    }
}
