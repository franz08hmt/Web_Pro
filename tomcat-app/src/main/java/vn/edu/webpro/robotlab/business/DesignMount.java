package vn.edu.webpro.robotlab.business;

import java.io.Serializable;

/** Dữ kiện và quy tắc trình bày của DesignMount. */
public class DesignMount implements Serializable {
    private String componentId = "";
    private String mountCode = "";
    private String mountType = "";
    private String label = "";
    private double x;
    private double y;
    private double z;
    private double rx;
    private double ry;
    private double rz;
    private int displayOrder;

    /** Tạo JavaBean rỗng theo quy ước môn học. */
    public DesignMount() {
    }

    /** Đọc dữ kiện componentId. */
    public String getComponentId() {
        return componentId;
    }

    /** Ghi dữ kiện componentId. */
    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }

    /** Đọc dữ kiện mountCode. */
    public String getMountCode() {
        return mountCode;
    }

    /** Ghi dữ kiện mountCode. */
    public void setMountCode(String mountCode) {
        this.mountCode = mountCode;
    }

    /** Đọc dữ kiện mountType. */
    public String getMountType() {
        return mountType;
    }

    /** Ghi dữ kiện mountType. */
    public void setMountType(String mountType) {
        this.mountType = mountType;
    }

    /** Đọc dữ kiện label. */
    public String getLabel() {
        return label;
    }

    /** Ghi dữ kiện label. */
    public void setLabel(String label) {
        this.label = label;
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


}
