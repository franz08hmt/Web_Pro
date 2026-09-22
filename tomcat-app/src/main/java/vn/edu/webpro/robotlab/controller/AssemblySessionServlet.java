package vn.edu.webpro.robotlab.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import vn.edu.webpro.robotlab.dao.AssemblySessionDao;
import vn.edu.webpro.robotlab.dao.AssemblyStepDao;
import vn.edu.webpro.robotlab.dao.DatabaseConnectionFactory;
import vn.edu.webpro.robotlab.dao.RobotDao;
import vn.edu.webpro.robotlab.model.AssemblySession;
import vn.edu.webpro.robotlab.model.User;
import vn.edu.webpro.robotlab.service.AssemblySessionService;
import vn.edu.webpro.robotlab.web.ApiResponses;
import vn.edu.webpro.robotlab.web.Json;

/**
 * Controller phiên lắp ráp của từng tài khoản.
 *
 * POST   /api/assembly-sessions            tạo hoặc tiếp tục phiên cho một robot
 * GET    /api/assembly-sessions            liệt kê phiên của người đang đăng nhập
 * GET    /api/assembly-sessions/{id}       chi tiết một phiên
 * PATCH  /api/assembly-sessions/{id}       đổi trạng thái phiên
 * PUT    /api/assembly-sessions/{id}/...   cập nhật linh kiện, bước hoặc part 3D
 *
 * Mọi thao tác ghi đều yêu cầu đăng nhập và CSRF token hợp lệ.
 */
@WebServlet("/api/assembly-sessions/*")
public final class AssemblySessionServlet extends HttpServlet {
    private static final int HTTP_UNPROCESSABLE_ENTITY = 422;
    private static final int DEFAULT_PAGE_SIZE = 20;

    private final AssemblySessionService service = new AssemblySessionService(
            new AssemblySessionDao(new DatabaseConnectionFactory(System.getenv())),
            new RobotDao(new DatabaseConnectionFactory(System.getenv())),
            new AssemblyStepDao(new DatabaseConnectionFactory(System.getenv()))
    );

