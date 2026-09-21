package vn.edu.webpro.robotlab.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;

/** Centralizes JDBC connection configuration outside controllers and JSPs. */
public final class DatabaseConnectionFactory {
    private final Map<String, String> environment;

    public DatabaseConnectionFactory(Map<String, String> environment) {
        this.environment = environment;
    }

    public boolean isConfigured() {
        return hasValue("DB_HOST") && hasValue("DB_PORT") && hasValue("DB_NAME")
                && hasValue("DB_USER") && hasValue("DB_PASSWORD");
    }

    public Connection openConnection() throws SQLException {
        if (!isConfigured()) {
            throw new IllegalStateException("Database configuration is incomplete.");
        }
        String url = "jdbc:mysql://" + environment.get("DB_HOST") + ":" + environment.get("DB_PORT")
                + "/" + environment.get("DB_NAME")
                + "?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Ho_Chi_Minh";
        return DriverManager.getConnection(url, environment.get("DB_USER"), environment.get("DB_PASSWORD"));
    }

    private boolean hasValue(String key) {
        String value = environment.get(key);
        return value != null && !value.isBlank();
    }
}
