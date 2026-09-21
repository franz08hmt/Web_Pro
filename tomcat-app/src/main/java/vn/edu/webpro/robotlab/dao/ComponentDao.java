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
}
