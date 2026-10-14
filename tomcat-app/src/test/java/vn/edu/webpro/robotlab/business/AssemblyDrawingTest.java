package vn.edu.webpro.robotlab.business;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class AssemblyDrawingTest {
    @Test
    void thirtyItemsInACrowdedFrameKeepAllLegendEntriesWithoutThrowing() {
        AssemblyDrawing drawing = new AssemblyDrawing();
        drawing.setWidth(160);
        drawing.setHeight(160);
        AssemblyLayout layout = layout(30);
        for (AssemblySlot slot : layout.getSpec().getSlots()) {
            slot.setX(0);
            slot.setY(0);
            slot.setZ(0);
        }
        drawing.build(layout);
        assertEquals(30, drawing.getTopItems().size());
        assertEquals(30, drawing.getSideItems().size());
        java.util.List<DrawingItem> all = new java.util.ArrayList<>();
        all.addAll(drawing.getTopItems());
        all.addAll(drawing.getSideItems());
        for (DrawingItem item : all) {
            assertTrue(item.getSvgX() >= 0 && item.getSvgY() >= 0);
            assertTrue(item.getSvgX() + item.getSvgWidth() <= drawing.getWidth());
            assertTrue(item.getSvgY() + item.getSvgHeight() <= drawing.getHeight());
            assertTrue(item.getLabelX() >= 0 && item.getLabelX() <= drawing.getWidth());
            assertTrue(item.getLabelY() >= 0 && item.getLabelY() <= drawing.getHeight());
        }
    }

    @Test
    void overlappingModelsHaveSeparatedLabelsInBothViews() {
        AssemblyLayout model = layout(40);
        for (AssemblySlot slot : model.getSpec().getSlots()) {
            slot.setX(0);
            slot.setY(0);
            slot.setZ(0);
        }
        AssemblyDrawing drawing = new AssemblyDrawing();
        drawing.build(model);
        assertLabels(drawing, drawing.getTopItems());
        assertLabels(drawing, drawing.getSideItems());
        for (AssemblySlot slot : model.getSpec().getSlots()) {
            slot.setComponentId("chassis-2wd");
        }
        drawing.build(model);
        assertLabels(drawing, drawing.getTopItems());
        assertLabels(drawing, drawing.getSideItems());
    }

    private void assertLabels(AssemblyDrawing drawing, java.util.List<DrawingItem> items) {
        for (int n = 0; n < items.size(); n++) {
            DrawingItem item = items.get(n);
            assertTrue(item.getLabelX() >= 16 && item.getLabelX() <= drawing.getWidth() - 16);
            assertTrue(item.getLabelY() >= 16 && item.getLabelY() <= drawing.getHeight() - 16);
            for (int k = 0; k < n; k++) {
                DrawingItem other = items.get(k);
                double distance = Math.hypot(item.getLabelX() - other.getLabelX(),
                        item.getLabelY() - other.getLabelY());
                assertTrue(distance + .00001 >= drawing.getLabelSeparation());
            }
        }
    }

    @Test
    void sensorZDefinesTheFrontAndSmallShapesAreDrawnLast() {
        AssemblyLayout model = layout(3);
        AssemblySlot sensor = model.getSpec().getSlots().get(2);
        sensor.setComponentId("hc-sr04");
        sensor.setZ(-2);
        AssemblyDrawing drawing = new AssemblyDrawing();
        drawing.build(model);
        assertTrue(drawing.isFrontKnown());
        assertEquals(-1, drawing.getFrontZSign());
        assertTrue(drawing.getTopFrontNote().contains("ở trên"));
        assertTrue(drawing.getSideFrontNote().contains("bên phải"));
        java.util.List<DrawingItem> shapes = drawing.getTopShapes();
        for (int n = 1; n < shapes.size(); n++) {
            double previous = shapes.get(n - 1).getSvgWidth() * shapes.get(n - 1).getSvgHeight();
            assertTrue(previous >= shapes.get(n).getSvgWidth() * shapes.get(n).getSvgHeight());
        }
        sensor.setZ(2);
        drawing.build(model);
        assertEquals(1, drawing.getFrontZSign());
    }

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
