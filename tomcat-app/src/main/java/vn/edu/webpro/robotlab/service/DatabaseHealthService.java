package vn.edu.webpro.robotlab.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import vn.edu.webpro.robotlab.dao.DatabaseConnectionFactory;

/** Checks the configured database with the smallest read-only query: SELECT 1. */
public final class DatabaseHealthService {
    private final Map<String, String> environment;

    public DatabaseHealthService(Map<String, String> environment) {
        this.environment = environment;
    }

    public String check() {
        if (!isConfigured()) return "not_configured";

        String jdbcUrl = "jdbc:mysql://" + environment.get("DB_HOST") + ":"
                + environment.get("DB_PORT") + "/" + environment.get("DB_NAME")
                + "?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Ho_Chi_Minh";

        try {
            DatabaseConnectionFactory.ensureDriverLoaded();
        } catch (SQLException exception) {
            return "unavailable";
        }

        try (Connection connection = DriverManager.getConnection(
                jdbcUrl, environment.get("DB_USER"), environment.get("DB_PASSWORD"));
             Statement statement = connection.createStatement()) {
            statement.execute("SELECT 1");
            return "connected";
        } catch (SQLException exception) {
            return "unavailable";
        }
    }

    private boolean isConfigured() {
        return hasValue("DB_HOST") && hasValue("DB_PORT") && hasValue("DB_NAME")
                && hasValue("DB_USER") && hasValue("DB_PASSWORD");
    }

    private boolean hasValue(String key) {
        String value = environment.get(key);
        return value != null && !value.isBlank();
    }
}
