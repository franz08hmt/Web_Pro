package vn.edu.webpro.robotlab.business;
import java.io.Serializable;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;
public class PracticeTask implements Serializable {
    private long id;
    private long creatorId;
    private String title = "";
    private String description = "";
    private String robotId = "";
    private String robotName = "";
    private Date dueAt;
    private Date publishedAt;
    private Date createdAt;
    private String state = "DRAFT";
    private String latePolicy = "REJECT_LATE";
    private String rubricTemplate = "A";
    private int maxSubmissions = 2;
    private int passThreshold = 70;
    private boolean allowPriorEvidence;
    public PracticeTask() {  }
    public long getId() { return id; }
    public void setId(long value) { id = value; }
    public long getCreatorId() { return creatorId; }
    public void setCreatorId(long value) { creatorId = value; }
    public String getTitle() { return title; }
    public void setTitle(String value) { title = value; }
    public String getDescription() { return description; }
    public void setDescription(String value) { description = value; }
    public String getRobotId() { return robotId; }
    public void setRobotId(String value) { robotId = value; }
    public String getRobotName() { return robotName; }
    public void setRobotName(String value) { robotName = value; }
    public Date getDueAt() { return dueAt; }
    public void setDueAt(Date value) { dueAt = value; }
    public Date getPublishedAt() { return publishedAt; }
    public void setPublishedAt(Date value) { publishedAt = value; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date value) { createdAt = value; }
    public String getState() { return state; }
    public void setState(String value) { state = value; }
    public String getLatePolicy() { return latePolicy; }
    public void setLatePolicy(String value) { latePolicy = value; }
    public String getRubricTemplate() { return rubricTemplate; }
    public void setRubricTemplate(String value) { rubricTemplate = value; }
    public int getMaxSubmissions() { return maxSubmissions; }
    public void setMaxSubmissions(int value) { maxSubmissions = value; }
    public int getPassThreshold() { return passThreshold; }
    public void setPassThreshold(int value) { passThreshold = value; }
    public boolean isAllowPriorEvidence() { return allowPriorEvidence; }
    public void setAllowPriorEvidence(boolean value) { allowPriorEvidence = value; }

    public boolean isDraft() { return "DRAFT".equals(state); }
    public boolean isOpen() { return "OPEN".equals(state); }
    public boolean isClosed() { return "CLOSED".equals(state); }
    public boolean isArchived() { return "ARCHIVED".equals(state); }
    public boolean isAcceptLate() { return "ACCEPT_LATE_FLAGGED".equals(latePolicy); }
    public boolean isLate(Date now) { return dueAt != null && now.after(dueAt); }
    public boolean canReceive(Date now) { return isOpen() && (!isLate(now) || isAcceptLate()); }
    public String getStateLabel() {
        if (isDraft()) return "Nháp";
        if (isOpen()) return "Đang mở";
        if (isClosed()) return "Đã đóng";
        return "Đã lưu trữ";
    }
    public String getLatePolicyLabel() { if (isAcceptLate()) return "Nhận bài muộn, có đánh dấu"; return "Từ chối bài muộn"; }
    public String getDueDisplay() { return TaskRubric.formatDate(dueAt); }
    public String getDueInput() { if (dueAt == null) return ""; return TaskRubric.formatDate(dueAt, "yyyy-MM-dd'T'HH:mm"); }
    public void validate(boolean robotExists, boolean hasQuiz) {
        TaskRubric.requireText(title, 5, 150, "Tiêu đề");
        TaskRubric.requireText(description, 1, 4000, "Mô tả");
        if (!robotExists || !hasQuiz) throw new IllegalArgumentException("Robot phải có trong catalog và có bài quiz.");
        if (dueAt == null || maxSubmissions < 1 || maxSubmissions > 3 || passThreshold < 50 || passThreshold > 100
                || !"A".equals(rubricTemplate) || (!"REJECT_LATE".equals(latePolicy) && !isAcceptLate()))
            throw new IllegalArgumentException("Thông tin nhiệm vụ không hợp lệ.");
    }
    public void requirePublish(int recipients, Date now) {
        if (!isDraft() || recipients < 1 || dueAt == null || !dueAt.after(now))
            throw new IllegalArgumentException("Chỉ công bố nháp có người được giao và hạn nộp trong tương lai.");
    }
    public void requireExtension(Date next, Date now) {
        if (!isOpen() || next == null || !next.after(dueAt) || !next.after(now))
            throw new IllegalArgumentException("Hạn mới phải muộn hơn hạn cũ và thời điểm hiện tại.");
    }
    public void requireDraft() {
        if (!isDraft()) throw new IllegalArgumentException("Chỉ được sửa hoặc xoá nhiệm vụ nháp; hãy nhân bản nếu muốn đổi nội dung.");
    }
    public void requireOpen() {
        if (!isOpen()) throw new IllegalArgumentException("Thao tác này chỉ áp dụng cho nhiệm vụ đang mở.");
    }
    public void requireClosed() {
        if (!isClosed()) throw new IllegalArgumentException("Chỉ lưu trữ nhiệm vụ đã đóng.");
    }

}
