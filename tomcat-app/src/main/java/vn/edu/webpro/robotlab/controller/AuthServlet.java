package vn.edu.webpro.robotlab.controller;

import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import vn.edu.webpro.robotlab.business.User;
import vn.edu.webpro.robotlab.data.UserDB;
import vn.edu.webpro.robotlab.util.JsonUtil;
import vn.edu.webpro.robotlab.util.PasswordUtil;
import vn.edu.webpro.robotlab.util.ResponseUtil;
import vn.edu.webpro.robotlab.util.SessionUtil;
import vn.edu.webpro.robotlab.util.ValidationUtil;

/**
 * Controller tài khoản: đăng ký, đăng nhập, đăng xuất, xem/sửa hồ sơ, đổi mật khẩu.
 *
 * POST  /api/auth/register   POST /api/auth/login   POST /api/auth/logout
 * GET   /api/auth/me         PATCH /api/auth/profile  PUT /api/auth/password
 *
 * Đi theo khuôn EmailListServlet (Chapter 12 slide 42-44): lấy dữ liệu từ
 * request, kiểm tra hợp lệ, rồi gọi lớp UserDB để đọc/ghi database.
 */
@WebServlet("/api/auth/*")
public class AuthServlet extends HttpServlet {
    private static final int HTTP_UNPROCESSABLE_ENTITY = 422;
    private static final int MAX_BODY = 8192;
    private static final int MYSQL_DUPLICATE_KEY = 1062;

