package vn.edu.webpro.robotlab.business;

import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.HashMap;
import org.junit.jupiter.api.Test;

class RobotDesignTest {
    @Test
    void oldRuleDisplaysAMigrationNoticeWithoutChangingItsPinnedVersion() {
        RobotDesign design = new RobotDesign();
        design.setRuleSetId(1);
        design.setRuleVersion(1);
        assertEquals("v1", design.getRuleVersionLabel());
        assertTrue(design.isLegacyDirections());
        assertTrue(design.getDirectionNotice().contains("hãy tạo thiết kế mới"));
        assertEquals(1, design.getRuleSetId());
        design.setRuleVersion(2);
        assertEquals("v2", design.getRuleVersionLabel());
        assertFalse(design.isLegacyDirections());
        assertEquals("", design.getDirectionNotice());
    }

    @Test
    void creationEnforcesInventoryBaseCountAndTotalWithoutCountingUnsupportedAsSupported() {
        RobotDesign design = new RobotDesign();
        design.setName("Thiết kế thử");
        DesignRuleSet rules = new DesignRuleSet();
        DesignRequirement goal = new DesignRequirement();
        goal.setGoalCode("BASIC_2WD");
        rules.getRequirements().add(goal);
        DesignComponentRole base = new DesignComponentRole();
        base.setComponentId("chassis-2wd");
        base.setRole("BASE");
        rules.getRoles().add(base);
        HashMap<String, Integer> quantities = new HashMap<>();
        quantities.put("chassis-2wd", 1);
        assertEquals("", design.validateCreation(rules, new ArrayList<KitItem>(), quantities, 0));
        design.setSource("KIT");
        assertFalse(design.validateCreation(rules, new ArrayList<KitItem>(), quantities, 0).isEmpty());
        design.setSource("LIBRARY");
        quantities.put("chassis-2wd", 2);
        assertFalse(design.validateCreation(rules, new ArrayList<KitItem>(), quantities, 0).isEmpty());
        quantities.put("chassis-2wd", 1);
        quantities.put("wheel", 10);
        quantities.put("dc-motor", 10);
        quantities.put("sg90", 10);
        assertFalse(design.validateCreation(rules, new ArrayList<KitItem>(), quantities, 0).isEmpty());
        assertFalse(design.validateCreation(rules, new ArrayList<KitItem>(), quantities, 20).isEmpty());
    }
}
