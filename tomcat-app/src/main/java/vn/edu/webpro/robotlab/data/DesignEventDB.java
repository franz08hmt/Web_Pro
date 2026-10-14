package vn.edu.webpro.robotlab.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import vn.edu.webpro.robotlab.business.DesignEvent;

/** Nhật ký thiết kế cùng transaction placement, không mở connection riêng để insert. */
public class DesignEventDB {
    /** INSERT SELECT kiểm lại owner, tham số không lấy danh tính từ client. */
    public static void insert(Connection connection, DesignEvent event, long userId) throws SQLException {
        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement("INSERT INTO design_events "
                    + "(design_id,action,outcome,reason_code,instance_id,host_instance_id,mount_code,"
                    + "orientation_code,prev_orientation_code,undo_of_event_id,layout_version_after,created_at) "
                    + "SELECT d.id,?,?,?,?,?,?,?,?,?,?,? FROM robot_designs d WHERE d.id = ? AND d.user_id = ?");
            ps.setString(1, event.getAction());
            ps.setString(2, event.getOutcome());
            ps.setString(3, event.getReasonCode());
            nullableId(ps, 4, event.getInstanceId());
            nullableId(ps, 5, event.getHostInstanceId());
            ps.setString(6, event.getMountCode());
            ps.setString(7, event.getOrientationCode());
            ps.setString(8, event.getPrevOrientationCode());
            nullableId(ps, 9, event.getUndoOfEventId());
            ps.setInt(10, event.getLayoutVersionAfter());
            ps.setTimestamp(11, new Timestamp(event.getCreatedAt().getTime()));
            ps.setLong(12, event.getDesignId());
            ps.setLong(13, userId);
            if (ps.executeUpdate() != 1) {
                throw new SQLException("Missing event owner");
            }
        } finally {
            DBUtil.closePreparedStatement(ps);
        }
    }

    /** Trần lỗi truy vết áp dưới khóa thiết kế để tránh ghi không giới hạn. */
    public static int countRejected(Connection connection, long id, long userId) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = connection.prepareStatement("SELECT COUNT(*) FROM design_events e "
                    + "JOIN robot_designs d ON d.id = e.design_id "
                    + "WHERE d.id = ? AND d.user_id = ? AND e.outcome = 'REJECTED'");
            ps.setLong(1, id);
            ps.setLong(2, userId);
            rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1);
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
        }
    }

    /** Sự kiện có thể đảo gần nhất, bỏ CHECK và các thao tác đã undo. */
    public static DesignEvent selectUndoTarget(Connection connection, long id, long userId) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = connection.prepareStatement("SELECT e.* FROM design_events e "
                    + "JOIN robot_designs d ON d.id = e.design_id WHERE d.id = ? AND d.user_id = ? "
                    + "AND e.outcome = 'ACCEPTED' AND e.action IN ('ATTACH','DETACH') "
                    + "AND NOT EXISTS (SELECT 1 FROM design_events u WHERE u.undo_of_event_id = e.id) "
                    + "ORDER BY e.id DESC LIMIT 1");
            ps.setLong(1, id);
            ps.setLong(2, userId);
            rs = ps.executeQuery();
            return rs.next() ? read(rs) : null;
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
        }
    }

    /** Lịch sử owner-scoped cho hồ sơ, nhãn lấy từ roster cùng design. */
    public static List<DesignEvent> selectEvents(long id, long userId) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        try {
            return readEvents(connection, id, userId);
        } finally {
            pool.freeConnection(connection);
        }
    }

    /** Dùng snapshot hồ sơ cùng connection, không mở thêm truy vấn từng vật thể. */
    public static List<DesignEvent> readEvents(Connection connection, long id, long userId) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = connection.prepareStatement("SELECT e.*,c.name,i.instance_no,u.id AS undo_id "
                    + "FROM design_events e JOIN robot_designs d ON d.id = e.design_id "
                    + "LEFT JOIN design_instances i ON i.id = e.instance_id AND i.design_id = d.id "
                    + "LEFT JOIN components c ON c.id = i.component_id "
                    + "LEFT JOIN design_events u ON u.undo_of_event_id = e.id "
                    + "WHERE d.id = ? AND d.user_id = ? ORDER BY e.id");
            ps.setLong(1, id);
            ps.setLong(2, userId);
            rs = ps.executeQuery();
            List<DesignEvent> events = new ArrayList<>();
            while (rs.next()) {
                DesignEvent event = read(rs);
                String name = rs.getString("name");
                event.setInstanceLabel(name == null ? "" : name + " " + rs.getInt("instance_no"));
                event.setUndone(rs.getLong("undo_id") > 0);
                events.add(event);
            }
            return events;
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
        }
    }

    /** Chỉ helper bind ID nullable, không kiểm luật tại JDBC. */
    public static void nullableId(PreparedStatement ps, int index, long id) throws SQLException {
        if (id > 0) {
            ps.setLong(index, id);
        } else {
            ps.setNull(index, Types.BIGINT);
        }
    }

    private static DesignEvent read(ResultSet rs) throws SQLException {
        DesignEvent event = new DesignEvent();
        event.setId(rs.getLong("id"));
        event.setDesignId(rs.getLong("design_id"));
        event.setAction(rs.getString("action"));
        event.setOutcome(rs.getString("outcome"));
        event.setReasonCode(rs.getString("reason_code"));
        event.setInstanceId(rs.getLong("instance_id"));
        event.setHostInstanceId(rs.getLong("host_instance_id"));
        event.setMountCode(rs.getString("mount_code"));
        event.setOrientationCode(rs.getString("orientation_code"));
        event.setPrevOrientationCode(rs.getString("prev_orientation_code"));
        event.setUndoOfEventId(rs.getLong("undo_of_event_id"));
        event.setLayoutVersionAfter(rs.getInt("layout_version_after"));
        event.setCreatedAt(rs.getTimestamp("created_at"));
        return event;
    }
}
