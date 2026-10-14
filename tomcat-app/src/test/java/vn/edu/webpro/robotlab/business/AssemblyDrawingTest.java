package vn.edu.webpro.robotlab.business;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class AssemblyDrawingTest {
    private AssemblyLayout layout(int count) {
        AssemblyLayout l = new AssemblyLayout();
        for (int n = 1; n <= count; n++) {
            AssemblySlot s = new AssemblySlot();
            s.setId(n);
            s.setX(n % 2 == 0 ? -10 : 10);
            s.setY(n / 2.0);
            s.setZ(n % 3 - 1);
            s.setComponentId(n % 2 == 0 ? "wheel" : "chassis-2wd");
            s.setLabel("Điểm " + n);
            l.getSpec().getSlots().add(s);
            AssemblyPlacement p = new AssemblyPlacement();
            p.setInstanceId(n);
            p.setSlotId(n);
            p.setInstanceLabel("Vật thể " + n);
            p.setSlotLabel(s.getLabel());
            p.setOrientationCode("Y90");
            l.getPlacements().add(p);
        }
        return l;
    }

    @Test
    void completeModelHasTenItemsInBothViewsWithinTheSvgFrame() {
        AssemblyDrawing d = new AssemblyDrawing();
        d.build(layout(10));
        assertEquals(10, d.getTopItems().size());
        assertEquals(10, d.getSideItems().size());
        for (DrawingItem i : d.getTopItems()) {
            assertTrue(i.getSvgX() >= 0 && i.getSvgY() >= 0);
            assertTrue(i.getSvgX() + i.getSvgWidth() <= d.getWidth() + 0.00001);
            assertTrue(i.getSvgY() + i.getSvgHeight() <= d.getHeight() + 0.00001);
            assertEquals("90°", i.getOrientationLabel());
        }
        for (DrawingItem i : d.getSideItems()) {
            assertTrue(i.getSvgX() >= 0 && i.getSvgY() >= 0);
            assertTrue(i.getSvgX() + i.getSvgWidth() <= d.getWidth() + 0.00001);
            assertTrue(i.getSvgY() + i.getSvgHeight() <= d.getHeight() + 0.00001);
        }
    }

    @Test
    void emptyAndSingleItemDrawingsNeverDivideByZero() {
        AssemblyDrawing d = new AssemblyDrawing();
        d.build(layout(0));
        assertFalse(d.isAvailable());
        d.build(layout(1));
        assertTrue(d.isAvailable());
        assertTrue(Double.isFinite(d.getTopItems().get(0).getSvgX()));
        assertTrue(d.getTopItems().get(0).getSvgWidth() > 0);
    }

    @Test
    void groupModeHasNoInventedServerCoordinates() {
        AssemblyDrawing d = new AssemblyDrawing();
        AssemblyLayout l = layout(10);
        l.setMode("GROUP");
        d.build(l);
        assertFalse(d.isAvailable());
    }
}
