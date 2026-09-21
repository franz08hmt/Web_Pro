package vn.edu.webpro.robotlab.web;

import java.io.IOException;
import java.util.UUID;
import javax.servlet.http.HttpServletResponse;

public final class ApiResponses {
    private ApiResponses() {
    }

    public static void json(HttpServletResponse response, int status, String body) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-store");
        response.getWriter().write(body);
    }

    public static void error(HttpServletResponse response, int status, String code, String message)
            throws IOException {
        String requestId = UUID.randomUUID().toString();
        json(response, status, "{\"error\":{\"code\":" + Json.quote(code)
                + ",\"message\":" + Json.quote(message)
                + ",\"requestId\":" + Json.quote(requestId) + "}}");
    }
}
