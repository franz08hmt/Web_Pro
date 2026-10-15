package vn.edu.webpro.robotlab.filter;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletResponse;

/** Bảo vệ cache cho các trang cũ có kết quả riêng hoặc menu theo session. */
@WebFilter(urlPatterns = {"/learning-summary", "/order-history", "/admin-stats", "/robots", "/components"})
public class WorkspaceCacheFilter implements Filter {
    /** Chỉ thêm header, không đổi dữ liệu, quyền, forward hay hợp đồng JSON. */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        ((HttpServletResponse) response).setHeader("Cache-Control", "no-store");
        chain.doFilter(request, response);
    }
}
