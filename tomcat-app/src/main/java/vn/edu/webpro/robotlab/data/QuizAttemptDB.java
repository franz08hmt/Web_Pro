package vn.edu.webpro.robotlab.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import vn.edu.webpro.robotlab.business.QuizAttempt;
import vn.edu.webpro.robotlab.business.QuizAttemptAnswer;
import vn.edu.webpro.robotlab.business.QuizOption;
import vn.edu.webpro.robotlab.business.QuizQuestion;
import vn.edu.webpro.robotlab.business.PracticeTask;
import vn.edu.webpro.robotlab.business.TaskRecipient;
import vn.edu.webpro.robotlab.business.TaskRound;
import java.util.Date;

/**
 * Chấm và lưu lượt làm bài kiểm tra, cùng lịch sử theo tài khoản.
 *
 * Mọi câu truy vấn lịch sử đều kèm user_id, nên một tài khoản không đọc được
 * lượt làm bài của tài khoản khác kể cả khi đoán đúng ID — cùng khuôn với
 * AssemblySessionDB.
 */
public class QuizAttemptDB {
    private static final String ATTEMPT_FIELDS = "id, user_id, robot_id, score, total_questions, submitted_at";

    /**
     * Chấm điểm và lưu một lượt làm bài trong một transaction.
     *
     * submittedAnswers là danh sách {questionId, optionId} lấy nguyên văn từ
     * request, CHƯA được tin: phương thức này đọc lại toàn bộ câu hỏi/lựa chọn
     * thật của robot rồi tự đối chiếu — client chỉ quyết định mình chọn gì, còn
     * đúng/sai và điểm số luôn do server tính. Ném IllegalArgumentException nếu
     * thiếu câu trả lời cho một câu hỏi nào đó, câu hỏi/lựa chọn không thuộc
     * robot này, hoặc một questionId xuất hiện quá một lần trong request.
     */
    public static QuizAttempt submitAttempt(long userId, String robotId, List<String[]> submittedAnswers)
            throws SQLException {
        QuizAttempt graded = gradeAnswers(robotId, submittedAnswers);
        return persistAttempt(userId, robotId, graded.getScore(), graded.getTotalQuestions(), graded.getAnswers());
    }

    private static QuizAttempt gradeAnswers(String robotId, List<String[]> submittedAnswers) throws SQLException {
        List<QuizQuestion> questions = QuizQuestionDB.selectQuestionsByRobot(robotId);
        if (questions.isEmpty()) {
            throw new IllegalArgumentException("robot-has-no-quiz");
        }

        List<String> seenQuestionIds = new ArrayList<>();
        for (String[] answer : submittedAnswers) {
            if (answer == null || answer.length != 2) throw new IllegalArgumentException("invalid-answer");
            if (seenQuestionIds.contains(answer[0])) {
                throw new IllegalArgumentException("duplicate-question-" + answer[0]);
            }
            seenQuestionIds.add(answer[0]);
        }
        boolean everyQuestion = true;
        for (QuizQuestion question : questions) {
            if (!seenQuestionIds.contains(question.getId())) everyQuestion = false;
        }
        if (submittedAnswers.size() != questions.size() || !everyQuestion) {
            throw new IllegalArgumentException("must-answer-every-question");
        }

        List<QuizAttemptAnswer> gradedAnswers = new ArrayList<>();
        int score = 0;
        for (String[] answer : submittedAnswers) {
            String questionId = answer[0];
            String optionId = answer[1];
            QuizQuestion question = findQuestion(questions, questionId);
            if (question == null) {
                throw new IllegalArgumentException("question-not-in-robot-" + questionId);
            }
            QuizOption selected = question.findOption(optionId);
            if (selected == null) {
                throw new IllegalArgumentException("option-not-in-question-" + optionId);
            }

            QuizOption correct = question.getCorrectOption();
            boolean isCorrect = question.isCorrectOption(optionId);
            if (isCorrect) score++;

            QuizAttemptAnswer graded = new QuizAttemptAnswer();
            graded.setQuestionId(questionId);
            graded.setQuestionPromptSnapshot(question.getPrompt());
            graded.setSelectedOptionId(optionId);
            graded.setSelectedOptionLabelSnapshot(selected.getLabel());
            graded.setCorrectOptionLabelSnapshot(correct == null ? "" : correct.getLabel());
            graded.setCorrect(isCorrect);
            graded.setExplanationSnapshot(question.getExplanation());
            gradedAnswers.add(graded);
        }

        QuizAttempt graded = new QuizAttempt();
        graded.setScore(score); graded.setTotalQuestions(questions.size()); graded.setAnswers(gradedAnswers);
        return graded;
    }

    private static QuizAttempt persistAttempt(long userId, String robotId, int score, int totalQuestions,
            List<QuizAttemptAnswer> answers) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        boolean originalAutoCommit = true;
        boolean transactionFinished = false;

