package vn.edu.webpro.robotlab.model;

import java.util.Map;
import vn.edu.webpro.robotlab.service.DatabaseHealthService;

/** Lightweight no-dependency test, runnable with the JDK before Maven is installed. */
public final class HealthPayloadTest {
    public static void main(String[] args) {
        HealthPayload payload = new HealthPayload("robot-assembly-lab-api", "ok", "connected");
        String expected = "{\"data\":{\"service\":\"robot-assembly-lab-api\",\"status\":\"ok\",\"database\":\"connected\"}}";
        if (!expected.equals(payload.toJson())) {
            throw new AssertionError("Health response must preserve the existing API envelope.");
        }

        String databaseStatus = new DatabaseHealthService(Map.of()).check();
        if (!"not_configured".equals(databaseStatus)) {
            throw new AssertionError("A missing database configuration must not attempt a connection.");
        }

        Component component = new Component("battery-holder", "Hộp pin AA 4", "Nguồn điện",
                "/assets/images/battery.png", "Cấp nguồn.", "{\"Cấu hình\":\"4 viên AA\"}");
        if (!component.toJson().contains("\"specs\":{\"Cấu hình\":\"4 viên AA\"}")) {
            throw new AssertionError("Component JSON must keep MySQL JSON columns as JSON objects.");
        }

        Robot robot = new Robot("line-follower", "Robot dò đường", "Cơ bản", "Theo vạch", "/assets/robot.png",
                "90 phút", "Cảm biến line", "PWM", "[\"VCC\",\"GND\"]");
        if (!robot.toJson().contains("\"wiring\":[\"VCC\",\"GND\"]")) {
            throw new AssertionError("Robot JSON must keep MySQL JSON columns as arrays.");
        }
    }
}
