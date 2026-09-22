package vn.edu.webpro.robotlab.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import vn.edu.webpro.robotlab.model.Component;
import vn.edu.webpro.robotlab.model.ComponentPage;

/** Data access only: parameterized SQL is kept out of the Servlet. */
public final class ComponentDao {
    private final DatabaseConnectionFactory connections;

    public ComponentDao(DatabaseConnectionFactory connections) {
        this.connections = connections;
    }

    public ComponentPage list(int page, int limit) throws SQLException {
        String query = "SELECT id, name, category, image, description, specs "
                + "FROM components ORDER BY id LIMIT ? OFFSET ?";
        String countQuery = "SELECT COUNT(*) AS total FROM components";
        List<Component> components = new ArrayList<>();

        try (Connection connection = connections.openConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             PreparedStatement countStatement = connection.prepareStatement(countQuery)) {
            statement.setInt(1, limit);
            statement.setInt(2, (page - 1) * limit);
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    components.add(new Component(
                            rows.getString("id"), rows.getString("name"), rows.getString("category"),
                            rows.getString("image"), rows.getString("description"), rows.getString("specs")
                    ));
                }
            }
            try (ResultSet count = countStatement.executeQuery()) {
                count.next();
                return new ComponentPage(components, page, limit, count.getLong("total"));
            }
        }
    }

    public boolean remove(String componentId) throws SQLException {
        try (Connection connection = connections.openConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM components WHERE id = ?")) {
            statement.setString(1, componentId);
            return statement.executeUpdate() > 0;
        }
    }
    /* Cột specs có kiểu JSON trong MySQL, nên chuỗi phải đi qua CAST(? AS JSON)
       thay vì gán thẳng như một chuỗi thường. */
    public Component create(Component component) throws SQLException {
        String sql = "INSERT INTO components (id, name, category, image, description, specs)"
                + " VALUES (?, ?, ?, ?, ?, CAST(? AS JSON))";

        try (Connection connection = connections.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, component.id());
            statement.setString(2, component.name());
            statement.setString(3, component.category());
            statement.setString(4, component.image());
            statement.setString(5, component.description());
            statement.setString(6, component.specsJson());
            statement.executeUpdate();
            return component;
        }
    }

    public Component update(String id, Component component) throws SQLException {
        String sql = "UPDATE components"
                + " SET name = ?, category = ?, image = ?, description = ?, specs = CAST(? AS JSON)"
                + " WHERE id = ?";

        try (Connection connection = connections.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, component.name());
            statement.setString(2, component.category());
            statement.setString(3, component.image());
            statement.setString(4, component.description());
            statement.setString(5, component.specsJson());
            statement.setString(6, id);
            if (statement.executeUpdate() == 0) return null;

            return new Component(id, component.name(), component.category(),
                    component.image(), component.description(), component.specsJson());
        }
    }
}
