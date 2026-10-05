package vn.edu.webpro.robotlab.business;
import java.io.Serializable;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;
public class TaskReview implements Serializable {
    private String reviewerName = "";
    public String getReviewerName() { return reviewerName; }
    public void setReviewerName(String value) { reviewerName = value; }
    private long id;
    private long submissionId;
    private long reviewerId;
    private long supersedesReviewId;
    private int explanationLevel;
    private int explanationPoints;
    private BigDecimal totalPoints = BigDecimal.ZERO;
    private String conclusion = "NOT_PASSED";
    private String strengths = "";
    private String improvements = "";
    private String retryGuidance = "";
    private String changeReason = "";
    private Date reviewedAt;
    private Date replacedAt;
    private String replacementReason = "";
    public String getReplacementReason() { return replacementReason; }
    public void setReplacementReason(String value) { replacementReason = value; }
    public TaskReview() {  }
    public long getId() { return id; }
    public void setId(long value) { id = value; }
    public long getSubmissionId() { return submissionId; }
    public void setSubmissionId(long value) { submissionId = value; }
    public long getReviewerId() { return reviewerId; }
    public void setReviewerId(long value) { reviewerId = value; }
    public long getSupersedesReviewId() { return supersedesReviewId; }
    public void setSupersedesReviewId(long value) { supersedesReviewId = value; }
    public int getExplanationLevel() { return explanationLevel; }
    public void setExplanationLevel(int value) { explanationLevel = value; }
    public int getExplanationPoints() { return explanationPoints; }
    public void setExplanationPoints(int value) { explanationPoints = value; }
    public BigDecimal getTotalPoints() { return totalPoints; }
    public void setTotalPoints(BigDecimal value) { totalPoints = value; }
    public String getConclusion() { return conclusion; }
    public void setConclusion(String value) { conclusion = value; }
    public String getStrengths() { return strengths; }
    public void setStrengths(String value) { strengths = value; }
    public String getImprovements() { return improvements; }
    public void setImprovements(String value) { improvements = value; }
    public String getRetryGuidance() { return retryGuidance; }
    public void setRetryGuidance(String value) { retryGuidance = value; }
    public String getChangeReason() { return changeReason; }
    public void setChangeReason(String value) { changeReason = value; }
    public Date getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(Date value) { reviewedAt = value; }
    public Date getReplacedAt() { return replacedAt; }
    public void setReplacedAt(Date value) { replacedAt = value; }

    public boolean isPassed() { return "PASSED".equals(conclusion); }
    public boolean isNeedsRevision() { return "NEEDS_REVISION".equals(conclusion); }
    public boolean isNotPassed() { return "NOT_PASSED".equals(conclusion); }
    public boolean isSuperseded() { return replacedAt != null; }
    public String getConclusionLabel() {
        if (isPassed()) return "Đạt yêu cầu";
        if (isNeedsRevision()) return "Cần bổ sung";
        return "Chưa đạt yêu cầu";
    }
    public String getTotalDisplay() { return TaskRubric.formatNumber(totalPoints); }
    public String getReviewedDisplay() { return TaskRubric.formatDate(reviewedAt); }
    public String getReplacedDisplay() { return TaskRubric.formatDate(replacedAt); }
    public String getExplanationLabel() { return new TaskRubric().explanationLabel(explanationLevel); }
    public void validate(PracticeTask task, TaskSubmission submission, int count, TaskRound next, Date now, boolean editing) {
        TaskRubric rubric = new TaskRubric();
        explanationPoints = rubric.explanationPoints(explanationLevel);
        totalPoints = rubric.totalPoints(submission.getAutomaticPoints(), explanationLevel);
        if (!isPassed() && !isNeedsRevision() && !isNotPassed()) throw new IllegalArgumentException("Kết luận không hợp lệ.");
        TaskRubric.requireText(strengths, 0, 4000, "Điểm mạnh");
        TaskRubric.requireText(improvements, 0, 4000, "Cần cải thiện");
        TaskRubric.requireText(retryGuidance, 0, 4000, "Hướng làm lại");
        TaskRubric.requireText(changeReason, editing ? 1 : 0, 4000, "Lý do sửa");
        if (isPassed() && !rubric.canPass(totalPoints, task.getPassThreshold(), submission.getSessionId() > 0))
            throw new IllegalArgumentException("Điểm dưới ngưỡng đạt hoặc thiếu bằng chứng lắp ráp.");
        if (!isPassed() && (improvements.trim().isEmpty() || retryGuidance.trim().isEmpty()))
            throw new IllegalArgumentException("Cần ghi rõ phần cần cải thiện và hướng làm lại.");
        if (isNeedsRevision() && !rubric.canRequestRevision(task, count, now))
            throw new IllegalArgumentException("Không thể yêu cầu bổ sung: đã hết lượt, nhiệm vụ đóng hoặc cần gia hạn trước.");
        if (editing && !rubric.canEditReview(next))
            throw new IllegalArgumentException("Vòng kế tiếp đã có hoạt động; không được sửa lần chấm.");
    }

}
