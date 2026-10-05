package vn.edu.webpro.robotlab.business;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

/** Dữ kiện và luật nghiệp vụ của DiagnosisScenario. */
public class DiagnosisScenario implements Serializable {
    private long id;
    private String robotId = "";
    private String robotName = "";
    private String title = "";
    private String contextText = "";
    private String symptomText = "";
    private String guideId = "";
    private String explanationText = "";
    private String state = "DRAFT";
    private int versionNo = 1;
    private long parentScenarioId;
    private long createdBy;
    private Date createdAt;
    private Date publishedAt;
    private List<DiagnosisCheck> checks = new ArrayList<>();
    private List<DiagnosisOption> options = new ArrayList<>();

    /** Khởi tạo JavaBean. */
    public DiagnosisScenario() {
    }

    /** Đọc id. */
    public long getId() {
        return id;
    }

    /** Gán id. */
    public void setId(long id) {
        this.id = id;
    }

    /** Đọc robotId. */
    public String getRobotId() {
        return robotId;
    }

    /** Gán robotId. */
    public void setRobotId(String robotId) {
        this.robotId = robotId;
    }

    /** Đọc robotName. */
    public String getRobotName() {
        return robotName;
    }

    /** Gán robotName. */
    public void setRobotName(String robotName) {
        this.robotName = robotName;
    }

    /** Đọc title. */
    public String getTitle() {
        return title;
    }

    /** Gán title. */
    public void setTitle(String title) {
        this.title = title;
    }

    /** Đọc contextText. */
    public String getContextText() {
        return contextText;
    }

    /** Gán contextText. */
    public void setContextText(String contextText) {
        this.contextText = contextText;
    }

    /** Đọc symptomText. */
    public String getSymptomText() {
        return symptomText;
    }

    /** Gán symptomText. */
    public void setSymptomText(String symptomText) {
        this.symptomText = symptomText;
    }

    /** Đọc guideId. */
    public String getGuideId() {
        return guideId;
    }

    /** Gán guideId. */
    public void setGuideId(String guideId) {
        this.guideId = guideId;
    }

    /** Đọc explanationText. */
    public String getExplanationText() {
        return explanationText;
    }

    /** Gán explanationText. */
    public void setExplanationText(String explanationText) {
        this.explanationText = explanationText;
    }

    /** Đọc state. */
    public String getState() {
        return state;
    }

    /** Gán state. */
    public void setState(String state) {
        this.state = state;
    }

    /** Đọc versionNo. */
    public int getVersionNo() {
        return versionNo;
    }

    /** Gán versionNo. */
    public void setVersionNo(int versionNo) {
        this.versionNo = versionNo;
    }

    /** Đọc parentScenarioId. */
    public long getParentScenarioId() {
        return parentScenarioId;
    }

    /** Gán parentScenarioId. */
    public void setParentScenarioId(long parentScenarioId) {
        this.parentScenarioId = parentScenarioId;
    }

    /** Đọc createdBy. */
    public long getCreatedBy() {
        return createdBy;
    }

    /** Gán createdBy. */
    public void setCreatedBy(long createdBy) {
        this.createdBy = createdBy;
    }

    /** Đọc createdAt. */
    public Date getCreatedAt() {
        return createdAt;
    }

    /** Gán createdAt. */
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    /** Đọc publishedAt. */
    public Date getPublishedAt() {
        return publishedAt;
    }

    /** Gán publishedAt. */
    public void setPublishedAt(Date publishedAt) {
        this.publishedAt = publishedAt;
    }

    /** Đọc checks. */
    public List<DiagnosisCheck> getChecks() {
        return checks;
    }

    /** Gán checks. */
    public void setChecks(List<DiagnosisCheck> checks) {
        this.checks = checks;
    }

    /** Đọc options. */
    public List<DiagnosisOption> getOptions() {
        return options;
    }

    /** Gán options. */
    public void setOptions(List<DiagnosisOption> options) {
        this.options = options;
    }

    /** Nháp được sửa; bản công bố giữ nguyên nội dung. */
    public boolean isDraft() {
        return "DRAFT".equals(state);
    }

    /** Tình huống đang công bố. */
    public boolean isPublished() {
        return "PUBLISHED".equals(state);
    }

    /** Tình huống đã lưu trữ. */
    public boolean isArchived() {
        return "ARCHIVED".equals(state);
    }

    /** Nhãn trạng thái trình bày. */
    public String getStateLabel() {
        if (isPublished()) {
            return "Đã công bố";
        } else if (isArchived()) {
            return "Đã lưu trữ";
        }
        return "Nháp";
    }

