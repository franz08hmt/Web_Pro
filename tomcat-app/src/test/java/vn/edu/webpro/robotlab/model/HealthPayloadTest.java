package vn.edu.webpro.robotlab.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;
import vn.edu.webpro.robotlab.dao.DatabaseConnectionFactory;
import vn.edu.webpro.robotlab.service.DatabaseHealthService;
import vn.edu.webpro.robotlab.service.PasswordService;

final class HealthPayloadTest {
    @Test
    void keepsTheApiEnvelopeAndReportsMissingDatabaseConfiguration() {
        HealthPayload payload = new HealthPayload("robot-assembly-lab-api", "ok", "connected");
        assertEquals(
                "{\"data\":{\"service\":\"robot-assembly-lab-api\",\"status\":\"ok\",\"database\":\"connected\"}}",
                payload.toJson()
        );
        assertEquals("not_configured", new DatabaseHealthService(Map.of()).check());
    }

    @Test
    void loadsTheJdbcDriverPackagedWithTheApplication() throws Exception {
        DatabaseConnectionFactory.ensureDriverLoaded();
    }

    @Test
    void preservesJsonColumnsInApiModels() {
        Component component = new Component("battery-holder", "Hộp pin AA 4", "Nguồn điện",
                "/assets/images/battery.png", "Cấp nguồn.", "{\"Cấu hình\":\"4 viên AA\"}");
        assertTrue(component.toJson().contains("\"specs\":{\"Cấu hình\":\"4 viên AA\"}"));

        Robot robot = new Robot("line-follower", "Robot dò đường", "Cơ bản", "Theo vạch",
                "/assets/robot.png", "90 phút", "Cảm biến line", "PWM", "[\"VCC\",\"GND\"]");
        assertTrue(robot.toJson().contains("\"wiring\":[\"VCC\",\"GND\"]"));
    }

    @Test
    void verifiesOnlyTheOriginalPassword() {
        PasswordService passwords = new PasswordService();
        String hash = passwords.hash("MatKhauAnToan123!");
        assertTrue(passwords.verify("MatKhauAnToan123!", hash));
        assertFalse(passwords.verify("sai-mat-khau", hash));
    }
}
