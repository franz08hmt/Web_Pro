package vn.edu.webpro.robotlab.business;

import java.io.Serializable;
import java.util.Date;

/** Dữ kiện và luật nghiệp vụ của DiagnosisCheck. */
public class DiagnosisCheck implements Serializable {
    private long id;
    private long scenarioId;
    private String label = "";
    private String observationText = "";
    private boolean required;
    private int displayOrder;
    private Date chosenAt;

    /** Khởi tạo JavaBean. */
    public DiagnosisCheck() {
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

    /** Đọc label. */
    public String getLabel() {
        return label;
    }

    /** Gán label. */
    public void setLabel(String label) {
        this.label = label;
    }

    /** Đọc observationText. */
    public String getObservationText() {
        return observationText;
    }

    /** Gán observationText. */
    public void setObservationText(String observationText) {
        this.observationText = observationText;
    }

    /** Đọc required. */
    public boolean isRequired() {
        return required;
    }

    /** Gán required. */
    public void setRequired(boolean required) {
        this.required = required;
    }

    /** Đọc displayOrder. */
    public int getDisplayOrder() {
        return displayOrder;
    }

    /** Gán displayOrder. */
    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    /** Đọc chosenAt. */
    public Date getChosenAt() {
        return chosenAt;
    }

    /** Gán chosenAt. */
    public void setChosenAt(Date chosenAt) {
        this.chosenAt = chosenAt;
    }

    /** Chỉ trình bày quan sát sau khi server ghi nhận lựa chọn. */
    public String getObservationDisplay() {
        if (chosenAt == null) {
            return "";
        }
        return observationText;
    }

    /** Phép kiểm tra đã có nhật ký chọn. */
    public boolean isChosen() {
        return chosenAt != null;
    }

    /** Thời điểm chọn theo giờ Việt Nam. */
    public String getChosenAtDisplay() {
        return TaskRubric.formatDate(chosenAt);
    }

    /** Kiểm tra văn bản trước khi công bố. */
    public void validate() {
        TaskRubric.requireText(label, 3, 200, "Tên phép kiểm tra");
        TaskRubric.requireText(observationText, 10, 600, "Quan sát");
    }

}
