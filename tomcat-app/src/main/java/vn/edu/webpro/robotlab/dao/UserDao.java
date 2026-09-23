package vn.edu.webpro.robotlab.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import vn.edu.webpro.robotlab.model.User;

/** Truy cập bảng users bằng JDBC; mật khẩu chỉ đọc dưới dạng hash. */
public class UserDao {
    private static final String FIELDS = "id, display_name, email, role, created_at, session_version";
    private final DatabaseConnectionFactory connections;

    public UserDao(DatabaseConnectionFactory connections) {
        this.connections = connections;
    }

    public User findByEmail(String email) throws SQLException {
        try (Connection connection = connections.openConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT " + FIELDS + " FROM users WHERE email = ?")) {
            statement.setString(1, email);

            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() ? map(rows) : null;
            }
        }
    }

    public User findById(long id) throws SQLException {
        try (Connection connection = connections.openConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT " + FIELDS + " FROM users WHERE id = ?")) {
            statement.setLong(1, id);
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

    public String passwordHashById(long id) throws SQLException {
        try (Connection connection = connections.openConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT password_hash FROM users WHERE id = ?")) {
            statement.setLong(1, id);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() ? rows.getString(1) : null;
            }
        }
    }

    public User updateName(long id, String name) throws SQLException {
        try (Connection connection = connections.openConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE users SET display_name = ? WHERE id = ?")) {
            statement.setString(1, name);
            statement.setLong(2, id);
            if (statement.executeUpdate() == 0) return null;
        }
        return findById(id);
    }

    public void updatePasswordHash(long id, String hash) throws SQLException {
        try (Connection connection = connections.openConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE users SET password_hash = ?, session_version = session_version + 1 WHERE id = ?")) {
            statement.setString(1, hash);
            statement.setLong(2, id);
            if (statement.executeUpdate() == 0) throw new IllegalArgumentException("not-found");
        }
    }

    public User updateRole(long id, String role) throws SQLException {
        try (Connection connection = connections.openConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE users SET role = ?, session_version = session_version + 1 WHERE id = ?")) {
            statement.setString(1, role.toLowerCase(java.util.Locale.ROOT));
            statement.setLong(2, id);
            if (statement.executeUpdate() == 0) return null;
        }
        return findById(id);
    }

    public java.util.List<User> list(int limit, int offset) throws SQLException {
        try (Connection connection = connections.openConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT " + FIELDS + " FROM users ORDER BY id DESC LIMIT ? OFFSET ?")) {
            statement.setInt(1, limit);
            statement.setInt(2, offset);
            try (ResultSet rows = statement.executeQuery()) {
                java.util.List<User> result = new java.util.ArrayList<>();
                while (rows.next()) result.add(map(rows));
                return result;
            }
        }
    }

    public long count() throws SQLException {
        try (Connection connection = connections.openConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM users");
             ResultSet rows = statement.executeQuery()) {
            rows.next();
            return rows.getLong(1);
        }
    }

    /** Tài khoản đăng ký qua website luôn có role 'user'; chỉ admin mới đổi qua API riêng. */
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
                rows.getString("role").toUpperCase(java.util.Locale.ROOT),
                rows.getTimestamp("created_at").toInstant().toString(),
                rows.getInt("session_version")
        );
    }
}