    /* HttpServlet không có doPatch(), nên PATCH được tách ra trong service(). */
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if ("PATCH".equals(request.getMethod())) {
            updateProfile(request, response);
            return;
        }
        super.service(request, response);
    }

    /** GET /api/auth/me — người đang đăng nhập và CSRF token của phiên. */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!"/me".equals(request.getPathInfo())) {
            sendNotFound(response);
            return;
        }

        User user = SessionUtil.requireUser(request, response);
        if (user == null) return;

        HttpSession session = request.getSession(false);
        String csrfToken = (String) session.getAttribute("csrfToken");
        ResponseUtil.sendJson(response, HttpServletResponse.SC_OK,
                "{\"data\":{\"user\":" + user.toJson() + "},\"csrfToken\":"
                        + JsonUtil.quote(csrfToken) + "}");
    }

    /** POST /api/auth/register, /login hoặc /logout. */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String path = request.getPathInfo();

        if ("/register".equals(path)) {
            register(request, response);
        } else if ("/login".equals(path)) {
            login(request, response);
        } else if ("/logout".equals(path)) {
            logout(request, response);
        } else {
            sendNotFound(response);
        }
    }

    /** PUT /api/auth/password — đổi mật khẩu, bắt buộc nhập đúng mật khẩu hiện tại. */
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!"/password".equals(request.getPathInfo())) {
            sendNotFound(response);
            return;
        }

        User user = SessionUtil.requireUser(request, response);
        if (user == null || !SessionUtil.hasValidCsrfToken(request, response)) return;

        try {
            String body = JsonUtil.readBody(request.getReader(), MAX_BODY);
            String currentPassword = JsonUtil.stringField(body, "currentPassword");
            String newPassword = JsonUtil.stringField(body, "newPassword");

            // validate the parameters
            String existingHash = UserDB.selectPasswordHash(user.getId());
            if (!PasswordUtil.verifyPassword(currentPassword, existingHash)) {
                ResponseUtil.sendError(response, HttpServletResponse.SC_UNAUTHORIZED,
                        "INVALID_CREDENTIALS", "Mật khẩu hiện tại không đúng.");
                return;
            }
            if (!ValidationUtil.isValidPassword(newPassword)
                    || PasswordUtil.verifyPassword(newPassword, existingHash)) {
                ResponseUtil.sendError(response, HTTP_UNPROCESSABLE_ENTITY, "VALIDATION_ERROR",
                        "Mật khẩu mới phải có 8–72 ký tự và khác mật khẩu cũ.");
                return;
            }

            UserDB.updatePassword(user.getId(), PasswordUtil.hashPassword(newPassword));
            // session_version đã tăng nên phiên này cũng phải đăng nhập lại.
            request.getSession(false).invalidate();
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (IllegalArgumentException e) {
            sendInvalidInput(response);
        } catch (SQLException e) {
            ResponseUtil.sendDatabaseUnavailable(response);
        }
    }

    private void register(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            // get parameters from the request
            String body = JsonUtil.readBody(request.getReader(), MAX_BODY);
            String fullName = JsonUtil.stringField(body, "fullName");
            String email = ValidationUtil.normalizeEmail(JsonUtil.stringField(body, "email"));
            String password = JsonUtil.stringField(body, "password");

            // validate the parameters
            if (!ValidationUtil.isValidFullName(fullName)
                    || !ValidationUtil.isValidEmail(email)
                    || !ValidationUtil.isValidPassword(password)) {
                sendInvalidInput(response);
                return;
            }
            if (UserDB.emailExists(email)) {
                sendEmailTaken(response);
                return;
            }

            // store data in User object and save User object in db
            User user = new User();
            user.setFullName(fullName.trim());
            user.setEmail(email);
            UserDB.insert(user, PasswordUtil.hashPassword(password));

            user = UserDB.selectUser(email);
            SessionUtil.startSession(request, user);
            ResponseUtil.sendJson(response, HttpServletResponse.SC_CREATED,
                    "{\"data\":{\"user\":" + user.toJson() + "}}");
        } catch (IllegalArgumentException e) {
            sendInvalidInput(response);
        } catch (SQLException e) {
            // Hai người đăng ký cùng email cùng lúc: khóa UNIQUE của MySQL chặn người thứ hai.
            if (e.getErrorCode() == MYSQL_DUPLICATE_KEY) {
                sendEmailTaken(response);
            } else {
                ResponseUtil.sendDatabaseUnavailable(response);
            }
        }
    }

    /* Email không tồn tại và sai mật khẩu đều trả cùng một thông báo, để người
       ngoài không dò được email nào đã đăng ký. */
    private void login(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String body = JsonUtil.readBody(request.getReader(), MAX_BODY);
            String email = ValidationUtil.normalizeEmail(JsonUtil.stringField(body, "email"));
            String password = JsonUtil.stringField(body, "password");

            if (!ValidationUtil.isValidEmail(email) || !ValidationUtil.isValidPassword(password)) {
                sendInvalidInput(response);
                return;
            }

            String hash = UserDB.selectPasswordHash(email);
            User user = PasswordUtil.verifyPassword(password, hash) ? UserDB.selectUser(email) : null;
            if (user == null) {
                ResponseUtil.sendError(response, HttpServletResponse.SC_UNAUTHORIZED,
                        "INVALID_CREDENTIALS", "Email hoặc mật khẩu không đúng.");
                return;
            }

            SessionUtil.startSession(request, user);
            ResponseUtil.sendJson(response, HttpServletResponse.SC_OK,
                    "{\"data\":{\"user\":" + user.toJson() + "}}");
        } catch (IllegalArgumentException e) {
            sendInvalidInput(response);
        } catch (SQLException e) {
            ResponseUtil.sendDatabaseUnavailable(response);
        }
    }

    private void logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User user = SessionUtil.requireUser(request, response);
        if (user == null || !SessionUtil.hasValidCsrfToken(request, response)) return;

        request.getSession(false).invalidate();
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    /** PATCH /api/auth/profile — chỉ đổi họ tên; email và role không đổi được ở đây. */
    private void updateProfile(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!"/profile".equals(request.getPathInfo())) {
            sendNotFound(response);
            return;
        }

        User user = SessionUtil.requireUser(request, response);
        if (user == null || !SessionUtil.hasValidCsrfToken(request, response)) return;

        try {
            String fullName = JsonUtil.stringField(JsonUtil.readBody(request.getReader(), MAX_BODY), "fullName");
            if (!ValidationUtil.isValidFullName(fullName)) {
                ResponseUtil.sendError(response, HTTP_UNPROCESSABLE_ENTITY,
                        "VALIDATION_ERROR", "Họ tên phải có 2–100 ký tự.");
                return;
            }

            user.setFullName(fullName.trim());
            UserDB.update(user);
            User updated = UserDB.selectUser(user.getId());
            request.getSession(false).setAttribute("user", updated);
            ResponseUtil.sendJson(response, HttpServletResponse.SC_OK,
                    "{\"data\":{\"user\":" + updated.toJson() + "}}");
        } catch (IllegalArgumentException e) {
            ResponseUtil.sendError(response, HTTP_UNPROCESSABLE_ENTITY,
                    "VALIDATION_ERROR", "Họ tên phải có 2–100 ký tự.");
        } catch (SQLException e) {
            ResponseUtil.sendDatabaseUnavailable(response);
        }
    }

    private void sendInvalidInput(HttpServletResponse response) throws IOException {
        ResponseUtil.sendError(response, HTTP_UNPROCESSABLE_ENTITY, "VALIDATION_ERROR", "Dữ liệu không hợp lệ.");
    }

    private void sendEmailTaken(HttpServletResponse response) throws IOException {
        ResponseUtil.sendError(response, HttpServletResponse.SC_CONFLICT,
                "EMAIL_ALREADY_EXISTS", "Email đã được sử dụng.");
    }

    private void sendNotFound(HttpServletResponse response) throws IOException {
        ResponseUtil.sendError(response, HttpServletResponse.SC_NOT_FOUND,
                "NOT_FOUND", "Không tìm thấy tài nguyên yêu cầu.");
    }
}
