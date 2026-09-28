package vn.edu.webpro.robotlab.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import vn.edu.webpro.robotlab.business.QuizOption;
import vn.edu.webpro.robotlab.business.QuizQuestion;

/** Đọc/ghi bảng quiz_questions và quiz_options — cùng khuôn với AssemblyStepDB. */
public class QuizQuestionDB {
    private static final String QUESTION_FIELDS = "id, robot_id, prompt, explanation, question_order";
    private static final String OPTION_FIELDS = "id, question_id, label, is_correct, option_order";

    /** Toàn bộ câu hỏi của một robot, mỗi câu kèm sẵn danh sách lựa chọn theo đúng thứ tự. */
    public static List<QuizQuestion> selectQuestionsByRobot(String robotId) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = connection.prepareStatement("SELECT " + QUESTION_FIELDS + " FROM quiz_questions "
                    + "WHERE robot_id = ? ORDER BY question_order");
            ps.setString(1, robotId);
            rs = ps.executeQuery();
            Map<String, QuizQuestion> questionsById = new LinkedHashMap<>();
            while (rs.next()) {
                QuizQuestion question = readQuestion(rs);
                questionsById.put(question.getId(), question);
            }
            DBUtil.closeResultSet(rs);
            rs = null;
            DBUtil.closePreparedStatement(ps);
            ps = null;
            if (questionsById.isEmpty()) return new ArrayList<>();

            /* Một truy vấn JOIN duy nhất cho toàn bộ lựa chọn của robot, thay vì
               N+1 truy vấn theo từng câu hỏi. */
            ps = connection.prepareStatement(
                    "SELECT o.id, o.question_id, o.label, o.is_correct, o.option_order "
                    + "FROM quiz_options o "
                    + "JOIN quiz_questions q ON q.id = o.question_id "
                    + "WHERE q.robot_id = ? ORDER BY o.question_id, o.option_order");
            ps.setString(1, robotId);
            rs = ps.executeQuery();
            while (rs.next()) {
                QuizOption option = readOption(rs);
                QuizQuestion question = questionsById.get(option.getQuestionId());
                if (question != null) question.getOptions().add(option);
            }
            return new ArrayList<>(questionsById.values());
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
            pool.freeConnection(connection);
        }
    }

    /** Một câu hỏi kèm lựa chọn theo ID, dùng cho trang sửa của admin. Null nếu không có. */
    public static QuizQuestion selectQuestion(String id) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = connection.prepareStatement("SELECT " + QUESTION_FIELDS + " FROM quiz_questions WHERE id = ?");
            ps.setString(1, id);
            rs = ps.executeQuery();
            if (!rs.next()) return null;
            QuizQuestion question = readQuestion(rs);
            DBUtil.closeResultSet(rs);
            rs = null;
            DBUtil.closePreparedStatement(ps);

            ps = connection.prepareStatement(
                    "SELECT " + OPTION_FIELDS + " FROM quiz_options WHERE question_id = ? ORDER BY option_order");
            ps.setString(1, id);
            rs = ps.executeQuery();
            while (rs.next()) {
                question.getOptions().add(readOption(rs));
            }
            return question;
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
            pool.freeConnection(connection);
        }
    }

    public static long countQuestions(String robotId) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = connection.prepareStatement("SELECT COUNT(*) AS total FROM quiz_questions WHERE robot_id = ?");
            ps.setString(1, robotId);
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
     * Lưu một câu hỏi và toàn bộ lựa chọn của nó trong một transaction: thêm/sửa
     * dòng quiz_questions, xóa hết lựa chọn cũ rồi chèn lại theo danh sách mới —
     * đơn giản và đúng vì số lựa chọn nhỏ (2-6), không cần so khớp từng dòng.
     * Trả về false nếu creating=false mà không có câu hỏi nào khớp id để sửa.
     */
    public static boolean saveQuestion(QuizQuestion question, boolean creating) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement ps = null;
        boolean originalAutoCommit = true;
        boolean transactionFinished = false;

        try {
            originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            if (creating) {
                ps = connection.prepareStatement("INSERT INTO quiz_questions (" + QUESTION_FIELDS + ") "
                        + "VALUES (?, ?, ?, ?, ?)");
                ps.setString(1, question.getId());
                ps.setString(2, question.getRobotId());
                ps.setString(3, question.getPrompt());
                ps.setString(4, question.getExplanation());
                ps.setInt(5, question.getQuestionOrder());
                ps.executeUpdate();
            } else {
                ps = connection.prepareStatement("UPDATE quiz_questions SET "
                        + "robot_id = ?, prompt = ?, explanation = ?, question_order = ? WHERE id = ?");
                ps.setString(1, question.getRobotId());
                ps.setString(2, question.getPrompt());
                ps.setString(3, question.getExplanation());
                ps.setInt(4, question.getQuestionOrder());
                ps.setString(5, question.getId());
                if (ps.executeUpdate() == 0) {
                    connection.rollback();
                    transactionFinished = true;
                    return false;
                }
            }
            DBUtil.closePreparedStatement(ps);

            ps = connection.prepareStatement("DELETE FROM quiz_options WHERE question_id = ?");
            ps.setString(1, question.getId());
            ps.executeUpdate();
            DBUtil.closePreparedStatement(ps);

            ps = connection.prepareStatement(
                    "INSERT INTO quiz_options (" + OPTION_FIELDS + ") VALUES (?, ?, ?, ?, ?)");
            for (QuizOption option : question.getOptions()) {
                ps.setString(1, option.getId());
                ps.setString(2, question.getId());
                ps.setString(3, option.getLabel());
                ps.setBoolean(4, option.isCorrect());
                ps.setInt(5, option.getOptionOrder());
                ps.addBatch();
            }
            ps.executeBatch();

            connection.commit();
            transactionFinished = true;
            return true;
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

    /** Xóa câu hỏi (và lựa chọn theo CASCADE); ném SQLException nếu đã có người làm bài (RESTRICT). */
    public static int deleteQuestion(String id) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement ps = null;

        try {
            ps = connection.prepareStatement("DELETE FROM quiz_questions WHERE id = ?");
            ps.setString(1, id);
            return ps.executeUpdate();
        } finally {
            DBUtil.closePreparedStatement(ps);
            pool.freeConnection(connection);
        }
    }

    private static QuizQuestion readQuestion(ResultSet rs) throws SQLException {
        QuizQuestion question = new QuizQuestion();
        question.setId(rs.getString("id"));
        question.setRobotId(rs.getString("robot_id"));
        question.setPrompt(rs.getString("prompt"));
        question.setExplanation(rs.getString("explanation"));
        question.setQuestionOrder(rs.getInt("question_order"));
        return question;
    }

    private static QuizOption readOption(ResultSet rs) throws SQLException {
        QuizOption option = new QuizOption();
        option.setId(rs.getString("id"));
        option.setQuestionId(rs.getString("question_id"));
        option.setLabel(rs.getString("label"));
        option.setCorrect(rs.getBoolean("is_correct"));
        option.setOptionOrder(rs.getInt("option_order"));
        return option;
    }
}
