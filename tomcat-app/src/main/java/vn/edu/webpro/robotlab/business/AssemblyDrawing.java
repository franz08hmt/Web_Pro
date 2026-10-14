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

    /** Có dữ kiện tọa độ server, không đoán vị trí của phiên GROUP. */
    public boolean isAvailable() {
        return !topItems.isEmpty();
    }

    /** Dựng hai phép chiếu từ placement thật và slot của cấu hình cố định. */
    public void build(AssemblyLayout layout) {
        topItems = new ArrayList<>();
        sideItems = new ArrayList<>();
        if (layout == null || !"DETAILED".equals(layout.getMode())) {
            return;
        }
        project(layout, topItems, true);
        project(layout, sideItems, false);
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
                double y = top ? slot.getZ() : slot.getY();
                minX = Math.min(minX, slot.getX() - size[0] / 2);
                maxX = Math.max(maxX, slot.getX() + size[0] / 2);
                minY = Math.min(minY, y - size[1] / 2);
                maxY = Math.max(maxY, y + size[1] / 2);
            }
        }
        if (!Double.isFinite(minX)) {
            return;
        }
        double scale = Math.min((width - 64) / Math.max(maxX - minX, .1),
                (height - 64) / Math.max(maxY - minY, .1));
        int number = 0;
        for (AssemblyPlacement p : layout.getPlacements()) {
            AssemblySlot slot = layout.getSpec().findSlot(p.getSlotId());
            if (slot != null) {
                double[] size = dimensions(slot.getComponentId(), top);
                double y = top ? slot.getZ() : slot.getY();
                DrawingItem item = new DrawingItem();
                item.setNumber(++number);
                item.setInstanceLabel(p.getInstanceLabel());
                item.setSlotLabel(p.getSlotLabel());
                item.setOrientationCode(p.getOrientationCode());
                item.setSvgX(32 + (slot.getX() - size[0] / 2 - minX) * scale);
                item.setSvgY(32 + (maxY - y - size[1] / 2) * scale);
                item.setSvgWidth(size[0] * scale);
                item.setSvgHeight(size[1] * scale);
                if ("wheel".equals(slot.getComponentId()) || "caster-wheel".equals(slot.getComponentId())) {
                    item.setShape("circle");
                }
                items.add(item);
            }
        }
    }

    // Kích thước khối gợi hình từ mô hình thủ tục, không phải thông số kỹ thuật robot thật.
    private double[] dimensions(String component, boolean top) {
        if ("chassis-2wd".equals(component)) {
            return new double[] {2.8, top ? 1.8 : .35};
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
            return new double[] {1.45, top ? .92 : .3};
        }
        if ("battery-holder".equals(component)) {
            return new double[] {1.55, top ? 1.05 : .45};
        }
        if ("hc-sr04".equals(component)) {
            return new double[] {1.15, top ? .45 : .55};
        }
        return new double[] {.95, top ? .8 : .35};
    }
}
