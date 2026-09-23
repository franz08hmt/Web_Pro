package vn.edu.webpro.robotlab.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import vn.edu.webpro.robotlab.business.Component;

/** Đọc/ghi bảng components — cùng khuôn với UserDB. */
public class ComponentDB {
    private static final String FIELDS = "id, name, category, image, description, specs";

    /* Cột specs có kiểu JSON trong MySQL nên tham số phải qua CAST(? AS JSON). */
    public static int insert(Component component) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement ps = null;

        String query = "INSERT INTO components (" + FIELDS + ") "
                + "VALUES (?, ?, ?, ?, ?, CAST(? AS JSON))";
        try {
            ps = connection.prepareStatement(query);
            ps.setString(1, component.getId());
            ps.setString(2, component.getName());
            ps.setString(3, component.getCategory());
            ps.setString(4, component.getImage());
            ps.setString(5, component.getDescription());
            ps.setString(6, component.getSpecsJson());
            return ps.executeUpdate();
        } finally {
            DBUtil.closePreparedStatement(ps);
            pool.freeConnection(connection);
        }
    }

    public static int update(Component component) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement ps = null;

        String query = "UPDATE components SET "
                + "name = ?, category = ?, image = ?, description = ?, specs = CAST(? AS JSON) "
                + "WHERE id = ?";
        try {
            ps = connection.prepareStatement(query);
            ps.setString(1, component.getName());
            ps.setString(2, component.getCategory());
            ps.setString(3, component.getImage());
            ps.setString(4, component.getDescription());
            ps.setString(5, component.getSpecsJson());
            ps.setString(6, component.getId());
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

        String query = "DELETE FROM components WHERE id = ?";
        try {
            ps = connection.prepareStatement(query);
            ps.setString(1, id);
            return ps.executeUpdate();
        } finally {
            DBUtil.closePreparedStatement(ps);
            pool.freeConnection(connection);
        }
    }

    public static List<Component> selectComponents(int limit, int offset) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        String query = "SELECT " + FIELDS + " FROM components ORDER BY id LIMIT ? OFFSET ?";
        try {
            ps = connection.prepareStatement(query);
            ps.setInt(1, limit);
            ps.setInt(2, offset);
            rs = ps.executeQuery();
            List<Component> components = new ArrayList<>();
            while (rs.next()) {
                Component component = new Component();
                component.setId(rs.getString("id"));
                component.setName(rs.getString("name"));
                component.setCategory(rs.getString("category"));
                component.setImage(rs.getString("image"));
                component.setDescription(rs.getString("description"));
                component.setSpecsJson(rs.getString("specs"));
                components.add(component);
            }
            return components;
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
            pool.freeConnection(connection);
        }
    }

    public static long countComponents() throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        String query = "SELECT COUNT(*) AS total FROM components";
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
}
