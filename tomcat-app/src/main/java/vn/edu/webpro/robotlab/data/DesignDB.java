package vn.edu.webpro.robotlab.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import vn.edu.webpro.robotlab.business.RobotDesign;
import vn.edu.webpro.robotlab.business.DesignInstance;
import vn.edu.webpro.robotlab.business.DesignRuleSet;
import vn.edu.webpro.robotlab.util.DesignException;

/** Quản lý thiết kế riêng theo owner; không chạm các phiên lắp theo mẫu. */
public class DesignDB {
    /** Danh sách chỉ của tài khoản trong HttpSession. */
    public static List<RobotDesign> selectDesigns(long userId) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = connection.prepareStatement("SELECT d.*,g.name AS goal_name FROM robot_designs d "
                    + "JOIN design_goals g ON g.code = d.goal_code WHERE d.user_id = ? ORDER BY d.id DESC");
            ps.setLong(1, userId);
            rs = ps.executeQuery();
            List<RobotDesign> designs = new ArrayList<>();
            while (rs.next()) {
                designs.add(fromRow(rs));
            }
            return designs;
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
            pool.freeConnection(connection);
        }
    }

    /** Đọc một thiết kế; trả null cả khi id thuộc người khác. */
    public static RobotDesign selectDesign(long id, long userId) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        try {
            return read(connection, id, userId, false);
        } finally {
            pool.freeConnection(connection);
        }
    }

    /** Dùng kết nối transaction cho khóa bố cục hoặc metadata. */
    public static RobotDesign read(Connection connection, long id, long userId, boolean lock) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String query = "SELECT d.*,g.name AS goal_name FROM robot_designs d "
                    + "JOIN design_goals g ON g.code = d.goal_code WHERE d.id = ? AND d.user_id = ?";
            if (lock) {
                query += " FOR UPDATE";
            }
            ps = connection.prepareStatement(query);
            ps.setLong(1, id);
            ps.setLong(2, userId);
            rs = ps.executeQuery();
            return rs.next() ? fromRow(rs) : null;
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
        }
    }

    /** Snapshot vật thể đọc bằng một JOIN, gồm số lượng cùng loại. */
    public static List<DesignInstance> readInstances(Connection connection, long id, long userId)
            throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = connection.prepareStatement("SELECT i.*,c.name,(SELECT COUNT(*) FROM design_instances n "
                    + "WHERE n.design_id = i.design_id AND n.component_id = i.component_id) AS quantity "
                    + "FROM design_instances i JOIN robot_designs d ON d.id = i.design_id "
                    + "JOIN components c ON c.id = i.component_id WHERE d.id = ? AND d.user_id = ? ORDER BY i.id");
            ps.setLong(1, id);
            ps.setLong(2, userId);
            rs = ps.executeQuery();
            List<DesignInstance> instances = new ArrayList<>();
            while (rs.next()) {
                DesignInstance item = new DesignInstance();
                item.setId(rs.getLong("id"));
                item.setDesignId(id);
                item.setComponentId(rs.getString("component_id"));
                item.setInstanceNo(rs.getInt("instance_no"));
                item.setQuantity(rs.getInt("quantity"));
                item.setName(rs.getString("name"));
                instances.add(item);
            }
            return instances;
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
        }
    }

    /** Tạo header và snapshot số lượng nguyên tử, giới hạn dưới khóa owner. */
    public static long create(long userId, RobotDesign draft, HashMap<String, Integer> quantities)
            throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        boolean original = true;
        boolean started = false;
        try {
            original = connection.getAutoCommit();
            connection.setAutoCommit(false);
            started = true;
            UserKitDB.lockOwner(connection, userId);
            ps = connection.prepareStatement("SELECT id FROM design_rule_sets WHERE state = 'PUBLISHED' "
                    + "ORDER BY version_no DESC LIMIT 1");
            rs = ps.executeQuery();
            if (!rs.next()) {
                throw new SQLException("No design rules available");
            }
            long ruleId = rs.getLong(1);
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
            ps = connection.prepareStatement("SELECT COUNT(*) FROM robot_designs WHERE user_id = ?");
            ps.setLong(1, userId);
            rs = ps.executeQuery();
            rs.next();
            int count = rs.getInt(1);
            DesignRuleSet rules = DesignRuleDB.read(connection, ruleId);
            String error = draft.validateCreation(rules, UserKitDB.readItems(connection, userId, rules),
                    quantities, count);
            if (!error.isEmpty()) {
                throw new IllegalArgumentException(error);
            }
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
            ps = connection.prepareStatement("INSERT INTO robot_designs "
                    + "(user_id,name,goal_code,rule_set_id,source) VALUES (?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, userId);
            ps.setString(2, draft.getName().trim());
            ps.setString(3, draft.getGoalCode());
            ps.setLong(4, ruleId);
            ps.setString(5, draft.getSource());
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            rs.next();
            long id = rs.getLong(1);
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
            ps = connection.prepareStatement("INSERT INTO design_instances "
                    + "(design_id,component_id,instance_no) VALUES (?,?,?)");
            for (String component : quantities.keySet()) {
                for (int n = 1; n <= quantities.get(component); n++) {
                    ps.setLong(1, id);
                    ps.setString(2, component);
                    ps.setInt(3, n);
                    ps.executeUpdate();
                }
            }
            connection.commit();
            return id;
        } catch (SQLException | IllegalArgumentException e) {
            if (started) {
                try {
                    connection.rollback();
                } catch (SQLException rollback) {
                    e.addSuppressed(rollback);
                }
            }
            throw e;
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
            try {
                if (started) {
                    connection.setAutoCommit(original);
                }
            } finally {
                pool.freeConnection(connection);
            }
        }
    }

    /** Đổi tên/xóa chỉ owner, có khóa và version; xóa con trước FK RESTRICT. */
    public static void changeMeta(long id, long userId, String name, boolean delete, int version) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement ps = null;
        boolean original = true;
        boolean started = false;
        try {
            original = connection.getAutoCommit();
            connection.setAutoCommit(false);
            started = true;
            UserKitDB.lockOwner(connection, userId);
            RobotDesign design = read(connection, id, userId, true);
            if (design == null) {
                throw new DesignException("NOT_FOUND", "Không tìm thấy thiết kế bạn được phép dùng.");
            }
            if (version != design.getLayoutVersion()) {
                throw new DesignException("LAYOUT_CONFLICT", "Thiết kế đã thay đổi ở tab khác. Hãy tải lại.");
            }
            if (delete) {
                ps = connection.prepareStatement("DELETE p FROM design_placements p "
                        + "JOIN robot_designs d ON d.id = p.design_id WHERE d.id = ? AND d.user_id = ?");
                ps.setLong(1, id);
                ps.setLong(2, userId);
                ps.executeUpdate();
                DBUtil.closePreparedStatement(ps);
                ps = connection.prepareStatement("DELETE FROM robot_designs WHERE id = ? AND user_id = ?");
                ps.setLong(1, id);
                ps.setLong(2, userId);
            } else {
                design.setName(name);
                if (!design.validateName().isEmpty()) {
                    throw new IllegalArgumentException(design.validateName());
                }
                ps = connection.prepareStatement("UPDATE robot_designs SET name = ?,"
                        + "layout_version = layout_version + 1,updated_at = CURRENT_TIMESTAMP "
                        + "WHERE id = ? AND user_id = ?");
                ps.setString(1, name.trim());
                ps.setLong(2, id);
                ps.setLong(3, userId);
            }
            ps.executeUpdate();
            connection.commit();
        } catch (SQLException | IllegalArgumentException e) {
            if (started) {
                try {
                    connection.rollback();
                } catch (SQLException rollback) {
                    e.addSuppressed(rollback);
                }
            }
            throw e;
        } finally {
            DBUtil.closePreparedStatement(ps);
            try {
                if (started) {
                    connection.setAutoCommit(original);
                }
            } finally {
                pool.freeConnection(connection);
            }
        }
    }

    private static RobotDesign fromRow(ResultSet rs) throws SQLException {
        RobotDesign design = new RobotDesign();
        design.setId(rs.getLong("id"));
        design.setUserId(rs.getLong("user_id"));
        design.setRuleSetId(rs.getLong("rule_set_id"));
        design.setName(rs.getString("name"));
        design.setGoalCode(rs.getString("goal_code"));
        design.setGoalName(rs.getString("goal_name"));
        design.setSource(rs.getString("source"));
        design.setStatus(rs.getString("status"));
        design.setLayoutVersion(rs.getInt("layout_version"));
        design.setLastCheckSummary(rs.getString("last_check_summary"));
        design.setLastCheckedAt(rs.getTimestamp("last_checked_at"));
        design.setCreatedAt(rs.getTimestamp("created_at"));
        design.setUpdatedAt(rs.getTimestamp("updated_at"));
        return design;
    }
}
