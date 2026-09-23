package vn.edu.webpro.robotlab.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import vn.edu.webpro.robotlab.business.Robot;
import vn.edu.webpro.robotlab.business.RobotComponent;

/** Đọc/ghi bảng robots và robot_components — cùng khuôn với UserDB. */
public class RobotDB {
    private static final String FIELDS =
            "id, name, level, summary, image, build_time, main_sensor, skills, wiring";

    /* Cột wiring có kiểu JSON trong MySQL nên tham số phải qua CAST(? AS JSON). */
    public static int insert(Robot robot) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement ps = null;

        String query = "INSERT INTO robots (" + FIELDS + ") "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, CAST(? AS JSON))";
        try {
            ps = connection.prepareStatement(query);
            ps.setString(1, robot.getId());
            ps.setString(2, robot.getName());
            ps.setString(3, robot.getLevel());
            ps.setString(4, robot.getSummary());
            ps.setString(5, robot.getImage());
            ps.setString(6, robot.getBuildTime());
            ps.setString(7, robot.getMainSensor());
            ps.setString(8, robot.getSkills());
            ps.setString(9, robot.getWiringJson());
            return ps.executeUpdate();
        } finally {
            DBUtil.closePreparedStatement(ps);
            pool.freeConnection(connection);
        }
    }

    public static int update(Robot robot) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement ps = null;

        String query = "UPDATE robots SET "
                + "name = ?, level = ?, summary = ?, image = ?, build_time = ?, "
                + "main_sensor = ?, skills = ?, wiring = CAST(? AS JSON) "
                + "WHERE id = ?";
        try {
            ps = connection.prepareStatement(query);
            ps.setString(1, robot.getName());
            ps.setString(2, robot.getLevel());
            ps.setString(3, robot.getSummary());
            ps.setString(4, robot.getImage());
            ps.setString(5, robot.getBuildTime());
            ps.setString(6, robot.getMainSensor());
            ps.setString(7, robot.getSkills());
            ps.setString(8, robot.getWiringJson());
            ps.setString(9, robot.getId());
            return ps.executeUpdate();
        } finally {
            DBUtil.closePreparedStatement(ps);
            pool.freeConnection(connection);
        }
    }

    /** Khóa ngoại chặn xóa khi robot còn được tham chiếu; khi đó MySQL ném SQLException. */
    public static int delete(String id) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement ps = null;

        String query = "DELETE FROM robots WHERE id = ?";
        try {
            ps = connection.prepareStatement(query);
            ps.setString(1, id);
            return ps.executeUpdate();
        } finally {
            DBUtil.closePreparedStatement(ps);
            pool.freeConnection(connection);
        }
    }

    public static boolean robotExists(String id) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        String query = "SELECT id FROM robots WHERE id = ?";
        try {
            ps = connection.prepareStatement(query);
            ps.setString(1, id);
            rs = ps.executeQuery();
            return rs.next();
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
            pool.freeConnection(connection);
        }
    }

    public static List<Robot> selectRobots(int limit, int offset) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        String query = "SELECT " + FIELDS + " FROM robots ORDER BY id LIMIT ? OFFSET ?";
        try {
            ps = connection.prepareStatement(query);
            ps.setInt(1, limit);
            ps.setInt(2, offset);
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

    public static long countRobots() throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        String query = "SELECT COUNT(*) AS total FROM robots";
        try {
            ps = connection.prepareStatement(query);
            rs = ps.executeQuery();
            rs.next();
            return rs.getLong("total");
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
            pool.freeConnection(connection);
        }
    }

    /** Danh sách linh kiện bắt buộc kèm số lượng (bảng quan hệ robot_components). */
    public static List<RobotComponent> selectRobotComponents(String robotId) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        String query = "SELECT component_id, quantity FROM robot_components "
                + "WHERE robot_id = ? ORDER BY component_id";
        try {
            ps = connection.prepareStatement(query);
            ps.setString(1, robotId);
            rs = ps.executeQuery();
            List<RobotComponent> parts = new ArrayList<>();
            while (rs.next()) {
                RobotComponent part = new RobotComponent();
                part.setComponentId(rs.getString("component_id"));
                part.setQuantity(rs.getInt("quantity"));
                parts.add(part);
            }
            return parts;
        } finally {
            DBUtil.closeResultSet(rs);
            DBUtil.closePreparedStatement(ps);
            pool.freeConnection(connection);
        }
    }

    /** Dùng để chặn việc tick một linh kiện không thuộc robot của phiên. */
    public static boolean robotHasComponent(String robotId, String componentId) throws SQLException {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = pool.getConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        String query = "SELECT component_id FROM robot_components "
                + "WHERE robot_id = ? AND component_id = ?";
        try {
            ps = connection.prepareStatement(query);
            ps.setString(1, robotId);
            ps.setString(2, componentId);
            rs = ps.executeQuery();
            return rs.next();
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
