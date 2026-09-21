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
    private final DatabaseConnectionFactory connections;

    public RobotDao(DatabaseConnectionFactory connections) { this.connections = connections; }

    public List<Robot> list(int limit, int offset) throws SQLException {
        String sql = "SELECT id, name, level, summary, image, build_time, main_sensor, skills, wiring "
                + "FROM robots ORDER BY id LIMIT ? OFFSET ?";
        List<Robot> robots = new ArrayList<>();
        try (Connection connection = connections.openConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, limit); statement.setInt(2, offset);
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) robots.add(map(rows));
            }
        }
        return robots;
    }

    public long count() throws SQLException {
        try (Connection connection = connections.openConnection(); PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) AS total FROM robots"); ResultSet rows = statement.executeQuery()) {
            rows.next(); return rows.getLong("total");
        }
    }

    public boolean exists(String robotId) throws SQLException {
        try (Connection connection = connections.openConnection(); PreparedStatement statement = connection.prepareStatement("SELECT id FROM robots WHERE id = ?")) {
            statement.setString(1, robotId);
            try (ResultSet rows = statement.executeQuery()) { return rows.next(); }
        }
    }

    public List<RobotComponent> parts(String robotId) throws SQLException {
        List<RobotComponent> parts = new ArrayList<>();
        try (Connection connection = connections.openConnection(); PreparedStatement statement = connection.prepareStatement(
                "SELECT component_id, quantity FROM robot_components WHERE robot_id = ? ORDER BY component_id")) {
            statement.setString(1, robotId);
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) parts.add(new RobotComponent(rows.getString("component_id"), rows.getInt("quantity")));
            }
        }
        return parts;
    }

    private Robot map(ResultSet row) throws SQLException {
        return new Robot(row.getString("id"), row.getString("name"), row.getString("level"),
                row.getString("summary"), row.getString("image"), row.getString("build_time"),
                row.getString("main_sensor"), row.getString("skills"), row.getString("wiring"));
    }
}
