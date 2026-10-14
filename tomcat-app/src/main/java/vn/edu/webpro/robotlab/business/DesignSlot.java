package vn.edu.webpro.robotlab.business;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

/** Dữ kiện và quy tắc trình bày của DesignSlot. */
public class DesignSlot implements Serializable {
    private String id = "";
    private long hostInstanceId;
    private String hostComponentId = "";
    private String mountCode = "";
    private String mountType = "";
    private String label = "";
    private boolean hostAvailable;
    private int displayOrder;
    private double x;
    private double y;
    private double z;
    private double rx;
    private double ry;
    private double rz;
    private double logicalYaw;
    private List<String> orientations = new ArrayList<>();

    /** Tạo JavaBean rỗng theo quy ước môn học. */
    public DesignSlot() {
    }

    /** Đọc dữ kiện id. */
    public String getId() {
        return id;
    }

    /** Ghi dữ kiện id. */
    public void setId(String id) {
        this.id = id;
    }

    /** Đọc dữ kiện hostInstanceId. */
    public long getHostInstanceId() {
        return hostInstanceId;
    }

    /** Ghi dữ kiện hostInstanceId. */
    public void setHostInstanceId(long hostInstanceId) {
        this.hostInstanceId = hostInstanceId;
    }

    /** Đọc dữ kiện hostComponentId. */
    public String getHostComponentId() {
        return hostComponentId;
    }

    /** Ghi dữ kiện hostComponentId. */
    public void setHostComponentId(String hostComponentId) {
        this.hostComponentId = hostComponentId;
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

    /** Đọc dữ kiện hostAvailable. */
    public boolean isHostAvailable() {
        return hostAvailable;
    }

    /** Ghi dữ kiện hostAvailable. */
    public void setHostAvailable(boolean hostAvailable) {
        this.hostAvailable = hostAvailable;
    }

    /** Đọc dữ kiện displayOrder. */
    public int getDisplayOrder() {
        return displayOrder;
    }

    /** Ghi dữ kiện displayOrder. */
    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
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

    /** Đọc dữ kiện logicalYaw. */
    public double getLogicalYaw() {
        return logicalYaw;
    }

    /** Ghi dữ kiện logicalYaw. */
    public void setLogicalYaw(double logicalYaw) {
        this.logicalYaw = logicalYaw;
    }

    /** Đọc dữ kiện orientations. */
    public List<String> getOrientations() {
        return orientations;
    }

    /** Ghi dữ kiện orientations. */
    public void setOrientations(List<String> orientations) {
        this.orientations = orientations;
    }


}
