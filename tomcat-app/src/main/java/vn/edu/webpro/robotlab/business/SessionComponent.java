package vn.edu.webpro.robotlab.business;

import java.io.Serializable;
import vn.edu.webpro.robotlab.util.JsonUtil;

/** Trạng thái chuẩn bị của một linh kiện trong một phiên lắp ráp. */
public class SessionComponent implements Serializable {
    private String componentId;
    private boolean prepared;

    public SessionComponent() {
        componentId = "";
    }

    public SessionComponent(String componentId, boolean prepared) {
        this.componentId = componentId;
        this.prepared = prepared;
    }

    public String getComponentId() {
        return componentId;
    }

    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }

    /** Thuộc tính kiểu boolean dùng tiền tố "is" theo quy ước JavaBean. */
    public boolean isPrepared() {
        return prepared;
    }

    public void setPrepared(boolean prepared) {
        this.prepared = prepared;
    }

    public String toJson() {
        return "{\"componentId\":" + JsonUtil.quote(componentId) + ",\"isPrepared\":" + prepared + "}";
    }
}
