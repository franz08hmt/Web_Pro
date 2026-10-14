package vn.edu.webpro.robotlab.business;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

/** Dữ kiện và quy tắc trình bày của DesignRuleSet. */
public class DesignRuleSet implements Serializable {
    private long id;
    private int versionNo;
    private String state = "DRAFT";
    private List<DesignComponentRole> roles = new ArrayList<>();
    private List<DesignMount> mounts = new ArrayList<>();
    private List<DesignFit> fits = new ArrayList<>();
    private List<DesignGoal> goals = new ArrayList<>();
    private List<DesignRequirement> requirements = new ArrayList<>();

    /** Tạo JavaBean rỗng theo quy ước môn học. */
    public DesignRuleSet() {
    }

    /** Đọc dữ kiện id. */
    public long getId() {
        return id;
    }

    /** Ghi dữ kiện id. */
    public void setId(long id) {
        this.id = id;
    }

    /** Đọc dữ kiện versionNo. */
    public int getVersionNo() {
        return versionNo;
    }

    /** Ghi dữ kiện versionNo. */
    public void setVersionNo(int versionNo) {
        this.versionNo = versionNo;
    }

    /** Đọc dữ kiện state. */
    public String getState() {
        return state;
    }

    /** Ghi dữ kiện state. */
    public void setState(String state) {
        this.state = state;
    }

    /** Đọc dữ kiện roles. */
    public List<DesignComponentRole> getRoles() {
        return roles;
    }

    /** Ghi dữ kiện roles. */
    public void setRoles(List<DesignComponentRole> roles) {
        this.roles = roles;
    }

    /** Đọc dữ kiện mounts. */
    public List<DesignMount> getMounts() {
        return mounts;
    }

    /** Ghi dữ kiện mounts. */
    public void setMounts(List<DesignMount> mounts) {
        this.mounts = mounts;
    }

    /** Đọc dữ kiện fits. */
    public List<DesignFit> getFits() {
        return fits;
    }

    /** Ghi dữ kiện fits. */
    public void setFits(List<DesignFit> fits) {
        this.fits = fits;
    }

    /** Đọc dữ kiện goals. */
    public List<DesignGoal> getGoals() {
        return goals;
    }

    /** Ghi dữ kiện goals. */
    public void setGoals(List<DesignGoal> goals) {
        this.goals = goals;
    }

    /** Đọc dữ kiện requirements. */
    public List<DesignRequirement> getRequirements() {
        return requirements;
    }

    /** Ghi dữ kiện requirements. */
    public void setRequirements(List<DesignRequirement> requirements) {
        this.requirements = requirements;
    }

    /** Không có vai trò nghĩa là chưa hỗ trợ, không mặc định coi là hợp lệ. */
    public DesignComponentRole roleFor(String componentId) {
        for (DesignComponentRole role : roles) {
            if (role.getComponentId().equals(componentId)) {
                return role;
            }
        }
        return null;
    }

    /** Tìm điểm gắn do loại vật thể cha cung cấp trong phiên bản cố định. */
    public DesignMount mountFor(String componentId, String code) {
        for (DesignMount mount : mounts) {
            if (mount.getComponentId().equals(componentId) && mount.getMountCode().equals(code)) {
                return mount;
            }
        }
        return null;
    }

    /** Loại phù hợp chưa có nghĩa hướng được chấp nhận. */
    public boolean fitsType(String componentId, String type) {
        for (DesignFit fit : fits) {
            if (fit.getComponentId().equals(componentId) && fit.getMountType().equals(type)) {
                return true;
            }
        }
        return false;
    }

    /** JavaBean đối chiếu hướng, không tin cờ compatible của client. */
    public boolean fitsOrientation(String componentId, String type, String orientation) {
        for (DesignFit fit : fits) {
            if (fit.getComponentId().equals(componentId) && fit.getMountType().equals(type)
                    && fit.getOrientationCode().equals(orientation)) {
                return true;
            }
        }
        return false;
    }

    /** Chỉ mục tiêu có yêu cầu trong phiên bản này mới được chọn. */
    public boolean hasGoal(String code) {
        for (DesignRequirement requirement : requirements) {
            if (requirement.getGoalCode().equals(code)) {
                return true;
            }
        }
        return false;
    }

}
