package vn.edu.webpro.robotlab.business;

import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;

class AssemblyLayoutTest {
    private AssemblyLayout layout() {
        AssemblyLayout layout = new AssemblyLayout();
        layout.setStatus("IN_PROGRESS");
        layout.setLayoutVersion(1);
        AssemblySpec spec = new AssemblySpec();
        spec.setId(1);
        layout.setSpec(spec);
        for (int n = 1; n <= 3; n++) {
            AssemblySlot slot = new AssemblySlot();
            slot.setId(n);
            slot.setSlotCode("slot-" + n);
            slot.setSpecId(1);
            slot.setComponentId(n == 1 ? "chassis" : "wheel");
            slot.setLabel("Điểm " + n);
            slot.setRequired(true);
            slot.getOrientations().add("Y0");
            if (n > 1) {
                slot.setParentSlotId(1);
            }
            spec.getSlots().add(slot);
            AssemblyInstance item = new AssemblyInstance();
            item.setId(n);
            item.setComponentId(slot.getComponentId());
            item.setInstanceNo(n == 1 ? 1 : n - 1);
            layout.getInstances().add(item);
        }
        return layout;
    }

    private void place(AssemblyLayout layout, long instance, long slot) {
        AssemblyPlacement placed = new AssemblyPlacement();
        placed.setInstanceId(instance);
        placed.setSlotId(slot);
        placed.setOrientationCode("Y0");
        layout.getPlacements().add(placed);
    }

    @Test void eachValidationReasonIsSpecific() {
        AssemblyLayout l = layout();
        assertEquals("NOT_FOUND", l.validateAttach(99, 1, "Y0", 1));
        assertEquals("WRONG_COMPONENT", l.validateAttach(2, 1, "Y0", 1));
        assertEquals("ORIENTATION_NOT_ALLOWED", l.validateAttach(1, 1, "Y90", 1));
        assertEquals("PARENT_MISSING", l.validateAttach(2, 2, "Y0", 1));
        assertEquals("LAYOUT_CONFLICT", l.validateAttach(1, 1, "Y0", 0));
        place(l, 1, 1);
        place(l, 2, 2);
        assertEquals("SLOT_OCCUPIED", l.validateAttach(3, 2, "Y0", 1));
        assertEquals("ALREADY_PLACED", l.validateAttach(2, 3, "Y0", 1));
        l.setStatus("COMPLETED");
        assertEquals("SESSION_LOCKED", l.validateAttach(3, 3, "Y0", 1));
    }

    @Test void exactRetryIsIdempotentButOtherEditsNeedVersion() {
        AssemblyLayout l = layout();
        place(l, 1, 1);
        assertEquals("UNCHANGED", l.validateAttach(1, 1, "Y0", 0));
        assertEquals("LAYOUT_CONFLICT", l.validateDetach(1, 0));
    }

    @Test void parentCannotBeRemovedUntilChildrenAreDetached() {
        AssemblyLayout l = layout();
        place(l, 1, 1);
        place(l, 2, 2);
        assertEquals("HAS_DEPENDENTS", l.validateDetach(1, 1));
        assertEquals("", l.validateDetach(2, 1));
        assertEquals("NOT_FOUND", l.validateDetach(88, 1));
    }

    @Test void oneWheelDoesNotCompleteBothPhysicalInstances() {
        AssemblyLayout l = layout();
        place(l, 1, 1);
        place(l, 2, 2);
        AssemblyCheckReport report = l.check();
        assertEquals(2, report.getCorrectCount());
        assertEquals(3, report.getRequiredCount());
        assertFalse(report.isComplete());
        assertEquals(1, report.getTrayLabels().size());
        place(l, 3, 3);
        assertTrue(l.check().isComplete());
    }

    @Test void reportRejectsWrongOrientationAndMissingAncestors() {
        AssemblyLayout l = layout();
        place(l, 2, 2);
        assertEquals(0, l.check().getCorrectCount());
        assertFalse(l.check().getErrors().isEmpty());
        place(l, 1, 1);
        l.getPlacements().get(1).setOrientationCode("Y90");
        assertFalse(l.check().isComplete());
    }

    @Test void publishedSpecIsValidatedAndCannotBeMutated() {
        AssemblySpec s = layout().getSpec();
        s.setRobotId("obstacle-avoider");
        assertEquals("", s.validate());
        s.getSlots().get(0).setParentSlotId(2);
        assertFalse(s.validate().isEmpty());
        s.getSlots().get(0).setParentSlotId(0);
        s.getSlots().get(0).setX(99);
        assertFalse(s.validate().isEmpty());
        s.setState("ARCHIVED");
        assertFalse(s.isPublished());
    }

    @Test void instanceGenerationUsesKitQuantitiesAndDistinctIdentity() {
        AssemblyLayout l = layout();
        ArrayList<RobotComponent> kit = new ArrayList<>();
        RobotComponent wheel = new RobotComponent();
        wheel.setComponentId("wheel");
        wheel.setQuantity(2);
        kit.add(wheel);
        l.createInstances(kit);
        assertEquals(2, l.getInstances().size());
        assertEquals(1, l.getInstances().get(0).getInstanceNo());
        assertEquals(2, l.getInstances().get(1).getInstanceNo());
    }
}
