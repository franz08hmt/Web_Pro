package vn.edu.webpro.robotlab.service;

import java.sql.SQLException;
import java.util.Locale;
import vn.edu.webpro.robotlab.dao.UserDao;
import vn.edu.webpro.robotlab.model.User;

/** Đăng ký và đăng nhập; mật khẩu chỉ tồn tại dưới dạng hash sau bước này. */
public final class AuthService {
    private static final String EMAIL_PATTERN = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$";
    private static final int EMAIL_MAX = 254;
    private static final int NAME_MIN = 2;
    private static final int NAME_MAX = 100;
    private static final int PASSWORD_MIN = 8;
    private static final int PASSWORD_MAX = 72;

    private final UserDao users;
    private final PasswordService passwords;

    public AuthService(UserDao users, PasswordService passwords) {
        this.users = users;
        this.passwords = passwords;
    }

    public User register(String fullName, String email, String password) throws SQLException {
        email = normalizeEmail(email);
        validateName(fullName);
        validateCredentials(email, password);
        if (users.findByEmail(email) != null) throw new IllegalStateException("exists");
        return users.create(fullName.trim(), email, passwords.hash(password));
    }

    /* Email không tồn tại và mật khẩu sai đều ném cùng một lỗi "credentials",
       để phía ngoài không suy ra được email nào đã đăng ký trong hệ thống. */
    public User login(String email, String password) throws SQLException {
        email = normalizeEmail(email);
        validateCredentials(email, password);
        String hash = users.passwordHash(email);
        if (hash == null || !passwords.verify(password, hash)) {
            throw new IllegalArgumentException("credentials");
        }
        User user = users.findByEmail(email);
        if (user == null) throw new IllegalArgumentException("credentials");
        return user;
    }

    public User updateProfile(long userId, String fullName) throws SQLException {
        validateName(fullName);
        User updated = users.updateName(userId, fullName.trim());
        if (updated == null) throw new IllegalArgumentException("not-found");
        return updated;
    }

    public void changePassword(long userId, String currentPassword, String newPassword) throws SQLException {
        validatePassword(newPassword);
        String existingHash = users.passwordHashById(userId);
        if (existingHash == null || currentPassword == null || !passwords.verify(currentPassword, existingHash)) {
            throw new IllegalArgumentException("credentials");
        }
        if (passwords.verify(newPassword, existingHash)) throw new IllegalArgumentException("same-password");
        users.updatePasswordHash(userId, passwords.hash(newPassword));
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    private void validateName(String name) {
        if (name == null || name.trim().length() < NAME_MIN || name.trim().length() > NAME_MAX) {
            throw new IllegalArgumentException("validation");
        }
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < PASSWORD_MIN || password.length() > PASSWORD_MAX) {
            throw new IllegalArgumentException("validation");
        }
    }

    private void validateCredentials(String email, String password) {
        if (email == null || email.length() > EMAIL_MAX || !email.matches(EMAIL_PATTERN)) {
            throw new IllegalArgumentException("validation");
        }
        validatePassword(password);
    }
}
