package vn.edu.webpro.robotlab.service;

import java.sql.SQLException;
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
        validate(fullName, email, password);
        if (users.findByEmail(email) != null) throw new IllegalStateException("exists");
        return users.create(fullName.trim(), email, passwords.hash(password));
    }

    /* Email không tồn tại và mật khẩu sai đều ném cùng một lỗi "credentials",
       để phía ngoài không suy ra được email nào đã đăng ký trong hệ thống. */
    public User login(String email, String password) throws SQLException {
        validate(null, email, password);
        String hash = users.passwordHash(email);
        if (hash == null || !passwords.verify(password, hash)) {
            throw new IllegalArgumentException("credentials");
        }
        return users.findByEmail(email);
    }

    /** fullName để null khi đăng nhập, vì lúc đó không cần kiểm tra họ tên. */
    private void validate(String fullName, String email, String password) {
        if (fullName != null) {
            int length = fullName.trim().length();
            if (length < NAME_MIN || length > NAME_MAX) {
                throw new IllegalArgumentException("validation");
            }
        }
        if (email == null || email.length() > EMAIL_MAX || !email.matches(EMAIL_PATTERN)) {
            throw new IllegalArgumentException("validation");
        }
        if (password == null || password.length() < PASSWORD_MIN || password.length() > PASSWORD_MAX) {
            throw new IllegalArgumentException("validation");
        }
    }
}
