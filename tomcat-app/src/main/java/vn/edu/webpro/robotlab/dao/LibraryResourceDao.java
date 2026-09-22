package vn.edu.webpro.robotlab.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import vn.edu.webpro.robotlab.model.LibraryResource;

/**
 * Truy cập bảng library_resources bằng JDBC.
 *
 * Toàn bộ giá trị do người dùng nhập đều đi qua tham số "?" của
 * PreparedStatement, không nối chuỗi vào câu SQL.
 */
public final class LibraryResourceDao {
    private final DatabaseConnectionFactory connections;

    public LibraryResourceDao(DatabaseConnectionFactory connections) {
        this.connections = connections;
    }

    public List<LibraryResource> list(int limit, int offset, String robotId) throws SQLException {
        // Chỉ mệnh đề WHERE cố định được ghép thêm, giá trị robotId vẫn là tham số.
        String sql = "SELECT id, robot_id, title, type, url, description FROM library_resources"
                + (robotId == null ? "" : " WHERE robot_id = ?")
                + " ORDER BY id LIMIT ? OFFSET ?";

        List<LibraryResource> data = new ArrayList<>();
        try (Connection connection = connections.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            int index = 1;
            if (robotId != null) statement.setString(index++, robotId);
            statement.setInt(index++, limit);
            statement.setInt(index, offset);

            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) data.add(map(rows));
            }
        }
        return data;
    }

    public long count(String robotId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM library_resources"
                + (robotId == null ? "" : " WHERE robot_id = ?");

        try (Connection connection = connections.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (robotId != null) statement.setString(1, robotId);

            try (ResultSet rows = statement.executeQuery()) {
                rows.next();
                return rows.getLong(1);
            }
        }
    }

    public LibraryResource create(LibraryResource resource) throws SQLException {
        String sql = "INSERT INTO library_resources (id, robot_id, title, type, url, description)"
                + " VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = connections.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, resource, false);
            statement.executeUpdate();
            return resource;
        }
    }

    public LibraryResource update(String id, LibraryResource resource) throws SQLException {
        String sql = "UPDATE library_resources"
                + " SET robot_id = ?, title = ?, type = ?, url = ?, description = ?"
                + " WHERE id = ?";

        try (Connection connection = connections.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, resource, true);
            statement.setString(6, id);
            if (statement.executeUpdate() == 0) return null;

            return new LibraryResource(id, resource.robotId(), resource.title(),
                    resource.type(), resource.url(), resource.description());
        }
    }

    public boolean remove(String id) throws SQLException {
        try (Connection connection = connections.openConnection();
             PreparedStatement statement =
                     connection.prepareStatement("DELETE FROM library_resources WHERE id = ?")) {
            statement.setString(1, id);
            return statement.executeUpdate() > 0;
        }
    }

    private LibraryResource map(ResultSet rows) throws SQLException {
        return new LibraryResource(
                rows.getString("id"),
                rows.getString("robot_id"),
                rows.getString("title"),
                rows.getString("type"),
                rows.getString("url"),
                rows.getString("description")
        );
    }

    /* INSERT có cột id ở đầu, UPDATE thì không — skipId cho biết nên bắt đầu
       gán tham số từ vị trí nào. */
    private void bind(PreparedStatement statement, LibraryResource resource, boolean skipId)
            throws SQLException {
        int index = 1;
        if (!skipId) statement.setString(index++, resource.id());
        statement.setString(index++, resource.robotId());
        statement.setString(index++, resource.title());
        statement.setString(index++, resource.type());
        statement.setString(index++, resource.url());
        statement.setString(index, resource.description());
    }
}
