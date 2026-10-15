package vn.edu.webpro.robotlab.business;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import vn.edu.webpro.robotlab.util.JsonUtil;

/** Liên kết ngữ cảnh chỉ đọc, dữ liệu cá nhân giới hạn theo session. */
public class RobotLearningLink implements Serializable {
    private String id = "";
    private String name = "";
    private boolean hasWiring = false;
    private boolean hasGuide = false;

    /** Tạo JavaBean rỗng. */
    public RobotLearningLink() {
    }

    /** Đọc id. */
    public String getId() { return id; }

    /** Ghi id. */
    public void setId(String id) { this.id = id; }

    /** Đọc name. */
    public String getName() { return name; }

    /** Ghi name. */
    public void setName(String name) { this.name = name; }

    /** Đọc hasWiring. */
    public boolean isHasWiring() { return hasWiring; }

    /** Ghi hasWiring. */
    public void setHasWiring(boolean hasWiring) { this.hasWiring = hasWiring; }

    /** Đọc hasGuide. */
    public boolean isHasGuide() { return hasGuide; }

    /** Ghi hasGuide. */
    public void setHasGuide(boolean hasGuide) { this.hasGuide = hasGuide; }

    /** JSON công khai của đường dẫn học tập. */
    public String toJson() {
        return "{\"id\":" + JsonUtil.quote(id) + ",\"name\":" + JsonUtil.quote(name)
                + ",\"hasWiring\":" + hasWiring + ",\"hasGuide\":" + hasGuide + "}";
    }
}

