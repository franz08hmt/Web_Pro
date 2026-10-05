package vn.edu.webpro.robotlab.data;

import vn.edu.webpro.robotlab.business.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/** Đọc tình huống và ghi từng bước chẩn đoán bằng transaction có kiểm sở hữu. */
public class DiagnosisDB {
    private static DiagnosisScenario scenario(Connection connection, long id) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = connection.prepareStatement("SELECT d.*, r.name AS robot_name FROM diagnosis_scenarios d "
                    + "JOIN robots r ON r.id=d.robot_id WHERE d.id = ?");
            ps.setLong(1, id);
            rs = ps.executeQuery();
            if (!rs.next()) {
                return null;
            }
            DiagnosisScenario value = new DiagnosisScenario();
            value.setId(id);
            value.setRobotId(rs.getString("robot_id"));
            value.setRobotName(rs.getString("robot_name"));
            value.setTitle(rs.getString("title"));
            value.setContextText(rs.getString("context_text"));
            value.setSymptomText(rs.getString("symptom_text"));
            value.setGuideId(rs.getString("guide_id"));
            value.setExplanationText(rs.getString("explanation_text"));
            value.setState(rs.getString("state"));
            value.setVersionNo(rs.getInt("version_no"));
            value.setParentScenarioId(rs.getLong("parent_scenario_id"));
            value.setCreatedBy(rs.getLong("created_by"));
            value.setCreatedAt(PracticeTaskDB.date(rs, "created_at"));
            value.setPublishedAt(PracticeTaskDB.date(rs, "published_at"));
            DBUtil.closeResultSet(rs);
            rs = null;
            DBUtil.closePreparedStatement(ps);
            ps = null;
            children(connection, value);
            return value;
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
        }
    }

    private static void children(Connection connection, DiagnosisScenario value) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = connection.prepareStatement("SELECT * FROM diagnosis_checks WHERE scenario_id = ? "
                    + "ORDER BY display_order, id");
            ps.setLong(1, value.getId());
            rs = ps.executeQuery();
            while (rs.next()) {
                DiagnosisCheck check = new DiagnosisCheck();
                check.setId(rs.getLong("id"));
                check.setScenarioId(value.getId());
                check.setLabel(rs.getString("label"));
                check.setObservationText(rs.getString("observation_text"));
                check.setRequired(rs.getBoolean("is_required"));
                check.setDisplayOrder(rs.getInt("display_order"));
                value.getChecks().add(check);
            }
            DBUtil.closeResultSet(rs);
            rs = null;
            DBUtil.closePreparedStatement(ps);
            ps = connection.prepareStatement("SELECT * FROM diagnosis_options WHERE scenario_id = ? "
                    + "ORDER BY kind, display_order, id");
            ps.setLong(1, value.getId());
            rs = ps.executeQuery();
            while (rs.next()) {
                DiagnosisOption option = new DiagnosisOption();
                option.setId(rs.getLong("id"));
                option.setScenarioId(value.getId());
                option.setKind(rs.getString("kind"));
                option.setLabel(rs.getString("label"));
                option.setFeedbackText(rs.getString("feedback_text"));
                option.setCorrect(rs.getBoolean("is_correct"));
                option.setDisplayOrder(rs.getInt("display_order"));
                value.getOptions().add(option);
            }
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
        }
    }

    /** Đọc tình huống; User chỉ được mở bản đang công bố. */
    public static DiagnosisScenario selectScenario(long id, boolean admin) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        try {
            DiagnosisScenario value = scenario(connection, id);
            if (value != null && !admin && !value.isPublished()) {
                return null;
            }
            return value;
        } finally {
            pool.freeConnection(connection);
        }
    }

    /** Danh sách tình huống; bản lưu trữ không xuất hiện để luyện hoặc giao mới. */
    public static List<DiagnosisScenario> selectScenarios(boolean admin) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String sql = "SELECT id FROM diagnosis_scenarios WHERE state='PUBLISHED' ORDER BY id";
            if (admin) {
                sql = "SELECT id FROM diagnosis_scenarios ORDER BY id";
            }
            ps = connection.prepareStatement(sql);
            rs = ps.executeQuery();
            List<Long> ids = new ArrayList<>();
            while (rs.next()) {
                ids.add(rs.getLong("id"));
            }
            DBUtil.closeResultSet(rs);
            rs = null;
            DBUtil.closePreparedStatement(ps);
            ps = null;
            List<DiagnosisScenario> list = new ArrayList<>();
            for (Long id : ids) {
                list.add(scenario(connection, id));
            }
            return list;
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
            pool.freeConnection(connection);
        }
    }

    private static DiagnosisAttempt attempt(Connection connection, long id, long owner, boolean lock)
            throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String sql = "SELECT * FROM diagnosis_attempts WHERE id = ? AND user_id = ?";
            if (lock) {
                sql += " FOR UPDATE";
            }
            ps = connection.prepareStatement(sql);
            PracticeTaskDB.bind(ps, id, owner);
            rs = ps.executeQuery();
            if (!rs.next()) {
                return null;
            }
            DiagnosisAttempt value = new DiagnosisAttempt();
            value.setId(id);
            value.setUserId(owner);
            value.setScenarioId(rs.getLong("scenario_id"));
            value.setMode(rs.getString("mode"));
            value.setState(rs.getString("state"));
            value.setStartedAt(PracticeTaskDB.date(rs, "started_at"));
            value.setSubmittedAt(PracticeTaskDB.date(rs, "submitted_at"));
            value.setCauseOptionId(rs.getLong("cause_option_id"));
            value.setActionOptionId(rs.getLong("action_option_id"));
            value.setRequiredDone(rs.getInt("required_done"));
            value.setRequiredTotal(rs.getInt("required_total"));
            value.setCauseCorrect(rs.getBoolean("cause_correct"));
            value.setActionCorrect(rs.getBoolean("action_correct"));
            DBUtil.closeResultSet(rs);
            rs = null;
            DBUtil.closePreparedStatement(ps);
            ps = null;
            value.setScenario(scenario(connection, value.getScenarioId()));
            chosenChecks(connection, value);
            return value;
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
        }
    }

    private static void chosenChecks(Connection connection, DiagnosisAttempt value) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = connection.prepareStatement("SELECT check_id, chosen_at FROM diagnosis_attempt_checks "
                    + "WHERE attempt_id = ? ORDER BY id");
            ps.setLong(1, value.getId());
            rs = ps.executeQuery();
            while (rs.next()) {
                for (DiagnosisCheck check : value.getScenario().getChecks()) {
                    if (check.getId() == rs.getLong("check_id")) {
                        check.setChosenAt(PracticeTaskDB.date(rs, "chosen_at"));
                    }
                }
            }
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
        }
    }

    /** Đọc dữ kiện chẩn đoán bằng kết nối hiện tại, không khóa dòng lượt khi nộp bài. */
    static DiagnosisAttempt evidence(Connection connection, long id, long owner) throws SQLException {
        return attempt(connection, id, owner, false);
    }

    /** Đọc lượt của chính User và loại bỏ dữ kiện chưa được phép xem. */
    public static DiagnosisAttempt selectAttempt(long id, long owner) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        try {
            DiagnosisAttempt value = attempt(connection, id, owner, false);
            if (value != null && !value.isSubmitted()) {
                hideAnswers(value);
            }
            return value;
        } finally {
            pool.freeConnection(connection);
        }
    }

    private static void hideAnswers(DiagnosisAttempt value) {
        DiagnosisScenario scenario = value.getScenario();
        scenario.setExplanationText("");
        for (DiagnosisCheck check : scenario.getChecks()) {
            if (!check.isChosen()) {
                check.setObservationText("");
            }
            check.setRequired(false);
        }
        for (DiagnosisOption option : scenario.getOptions()) {
            option.setCorrect(false);
            option.setFeedbackText("");
        }
    }

    /** Danh sách lượt đang làm dở của chính User. */
    public static List<DiagnosisAttempt> selectInProgress(long owner) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = connection.prepareStatement("SELECT id FROM diagnosis_attempts WHERE user_id = ? "
                    + "AND state='IN_PROGRESS' ORDER BY id DESC");
            ps.setLong(1, owner);
            rs = ps.executeQuery();
            List<Long> ids = new ArrayList<>();
            while (rs.next()) {
                ids.add(rs.getLong("id"));
            }
            DBUtil.closeResultSet(rs);
            rs = null;
            DBUtil.closePreparedStatement(ps);
            ps = null;
            List<DiagnosisAttempt> list = new ArrayList<>();
            for (Long id : ids) {
                DiagnosisAttempt value = attempt(connection, id, owner, false);
                hideAnswers(value);
                list.add(value);
            }
            return list;
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
            pool.freeConnection(connection);
        }
    }

    /** Tạo lượt luyện chỉ từ tình huống đang công bố. */
    public static long startPractice(long scenarioId, long owner) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        boolean original = connection.getAutoCommit();
        try {
            connection.setAutoCommit(false);
            long exists = PracticeTaskDB.scalar(connection,
                    "SELECT id FROM diagnosis_scenarios WHERE id=? AND state='PUBLISHED' FOR UPDATE", scenarioId);
            if (exists == 0) {
                return 0;
            }
            long id = createAttempt(connection, scenarioId, owner, "PRACTICE");
            connection.commit();
            return id;
        } catch (SQLException | RuntimeException e) {
            connection.rollback();
            throw e;
        } finally {
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
    }

    private static long createAttempt(Connection connection, long scenarioId, long owner, String mode)
            throws SQLException {
        return PracticeTaskDB.change(connection,
                "INSERT INTO diagnosis_attempts(scenario_id,user_id,mode,started_at) VALUES (?,?,?,?)",
                scenarioId, owner, mode, new Date());
    }

    /** Gắn ngay lượt TASK trong thứ tự khóa task, recipient, round; bấm lặp trả lượt cũ. */
    public static long startTask(long taskId, long owner) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        boolean original = connection.getAutoCommit();
        try {
            connection.setAutoCommit(false);
            PracticeTask task = PracticeTaskDB.task(connection, taskId, true);
            TaskRecipient recipient = PracticeTaskDB.recipient(connection, taskId, owner, true);
            if (task == null || recipient == null) {
                return 0;
            }
            TaskRound round = PracticeTaskDB.round(connection, recipient.getId(), 0, true);
            recipient.setTask(task);
            recipient.setCurrentRound(round);
            recipient.setLatestSubmission(TaskSubmissionDB.latest(connection, recipient.getId()));
            recipient.setSubmissionCount((int) PracticeTaskDB.scalar(connection,
                    "SELECT COUNT(*) FROM task_submissions WHERE recipient_id=?", recipient.getId()));
            if (!recipient.isCanStartDiagnosis()) {
                throw new IllegalArgumentException("Vòng này không được bắt đầu chẩn đoán.");
            }
            long id = round.getDiagnosisAttemptId();
            if (id == 0) {
                id = createAttempt(connection, task.getDiagnosisScenarioId(), owner, "TASK");
                PracticeTaskDB.change(connection, "UPDATE task_rounds SET diagnosis_attempt_id=? WHERE id=?",
                        id, round.getId());
            }
            connection.commit();
            return id;
        } catch (SQLException | RuntimeException e) {
            connection.rollback();
            throw e;
        } finally {
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
    }

    /** Chọn phép kiểm tra một lần và ghi quan sát bằng nhật ký server. */
    public static boolean check(long id, long owner, long checkId) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        boolean original = connection.getAutoCommit();
        try {
            connection.setAutoCommit(false);
            DiagnosisAttempt value = attempt(connection, id, owner, true);
            if (value == null) {
                return false;
            }
            if (!value.canConclude()) {
                throw new IllegalArgumentException("Lượt đã kết luận.");
            }
            boolean found = false;
            for (DiagnosisCheck check : value.getScenario().getChecks()) {
                if (check.getId() == checkId) {
                    found = true;
                }
            }
            if (!found) {
                throw new IllegalArgumentException("Phép kiểm tra không thuộc tình huống.");
            }
            PracticeTaskDB.change(connection,
                    "INSERT IGNORE INTO diagnosis_attempt_checks(attempt_id,check_id,chosen_at) VALUES (?,?,?)",
                    id, checkId, new Date());
            connection.commit();
            return true;
        } catch (SQLException | RuntimeException e) {
            connection.rollback();
            throw e;
        } finally {
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
    }

    /** Chốt đúng một lần; chỉ khóa lượt của mình, không khóa task hoặc recipient. */
    public static boolean conclude(long id, long owner, long causeId, long actionId) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        boolean original = connection.getAutoCommit();
        try {
            connection.setAutoCommit(false);
            DiagnosisAttempt value = attempt(connection, id, owner, true);
            if (value == null) {
                return false;
            }
            value.conclude(causeId, actionId);
            PracticeTaskDB.change(connection,
                    "UPDATE diagnosis_attempts SET state='SUBMITTED', submitted_at=?, cause_option_id=?, "
                            + "action_option_id=?, required_done=?, required_total=?, cause_correct=?, "
                            + "action_correct=? WHERE id=?",
                    new Date(), causeId, actionId, value.getRequiredDone(), value.getRequiredTotal(),
                    value.isCauseCorrect(), value.isActionCorrect(), id);
            connection.commit();
            return true;
        } catch (SQLException | RuntimeException e) {
            connection.rollback();
            throw e;
        } finally {
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
    }

    private static void validateCatalog(Connection connection, DiagnosisScenario value) throws SQLException {
        boolean robotExists = PracticeTaskDB.scalar(connection,
                "SELECT COUNT(*) FROM robots WHERE id=?", value.getRobotId()) > 0;
        boolean compatible = value.getGuideId() == null || value.getGuideId().isEmpty();
        if (!compatible) {
            compatible = PracticeTaskDB.scalar(connection,
                    "SELECT COUNT(*) FROM troubleshooting_guides WHERE id=? AND (robot_id=? OR robot_id IS NULL)",
                    value.getGuideId(), value.getRobotId()) > 0;
        }
        value.validatePublication(robotExists, compatible);
    }

    private static Object nullable(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        return value;
    }

    private static void saveChildren(Connection connection, DiagnosisScenario value) throws SQLException {
        for (DiagnosisCheck check : value.getChecks()) {
            PracticeTaskDB.change(connection,
                    "INSERT INTO diagnosis_checks(scenario_id,label,observation_text,is_required,display_order) "
                            + "VALUES (?,?,?,?,?)",
                    value.getId(), check.getLabel(), check.getObservationText(), check.isRequired(),
                    check.getDisplayOrder());
        }
        for (DiagnosisOption option : value.getOptions()) {
            PracticeTaskDB.change(connection,
                    "INSERT INTO diagnosis_options(scenario_id,kind,label,feedback_text,is_correct,display_order) "
                            + "VALUES (?,?,?,?,?,?)",
                    value.getId(), option.getKind(), option.getLabel(), option.getFeedbackText(),
                    option.isCorrect(), option.getDisplayOrder());
        }
    }

    /** Lưu nháp; không cho sửa nội dung bản đã công bố hoặc lưu trữ. */
    public static long saveDraft(DiagnosisScenario value, long admin) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        boolean original = connection.getAutoCommit();
        try {
            connection.setAutoCommit(false);
            value.validateDraft();
            if (PracticeTaskDB.scalar(connection, "SELECT COUNT(*) FROM robots WHERE id=?", value.getRobotId()) == 0) {
                throw new IllegalArgumentException("Robot không hợp lệ.");
            }
            if (value.getGuideId() != null && !value.getGuideId().isEmpty()
                    && PracticeTaskDB.scalar(connection,
                            "SELECT COUNT(*) FROM troubleshooting_guides WHERE id=? "
                                    + "AND (robot_id=? OR robot_id IS NULL)",
                            value.getGuideId(), value.getRobotId()) == 0) {
                throw new IllegalArgumentException("Hướng dẫn không phù hợp với robot.");
            }
            if (value.getId() > 0) {
                PracticeTaskDB.scalar(connection, "SELECT id FROM diagnosis_scenarios WHERE id=? FOR UPDATE",
                        value.getId());
                DiagnosisScenario old = scenario(connection, value.getId());
                if (old == null) {
                    return 0;
                }
                old.requireDraft();
                PracticeTaskDB.change(connection,
                        "UPDATE diagnosis_scenarios SET robot_id=?,title=?,context_text=?,symptom_text=?, "
                                + "guide_id=?,explanation_text=? WHERE id=?",
                        value.getRobotId(), value.getTitle(), value.getContextText(), value.getSymptomText(),
                        nullable(value.getGuideId()), value.getExplanationText(), value.getId());
                PracticeTaskDB.change(connection, "DELETE FROM diagnosis_checks WHERE scenario_id=?", value.getId());
                PracticeTaskDB.change(connection, "DELETE FROM diagnosis_options WHERE scenario_id=?", value.getId());
            } else {
                value.setId(PracticeTaskDB.change(connection,
                        "INSERT INTO diagnosis_scenarios(robot_id,title,context_text,symptom_text,guide_id, "
                                + "explanation_text,created_by,created_at) VALUES (?,?,?,?,?,?,?,?)",
                        value.getRobotId(), value.getTitle(), value.getContextText(), value.getSymptomText(),
                        nullable(value.getGuideId()), value.getExplanationText(), admin, new Date()));
            }
            saveChildren(connection, value);
            connection.commit();
            return value.getId();
        } catch (SQLException | RuntimeException e) {
            connection.rollback();
            throw e;
        } finally {
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
    }

    /** Công bố, lưu trữ hoặc nhân bản thành nháp mới với phiên bản kế tiếp. */
    public static long manage(long id, long admin, String action) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        boolean original = connection.getAutoCommit();
        try {
            connection.setAutoCommit(false);
            PracticeTaskDB.scalar(connection, "SELECT id FROM diagnosis_scenarios WHERE id=? FOR UPDATE", id);
            DiagnosisScenario value = scenario(connection, id);
            if (value == null) {
                return 0;
            }
            if ("publish".equals(action)) {
                value.requireDraft();
                validateCatalog(connection, value);
                PracticeTaskDB.change(connection,
                        "UPDATE diagnosis_scenarios SET state='PUBLISHED',published_at=? WHERE id=?", new Date(), id);
            } else if ("archive".equals(action)) {
                if (!value.isPublished()) {
                    throw new IllegalArgumentException("Chỉ lưu trữ bản đang công bố.");
                }
                PracticeTaskDB.change(connection, "UPDATE diagnosis_scenarios SET state='ARCHIVED' WHERE id=?", id);
            } else if ("duplicate".equals(action)) {
                long newId = PracticeTaskDB.change(connection,
                        "INSERT INTO diagnosis_scenarios(robot_id,title,context_text,symptom_text,guide_id, "
                                + "explanation_text,version_no,parent_scenario_id,created_by,created_at) "
                                + "VALUES (?,?,?,?,?,?,?,?,?,?)",
                        value.getRobotId(), value.getTitle(), value.getContextText(), value.getSymptomText(),
                        nullable(value.getGuideId()), value.getExplanationText(), value.getVersionNo() + 1,
                        id, admin, new Date());
                value.setId(newId);
                saveChildren(connection, value);
                id = newId;
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
                connection.rollback();
            } finally {
                try {
                    connection.setAutoCommit(original);
                } finally {
                    pool.freeConnection(connection);
                }
            }
        }
    }

    /** Quá trình chẩn đoán chỉ đọc qua bài nộp được phép xem. */
    public static DiagnosisAttempt selectForSubmission(long submissionId, long owner, boolean admin)
            throws SQLException {
        TaskSubmission submission = TaskSubmissionDB.selectSubmission(submissionId, owner, admin);
        if (submission == null || !submission.isHasDiagnosis()) {
            return null;
        }
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        try {
            long actualOwner = PracticeTaskDB.scalar(connection,
                    "SELECT user_id FROM task_recipients WHERE id=?", submission.getRecipientId());
            return attempt(connection, submission.getDiagnosisAttemptId(), actualOwner, false);
        } finally {
            pool.freeConnection(connection);
        }
    }
}
