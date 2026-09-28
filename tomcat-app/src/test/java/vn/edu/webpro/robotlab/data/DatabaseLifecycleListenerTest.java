package vn.edu.webpro.robotlab.data;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Proxy;
import java.sql.Driver;
import java.sql.DriverAction;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.annotation.WebListener;
import org.junit.jupiter.api.Test;

final class DatabaseLifecycleListenerTest {

    @Test
    void contextShutdownDeregistersWebappDrivers() throws SQLException {
        AtomicBoolean deregistered = new AtomicBoolean();
        Driver driver = (Driver) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[] { Driver.class },
                (proxy, method, arguments) -> null);
        DriverAction action = () -> deregistered.set(true);
        DriverManager.registerDriver(driver, action);

        try {
            assertTrue(isRegistered(driver));

            new DatabaseLifecycleListener().contextDestroyed(
                    new ServletContextEvent(emptyServletContext()));

            assertFalse(isRegistered(driver));
            assertTrue(deregistered.get());
        } finally {
            if (isRegistered(driver)) DriverManager.deregisterDriver(driver);
        }
    }

    @Test
    void listenerIsDiscoveredByTheServletContainer() {
        assertTrue(DatabaseLifecycleListener.class.isAnnotationPresent(WebListener.class));
    }

    private boolean isRegistered(Driver expected) {
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            if (drivers.nextElement() == expected) return true;
        }
        return false;
    }

    private ServletContext emptyServletContext() {
        return (ServletContext) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[] { ServletContext.class },
                (proxy, method, arguments) -> null);
    }
}
