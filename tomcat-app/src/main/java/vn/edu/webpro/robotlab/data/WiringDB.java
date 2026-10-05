package vn.edu.webpro.robotlab.data;

import vn.edu.webpro.robotlab.business.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Date;

/** JDBC phòng nối dây; dữ kiện và kết quả được lưu nguyên tử theo luật bean. */
public class WiringDB {

    private static long change(Connection c, String sql, Object... values) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            bind(ps, values);
            int affected = ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getLong(1);
            }
            return affected;
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
        }
    }

    private static void bind(PreparedStatement ps, Object... values) throws SQLException {
        for (int i = 0; i < values.length; i++) {
            if (values[i] instanceof Date) {
                ps.setTimestamp(i + 1, new Timestamp(((Date) values[i]).getTime()));
            } else {
                ps.setObject(i + 1, values[i]);
            }
        }
    }

    private static Date date(ResultSet rs, String name) throws SQLException {
        Timestamp value = rs.getTimestamp(name);
        if (value == null) {
            return null;
        }
        return new Date(value.getTime());
    }

    private static void finish(Connection connection, boolean original, ConnectionPool pool) throws SQLException {
        try {
            connection.rollback();
        } finally {
            try {
                connection.setAutoCommit(original);
            } finally {
                pool.freeConnection(connection);
            }
        }
    }

    private static WiringExercise readExercise(ResultSet rs) throws SQLException {
        WiringExercise e = new WiringExercise();
        e.setId(rs.getLong("id"));
        e.setCode(rs.getString("code"));
        e.setTitle(rs.getString("title"));
        e.setRobotId(rs.getString("robot_id"));
        e.setRobotName(rs.getString("robot_name"));
        e.setObjective(rs.getString("objective"));
        e.setScopeText(rs.getString("scope_text"));
        e.setState(rs.getString("state"));
        e.setCreatedBy(rs.getLong("created_by"));
        e.setCreatedAt(date(rs, "created_at"));
        e.setPublishedAt(date(rs, "published_at"));
        return e;
    }

    private static WiringExercise exercise(Connection c, long id, boolean lock) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String sql = "SELECT e.*, r.name AS robot_name FROM wiring_exercises e "
                    + "JOIN robots r ON r.id=e.robot_id WHERE e.id = ?";
            if (lock) {
                sql += " FOR UPDATE";
            }
            ps = c.prepareStatement(sql);
            ps.setLong(1, id);
            rs = ps.executeQuery();
            if (!rs.next()) {
                return null;
            }
            WiringExercise e = readExercise(rs);
            DBUtil.closeResultSet(rs);
            rs = null;
            DBUtil.closePreparedStatement(ps);
            ps = null;
            children(c, e);
            return e;
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
        }
    }

    private static void children(Connection c, WiringExercise e) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = c.prepareStatement("SELECT * FROM wiring_terminals WHERE exercise_id = ? ORDER BY display_order,id");
            ps.setLong(1, e.getId());
            rs = ps.executeQuery();
            while (rs.next()) {
                WiringTerminal t = new WiringTerminal();
                t.setId(rs.getLong("id"));
                t.setCode(rs.getString("code"));
                t.setDeviceCode(rs.getString("device_code"));
                t.setDeviceLabel(rs.getString("device_label"));
                t.setPinLabel(rs.getString("pin_label"));
                t.setDisplayOrder(rs.getInt("display_order"));
                t.setX(rs.getInt("x"));
                t.setY(rs.getInt("y"));
                e.getTerminals().add(t);
            }
            DBUtil.closeResultSet(rs);
            rs = null;
            DBUtil.closePreparedStatement(ps);
            ps = c.prepareStatement("SELECT * FROM wiring_rules WHERE exercise_id = ? ORDER BY id");
            ps.setLong(1, e.getId());
            rs = ps.executeQuery();
            while (rs.next()) {
                WiringRule r = new WiringRule();
                r.setTerminalA(rs.getLong("terminal_a"));
                r.setTerminalB(rs.getLong("terminal_b"));
                r.setKind(rs.getString("kind"));
                r.setExplanation(rs.getString("explanation"));
                e.getRules().add(r);
            }
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
        }
    }

    /** Catalog chỉ đọc metadata bằng một query, không tải từng bài N+1. */
    public static ArrayList<WiringExercise> selectExercises(boolean admin) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection c = pool.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String sql = "SELECT e.*, r.name AS robot_name FROM wiring_exercises e "
                    + "JOIN robots r ON r.id=e.robot_id";
            if (!admin) {
                sql += " WHERE e.state='PUBLISHED'";
            }
            ps = c.prepareStatement(sql + " ORDER BY e.id DESC");
            rs = ps.executeQuery();
            ArrayList<WiringExercise> list = new ArrayList<>();
            while (rs.next()) {
                list.add(readExercise(rs));
            }
            return list;
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
            pool.freeConnection(c);
        }
    }

    /** Bài cho USER chỉ đọc khi công bố; ADMIN xem được mọi trạng thái. */
    public static WiringExercise selectExercise(long id, boolean admin) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection c = pool.getConnection();
        try {
            WiringExercise e = exercise(c, id, false);
            if (e != null && !admin && !e.isPublished()) {
                return null;
            }
            return e;
        } finally {
            pool.freeConnection(c);
        }
    }

    private static boolean robotExists(Connection c, String id) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = c.prepareStatement("SELECT id FROM robots WHERE id = ?");
            ps.setString(1, id);
            rs = ps.executeQuery();
            return rs.next();
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
        }
    }

    private static void saveChildren(Connection c, WiringExercise e) throws SQLException {
        HashMap<Long, Long> mapped = new HashMap<>();
        for (WiringTerminal t : e.getTerminals()) {
            long id = change(c, "INSERT INTO wiring_terminals "
                    + "(exercise_id,code,device_code,device_label,pin_label,display_order,x,y) "
                    + "VALUES (?,?,?,?,?,?,?,?)",
                    e.getId(), t.getCode(), t.getDeviceCode(), t.getDeviceLabel(), t.getPinLabel(),
                    t.getDisplayOrder(), t.getX(), t.getY());
            mapped.put(t.getId(), id);
        }
        for (WiringRule r : e.getRules()) {
            WiringRule stored = new WiringRule();
            stored.setTerminalA(mapped.get(r.getTerminalA()));
            stored.setTerminalB(mapped.get(r.getTerminalB()));
            stored.normalize();
            change(c, "INSERT INTO wiring_rules (exercise_id,terminal_a,terminal_b,kind,explanation) "
                    + "VALUES (?,?,?,?,?)", e.getId(), stored.getTerminalA(), stored.getTerminalB(),
                    r.getKind(), r.getExplanation());
        }
    }

    private static long insertExercise(Connection c, WiringExercise e, long admin) throws SQLException {
        long id = change(c, "INSERT INTO wiring_exercises (code,title,robot_id,objective,scope_text,created_by) "
                + "VALUES (?,?,?,?,?,?)", e.getCode(), e.getTitle(), e.getRobotId(),
                e.getObjective(), e.getScopeText(), admin);
        e.setId(id);
        saveChildren(c, e);
        return id;
    }

    /** Lưu toàn bộ nháp; chỉ xóa các đầu nối/quy tắc của nháp chưa từng công bố. */
    public static long saveDraft(WiringExercise e, long admin) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection c = pool.getConnection();
        boolean original = c.getAutoCommit();
        try {
            c.setAutoCommit(false);
            if (e.getId() != 0) {
                WiringExercise stored = exercise(c, e.getId(), true);
                if (stored == null) {
                    return 0;
                }
                stored.requireDraft();
            }
            e.validateDraft(robotExists(c, e.getRobotId()));
            if (e.getId() == 0) {
                insertExercise(c, e, admin);
            } else {
                change(c, "UPDATE wiring_exercises SET code=?,title=?,robot_id=?,objective=?,scope_text=? WHERE id=?",
                        e.getCode(), e.getTitle(), e.getRobotId(), e.getObjective(), e.getScopeText(), e.getId());
                change(c, "DELETE FROM wiring_rules WHERE exercise_id=?", e.getId());
                change(c, "DELETE FROM wiring_terminals WHERE exercise_id=?", e.getId());
                saveChildren(c, e);
            }
            c.commit();
            return e.getId();
        } catch (SQLException error) {
            c.rollback();
            if (error.getErrorCode() == 1062) {
                throw new IllegalArgumentException("Mã bài đã tồn tại; hãy chọn mã khác.");
            }
            throw error;
        } catch (RuntimeException error) {
            c.rollback();
            throw error;
        } finally {
            finish(c, original, pool);
        }
    }

    /** Công bố/nhân bản/lưu trữ trong khóa bài; không sửa bài đã công bố. */
    public static long manage(long id, long admin, String action) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection c = pool.getConnection();
        boolean original = c.getAutoCommit();
        try {
            c.setAutoCommit(false);
            WiringExercise e = exercise(c, id, true);
            if (e == null) {
                return 0;
            }
            if ("publish".equals(action)) {
                e.publish(robotExists(c, e.getRobotId()));
                change(c, "UPDATE wiring_exercises SET state='PUBLISHED',published_at=? WHERE id=?",
                        e.getPublishedAt(), id);
            } else if ("archive".equals(action)) {
                e.archive();
                change(c, "UPDATE wiring_exercises SET state='ARCHIVED' WHERE id=?", id);
            } else if ("duplicate".equals(action)) {
                id = insertExercise(c, e.copyDraft(), admin);
            } else {
                throw new IllegalArgumentException("Thao tác không hợp lệ.");
            }
            c.commit();
            return id;
        } catch (SQLException | RuntimeException error) {
            c.rollback();
            throw error;
        } finally {
            finish(c, original, pool);
        }
    }

    private static WiringAttempt readAttempt(ResultSet rs) throws SQLException {
        WiringAttempt a = new WiringAttempt();
        a.setId(rs.getLong("id"));
        a.setExerciseId(rs.getLong("exercise_id"));
        a.setUserId(rs.getLong("user_id"));
        a.setVersion(rs.getInt("version"));
        a.setState(rs.getString("state"));
        a.setCreatedAt(date(rs, "created_at"));
        a.setUpdatedAt(date(rs, "updated_at"));
        a.setSubmittedAt(date(rs, "submitted_at"));
        if (a.isSubmitted()) {
            WiringGrade g = new WiringGrade();
            g.setRequiredCount(rs.getInt("required_count"));
            g.setCorrectCount(rs.getInt("correct_count"));
            g.setWrongCount(rs.getInt("wrong_count"));
            g.setMissingCount(rs.getInt("missing_count"));
            g.setScore(rs.getBigDecimal("score"));
            a.setGrade(g);
        }
        return a;
    }

    private static WiringAttempt attempt(Connection c, long id, long owner, boolean lock) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String sql = "SELECT * FROM wiring_attempts WHERE id = ? AND user_id = ?";
            if (lock) {
                sql += " FOR UPDATE";
            }
            ps = c.prepareStatement(sql);
            bind(ps, id, owner);
            rs = ps.executeQuery();
            if (!rs.next()) {
                return null;
            }
            return readAttempt(rs);
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
        }
    }

    private static void connections(Connection c, WiringAttempt a) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = c.prepareStatement("SELECT terminal_a,terminal_b FROM wiring_attempt_connections "
                    + "WHERE attempt_id = ? ORDER BY terminal_a,terminal_b");
            ps.setLong(1, a.getId());
            rs = ps.executeQuery();
            while (rs.next()) {
                WiringConnection w = new WiringConnection();
                w.setTerminalA(rs.getLong("terminal_a"));
                w.setTerminalB(rs.getLong("terminal_b"));
                a.getConnections().add(w);
            }
            a.setConnections(a.getExercise().normalize(a.getConnections()));
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
        }
    }

    /** Lượt chỉ được đọc bởi chủ sở hữu; giải thích chỉ dựng sau khi đã nộp. */
    public static WiringAttempt selectAttempt(long id, long owner) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection c = pool.getConnection();
        try {
            WiringAttempt a = attempt(c, id, owner, false);
            if (a != null) {
                a.setExercise(exercise(c, a.getExerciseId(), false));
                connections(c, a);
                if (a.isSubmitted()) {
                    WiringGrade explained = a.getExercise().grade(a.getConnections());
                    a.getGrade().setRows(explained.getRows());
                } else {
                    a.getExercise().getRules().clear();
                }
            }
            return a;
        } finally {
            pool.freeConnection(c);
        }
    }

    /** Lịch sử metadata của chính người dùng bằng một query, không tải con N+1. */
    public static ArrayList<WiringAttempt> selectAttempts(long owner) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection c = pool.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = c.prepareStatement("SELECT a.*,e.title,e.state AS exercise_state,r.name AS robot_name "
                    + "FROM wiring_attempts a JOIN wiring_exercises e ON e.id=a.exercise_id "
                    + "JOIN robots r ON r.id=e.robot_id WHERE a.user_id = ? ORDER BY a.id DESC");
            ps.setLong(1, owner);
            rs = ps.executeQuery();
            ArrayList<WiringAttempt> list = new ArrayList<>();
            while (rs.next()) {
                WiringAttempt a = readAttempt(rs);
                WiringExercise e = new WiringExercise();
                e.setId(a.getExerciseId());
                e.setTitle(rs.getString("title"));
                e.setState(rs.getString("exercise_state"));
                e.setRobotName(rs.getString("robot_name"));
                a.setExercise(e);
                list.add(a);
            }
            return list;
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
            pool.freeConnection(c);
        }
    }

    private static void saveConnections(Connection c, WiringAttempt a) throws SQLException {
        change(c, "DELETE FROM wiring_attempt_connections WHERE attempt_id=?", a.getId());
        for (WiringConnection w : a.getConnections()) {
            change(c, "INSERT INTO wiring_attempt_connections (attempt_id,exercise_id,terminal_a,terminal_b) "
                    + "VALUES (?,?,?,?)", a.getId(), a.getExerciseId(), w.getTerminalA(), w.getTerminalB());
        }
    }

    /** Tạo nháp mới hoặc luyện lại với bản sao dây; chỉ bài còn công bố. */
    public static long start(long exerciseId, long owner, long sourceId, int expectedVersion) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection c = pool.getConnection();
        boolean original = c.getAutoCommit();
        try {
            c.setAutoCommit(false);
            WiringExercise e = exercise(c, exerciseId, true);
            if (e == null || !e.isPublished()) {
                return 0;
            }
            WiringAttempt fresh = new WiringAttempt();
            fresh.setExerciseId(exerciseId);
            fresh.setExercise(e);
            if (sourceId != 0) {
                WiringAttempt source = attempt(c, sourceId, owner, true);
                if (source == null || source.getExerciseId() != exerciseId) {
                    return 0;
                }
                if (!source.isSubmitted() || source.getVersion() != expectedVersion) {
                    throw new IllegalArgumentException("Chỉ luyện lại từ kết quả đã nộp và đúng phiên bản.");
                }
                source.setExercise(e);
                connections(c, source);
                fresh.setConnections(source.getConnections());
            }
            fresh.setId(change(c, "INSERT INTO wiring_attempts(exercise_id,user_id) VALUES (?,?)",
                    exerciseId, owner));
            saveConnections(c, fresh);
            c.commit();
            return fresh.getId();
        } catch (SQLException | RuntimeException error) {
            c.rollback();
            throw error;
        } finally {
            finish(c, original, pool);
        }
    }

    /** Khóa bài rồi lượt, kiểm owner/version; nộp lặp trả kết quả cũ, không ghi payload mới. */
    public static WiringAttempt mutate(long id, long owner, int expectedVersion, String action,
            ArrayList<WiringConnection> input, String removePair) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection c = pool.getConnection();
        boolean original = c.getAutoCommit();
        try {
            c.setAutoCommit(false);
            WiringAttempt metadata = attempt(c, id, owner, false);
            if (metadata == null) {
                return null;
            }
            WiringExercise e = exercise(c, metadata.getExerciseId(), true);
            WiringAttempt a = attempt(c, id, owner, true);
            a.setExercise(e);
            if ("submit".equals(action) && a.isSubmitted()) {
                c.commit();
                return a;
            }
            a.requireEditable(expectedVersion);
            if (e.isDraft()) {
                throw new IllegalArgumentException("Bài chưa công bố.");
            }
            ArrayList<WiringConnection> normalized = e.normalize(input);
            if ("remove".equals(action)) {
                boolean found = false;
                int last = normalized.size() - 1;
                for (int i = last; i >= 0; i--) {
                    if (normalized.get(i).getPairKey().equals(removePair)) {
                        normalized.remove(i);
                        found = true;
                    }
                }
                if (!found) {
                    throw new IllegalArgumentException("Dây cần xóa không có trong sơ đồ.");
                }
            }
            a.setConnections(normalized);
            saveConnections(c, a);
            if ("submit".equals(action)) {
                e.validatePublication(robotExists(c, e.getRobotId()));
                WiringGrade g = e.grade(normalized);
                change(c, "UPDATE wiring_attempts SET state='SUBMITTED',version=version+1,required_count=?, "
                        + "correct_count=?,wrong_count=?,missing_count=?,score=?,submitted_at=?,updated_at=? "
                        + "WHERE id=?",
                        g.getRequiredCount(), g.getCorrectCount(), g.getWrongCount(), g.getMissingCount(),
                        g.getScore(), new Date(), new Date(), id);
            } else {
                change(c, "UPDATE wiring_attempts SET version=version+1,updated_at=? WHERE id=?", new Date(), id);
            }
            c.commit();
            return a;
        } catch (SQLException | RuntimeException error) {
            c.rollback();
            throw error;
        } finally {
            finish(c, original, pool);
        }
    }
}
