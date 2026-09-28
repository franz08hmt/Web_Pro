package vn.edu.webpro.robotlab.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import vn.edu.webpro.robotlab.business.QuestionMissRate;
import vn.edu.webpro.robotlab.business.QuizRobotAggregate;
import vn.edu.webpro.robotlab.business.QuizRobotScore;
import vn.edu.webpro.robotlab.business.Robot;
import vn.edu.webpro.robotlab.business.RobotPopularity;

/**
 * Truy vấn tổng hợp cho trang tổng kết học tập cá nhân và thống kê quản trị
 * (Chức năng 5) — không phải CRUD một bảng cụ thể nên tách khỏi các lớp XxxDB
 * theo thực thể, cùng tinh thần với HealthDB.
 *
 * Mọi số liệu đọc trực tiếp từ MySQL bằng GROUP BY/COUNT DISTINCT ngay trong
 * câu SQL, không cộng dồn ở tầng Java, để tránh đếm trùng khi có JOIN.
 */
public class StatsDB {
    private static final String[] SESSION_STATUSES =
            {"PREPARING", "READY", "IN_PROGRESS", "COMPLETED", "ABANDONED"};

    // ---------- Cá nhân ----------

    /** Số phiên theo từng trạng thái của một tài khoản; trạng thái chưa có phiên nào vẫn xuất hiện với giá trị 0. */
    public static Map<String, Long> countSessionsByStatusForUser(long userId) throws SQLException {
        return countSessionsByStatus("WHERE user_id = ?", userId);
    }

