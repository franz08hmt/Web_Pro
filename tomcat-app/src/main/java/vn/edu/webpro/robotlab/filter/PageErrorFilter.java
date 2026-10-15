package vn.edu.webpro.robotlab.filter;

import java.io.IOException;
import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import vn.edu.webpro.robotlab.util.ResponseUtil;

/** Trang lỗi chỉ dành cho HTML; lỗi container của API vẫn giữ JSON và mã HTTP. */
@WebFilter(urlPatterns = "/*", dispatcherTypes = {DispatcherType.ERROR})
public class PageErrorFilter implements Filter {
    /** Không in exception/message của container ra trang người học. */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest http = (HttpServletRequest) request;
        HttpServletResponse result = (HttpServletResponse) response;
        result.setHeader("Cache-Control", "no-store");
        Object original = request.getAttribute("javax.servlet.error.request_uri");
        if (original instanceof String && ((String) original).startsWith(http.getContextPath() + "/api/")) {
            Object value = request.getAttribute("javax.servlet.error.status_code");
            int status = value instanceof Integer ? ((Integer) value).intValue() : 500;
            result.resetBuffer();
            ResponseUtil.sendError(result, status, status == 404 ? "NOT_FOUND" : "INTERNAL_SERVER_ERROR",
                    status == 404 ? "Không tìm thấy nội dung bạn yêu cầu." : "Đã xảy ra lỗi. Vui lòng thử lại sau.");
            return;
        }
        chain.doFilter(request, response);
    }
}
