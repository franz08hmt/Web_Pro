package vn.edu.webpro.robotlab.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import vn.edu.webpro.robotlab.business.DesignLayout;
import vn.edu.webpro.robotlab.business.DesignPlacement;
import vn.edu.webpro.robotlab.business.DesignSlot;
import vn.edu.webpro.robotlab.business.DesignEvent;
import vn.edu.webpro.robotlab.business.RobotDesign;
import vn.edu.webpro.robotlab.util.DesignException;

/** JDBC bố cục riêng, khóa owner/design trước khi gọi luật JavaBean và ghi nhật ký. */
public class DesignPlacementDB {
    /** Đọc snapshot nhất quán, GET không ghi hoặc tự kiểm thiết kế. */
    public static DesignLayout selectLayout(long id, long userId) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        boolean original = true;
        boolean started = false;
        try {
            original = connection.getAutoCommit();
            connection.setAutoCommit(false);
            started = true;
            DesignLayout layout = load(connection, id, userId, false);
            connection.commit();
            return layout;
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
            try {
                if (started) {
                    connection.setAutoCommit(original);
                }
            } finally {
                pool.freeConnection(connection);
            }
        }
    }

    /** Đọc từ cùng kết nối khi đang khóa transaction, không lấy version mới nhất của luật. */
    public static DesignLayout load(Connection connection, long id, long userId, boolean lock) throws SQLException {
        RobotDesign design = DesignDB.read(connection, id, userId, lock);
        if (design == null) {
            return null;
        }
        DesignLayout layout = new DesignLayout();
        layout.setDesign(design);
        layout.setRules(DesignRuleDB.read(connection, design.getRuleSetId()));
        layout.setInstances(DesignDB.readInstances(connection, id, userId));
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = connection.prepareStatement("SELECT p.* FROM design_placements p "
                    + "JOIN robot_designs d ON d.id = p.design_id WHERE d.id = ? AND d.user_id = ? "
                    + "ORDER BY p.instance_id");
            ps.setLong(1, id);
            ps.setLong(2, userId);
            rs = ps.executeQuery();
            while (rs.next()) {
                DesignPlacement placed = new DesignPlacement();
                placed.setInstanceId(rs.getLong("instance_id"));
                placed.setHostInstanceId(rs.getLong("host_instance_id"));
                placed.setMountCode(rs.getString("mount_code"));
                placed.setOrientationCode(rs.getString("orientation_code"));
                placed.setPlacedAt(rs.getTimestamp("placed_at"));
                layout.getPlacements().add(placed);
            }
            return layout;
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
        }
    }

    /** Gắn/tháo/undo nguyên tử; lỗi truy vết không đổi placement/version. */
    public static DesignLayout change(long id, long userId, String action, long itemId,
            String slotId, String orientation, int version) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement ps = null;
        boolean original = true;
        boolean started = false;
        boolean committed = false;
        try {
            original = connection.getAutoCommit();
            connection.setAutoCommit(false);
            started = true;
            DesignLayout layout = load(connection, id, userId, true);
            if (layout == null) {
                throw new DesignException("NOT_FOUND", "Không tìm thấy thiết kế bạn được phép dùng.");
            }
            String reason;
            String effect = action;
            DesignEvent target = null;
            if ("undo".equals(action)) {
                target = DesignEventDB.selectUndoTarget(connection, id, userId);
                reason = layout.validateUndo(target, version);
                if (target != null) {
                    effect = target.getInverseAction();
                    itemId = target.getInstanceId();
                    slotId = target.getHostInstanceId() + ":" + target.getMountCode();
                    orientation = target.getInverseOrientation();
                }
            } else if ("attach".equals(action)) {
                reason = layout.validateAttach(itemId, slotId, orientation, version);
            } else if ("detach".equals(action)) {
                reason = layout.validateDetach(itemId, version);
            } else {
                throw new DesignException("VALIDATION_ERROR", "Thao tác không hợp lệ. Hãy tải lại mô hình.");
            }
            if ("UNCHANGED".equals(reason)) {
                layout.setUnchanged(true);
                connection.commit();
                return layout;
            }
            if (!reason.isEmpty()) {
                if (!"NOT_FOUND".equals(reason) && DesignEventDB.countRejected(connection, id, userId) < 300) {
                    DesignEvent event = new DesignEvent();
                    event.capture(action, layout, itemId, slotId, orientation);
                    event.setOutcome("REJECTED");
                    event.setReasonCode(reason);
                    DesignEventDB.insert(connection, event, userId);
                    connection.commit();
                    committed = true;
                }
                throw new DesignException(reason, layout.messageFor(reason, itemId, slotId));
            }
            DesignEvent event = new DesignEvent();
            event.capture(action, layout, itemId, slotId, orientation);
            if (target != null) {
                event.setUndoOfEventId(target.getId());
                event.setOrientationCode("attach".equals(effect) ? orientation : null);
            }
            if ("attach".equals(effect)) {
                DesignSlot slot = layout.resolveSlot(slotId);
                ps = connection.prepareStatement("INSERT INTO design_placements "
                        + "(design_id,instance_id,host_instance_id,rule_set_id,component_id,host_component_id,"
                        + "mount_code,mount_type,orientation_code) VALUES (?,?,?,?,?,?,?,?,?) "
                        + "ON DUPLICATE KEY UPDATE orientation_code = VALUES(orientation_code),"
                        + "placed_at = CURRENT_TIMESTAMP");
                ps.setLong(1, id);
                ps.setLong(2, itemId);
                DesignEventDB.nullableId(ps, 3, slot.getHostInstanceId());
                ps.setLong(4, layout.getRules().getId());
                ps.setString(5, layout.findInstance(itemId).getComponentId());
                ps.setString(6, slot.getHostInstanceId() == 0 ? null : slot.getHostComponentId());
                ps.setString(7, slot.getMountCode());
                ps.setString(8, slot.getMountType());
                ps.setString(9, orientation);
            } else {
                ps = connection.prepareStatement("DELETE p FROM design_placements p "
                        + "JOIN robot_designs d ON d.id = p.design_id "
                        + "WHERE d.id = ? AND d.user_id = ? AND p.instance_id = ?");
                ps.setLong(1, id);
                ps.setLong(2, userId);
                ps.setLong(3, itemId);
            }
            ps.executeUpdate();
            DBUtil.closePreparedStatement(ps);
            ps = connection.prepareStatement("UPDATE robot_designs SET layout_version = layout_version + 1,"
                    + "status = 'DRAFT',last_check_summary = NULL,last_checked_at = NULL,"
                    + "updated_at = CURRENT_TIMESTAMP "
                    + "WHERE id = ? AND user_id = ? AND layout_version = ?");
            ps.setLong(1, id);
            ps.setLong(2, userId);
            ps.setInt(3, version);
            if (ps.executeUpdate() != 1) {
                throw new DesignException("LAYOUT_CONFLICT", "Thiết kế đã thay đổi. Hãy tải lại mô hình.");
            }
            DesignLayout updated = load(connection, id, userId, false);
            event.setLayoutVersionAfter(updated.getDesign().getLayoutVersion());
            DesignEventDB.insert(connection, event, userId);
            connection.commit();
            committed = true;
            return updated;
        } catch (SQLException | IllegalArgumentException e) {
            if (started && !committed) {
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
}
