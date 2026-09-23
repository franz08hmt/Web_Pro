package vn.edu.webpro.robotlab.data;

import java.sql.Connection;
import java.sql.SQLException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * Connection pool theo Chapter 12, slide 35-37.
 *
 * Tomcat tạo pool từ Resource "jdbc/robotlab" khai báo trong META-INF/context.xml.
 * Các lớp XxxDB lấy kết nối bằng getInstance().getConnection() và trả lại bằng
 * freeConnection() trong khối finally.
 *
 * Khác slide có chủ đích: getConnection() ném SQLException thay vì in lỗi rồi
 * trả null, để servlet trả mã 503 "cơ sở dữ liệu chưa sẵn sàng" thay vì gặp
 * NullPointerException ở dòng connection.prepareStatement(...).
 */
public class ConnectionPool {
    private static final String JNDI_NAME = "java:/comp/env/jdbc/robotlab";

    private static ConnectionPool pool = null;
    private static DataSource dataSource = null;

    private ConnectionPool() {
        try {
            InitialContext ic = new InitialContext();
            dataSource = (DataSource) ic.lookup(JNDI_NAME);
        } catch (NamingException e) {
            System.out.println(e);
        }
    }

    public static synchronized ConnectionPool getInstance() {
        if (pool == null) {
            pool = new ConnectionPool();
        }
        return pool;
    }

    public Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("Chưa cấu hình Resource jdbc/robotlab trong context.xml.");
        }
        return dataSource.getConnection();
    }

    public void freeConnection(Connection c) {
        try {
            if (c != null) {
                c.close();
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
    }
}
