package vn.edu.webpro.robotlab.business;

import java.io.Serializable;
import vn.edu.webpro.robotlab.util.JsonUtil;

/** Một linh kiện bắt buộc của robot kèm số lượng (bảng robot_components). */
public class RobotComponent implements Serializable {
    private String componentId;
    private int quantity;

    public RobotComponent() {
        componentId = "";
    }

    public RobotComponent(String componentId, int quantity) {
        this.componentId = componentId;
        this.quantity = quantity;
    }

    public String getComponentId() {
        return componentId;
    }

    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String toJson() {
        return "{\"componentId\":" + JsonUtil.quote(componentId) + ",\"quantity\":" + quantity + "}";
    }
}
