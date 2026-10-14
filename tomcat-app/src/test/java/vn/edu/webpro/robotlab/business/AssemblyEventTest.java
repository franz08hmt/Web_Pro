package vn.edu.webpro.robotlab.business;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class AssemblyEventTest {
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
