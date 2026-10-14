package vn.edu.webpro.robotlab.business;

import java.io.Serializable;

/** Dữ kiện và quy tắc trình bày của DesignFit. */
public class DesignFit implements Serializable {
    private String componentId = "";
    private String mountType = "";
    private String orientationCode = "Y0";

    /** Tạo JavaBean rỗng theo quy ước môn học. */
    public DesignFit() {
    }

    /** Đọc dữ kiện componentId. */
    public String getComponentId() {
        return componentId;
    }

    /** Ghi dữ kiện componentId. */
    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }

    /** Đọc dữ kiện mountType. */
    public String getMountType() {
        return mountType;
    }

    /** Ghi dữ kiện mountType. */
    public void setMountType(String mountType) {
        this.mountType = mountType;
    }

    /** Đọc dữ kiện orientationCode. */
    public String getOrientationCode() {
        return orientationCode;
    }

    /** Ghi dữ kiện orientationCode. */
    public void setOrientationCode(String orientationCode) {
        this.orientationCode = orientationCode;
    }


}
