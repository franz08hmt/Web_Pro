package vn.edu.webpro.robotlab.business;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

    private String assemblyMode = "GROUP";
    private long specId;
    private int layoutVersion = 1;
    private int placedSlotCount;
    private int requiredSlotCount;
    private long id;
    private long userId;
    private String robotId;
    private String status;
    private List<SessionComponent> components;
    private List<SessionStep> steps;
    private List<String> assembledPartIds;
    private int requiredComponentCount;
    private int totalStepCount;
    private String createdAt;
    private String updatedAt;
    private String completedAt;

    /** Tạo phiên rỗng để Servlet và JDBC gán dữ kiện. */
    public AssemblySession() {
        robotId = "";
        status = PREPARING;
        createdAt = "";
        updatedAt = "";
        components = new ArrayList<>();
        steps = new ArrayList<>();
        assembledPartIds = new ArrayList<>();
    }

    /** Đọc assemblyMode được server lưu khi tạo phiên. */
    public String getAssemblyMode() {
        return assemblyMode;
    }

    /** Ghi dữ kiện assemblyMode từ database. */
    public void setAssemblyMode(String assemblyMode) {
        this.assemblyMode = assemblyMode;
    }

    /** Đọc specId được server lưu khi tạo phiên. */
    public long getSpecId() {
        return specId;
    }

    /** Ghi dữ kiện specId từ database. */
    public void setSpecId(long specId) {
        this.specId = specId;
    }

    /** Đọc layoutVersion được server lưu khi tạo phiên. */
    public int getLayoutVersion() {
        return layoutVersion;
    }

    /** Ghi dữ kiện layoutVersion từ database. */
    public void setLayoutVersion(int layoutVersion) {
        this.layoutVersion = layoutVersion;
    }

    /** Đọc số điểm đã gắn hợp lệ từ dữ liệu server. */
    public int getPlacedSlotCount() {
        return placedSlotCount;
    }

    /** Ghi số điểm đã gắn từ truy vấn của chính phiên. */
    public void setPlacedSlotCount(int placedSlotCount) {
        this.placedSlotCount = placedSlotCount;
    }

    /** Đọc số điểm bắt buộc thuộc cấu hình cố định của phiên. */
    public int getRequiredSlotCount() {
        return requiredSlotCount;
    }

    /** Ghi mẫu số từ cấu hình phiên, không nhận từ client. */
    public void setRequiredSlotCount(int requiredSlotCount) {
        this.requiredSlotCount = requiredSlotCount;
    }

    /** Chỉ phiên mới có spec mới dùng vật thể chi tiết. */
    public boolean isDetailed() {
        return "DETAILED".equals(assemblyMode);
    }

    /** Trạng thái phiếu kết quả dùng getter thay enum ở JSP. */
    public boolean isCompleted() {
        return COMPLETED.equals(status);
    }

    /** Đọc dữ kiện của phiên theo quy ước JavaBean. */
    public long getId() {
        return id;
    }

    /** Ghi dữ kiện phiên; luật được kiểm trước thao tác lưu. */
    public void setId(long id) {
        this.id = id;
    }

    /** Đọc dữ kiện của phiên theo quy ước JavaBean. */
    public long getUserId() {
        return userId;
    }

    /** Ghi dữ kiện phiên; luật được kiểm trước thao tác lưu. */
    public void setUserId(long userId) {
        this.userId = userId;
    }

    /** Đọc dữ kiện của phiên theo quy ước JavaBean. */
    public String getRobotId() {
        return robotId;
    }

    /** Ghi dữ kiện phiên; luật được kiểm trước thao tác lưu. */
    public void setRobotId(String robotId) {
        this.robotId = robotId;
    }

    /** Đọc dữ kiện của phiên theo quy ước JavaBean. */
    public String getStatus() {
        return status;
    }

    /** Ghi dữ kiện phiên; luật được kiểm trước thao tác lưu. */
    public void setStatus(String status) {
        this.status = status;
    }

    /** Đọc dữ kiện của phiên theo quy ước JavaBean. */
    public List<SessionComponent> getComponents() {
        return components;
    }

    /** Ghi dữ kiện phiên; luật được kiểm trước thao tác lưu. */
    public void setComponents(List<SessionComponent> components) {
        this.components = components;
    }

    /** Đọc dữ kiện của phiên theo quy ước JavaBean. */
    public List<SessionStep> getSteps() {
        return steps;
    }

    /** Ghi dữ kiện phiên; luật được kiểm trước thao tác lưu. */
    public void setSteps(List<SessionStep> steps) {
        this.steps = steps;
    }

    /** Đọc dữ kiện của phiên theo quy ước JavaBean. */
    public List<String> getAssembledPartIds() {
        return assembledPartIds;
    }

    /** Ghi dữ kiện phiên; luật được kiểm trước thao tác lưu. */
    public void setAssembledPartIds(List<String> assembledPartIds) {
        this.assembledPartIds = assembledPartIds;
    }

    /** Số nhóm linh kiện bắt buộc của robot, lấy từ bảng robot_components. */
    public int getRequiredComponentCount() {
        return requiredComponentCount;
    }

    /** Ghi dữ kiện phiên; luật được kiểm trước thao tác lưu. */
    public void setRequiredComponentCount(int requiredComponentCount) {
        this.requiredComponentCount = requiredComponentCount;
    }

    /** Tổng số bước lắp ráp của robot, lấy từ bảng assembly_steps. */
    public int getTotalStepCount() {
        return totalStepCount;
    }

    /** Ghi dữ kiện phiên; luật được kiểm trước thao tác lưu. */
    public void setTotalStepCount(int totalStepCount) {
        this.totalStepCount = totalStepCount;
    }

    /** Thời điểm phiên được tạo (cột created_at) — "thời gian bắt đầu" trên phiếu kết quả. */
    public String getCreatedAt() {
        return createdAt;
    }

    /** Ghi dữ kiện phiên; luật được kiểm trước thao tác lưu. */
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    /** Đọc dữ kiện của phiên theo quy ước JavaBean. */
    public String getUpdatedAt() {
        return updatedAt;
    }

    /** Ghi dữ kiện phiên; luật được kiểm trước thao tác lưu. */
    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    /** NULL cho tới khi phiên chuyển sang COMPLETED; xem AssemblySessionDB.completeSession(). */
    public String getCompletedAt() {
        return completedAt;
    }

    /** Ghi dữ kiện phiên; luật được kiểm trước thao tác lưu. */
    public void setCompletedAt(String completedAt) {
        this.completedAt = completedAt;
    }

    /** Đếm bước hoàn tất bằng vòng lặp theo kiến thức môn học. */
    public long countCompletedSteps() {
        int count = 0;
        for (SessionStep step : steps) {
            if (SessionStep.COMPLETED.equals(step.getStatus())) {
                count++;
            }
        }
        return count;
    }

    /** Giai đoạn chuẩn bị linh kiện: chỉ lúc này mới được tick/bỏ tick linh kiện. */
    public boolean isInPreparation() {
        return PREPARING.equals(status) || READY.equals(status);
    }

    /** Giai đoạn lắp ráp: chỉ lúc này mới được đánh dấu bước và lắp part 3D. */
    public boolean isInProgress() {
        return IN_PROGRESS.equals(status);
    }

    /** Cho phép xóa tiến độ để bắt đầu lại trước hoặc trong giai đoạn lắp ráp. */
    public boolean canResetProgress() {
        return isInPreparation() || isInProgress();
    }

    /** Chỉ cho phép các bước chuyển hợp lệ, không cho nhảy thẳng tới COMPLETED. */
    public boolean canChangeStatusTo(String target) {
        return (READY.equals(status) && IN_PROGRESS.equals(target))
                || (IN_PROGRESS.equals(status) && COMPLETED.equals(target))
                || (!COMPLETED.equals(status) && ABANDONED.equals(target));
    }

    /**
     * Đủ điều kiện hoàn tất khi đang IN_PROGRESS và mọi linh kiện bắt buộc của
     * robot (theo component_id trong robot_components) đều có mặt trong
     * assembledPartIds. So theo TẬP HỢP mã linh kiện phân biệt — không so số
     * lượng — vì session_visual_parts chỉ lưu một dòng cho mỗi nhóm linh kiện
     * bất kể quantity yêu cầu bao nhiêu; đây chỉ là bước kiểm tra nhanh phía
     * servlet, quyết định thật và chống ghi trùng khi có request gần đồng
     * thời nằm ở AssemblySessionDB.completeSession().
     */
    public boolean canCompleteAssembly(List<RobotComponent> requiredComponents) {
        if (isDetailed() || !isInProgress() || requiredComponents.isEmpty()) {
            return false;
        }
        Set<String> assembled = new HashSet<>(assembledPartIds);
        for (RobotComponent required : requiredComponents) {
            if (!assembled.contains(required.getComponentId())) {
                return false;
            }
        }
        return true;
    }

    /* Đếm theo mã linh kiện riêng biệt để một linh kiện lỡ có hai dòng cũng chỉ
       được tính một lần. */
    public long countPreparedComponents() {
        HashSet<String> prepared = new HashSet<>();
        for (SessionComponent component : components) {
            if (component.isPrepared()) {
                prepared.add(component.getComponentId());
            }
        }
        return prepared.size();
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

    /** Đọc dữ kiện của phiên theo quy ước JavaBean. */
    public int getProgressPercent() {
        if (requiredComponentCount == 0) {
            return 0;
        }
        return (int) (countPreparedComponents() * 100 / requiredComponentCount);
    }

    /** Chuyển dữ kiện đã kiểm thành JSON cho client. */
    public String toJson() {
        StringBuilder componentJson = new StringBuilder();
        for (SessionComponent component : components) {
            if (componentJson.length() > 0) {
                componentJson.append(',');
            }
            componentJson.append(component.toJson());
        }

        StringBuilder stepJson = new StringBuilder();
        for (SessionStep step : steps) {
            if (stepJson.length() > 0) {
                stepJson.append(',');
            }
            stepJson.append(step.toJson());
        }

        StringBuilder assembledJson = new StringBuilder();
        for (String partId : assembledPartIds) {
            if (assembledJson.length() > 0) {
                assembledJson.append(',');
            }
            assembledJson.append(JsonUtil.quote(partId));
        }

        return "{\"id\":" + JsonUtil.quote(Long.toString(id))
                + ",\"userId\":" + JsonUtil.quote(Long.toString(userId))
                + ",\"robotId\":" + JsonUtil.quote(robotId)
                + ",\"status\":" + JsonUtil.quote(status)
                + ",\"assemblyMode\":" + JsonUtil.quote(assemblyMode)
                + ",\"layoutVersion\":" + layoutVersion
                + ",\"placedSlotCount\":" + placedSlotCount
                + ",\"requiredSlotCount\":" + requiredSlotCount
                + ",\"progressPercent\":" + getProgressPercent()
                + ",\"completedStepCount\":" + countCompletedSteps()
                + ",\"totalStepCount\":" + totalStepCount
                + ",\"createdAt\":" + JsonUtil.quote(createdAt)
                + ",\"updatedAt\":" + JsonUtil.quote(updatedAt)
                + ",\"completedAt\":" + JsonUtil.quote(completedAt)
                + ",\"components\":[" + componentJson + "]"
                + ",\"steps\":[" + stepJson + "]"
                + ",\"assembledPartIds\":[" + assembledJson + "]}";
    }
}