    /* HttpServlet không có doPatch(), nên PATCH được tách ra từ service()
       trước khi gọi super — đây là cách chuẩn để bổ sung một HTTP method. */
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if ("PATCH".equals(request.getMethod())) {
            doPatch(request, response);
            return;
        }
        super.service(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User user = currentUser(request, response);
        if (user == null) return;

        try {
            String body = readBody(request);
            AssemblySession session = service.create(user.id(), Json.stringField(body, "robotId"));
            ApiResponses.json(response, HttpServletResponse.SC_CREATED,
                    "{\"data\":" + session.toJson() + "}");
        } catch (IllegalArgumentException exception) {
            ApiResponses.error(response, HTTP_UNPROCESSABLE_ENTITY,
                    "VALIDATION_ERROR", "Robot không tồn tại hoặc dữ liệu không hợp lệ.");
        } catch (SQLException exception) {
            databaseUnavailable(response);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User user = currentUser(request, response);
        if (user == null) return;

        try {
            String path = request.getPathInfo();
            if (path == null || "/".equals(path)) {
                listSessions(response, user);
                return;
            }

            long sessionId = Long.parseLong(path.substring(1));
            ApiResponses.json(response, HttpServletResponse.SC_OK,
                    "{\"data\":" + service.get(user.id(), sessionId).toJson() + "}");
        } catch (IllegalArgumentException exception) {
            ApiResponses.error(response, HttpServletResponse.SC_NOT_FOUND,
                    "NOT_FOUND", "Không tìm thấy phiên lắp ráp.");
        } catch (SQLException exception) {
            databaseUnavailable(response);
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User user = currentUser(request, response);
        if (user == null || !hasValidCsrfToken(request, response)) return;

        try {
            // Đường dẫn dạng {id}/{nhóm}/{mã} — ví dụ 42/components/arduino-uno.
            String[] segments = request.getPathInfo().substring(1).split("/");
            long sessionId = Long.parseLong(segments[0]);
            String group = segments[1];
            String itemId = segments[2];
            String body = readBody(request);

            AssemblySession session;
            switch (group) {
                case "components" -> session = service.component(user.id(), sessionId, itemId,
                        Boolean.parseBoolean(Json.stringField(body, "isPrepared")));
                case "steps" -> session = service.step(user.id(), sessionId, itemId,
                        Json.stringField(body, "status"));
                case "visual-parts" -> session = service.visual(user.id(), sessionId, itemId,
                        Boolean.parseBoolean(Json.stringField(body, "isAssembled")));
                default -> session = null;
            }

            if (session == null) {
                ApiResponses.error(response, HttpServletResponse.SC_NOT_FOUND,
                        "NOT_FOUND", "Không tìm thấy tài nguyên yêu cầu.");
                return;
            }
            ApiResponses.json(response, HttpServletResponse.SC_OK,
                    "{\"data\":" + session.toJson() + "}");
        } catch (SQLException exception) {
            databaseUnavailable(response);
        } catch (RuntimeException exception) {
            // Đường dẫn thiếu đoạn, ID không phải số hoặc thân request sai định dạng.
            ApiResponses.error(response, HTTP_UNPROCESSABLE_ENTITY,
                    "VALIDATION_ERROR", "Dữ liệu phiên lắp ráp không hợp lệ.");
        }
    }

    private void doPatch(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User user = currentUser(request, response);
        if (user == null || !hasValidCsrfToken(request, response)) return;

        try {
            long sessionId = Long.parseLong(request.getPathInfo().substring(1));
            String body = readBody(request);
            AssemblySession session = service.status(user.id(), sessionId,
                    Json.stringField(body, "status"));
            ApiResponses.json(response, HttpServletResponse.SC_OK,
                    "{\"data\":" + session.toJson() + "}");
        } catch (IllegalArgumentException exception) {
            boolean badTransition = "transition".equals(exception.getMessage());
            ApiResponses.error(response,
                    badTransition ? HttpServletResponse.SC_CONFLICT : HTTP_UNPROCESSABLE_ENTITY,
                    badTransition ? "INVALID_STATE_TRANSITION" : "VALIDATION_ERROR",
                    "Không thể chuyển sang trạng thái yêu cầu.");
        } catch (SQLException exception) {
            databaseUnavailable(response);
        }
    }

    private void listSessions(HttpServletResponse response, User user) throws SQLException, IOException {
        List<AssemblySession> sessions = service.list(user.id());
        String items = sessions.stream()
                .map(AssemblySession::toJson)
                .reduce((left, right) -> left + "," + right)
                .orElse("");
        ApiResponses.json(response, HttpServletResponse.SC_OK,
                "{\"data\":[" + items + "],\"meta\":{\"page\":1,\"pageSize\":" + DEFAULT_PAGE_SIZE
                        + ",\"total\":" + sessions.size() + "}}");
    }

    private User currentUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        Object value = session == null ? null : session.getAttribute("user");
        if (value instanceof User user) return user;

        ApiResponses.error(response, HttpServletResponse.SC_UNAUTHORIZED,
                "AUTH_REQUIRED", "Bạn cần đăng nhập để tiếp tục.");
        return null;
    }

    private boolean hasValidCsrfToken(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        Object token = session == null ? null : session.getAttribute("csrfToken");
        if (token instanceof String expected && expected.equals(request.getHeader("X-CSRF-Token"))) {
            return true;
        }

        ApiResponses.error(response, HttpServletResponse.SC_FORBIDDEN,
                "CSRF_REQUIRED", "Yêu cầu cần mã CSRF hợp lệ.");
        return false;
    }

    private void databaseUnavailable(HttpServletResponse response) throws IOException {
        ApiResponses.error(response, HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                "DEPENDENCY_NOT_READY", "Cơ sở dữ liệu chưa được cấu hình.");
    }

    private String readBody(HttpServletRequest request) throws IOException {
        return request.getReader().lines().reduce("", (left, right) -> left + right);
    }
}
