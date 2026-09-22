package vn.edu.webpro.robotlab.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import vn.edu.webpro.robotlab.model.User;

/** Truy cập bảng users bằng JDBC; mật khẩu chỉ đọc dưới dạng hash. */
public final class UserDao {
    private final DatabaseConnectionFactory connections;

    public UserDao(DatabaseConnectionFactory connections) {
        this.connections = connections;
    }

    public User findByEmail(String email) throws SQLException {
        try (Connection connection = connections.openConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT id, display_name, email, role FROM users WHERE email = ?")) {
            statement.setString(1, email);

            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() ? map(rows) : null;
            }
        }
    }

    /* Tách riêng khỏi findByEmail để hash không bao giờ lọt vào model User,
       nhờ đó không có đường nào vô tình trả hash ra JSON. */
    public String passwordHash(String email) throws SQLException {
        try (Connection connection = connections.openConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT password_hash FROM users WHERE email = ?")) {
            statement.setString(1, email);

            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() ? rows.getString(1) : null;
            }
        }
    }

    /** Tài khoản đăng ký qua website luôn có role 'user'; nâng quyền làm thủ công. */
    public User create(String fullName, String email, String passwordHash) throws SQLException {
        String sql = "INSERT INTO users (display_name, email, password_hash, role)"
                + " VALUES (?, ?, ?, 'user')";

        try (Connection connection = connections.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, fullName);
            statement.setString(2, email);
            statement.setString(3, passwordHash);
            statement.executeUpdate();
        }
        return findByEmail(email);
    }

    /* Cột role là ENUM('user','admin') chữ thường, còn tầng Java so sánh
       "ADMIN"/"USER" chữ hoa, nên chuẩn hóa ngay khi đọc. */
    private User map(ResultSet rows) throws SQLException {
        return new User(
                rows.getLong("id"),
                rows.getString("display_name"),
                rows.getString("email"),
                rows.getString("role").toUpperCase()
        );
    }
}
