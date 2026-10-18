package vn.edu.webpro.robotlab.data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/** Đóng tài nguyên JDBC trong khối finally, theo Chapter 12 slide 52-53. */
public class DBUtil {

    public static void closeStatement(Statement s) {
        try {
            if (s != null) {
                s.close();
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
    }

    public static void closePreparedStatement(Statement ps) {
        // Đóng statement trong finally; ResultSet được đóng trước và connection trả về pool sau.
        try {
            if (ps != null) {
                ps.close();
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
    }

    public static void closeResultSet(ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
    }
}
