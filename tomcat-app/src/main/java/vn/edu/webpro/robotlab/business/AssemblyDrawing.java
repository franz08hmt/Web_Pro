package vn.edu.webpro.robotlab.business;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

/** Dữ kiện và quy tắc trình bày của AssemblyDrawing. */
public class AssemblyDrawing implements Serializable {
    private double width = 720;
    private double height = 360;
    private List<DrawingItem> topItems = new ArrayList<>();
    private List<DrawingItem> sideItems = new ArrayList<>();
    private int frontZSign;

    /** Tạo JavaBean rỗng theo quy ước môn học. */
    public AssemblyDrawing() {
    }

    /** Đọc dữ kiện width. */
    public double getWidth() {
        return width;
    }

    /** Ghi dữ kiện width. */
    public void setWidth(double width) {
        this.width = width;
    }

    /** Đọc dữ kiện height. */
    public double getHeight() {
        return height;
    }

    /** Ghi dữ kiện height. */
    public void setHeight(double height) {
        this.height = height;
    }

    /** Đọc dữ kiện topItems. */
    public List<DrawingItem> getTopItems() {
        return topItems;
    }

    /** Ghi dữ kiện topItems. */
    public void setTopItems(List<DrawingItem> topItems) {
        this.topItems = topItems;
    }

    /** Đọc dữ kiện sideItems. */
    public List<DrawingItem> getSideItems() {
        return sideItems;
    }

    /** Ghi dữ kiện sideItems. */
    public void setSideItems(List<DrawingItem> sideItems) {
        this.sideItems = sideItems;
    }

    /** Chiều trước suy từ cảm biến và nền của chính spec, không hardcode dấu trục z. */
    public int getFrontZSign() {
        return frontZSign;
    }

    /** Ghi chiều trục dùng trong trình bày. */
    public void setFrontZSign(int frontZSign) {
        this.frontZSign = frontZSign;
    }

    /** Không bịa hướng khi spec không có căn cứ cảm biến. */
    public boolean isFrontKnown() {
        return frontZSign != 0;
    }

    /** Khoảng cách tâm nhãn lớn hơn đường kính nền số thứ tự. */
    public double getLabelSeparation() {
        return 44;
    }

    /** Tọa độ ghi chú lấy từ kích thước khung. */
    public double getFrontLabelX() {
        return width / 2;
    }

    /** Vùng riêng phía trên, không đè lên hình và số thứ tự. */
    public double getFrontLabelY() {
        return 24;
    }

    /** Giải thích hướng của hình chiếu trên. */
    public String getTopFrontNote() {
        return isFrontKnown() ? "Phía trước ở trên, theo chiều " + frontAxisLabel()
                + " của trục z, xác định từ vị trí cảm biến HC-SR04." : "Chưa xác định phía trước từ vị trí cảm biến.";
    }

    /** Hình ngang dùng z,y để hướng trước không bị mất trong phép chiếu x,y. */
    public String getSideFrontNote() {
        return isFrontKnown() ? "Nhìn ngang theo z,y: phía trước ở bên phải, theo chiều " + frontAxisLabel()
                + " của trục z." : "Chưa xác định phía trước từ vị trí cảm biến.";
    }

    /** Hình lớn vẽ trước, hình nhỏ còn nhìn thấy; giữ STT/chú giải theo thứ tự phiên. */
    public List<DrawingItem> getTopShapes() {
        return orderedShapes(topItems);
    }

    /** Thứ tự lớp vẽ của hình chiếu ngang. */
    public List<DrawingItem> getSideShapes() {
        return orderedShapes(sideItems);
    }

    /** Có dữ kiện tọa độ server, không đoán vị trí của phiên GROUP. */
    public boolean isAvailable() {
        return !topItems.isEmpty();
    }

    /** Dựng hai phép chiếu từ placement thật và slot của cấu hình cố định. */
    public void build(AssemblyLayout layout) {
        topItems = new ArrayList<>();
        sideItems = new ArrayList<>();
        frontZSign = 0;
        if (layout == null || !"DETAILED".equals(layout.getMode())) {
            return;
        }
        findFront(layout);
        project(layout, topItems, true);
        project(layout, sideItems, false);
    }

    private String frontAxisLabel() {
        return frontZSign < 0 ? "âm" : "dương";
    }

    private void findFront(AssemblyLayout layout) {
        AssemblySlot sensor = null;
        double baseZ = 0;
        for (AssemblySlot slot : layout.getSpec().getSlots()) {
            if ("hc-sr04".equals(slot.getComponentId())) {
                sensor = slot;
            }
            if ("chassis-2wd".equals(slot.getComponentId())) {
                baseZ = slot.getZ();
            }
        }
        if (sensor != null && Math.abs(sensor.getZ() - baseZ) > .00001) {
            frontZSign = sensor.getZ() > baseZ ? 1 : -1;
        }
    }

    private double horizontal(AssemblySlot slot, boolean top) {
        return top ? slot.getX() : slot.getZ() * (isFrontKnown() ? frontZSign : 1);
    }

    private double vertical(AssemblySlot slot, boolean top) {
        return top ? slot.getZ() * (isFrontKnown() ? frontZSign : 1) : slot.getY();
    }

