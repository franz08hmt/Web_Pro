package vn.edu.webpro.robotlab.business;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/** Nhiệm vụ mẫu A và điều kiện chuyển trạng thái. */
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
    private long diagnosisScenarioId;



    /** Khởi tạo JavaBean với giá trị mặc định. */
    public PracticeTask() {
    }

    /** Trả mã bản ghi cho JSP hoặc lớp dữ liệu. */
    public long getId() {
        return id;
    }

    /** Gán mã bản ghi vào JavaBean. */
    public void setId(long value) {
        id = value;
    }

    /** Trả mã người tạo cho JSP hoặc lớp dữ liệu. */
    public long getCreatorId() {
        return creatorId;
    }

    /** Gán mã người tạo vào JavaBean. */
    public void setCreatorId(long value) {
        creatorId = value;
    }

    /** Trả tiêu đề nhiệm vụ cho JSP hoặc lớp dữ liệu. */
    public String getTitle() {
        return title;
    }

    /** Gán tiêu đề nhiệm vụ vào JavaBean. */
    public void setTitle(String value) {
        title = value;
    }

    /** Trả mô tả nhiệm vụ cho JSP hoặc lớp dữ liệu. */
    public String getDescription() {
        return description;
    }

    /** Gán mô tả nhiệm vụ vào JavaBean. */
    public void setDescription(String value) {
        description = value;
    }

    /** Trả mã mẫu robot cho JSP hoặc lớp dữ liệu. */
    public String getRobotId() {
        return robotId;
    }

    /** Gán mã mẫu robot vào JavaBean. */
    public void setRobotId(String value) {
        robotId = value;
    }

    /** Trả tên mẫu robot cho JSP hoặc lớp dữ liệu. */
    public String getRobotName() {
        return robotName;
    }

    /** Gán tên mẫu robot vào JavaBean. */
    public void setRobotName(String value) {
        robotName = value;
    }

    /** Trả hạn nộp cho JSP hoặc lớp dữ liệu. */
    public Date getDueAt() {
        return dueAt;
    }

    /** Gán hạn nộp vào JavaBean. */
    public void setDueAt(Date value) {
        dueAt = value;
    }

    /** Trả thời điểm công bố cho JSP hoặc lớp dữ liệu. */
    public Date getPublishedAt() {
        return publishedAt;
    }

    /** Gán thời điểm công bố vào JavaBean. */
    public void setPublishedAt(Date value) {
        publishedAt = value;
    }

    /** Trả thời điểm tạo cho JSP hoặc lớp dữ liệu. */
    public Date getCreatedAt() {
        return createdAt;
    }

    /** Gán thời điểm tạo vào JavaBean. */
    public void setCreatedAt(Date value) {
        createdAt = value;
    }

    /** Trả trạng thái cho JSP hoặc lớp dữ liệu. */
    public String getState() {
        return state;
    }

    /** Gán trạng thái vào JavaBean. */
    public void setState(String value) {
        state = value;
    }

    /** Trả chính sách nộp muộn cho JSP hoặc lớp dữ liệu. */
    public String getLatePolicy() {
        return latePolicy;
    }

    /** Gán chính sách nộp muộn vào JavaBean. */
    public void setLatePolicy(String value) {
        latePolicy = value;
    }

    /** Trả mẫu tiêu chí cho JSP hoặc lớp dữ liệu. */
    public String getRubricTemplate() {
        return rubricTemplate;
    }

    /** Gán mẫu tiêu chí vào JavaBean. */
    public void setRubricTemplate(String value) {
        rubricTemplate = value;
    }

    /** Trả số lần nộp tối đa cho JSP hoặc lớp dữ liệu. */
    public int getMaxSubmissions() {
        return maxSubmissions;
    }

    /** Gán số lần nộp tối đa vào JavaBean. */
    public void setMaxSubmissions(int value) {
        maxSubmissions = value;
    }

    /** Trả ngưỡng đạt cho JSP hoặc lớp dữ liệu. */
    public int getPassThreshold() {
        return passThreshold;
    }

    /** Gán ngưỡng đạt vào JavaBean. */
    public void setPassThreshold(int value) {
        passThreshold = value;
    }

    /** Kiểm cho phép dùng bằng chứng trước vòng đầu để JSP chỉ trình bày. */
    public boolean isAllowPriorEvidence() {
        return allowPriorEvidence;
    }

    /** Gán chính sách cho phép bằng chứng trước vòng đầu vào JavaBean. */
    public void setAllowPriorEvidence(boolean value) {
        allowPriorEvidence = value;
    }

    /** Kiểm nhiệm vụ còn nháp để JSP chỉ trình bày. */
    public boolean isDraft() {
        return "DRAFT".equals(state);
    }

    /** Kiểm nhiệm vụ đang mở để JSP chỉ trình bày. */
    public boolean isOpen() {
        return "OPEN".equals(state);
    }

    /** Kiểm nhiệm vụ đã đóng để JSP chỉ trình bày. */
    public boolean isClosed() {
        return "CLOSED".equals(state);
    }

    /** Kiểm nhiệm vụ đã lưu trữ để JSP chỉ trình bày. */
    public boolean isArchived() {
        return "ARCHIVED".equals(state);
    }

    /** Kiểm chính sách cho phép nhận bài muộn để JSP chỉ trình bày. */
    public boolean isAcceptLate() {
        return "ACCEPT_LATE_FLAGGED".equals(latePolicy);
    }

    /** Kiểm thời điểm đã quá hạn nộp để JSP chỉ trình bày. */
    public boolean isLate(Date now) {
        return dueAt != null && now.after(dueAt);
    }

    /** Kiểm nhiệm vụ mở và chính sách nhận bài sau hạn. */
    public boolean canReceive(Date now) {
        return isOpen() && (!isLate(now) || isAcceptLate());
    }

    /** Trả nhãn trạng thái nhiệm vụ cho JSP hoặc lớp dữ liệu. */
    public String getStateLabel() {
        if (isDraft()) {
            return "Nháp";
        }
        if (isOpen()) {
            return "Đang mở";
        }
        if (isClosed()) {
            return "Đã đóng";
        }
        return "Đã lưu trữ";
    }

    /** Trả nhãn chính sách nộp muộn cho JSP hoặc lớp dữ liệu. */
    public String getLatePolicyLabel() {
        if (isAcceptLate()) {
            return "Nhận bài muộn, có đánh dấu";
        }
        return "Từ chối bài muộn";
    }

    /** Trả hạn nộp theo giờ Việt Nam cho JSP hoặc lớp dữ liệu. */
    public String getDueDisplay() {
        return TaskRubric.formatDate(dueAt);
    }

    /** Trả hạn nộp theo mẫu datetime-local cho JSP hoặc lớp dữ liệu. */
    public String getDueInput() {
        if (dueAt == null) {
            return "";
        }
        return TaskRubric.formatDate(dueAt, "yyyy-MM-dd'T'HH:mm");
    }

    /** Kiểm các điều kiện hợp lệ của dữ liệu nghiệp vụ. */
    public void validate(boolean robotExists, boolean hasQuiz) {
        TaskRubric.requireText(title, 5, 150, "Tiêu đề");
        TaskRubric.requireText(description, 1, 4000, "Mô tả");
        if (!robotExists || !hasQuiz) {
            throw new IllegalArgumentException("Robot phải có trong catalog và có bài quiz.");
        }
        if (dueAt == null
                || maxSubmissions < 1
                || maxSubmissions > 3
                || passThreshold < 50
                || passThreshold > 100
                || (!"A".equals(rubricTemplate) && !isRubricB()) || (isRubricB() && diagnosisScenarioId <= 0)
                || (!"REJECT_LATE".equals(latePolicy) && !isAcceptLate())) {
            throw new IllegalArgumentException("Thông tin nhiệm vụ không hợp lệ.");
        }
    }

    /** Kiểm nháp, người được giao và hạn tương lai trước công bố. */
    public void requirePublish(int recipients, Date now) {
        if (!isDraft() || recipients < 1 || dueAt == null || !dueAt.after(now)) {
            throw new IllegalArgumentException(
                    "Chỉ công bố nháp có người được giao và hạn nộp trong tương lai.");
        }
    }

    /** Chỉ gia hạn nhiệm vụ mở đến thời điểm muộn hơn. */
    public void requireExtension(Date next, Date now) {
        if (!isOpen() || next == null || !next.after(dueAt) || !next.after(now)) {
            throw new IllegalArgumentException(
                    "Hạn mới phải muộn hơn hạn cũ và thời điểm hiện tại.");
        }
    }

    /** Yêu cầu nhiệm vụ nháp trước khi sửa hoặc xoá. */
    public void requireDraft() {
        if (!isDraft()) {
            throw new IllegalArgumentException(
                    "Chỉ được sửa hoặc xoá nhiệm vụ nháp; hãy nhân bản nếu muốn đổi nội dung.");
        }
    }

    /** Yêu cầu nhiệm vụ đang mở trước thao tác quản lý. */
    public void requireOpen() {
        if (!isOpen()) {
            throw new IllegalArgumentException("Thao tác này chỉ áp dụng cho nhiệm vụ đang mở.");
        }
    }

    /** Yêu cầu nhiệm vụ đã đóng trước khi lưu trữ. */
    public void requireClosed() {
        if (!isClosed()) {
            throw new IllegalArgumentException("Chỉ lưu trữ nhiệm vụ đã đóng.");
        }
    }

    /** Đọc diagnosisScenarioId. */
    public long getDiagnosisScenarioId() {
        return diagnosisScenarioId;
    }

    /** Gán diagnosisScenarioId. */
    public void setDiagnosisScenarioId(long diagnosisScenarioId) {
        this.diagnosisScenarioId = diagnosisScenarioId;
    }


    /** Mẫu B yêu cầu chẩn đoán; mẫu A giữ nguyên luồng cũ. */
    public boolean isRubricB() {
        return "B".equals(rubricTemplate);
    }

    /** Dựng tiêu chí cố định từ mẫu của nhiệm vụ. */
    public TaskRubric getRubric() {
        TaskRubric rubric = new TaskRubric();
        rubric.setTemplate(rubricTemplate);
        return rubric;
    }

}
