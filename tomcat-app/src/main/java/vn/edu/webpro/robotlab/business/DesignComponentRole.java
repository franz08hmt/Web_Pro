package vn.edu.webpro.robotlab.business;

import java.io.Serializable;

/** Dữ kiện và quy tắc trình bày của DesignComponentRole. */
public class DesignComponentRole implements Serializable {
    private String componentId = "";
    private String role = "";
    private int driverChannels;

    /** Tạo JavaBean rỗng theo quy ước môn học. */
    public DesignComponentRole() {
    }

    /** Đọc dữ kiện componentId. */
    public String getComponentId() {
        return componentId;
    }

    /** Ghi dữ kiện componentId. */
    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }

    /** Đọc dữ kiện role. */
    public String getRole() {
        return role;
    }

    /** Ghi dữ kiện role. */
    public void setRole(String role) {
        this.role = role;
    }

    /** Đọc dữ kiện driverChannels. */
    public int getDriverChannels() {
        return driverChannels;
    }

    /** Ghi dữ kiện driverChannels. */
    public void setDriverChannels(int driverChannels) {
        this.driverChannels = driverChannels;
    }


}
