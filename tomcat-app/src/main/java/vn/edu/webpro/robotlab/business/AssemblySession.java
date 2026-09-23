package vn.edu.webpro.robotlab.business;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import vn.edu.webpro.robotlab.util.JsonUtil;

/**
 * Một phiên thực hành của một tài khoản với một mô hình robot.
 *
 * Ngoài get/set như JavaBean, lớp này tự giữ luật nghiệp vụ của chính nó —
 * giống lớp Cart trong sách tự có addItem/removeItem — để servlet chỉ việc hỏi
 * "phiên có cho phép thao tác này không" rồi gọi AssemblySessionDB.
 *
 * Vòng đời: PREPARING ⇄ READY → IN_PROGRESS → COMPLETED; ABANDONED được phép từ
 * mọi trạng thái trừ COMPLETED.
 */
public class AssemblySession implements Serializable {
    public static final String PREPARING = "PREPARING";
    public static final String READY = "READY";
    public static final String IN_PROGRESS = "IN_PROGRESS";
    public static final String COMPLETED = "COMPLETED";
    public static final String ABANDONED = "ABANDONED";

    private long id;
    private long userId;
    private String robotId;
    private String status;
    private List<SessionComponent> components;
    private List<SessionStep> steps;
    private List<String> assembledPartIds;
    private int requiredComponentCount;
    private int totalStepCount;
    private String updatedAt;

    public AssemblySession() {
        robotId = "";
        status = PREPARING;
        updatedAt = "";
        components = new ArrayList<>();
        steps = new ArrayList<>();
        assembledPartIds = new ArrayList<>();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getRobotId() {
        return robotId;
    }

    public void setRobotId(String robotId) {
        this.robotId = robotId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<SessionComponent> getComponents() {
        return components;
    }

    public void setComponents(List<SessionComponent> components) {
        this.components = components;
    }

    public List<SessionStep> getSteps() {
        return steps;
    }

    public void setSteps(List<SessionStep> steps) {
        this.steps = steps;
    }

    public List<String> getAssembledPartIds() {
        return assembledPartIds;
    }

    public void setAssembledPartIds(List<String> assembledPartIds) {
        this.assembledPartIds = assembledPartIds;
    }

    /** Số nhóm linh kiện bắt buộc của robot, lấy từ bảng robot_components. */
    public int getRequiredComponentCount() {
        return requiredComponentCount;
    }

    public void setRequiredComponentCount(int requiredComponentCount) {
        this.requiredComponentCount = requiredComponentCount;
    }

    /** Tổng số bước lắp ráp của robot, lấy từ bảng assembly_steps. */
    public int getTotalStepCount() {
        return totalStepCount;
    }

    public void setTotalStepCount(int totalStepCount) {
        this.totalStepCount = totalStepCount;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public long countCompletedSteps() {
        return steps.stream()
                .filter(step -> SessionStep.COMPLETED.equals(step.getStatus()))
                .count();
    }

    /** Giai đoạn chuẩn bị linh kiện: chỉ lúc này mới được tick/bỏ tick linh kiện. */
    public boolean isInPreparation() {
        return PREPARING.equals(status) || READY.equals(status);
    }

    /** Giai đoạn lắp ráp: chỉ lúc này mới được đánh dấu bước và lắp part 3D. */
    public boolean isInProgress() {
        return IN_PROGRESS.equals(status);
    }

    /** Chỉ cho phép các bước chuyển hợp lệ, không cho nhảy thẳng tới COMPLETED. */
    public boolean canChangeStatusTo(String target) {
        return (READY.equals(status) && IN_PROGRESS.equals(target))
                || (IN_PROGRESS.equals(status) && COMPLETED.equals(target))
                || (!COMPLETED.equals(status) && ABANDONED.equals(target));
    }

    /* Đếm theo mã linh kiện riêng biệt để một linh kiện lỡ có hai dòng cũng chỉ
       được tính một lần. */
    public long countPreparedComponents() {
        return components.stream()
                .filter(SessionComponent::isPrepared)
                .map(SessionComponent::getComponentId)
                .distinct()
                .count();
    }

    /**
     * Trạng thái đúng của giai đoạn chuẩn bị, tính lại từ dữ liệu thật: đủ mọi
     * linh kiện bắt buộc thì READY, còn thiếu thì PREPARING. Trình duyệt không
     * tự khai báo được là đã đủ.
     */
    public String getExpectedPreparationStatus() {
        boolean complete = requiredComponentCount > 0
                && countPreparedComponents() == requiredComponentCount;
        return complete ? READY : PREPARING;
    }

    public int getProgressPercent() {
        if (requiredComponentCount == 0) return 0;
        return (int) (countPreparedComponents() * 100 / requiredComponentCount);
    }

    public String toJson() {
        StringBuilder componentJson = new StringBuilder();
        for (SessionComponent component : components) {
            if (componentJson.length() > 0) componentJson.append(',');
            componentJson.append(component.toJson());
        }

        StringBuilder stepJson = new StringBuilder();
        for (SessionStep step : steps) {
            if (stepJson.length() > 0) stepJson.append(',');
            stepJson.append(step.toJson());
        }

        StringBuilder assembledJson = new StringBuilder();
        for (String partId : assembledPartIds) {
            if (assembledJson.length() > 0) assembledJson.append(',');
            assembledJson.append(JsonUtil.quote(partId));
        }

        return "{\"id\":" + JsonUtil.quote(Long.toString(id))
                + ",\"userId\":" + JsonUtil.quote(Long.toString(userId))
                + ",\"robotId\":" + JsonUtil.quote(robotId)
                + ",\"status\":" + JsonUtil.quote(status)
                + ",\"progressPercent\":" + getProgressPercent()
                + ",\"completedStepCount\":" + countCompletedSteps()
                + ",\"totalStepCount\":" + totalStepCount
                + ",\"updatedAt\":" + JsonUtil.quote(updatedAt)
                + ",\"components\":[" + componentJson + "]"
                + ",\"steps\":[" + stepJson + "]"
                + ",\"assembledPartIds\":[" + assembledJson + "]}";
    }
}
