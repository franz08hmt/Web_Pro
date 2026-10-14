package vn.edu.webpro.robotlab.business;

import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;

class DesignSheetTest {
    @Test
    void bomIncludesTrayPartsAndUsesCurrentKitWithoutChangingSnapshot() {
        DesignLayout layout = DesignCheckTest.complete("OBSTACLE_AVOIDER");
        KitItem owned = new KitItem();
        owned.setComponentId("wheel");
        owned.setQuantity(1);
        ArrayList<KitItem> kit = new ArrayList<>();
        kit.add(owned);
        DesignSheet sheet = new DesignSheet();
        sheet.build(layout, kit, new ArrayList<DesignEvent>());
        for (DesignBomItem item : sheet.getBomItems()) {
            if ("wheel".equals(item.getComponentId())) {
                assertEquals(2, item.getNeeded());
                assertEquals(1, item.getOwned());
                assertEquals(1, item.getMissing());
            }
        }
        assertEquals(10, sheet.getDrawing().getTopItems().size());
        assertEquals(10, sheet.getSteps().size());
        assertFalse(sheet.isHasReport());
    }

    @Test
    void proposedStepsKeepEveryParentBeforeItsChildrenAndNeverWriteOfficialSessions() {
        DesignLayout layout = DesignCheckTest.complete("SCANNING_AVOIDER");
        DesignSheet sheet = new DesignSheet();
        sheet.build(layout, new ArrayList<KitItem>(), new ArrayList<DesignEvent>());
        ArrayList<Long> visited = new ArrayList<>();
        for (DesignStep step : sheet.getSteps()) {
            DesignPlacement placed = layout.findPlacement(step.getInstanceId());
            assertTrue(placed.getHostInstanceId() == 0 || visited.contains(placed.getHostInstanceId()));
            visited.add(step.getInstanceId());
        }
        assertEquals(layout.getPlacements().size(), visited.size());
    }

    @Test
    void workshopSummaryUsesTheSharedLearningErrorRuleAndExcludesTabUndoAndRequestErrors() {
        String[] codes = {"LAYOUT_CONFLICT", "NOTHING_TO_UNDO", "SESSION_LOCKED", "GROUP_MODE",
                "VALIDATION_ERROR", "MOUNT_INCOMPATIBLE", "MOUNT_OCCUPIED", "HOST_MISSING", "NOT_SUPPORTED",
                "ORIENTATION_NOT_ALLOWED", "ORIENTATION_NOT_ALLOWED"};
        ArrayList<DesignEvent> events = new ArrayList<>();
        for (String code : codes) {
            DesignEvent event = new DesignEvent();
            event.setOutcome("REJECTED");
            event.setReasonCode(code);
            events.add(event);
        }
        DesignSheet sheet = new DesignSheet();
        sheet.build(DesignCheckTest.complete("BASIC_2WD"), new ArrayList<KitItem>(), events);
        assertEquals(5, sheet.getLearningErrors().size());
        for (DesignLearningError error : sheet.getLearningErrors()) {
            assertTrue(new AssemblyReview().isLearningError(error.getCode()));
            if ("ORIENTATION_NOT_ALLOWED".equals(error.getCode())) {
                assertEquals(2, error.getCount());
            }
        }
    }
}
