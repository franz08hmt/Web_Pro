package vn.edu.webpro.robotlab.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import vn.edu.webpro.robotlab.model.AssemblySession;
import vn.edu.webpro.robotlab.model.SessionComponent;
import vn.edu.webpro.robotlab.model.SessionStep;

/**
 * Truy cập phiên lắp ráp và ba bảng tiến độ đi kèm.
 *
 * Mọi câu truy vấn đều kèm user_id để một tài khoản không đọc hay sửa được
 * phiên của tài khoản khác, kể cả khi đoán đúng ID phiên.
 */
public final class AssemblySessionDao {
    private final DatabaseConnectionFactory connections;

    public AssemblySessionDao(DatabaseConnectionFactory connections) {
        this.connections = connections;
    }

    /** Trả về phiên đang mở của robot này, hoặc tạo mới nếu chưa có. */
    public AssemblySession createOrResume(long userId, String robotId) throws SQLException {
        String findOpen = "SELECT id, user_id, robot_id, status FROM assembly_sessions"
                + " WHERE user_id = ? AND robot_id = ?"
                + " AND status IN ('PREPARING', 'READY', 'IN_PROGRESS')"
                + " ORDER BY updated_at DESC, id DESC LIMIT 1";
        String insert = "INSERT INTO assembly_sessions (user_id, robot_id, status)"
                + " VALUES (?, ?, 'PREPARING')";

        try (Connection connection = connections.openConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(findOpen)) {
                statement.setLong(1, userId);
                statement.setString(2, robotId);

                try (ResultSet rows = statement.executeQuery()) {
                    if (rows.next()) return hydrate(connection, rows);
                }
            }

            try (PreparedStatement statement =
                         connection.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
                statement.setLong(1, userId);
                statement.setString(2, robotId);
                statement.executeUpdate();

                try (ResultSet keys = statement.getGeneratedKeys()) {
                    keys.next();
                    return find(connection, keys.getLong(1), userId);
                }
            }
        }
    }

    public AssemblySession find(long id, long userId) throws SQLException {
        try (Connection connection = connections.openConnection()) {
            return find(connection, id, userId);
        }
    }

    public List<AssemblySession> list(long userId) throws SQLException {
        String sql = "SELECT id, user_id, robot_id, status FROM assembly_sessions"
                + " WHERE user_id = ? ORDER BY updated_at DESC, id DESC";

        List<AssemblySession> items = new ArrayList<>();
        try (Connection connection = connections.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);

            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) items.add(hydrate(connection, rows));
            }
        }
        return items;
    }

    public AssemblySession updateStatus(long id, long userId, String status) throws SQLException {
        return change(id, userId, (connection, session) -> {
            try (PreparedStatement statement = connection.prepareStatement(
                    "UPDATE assembly_sessions SET status = ? WHERE id = ?")) {
                statement.setString(1, status);
                statement.setLong(2, id);
                statement.executeUpdate();
            }
        });
    }

    public AssemblySession setComponent(long id, long userId, String componentId, boolean prepared)
            throws SQLException {
        String sql = "INSERT INTO session_components (session_id, robot_id, component_id, prepared)"
                + " VALUES (?, ?, ?, ?)"
                + " ON DUPLICATE KEY UPDATE prepared = VALUES(prepared)";

        return change(id, userId, (connection, session) -> {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, id);
                statement.setString(2, session.robotId());
                statement.setString(3, componentId);
                statement.setBoolean(4, prepared);
                statement.executeUpdate();
            }
        });
    }

    public AssemblySession setStep(long id, long userId, String stepId, String status)
            throws SQLException {
        String sql = "INSERT INTO session_steps (session_id, robot_id, step_id, completed)"
                + " VALUES (?, ?, ?, ?)"
                + " ON DUPLICATE KEY UPDATE completed = VALUES(completed)";

        return change(id, userId, (connection, session) -> {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, id);
                statement.setString(2, session.robotId());
                statement.setString(3, stepId);
                statement.setBoolean(4, "COMPLETED".equals(status));
                statement.executeUpdate();
            }
        });
    }

    /** Lưu trạng thái part 3D: lắp thì thêm dòng, gỡ thì xóa dòng. */
    public AssemblySession setVisual(long id, long userId, String componentId, boolean assembled)
            throws SQLException {
        String insert = "INSERT INTO session_visual_parts (session_id, robot_id, component_id)"
                + " VALUES (?, ?, ?)"
                + " ON DUPLICATE KEY UPDATE component_id = VALUES(component_id)";
        String delete = "DELETE FROM session_visual_parts WHERE session_id = ? AND component_id = ?";

        return change(id, userId, (connection, session) -> {
            try (PreparedStatement statement =
                         connection.prepareStatement(assembled ? insert : delete)) {
                statement.setLong(1, id);
                if (assembled) {
                    statement.setString(2, session.robotId());
                    statement.setString(3, componentId);
                } else {
                    statement.setString(2, componentId);
                }
                statement.executeUpdate();
            }
        });
    }

    /* Khung dùng chung cho mọi thao tác ghi: xác nhận phiên thuộc đúng chủ,
       chạy phần việc riêng, rồi đọc lại phiên để trả về trạng thái mới nhất. */
    private AssemblySession change(long id, long userId, Work work) throws SQLException {
        try (Connection connection = connections.openConnection()) {
            AssemblySession session = find(connection, id, userId);
            if (session == null) return null;

            work.run(connection, session);
            return find(connection, id, userId);
        }
    }

    private AssemblySession find(Connection connection, long id, long userId) throws SQLException {
        String sql = "SELECT id, user_id, robot_id, status FROM assembly_sessions"
                + " WHERE id = ? AND user_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            statement.setLong(2, userId);

            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() ? hydrate(connection, rows) : null;
            }
        }
    }

    /** Ghép một dòng assembly_sessions với ba bảng tiến độ thành model đầy đủ. */
    private AssemblySession hydrate(Connection connection, ResultSet row) throws SQLException {
        long id = row.getLong("id");
        List<SessionComponent> components = new ArrayList<>();
        List<SessionStep> steps = new ArrayList<>();
        List<String> assembledPartIds = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT component_id, prepared FROM session_components WHERE session_id = ?")) {
            statement.setLong(1, id);
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    components.add(new SessionComponent(rows.getString(1), rows.getBoolean(2)));
                }
            }
        }

        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT step_id, completed FROM session_steps WHERE session_id = ?")) {
            statement.setLong(1, id);
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    steps.add(new SessionStep(rows.getString(1),
                            rows.getBoolean(2) ? "COMPLETED" : "PENDING"));
                }
            }
        }

        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT component_id FROM session_visual_parts WHERE session_id = ?")) {
            statement.setLong(1, id);
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) assembledPartIds.add(rows.getString(1));
            }
        }

        return new AssemblySession(id, row.getLong("user_id"), row.getString("robot_id"),
                row.getString("status"), components, steps, assembledPartIds, 0);
    }

    /** Phần việc ghi riêng của từng thao tác, chạy bên trong change(). */
    @FunctionalInterface
    private interface Work {
        void run(Connection connection, AssemblySession session) throws SQLException;
    }
}
