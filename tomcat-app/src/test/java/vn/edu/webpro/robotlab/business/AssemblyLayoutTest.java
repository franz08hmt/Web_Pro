package vn.edu.webpro.robotlab.business;

import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;

class AssemblyLayoutTest {
    @Test void detailedCountsAreFactsAndLegacyPreparationPercentIsUnchanged() {
        AssemblySession s = new AssemblySession();
        s.setAssemblyMode("DETAILED");
        s.setPlacedSlotCount(1);
        s.setRequiredSlotCount(10);
        assertEquals(1, s.getPlacedSlotCount());
        assertEquals(10, s.getRequiredSlotCount());
        assertTrue(s.toJson().contains("\"placedSlotCount\":1"));
        assertTrue(s.toJson().contains("\"requiredSlotCount\":10"));
        s.setAssemblyMode("GROUP");
        s.setRequiredComponentCount(2);
        s.getComponents().add(new SessionComponent("wheel", true));
        assertEquals(50, s.getProgressPercent());
    }
    @Test void rejectionMessagesNameOnlyPartsFromTheLoadedLayout() {
        AssemblyLayout l = layout();
        l.findInstance(1).setLabel("Khung xe");
        l.findInstance(2).setLabel("Bánh xe trái");
        l.findInstance(3).setLabel("Bánh xe phải");
        l.getSpec().findSlot(1).setLabel("Khung nền");
        l.getSpec().findSlot(2).setLabel("Trục trái");
        l.getSpec().findSlot(3).setLabel("Trục phải");
        assertEquals("Không thể gắn Bánh xe trái vào Khung nền: điểm này dành cho loại bộ phận khác.",
                l.messageFor("WRONG_COMPONENT", 2, 1));
        assertEquals("Bánh xe trái chưa đúng hướng tại Trục trái. Hãy xoay rồi thử lại.",
                l.messageFor("ORIENTATION_NOT_ALLOWED", 2, 2));
        assertEquals("Cần gắn Khung nền trước khi gắn Bánh xe trái.", l.messageFor("PARENT_MISSING", 2, 2));
        place(l, 1, 1);
        place(l, 2, 2);
        assertEquals("Trục trái đã có Bánh xe trái. Hãy tháo vật thể đó trước.",
                l.messageFor("SLOT_OCCUPIED", 3, 2));
        assertEquals("Bánh xe trái đang ở Trục trái. Hãy tháo trước khi chuyển.",
                l.messageFor("ALREADY_PLACED", 2, 3));
        assertEquals("Cần tháo Bánh xe trái trước khi tháo Khung xe.", l.messageFor("HAS_DEPENDENTS", 1, 0));
        l.findInstance(2).setLabel(null);
        assertEquals(l.messageFor("WRONG_COMPONENT"), l.messageFor("WRONG_COMPONENT", 2, 1));
    }

    @Test void anonymousRejectionsNeverRevealPartLabels() {
        AssemblyLayout l = layout();
        l.findInstance(1).setLabel("Tên riêng không được lộ");
        for (String code : new String[] {"NOT_FOUND", "LAYOUT_CONFLICT", "SESSION_LOCKED", "GROUP_MODE"}) {
            assertEquals(l.messageFor(code), l.messageFor(code, 1, 1));
            assertFalse(l.messageFor(code, 1, 1).contains("Tên riêng"));
        }
    }
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
        l.setStatus("COMPLETED");
        assertEquals("UNCHANGED", l.validateAttach(1, 1, "Y0", 0));
        assertEquals("SESSION_LOCKED", l.validateAttach(1, 1, "Y90", 1));
    }

    @Test void kitMismatchAndCompletionAreDecidedByTheBean() {
        AssemblyLayout l = layout();
        assertEquals("", l.validateKit());
        assertEquals("ASSEMBLY_INCOMPLETE", l.validateChange("complete", 1));
        assertEquals("LAYOUT_CONFLICT", l.validateChange("reset", 0));
        assertEquals("", l.validateChange("reset", 1));
        l.setStatus("READY");
        assertEquals("", l.validateChange("reset", 1));
        assertEquals("SESSION_LOCKED", l.validateChange("complete", 1));
        l.setStatus("IN_PROGRESS");
        place(l, 1, 1);
        place(l, 2, 2);
        place(l, 3, 3);
        assertEquals("", l.validateChange("complete", 1));
        l.setStatus("COMPLETED");
        assertEquals("UNCHANGED", l.validateChange("complete", 0));
        assertEquals("SESSION_LOCKED", l.validateChange("reset", 1));
        l.getInstances().remove(2);
        assertEquals("KIT_SPEC_MISMATCH", l.validateKit());
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

    @Test void v2OrientationsRequireRotationAndAcceptEveryDeclaredAlternative() {
        AssemblyLayout l = layout();
        place(l, 1, 1);
        AssemblySlot slot = l.getSpec().findSlot(2);
        slot.getOrientations().clear();
        slot.getOrientations().add("Y90");
        assertEquals("ORIENTATION_NOT_ALLOWED", l.validateAttach(2, 2, "Y0", 1));
        assertEquals("", l.validateAttach(2, 2, "Y90", 1));
        slot.getOrientations().add("Y270");
        assertEquals("", l.validateAttach(2, 2, "Y270", 1));
        assertEquals("ORIENTATION_NOT_ALLOWED", l.validateAttach(2, 2, "Y180", 1));
        slot.getOrientations().add("Y0");
        slot.getOrientations().add("Y180");
        for (String direction : slot.getOrientations()) {
            assertEquals("", l.validateAttach(2, 2, direction, 1));
        }
    }
}
