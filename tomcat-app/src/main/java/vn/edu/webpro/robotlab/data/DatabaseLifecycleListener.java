package vn.edu.webpro.robotlab.data;

import com.mysql.cj.jdbc.AbandonedConnectionCleanupThread;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * Releases JDBC resources owned by this web application when Tomcat undeploys it.
 * The Tomcat-managed DataSource remains owned and closed by Tomcat itself.
 */
@WebListener
public final class DatabaseLifecycleListener implements ServletContextListener {

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        ServletContext context = event.getServletContext();

        try {
            AbandonedConnectionCleanupThread.checkedShutdown();
        } catch (RuntimeException e) {
            context.log("Could not stop the MySQL Connector/J cleanup thread.", e);
        }

        ClassLoader webAppClassLoader = getClass().getClassLoader();
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            if (driver.getClass().getClassLoader() != webAppClassLoader) {
                continue;
            }
            try {
                DriverManager.deregisterDriver(driver);
            } catch (SQLException e) {
                context.log("Could not deregister a JDBC driver loaded by this web application.", e);
            }
        }
    }
}
