package vn.edu.webpro.robotlab.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import vn.edu.webpro.robotlab.business.AssemblyEvent;

/** Nhật ký chỉ dùng kết nối transaction của thao tác; đọc luôn lọc chủ phiên. */
public class AssemblyEventDB {
    /** Ghi nhật ký cùng transaction; không tự mở kết nối hoặc commit riêng. */
    public static void insert(Connection connection, AssemblyEvent event, long userId) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = connection.prepareStatement("INSERT INTO assembly_events "
                    + "(session_id, spec_id, action, outcome, reason_code, instance_id, slot_id, "
                    + "orientation_code, prev_orientation_code, undo_of_event_id, layout_version_after, created_at) "
                    + "SELECT s.id, s.spec_id, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? "
                    + "FROM assembly_sessions s WHERE s.id = ? AND s.user_id = ?", Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, event.getAction());
            ps.setString(2, event.getOutcome());
            ps.setString(3, event.getReasonCode());
            nullableId(ps, 4, event.getInstanceId());
            nullableId(ps, 5, event.getSlotId());
            ps.setString(6, event.getOrientationCode());
            ps.setString(7, event.getPrevOrientationCode());
            nullableId(ps, 8, event.getUndoOfEventId());
            ps.setInt(9, event.getLayoutVersionAfter());
            ps.setTimestamp(10, new Timestamp(event.getCreatedAt().getTime()));
            ps.setLong(11, event.getSessionId());
            ps.setLong(12, userId);
            if (ps.executeUpdate() != 1) {
                throw new SQLException("Event owner row missing");
            }
            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                event.setId(rs.getLong(1));
            }
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
        }
    }

    private static void nullableId(PreparedStatement ps, int index, long id) throws SQLException {
        if (id > 0) {
            ps.setLong(index, id);
        } else {
            ps.setNull(index, java.sql.Types.BIGINT);
        }
    }

    /** Giới hạn số từ chối dưới khóa phiên, không tin bộ đếm client. */
    public static int countRejected(Connection connection, long sessionId, long userId) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = connection.prepareStatement("SELECT COUNT(*) FROM assembly_events e "
                    + "JOIN assembly_sessions s ON s.id = e.session_id "
                    + "WHERE e.session_id = ? AND s.user_id = ? AND e.outcome = 'REJECTED'");
            ps.setLong(1, sessionId);
            ps.setLong(2, userId);
            rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1);
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
        }
    }

    /** Đọc thứ tự và nhãn bằng một truy vấn, không SELECT riêng từng vật thể. */
    public static List<AssemblyEvent> selectBySession(long sessionId, long userId) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        try {
            return select(connection, sessionId, userId);
        } finally {
            pool.freeConnection(connection);
        }
    }

    private static List<AssemblyEvent> select(Connection connection, long sessionId, long userId)
            throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = connection.prepareStatement("SELECT e.*, i.instance_no, i.component_id, c.name AS component_name, "
                    + "sl.label AS slot_label, u.id AS undo_id, "
                    + "(SELECT COUNT(*) FROM assembly_instances ai WHERE ai.session_id = e.session_id "
                    + "AND ai.component_id = i.component_id) AS instance_count "
                    + "FROM assembly_events e JOIN assembly_sessions s ON s.id = e.session_id "
                    + "LEFT JOIN assembly_instances i ON i.id = e.instance_id AND i.session_id = e.session_id "
                    + "LEFT JOIN components c ON c.id = i.component_id "
                    + "LEFT JOIN assembly_slots sl ON sl.id = e.slot_id AND sl.spec_id = e.spec_id "
                    + "LEFT JOIN assembly_events u ON u.undo_of_event_id = e.id "
                    + "WHERE e.session_id = ? AND s.user_id = ? ORDER BY e.id");
            ps.setLong(1, sessionId);
            ps.setLong(2, userId);
            rs = ps.executeQuery();
            List<AssemblyEvent> events = new ArrayList<>();
            while (rs.next()) {
                AssemblyEvent event = read(rs);
                String name = rs.getString("component_name");
                if (name != null) {
                    if ("dc-motor".equals(rs.getString("component_id"))) {
                        name = "Động cơ DC TT";
                    }
                    event.setInstanceLabel(name + " " + rs.getInt("instance_no") + "/"
                            + rs.getInt("instance_count"));
                }
                String slotLabel = rs.getString("slot_label");
                event.setSlotLabel(slotLabel == null ? "" : slotLabel);
                event.setUndone(rs.getLong("undo_id") > 0);
                events.add(event);
            }
            return events;
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
        }
    }

    /** Sự kiện có thể đảo gần nhất sau reset, trong kết nối đang khóa phiên. */
    public static AssemblyEvent selectUndoTarget(Connection connection, long sessionId, long userId)
            throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = connection.prepareStatement("SELECT e.* FROM assembly_events e "
                    + "JOIN assembly_sessions s ON s.id = e.session_id "
                    + "WHERE e.session_id = ? AND s.user_id = ? AND e.outcome = 'ACCEPTED' "
                    + "AND e.action IN ('ATTACH','DETACH') "
                    + "AND NOT EXISTS (SELECT 1 FROM assembly_events u WHERE u.undo_of_event_id = e.id) "
                    + "AND e.id > COALESCE((SELECT MAX(r.id) FROM assembly_events r "
                    + "WHERE r.session_id = e.session_id AND r.action = 'RESET' AND r.outcome = 'ACCEPTED'), 0) "
                    + "ORDER BY e.id DESC LIMIT 1");
            ps.setLong(1, sessionId);
            ps.setLong(2, userId);
            rs = ps.executeQuery();
            return rs.next() ? read(rs) : null;
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
        }
    }

    private static AssemblyEvent read(ResultSet rs) throws SQLException {
        AssemblyEvent e = new AssemblyEvent();
        e.setId(rs.getLong("id"));
        e.setSessionId(rs.getLong("session_id"));
        e.setSpecId(rs.getLong("spec_id"));
        e.setAction(rs.getString("action"));
        e.setOutcome(rs.getString("outcome"));
        e.setReasonCode(rs.getString("reason_code"));
        e.setInstanceId(rs.getLong("instance_id"));
        e.setSlotId(rs.getLong("slot_id"));
        e.setOrientationCode(rs.getString("orientation_code"));
        e.setPrevOrientationCode(rs.getString("prev_orientation_code"));
        e.setUndoOfEventId(rs.getLong("undo_of_event_id"));
        e.setLayoutVersionAfter(rs.getInt("layout_version_after"));
        e.setCreatedAt(rs.getTimestamp("created_at"));
        return e;
    }
}
