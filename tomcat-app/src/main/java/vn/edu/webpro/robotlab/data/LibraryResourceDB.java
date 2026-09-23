package vn.edu.webpro.robotlab.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import vn.edu.webpro.robotlab.business.LibraryResource;

/** Đọc/ghi bảng library_resources — cùng khuôn với UserDB. */
public class LibraryResourceDB {
    private static final String FIELDS = "id, robot_id, title, type, url, description";

    public static int insert(LibraryResource resource) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement ps = null;

        String query = "INSERT INTO library_resources (" + FIELDS + ") "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try {
            ps = connection.prepareStatement(query);
            ps.setString(1, resource.getId());
            ps.setString(2, resource.getRobotId());
            ps.setString(3, resource.getTitle());
            ps.setString(4, resource.getType());
            ps.setString(5, resource.getUrl());
            ps.setString(6, resource.getDescription());
            return ps.executeUpdate();
        } finally {
            DBUtil.closePreparedStatement(ps);
            pool.freeConnection(connection);
        }
    }

    public static int update(LibraryResource resource) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement ps = null;

        String query = "UPDATE library_resources SET "
                + "robot_id = ?, title = ?, type = ?, url = ?, description = ? "
                + "WHERE id = ?";
        try {
            ps = connection.prepareStatement(query);
            ps.setString(1, resource.getRobotId());
            ps.setString(2, resource.getTitle());
            ps.setString(3, resource.getType());
            ps.setString(4, resource.getUrl());
            ps.setString(5, resource.getDescription());
            ps.setString(6, resource.getId());
            return ps.executeUpdate();
        } finally {
            DBUtil.closePreparedStatement(ps);
            pool.freeConnection(connection);
        }
    }

    public static int delete(String id) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement ps = null;

        String query = "DELETE FROM library_resources WHERE id = ?";
        try {
            ps = connection.prepareStatement(query);
            ps.setString(1, id);
            return ps.executeUpdate();
        } finally {
            DBUtil.closePreparedStatement(ps);
            pool.freeConnection(connection);
        }
    }

    /**
     * robotId null nghĩa là lấy tất cả. Chỉ đoạn " WHERE robot_id = ?" cố định được
     * ghép thêm vào câu lệnh; giá trị robotId vẫn đi qua tham số "?".
     */
    public static List<LibraryResource> selectResources(int limit, int offset, String robotId)
            throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        String query = "SELECT " + FIELDS + " FROM library_resources"
                + (robotId == null ? "" : " WHERE robot_id = ?")
                + " ORDER BY id LIMIT ? OFFSET ?";
        try {
            ps = connection.prepareStatement(query);
            int index = 1;
            if (robotId != null) {
                ps.setString(index++, robotId);
            }
            ps.setInt(index++, limit);
            ps.setInt(index, offset);
            rs = ps.executeQuery();
            List<LibraryResource> resources = new ArrayList<>();
            while (rs.next()) {
                LibraryResource resource = new LibraryResource();
                resource.setId(rs.getString("id"));
                resource.setRobotId(rs.getString("robot_id"));
                resource.setTitle(rs.getString("title"));
                resource.setType(rs.getString("type"));
                resource.setUrl(rs.getString("url"));
                resource.setDescription(rs.getString("description"));
                resources.add(resource);
            }
            return resources;
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
            pool.freeConnection(connection);
        }
    }

    public static long countResources(String robotId) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        String query = "SELECT COUNT(*) AS total FROM library_resources"
                + (robotId == null ? "" : " WHERE robot_id = ?");
        try {
            ps = connection.prepareStatement(query);
            if (robotId != null) {
                ps.setString(1, robotId);
            }
            rs = ps.executeQuery();
            rs.next();
            return rs.getLong("total");
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
            pool.freeConnection(connection);
        }
    }
}
