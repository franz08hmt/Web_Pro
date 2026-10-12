package vn.edu.webpro.robotlab.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import vn.edu.webpro.robotlab.business.AssemblyLayout;
import vn.edu.webpro.robotlab.business.AssemblySpec;
import vn.edu.webpro.robotlab.business.AssemblySlot;
import vn.edu.webpro.robotlab.business.AssemblyInstance;
import vn.edu.webpro.robotlab.business.AssemblyPlacement;
import vn.edu.webpro.robotlab.business.RobotComponent;

/** JDBC cho vật thể riêng; mọi thao tác khóa phiên trước khi gọi luật bean. */
public class AssemblyPlacementDB {
    /** Tạo phiên và kit trong cùng transaction; không chuyển đổi phiên cũ. */
    public static long createSession(long userId, String robotId) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        boolean original = true;
        boolean started = false;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            original = connection.getAutoCommit();
            connection.setAutoCommit(false);
            started = true;
            ps = connection.prepareStatement("SELECT id FROM assembly_specs WHERE robot_id = ? "
                    + "AND state = 'PUBLISHED' ORDER BY version_no DESC LIMIT 1 FOR UPDATE");
            ps.setString(1, robotId);
            rs = ps.executeQuery();
            long specId = rs.next() ? rs.getLong(1) : 0;
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
            ps = connection.prepareStatement("INSERT INTO assembly_sessions "
                    + "(user_id, robot_id, status, assembly_mode, spec_id) VALUES (?, ?, 'PREPARING', ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, userId);
            ps.setString(2, robotId);
            ps.setString(3, specId == 0 ? "GROUP" : "DETAILED");
            if (specId == 0) {
                ps.setNull(4, java.sql.Types.BIGINT);
            } else {
                ps.setLong(4, specId);
            }
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            rs.next();
            long id = rs.getLong(1);
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
            rs = null;
            ps = null;
            if (specId > 0) {
                AssemblyLayout layout = new AssemblyLayout();
                layout.setSessionId(id);
                layout.setSpec(readSpec(connection, specId));
                if (!layout.getSpec().validate().isEmpty()) {
                    throw new IllegalArgumentException("INVALID_SPEC");
                }
                layout.createInstances(readKit(connection, robotId));
                if (!layout.validateKit().isEmpty()) {
                    throw new IllegalArgumentException("KIT_SPEC_MISMATCH");
                }
                ps = connection.prepareStatement("INSERT INTO assembly_instances "
                        + "(session_id, component_id, instance_no) VALUES (?, ?, ?)");
                for (AssemblyInstance item : layout.getInstances()) {
                    ps.setLong(1, id);
                    ps.setString(2, item.getComponentId());
                    ps.setInt(3, item.getInstanceNo());
                    ps.executeUpdate();
                }
            }
            connection.commit();
            return id;
        } catch (SQLException | IllegalArgumentException e) {
            if (started) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
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

    /** Đọc snapshot nhất quán; SELECT chỉ lấy phiên thuộc user của session. */
    public static AssemblyLayout selectLayout(long id, long userId) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        boolean original = true;
        boolean started = false;
        try {
            original = connection.getAutoCommit();
            connection.setAutoCommit(false);
            started = true;
            AssemblyLayout layout = load(connection, id, userId, false);
            connection.commit();
            return layout;
        } catch (SQLException e) {
            if (started) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }
            }
            throw e;
        } finally {
            try {
                if (started) {
                    connection.setAutoCommit(original);
                }
            } finally {
                pool.freeConnection(connection);
            }
        }
    }

    /** Gắn/tháo/reset/hoàn tất nguyên tử; client chỉ gửi ý định và version. */
    public static AssemblyLayout change(long id, long userId, String action, long instanceId,
            long slotId, String orientation, int expectedVersion) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        boolean original = true;
        boolean started = false;
        PreparedStatement ps = null;
        try {
            original = connection.getAutoCommit();
            connection.setAutoCommit(false);
            started = true;
            AssemblyLayout layout = load(connection, id, userId, true);
            if (layout == null) {
                throw new IllegalArgumentException("NOT_FOUND");
            }
            if (!"DETAILED".equals(layout.getMode())) {
                throw new IllegalArgumentException("GROUP_MODE");
            }
            String reason = "";
            if ("attach".equals(action)) {
                reason = layout.validateAttach(instanceId, slotId, orientation, expectedVersion);
            } else if ("detach".equals(action)) {
                reason = layout.validateDetach(instanceId, expectedVersion);
            } else {
                reason = layout.validateChange(action, expectedVersion);
            }
            if ("UNCHANGED".equals(reason)) {
                layout.setUnchanged(true);
                connection.commit();
                return layout;
            }
            if (!reason.isEmpty()) {
                throw new IllegalArgumentException(reason);
            }
            if ("attach".equals(action)) {
                ps = connection.prepareStatement("INSERT INTO assembly_placements "
                        + "(instance_id, session_id, spec_id, slot_id, component_id, orientation_code) "
                        + "VALUES (?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE "
                        + "orientation_code = VALUES(orientation_code), placed_at = CURRENT_TIMESTAMP");
                ps.setLong(1, instanceId);
                ps.setLong(2, id);
                ps.setLong(3, layout.getSpec().getId());
                ps.setLong(4, slotId);
                ps.setString(5, layout.findInstance(instanceId).getComponentId());
                ps.setString(6, orientation);
                ps.executeUpdate();
            } else if ("detach".equals(action) || "reset".equals(action)) {
                String query = "DELETE FROM assembly_placements WHERE session_id = ?";
                if ("detach".equals(action)) {
                    query += " AND instance_id = ?";
                }
                ps = connection.prepareStatement(query);
                ps.setLong(1, id);
                if ("detach".equals(action)) {
                    ps.setLong(2, instanceId);
                }
                ps.executeUpdate();
            }
            DBUtil.closePreparedStatement(ps);
            String update = "UPDATE assembly_sessions SET layout_version = layout_version + 1";
            if ("complete".equals(action)) {
                update += ", status = 'COMPLETED', completed_at = CURRENT_TIMESTAMP";
            }
            update += " WHERE id = ? AND user_id = ?";
            if ("reset".equals(action)) {
                update += " AND status IN ('PREPARING','READY','IN_PROGRESS')";
            } else {
                update += " AND status = 'IN_PROGRESS'";
            }
            update += " AND layout_version = ?";
            ps = connection.prepareStatement(update);
            ps.setLong(1, id);
            ps.setLong(2, userId);
            ps.setInt(3, expectedVersion);
            if (ps.executeUpdate() != 1) {
                throw new IllegalArgumentException("LAYOUT_CONFLICT");
            }
            AssemblyLayout updated = load(connection, id, userId, false);
            updated.setTransitioned("complete".equals(action));
            connection.commit();
            return updated;
        } catch (SQLException | IllegalArgumentException e) {
            if (started) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
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

    private static AssemblyLayout load(Connection connection, long id, long userId, boolean lock)
            throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        AssemblyLayout layout = new AssemblyLayout();
        long specId;
        try {
            String query = "SELECT id, status, assembly_mode, spec_id, layout_version "
                    + "FROM assembly_sessions WHERE id = ? AND user_id = ?";
            if (lock) {
                query += " FOR UPDATE";
            }
            ps = connection.prepareStatement(query);
            ps.setLong(1, id);
            ps.setLong(2, userId);
            rs = ps.executeQuery();
            if (!rs.next()) {
                return null;
            }
            layout.setSessionId(id);
            layout.setStatus(rs.getString("status"));
            layout.setMode(rs.getString("assembly_mode"));
            layout.setLayoutVersion(rs.getInt("layout_version"));
            specId = rs.getLong("spec_id");
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
        }
        if ("GROUP".equals(layout.getMode())) {
            return layout;
        }
        layout.setSpec(readSpec(connection, specId));
        readInstances(connection, layout);
        readPlacements(connection, layout);
        return layout;
    }

    private static AssemblySpec readSpec(Connection connection, long id) throws SQLException {
        AssemblySpec spec = new AssemblySpec();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = connection.prepareStatement("SELECT * FROM assembly_specs WHERE id = ?");
            ps.setLong(1, id);
            rs = ps.executeQuery();
            if (!rs.next()) {
                throw new SQLException("Missing immutable assembly spec");
            }
            spec.setId(id);
            spec.setRobotId(rs.getString("robot_id"));
            spec.setVersionNo(rs.getInt("version_no"));
            spec.setState(rs.getString("state"));
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
            ps = connection.prepareStatement("SELECT * FROM assembly_slots WHERE spec_id = ? ORDER BY display_order");
            ps.setLong(1, id);
            rs = ps.executeQuery();
            while (rs.next()) {
                AssemblySlot slot = new AssemblySlot();
                slot.setId(rs.getLong("id"));
                slot.setSpecId(id);
                slot.setSlotCode(rs.getString("slot_code"));
                slot.setParentSlotId(rs.getLong("parent_slot_id"));
                slot.setComponentId(rs.getString("component_id"));
                slot.setLabel(rs.getString("label"));
                slot.setRequired(rs.getBoolean("required"));
                slot.setX(rs.getDouble("x"));
                slot.setY(rs.getDouble("y"));
                slot.setZ(rs.getDouble("z"));
                slot.setRx(rs.getDouble("rx"));
                slot.setRy(rs.getDouble("ry"));
                slot.setRz(rs.getDouble("rz"));
                slot.setDisplayOrder(rs.getInt("display_order"));
                spec.getSlots().add(slot);
            }
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
            ps = connection.prepareStatement("SELECT o.slot_id, o.orientation_code "
                     + "FROM assembly_slot_orientations o JOIN assembly_slots s ON s.id = o.slot_id "
                    + "WHERE s.spec_id = ?");
            ps.setLong(1, id);
            rs = ps.executeQuery();
            while (rs.next()) {
                spec.findSlot(rs.getLong("slot_id")).getOrientations().add(rs.getString("orientation_code"));
            }
            return spec;
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
        }
    }

    private static List<RobotComponent> readKit(Connection connection, String robotId) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<RobotComponent> items = new ArrayList<>();
        try {
            ps = connection.prepareStatement("SELECT component_id, quantity FROM robot_components WHERE robot_id = ?");
            ps.setString(1, robotId);
            rs = ps.executeQuery();
            while (rs.next()) {
                RobotComponent item = new RobotComponent();
                item.setComponentId(rs.getString(1));
                item.setQuantity(rs.getInt(2));
                items.add(item);
            }
            return items;
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
        }
    }

    private static void readInstances(Connection connection, AssemblyLayout layout) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = connection.prepareStatement("SELECT i.*, c.name, "
                    + "(SELECT COUNT(*) FROM assembly_instances k WHERE k.session_id = i.session_id "
                    + "AND k.component_id = i.component_id) AS quantity "
                    + "FROM assembly_instances i JOIN components c ON c.id = i.component_id WHERE i.session_id = ? "
                    + "ORDER BY i.id");
            ps.setLong(1, layout.getSessionId());
            rs = ps.executeQuery();
            while (rs.next()) {
                AssemblyInstance item = new AssemblyInstance();
                item.setId(rs.getLong("id"));
                item.setSessionId(layout.getSessionId());
                item.setComponentId(rs.getString("component_id"));
                item.setInstanceNo(rs.getInt("instance_no"));
                String name = "dc-motor".equals(item.getComponentId()) ? "Động cơ DC TT" : rs.getString("name");
                item.setLabel(name + " " + item.getInstanceNo() + "/" + rs.getInt("quantity"));
                layout.getInstances().add(item);
            }
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
        }
    }

    private static void readPlacements(Connection connection, AssemblyLayout layout) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = connection.prepareStatement(
                    "SELECT * FROM assembly_placements WHERE session_id = ? ORDER BY slot_id");
            ps.setLong(1, layout.getSessionId());
            rs = ps.executeQuery();
            while (rs.next()) {
                AssemblyPlacement placement = new AssemblyPlacement();
                placement.setInstanceId(rs.getLong("instance_id"));
                placement.setSessionId(layout.getSessionId());
                placement.setSlotId(rs.getLong("slot_id"));
                placement.setOrientationCode(rs.getString("orientation_code"));
                placement.setInstanceLabel(layout.findInstance(placement.getInstanceId()).getLabel());
                placement.setSlotLabel(layout.getSpec().findSlot(placement.getSlotId()).getLabel());
                java.text.SimpleDateFormat date = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
                date.setTimeZone(java.util.TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
                placement.setPlacedAt(date.format(rs.getTimestamp("placed_at")));
                layout.getPlacements().add(placement);
            }
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
        }
    }
}
