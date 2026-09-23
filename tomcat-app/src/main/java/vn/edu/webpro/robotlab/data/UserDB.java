package vn.edu.webpro.robotlab.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import vn.edu.webpro.robotlab.business.User;

/**
 * Đọc/ghi bảng users — cùng khuôn với lớp UserDB ở Chapter 12 slide 45-51.
 *
 * Mỗi method: lấy Connection từ ConnectionPool, chạy PreparedStatement với tham số
 * "?" (chống SQL injection, slide 17-20), rồi đóng tài nguyên trong finally.
 *
 * Khác slide có chủ đích: method khai báo throws SQLException thay vì in lỗi và
 * trả null/0, để servlet phân biệt được "không tìm thấy" với "database đang lỗi".
 */
public class UserDB {
    private static final String FIELDS =
            "id, display_name, email, role, created_at, session_version";

    /** Tài khoản đăng ký qua website luôn có role 'user'; không nhận role từ request. */
    public static int insert(User user, String passwordHash) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement ps = null;

        String query = "INSERT INTO users (display_name, email, password_hash, role) "
                + "VALUES (?, ?, ?, 'user')";
        try {
            ps = connection.prepareStatement(query);
            ps.setString(1, user.getFullName());
            ps.setString(2, user.getEmail());
            ps.setString(3, passwordHash);
            return ps.executeUpdate();
        } finally {
            DBUtil.closePreparedStatement(ps);
            pool.freeConnection(connection);
        }
    }

    /** Chỉ đổi họ tên; email và role không đổi được qua đường này. */
    public static int update(User user) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement ps = null;

        String query = "UPDATE users SET display_name = ? WHERE id = ?";
        try {
            ps = connection.prepareStatement(query);
            ps.setString(1, user.getFullName());
            ps.setLong(2, user.getId());
            return ps.executeUpdate();
        } finally {
            DBUtil.closePreparedStatement(ps);
            pool.freeConnection(connection);
        }
    }

    /** Đổi mật khẩu và tăng session_version để mọi phiên đăng nhập cũ hết hiệu lực. */
    public static int updatePassword(long id, String passwordHash) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement ps = null;

        String query = "UPDATE users SET password_hash = ?, "
                + "session_version = session_version + 1 WHERE id = ?";
        try {
            ps = connection.prepareStatement(query);
            ps.setString(1, passwordHash);
            ps.setLong(2, id);
            return ps.executeUpdate();
        } finally {
            DBUtil.closePreparedStatement(ps);
            pool.freeConnection(connection);
        }
    }

    /** Đổi quyền và tăng session_version để phiên cũ không giữ quyền cũ. */
    public static int updateRole(long id, String role) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement ps = null;

        String query = "UPDATE users SET role = ?, "
                + "session_version = session_version + 1 WHERE id = ?";
        try {
            ps = connection.prepareStatement(query);
            // Cột role là ENUM('user','admin') chữ thường.
            ps.setString(1, role.toLowerCase(Locale.ROOT));
            ps.setLong(2, id);
            return ps.executeUpdate();
        } finally {
            DBUtil.closePreparedStatement(ps);
            pool.freeConnection(connection);
        }
    }

    public static boolean emailExists(String email) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        String query = "SELECT email FROM users WHERE email = ?";
        try {
            ps = connection.prepareStatement(query);
            ps.setString(1, email);
            rs = ps.executeQuery();
            return rs.next();
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
            pool.freeConnection(connection);
        }
    }

    public static User selectUser(String email) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        String query = "SELECT " + FIELDS + " FROM users WHERE email = ?";
        try {
            ps = connection.prepareStatement(query);
            ps.setString(1, email);
            rs = ps.executeQuery();
            User user = null;
            if (rs.next()) {
                user = readUser(rs);
            }
            return user;
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
            pool.freeConnection(connection);
        }
    }

    public static User selectUser(long id) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        String query = "SELECT " + FIELDS + " FROM users WHERE id = ?";
        try {
            ps = connection.prepareStatement(query);
            ps.setLong(1, id);
            rs = ps.executeQuery();
            User user = null;
            if (rs.next()) {
                user = readUser(rs);
            }
            return user;
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
            pool.freeConnection(connection);
        }
    }

    /* Hash mật khẩu được đọc riêng, không nằm trong User, nên không có đường nào
       vô tình đưa hash ra JSON hay JSP. */
    public static String selectPasswordHash(String email) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        String query = "SELECT password_hash FROM users WHERE email = ?";
        try {
            ps = connection.prepareStatement(query);
            ps.setString(1, email);
            rs = ps.executeQuery();
            return rs.next() ? rs.getString("password_hash") : null;
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
            pool.freeConnection(connection);
        }
    }

    public static String selectPasswordHash(long id) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        String query = "SELECT password_hash FROM users WHERE id = ?";
        try {
            ps = connection.prepareStatement(query);
            ps.setLong(1, id);
            rs = ps.executeQuery();
            return rs.next() ? rs.getString("password_hash") : null;
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
            pool.freeConnection(connection);
        }
    }

    public static List<User> selectUsers(int limit, int offset) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        String query = "SELECT " + FIELDS + " FROM users ORDER BY id DESC LIMIT ? OFFSET ?";
        try {
            ps = connection.prepareStatement(query);
            ps.setInt(1, limit);
            ps.setInt(2, offset);
            rs = ps.executeQuery();
            List<User> users = new ArrayList<>();
            while (rs.next()) {
                users.add(readUser(rs));
            }
            return users;
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
            pool.freeConnection(connection);
        }
    }

    public static long countUsers() throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        String query = "SELECT COUNT(*) AS total FROM users";
        try {
            ps = connection.prepareStatement(query);
            rs = ps.executeQuery();
            rs.next();
            return rs.getLong("total");
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
            pool.freeConnection(connection);
        }
    }

    /** Tạo JavaBean User từ dòng hiện tại của ResultSet bằng các setter (slide 50). */
    private static User readUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setFullName(rs.getString("display_name"));
        user.setEmail(rs.getString("email"));
        // Cột role là chữ thường, còn tầng Java so sánh "USER"/"ADMIN".
        user.setRole(rs.getString("role").toUpperCase(Locale.ROOT));
        user.setCreatedAt(rs.getTimestamp("created_at").toInstant().toString());
        user.setSessionVersion(rs.getInt("session_version"));
        return user;
    }
}
