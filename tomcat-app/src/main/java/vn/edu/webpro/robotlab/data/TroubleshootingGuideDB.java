package vn.edu.webpro.robotlab.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import vn.edu.webpro.robotlab.business.TroubleshootingGuide;

/** Đọc/ghi bảng troubleshooting_guides — cùng khuôn với LibraryResourceDB. */
public class TroubleshootingGuideDB {
    private static final String FIELDS = "id, robot_id, component_group, symptom, "
            + "possible_causes, resolution_steps, related_component_id, display_order";

    public static int insert(TroubleshootingGuide guide) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement ps = null;

        String query = "INSERT INTO troubleshooting_guides (" + FIELDS + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            ps = connection.prepareStatement(query);
            bindGuide(ps, guide);
            return ps.executeUpdate();
        } finally {
            DBUtil.closePreparedStatement(ps);
            pool.freeConnection(connection);
        }
    }

    public static int update(TroubleshootingGuide guide) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement ps = null;

        String query = "UPDATE troubleshooting_guides SET robot_id = ?, component_group = ?, symptom = ?, "
                + "possible_causes = ?, resolution_steps = ?, related_component_id = ?, display_order = ? "
                + "WHERE id = ?";
        try {
            ps = connection.prepareStatement(query);
            ps.setString(1, guide.getRobotId());
            ps.setString(2, guide.getComponentGroup());
            ps.setString(3, guide.getSymptom());
            ps.setString(4, guide.getPossibleCauses());
            ps.setString(5, guide.getResolutionSteps());
            ps.setString(6, guide.getRelatedComponentId());
            ps.setInt(7, guide.getDisplayOrder());
            ps.setString(8, guide.getId());
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

        try {
            ps = connection.prepareStatement("DELETE FROM troubleshooting_guides WHERE id = ?");
            ps.setString(1, id);
            return ps.executeUpdate();
        } finally {
            DBUtil.closePreparedStatement(ps);
            pool.freeConnection(connection);
        }
    }

    public static TroubleshootingGuide selectGuide(String id) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = connection.prepareStatement("SELECT " + FIELDS + " FROM troubleshooting_guides WHERE id = ?");
            ps.setString(1, id);
            rs = ps.executeQuery();
            return rs.next() ? readGuide(rs) : null;
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
            pool.freeConnection(connection);
        }
    }

    /**
     * Tìm/lọc tình huống theo robot, nhóm linh kiện và từ khóa trong triệu chứng.
     * robotId khớp cả các tình huống dùng chung (robot_id NULL) vì chúng cũng áp
     * dụng cho mọi robot. Mọi tham số lọc đều tùy chọn (truyền null để bỏ qua).
     */
    public static List<TroubleshootingGuide> selectGuides(String robotId, String componentGroup,
            String search, int limit, int offset) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        StringBuilder query = new StringBuilder("SELECT " + FIELDS + " FROM troubleshooting_guides WHERE 1 = 1");
        if (robotId != null) query.append(" AND (robot_id = ? OR robot_id IS NULL)");
        if (componentGroup != null) query.append(" AND component_group = ?");
        if (search != null) query.append(" AND symptom LIKE ?");
        query.append(" ORDER BY display_order, id LIMIT ? OFFSET ?");

        try {
            ps = connection.prepareStatement(query.toString());
            int index = 1;
            if (robotId != null) ps.setString(index++, robotId);
            if (componentGroup != null) ps.setString(index++, componentGroup);
            if (search != null) ps.setString(index++, "%" + search + "%");
            ps.setInt(index++, limit);
            ps.setInt(index, offset);
            rs = ps.executeQuery();
            List<TroubleshootingGuide> guides = new ArrayList<>();
            while (rs.next()) {
                guides.add(readGuide(rs));
            }
            return guides;
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
            pool.freeConnection(connection);
        }
    }

    public static long countGuides(String robotId, String componentGroup, String search) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        StringBuilder query = new StringBuilder("SELECT COUNT(*) AS total FROM troubleshooting_guides WHERE 1 = 1");
        if (robotId != null) query.append(" AND (robot_id = ? OR robot_id IS NULL)");
        if (componentGroup != null) query.append(" AND component_group = ?");
        if (search != null) query.append(" AND symptom LIKE ?");

        try {
            ps = connection.prepareStatement(query.toString());
            int index = 1;
            if (robotId != null) ps.setString(index++, robotId);
            if (componentGroup != null) ps.setString(index++, componentGroup);
            if (search != null) ps.setString(index, "%" + search + "%");
            rs = ps.executeQuery();
            rs.next();
            return rs.getLong("total");
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
            pool.freeConnection(connection);
        }
    }

    /** Danh sách nhóm linh kiện đang có, dùng để dựng bộ lọc trên trang tra cứu. */
    public static List<String> selectComponentGroups() throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = connection.prepareStatement(
                    "SELECT DISTINCT component_group FROM troubleshooting_guides ORDER BY component_group");
            rs = ps.executeQuery();
            List<String> groups = new ArrayList<>();
            while (rs.next()) {
                groups.add(rs.getString("component_group"));
            }
            return groups;
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
            pool.freeConnection(connection);
        }
    }

    private static void bindGuide(PreparedStatement ps, TroubleshootingGuide guide) throws SQLException {
        ps.setString(1, guide.getId());
        ps.setString(2, guide.getRobotId());
        ps.setString(3, guide.getComponentGroup());
        ps.setString(4, guide.getSymptom());
        ps.setString(5, guide.getPossibleCauses());
        ps.setString(6, guide.getResolutionSteps());
        ps.setString(7, guide.getRelatedComponentId());
        ps.setInt(8, guide.getDisplayOrder());
    }

    private static TroubleshootingGuide readGuide(ResultSet rs) throws SQLException {
        TroubleshootingGuide guide = new TroubleshootingGuide();
        guide.setId(rs.getString("id"));
        guide.setRobotId(rs.getString("robot_id"));
        guide.setComponentGroup(rs.getString("component_group"));
        guide.setSymptom(rs.getString("symptom"));
        guide.setPossibleCauses(rs.getString("possible_causes"));
        guide.setResolutionSteps(rs.getString("resolution_steps"));
        guide.setRelatedComponentId(rs.getString("related_component_id"));
        guide.setDisplayOrder(rs.getInt("display_order"));
        return guide;
    }
}
