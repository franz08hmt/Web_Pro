package vn.edu.webpro.robotlab.business;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class DesignRulesTest {
    @Test
    void unlistedComponentsStayUnsupportedAndOrientationIsCheckedSeparately() {
        DesignRuleSet rules = new DesignRuleSet();
        assertNull(rules.roleFor("arm-frame"));
        assertFalse(rules.fitsType("arm-frame", "BOARD_DECK"));
        DesignFit fit = new DesignFit();
        fit.setComponentId("dc-motor");
        fit.setMountType("MOTOR_MOUNT_L");
        fit.setOrientationCode("Y90");
        rules.getFits().add(fit);
        assertTrue(rules.fitsType("dc-motor", "MOTOR_MOUNT_L"));
        assertFalse(rules.fitsOrientation("dc-motor", "MOTOR_MOUNT_L", "Y0"));
        assertTrue(rules.fitsOrientation("dc-motor", "MOTOR_MOUNT_L", "Y90"));
        assertFalse(rules.fitsType("dc-motor", "MOTOR_MOUNT_R"));
    }
}
