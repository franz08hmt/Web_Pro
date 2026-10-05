package vn.edu.webpro.robotlab.data;

import vn.edu.webpro.robotlab.business.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;

/** JDBC hỗ trợ: khóa lượt trước yêu cầu, chỉ thêm tin/bản chụp và đổi trạng thái hỗ trợ. */
public class WiringSupportDB {
    private static void bind(PreparedStatement ps, Object... values) throws SQLException {
        for (int i = 0; i < values.length; i++) {
            if (values[i] instanceof Date) {
                ps.setTimestamp(i + 1, new Timestamp(((Date) values[i]).getTime()));
            } else {
                ps.setObject(i + 1, values[i]);
            }
        }
    }

    private static long change(Connection c, String sql, Object... values) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            bind(ps, values);
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getLong(1);
            }
            return 0;
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
        }
    }

    private static Date date(ResultSet rs, String name) throws SQLException {
        Timestamp ts = rs.getTimestamp(name);
        if (ts == null) {
            return null;
        }
        return new Date(ts.getTime());
    }

    private static WiringSupportRequest read(ResultSet rs) throws SQLException {
        WiringSupportRequest r = new WiringSupportRequest();
        r.setId(rs.getLong("id"));
        r.setAttemptId(rs.getLong("attempt_id"));
        r.setExerciseId(rs.getLong("exercise_id"));
        r.setUserId(rs.getLong("user_id"));
        r.setState(rs.getString("state"));
        r.setVersion(rs.getInt("version"));
        r.setCreatedAt(date(rs, "created_at"));
        r.setUpdatedAt(date(rs, "updated_at"));
        r.setClosedAt(date(rs, "closed_at"));
        r.setOwnerName(rs.getString("owner_name"));
        r.setExerciseTitle(rs.getString("exercise_title"));
        return r;
    }

    private static WiringSupportRequest request(Connection c, long id, long owner, boolean admin, boolean lock)
            throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String sql = "SELECT s.*,u.display_name AS owner_name,e.title AS exercise_title "
                    + "FROM wiring_support_requests s JOIN users u ON u.id=s.user_id "
                    + "JOIN wiring_exercises e ON e.id=s.exercise_id WHERE s.id = ?";
            if (lock) {
                sql = "SELECT s.*,'' AS owner_name,'' AS exercise_title FROM wiring_support_requests s WHERE s.id = ?";
            }
            if (!admin) {
                sql += " AND s.user_id = ?";
            }
            if (lock) {
                sql += " FOR UPDATE";
            }
            ps = c.prepareStatement(sql);
            ps.setLong(1, id);
            if (!admin) {
                ps.setLong(2, owner);
            }
            rs = ps.executeQuery();
            if (rs.next()) {
                return read(rs);
            }
            return null;
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
        }
    }

    private static void messages(Connection c, WiringSupportRequest r) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = c.prepareStatement("SELECT m.*,u.display_name AS author_name FROM wiring_support_messages m "
                    + "JOIN users u ON u.id=m.author_id WHERE m.request_id = ? ORDER BY m.id");
            ps.setLong(1, r.getId());
            rs = ps.executeQuery();
            while (rs.next()) {
                WiringSupportMessage m = new WiringSupportMessage();
                m.setId(rs.getLong("id"));
                m.setRequestId(r.getId());
                m.setAuthorId(rs.getLong("author_id"));
                m.setAuthorName(rs.getString("author_name"));
                m.setAdmin(rs.getBoolean("is_admin"));
                m.setContent(rs.getString("content"));
                long terminal = rs.getLong("terminal_id");
                if (!rs.wasNull()) {
                    m.setTerminalId(terminal);
                }
                m.setCreatedAt(date(rs, "created_at"));
                m.setSnapshotText(rs.getString("snapshot_text"));
                m.setSnapshotPairs(rs.getString("snapshot_pairs"));
                m.setSnapshotVersion(rs.getInt("snapshot_version"));
                m.setSnapshotState(rs.getString("snapshot_state"));
                m.setSnapshotScore(rs.getBigDecimal("snapshot_score"));
                m.setSnapshotAt(date(rs, "snapshot_at"));
                m.setExercise(r.getExercise());
                r.getMessages().add(m);
            }
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
        }
    }

    /** Danh sách dùng một query; USER chỉ lấy hàng của mình, Admin đọc mọi yêu cầu. */
    public static ArrayList<WiringSupportRequest> selectRequests(long owner, boolean admin) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection c = pool.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String sql = "SELECT s.*,u.display_name AS owner_name,e.title AS exercise_title "
                    + "FROM wiring_support_requests s JOIN users u ON u.id=s.user_id "
                    + "JOIN wiring_exercises e ON e.id=s.exercise_id";
            if (!admin) {
                sql += " WHERE s.user_id = ?";
            }
            sql += " ORDER BY s.id DESC";
            ps = c.prepareStatement(sql);
            if (!admin) {
                ps.setLong(1, owner);
            }
            rs = ps.executeQuery();
            ArrayList<WiringSupportRequest> list = new ArrayList<>();
            while (rs.next()) {
                list.add(read(rs));
            }
            return list;
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
            pool.freeConnection(c);
        }
    }

    /** Đọc hội thoại theo owner; tải bài một lần và lịch sử bằng một query, không N+1. */
    public static WiringSupportRequest selectRequest(long id, long owner, boolean admin) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection c = pool.getConnection();
        try {
            WiringSupportRequest r = request(c, id, owner, admin, false);
            if (r != null) {
                WiringAttempt a = WiringDB.selectAttempt(c, r.getAttemptId(), r.getUserId(), false);
                r.setExercise(a.getExercise());
                messages(c, r);
            }
            return r;
        } finally {
            pool.freeConnection(c);
        }
    }

    private static void insertMessage(Connection c, WiringSupportRequest r, WiringSupportMessage m)
            throws SQLException {
        Integer snapshotVersion = null;
        if (m.isHasSnapshot()) {
            snapshotVersion = m.getSnapshotVersion();
        }
        change(c, "INSERT INTO wiring_support_messages "
                + "(request_id,exercise_id,author_id,is_admin,content,terminal_id,created_at,"
                + "snapshot_text,snapshot_pairs,snapshot_version,snapshot_state,snapshot_score,snapshot_at) "
                + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)",
                r.getId(), r.getExerciseId(), m.getAuthorId(), m.isAdmin(), m.getContent(), m.getTerminalId(),
                new Date(), m.getSnapshotText(), m.getSnapshotPairs(), snapshotVersion, m.getSnapshotState(),
                m.getSnapshotScore(), m.getSnapshotAt());
    }

    private static void finish(Connection connection, boolean original, ConnectionPool pool) throws SQLException {
        try {
            if (!connection.getAutoCommit()) {
                connection.rollback();
            }
        } finally {
            try {
                connection.setAutoCommit(original);
            } finally {
                pool.freeConnection(connection);
            }
        }
    }

    private static void reference(WiringAttempt a, WiringSupportMessage m) {
        if (m.getTerminalId() != null) {
            a.getExercise().terminal(m.getTerminalId());
        }
    }

    /** Tạo và chụp nguyên tử; khóa lượt + UNIQUE cột sinh ngăn hai yêu cầu đang mở. */
    public static long create(long attemptId, long owner, int expectedVersion, WiringSupportMessage m)
            throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection c = pool.getConnection();
        boolean original = c.getAutoCommit();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            WiringAttempt before = WiringDB.selectAttempt(c, attemptId, owner, false);
            if (before == null) {
                return 0;
            }
            c.setAutoCommit(false);
            WiringDB.lockExercise(c, before.getExerciseId());
            WiringAttempt a = WiringDB.selectAttempt(c, attemptId, owner, true);
            if (a == null) {
                return 0;
            }
            m.validate(false);
            if (a.getVersion() != expectedVersion) {
                throw new IllegalArgumentException("Lượt đã thay đổi; tải lại và lưu trước khi gửi hỗ trợ.");
            }
            ps = c.prepareStatement("SELECT id FROM wiring_support_requests "
                    + "WHERE attempt_id = ? AND state IN ('OPEN','ANSWERED') FOR UPDATE");
            ps.setLong(1, attemptId);
            rs = ps.executeQuery();
            if (rs.next()) {
                throw new IllegalArgumentException("Lượt này đã có yêu cầu đang mở; hãy gửi tiếp trong yêu cầu đó.");
            }
            reference(a, m);
            m.capture(a, new Date());
            m.setAuthorId(owner);
            WiringSupportRequest r = new WiringSupportRequest();
            r.setAttemptId(attemptId);
            r.setExerciseId(a.getExerciseId());
            r.setUserId(owner);
            r.setId(change(c, "INSERT INTO wiring_support_requests (attempt_id,exercise_id,user_id) VALUES (?,?,?)",
                    attemptId, a.getExerciseId(), owner));
            insertMessage(c, r, m);
            c.commit();
            return r.getId();
        } catch (SQLException error) {
            c.rollback();
            if (error.getErrorCode() == 1062) {
                throw new IllegalArgumentException("Lượt đã có hỗ trợ đang mở; tải lại danh sách.");
            }
            throw error;
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
            finish(c, original, pool);
        }
    }

    /** Tin/đóng kiểm version sau khóa; USER tùy chọn chụp mới, Admin chỉ phản hồi văn bản. */
    public static boolean mutate(long id, long actor, boolean admin, int expectedVersion,
            WiringSupportMessage m, boolean snapshot, boolean close) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection c = pool.getConnection();
        boolean original = c.getAutoCommit();
        try {
            // Metadata đọc trước transaction; consistent read bản chụp chỉ bắt đầu sau khóa lượt.
            WiringSupportRequest before = request(c, id, actor, admin, false);
            if (before == null) {
                return false;
            }
            c.setAutoCommit(false);
            WiringDB.lockExercise(c, before.getExerciseId());
            WiringAttempt a = WiringDB.selectAttempt(c, before.getAttemptId(), before.getUserId(), true);
            WiringSupportRequest r = request(c, id, actor, admin, true);
            if (r == null) {
                return false;
            }
            if (close) {
                r.close(expectedVersion);
            } else {
                r.requireCurrent(expectedVersion);
                m.validate(admin);
                reference(a, m);
                m.setAuthorId(actor);
                if (snapshot && !admin) {
                    m.capture(a, new Date());
                }
                r.applyMessage(admin, expectedVersion);
                insertMessage(c, r, m);
            }
            change(c, "UPDATE wiring_support_requests SET state=?,version=?,updated_at=?,closed_at=? "
                    + "WHERE id=?", r.getState(), r.getVersion(), r.getUpdatedAt(), r.getClosedAt(), r.getId());
            c.commit();
            return true;
        } finally {
            finish(c, original, pool);
        }
    }
}