    /** Chặn sửa mọi bản không còn là nháp. */
    public void requireDraft() {
        if (!isDraft()) {
            throw new IllegalArgumentException("Chỉ được sửa tình huống nháp; hãy nhân bản bản đã công bố.");
        }
    }

    /** Lọc lựa chọn nguyên nhân cho form radio. */
    public List<DiagnosisOption> getCauses() {
        return optionsOf("CAUSE");
    }

    /** Lọc lựa chọn biện pháp cho form radio. */
    public List<DiagnosisOption> getActions() {
        return optionsOf("ACTION");
    }

    private List<DiagnosisOption> optionsOf(String kind) {
        List<DiagnosisOption> list = new ArrayList<>();
        for (DiagnosisOption option : options) {
            if (kind.equals(option.getKind())) {
                list.add(option);
            }
        }
        return list;
    }

    /** Bốn ô cố định giúp form biên soạn hoạt động không cần JavaScript. */
    public List<DiagnosisCheck> getCheckSlots() {
        List<DiagnosisCheck> list = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            DiagnosisCheck slot = new DiagnosisCheck();
            slot.setDisplayOrder(i);
            for (DiagnosisCheck check : checks) {
                if (check.getDisplayOrder() == i) {
                    slot = check;
                }
            }
            list.add(slot);
        }
        return list;
    }

    /** Bốn ô nguyên nhân cố định. */
    public List<DiagnosisOption> getCauseSlots() {
        return optionSlots("CAUSE");
    }

    /** Bốn ô biện pháp cố định. */
    public List<DiagnosisOption> getActionSlots() {
        return optionSlots("ACTION");
    }

    private List<DiagnosisOption> optionSlots(String kind) {
        List<DiagnosisOption> list = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            DiagnosisOption slot = new DiagnosisOption();
            slot.setKind(kind);
            slot.setDisplayOrder(i);
            for (DiagnosisOption option : optionsOf(kind)) {
                if (option.getDisplayOrder() == i) {
                    slot = option;
                }
            }
            list.add(slot);
        }
        return list;
    }

    /** Nháp được thiếu nội dung, nhưng không được vượt giới hạn lưu trữ. */
    public void validateDraft() {
        TaskRubric.requireText(title, 5, 150, "Tiêu đề");
        TaskRubric.requireText(contextText, 0, 1500, "Bối cảnh");
        TaskRubric.requireText(symptomText, 0, 1500, "Triệu chứng");
        TaskRubric.requireText(explanationText, 0, 2000, "Giải thích");
        for (DiagnosisCheck check : checks) {
            TaskRubric.requireText(check.getLabel(), 0, 200, "Tên phép kiểm tra");
            TaskRubric.requireText(check.getObservationText(), 0, 600, "Quan sát");
        }
        for (DiagnosisOption option : options) {
            TaskRubric.requireText(option.getLabel(), 0, 200, "Tên lựa chọn");
            TaskRubric.requireText(option.getFeedbackText(), 0, 2000, "Phản hồi");
        }
    }

    /** Kiểm toàn bộ điều kiện công bố; dữ kiện catalog do XxxDB cung cấp. */
    public void validatePublication(boolean robotExists, boolean guideCompatible) {
        TaskRubric.requireText(title, 5, 150, "Tiêu đề");
        TaskRubric.requireText(contextText, 10, 1500, "Bối cảnh");
        TaskRubric.requireText(symptomText, 10, 1500, "Triệu chứng");
        TaskRubric.requireText(explanationText, 20, 2000, "Giải thích");
        if (!robotExists || !guideCompatible) {
            throw new IllegalArgumentException("Robot hoặc hướng dẫn không phù hợp.");
        }
        if (checks.size() < 2 || checks.size() > 4) {
            throw new IllegalArgumentException("Cần 2–4 phép kiểm tra.");
        }
        int required = 0;
        for (DiagnosisCheck check : checks) {
            check.validate();
            if (check.isRequired()) {
                required++;
            }
        }
        if (required == 0) {
            throw new IllegalArgumentException("Cần ít nhất một phép kiểm tra cần thiết.");
        }
        validateOptions(getCauses());
        validateOptions(getActions());
    }

    private void validateOptions(List<DiagnosisOption> list) {
        if (list.size() < 3 || list.size() > 4) {
            throw new IllegalArgumentException("Cần 3–4 nguyên nhân và 3–4 biện pháp.");
        }
        int correct = 0;
        for (DiagnosisOption option : list) {
            option.validate();
            if (option.isCorrect()) {
                correct++;
            }
        }
        if (correct != 1) {
            throw new IllegalArgumentException("Mỗi nhóm phải có đúng một lựa chọn đúng.");
        }
    }

}