    /** Các mẫu robot đã hoàn tất ít nhất một lần, không lặp lại dù hoàn tất nhiều phiên của cùng một mẫu. */
    public static List<Robot> selectCompletedRobots(long userId) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = connection.prepareStatement(
                    "SELECT DISTINCT r.id, r.name, r.level, r.summary, r.image, r.build_time, "
                    + "r.main_sensor, r.skills, r.wiring "
                    + "FROM robots r "
                    + "JOIN assembly_sessions s ON s.robot_id = r.id "
                    + "WHERE s.user_id = ? AND s.status = 'COMPLETED' "
                    + "ORDER BY r.name");
            ps.setLong(1, userId);
            rs = ps.executeQuery();
            List<Robot> robots = new ArrayList<>();
            while (rs.next()) {
                robots.add(readRobot(rs));
            }
            return robots;
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
            pool.freeConnection(connection);
        }
    }

    /** Điểm tốt nhất/gần nhất và số lần làm bài của một tài khoản, theo từng mẫu robot đã từng làm bài. */
    public static List<QuizRobotScore> selectQuizScoresByRobot(long userId) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = connection.prepareStatement(
                    "SELECT a.robot_id, r.name AS robot_name, MAX(a.score) AS best_score, "
                    + "MAX(a.total_questions) AS total_questions, COUNT(*) AS attempt_count, "
                    + "(SELECT la.score FROM quiz_attempts la WHERE la.user_id = a.user_id "
                    + " AND la.robot_id = a.robot_id ORDER BY la.submitted_at DESC, la.id DESC LIMIT 1) "
                    + "AS latest_score "
                    + "FROM quiz_attempts a "
                    + "JOIN robots r ON r.id = a.robot_id "
                    + "WHERE a.user_id = ? "
                    + "GROUP BY a.robot_id, r.name, a.user_id "
                    + "ORDER BY r.name");
            ps.setLong(1, userId);
            rs = ps.executeQuery();
            List<QuizRobotScore> scores = new ArrayList<>();
            while (rs.next()) {
                QuizRobotScore score = new QuizRobotScore();
                score.setRobotId(rs.getString("robot_id"));
                score.setRobotName(rs.getString("robot_name"));
                score.setBestScore(rs.getInt("best_score"));
                score.setLatestScore(rs.getInt("latest_score"));
                score.setTotalQuestions(rs.getInt("total_questions"));
                score.setAttemptCount(rs.getInt("attempt_count"));
                scores.add(score);
            }
            return scores;
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
            pool.freeConnection(connection);
        }
    }

    // ---------- Quản trị ----------

    /** Số phiên theo từng trạng thái trên toàn hệ thống; trạng thái chưa có phiên nào vẫn xuất hiện với giá trị 0. */
    public static Map<String, Long> countAllSessionsByStatus() throws SQLException {
        return countSessionsByStatus("", null);
    }

    /** Tổng số tài khoản role 'user' (không tính admin) — mẫu số của "số người có hoạt động". */
    public static long countLearners() throws SQLException {
        return countSingleValue("SELECT COUNT(*) AS total FROM users WHERE role = 'user'", null);
    }

    /**
     * Số tài khoản đã có ít nhất một hoạt động (một phiên lắp ráp HOẶC một lượt
     * làm bài). Gộp hai nguồn bằng UNION rồi COUNT DISTINCT nên mỗi tài khoản
     * chỉ tính một lần dù có cả hai loại hoạt động — không đếm trùng.
     */
    public static long countActiveLearners() throws SQLException {
        return countSingleValue(
                "SELECT COUNT(DISTINCT user_id) AS total FROM ("
                + "SELECT user_id FROM assembly_sessions UNION SELECT user_id FROM quiz_attempts"
                + ") AS active_users", null);
    }

    /** Mẫu robot có nhiều phiên lắp ráp nhất, nhiều nhất trước; chỉ tính robot đã có ít nhất một phiên. */
    public static List<RobotPopularity> selectPopularRobots(int limit) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = connection.prepareStatement(
                    "SELECT r.id, r.name, COUNT(s.id) AS session_count "
                    + "FROM robots r "
                    + "JOIN assembly_sessions s ON s.robot_id = r.id "
                    + "GROUP BY r.id, r.name "
                    + "ORDER BY session_count DESC, r.name "
                    + "LIMIT ?");
            ps.setInt(1, limit);
            rs = ps.executeQuery();
            List<RobotPopularity> robots = new ArrayList<>();
            while (rs.next()) {
                RobotPopularity robot = new RobotPopularity();
                robot.setRobotId(rs.getString("id"));
                robot.setRobotName(rs.getString("name"));
                robot.setSessionCount(rs.getLong("session_count"));
                robots.add(robot);
            }
            return robots;
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
            pool.freeConnection(connection);
        }
    }

    /** Kết quả kiểm tra tổng hợp theo từng mẫu robot: số lượt làm bài và điểm trung bình theo phần trăm. */
    public static List<QuizRobotAggregate> selectQuizAggregatesByRobot() throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = connection.prepareStatement(
                    "SELECT r.id, r.name, COUNT(*) AS attempt_count, "
                    + "ROUND(AVG(a.score / a.total_questions) * 100) AS average_score_percent "
                    + "FROM quiz_attempts a "
                    + "JOIN robots r ON r.id = a.robot_id "
                    + "GROUP BY r.id, r.name "
                    + "ORDER BY r.name");
            rs = ps.executeQuery();
            List<QuizRobotAggregate> aggregates = new ArrayList<>();
            while (rs.next()) {
                QuizRobotAggregate aggregate = new QuizRobotAggregate();
                aggregate.setRobotId(rs.getString("id"));
                aggregate.setRobotName(rs.getString("name"));
                aggregate.setAttemptCount(rs.getLong("attempt_count"));
                aggregate.setAverageScorePercent(rs.getInt("average_score_percent"));
                aggregates.add(aggregate);
            }
            return aggregates;
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
            pool.freeConnection(connection);
        }
    }

    /**
     * Câu hỏi có tỷ lệ trả lời sai cao nhất, chỉ tính câu đã có ít nhất
     * minAttempts lượt trả lời — một lượt làm sai duy nhất không nên bị tính
     * thành "sai 100%". Lấy prompt hiện tại từ quiz_questions (không phải bản
     * chụp) vì đây là thống kê cho admin xem câu hỏi NÀO đang khó, không phải
     * hiển thị lại một kết quả lịch sử.
     */
    public static List<QuestionMissRate> selectHighMissRateQuestions(int minAttempts, int limit) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = connection.prepareStatement(
                    "SELECT qa.question_id, q.robot_id, q.prompt, COUNT(*) AS attempt_count, "
                    + "ROUND(SUM(CASE WHEN qa.is_correct THEN 0 ELSE 1 END) / COUNT(*) * 100) AS miss_rate_percent "
                    + "FROM quiz_attempt_answers qa "
                    + "JOIN quiz_questions q ON q.id = qa.question_id "
                    + "GROUP BY qa.question_id, q.robot_id, q.prompt "
                    + "HAVING COUNT(*) >= ? "
                    + "ORDER BY miss_rate_percent DESC, attempt_count DESC "
                    + "LIMIT ?");
            ps.setInt(1, minAttempts);
            ps.setInt(2, limit);
            rs = ps.executeQuery();
            List<QuestionMissRate> questions = new ArrayList<>();
            while (rs.next()) {
                QuestionMissRate question = new QuestionMissRate();
                question.setQuestionId(rs.getString("question_id"));
                question.setRobotId(rs.getString("robot_id"));
                question.setPrompt(rs.getString("prompt"));
                question.setAttemptCount(rs.getInt("attempt_count"));
                question.setMissRatePercent(rs.getInt("miss_rate_percent"));
                questions.add(question);
            }
            return questions;
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
            pool.freeConnection(connection);
        }
    }

    private static Map<String, Long> countSessionsByStatus(String whereClause, Long userId) throws SQLException {
        Map<String, Long> counts = new LinkedHashMap<>();
        for (String status : SESSION_STATUSES) {
            counts.put(status, 0L);
        }
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = connection.prepareStatement(
                    "SELECT status, COUNT(*) AS total FROM assembly_sessions " + whereClause + " GROUP BY status");
            if (userId != null) ps.setLong(1, userId);
            rs = ps.executeQuery();
            while (rs.next()) {
                counts.put(rs.getString("status"), rs.getLong("total"));
            }
            return counts;
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
            pool.freeConnection(connection);
        }
    }

    private static long countSingleValue(String query, Long param) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = connection.prepareStatement(query);
            if (param != null) ps.setLong(1, param);
            rs = ps.executeQuery();
            rs.next();
            return rs.getLong("total");
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
            pool.freeConnection(connection);
        }
    }

    private static Robot readRobot(ResultSet rs) throws SQLException {
        Robot robot = new Robot();
        robot.setId(rs.getString("id"));
        robot.setName(rs.getString("name"));
        robot.setLevel(rs.getString("level"));
        robot.setSummary(rs.getString("summary"));
        robot.setImage(rs.getString("image"));
        robot.setBuildTime(rs.getString("build_time"));
        robot.setMainSensor(rs.getString("main_sensor"));
        robot.setSkills(rs.getString("skills"));
        robot.setWiringJson(rs.getString("wiring"));
        return robot;
    }
}