    private List<DrawingItem> orderedShapes(List<DrawingItem> items) {
        List<DrawingItem> sorted = new ArrayList<>();
        for (DrawingItem item : items) {
            int index = 0;
            while (index < sorted.size() && area(sorted.get(index)) >= area(item)) {
                index++;
            }
            sorted.add(index, item);
        }
        return sorted;
    }

    private double area(DrawingItem item) {
        return item.getSvgWidth() * item.getSvgHeight();
    }

    private void project(AssemblyLayout layout, List<DrawingItem> items, boolean top) {
        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        for (AssemblyPlacement p : layout.getPlacements()) {
            AssemblySlot slot = layout.getSpec().findSlot(p.getSlotId());
            if (slot != null) {
                double[] size = dimensions(slot.getComponentId(), top);
                double x = horizontal(slot, top);
                double y = vertical(slot, top);
                minX = Math.min(minX, x - size[0] / 2);
                maxX = Math.max(maxX, x + size[0] / 2);
                minY = Math.min(minY, y - size[1] / 2);
                maxY = Math.max(maxY, y + size[1] / 2);
            }
        }
        if (!Double.isFinite(minX)) {
            return;
        }
        double scale = Math.min((width - 120) / Math.max(maxX - minX, .1),
                (height - 136) / Math.max(maxY - minY, .1));
        int number = 0;
        for (AssemblyPlacement p : layout.getPlacements()) {
            AssemblySlot slot = layout.getSpec().findSlot(p.getSlotId());
            if (slot != null) {
                double[] size = dimensions(slot.getComponentId(), top);
                double y = vertical(slot, top);
                DrawingItem item = new DrawingItem();
                item.setNumber(++number);
                item.setInstanceLabel(p.getInstanceLabel());
                item.setSlotLabel(p.getSlotLabel());
                item.setOrientationCode(p.getOrientationCode());
                item.setSvgX(60 + (horizontal(slot, top) - size[0] / 2 - minX) * scale);
                item.setSvgY(72 + (maxY - y - size[1] / 2) * scale);
                item.setSvgWidth(size[0] * scale);
                item.setSvgHeight(size[1] * scale);
                if ("wheel".equals(slot.getComponentId()) || "caster-wheel".equals(slot.getComponentId())) {
                    item.setShape("circle");
                }
                items.add(item);
            }
        }
        placeLabels(items);
    }

    // Chọn vị trí ngoài hộp gần tâm nhất; không đổi tọa độ vật thể hay dữ liệu đã lưu.
    private void placeLabels(List<DrawingItem> items) {
        List<DrawingItem> placed = new ArrayList<>();
        for (DrawingItem item : items) {
            double x = item.getCenterX();
            double y = item.getCenterY();
            if (!clearLabel(x, y, placed)) {
                double best = Double.POSITIVE_INFINITY;
                for (double candidateY = 56; candidateY <= height - 20; candidateY += getLabelSeparation()) {
                    for (double candidateX = 20; candidateX <= width - 20; candidateX += getLabelSeparation()) {
                        if (outside(item, candidateX, candidateY) && clearLabel(candidateX, candidateY, placed)) {
                            double distance = Math.hypot(candidateX - x, candidateY - y);
                            if (distance < best) {
                                best = distance;
                                item.setLabelX(candidateX);
                                item.setLabelY(candidateY);
                            }
                        }
                    }
                }
                if (!Double.isFinite(best)) {
                    // Khung đông vật thể vẫn đọc được toàn bộ chú giải, không làm trang lỗi 500.
                    item.setLabelX(x);
                    item.setLabelY(y);
                }
            } else {
                item.setLabelX(x);
                item.setLabelY(y);
            }
            placed.add(item);
        }
    }

    private boolean clearLabel(double x, double y, List<DrawingItem> items) {
        for (DrawingItem item : items) {
            if (Math.hypot(x - item.getLabelX(), y - item.getLabelY()) < getLabelSeparation()) {
                return false;
            }
        }
        return true;
    }

    private boolean outside(DrawingItem item, double x, double y) {
        return x <= item.getSvgX() - item.getLabelRadius()
                || x >= item.getSvgX() + item.getSvgWidth() + item.getLabelRadius()
                || y <= item.getSvgY() - item.getLabelRadius()
                || y >= item.getSvgY() + item.getSvgHeight() + item.getLabelRadius();
    }

    // Kích thước khối gợi hình từ mô hình thủ tục, không phải thông số kỹ thuật robot thật.
    private double[] dimensions(String component, boolean top) {
        if ("chassis-2wd".equals(component)) {
            return new double[] {top ? 2.8 : 1.8, top ? 1.8 : .35};
        }
        if ("wheel".equals(component)) {
            return new double[] {1, 1};
        }
        if ("caster-wheel".equals(component)) {
            return new double[] {.6, .6};
        }
        if ("dc-motor".equals(component)) {
            return new double[] {.72, .56};
        }
        if ("arduino-uno".equals(component)) {
            return new double[] {top ? 1.45 : .92, top ? .92 : .3};
        }
        if ("battery-holder".equals(component)) {
            return new double[] {top ? 1.55 : 1.05, top ? 1.05 : .45};
        }
        if ("hc-sr04".equals(component)) {
            return new double[] {top ? 1.15 : .45, top ? .45 : .55};
        }
        return new double[] {.95, top ? .8 : .35};
    }
}
