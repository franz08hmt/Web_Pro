package vn.edu.webpro.robotlab.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import vn.edu.webpro.robotlab.model.AssemblyStep;

/** Truy cập bảng assembly_steps bằng JDBC. */
public final class AssemblyStepDao {
    private static final String COLUMNS =
            "id, robot_id, step_order, title, instruction, illustration";

    private final DatabaseConnectionFactory connections;

    public AssemblyStepDao(DatabaseConnectionFactory connections) {
        this.connections = connections;
    }

    public List<AssemblyStep> listByRobot(String robotId, int limit, int offset) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM assembly_steps"
                + " WHERE robot_id = ? ORDER BY step_order, id LIMIT ? OFFSET ?";

        List<AssemblyStep> steps = new ArrayList<>();
        try (Connection connection = connections.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, robotId);
            statement.setInt(2, limit);
            statement.setInt(3, offset);

            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) steps.add(map(rows));
            }
        }
        return steps;
    }

    public List<AssemblyStep> list(int limit, int offset) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM assembly_steps"
                + " ORDER BY robot_id, step_order, id LIMIT ? OFFSET ?";

        List<AssemblyStep> data = new ArrayList<>();
        try (Connection connection = connections.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, limit);
            statement.setInt(2, offset);

            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) data.add(map(rows));
            }
        }
        return data;
    }

    public long countByRobot(String robotId) throws SQLException {
        try (Connection connection = connections.openConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT COUNT(*) AS total FROM assembly_steps WHERE robot_id = ?")) {
            statement.setString(1, robotId);

            try (ResultSet rows = statement.executeQuery()) {
                rows.next();
                return rows.getLong("total");
            }
        }
    }

    /** Dùng để chặn việc đánh dấu một bước không thuộc robot của phiên. */
    public boolean belongsToRobot(String robotId, String stepId) throws SQLException {
        try (Connection connection = connections.openConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT id FROM assembly_steps WHERE robot_id = ? AND id = ?")) {
            statement.setString(1, robotId);
            statement.setString(2, stepId);

            try (ResultSet rows = statement.executeQuery()) {
                return rows.next();
            }
        }
    }

    /* Cột illustration có kiểu JSON trong MySQL nên cần CAST(? AS JSON). */
    public AssemblyStep create(AssemblyStep step) throws SQLException {
        String sql = "INSERT INTO assembly_steps (" + COLUMNS + ")"
                + " VALUES (?, ?, ?, ?, ?, CAST(? AS JSON))";

        try (Connection connection = connections.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, step, false);
            statement.executeUpdate();
            return step;
        }
    }

    public AssemblyStep update(String id, AssemblyStep step) throws SQLException {
        String sql = "UPDATE assembly_steps"
                + " SET robot_id = ?, step_order = ?, title = ?, instruction = ?,"
                + " illustration = CAST(? AS JSON)"
                + " WHERE id = ?";

        try (Connection connection = connections.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, step, true);
            statement.setString(6, id);
            if (statement.executeUpdate() == 0) return null;

            return new AssemblyStep(id, step.robotId(), step.stepOrder(),
                    step.title(), step.instruction(), step.illustrationJson());
        }
    }

    public boolean remove(String id) throws SQLException {
        try (Connection connection = connections.openConnection();
             PreparedStatement statement =
                     connection.prepareStatement("DELETE FROM assembly_steps WHERE id = ?")) {
            statement.setString(1, id);
            return statement.executeUpdate() > 0;
        }
    }

    private AssemblyStep map(ResultSet rows) throws SQLException {
        return new AssemblyStep(
                rows.getString("id"),
                rows.getString("robot_id"),
                rows.getInt("step_order"),
                rows.getString("title"),
                rows.getString("instruction"),
                rows.getString("illustration")
        );
    }

    /* INSERT có cột id ở đầu, UPDATE thì không — skipId cho biết nên bắt đầu
       gán tham số từ vị trí nào. */
    private void bind(PreparedStatement statement, AssemblyStep step, boolean skipId)
            throws SQLException {
        int index = 1;
        if (!skipId) statement.setString(index++, step.id());
        statement.setString(index++, step.robotId());
        statement.setInt(index++, step.stepOrder());
        statement.setString(index++, step.title());
        statement.setString(index++, step.instruction());
        statement.setString(index, step.illustrationJson());
    }
}
