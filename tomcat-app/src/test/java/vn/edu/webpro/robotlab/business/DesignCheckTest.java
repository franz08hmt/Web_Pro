package vn.edu.webpro.robotlab.business;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class DesignCheckTest {
    static DesignLayout complete(String goal) {
        DesignLayout layout = new DesignLayout();
        layout.getDesign().setGoalCode(goal);
        String[] parts = {"chassis-2wd", "arduino-uno", "l298n", "dc-motor", "wheel", "caster-wheel",
                "battery-holder", "line-sensor", "hc-sr04", "sg90"};
        String[] roles = {"BASE", "CONTROLLER", "MOTOR_DRIVER", "DRIVE_MOTOR", "WHEEL", "SUPPORT_WHEEL",
                "POWER", "LINE_SENSOR", "DISTANCE_SENSOR", "SERVO"};
        int[] quantities = {1, 1, 1, 2, 2, 1, 1, goal.contains("LINE") || "COMBO".equals(goal) ? 2 : 0,
                "BASIC_2WD".equals(goal) || "LINE_FOLLOWER".equals(goal) ? 0 : 1,
                "SCANNING_AVOIDER".equals(goal) ? 1 : 0};
        long id = 0;
        for (int n = 0; n < parts.length; n++) {
            DesignComponentRole role = new DesignComponentRole();
            role.setComponentId(parts[n]);
            role.setRole(roles[n]);
            role.setDriverChannels("MOTOR_DRIVER".equals(roles[n]) ? 2 : 0);
            layout.getRules().getRoles().add(role);
            DesignRequirement requirement = new DesignRequirement();
            requirement.setGoalCode(goal);
            requirement.setRole(roles[n]);
            requirement.setMinCount(quantities[n]);
            requirement.setMessage("Cần " + roles[n]);
            layout.getRules().getRequirements().add(requirement);
            DesignLayoutTest.fit(layout, parts[n], n == 0 ? "ROOT" : "TYPE" + n, "Y0");
            for (int q = 0; q < quantities[n]; q++) {
                DesignInstance item = new DesignInstance();
                item.setId(++id);
                item.setComponentId(parts[n]);
                item.setName(parts[n]);
                layout.getInstances().add(item);
                if (n == 0) {
                    DesignLayoutTest.placed(layout, id, 0, "base", "Y0");
                } else {
                    DesignLayoutTest.mount(layout, "chassis-2wd", "mount-" + id, "TYPE" + n, 0, .1, -.2);
                    DesignLayoutTest.placed(layout, id, 1, "mount-" + id, "Y0");
                }
            }
        }
        if ("SCANNING_AVOIDER".equals(goal)) {
            DesignInstance servo = find(layout, "sg90");
            DesignLayoutTest.mount(layout, "sg90", "horn", "SERVO_HORN", 0, .2, 0);
            DesignLayoutTest.fit(layout, "hc-sr04", "SERVO_HORN", "Y0");
            DesignPlacement sensor = layout.findPlacement(find(layout, "hc-sr04").getId());
            sensor.setHostInstanceId(servo.getId());
            sensor.setMountCode("horn");
        }
        return layout;
    }

    static DesignInstance find(DesignLayout layout, String component) {
        for (DesignInstance item : layout.getInstances()) {
            if (component.equals(item.getComponentId())) {
                return item;
            }
        }
        return null;
    }

    static DesignCheck evaluate(DesignLayout layout) {
        DesignCheck report = new DesignCheck();
        report.evaluate(layout);
        return report;
    }

    static boolean has(DesignCheck report, String result, String phrase) {
        for (DesignCheckItem item : report.getItems()) {
            if (result.equals(item.getResult()) && item.getMessage().contains(phrase)) {
                return true;
            }
        }
        return false;
    }

    @Test
    void everySupportedGoalCanPassWithoutCreatingAnyNewGrade() {
        String[] goals = {"BASIC_2WD", "LINE_FOLLOWER", "OBSTACLE_AVOIDER", "SCANNING_AVOIDER", "COMBO"};
        for (String goal : goals) {
            assertTrue(evaluate(complete(goal)).isPassed(), goal);
        }
    }

    @Test
    void removedDriverFailsAndUnplacedPartsAreNotCounted() {
        DesignLayout layout = complete("OBSTACLE_AVOIDER");
        layout.getPlacements().remove(layout.findPlacement(find(layout, "l298n").getId()));
        DesignCheck report = evaluate(layout);
        assertFalse(report.isPassed());
        assertTrue(has(report, "FAIL", "Động cơ DC cần mạch"));
        assertTrue(has(report, "WARN", "còn trong khay"));
    }

    @Test
    void driverChannelLimitPowerAndControllerAreServerRules() {
        DesignLayout layout = complete("BASIC_2WD");
        layout.getRules().roleFor("l298n").setDriverChannels(1);
        layout.getPlacements().remove(layout.findPlacement(find(layout, "battery-holder").getId()));
        layout.getPlacements().remove(layout.findPlacement(find(layout, "arduino-uno").getId()));
        DesignCheck report = evaluate(layout);
        assertTrue(has(report, "FAIL", "vượt số kênh"));
        assertTrue(has(report, "FAIL", "bộ nguồn"));
        assertTrue(has(report, "FAIL", "cần mạch điều khiển"));
    }

    @Test
    void multipleControllersBalanceAndServoPowerAreWarnings() {
        DesignLayout layout = complete("SCANNING_AVOIDER");
        DesignInstance extra = new DesignInstance();
        extra.setId(50);
        extra.setComponentId("arduino-uno");
        layout.getInstances().add(extra);
        DesignLayoutTest.mount(layout, "chassis-2wd", "extra", "TYPE1", 0, .1, 0);
        DesignLayoutTest.placed(layout, 50, 1, "extra", "Y0");
        layout.getPlacements().remove(layout.findPlacement(find(layout, "caster-wheel").getId()));
        DesignCheck report = evaluate(layout);
        assertTrue(has(report, "WARN", "nhiều hơn một mạch"));
        assertTrue(has(report, "WARN", "mất cân bằng"));
        assertTrue(has(report, "WARN", "5V riêng"));
    }

    @Test
    void unknownPartsRemainUnsupportedEvenWhenStillInTray() {
        DesignLayout layout = complete("BASIC_2WD");
        DesignInstance item = new DesignInstance();
        item.setId(90);
        item.setComponentId("arm-frame");
        layout.getInstances().add(item);
        DesignCheck report = evaluate(layout);
        assertFalse(report.isPassed());
        assertTrue(has(report, "UNSUPPORTED", "chưa hỗ trợ"));
    }

    @Test
    void scanningRequiresSensorOnServoAndInvalidOrientationCannotPass() {
        DesignLayout layout = complete("SCANNING_AVOIDER");
        DesignPlacement sensor = layout.findPlacement(find(layout, "hc-sr04").getId());
        sensor.setHostInstanceId(1);
        sensor.setMountCode("mount-" + sensor.getInstanceId());
        DesignCheck report = evaluate(layout);
        assertTrue(has(report, "FAIL", "đầu servo"));
        sensor.setOrientationCode("Y90");
        assertTrue(has(evaluate(layout), "FAIL", "chưa đúng chuỗi"));
    }
}
