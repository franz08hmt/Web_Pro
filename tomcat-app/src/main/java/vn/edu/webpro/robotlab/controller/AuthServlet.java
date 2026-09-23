package vn.edu.webpro.robotlab.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import vn.edu.webpro.robotlab.dao.DatabaseConnectionFactory;
import vn.edu.webpro.robotlab.dao.UserDao;
import vn.edu.webpro.robotlab.model.User;
import vn.edu.webpro.robotlab.service.AuthService;
import vn.edu.webpro.robotlab.service.AccountSession;
import vn.edu.webpro.robotlab.service.PasswordService;
import vn.edu.webpro.robotlab.web.ApiResponses;
import vn.edu.webpro.robotlab.web.Json;
import vn.edu.webpro.robotlab.web.AdminAccess;

/**
 * Controller xác thực và tự quản lý tài khoản: register, login, logout, me,
 * profile và password.
 *
 * Trạng thái đăng nhập nằm trong HttpSession của Tomcat (cookie JSESSIONID),
 * không nằm ở phía trình duyệt. Mỗi phiên giữ thêm một CSRF token để các
 * request ghi phải chứng minh là do chính trang của ứng dụng gửi lên.
 */
@WebServlet("/api/auth/*")
public final class AuthServlet extends HttpServlet {
    private static final int HTTP_UNPROCESSABLE_ENTITY = 422;

    private final AuthService service = new AuthService(
            new UserDao(new DatabaseConnectionFactory(System.getenv())),
            new PasswordService()
    );

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws javax.servlet.ServletException, IOException {
        if ("PATCH".equals(request.getMethod())) {
            updateProfile(request, response);
            return;
        }
        super.service(request, response);
    }

