package vn.edu.webpro.robotlab.business;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

/**
 * Một dòng trong lịch sử chẩn đoán PRACTICE đã nộp.
 * Chỉ chứa dữ liệu cần thiết để hiển thị danh sách.
 */
public class DiagnosisHistoryItem implements Serializable {

    private long id;
    private String scenarioTitle;
    private String robotName;
    private BigDecimal score;
    private Date submittedAt;

    public DiagnosisHistoryItem() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getScenarioTitle() {
        return scenarioTitle;
    }

    public void setScenarioTitle(String scenarioTitle) {
        this.scenarioTitle = scenarioTitle;
    }

    public String getRobotName() {
        return robotName;
    }

    public void setRobotName(String robotName) {
        this.robotName = robotName;
    }

    public BigDecimal getScore() {
        return score;
    }

    public void setScore(BigDecimal score) {
        this.score = score;
    }

    public Date getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(Date submittedAt) {
        this.submittedAt = submittedAt;
    }

    /** Dùng luật điểm duy nhất của lượt đã lưu, không tạo công thức riêng cho lịch sử. */
    public void useAttempt(DiagnosisAttempt attempt) {
        if (attempt == null) {
            throw new IllegalArgumentException("Thiếu dữ liệu lượt chẩn đoán.");
        }
        score = attempt.getScoreExact();
        id = attempt.getId();
        submittedAt = attempt.getSubmittedAt();
    }

    /** Làm tròn khi trình bày, giống trang kết quả. Thiếu dữ liệu không phải điểm 0. */
    public String getScoreDisplay() {
        if (score == null) {
            return "Chưa có dữ liệu";
        }
        return TaskRubric.formatNumber(score.setScale(1, RoundingMode.HALF_UP));
    }

    /** Giờ Việt Nam theo cùng tiện ích đang dùng ở trang kết quả chẩn đoán. */
    public String getSubmittedAtDisplay() {
        if (submittedAt == null) {
            return "Chưa có dữ liệu";
        }
        return TaskRubric.formatDate(submittedAt);
    }
}
