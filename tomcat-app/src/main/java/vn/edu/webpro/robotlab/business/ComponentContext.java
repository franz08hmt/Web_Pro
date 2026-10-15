package vn.edu.webpro.robotlab.business;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import vn.edu.webpro.robotlab.util.JsonUtil;

/** Liên kết ngữ cảnh chỉ đọc, dữ liệu cá nhân giới hạn theo session. */
public class ComponentContext implements Serializable {
    private String componentId = "";
    private int owned = 0;
    private String productId = "";
    private List<RobotLearningLink> robots = new ArrayList<>();

    /** Tạo JavaBean rỗng. */
    public ComponentContext() {
    }

    /** Đọc componentId. */
    public String getComponentId() { return componentId; }

    /** Ghi componentId. */
    public void setComponentId(String componentId) { this.componentId = componentId; }

    /** Đọc owned. */
    public int getOwned() { return owned; }

    /** Ghi owned. */
    public void setOwned(int owned) { this.owned = owned; }

    /** Đọc productId. */
    public String getProductId() { return productId; }

    /** Ghi productId. */
    public void setProductId(String productId) { this.productId = productId; }

    /** Đọc robots. */
    public List<RobotLearningLink> getRobots() { return robots; }

    /** Ghi robots. */
    public void setRobots(List<RobotLearningLink> robots) { this.robots = robots; }

    /** JSON; số đang có chỉ hiện sau khi server đã xác thực người dùng. */
    public String toJson(boolean authenticated) {
        StringBuilder json = new StringBuilder("{\"componentId\":");
        json.append(JsonUtil.quote(componentId)).append(",\"owned\":");
        json.append(authenticated ? String.valueOf(owned) : "null");
        json.append(",\"productId\":").append(JsonUtil.quote(productId)).append(",\"robots\":[");
        for (RobotLearningLink robot : robots) {
            if (json.charAt(json.length() - 1) != '[') {
                json.append(',');
            }
            json.append(robot.toJson());
        }
        return json.append("]}").toString();
    }
}

