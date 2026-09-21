package vn.edu.webpro.robotlab.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import vn.edu.webpro.robotlab.model.AssemblyStep;

public final class AssemblyStepDao {
    private final DatabaseConnectionFactory connections;
    public AssemblyStepDao(DatabaseConnectionFactory connections) { this.connections = connections; }

    public List<AssemblyStep> listByRobot(String robotId, int limit, int offset) throws SQLException {
        String sql = "SELECT id, robot_id, step_order, title, instruction, illustration FROM assembly_steps "
                + "WHERE robot_id = ? ORDER BY step_order, id LIMIT ? OFFSET ?";
        List<AssemblyStep> steps = new ArrayList<>();
        try (Connection connection = connections.openConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, robotId); statement.setInt(2, limit); statement.setInt(3, offset);
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) steps.add(new AssemblyStep(rows.getString("id"), rows.getString("robot_id"),
                        rows.getInt("step_order"), rows.getString("title"), rows.getString("instruction"), rows.getString("illustration")));
            }
        }
        return steps;
    }

    public long countByRobot(String robotId) throws SQLException {
        try (Connection connection = connections.openConnection(); PreparedStatement statement = connection.prepareStatement(
                "SELECT COUNT(*) AS total FROM assembly_steps WHERE robot_id = ?")) {
            statement.setString(1, robotId);
            try (ResultSet rows = statement.executeQuery()) { rows.next(); return rows.getLong("total"); }
        }
    }
}
