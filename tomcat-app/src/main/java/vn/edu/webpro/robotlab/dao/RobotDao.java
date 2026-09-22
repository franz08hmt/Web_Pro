package vn.edu.webpro.robotlab.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import vn.edu.webpro.robotlab.model.Robot;
import vn.edu.webpro.robotlab.model.RobotComponent;

public final class RobotDao {
    private static final String COLUMNS =
            "id, name, level, summary, image, build_time, main_sensor, skills, wiring";

    private final DatabaseConnectionFactory connections;

    public RobotDao(DatabaseConnectionFactory connections) {
        this.connections = connections;
    }

    public List<Robot> list(int limit, int offset) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM robots ORDER BY id LIMIT ? OFFSET ?";

        List<Robot> robots = new ArrayList<>();
        try (Connection connection = connections.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, limit);
            statement.setInt(2, offset);

            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) robots.add(map(rows));
            }
        }
        return robots;
    }

    public long count() throws SQLException {
        try (Connection connection = connections.openConnection();
             PreparedStatement statement =
                     connection.prepareStatement("SELECT COUNT(*) AS total FROM robots");
             ResultSet rows = statement.executeQuery()) {
            rows.next();
            return rows.getLong("total");
        }
    }

    public boolean exists(String robotId) throws SQLException {
        try (Connection connection = connections.openConnection();
             PreparedStatement statement =
                     connection.prepareStatement("SELECT id FROM robots WHERE id = ?")) {
            statement.setString(1, robotId);

            try (ResultSet rows = statement.executeQuery()) {
                return rows.next();
            }
        }
    }

    /** Danh sách linh kiện bắt buộc kèm số lượng (bảng quan hệ robot_components). */
    public List<RobotComponent> parts(String robotId) throws SQLException {
        String sql = "SELECT component_id, quantity FROM robot_components"
                + " WHERE robot_id = ? ORDER BY component_id";

        List<RobotComponent> parts = new ArrayList<>();
        try (Connection connection = connections.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, robotId);

            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    parts.add(new RobotComponent(
                            rows.getString("component_id"), rows.getInt("quantity")));
                }
            }
        }
        return parts;
    }
    /** Dùng để chặn việc tick một linh kiện không thuộc robot của phiên. */
    public boolean hasComponent(String robotId, String componentId) throws SQLException {
        try (Connection connection = connections.openConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT component_id FROM robot_components"
                             + " WHERE robot_id = ? AND component_id = ?")) {
            statement.setString(1, robotId);
            statement.setString(2, componentId);

            try (ResultSet rows = statement.executeQuery()) {
                return rows.next();
            }
        }
    }

    /* Cột wiring có kiểu JSON trong MySQL nên cần CAST(? AS JSON). */
    public Robot create(Robot robot) throws SQLException {
        String sql = "INSERT INTO robots"
                + " (id, name, level, summary, image, build_time, main_sensor, skills, wiring)"
                + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, CAST(? AS JSON))";

        try (Connection connection = connections.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, robot, false);
            statement.executeUpdate();
            return robot;
        }
    }

    public Robot update(String id, Robot robot) throws SQLException {
        String sql = "UPDATE robots"
                + " SET name = ?, level = ?, summary = ?, image = ?, build_time = ?,"
                + " main_sensor = ?, skills = ?, wiring = CAST(? AS JSON)"
                + " WHERE id = ?";

        try (Connection connection = connections.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, robot, true);
            statement.setString(9, id);
            if (statement.executeUpdate() == 0) return null;

            return new Robot(id, robot.name(), robot.level(), robot.summary(), robot.image(),
                    robot.buildTime(), robot.mainSensor(), robot.skills(), robot.wiringJson());
        }
    }

    public boolean remove(String id) throws SQLException {
        try (Connection connection = connections.openConnection();
             PreparedStatement statement =
                     connection.prepareStatement("DELETE FROM robots WHERE id = ?")) {
            statement.setString(1, id);
            return statement.executeUpdate() > 0;
        }
    }

    /* INSERT có cột id ở đầu, UPDATE thì không — skipId cho biết nên bắt đầu
       gán tham số từ vị trí nào. */
    private void bind(PreparedStatement statement, Robot robot, boolean skipId) throws SQLException {
        int index = 1;
        if (!skipId) statement.setString(index++, robot.id());
        statement.setString(index++, robot.name());
        statement.setString(index++, robot.level());
        statement.setString(index++, robot.summary());
        statement.setString(index++, robot.image());
        statement.setString(index++, robot.buildTime());
        statement.setString(index++, robot.mainSensor());
        statement.setString(index++, robot.skills());
        statement.setString(index, robot.wiringJson());
    }

    private Robot map(ResultSet row) throws SQLException {
        return new Robot(row.getString("id"), row.getString("name"), row.getString("level"),
                row.getString("summary"), row.getString("image"), row.getString("build_time"),
                row.getString("main_sensor"), row.getString("skills"), row.getString("wiring"));
    }
}
