package vn.edu.webpro.robotlab.filter;

import java.io.IOException;
import java.util.UUID;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import vn.edu.webpro.robotlab.util.ResponseUtil;

/** Cross-cutting HTTP work: encoding, request correlation and safe fallback errors. */
@WebFilter(urlPatterns = {"/api/*", "/account", "/architecture"})
public class RequestContextFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String requestId = UUID.randomUUID().toString();
        httpRequest.setAttribute("requestId", requestId);
        httpResponse.setHeader("X-Request-Id", requestId);

        try {
            chain.doFilter(request, response);
        } catch (Exception exception) {
            if (httpResponse.isCommitted()) throw new ServletException(exception);
            if (httpRequest.getRequestURI().contains("/api/")) {
                ResponseUtil.sendError(httpResponse, 500, "INTERNAL_SERVER_ERROR",
                        "Đã xảy ra lỗi máy chủ. Vui lòng thử lại sau.");
            } else {
                httpResponse.sendError(500, "Đã xảy ra lỗi máy chủ.");
            }
        }
    }
}
