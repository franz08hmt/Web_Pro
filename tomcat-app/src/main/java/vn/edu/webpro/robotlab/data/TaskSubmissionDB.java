package vn.edu.webpro.robotlab.data;

import vn.edu.webpro.robotlab.business.*;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/** Nộp/chấm: khóa task, recipient và vòng; snapshot không lấy điểm từ form. */
public class TaskSubmissionDB {

    static TaskSubmission mapSubmission(ResultSet rs) throws SQLException {
        TaskSubmission value = new TaskSubmission();
        value.setId(rs.getLong("id"));
        value.setRecipientId(rs.getLong("recipient_id"));
        value.setRoundId(rs.getLong("round_id"));
        value.setSubmissionNo(rs.getInt("submission_no"));
        value.setSessionId(rs.getLong("session_id"));
        value.setQuizAttemptId(rs.getLong("quiz_attempt_id"));
        value.setRobotName(rs.getString("robot_name"));
        value.setAssemblyCompletedAt(PracticeTaskDB.date(rs, "assembly_completed_at"));
        value.setQuizScore(rs.getInt("quiz_score"));
        value.setQuizTotal(rs.getInt("quiz_total"));
        value.setQuizReusedFrom(rs.getInt("quiz_reused_from"));
        value.setQuizSubmittedAt(PracticeTaskDB.date(rs, "quiz_submitted_at"));
        value.setSubmittedAt(PracticeTaskDB.date(rs, "submitted_at"));
        value.setAssemblyPoints(rs.getBigDecimal("assembly_points"));
        value.setQuizPoints(rs.getBigDecimal("quiz_points"));
        value.setAutomaticPoints(rs.getBigDecimal("automatic_points"));
        value.setLate(rs.getBoolean("is_late"));
        value.setProblem(rs.getString("problem"));
        value.setReasoning(rs.getString("reasoning"));
        value.setImprovement(rs.getString("improvement"));
        value.setState(rs.getString("state"));
        value.setDiagnosisAttemptId(rs.getLong("diagnosis_attempt_id"));
        if (value.isHasDiagnosis()) {
            value.setDiagnosisTitle(rs.getString("diagnosis_title"));
            value.setDiagRequiredDone(rs.getInt("diag_required_done"));
            value.setDiagRequiredTotal(rs.getInt("diag_required_total"));
            value.setDiagCauseCorrect(rs.getBoolean("diag_cause_correct"));
            value.setDiagActionCorrect(rs.getBoolean("diag_action_correct"));
            value.setDiagnosisScore(rs.getBigDecimal("diagnosis_score"));
            value.setDiagnosisPoints(rs.getBigDecimal("diagnosis_points"));
            value.setDiagnosisReusedFrom(rs.getInt("diagnosis_reused_from"));
            value.setDiagnosisSubmittedAt(PracticeTaskDB.date(rs, "diagnosis_submitted_at"));
        }
        return value;
    }