    /** GET /api/auth/me — trả về người dùng của phiên hiện tại và CSRF token. */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!"/me".equals(request.getPathInfo())) {
            ApiResponses.error(response, HttpServletResponse.SC_NOT_FOUND,
                    "NOT_FOUND", "Không tìm thấy tài nguyên yêu cầu.");
            return;
        }

        User user = currentUser(request, response);
        if (user == null) return;
        HttpSession session = request.getSession(false);
        String csrfToken = Json.quote((String) session.getAttribute("csrfToken"));
        ApiResponses.json(response, HttpServletResponse.SC_OK,
                "{\"data\":{\"user\":" + user.toJson() + "},\"csrfToken\":" + csrfToken + "}");
    }

    private User currentUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            User user = AccountSession.load(request);
            if (user != null) return user;
        } catch (SQLException exception) {
            ApiResponses.error(response, 503, "DEPENDENCY_NOT_READY", "Cơ sở dữ liệu chưa sẵn sàng.");
            return null;
        }
        ApiResponses.error(response, HttpServletResponse.SC_UNAUTHORIZED,
                "AUTH_REQUIRED", "Bạn cần đăng nhập để tiếp tục.");
        return null;
    }

    /** POST /api/auth/register, /login hoặc /logout. */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String path = request.getPathInfo();

        if ("/logout".equals(path)) {
            if (currentUser(request, response) == null || !AdminAccess.requireCsrf(request, response)) return;
            logout(request, response);
            return;
        }

        try {
            String body = readBody(request);
            String email = Json.stringField(body, "email");
            String password = Json.stringField(body, "password");

            User user;
            if ("/register".equals(path)) {
                user = service.register(Json.stringField(body, "fullName"), email, password);
            } else if ("/login".equals(path)) {
                user = service.login(email, password);
            } else {
                ApiResponses.error(response, HttpServletResponse.SC_NOT_FOUND,
                        "NOT_FOUND", "Không tìm thấy tài nguyên yêu cầu.");
                return;
            }

            startSession(request, user);
            int status = "/register".equals(path)
                    ? HttpServletResponse.SC_CREATED
                    : HttpServletResponse.SC_OK;
            ApiResponses.json(response, status, "{\"data\":{\"user\":" + user.toJson() + "}}");
        } catch (IllegalStateException exception) {
            ApiResponses.error(response, HttpServletResponse.SC_CONFLICT,
                    "EMAIL_ALREADY_EXISTS", "Email đã được sử dụng.");
        } catch (IllegalArgumentException exception) {
            rejectInput(response, exception);
        } catch (SQLException exception) {
            if (exception.getErrorCode() == 1062) {
                ApiResponses.error(response, 409, "EMAIL_ALREADY_EXISTS", "Email đã được sử dụng.");
            } else {
                ApiResponses.error(response, 503, "DEPENDENCY_NOT_READY", "Cơ sở dữ liệu chưa sẵn sàng.");
            }
        }
    }

    private void updateProfile(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!"/profile".equals(request.getPathInfo())) {
            ApiResponses.error(response, 404, "NOT_FOUND", "Không tìm thấy tài nguyên yêu cầu.");
            return;
        }
        User actor = currentUser(request, response);
        if (actor == null || !AdminAccess.requireCsrf(request, response)) return;
        try {
            User updated = service.updateProfile(actor.id(), Json.stringField(readBody(request), "fullName"));
            request.getSession(false).setAttribute("user", updated);
            ApiResponses.json(response, 200, "{\"data\":{\"user\":" + updated.toJson() + "}}");
        } catch (IllegalArgumentException exception) {
            ApiResponses.error(response, HTTP_UNPROCESSABLE_ENTITY, "VALIDATION_ERROR", "Họ tên phải có 2–100 ký tự.");
        } catch (SQLException exception) {
            ApiResponses.error(response, 503, "DEPENDENCY_NOT_READY", "Cơ sở dữ liệu chưa sẵn sàng.");
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!"/password".equals(request.getPathInfo())) {
            ApiResponses.error(response, 404, "NOT_FOUND", "Không tìm thấy tài nguyên yêu cầu.");
            return;
        }
        User actor = currentUser(request, response);
        if (actor == null || !AdminAccess.requireCsrf(request, response)) return;
        try {
            String body = readBody(request);
            service.changePassword(actor.id(), Json.stringField(body, "currentPassword"),
                    Json.stringField(body, "newPassword"));
            request.getSession(false).invalidate();
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (IllegalArgumentException exception) {
            if ("credentials".equals(exception.getMessage())) {
                ApiResponses.error(response, 401, "INVALID_CREDENTIALS", "Mật khẩu hiện tại không đúng.");
            } else {
                ApiResponses.error(response, HTTP_UNPROCESSABLE_ENTITY, "VALIDATION_ERROR",
                        "Mật khẩu mới phải có 8–72 ký tự và khác mật khẩu cũ.");
            }
        } catch (SQLException exception) {
            ApiResponses.error(response, 503, "DEPENDENCY_NOT_READY", "Cơ sở dữ liệu chưa sẵn sàng.");
        }
    }

    private void logout(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    /* Tạo phiên mới sau khi xác thực thành công. Token CSRF sinh một lần cho cả
       phiên; client đọc lại qua /api/auth/me rồi gửi kèm header X-CSRF-Token. */
    private void startSession(HttpServletRequest request, User user) {
        HttpSession old = request.getSession(false);
        if (old != null) old.invalidate();
        HttpSession session = request.getSession(true);
        session.setAttribute("user", user);
        session.setAttribute("csrfToken", UUID.randomUUID().toString());
    }

    /* Sai mật khẩu trả 401, còn dữ liệu nhập không hợp lệ trả 422. Thông điệp
       giữ chung chung để không tiết lộ email nào đã tồn tại trong hệ thống. */
    private void rejectInput(HttpServletResponse response, IllegalArgumentException exception)
            throws IOException {
        boolean wrongCredentials = "credentials".equals(exception.getMessage());
        if (wrongCredentials) {
            ApiResponses.error(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "INVALID_CREDENTIALS", "Email hoặc mật khẩu không đúng.");
        } else {
            ApiResponses.error(response, HTTP_UNPROCESSABLE_ENTITY,
                    "VALIDATION_ERROR", "Dữ liệu không hợp lệ.");
        }
    }

    private String readBody(HttpServletRequest request) throws IOException {
        return Json.readBody(request.getReader(), 8192);
    }
}