        try {
            originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            long attemptId = insertAttempt(connection, userId, robotId, score, totalQuestions, answers);
            connection.commit();
            transactionFinished = true;

            return selectAttempt(attemptId, userId);
        } catch (SQLException e) {
            if (!transactionFinished) {
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
            if (!transactionFinished) {
                try {
                    connection.rollback();
                } catch (SQLException e) {
                    System.out.println(e);
                }
            }
            try {
                connection.setAutoCommit(originalAutoCommit);
            } catch (SQLException e) {
                System.out.println(e);
            }
            pool.freeConnection(connection);
        }
    }


    private static long insertAttempt(Connection connection,long userId,String robotId,int score,int totalQuestions,
            List<QuizAttemptAnswer> answers) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
        ps = connection.prepareStatement(
                    "INSERT INTO quiz_attempts (user_id, robot_id, score, total_questions) VALUES (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, userId);
            ps.setString(2, robotId);
            ps.setInt(3, score);
            ps.setInt(4, totalQuestions);
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            rs.next();
            long attemptId = rs.getLong(1);
            DBUtil.closeResultSet(rs);
            rs = null;
            DBUtil.closePreparedStatement(ps);

            ps = connection.prepareStatement("INSERT INTO quiz_attempt_answers ("
                    + "attempt_id, question_id, question_prompt_snapshot, selected_option_id, "
                    + "selected_option_label_snapshot, correct_option_label_snapshot, is_correct, "
                    + "explanation_snapshot) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
            for (QuizAttemptAnswer answer : answers) {
                ps.setLong(1, attemptId);
                ps.setString(2, answer.getQuestionId());
                ps.setString(3, answer.getQuestionPromptSnapshot());
                ps.setString(4, answer.getSelectedOptionId());
                ps.setString(5, answer.getSelectedOptionLabelSnapshot());
                ps.setString(6, answer.getCorrectOptionLabelSnapshot());
                ps.setBoolean(7, answer.isCorrect());
                ps.setString(8, answer.getExplanationSnapshot());
                ps.addBatch();
            }
            ps.executeBatch();

            return attemptId;
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
        }
    }

