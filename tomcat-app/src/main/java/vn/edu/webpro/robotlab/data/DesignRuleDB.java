package vn.edu.webpro.robotlab.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import vn.edu.webpro.robotlab.business.DesignRuleSet;
import vn.edu.webpro.robotlab.business.DesignComponentRole;
import vn.edu.webpro.robotlab.business.DesignMount;
import vn.edu.webpro.robotlab.business.DesignFit;
import vn.edu.webpro.robotlab.business.DesignGoal;
import vn.edu.webpro.robotlab.business.DesignRequirement;

/** Đọc catalog luật bất biến bằng số truy vấn cố định; không truy vấn từng linh kiện. */
public class DesignRuleDB {
    /** Phiên bản công bố mới nhất; thiết kế lưu id này ngay lúc tạo. */
    public static DesignRuleSet selectPublished() throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = connection.prepareStatement("SELECT id FROM design_rule_sets WHERE state = 'PUBLISHED' "
                    + "ORDER BY version_no DESC LIMIT 1");
            rs = ps.executeQuery();
            if (!rs.next()) {
                throw new SQLException("No published design rules");
            }
            return read(connection, rs.getLong(1));
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
            pool.freeConnection(connection);
        }
    }

    /** Dùng kết nối transaction của thiết kế, không chọn lại phiên bản mới nhất. */
    public static DesignRuleSet read(Connection connection, long id) throws SQLException {
        DesignRuleSet rules = new DesignRuleSet();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = connection.prepareStatement("SELECT id,version_no,state FROM design_rule_sets WHERE id = ?");
            ps.setLong(1, id);
            rs = ps.executeQuery();
            if (!rs.next()) {
                throw new SQLException("Missing design rule version");
            }
            rules.setId(id);
            rules.setVersionNo(rs.getInt("version_no"));
            rules.setState(rs.getString("state"));
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
            ps = connection.prepareStatement("SELECT component_id,role,driver_channels "
                    + "FROM design_component_roles WHERE rule_set_id = ?");
            ps.setLong(1, id);
            rs = ps.executeQuery();
            while (rs.next()) {
                DesignComponentRole role = new DesignComponentRole();
                role.setComponentId(rs.getString(1));
                role.setRole(rs.getString(2));
                role.setDriverChannels(rs.getInt(3));
                rules.getRoles().add(role);
            }
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
            ps = connection.prepareStatement("SELECT * FROM design_mounts WHERE rule_set_id = ? "
                    + "ORDER BY display_order");
            ps.setLong(1, id);
            rs = ps.executeQuery();
            while (rs.next()) {
                DesignMount mount = new DesignMount();
                mount.setComponentId(rs.getString("component_id"));
                mount.setMountCode(rs.getString("mount_code"));
                mount.setMountType(rs.getString("mount_type"));
                mount.setLabel(rs.getString("label"));
                mount.setX(rs.getDouble("x"));
                mount.setY(rs.getDouble("y"));
                mount.setZ(rs.getDouble("z"));
                mount.setRx(rs.getDouble("rx"));
                mount.setRy(rs.getDouble("ry"));
                mount.setRz(rs.getDouble("rz"));
                mount.setDisplayOrder(rs.getInt("display_order"));
                rules.getMounts().add(mount);
            }
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
            ps = connection.prepareStatement("SELECT component_id,mount_type,orientation_code "
                    + "FROM design_fits WHERE rule_set_id = ? ORDER BY component_id,orientation_code");
            ps.setLong(1, id);
            rs = ps.executeQuery();
            while (rs.next()) {
                DesignFit fit = new DesignFit();
                fit.setComponentId(rs.getString(1));
                fit.setMountType(rs.getString(2));
                fit.setOrientationCode(rs.getString(3));
                rules.getFits().add(fit);
            }
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
            ps = connection.prepareStatement("SELECT DISTINCT g.code,g.name,g.description FROM design_goals g "
                    + "JOIN design_goal_requirements r ON r.goal_code = g.code WHERE r.rule_set_id = ? "
                    + "ORDER BY g.code");
            ps.setLong(1, id);
            rs = ps.executeQuery();
            while (rs.next()) {
                DesignGoal goal = new DesignGoal();
                goal.setCode(rs.getString(1));
                goal.setName(rs.getString(2));
                goal.setDescription(rs.getString(3));
                rules.getGoals().add(goal);
            }
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
            ps = connection.prepareStatement("SELECT goal_code,role,min_count,message "
                    + "FROM design_goal_requirements WHERE rule_set_id = ? ORDER BY goal_code,role");
            ps.setLong(1, id);
            rs = ps.executeQuery();
            while (rs.next()) {
                DesignRequirement requirement = new DesignRequirement();
                requirement.setGoalCode(rs.getString(1));
                requirement.setRole(rs.getString(2));
                requirement.setMinCount(rs.getInt(3));
                requirement.setMessage(rs.getString(4));
                rules.getRequirements().add(requirement);
            }
            return rules;
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
        }
    }
}