    static void reviews(Connection connection, TaskSubmission submission) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps =
                    connection.prepareStatement(
                            "SELECT r.*, u.display_name AS reviewer_name FROM task_reviews r JOIN"
                                + " users u ON u.id=r.reviewer_id WHERE r.submission_id = ? ORDER"
                                + " BY r.id");
            ps.setLong(1, submission.getId());
            rs = ps.executeQuery();
            while (rs.next()) {
                TaskReview review = new TaskReview();
                review.setId(rs.getLong("id"));
                review.setSubmissionId(submission.getId());
                review.setReviewerId(rs.getLong("reviewer_id"));
                review.setReviewerName(rs.getString("reviewer_name"));
                review.setExplanationLevel(rs.getInt("explanation_level"));
                review.setExplanationPoints(rs.getInt("explanation_points"));
                review.setTotalPoints(rs.getBigDecimal("total_points"));
                review.setConclusion(rs.getString("conclusion"));
                review.setStrengths(rs.getString("strengths"));
                review.setImprovements(rs.getString("improvements"));
                review.setRetryGuidance(rs.getString("retry_guidance"));
                review.setReviewedAt(PracticeTaskDB.date(rs, "reviewed_at"));
                review.setSupersedesReviewId(rs.getLong("supersedes_review_id"));
                review.setChangeReason(rs.getString("change_reason"));
                for (TaskReview older : submission.getReviews()) {
                    if (older.getId() == review.getSupersedesReviewId()) {
                        older.setReplacedAt(review.getReviewedAt());
                        older.setReplacementReason(review.getChangeReason());
                    }
                }
                submission.getReviews().add(review);
            }
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
        }
    }

    static TaskSubmission latest(Connection connection, long recipientId) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps =
                    connection.prepareStatement(
                            "SELECT s.*, "
                            + "(SELECT d.submitted_at FROM diagnosis_attempts d WHERE d.id=s.diagnosis_attempt_id) "
                            + "AS diagnosis_submitted_at, "
                            + "s.id FROM task_submissions s WHERE recipient_id = ? ORDER BY "
                                    + "submission_no DESC LIMIT 1");
            ps.setLong(1, recipientId);
            rs = ps.executeQuery();
            if (!rs.next()) {
                return null;
            }
            TaskSubmission value = mapSubmission(rs);
            DBUtil.closeResultSet(rs);
            rs = null;
            DBUtil.closePreparedStatement(ps);
            ps = null;
            reviews(connection, value);
            return value;
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
        }
    }

    /** Đọc các bài nộp và lịch sử chấm theo quyền xem. */
    public static List<TaskSubmission> selectSubmissions(long taskId, long userId, boolean admin)
            throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String sql =
                    "SELECT s.*, "
                            + "(SELECT d.submitted_at FROM diagnosis_attempts d WHERE d.id=s.diagnosis_attempt_id) "
                            + "AS diagnosis_submitted_at, "
                            + "s.id FROM task_submissions s JOIN task_recipients tr ON "
                            + "tr.id=s.recipient_id WHERE tr.task_id = ?";
            if (!admin) {
                sql += " AND tr.user_id = ?";
            }
            sql += " ORDER BY s.recipient_id, s.submission_no";
            ps = connection.prepareStatement(sql);
            ps.setLong(1, taskId);
            if (!admin) {
                ps.setLong(2, userId);
            }
            rs = ps.executeQuery();
            List<TaskSubmission> list = new ArrayList<>();
            while (rs.next()) {
                list.add(mapSubmission(rs));
            }
            DBUtil.closeResultSet(rs);
            rs = null;
            DBUtil.closePreparedStatement(ps);
            ps = null;
            for (TaskSubmission value : list) {
                reviews(connection, value);
            }
            return list;
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
            pool.freeConnection(connection);
        }
    }

    /** Đọc một bài nộp trong phạm vi quyền xem. */
    public static TaskSubmission selectSubmission(long id, long userId, boolean admin)
            throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String sql =
                    "SELECT s.*, "
                            + "(SELECT d.submitted_at FROM diagnosis_attempts d WHERE d.id=s.diagnosis_attempt_id) "
                            + "AS diagnosis_submitted_at, "
                            + "s.id FROM task_submissions s JOIN task_recipients tr ON "
                            + "tr.id=s.recipient_id WHERE s.id = ?";
            if (!admin) {
                sql += " AND tr.user_id = ?";
            }
            ps = connection.prepareStatement(sql);
            ps.setLong(1, id);
            if (!admin) {
                ps.setLong(2, userId);
            }
            rs = ps.executeQuery();
            if (!rs.next()) {
                return null;
            }
            TaskSubmission value = mapSubmission(rs);
            DBUtil.closeResultSet(rs);
            rs = null;
            DBUtil.closePreparedStatement(ps);
            ps = null;
            reviews(connection, value);
            return value;
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
            pool.freeConnection(connection);
        }
    }

    /** Đọc phiên hoàn tất của người học theo mốc bằng chứng. */
    public static List<TaskSubmission> selectEvidence(long taskId, long userId)
            throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            PracticeTask task = PracticeTaskDB.task(connection, taskId, false);
            TaskRecipient recipient = PracticeTaskDB.recipient(connection, taskId, userId, false);
            if (task == null || recipient == null) {
                return new ArrayList<>();
            }
            TaskRound first = PracticeTaskDB.round(connection, recipient.getId(), 1, false);
            if (first == null) {
                return new ArrayList<>();
            }
            String sql =
                    "SELECT id, completed_at FROM assembly_sessions WHERE user_id = ? "
                            + "AND robot_id = ? AND status='COMPLETED'";
            if (!task.isAllowPriorEvidence()) {
                sql += " AND completed_at >= ?";
            }
            sql += " ORDER BY completed_at DESC, id DESC";
            ps = connection.prepareStatement(sql);
            ps.setLong(1, userId);
            ps.setString(2, task.getRobotId());
            if (!task.isAllowPriorEvidence()) {
                ps.setTimestamp(3, new Timestamp(first.getStartedAt().getTime()));
            }
            rs = ps.executeQuery();
            List<TaskSubmission> list = new ArrayList<>();
            while (rs.next()) {
                TaskSubmission value = new TaskSubmission();
                value.setSessionId(rs.getLong("id"));
                value.setRobotName(task.getRobotName());
                value.setAssemblyCompletedAt(PracticeTaskDB.date(rs, "completed_at"));
                list.add(value);
            }
            return list;
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
            pool.freeConnection(connection);
        }
    }

    static void quizSnapshot(
            Connection connection, long quizId, long userId, String robotId, TaskSubmission value)
            throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps =
                    connection.prepareStatement(
                            "SELECT score,total_questions,submitted_at FROM quiz_attempts "
                                    + "WHERE id = ? AND user_id = ? AND robot_id = ?");
            PracticeTaskDB.bind(ps, quizId, userId, robotId);
            rs = ps.executeQuery();
            if (!rs.next()) {
                throw new IllegalArgumentException("Vòng này chưa có lượt quiz tính điểm hợp lệ.");
            }
            value.setQuizAttemptId(quizId);
            value.setQuizScore(rs.getInt("score"));
            value.setQuizTotal(rs.getInt("total_questions"));
            value.setQuizSubmittedAt(PracticeTaskDB.date(rs, "submitted_at"));
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
        }
    }

    /** Đọc quiz của vòng hoặc lượt dùng lại từ bài trước. */
    public static TaskSubmission selectRoundQuiz(long taskId, long userId) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        try {
            PracticeTask task = PracticeTaskDB.task(connection, taskId, false);
            TaskRecipient recipient = PracticeTaskDB.recipient(connection, taskId, userId, false);
            if (task == null || recipient == null) {
                return null;
            }
            TaskRound round = PracticeTaskDB.round(connection, recipient.getId(), 0, false);
            if (round == null) {
                return null;
            }
            TaskSubmission previous = latest(connection, recipient.getId());
            long previousId = 0;
            if (previous != null) {
                previousId = previous.getQuizAttemptId();
            }
            long quiz =
                    new TaskRubric()
                            .chooseQuizAttempt(
                                    round.getRoundNo(), round.getQuizAttemptId(), previousId);
            if (quiz == 0) {
                return null;
            }
            TaskSubmission value = new TaskSubmission();
            quizSnapshot(connection, quiz, userId, task.getRobotId(), value);
            if (round.getQuizAttemptId() == 0 && previous != null) {
                value.setQuizReusedFrom(previous.getSubmissionNo());
            }
            return value;
        } finally {
            pool.freeConnection(connection);
        }
    }

    /** Kiểm lại bằng chứng; chỉ ghi bài khi xác nhận hợp lệ. */
    public static TaskSubmission submit(
            long taskId, long userId, TaskSubmission input, boolean confirm) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        boolean original = connection.getAutoCommit();
        try {
            connection.setAutoCommit(false);
            Date now = new Date();
            PracticeTask task = PracticeTaskDB.task(connection, taskId, true);
            TaskRecipient recipient = PracticeTaskDB.recipient(connection, taskId, userId, true);
            if (task == null || recipient == null) {
                throw new IllegalArgumentException("Không tìm thấy nhiệm vụ được giao.");
            }
            TaskRound round = PracticeTaskDB.round(connection, recipient.getId(), 0, true);
            TaskSubmission previous = latest(connection, recipient.getId());
            int count =
                    (int)
                            PracticeTaskDB.scalar(
                                    connection,
                                    "SELECT COUNT(*) FROM task_submissions WHERE recipient_id = ?",
                                    recipient.getId());
            recipient.setTask(task);
            recipient.setCurrentRound(round);
            recipient.setSubmissionCount(count);
            recipient.setLatestSubmission(previous);
            if (!recipient.canSubmit(now)) {
                throw new IllegalArgumentException(
                        "Không thể nộp: nhiệm vụ hết hạn/đã đóng, hết lượt hoặc bài đang chờ"
                            + " chấm.");
            }
            if (input.getRoundId() != round.getId()) {
                throw new IllegalArgumentException("Vòng đã thay đổi; hãy mở lại trang nộp bài.");
            }
            input.validateExplanation();
            TaskRound first = PracticeTaskDB.round(connection, recipient.getId(), 1, false);
            ps =
                    connection.prepareStatement(
                            "SELECT robot_id,status,completed_at FROM assembly_sessions WHERE "
                                    + "id = ? AND user_id = ? FOR UPDATE");
            PracticeTaskDB.bind(ps, input.getSessionId(), userId);
            rs = ps.executeQuery();
            if (!rs.next()
                    || !new TaskRubric()
                            .validAssemblyEvidence(
                                    true,
                                    task.getRobotId().equals(rs.getString("robot_id")),
                                    "COMPLETED".equals(rs.getString("status")),
                                    PracticeTaskDB.date(rs, "completed_at"),
                                    first.getStartedAt(),
                                    task.isAllowPriorEvidence())) {
                throw new IllegalArgumentException(
                        "Phiên lắp ráp không thuộc bạn, sai robot, chưa hoàn tất hoặc "
                                + "trước mốc vòng 1.");
            }
            input.setAssemblyCompletedAt(PracticeTaskDB.date(rs, "completed_at"));
            DBUtil.closeResultSet(rs);
            rs = null;
            DBUtil.closePreparedStatement(ps);
            ps = null;
            long priorQuiz = 0;
            if (previous != null) {
                priorQuiz = previous.getQuizAttemptId();
            }
            long quiz =
                    new TaskRubric()
                            .chooseQuizAttempt(
                                    round.getRoundNo(), round.getQuizAttemptId(), priorQuiz);
            quizSnapshot(connection, quiz, userId, task.getRobotId(), input);
            input.setQuizReusedFrom(0);
            if (round.getQuizAttemptId() == 0 && previous != null) {
                input.setQuizReusedFrom(previous.getSubmissionNo());
            }
            input.setRecipientId(recipient.getId());
            input.setRoundId(round.getId());
            input.setSubmissionNo(count + 1);
            input.setRobotName(task.getRobotName());
            input.setLate(task.isLate(now));
            input.setSubmittedAt(now);
            TaskRubric rubric = task.getRubric();
            input.setAssemblyPoints(new BigDecimal(rubric.getAssemblyWeight()).setScale(1));
            if (task.isRubricB()) {
                long previousDiagnosis = 0;
                if (previous != null) {
                    previousDiagnosis = previous.getDiagnosisAttemptId();
                }
                long diagnosisId = rubric.chooseDiagnosisAttempt(round.getRoundNo(),
                        round.getDiagnosisAttemptId(), previousDiagnosis);
                if (input.getDiagnosisAttemptId() > 0 && input.getDiagnosisAttemptId() != diagnosisId) {
                    throw new IllegalArgumentException("Không được thay lượt chẩn đoán đã gắn vào vòng.");
                }
                DiagnosisAttempt diagnosis = DiagnosisDB.evidence(connection, diagnosisId, userId);
                if (diagnosis == null || !diagnosis.validTaskEvidence(userId, task.getDiagnosisScenarioId())) {
                    throw new IllegalArgumentException("Cần kết luận xong lượt chẩn đoán TASK của vòng này.");
                }
                diagnosisSnapshot(input, diagnosis, rubric);
                if (round.getDiagnosisAttemptId() == 0 && previous != null) {
                    input.setDiagnosisReusedFrom(previous.getSubmissionNo());
                }
                input.setAutomaticPoints(rubric.automaticPoints(input.getQuizScore(), input.getQuizTotal(),
                        diagnosis.getScoreExact()));
                input.setQuizPoints(rubric.roundAutomatic(rubric.quizContribution(
                        input.getQuizScore(), input.getQuizTotal())));
            } else {
                input.setDiagnosisAttemptId(0);
                input.setAutomaticPoints(rubric.automaticPoints(input.getQuizScore(), input.getQuizTotal()));
                input.setQuizPoints(input.getAutomaticPoints().subtract(input.getAssemblyPoints()));
            }
            if (confirm) {
                input.setId(
                        PracticeTaskDB.change(
                                connection,
                                "INSERT INTO task_submissions(recipient_id,round_id,submission_no,"
                                    + "session_id,quiz_attempt_id,robot_name,assembly_completed_at,"
                                    + "quiz_score,quiz_total,quiz_submitted_at,quiz_reused_from,"
                                    + "assembly_points,quiz_points,automatic_points,"
                                    + "is_late,problem,reasoning,improvement,submitted_at,"
                                    + "diagnosis_attempt_id,diagnosis_title,diag_required_done,diag_required_total,"
                                    + "diag_cause_correct,diag_action_correct,diagnosis_score,diagnosis_points,"
                                    + "diagnosis_reused_from)"
                                    + " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                                input.getRecipientId(),
                                input.getRoundId(),
                                input.getSubmissionNo(),
                                input.getSessionId(),
                                input.getQuizAttemptId(),
                                input.getRobotName(),
                                input.getAssemblyCompletedAt(),
                                input.getQuizScore(),
                                input.getQuizTotal(),
                                input.getQuizSubmittedAt(),
                                input.getQuizReusedFrom(),
                                input.getAssemblyPoints(),
                                input.getQuizPoints(),
                                input.getAutomaticPoints(),
                                input.isLate(),
                                input.getProblem(),
                                input.getReasoning(),
                                input.getImprovement(),
                                now,
                                diagnosisField(input, input.getDiagnosisAttemptId()),
                                diagnosisField(input, input.getDiagnosisTitle()),
                                diagnosisField(input, input.getDiagRequiredDone()),
                                diagnosisField(input, input.getDiagRequiredTotal()),
                                diagnosisField(input, input.isDiagCauseCorrect()),
                                diagnosisField(input, input.isDiagActionCorrect()),
                                diagnosisField(input, input.getDiagnosisScore()),
                                diagnosisField(input, input.getDiagnosisPoints()),
                                input.getDiagnosisReusedFrom()));
                PracticeTaskDB.change(
                        connection,
                        "UPDATE task_rounds SET state='SUBMITTED' WHERE id=?",
                        round.getId());
            }
            connection.commit();
            return input;
        } catch (SQLException | RuntimeException e) {
            connection.rollback();
            throw e;
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
            try {
                connection.setAutoCommit(original);
            } finally {
                pool.freeConnection(connection);
            }
        }
    }

    private static Object diagnosisField(TaskSubmission value, Object field) {
        if (!value.isHasDiagnosis()) {
            return null;
        }
        return field;
    }

    private static void diagnosisSnapshot(TaskSubmission value, DiagnosisAttempt attempt, TaskRubric rubric) {
        value.setDiagnosisAttemptId(attempt.getId());
        value.setDiagnosisTitle(attempt.getScenario().getTitle());
        value.setDiagRequiredDone(attempt.getRequiredDone());
        value.setDiagRequiredTotal(attempt.getRequiredTotal());
        value.setDiagCauseCorrect(attempt.isCauseCorrect());
        value.setDiagActionCorrect(attempt.isActionCorrect());
        value.setDiagnosisScore(rubric.roundAutomatic(attempt.getScoreExact()));
        value.setDiagnosisPoints(rubric.roundAutomatic(rubric.diagnosisContribution(attempt.getScoreExact())));
        value.setDiagnosisSubmittedAt(attempt.getSubmittedAt());
        value.setDiagnosisReusedFrom(0);
    }

    /** Đọc bản chụp lượt của vòng hoặc lượt dùng lại, không thay thế lượt đang làm. */
    public static TaskSubmission selectRoundDiagnosis(long taskId, long userId) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        try {
            PracticeTask task = PracticeTaskDB.task(connection, taskId, false);
            TaskRecipient recipient = PracticeTaskDB.recipient(connection, taskId, userId, false);
            if (task == null || recipient == null || !task.isRubricB()) {
                return null;
            }
            TaskRound round = PracticeTaskDB.round(connection, recipient.getId(), 0, false);
            if (round == null) {
                return null;
            }
            TaskSubmission previous = latest(connection, recipient.getId());
            long previousId = 0;
            if (previous != null) {
                previousId = previous.getDiagnosisAttemptId();
            }
            long id = task.getRubric().chooseDiagnosisAttempt(round.getRoundNo(),
                    round.getDiagnosisAttemptId(), previousId);
            DiagnosisAttempt attempt = DiagnosisDB.evidence(connection, id, userId);
            if (attempt == null || !attempt.validTaskEvidence(userId, task.getDiagnosisScenarioId())) {
                return null;
            }
            TaskSubmission value = new TaskSubmission();
            diagnosisSnapshot(value, attempt, task.getRubric());
            if (round.getDiagnosisAttemptId() == 0 && previous != null) {
                value.setDiagnosisReusedFrom(previous.getSubmissionNo());
            }
            return value;
        } finally {
            pool.freeConnection(connection);
        }
    }

    /** Đọc mã nhiệm vụ của bài nộp để lấy khóa đúng thứ tự. */
    public static long taskIdForSubmission(long submissionId) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        try {
            return PracticeTaskDB.scalar(
                    connection,
                    "SELECT tr.task_id FROM task_submissions s JOIN task_recipients tr "
                            + "ON tr.id=s.recipient_id WHERE s.id=?",
                    submissionId);
        } finally {
            pool.freeConnection(connection);
        }
    }

    /** Lưu lần chấm mới và cập nhật vòng trong cùng transaction. */
    public static void review(
            long submissionId, long adminId, long expectedReviewId, TaskReview input)
            throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        boolean original = connection.getAutoCommit();
        try {
            long taskId =
                    PracticeTaskDB.scalar(
                            connection,
                            "SELECT tr.task_id FROM task_submissions s JOIN task_recipients tr "
                                    + "ON tr.id=s.recipient_id WHERE s.id=?",
                            submissionId);
            connection.setAutoCommit(false);
            PracticeTask task = PracticeTaskDB.task(connection, taskId, true);
            ps =
                    connection.prepareStatement(
                            "SELECT s.*, "
                            + "(SELECT d.submitted_at FROM diagnosis_attempts d WHERE d.id=s.diagnosis_attempt_id) "
                            + "AS diagnosis_submitted_at, "
                            + "tr.user_id FROM task_submissions s JOIN "
                                    + "task_recipients tr ON tr.id=s.recipient_id WHERE s.id=? FOR UPDATE");
            ps.setLong(1, submissionId);
            rs = ps.executeQuery();
            if (task == null || !rs.next()) {
                throw new IllegalArgumentException("Không tìm thấy bài nộp.");
            }
            TaskSubmission submission = mapSubmission(rs);
            long userId = rs.getLong("user_id");
            DBUtil.closeResultSet(rs);
            rs = null;
            DBUtil.closePreparedStatement(ps);
            ps = null;
            TaskRecipient recipient = PracticeTaskDB.recipient(connection, taskId, userId, true);
            TaskRound current =
                    PracticeTaskDB.round(
                            connection, recipient.getId(), submission.getSubmissionNo(), true);
            TaskRound next =
                    PracticeTaskDB.round(
                            connection, recipient.getId(), current.getRoundNo() + 1, true);
            reviews(connection, submission);
            TaskReview old = submission.getCurrentReview();
            long actual = 0;
            if (old != null) {
                actual = old.getId();
            }
            if (expectedReviewId != actual) {
                throw new IllegalArgumentException(
                        "Lần chấm đã thay đổi; hãy tải lại trước khi lưu.");
            }
            int count =
                    (int)
                            PracticeTaskDB.scalar(
                                    connection,
                                    "SELECT COUNT(*) FROM task_submissions WHERE recipient_id=?",
                                    recipient.getId());
            Date now = new Date();
            input.validate(task, submission, count, next, now, old != null);
            Object oldId = null;
            if (old != null) {
                oldId = old.getId();
            }
            PracticeTaskDB.change(
                    connection,
                    "INSERT INTO task_reviews(submission_id,reviewer_id,"
                            + "explanation_level,explanation_points,total_points,conclusion,"
                            + "strengths,improvements,retry_guidance,reviewed_at,"
                            + "supersedes_review_id,change_reason) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)",
                    submissionId,
                    adminId,
                    input.getExplanationLevel(),
                    input.getExplanationPoints(),
                    input.getTotalPoints(),
                    input.getConclusion(),
                    input.getStrengths(),
                    input.getImprovements(),
                    input.getRetryGuidance(),
                    now,
                    oldId,
                    input.getChangeReason());
            PracticeTaskDB.change(
                    connection,
                    "UPDATE task_submissions SET state='REVIEWED' WHERE id=?",
                    submissionId);
            if (input.isNeedsRevision()) {
                if (next == null) {
                    PracticeTaskDB.change(
                            connection,
                            "INSERT INTO task_rounds(recipient_id,round_no,started_at) VALUES"
                                + " (?,?,?)",
                            recipient.getId(),
                            current.getRoundNo() + 1,
                            now);
                } else if (next.isCancelled()) {
                    PracticeTaskDB.change(
                            connection,
                            "UPDATE task_rounds SET state='ACTIVE',started_at=? WHERE id=?",
                            now,
                            next.getId());
                }
            } else if (next != null) {
                PracticeTaskDB.change(
                        connection,
                        "UPDATE task_rounds SET state='CANCELLED' WHERE id=?",
                        next.getId());
            }
            connection.commit();
        } catch (SQLException | RuntimeException e) {
            connection.rollback();
            throw e;
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
            try {
                connection.setAutoCommit(original);
            } finally {
                pool.freeConnection(connection);
            }
        }
    }
}
