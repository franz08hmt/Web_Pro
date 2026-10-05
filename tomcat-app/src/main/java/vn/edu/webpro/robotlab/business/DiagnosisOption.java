package vn.edu.webpro.robotlab.business;

import java.io.Serializable;

/** Dữ kiện và luật nghiệp vụ của DiagnosisOption. */
public class DiagnosisOption implements Serializable {
    private long id;
    private long scenarioId;
    private String kind = "CAUSE";
    private String label = "";
    private String feedbackText = "";
    private boolean correct;
    private int displayOrder;

    /** Khởi tạo JavaBean. */
    public DiagnosisOption() {
    }

    /** Đọc id. */
    public long getId() {
        return id;
    }

    /** Gán id. */
    public void setId(long id) {
        this.id = id;
    }

    /** Đọc scenarioId. */
    public long getScenarioId() {
        return scenarioId;
    }

    /** Gán scenarioId. */
    public void setScenarioId(long scenarioId) {
        this.scenarioId = scenarioId;
    }

    /** Đọc kind. */
    public String getKind() {
        return kind;
    }

    /** Gán kind. */
    public void setKind(String kind) {
        this.kind = kind;
    }

    /** Đọc label. */
    public String getLabel() {
        return label;
    }

    /** Gán label. */
    public void setLabel(String label) {
        this.label = label;
    }

    /** Đọc feedbackText. */
    public String getFeedbackText() {
        return feedbackText;
    }

    /** Gán feedbackText. */
    public void setFeedbackText(String feedbackText) {
        this.feedbackText = feedbackText;
    }

    /** Đọc correct. */
    public boolean isCorrect() {
        return correct;
    }

    /** Gán correct. */
    public void setCorrect(boolean correct) {
        this.correct = correct;
    }

    /** Đọc displayOrder. */
    public int getDisplayOrder() {
        return displayOrder;
    }

    /** Gán displayOrder. */
    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    /** Lựa chọn nguyên nhân. */
    public boolean isCause() {
        return "CAUSE".equals(kind);
    }

    /** Lựa chọn biện pháp. */
    public boolean isAction() {
        return "ACTION".equals(kind);
    }

    /** Kiểm tra loại và nội dung lựa chọn. */
    public void validate() {
        if (!isCause() && !isAction()) {
            throw new IllegalArgumentException("Loại lựa chọn không hợp lệ.");
        }
        TaskRubric.requireText(label, 3, 200, "Lựa chọn");
        TaskRubric.requireText(feedbackText, 1, 2000, "Phản hồi");
    }

}
