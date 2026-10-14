package vn.edu.webpro.robotlab.business;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class AssemblyEventTest {
    @Test
    void undoUsesTheSameAttachDetachRulesAndVersionChecks() {
        AssemblyLayout l = new AssemblyLayout();
        l.setMode("DETAILED");
        l.setStatus("IN_PROGRESS");
        AssemblySlot slot = new AssemblySlot();
        slot.setId(1);
        slot.setComponentId("wheel");
        slot.getOrientations().add("Y0");
        slot.getOrientations().add("Y180");
        l.getSpec().getSlots().add(slot);
        AssemblyInstance item = new AssemblyInstance();
        item.setId(1);
        item.setComponentId("wheel");
        l.getInstances().add(item);
        AssemblyPlacement placed = new AssemblyPlacement();
        placed.setInstanceId(1);
        placed.setSlotId(1);
        placed.setOrientationCode("Y180");
        l.getPlacements().add(placed);
        AssemblyEvent e = new AssemblyEvent();
        e.setAction("ATTACH");
        e.setOutcome("ACCEPTED");
        e.setInstanceId(1);
        e.setSlotId(1);
        assertEquals("", l.validateUndo(e, 1));
        e.setPrevOrientationCode("Y0");
        assertEquals("", l.validateUndo(e, 1));
        assertEquals("LAYOUT_CONFLICT", l.validateUndo(e, 2));
        l.getPlacements().clear();
        e.setAction("DETACH");
        e.setOrientationCode("Y0");
        assertEquals("", l.validateUndo(e, 1));
        assertEquals("NOTHING_TO_UNDO", l.validateUndo(null, 1));
        l.setStatus("COMPLETED");
        assertEquals("SESSION_LOCKED", l.validateUndo(e, 1));
        l.setMode("GROUP");
        assertEquals("GROUP_MODE", l.validateUndo(e, 1));
    }
    @Test
    void eventCapturesPreviousOrientationAndHasLearnerLabels() {
        AssemblyLayout l = new AssemblyLayout();
        l.setSessionId(10);
        l.getSpec().setId(2);
        AssemblyPlacement p = new AssemblyPlacement();
        p.setInstanceId(7);
        p.setSlotId(8);
        p.setOrientationCode("Y0");
        l.getPlacements().add(p);
        AssemblyEvent e = new AssemblyEvent();
        e.capture("attach", l, 7, 8, "Y180");
        assertEquals("Y0", e.getPrevOrientationCode());
        assertEquals("Đổi hướng", e.getActionLabel());
        assertEquals("attach", e.getInverseAction());
        assertEquals("Y0", e.getInverseOrientation());
        e.setReasonCode("ORIENTATION_NOT_ALLOWED");
        assertEquals("Sai hướng", e.getReasonLabel());
    }

    @Test
    void freshAttachAndDetachHaveDifferentInverses() {
        AssemblyEvent e = new AssemblyEvent();
        e.setAction("ATTACH");
        assertEquals("detach", e.getInverseAction());
        e.setAction("DETACH");
        e.setOrientationCode("Y90");
        assertEquals("attach", e.getInverseAction());
        assertEquals("Y90", e.getInverseOrientation());
    }

    @Test
    void rejectedAndUndoneEventsAreNotActiveTimelineSteps() {
        AssemblyEvent e = new AssemblyEvent();
        e.setAction("ATTACH");
        e.setOutcome("ACCEPTED");
        assertTrue(e.isActiveStep());
        e.setUndone(true);
        assertFalse(e.isActiveStep());
        e.setUndone(false);
        e.setOutcome("REJECTED");
        assertFalse(e.isActiveStep());
    }
}
