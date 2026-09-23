package vn.edu.webpro.robotlab.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import vn.edu.webpro.robotlab.business.AssemblyStep;

/** Đọc/ghi bảng assembly_steps — cùng khuôn với UserDB. */
public class AssemblyStepDB {
    private static final String FIELDS =
            "id, robot_id, step_order, title, instruction, illustration";

    /* Cột illustration có kiểu JSON trong MySQL nên tham số phải qua CAST(? AS JSON). */
    public static int insert(AssemblyStep step) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement ps = null;

        String query = "INSERT INTO assembly_steps (" + FIELDS + ") "
                + "VALUES (?, ?, ?, ?, ?, CAST(? AS JSON))";
        try {
            ps = connection.prepareStatement(query);
            ps.setString(1, step.getId());
            ps.setString(2, step.getRobotId());
            ps.setInt(3, step.getStepOrder());
            ps.setString(4, step.getTitle());
            ps.setString(5, step.getInstruction());
            ps.setString(6, step.getIllustrationJson());
            return ps.executeUpdate();
        } finally {
            DBUtil.closePreparedStatement(ps);
            pool.freeConnection(connection);
        }
    }

    public static int update(AssemblyStep step) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement ps = null;

        String query = "UPDATE assembly_steps SET "
                + "robot_id = ?, step_order = ?, title = ?, instruction = ?, "
                + "illustration = CAST(? AS JSON) "
                + "WHERE id = ?";
        try {
            ps = connection.prepareStatement(query);
            ps.setString(1, step.getRobotId());
            ps.setInt(2, step.getStepOrder());
            ps.setString(3, step.getTitle());
            ps.setString(4, step.getInstruction());
            ps.setString(5, step.getIllustrationJson());
            ps.setString(6, step.getId());
            return ps.executeUpdate();
        } finally {
            DBUtil.closePreparedStatement(ps);
            pool.freeConnection(connection);
        }
    }

    public static int delete(String id) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement ps = null;

        String query = "DELETE FROM assembly_steps WHERE id = ?";
        try {
            ps = connection.prepareStatement(query);
            ps.setString(1, id);
            return ps.executeUpdate();
        } finally {
            DBUtil.closePreparedStatement(ps);
            pool.freeConnection(connection);
        }
    }

    public static List<AssemblyStep> selectSteps(int limit, int offset) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        String query = "SELECT " + FIELDS + " FROM assembly_steps "
                + "ORDER BY robot_id, step_order, id LIMIT ? OFFSET ?";
        try {
            ps = connection.prepareStatement(query);
            ps.setInt(1, limit);
            ps.setInt(2, offset);
            rs = ps.executeQuery();
            List<AssemblyStep> steps = new ArrayList<>();
            while (rs.next()) {
                steps.add(readStep(rs));
            }
            return steps;
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
            pool.freeConnection(connection);
        }
    }

    public static List<AssemblyStep> selectSteps(String robotId, int limit, int offset)
            throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        String query = "SELECT " + FIELDS + " FROM assembly_steps "
                + "WHERE robot_id = ? ORDER BY step_order, id LIMIT ? OFFSET ?";
        try {
            ps = connection.prepareStatement(query);
            ps.setString(1, robotId);
            ps.setInt(2, limit);
            ps.setInt(3, offset);
            rs = ps.executeQuery();
            List<AssemblyStep> steps = new ArrayList<>();
            while (rs.next()) {
                steps.add(readStep(rs));
            }
            return steps;
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
            pool.freeConnection(connection);
        }
    }

    public static long countSteps(String robotId) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        String query = "SELECT COUNT(*) AS total FROM assembly_steps WHERE robot_id = ?";
        try {
            ps = connection.prepareStatement(query);
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

    /** Dùng để chặn việc đánh dấu một bước không thuộc robot của phiên. */
    public static boolean stepBelongsToRobot(String robotId, String stepId) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        String query = "SELECT id FROM assembly_steps WHERE robot_id = ? AND id = ?";
        try {
            ps = connection.prepareStatement(query);
            ps.setString(1, robotId);
            ps.setString(2, stepId);
            rs = ps.executeQuery();
            return rs.next();
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
            pool.freeConnection(connection);
        }
    }

    private static AssemblyStep readStep(ResultSet rs) throws SQLException {
        AssemblyStep step = new AssemblyStep();
        step.setId(rs.getString("id"));
        step.setRobotId(rs.getString("robot_id"));
        step.setStepOrder(rs.getInt("step_order"));
        step.setTitle(rs.getString("title"));
        step.setInstruction(rs.getString("instruction"));
        step.setIllustrationJson(rs.getString("illustration"));
        return step;
    }
}
