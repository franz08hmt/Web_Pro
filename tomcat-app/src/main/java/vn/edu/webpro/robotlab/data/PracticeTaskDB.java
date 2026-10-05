package vn.edu.webpro.robotlab.data;

import vn.edu.webpro.robotlab.business.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/** JDBC theo Ch12; mọi ghi nhiệm vụ khóa task trước recipient rồi round (Ch13). */
public class PracticeTaskDB {

    static void bind(PreparedStatement ps, Object... values) throws SQLException {
        for (int i = 0; i < values.length; i++) {
            Object value = values[i];
            if (value instanceof Date) {
                ps.setTimestamp(i + 1, new Timestamp(((Date) value).getTime()));
            } else {
                ps.setObject(i + 1, value);
            }
        }
    }

    static long change(Connection connection, String sql, Object... values) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
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

    static long scalar(Connection connection, String sql, Object... values) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = connection.prepareStatement(sql);
            bind(ps, values);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getLong(1);
            }
            return 0;
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
        }
    }

    static Date date(ResultSet rs, String name) throws SQLException {
        Timestamp value = rs.getTimestamp(name);
        if (value == null) {
            return null;
        }
        return new Date(value.getTime());
    }

    static PracticeTask mapTask(ResultSet rs) throws SQLException {
        PracticeTask task = new PracticeTask();
        task.setId(rs.getLong("id"));
        task.setCreatorId(rs.getLong("creator_id"));
        task.setTitle(rs.getString("title"));
        task.setDescription(rs.getString("description"));
        task.setRobotId(rs.getString("robot_id"));
        task.setRobotName(rs.getString("robot_name"));
        task.setDueAt(date(rs, "due_at"));
        task.setCreatedAt(date(rs, "created_at"));
        task.setPublishedAt(date(rs, "published_at"));
        task.setState(rs.getString("state"));
        task.setLatePolicy(rs.getString("late_policy"));
        task.setMaxSubmissions(rs.getInt("max_submissions"));
        task.setPassThreshold(rs.getInt("pass_threshold"));
        task.setAllowPriorEvidence(rs.getBoolean("allow_prior_evidence"));
        task.setRubricTemplate(rs.getString("rubric_template"));
        task.setDiagnosisScenarioId(rs.getLong("diagnosis_scenario_id"));
        return task;
    }

    static PracticeTask task(Connection connection, long id, boolean lock) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String sql =
                    "SELECT t.*, r.name AS robot_name FROM practice_tasks t JOIN "
                            + "robots r ON r.id=t.robot_id WHERE t.id = ?";
            if (lock) {
                sql += " FOR UPDATE";
            }
            ps = connection.prepareStatement(sql);
            ps.setLong(1, id);
            rs = ps.executeQuery();
            if (rs.next()) {
                return mapTask(rs);
            }
            return null;
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
        }
    }

    /** Đọc nhiệm vụ theo quyền ADMIN hoặc người được giao. */
    public static PracticeTask selectTask(long id, long userId, boolean admin) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        try {
            PracticeTask task = task(connection, id, false);
            if (!admin
                    && (task == null
                            || task.isDraft()
                            || scalar(
                                            connection,
                                            "SELECT id FROM task_recipients WHERE task_id = ? AND"
                                                + " user_id = ?",
                                            id,
                                            userId)
                                    == 0)) {
                return null;
            }
            return task;
        } finally {
            pool.freeConnection(connection);
        }
    }

    /** Đọc danh sách nhiệm vụ trong phạm vi tài khoản. */
    public static List<PracticeTask> selectTasks(long userId, boolean admin, String state)
            throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String sql =
                    "SELECT t.*, r.name AS robot_name FROM practice_tasks t JOIN "
                            + "robots r ON r.id=t.robot_id";
            if (!admin) {
                sql += " JOIN task_recipients tr ON tr.task_id=t.id AND tr.user_id = ?";
            }
            sql += " WHERE 1=1";
            if (!admin) {
                sql += " AND t.state <> 'DRAFT'";
            }
            if (state == null || state.isEmpty()) {
                sql += " AND t.state <> 'ARCHIVED'";
            } else {
                sql += " AND t.state = ?";
            }
            sql += " ORDER BY t.id DESC";
            ps = connection.prepareStatement(sql);
            int p = 1;
            if (!admin) {
                ps.setLong(p++, userId);
            }
            if (state != null && !state.isEmpty()) {
                ps.setString(p, state);
            }
            rs = ps.executeQuery();
            List<PracticeTask> tasks = new ArrayList<>();
            while (rs.next()) {
                tasks.add(mapTask(rs));
            }
            return tasks;
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
            pool.freeConnection(connection);
        }
    }

    static TaskRound round(Connection connection, long recipientId, int roundNo, boolean lock)
            throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String sql = "SELECT * FROM task_rounds WHERE recipient_id = ?";
            if (roundNo > 0) {
                sql += " AND round_no = ?";
            }
            sql += " ORDER BY round_no DESC LIMIT 1";
            if (lock) {
                sql += " FOR UPDATE";
            }
            ps = connection.prepareStatement(sql);
            ps.setLong(1, recipientId);
            if (roundNo > 0) {
                ps.setInt(2, roundNo);
            }
            rs = ps.executeQuery();
            if (!rs.next()) {
                return null;
            }
            TaskRound round = new TaskRound();
            round.setId(rs.getLong("id"));
            round.setRecipientId(recipientId);
            round.setRoundNo(rs.getInt("round_no"));
            round.setState(rs.getString("state"));
            round.setStartedAt(date(rs, "started_at"));
            round.setQuizAttemptId(rs.getLong("quiz_attempt_id"));
            round.setDiagnosisAttemptId(rs.getLong("diagnosis_attempt_id"));
            return round;
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
        }
    }

    static TaskRecipient recipient(Connection connection, long taskId, long userId, boolean lock)
            throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String sql =
                    "SELECT tr.*, u.display_name FROM task_recipients tr JOIN users u "
                            + "ON u.id=tr.user_id WHERE tr.task_id = ? AND tr.user_id = ?";
            if (lock) {
                sql += " FOR UPDATE";
            }
            ps = connection.prepareStatement(sql);
            bind(ps, taskId, userId);
            rs = ps.executeQuery();
            if (!rs.next()) {
                return null;
            }
            TaskRecipient recipient = new TaskRecipient();
            recipient.setId(rs.getLong("id"));
            recipient.setTaskId(taskId);
            recipient.setUserId(userId);
            recipient.setFullName(rs.getString("display_name"));
            return recipient;
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
        }
    }

    static void hydrate(Connection connection, TaskRecipient recipient, PracticeTask task)
            throws SQLException {
        recipient.setTask(task);
        recipient.setCurrentRound(round(connection, recipient.getId(), 0, false));
        recipient.setSubmissionCount(
                (int)
                        scalar(
                                connection,
                                "SELECT COUNT(*) FROM task_submissions WHERE recipient_id = ?",
                                recipient.getId()));
        recipient.setLatestSubmission(TaskSubmissionDB.latest(connection, recipient.getId()));
    }

    /** Đọc người được giao cùng vòng và bài nộp mới nhất. */
    public static List<TaskRecipient> selectRecipients(long taskId, long userId, boolean admin)
            throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String sql =
                    "SELECT tr.*, u.display_name FROM task_recipients tr JOIN users u "
                            + "ON u.id=tr.user_id WHERE tr.task_id = ?";
            if (!admin) {
                sql += " AND tr.user_id = ?";
            }
            sql += " ORDER BY u.display_name, tr.id";
            ps = connection.prepareStatement(sql);
            ps.setLong(1, taskId);
            if (!admin) {
                ps.setLong(2, userId);
            }
            rs = ps.executeQuery();
            List<TaskRecipient> list = new ArrayList<>();
            while (rs.next()) {
                TaskRecipient item = new TaskRecipient();
                item.setId(rs.getLong("id"));
                item.setTaskId(taskId);
                item.setUserId(rs.getLong("user_id"));
                item.setFullName(rs.getString("display_name"));
                list.add(item);
            }
            DBUtil.closeResultSet(rs);
            rs = null;
            DBUtil.closePreparedStatement(ps);
            ps = null;
            PracticeTask task = task(connection, taskId, false);
            for (TaskRecipient item : list) {
                hydrate(connection, item, task);
            }
            return list;
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
            pool.freeConnection(connection);
        }
    }

    private static Object scenarioId(PracticeTask task) {
        if (task.isRubricB()) {
            return task.getDiagnosisScenarioId();
        }
        return null;
    }

    static void validateCatalog(Connection connection, PracticeTask task) throws SQLException {
        task.validate(
                scalar(connection, "SELECT COUNT(*) FROM robots WHERE id = ?", task.getRobotId())
                        > 0,
                scalar(
                                connection,
                                "SELECT COUNT(*) FROM quiz_questions WHERE robot_id = ?",
                                task.getRobotId())
                        > 0);
        if (task.isRubricB() && scalar(connection,
                "SELECT COUNT(*) FROM diagnosis_scenarios WHERE id=? AND robot_id=? AND state='PUBLISHED'",
                task.getDiagnosisScenarioId(), task.getRobotId()) == 0) {
            throw new IllegalArgumentException("Mẫu B cần tình huống đang công bố cùng robot.");
        }
    }

    static void addRecipients(Connection connection, PracticeTask task, long[] userIds, Date now)
            throws SQLException {
        for (long userId : userIds) {
            if (scalar(
                            connection,
                            "SELECT id FROM users WHERE id = ? AND role = 'user' FOR UPDATE",
                            userId)
                    == 0) {
                throw new IllegalArgumentException("Chỉ được giao cho tài khoản USER.");
            }
            if (scalar(
                            connection,
                            "SELECT id FROM task_recipients WHERE task_id = ? AND user_id = ?",
                            task.getId(),
                            userId)
                    == 0) {
                long recipient =
                        change(
                                connection,
                                "INSERT INTO task_recipients(task_id,user_id) VALUES (?,?)",
                                task.getId(),
                                userId);
                if (task.isOpen()) {
                    change(
                            connection,
                            "INSERT INTO task_rounds(recipient_id,round_no,started_at) VALUES"
                                + " (?,1,?)",
                            recipient,
                            now);
                }
            }
        }
    }

    /** Lưu nháp và người được giao trong cùng transaction. */
    public static long saveDraft(PracticeTask input, long adminId, long[] recipients)
            throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        boolean original = connection.getAutoCommit();
        try {
            connection.setAutoCommit(false);
            Date now = new Date();
            validateCatalog(connection, input);
            if (input.getId() > 0) {
                PracticeTask old = task(connection, input.getId(), true);
                if (old == null) {
                    throw new IllegalArgumentException("Không tìm thấy nhiệm vụ.");
                }
                old.requireDraft();
                change(
                        connection,
                        "UPDATE practice_tasks SET title=?,description=?,robot_id=?,"
                                + "due_at=?,late_policy=?,max_submissions=?,pass_threshold=?,"
                                + "allow_prior_evidence=?,rubric_template=?,diagnosis_scenario_id=? WHERE id=?",
                        input.getTitle(),
                        input.getDescription(),
                        input.getRobotId(),
                        input.getDueAt(),
                        input.getLatePolicy(),
                        input.getMaxSubmissions(),
                        input.getPassThreshold(),
                        input.isAllowPriorEvidence(),
                        input.getRubricTemplate(),
                        scenarioId(input),
                        input.getId());
                change(connection, "DELETE FROM task_recipients WHERE task_id = ?", input.getId());
            } else {
                input.setId(
                        change(
                                connection,
                                "INSERT INTO practice_tasks(creator_id,title,description,robot_id,"
                                    + "due_at,late_policy,max_submissions,"
                                    + "pass_threshold,allow_prior_evidence,rubric_template,"
                                    + "diagnosis_scenario_id,created_at)"
                                    + " VALUES (?,?,?,?,?,?,?,?,?,?,?,?)",
                                adminId,
                                input.getTitle(),
                                input.getDescription(),
                                input.getRobotId(),
                                input.getDueAt(),
                                input.getLatePolicy(),
                                input.getMaxSubmissions(),
                                input.getPassThreshold(),
                                input.isAllowPriorEvidence(),
                                input.getRubricTemplate(),
                                scenarioId(input),
                                now));
            }
            input.setState("DRAFT");
            addRecipients(connection, input, recipients, now);
            connection.commit();
            return input.getId();
        } catch (SQLException | RuntimeException e) {
            connection.rollback();
            throw e;
        } finally {
            try {
                connection.setAutoCommit(original);
            } finally {
                pool.freeConnection(connection);
            }
        }
    }

    /** Thực hiện thao tác quản lý với khóa và kiểm trạng thái. */
    public static long manage(long id, String action, long adminId, long[] recipients, Date due)
            throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        boolean original = connection.getAutoCommit();
        try {
            connection.setAutoCommit(false);
            PracticeTask task = task(connection, id, true);
            Date now = new Date();
            if (task == null) {
                throw new IllegalArgumentException("Không tìm thấy nhiệm vụ.");
            }
            if ("publish".equals(action)) {
                validateCatalog(connection, task);
                int count =
                        (int)
                                scalar(
                                        connection,
                                        "SELECT COUNT(*) FROM task_recipients WHERE task_id = ?",
                                        id);
                task.requirePublish(count, now);
                if (scalar(
                                connection,
                                "SELECT COUNT(*) FROM task_recipients tr JOIN users u ON "
                                        + "u.id=tr.user_id WHERE tr.task_id=? AND u.role <> 'user'",
                                id)
                        > 0) {
                    throw new IllegalArgumentException(
                            "Người được giao phải giữ role USER khi công bố.");
                }
                change(
                        connection,
                        "UPDATE practice_tasks SET state='OPEN',published_at=? WHERE id=?",
                        now,
                        id);
                change(
                        connection,
                        "INSERT INTO task_rounds(recipient_id,round_no,started_at) SELECT "
                                + "id,1,? FROM task_recipients WHERE task_id=?",
                        now,
                        id);
            } else if ("addRecipients".equals(action)) {
                task.requireOpen();
                addRecipients(connection, task, recipients, now);
            } else if ("extend".equals(action)) {
                task.requireExtension(due, now);
                change(connection, "UPDATE practice_tasks SET due_at=? WHERE id=?", due, id);
            } else if ("close".equals(action)) {
                task.requireOpen();
                change(connection, "UPDATE practice_tasks SET state='CLOSED' WHERE id=?", id);
            } else if ("archive".equals(action)) {
                task.requireClosed();
                change(connection, "UPDATE practice_tasks SET state='ARCHIVED' WHERE id=?", id);
            } else if ("duplicate".equals(action)) {
                id =
                        change(
                                connection,
                                "INSERT INTO practice_tasks(creator_id,title,description,robot_id,"
                                    + "due_at,late_policy,max_submissions,"
                                    + "pass_threshold,allow_prior_evidence,rubric_template,"
                                    + "diagnosis_scenario_id,created_at)"
                                    + " VALUES (?,?,?,?,?,?,?,?,?,?,?,?)",
                                adminId,
                                task.getTitle(),
                                task.getDescription(),
                                task.getRobotId(),
                                task.getDueAt(),
                                task.getLatePolicy(),
                                task.getMaxSubmissions(),
                                task.getPassThreshold(),
                                task.isAllowPriorEvidence(),
                                task.getRubricTemplate(),
                                scenarioId(task),
                                now);
            } else if ("deleteDraft".equals(action)) {
                task.requireDraft();
                change(connection, "DELETE FROM task_recipients WHERE task_id=?", id);
                change(connection, "DELETE FROM practice_tasks WHERE id=?", id);
                id = 0;
            } else {
                throw new IllegalArgumentException("Thao tác không hợp lệ.");
            }
            connection.commit();
            return id;
        } catch (SQLException | RuntimeException e) {
            connection.rollback();
            throw e;
        } finally {
            try {
                connection.setAutoCommit(original);
            } finally {
                pool.freeConnection(connection);
            }
        }
    }
}