    /** Lượt tính điểm của vòng nhiệm vụ: khóa theo cùng thứ tự task/recipient/round. */
    public static QuizAttempt submitAttempt(long userId, String robotId, List<String[]> answers, long roundId) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        boolean original = connection.getAutoCommit();
        try {
            long taskId = PracticeTaskDB.scalar(connection,
                "SELECT tr.task_id FROM task_rounds r JOIN task_recipients tr ON tr.id=r.recipient_id WHERE r.id=? AND tr.user_id = ?", roundId, userId);
            connection.setAutoCommit(false);
            PracticeTask task = PracticeTaskDB.task(connection, taskId, true);
            TaskRecipient recipient = PracticeTaskDB.recipient(connection, taskId, userId, true);
            if (task == null || recipient == null || !task.getRobotId().equals(robotId))
                throw new IllegalArgumentException("Không tìm thấy nhiệm vụ được giao.");
            TaskRound round = PracticeTaskDB.round(connection, recipient.getId(), 0, true);
            PracticeTaskDB.hydrate(connection, recipient, task);
            if (round == null || round.getId() != roundId || !round.isActive() || round.getQuizAttemptId() != 0 || !recipient.isCanTakeQuiz())
                throw new IllegalArgumentException("Vòng không còn mở hoặc lượt tính điểm đã chốt.");
            QuizAttempt graded = gradeAnswers(robotId, answers);
            long attemptId = insertAttempt(connection,userId,robotId,graded.getScore(),graded.getTotalQuestions(),graded.getAnswers());
            PracticeTaskDB.change(connection,"UPDATE task_rounds SET quiz_attempt_id=? WHERE id=?",attemptId,roundId);
            connection.commit();
            graded.setId(attemptId); graded.setUserId(userId); graded.setRobotId(robotId);
            return graded;
        } catch (SQLException | RuntimeException e) {
            connection.rollback(); throw e;
        } finally {
            try { connection.setAutoCommit(original); }
            finally { pool.freeConnection(connection); }
        }
    }

    /** Một lượt làm bài kèm từng câu trả lời; chỉ trả về khi đúng chủ (user_id). */
    public static QuizAttempt selectAttempt(long id, long userId) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = connection.prepareStatement(
                    "SELECT " + ATTEMPT_FIELDS + " FROM quiz_attempts WHERE id = ? AND user_id = ?");
            ps.setLong(1, id);
            ps.setLong(2, userId);
            rs = ps.executeQuery();
            if (!rs.next()) return null;
            QuizAttempt attempt = readAttempt(rs);
            DBUtil.closeResultSet(rs);
            rs = null;
            DBUtil.closePreparedStatement(ps);

            ps = connection.prepareStatement("SELECT question_id, question_prompt_snapshot, "
                    + "selected_option_id, selected_option_label_snapshot, correct_option_label_snapshot, "
                    + "is_correct, explanation_snapshot FROM quiz_attempt_answers WHERE attempt_id = ?");
            ps.setLong(1, id);
            rs = ps.executeQuery();
            while (rs.next()) {
                attempt.getAnswers().add(readAnswer(rs));
            }
            return attempt;
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
            pool.freeConnection(connection);
        }
    }

    /** Lịch sử làm bài của một tài khoản, mới nhất trước; lọc theo robot nếu có. */
    public static List<QuizAttempt> selectAttempts(long userId, String robotId, int limit, int offset)
            throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        String query = "SELECT " + ATTEMPT_FIELDS + " FROM quiz_attempts WHERE user_id = ?"
                + (robotId == null ? "" : " AND robot_id = ?")
                + " ORDER BY submitted_at DESC, id DESC LIMIT ? OFFSET ?";
        try {
            ps = connection.prepareStatement(query);
            int index = 1;
            ps.setLong(index++, userId);
            if (robotId != null) ps.setString(index++, robotId);
            ps.setInt(index++, limit);
            ps.setInt(index, offset);
            rs = ps.executeQuery();
            List<QuizAttempt> attempts = new ArrayList<>();
            while (rs.next()) {
                attempts.add(readAttempt(rs));
            }
            return attempts;
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
            pool.freeConnection(connection);
        }
    }

    public static long countAttempts(long userId, String robotId) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        String query = "SELECT COUNT(*) AS total FROM quiz_attempts WHERE user_id = ?"
                + (robotId == null ? "" : " AND robot_id = ?");
        try {
            ps = connection.prepareStatement(query);
            ps.setLong(1, userId);
            if (robotId != null) ps.setString(2, robotId);
            rs = ps.executeQuery();
            rs.next();
            return rs.getLong("total");
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
            pool.freeConnection(connection);
        }
    }

    /**
     * Điểm cao nhất và gần nhất theo từng robot của một tài khoản — phục vụ
     * trang tổng kết học tập (Chức năng 5). MAX/nhóm theo robot_id nên không
     * cần đọc toàn bộ lịch sử về tầng ứng dụng rồi tự tính.
     */
    public static Map<String, int[]> selectBestAndLatestScoreByRobot(long userId) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = connection.prepareStatement(
                    "SELECT a.robot_id, MAX(a.score) AS best_score, "
                    + "(SELECT score FROM quiz_attempts la WHERE la.user_id = a.user_id "
                    + "AND la.robot_id = a.robot_id ORDER BY la.submitted_at DESC, la.id DESC LIMIT 1) "
                    + "AS latest_score, a.total_questions "
                    + "FROM quiz_attempts a WHERE a.user_id = ? "
                    + "GROUP BY a.robot_id, a.total_questions");
            ps.setLong(1, userId);
            rs = ps.executeQuery();
            Map<String, int[]> result = new java.util.LinkedHashMap<>();
            while (rs.next()) {
                result.put(rs.getString("robot_id"),
                        new int[] {rs.getInt("best_score"), rs.getInt("latest_score"), rs.getInt("total_questions")});
            }
            return result;
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
            pool.freeConnection(connection);
        }
    }

    private static QuizQuestion findQuestion(List<QuizQuestion> questions, String id) {
        for (QuizQuestion question : questions) {
            if (question.getId().equals(id)) return question;
        }
        return null;
    }

    private static QuizAttempt readAttempt(ResultSet rs) throws SQLException {
        QuizAttempt attempt = new QuizAttempt();
        attempt.setId(rs.getLong("id"));
        attempt.setUserId(rs.getLong("user_id"));
        attempt.setRobotId(rs.getString("robot_id"));
        attempt.setScore(rs.getInt("score"));
        attempt.setTotalQuestions(rs.getInt("total_questions"));
        attempt.setSubmittedAt(rs.getTimestamp("submitted_at").toInstant().toString());
        return attempt;
    }

    private static QuizAttemptAnswer readAnswer(ResultSet rs) throws SQLException {
        QuizAttemptAnswer answer = new QuizAttemptAnswer();
        answer.setQuestionId(rs.getString("question_id"));
        answer.setQuestionPromptSnapshot(rs.getString("question_prompt_snapshot"));
        answer.setSelectedOptionId(rs.getString("selected_option_id"));
        answer.setSelectedOptionLabelSnapshot(rs.getString("selected_option_label_snapshot"));
        answer.setCorrectOptionLabelSnapshot(rs.getString("correct_option_label_snapshot"));
        answer.setCorrect(rs.getBoolean("is_correct"));
        answer.setExplanationSnapshot(rs.getString("explanation_snapshot"));
        return answer;
    }
}
