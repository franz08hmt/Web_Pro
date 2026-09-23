package vn.edu.webpro.robotlab.data;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/** Kiểm tra kết nối MySQL bằng câu truy vấn nhỏ nhất: SELECT 1. */
public class HealthDB {

    public static String selectDatabaseStatus() {
        ConnectionPool pool = ConnectionPool.getInstance();
        Connection connection = null;
        Statement statement = null;
        try {
            connection = pool.getConnection();
            statement = connection.createStatement();
            statement.execute("SELECT 1");
            return "connected";
        } catch (SQLException e) {
            System.out.println(e);
            return "unavailable";
        } finally {
            DBUtil.closeStatement(statement);
            pool.freeConnection(connection);
        }
    }
}
