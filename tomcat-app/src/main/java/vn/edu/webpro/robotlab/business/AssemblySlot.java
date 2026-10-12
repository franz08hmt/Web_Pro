package vn.edu.webpro.robotlab.business;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import vn.edu.webpro.robotlab.util.JsonUtil;

/** Dữ kiện và luật lắp ráp mô phỏng do server xác thực. */
public class AssemblySlot implements Serializable {
    private long id;
    private long specId;
    private String slotCode = "";
    private long parentSlotId;
    private String componentId = "";
    private String label = "";
    private boolean required;
    private double x;
    private double y;
    private double z;
    private double rx;
    private double ry;
    private double rz;
    private int displayOrder = 1;
    private List<String> orientations = new ArrayList<>();

    /** Tạo bean rỗng theo quy ước môn học. */
    public AssemblySlot() {
    }

    /** Đọc dữ kiện id. */
    public long getId() {
        return id;
    }

    /** Ghi dữ kiện id. */
    public void setId(long id) {
        this.id = id;
    }

    /** Đọc dữ kiện specId. */
    public long getSpecId() {
        return specId;
    }

    /** Ghi dữ kiện specId. */
    public void setSpecId(long specId) {
        this.specId = specId;
    }

    /** Đọc dữ kiện slotCode. */
    public String getSlotCode() {
        return slotCode;
    }

    /** Ghi dữ kiện slotCode. */
    public void setSlotCode(String slotCode) {
        this.slotCode = slotCode;
    }

    /** Đọc dữ kiện parentSlotId. */
    public long getParentSlotId() {
        return parentSlotId;
    }

    /** Ghi dữ kiện parentSlotId. */
    public void setParentSlotId(long parentSlotId) {
        this.parentSlotId = parentSlotId;
    }

    /** Đọc dữ kiện componentId. */
    public String getComponentId() {
        return componentId;
    }

    /** Ghi dữ kiện componentId. */
    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }

    /** Đọc dữ kiện label. */
    public String getLabel() {
        return label;
    }

    /** Ghi dữ kiện label. */
    public void setLabel(String label) {
        this.label = label;
    }

    /** Đọc dữ kiện required. */
    public boolean isRequired() {
        return required;
    }

    /** Ghi dữ kiện required. */
    public void setRequired(boolean required) {
        this.required = required;
    }

    /** Đọc dữ kiện x. */
    public double getX() {
        return x;
    }

    /** Ghi dữ kiện x. */
    public void setX(double x) {
        this.x = x;
    }

    /** Đọc dữ kiện y. */
    public double getY() {
        return y;
    }

    /** Ghi dữ kiện y. */
    public void setY(double y) {
        this.y = y;
    }

    /** Đọc dữ kiện z. */
    public double getZ() {
        return z;
    }

    /** Ghi dữ kiện z. */
    public void setZ(double z) {
        this.z = z;
    }

    /** Đọc dữ kiện rx. */
    public double getRx() {
        return rx;
    }

    /** Ghi dữ kiện rx. */
    public void setRx(double rx) {
        this.rx = rx;
    }

    /** Đọc dữ kiện ry. */
    public double getRy() {
        return ry;
    }

    /** Ghi dữ kiện ry. */
    public void setRy(double ry) {
        this.ry = ry;
    }

    /** Đọc dữ kiện rz. */
    public double getRz() {
        return rz;
    }

    /** Ghi dữ kiện rz. */
    public void setRz(double rz) {
        this.rz = rz;
    }

    /** Đọc dữ kiện displayOrder. */
    public int getDisplayOrder() {
        return displayOrder;
    }

    /** Ghi dữ kiện displayOrder. */
    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    /** Đọc dữ kiện orientations. */
    public List<String> getOrientations() {
        return orientations;
    }

    /** Ghi dữ kiện orientations. */
    public void setOrientations(List<String> orientations) {
        this.orientations = orientations;
    }

    /** Kiểm tra hướng rời rạc theo cấu hình đã công bố. */
    public boolean acceptsOrientation(String orientation) {
        return orientations.contains(orientation);
    }

    /** Kiểm tra giới hạn hiển thị và hướng; không mô phỏng điện. */
    public boolean isValid() {
        if (label == null || label.trim().isEmpty() || label.length() > 150
                || componentId == null || componentId.isEmpty() || slotCode == null
                || !slotCode.matches("[a-z0-9][a-z0-9.-]{0,63}")
                || displayOrder < 1 || displayOrder > 40 || orientations.isEmpty()) {
            return false;
        }
        for (double value : new double[] {x, y, z}) {
            if (!Double.isFinite(value) || Math.abs(value) > 10) {
                return false;
            }
        }
        for (double value : new double[] {rx, ry, rz}) {
            if (!Double.isFinite(value) || Math.abs(value) > 6.284) {
                return false;
            }
        }
        HashSet<String> unique = new HashSet<>();
        for (String orientation : orientations) {
            if (!unique.add(orientation)) {
                return false;
            }
            if (!"Y0".equals(orientation) && !"Y90".equals(orientation)
                    && !"Y180".equals(orientation) && !"Y270".equals(orientation)) {
                return false;
            }
        }
        return true;
    }

}
