package vn.edu.webpro.robotlab.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import vn.edu.webpro.robotlab.business.AssemblySession;
import vn.edu.webpro.robotlab.business.SessionComponent;
import vn.edu.webpro.robotlab.business.SessionStep;

/**
 * Đọc/ghi phiên lắp ráp và ba bảng tiến độ đi kèm — cùng khuôn với UserDB.
 *
 * Mọi câu truy vấn phiên đều kèm user_id, nên một tài khoản không đọc hay sửa
 * được phiên của tài khoản khác kể cả khi đoán đúng ID.
 */
public class AssemblySessionDB {
    private static final String FIELDS = "id, user_id, robot_id, status, updated_at";

    /** Tạo phiên mới ở trạng thái PREPARING và trả về ID MySQL vừa sinh. */
    public static long insert(long userId, String robotId) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        String query = "INSERT INTO assembly_sessions (user_id, robot_id, status) "
                + "VALUES (?, ?, 'PREPARING')";
        try {
            ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, userId);
            ps.setString(2, robotId);
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            rs.next();
            return rs.getLong(1);
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
            pool.freeConnection(connection);
        }
    }

    public static int updateStatus(long id, long userId, String status) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement ps = null;

        String query = "UPDATE assembly_sessions SET status = ? WHERE id = ? AND user_id = ?";
        try {
            ps = connection.prepareStatement(query);
            ps.setString(1, status);
            ps.setLong(2, id);
            ps.setLong(3, userId);
            return ps.executeUpdate();
        } finally {
            DBUtil.closePreparedStatement(ps);
            pool.freeConnection(connection);
        }
    }

    /** Xóa toàn bộ tiến độ của phiên thuộc user và đưa phiên về PREPARING. */
    public static boolean resetProgress(long id, long userId) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        boolean originalAutoCommit = true;
        boolean transactionStarted = false;
        boolean transactionFinished = false;

        try {
            originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            transactionStarted = true;

            ps = connection.prepareStatement(
                    "SELECT status FROM assembly_sessions WHERE id = ? AND user_id = ? FOR UPDATE");
            ps.setLong(1, id);
            ps.setLong(2, userId);
            rs = ps.executeQuery();
            if (!rs.next()) {
                connection.rollback();
                transactionFinished = true;
                return false;
            }
            String status = rs.getString("status");
            DBUtil.closeResultSet(rs);
            rs = null;
            DBUtil.closePreparedStatement(ps);
            ps = null;

            if (!AssemblySession.PREPARING.equals(status)
                    && !AssemblySession.READY.equals(status)
                    && !AssemblySession.IN_PROGRESS.equals(status)) {
                connection.rollback();
                transactionFinished = true;
                return false;
            }

            deleteProgressRows(connection, "session_components", id);
            deleteProgressRows(connection, "session_steps", id);
            deleteProgressRows(connection, "session_visual_parts", id);

            ps = connection.prepareStatement(
                    "UPDATE assembly_sessions SET status = 'PREPARING' WHERE id = ? AND user_id = ?");
            ps.setLong(1, id);
            ps.setLong(2, userId);
            ps.executeUpdate();
            connection.commit();
            transactionFinished = true;
            return true;
        } catch (SQLException e) {
            if (transactionStarted && !transactionFinished) {
                try {
                    connection.rollback();
                    transactionFinished = true;
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }
            }
            throw e;
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
            if (transactionStarted && !transactionFinished) {
                try {
                    connection.rollback();
                } catch (SQLException e) {
                    System.out.println(e);
                }
            }
            if (transactionStarted) {
                try {
                    connection.setAutoCommit(originalAutoCommit);
                } catch (SQLException e) {
                    System.out.println(e);
                }
            }
            pool.freeConnection(connection);
        }
    }

    private static void deleteProgressRows(Connection connection, String table, long sessionId)
            throws SQLException {
        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement("DELETE FROM " + table + " WHERE session_id = ?");
            ps.setLong(1, sessionId);
            ps.executeUpdate();
        } finally {
            DBUtil.closePreparedStatement(ps);
        }
    }

    /** Lưu trạng thái tick của một linh kiện; dòng đã có thì cập nhật. */
    public static int updateComponent(AssemblySession session, String componentId, boolean prepared)
            throws SQLException {
        String query = "INSERT INTO session_components (session_id, robot_id, component_id, prepared) "
                + "VALUES (?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE prepared = VALUES(prepared)";
        return writeProgress(session, query, componentId, prepared);
    }

    /** Lưu trạng thái hoàn thành của một bước lắp ráp. */
    public static int updateStep(AssemblySession session, String stepId, boolean completed)
            throws SQLException {
        String query = "INSERT INTO session_steps (session_id, robot_id, step_id, completed) "
                + "VALUES (?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE completed = VALUES(completed)";
        return writeProgress(session, query, stepId, completed);
    }

    /** Ghi nhận một part đã được lắp trong phòng 3D. */
    public static int insertVisualPart(AssemblySession session, String componentId) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement ps = null;

        String query = "INSERT INTO session_visual_parts (session_id, robot_id, component_id) "
                + "VALUES (?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE component_id = VALUES(component_id)";
        try {
            ps = connection.prepareStatement(query);
            ps.setLong(1, session.getId());
            ps.setString(2, session.getRobotId());
            ps.setString(3, componentId);
            int count = ps.executeUpdate();
            touch(connection, session.getId());
            return count;
        } finally {
            DBUtil.closePreparedStatement(ps);
            pool.freeConnection(connection);
        }
    }

    /** Gỡ một part khỏi mô hình 3D. */
    public static int deleteVisualPart(AssemblySession session, String componentId) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement ps = null;

        String query = "DELETE FROM session_visual_parts WHERE session_id = ? AND component_id = ?";
        try {
            ps = connection.prepareStatement(query);
            ps.setLong(1, session.getId());
            ps.setString(2, componentId);
            int count = ps.executeUpdate();
            touch(connection, session.getId());
            return count;
        } finally {
            DBUtil.closePreparedStatement(ps);
            pool.freeConnection(connection);
        }
    }

    /** Phiên đang mở (chưa hoàn thành, chưa dừng) gần nhất của robot này, nếu có. */
    public static AssemblySession selectOpenSession(long userId, String robotId) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        String query = "SELECT " + FIELDS + " FROM assembly_sessions "
                + "WHERE user_id = ? AND robot_id = ? "
                + "AND status IN ('PREPARING', 'READY', 'IN_PROGRESS') "
                + "ORDER BY updated_at DESC, id DESC LIMIT 1";
        try {
            ps = connection.prepareStatement(query);
            ps.setLong(1, userId);
            ps.setString(2, robotId);
            rs = ps.executeQuery();
            AssemblySession session = null;
            if (rs.next()) {
                session = readSession(connection, rs);
            }
            return session;
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
            pool.freeConnection(connection);
        }
    }

    /** Chỉ trả về phiên khi đúng chủ sở hữu; phiên của người khác trả về null. */
    public static AssemblySession selectSession(long id, long userId) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        String query = "SELECT " + FIELDS + " FROM assembly_sessions WHERE id = ? AND user_id = ?";
        try {
            ps = connection.prepareStatement(query);
            ps.setLong(1, id);
            ps.setLong(2, userId);
            rs = ps.executeQuery();
            AssemblySession session = null;
            if (rs.next()) {
                session = readSession(connection, rs);
            }
            return session;
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
            pool.freeConnection(connection);
        }
    }

    /** Các phiên của một tài khoản, phiên vừa thao tác gần nhất đứng đầu. */
    public static List<AssemblySession> selectSessions(long userId) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        String query = "SELECT " + FIELDS + " FROM assembly_sessions "
                + "WHERE user_id = ? ORDER BY updated_at DESC, id DESC";
        try {
            ps = connection.prepareStatement(query);
            ps.setLong(1, userId);
            rs = ps.executeQuery();
            List<AssemblySession> sessions = new ArrayList<>();
            while (rs.next()) {
                sessions.add(readSession(connection, rs));
            }
            return sessions;
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
            pool.freeConnection(connection);
        }
    }

    /* Dùng chung cho linh kiện và bước: hai bảng có cùng dạng
       (session_id, robot_id, mã, giá trị true/false). */
    private static int writeProgress(AssemblySession session, String query, String itemId, boolean value)
            throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement(query);
            ps.setLong(1, session.getId());
            ps.setString(2, session.getRobotId());
            ps.setString(3, itemId);
            ps.setBoolean(4, value);
            int count = ps.executeUpdate();
            touch(connection, session.getId());
            return count;
        } finally {
            DBUtil.closePreparedStatement(ps);
            pool.freeConnection(connection);
        }
    }

    /* Cập nhật mốc thời gian của phiên khi tiến độ thay đổi, để trang Tài khoản và
       trang chủ đưa đúng phiên vừa thao tác lên đầu. */
    private static void touch(Connection connection, long sessionId) throws SQLException {
        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement(
                    "UPDATE assembly_sessions SET updated_at = CURRENT_TIMESTAMP WHERE id = ?");
            ps.setLong(1, sessionId);
            ps.executeUpdate();
        } finally {
            DBUtil.closePreparedStatement(ps);
        }
    }

    /** Ghép một dòng assembly_sessions với ba bảng tiến độ thành JavaBean đầy đủ. */
    private static AssemblySession readSession(Connection connection, ResultSet row) throws SQLException {
        AssemblySession session = new AssemblySession();
        session.setId(row.getLong("id"));
        session.setUserId(row.getLong("user_id"));
        session.setRobotId(row.getString("robot_id"));
        session.setStatus(row.getString("status"));
        session.setUpdatedAt(row.getTimestamp("updated_at").toInstant().toString());
        session.setComponents(selectComponents(connection, session.getId()));
        session.setSteps(selectSteps(connection, session.getId()));
        session.setAssembledPartIds(selectVisualParts(connection, session.getId()));
        return session;
    }

    private static List<SessionComponent> selectComponents(Connection connection, long sessionId)
            throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = connection.prepareStatement(
                    "SELECT component_id, prepared FROM session_components WHERE session_id = ?");
            ps.setLong(1, sessionId);
            rs = ps.executeQuery();
            List<SessionComponent> components = new ArrayList<>();
            while (rs.next()) {
                components.add(new SessionComponent(rs.getString("component_id"), rs.getBoolean("prepared")));
            }
            return components;
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
        }
    }

    private static List<SessionStep> selectSteps(Connection connection, long sessionId)
            throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = connection.prepareStatement(
                    "SELECT step_id, completed FROM session_steps WHERE session_id = ?");
            ps.setLong(1, sessionId);
            rs = ps.executeQuery();
            List<SessionStep> steps = new ArrayList<>();
            while (rs.next()) {
                String status = rs.getBoolean("completed") ? SessionStep.COMPLETED : SessionStep.PENDING;
                steps.add(new SessionStep(rs.getString("step_id"), status));
            }
            return steps;
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
        }
    }

    private static List<String> selectVisualParts(Connection connection, long sessionId)
            throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = connection.prepareStatement(
                    "SELECT component_id FROM session_visual_parts WHERE session_id = ?");
            ps.setLong(1, sessionId);
            rs = ps.executeQuery();
            List<String> partIds = new ArrayList<>();
            while (rs.next()) {
                partIds.add(rs.getString("component_id"));
            }
            return partIds;
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
        }
    }
}
