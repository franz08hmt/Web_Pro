package vn.edu.webpro.robotlab.util;

import java.io.IOException;
import java.util.UUID;
import javax.servlet.http.HttpServletResponse;

/** Ghi response JSON cho các API; mọi lỗi có cùng dạng {"error":{code,message,requestId}}. */
public final class ResponseUtil {
    private ResponseUtil() {
    }

    public static void sendJson(HttpServletResponse response, int status, String body) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-store");
        response.getWriter().write(body);
    }

    public static void sendError(HttpServletResponse response, int status, String code, String message)
            throws IOException {
        String requestId = UUID.randomUUID().toString();
        sendJson(response, status, "{\"error\":{\"code\":" + JsonUtil.quote(code)
                + ",\"message\":" + JsonUtil.quote(message)
                + ",\"requestId\":" + JsonUtil.quote(requestId) + "}}");
    }

    /** Lỗi kết nối MySQL: trả 503 để giao diện báo "thử lại sau", không báo nhầm là sai dữ liệu. */
    public static void sendDatabaseUnavailable(HttpServletResponse response) throws IOException {
        sendError(response, HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                "DEPENDENCY_NOT_READY", "Cơ sở dữ liệu chưa sẵn sàng.");
    }
}
