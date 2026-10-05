package vn.edu.webpro.robotlab.business;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/** Lần chấm bất biến và điều kiện sửa kết luận. */
public class TaskReview implements Serializable {

    private String reviewerName = "";

    /** Trả họ tên người chấm cho JSP hoặc lớp dữ liệu. */
    public String getReviewerName() {
        return reviewerName;
    }

    /** Gán họ tên người chấm vào JavaBean. */
    public void setReviewerName(String value) {
        reviewerName = value;
    }

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

    /** Trả lý do lần chấm bị thay thế cho JSP hoặc lớp dữ liệu. */
    public String getReplacementReason() {
        return replacementReason;
    }

    /** Gán lý do lần chấm bị thay thế vào JavaBean. */
    public void setReplacementReason(String value) {
        replacementReason = value;
    }

    /** Khởi tạo JavaBean với giá trị mặc định. */
    public TaskReview() {
    }

    /** Trả mã bản ghi cho JSP hoặc lớp dữ liệu. */
    public long getId() {
        return id;
    }

    /** Gán mã bản ghi vào JavaBean. */
    public void setId(long value) {
        id = value;
    }

    /** Trả mã bài nộp cho JSP hoặc lớp dữ liệu. */
    public long getSubmissionId() {
        return submissionId;
    }

    /** Gán mã bài nộp vào JavaBean. */
    public void setSubmissionId(long value) {
        submissionId = value;
    }

    /** Trả mã người chấm cho JSP hoặc lớp dữ liệu. */
    public long getReviewerId() {
        return reviewerId;
    }

    /** Gán mã người chấm vào JavaBean. */
    public void setReviewerId(long value) {
        reviewerId = value;
    }

    /** Trả mã lần chấm bị thay thế cho JSP hoặc lớp dữ liệu. */
    public long getSupersedesReviewId() {
        return supersedesReviewId;
    }

    /** Gán mã lần chấm bị thay thế vào JavaBean. */
    public void setSupersedesReviewId(long value) {
        supersedesReviewId = value;
    }

    /** Trả mức giải thích từ 0 đến 4 cho JSP hoặc lớp dữ liệu. */
    public int getExplanationLevel() {
        return explanationLevel;
    }

    /** Gán mức giải thích từ 0 đến 4 vào JavaBean. */
    public void setExplanationLevel(int value) {
        explanationLevel = value;
    }

    /** Trả điểm giải thích cho JSP hoặc lớp dữ liệu. */
    public int getExplanationPoints() {
        return explanationPoints;
    }

    /** Gán điểm giải thích vào JavaBean. */
    public void setExplanationPoints(int value) {
        explanationPoints = value;
    }

    /** Trả tổng điểm theo giá trị đã lưu cho JSP hoặc lớp dữ liệu. */
    public BigDecimal getTotalPoints() {
        return totalPoints;
    }

    /** Gán tổng điểm theo giá trị đã lưu vào JavaBean. */
    public void setTotalPoints(BigDecimal value) {
        totalPoints = value;
    }

    /** Trả kết luận chấm cho JSP hoặc lớp dữ liệu. */
    public String getConclusion() {
        return conclusion;
    }

    /** Gán kết luận chấm vào JavaBean. */
    public void setConclusion(String value) {
        conclusion = value;
    }

    /** Trả nhận xét điểm mạnh cho JSP hoặc lớp dữ liệu. */
    public String getStrengths() {
        return strengths;
    }

    /** Gán nhận xét điểm mạnh vào JavaBean. */
    public void setStrengths(String value) {
        strengths = value;
    }

    /** Trả nhận xét cần cải thiện cho JSP hoặc lớp dữ liệu. */
    public String getImprovements() {
        return improvements;
    }

    /** Gán nhận xét cần cải thiện vào JavaBean. */
    public void setImprovements(String value) {
        improvements = value;
    }

    /** Trả hướng làm lại cho JSP hoặc lớp dữ liệu. */
    public String getRetryGuidance() {
        return retryGuidance;
    }

    /** Gán hướng làm lại vào JavaBean. */
    public void setRetryGuidance(String value) {
        retryGuidance = value;
    }

    /** Trả lý do sửa lần chấm cho JSP hoặc lớp dữ liệu. */
    public String getChangeReason() {
        return changeReason;
    }

    /** Gán lý do sửa lần chấm vào JavaBean. */
    public void setChangeReason(String value) {
        changeReason = value;
    }

    /** Trả thời điểm chấm cho JSP hoặc lớp dữ liệu. */
    public Date getReviewedAt() {
        return reviewedAt;
    }

    /** Gán thời điểm chấm vào JavaBean. */
    public void setReviewedAt(Date value) {
        reviewedAt = value;
    }

    /** Trả thời điểm sửa lần chấm cho JSP hoặc lớp dữ liệu. */
    public Date getReplacedAt() {
        return replacedAt;
    }

    /** Gán thời điểm sửa lần chấm vào JavaBean. */
    public void setReplacedAt(Date value) {
        replacedAt = value;
    }

    /** Kiểm kết luận đạt yêu cầu để JSP chỉ trình bày. */
    public boolean isPassed() {
        return "PASSED".equals(conclusion);
    }

    /** Kiểm kết luận cần bổ sung để JSP chỉ trình bày. */
    public boolean isNeedsRevision() {
        return "NEEDS_REVISION".equals(conclusion);
    }

    /** Kiểm kết luận chưa đạt để JSP chỉ trình bày. */
    public boolean isNotPassed() {
        return "NOT_PASSED".equals(conclusion);
    }

    /** Kiểm lần chấm đã được thay thế để JSP chỉ trình bày. */
    public boolean isSuperseded() {
        return replacedAt != null;
    }

    /** Trả nhãn kết luận chấm cho JSP hoặc lớp dữ liệu. */
    public String getConclusionLabel() {
        if (isPassed()) {
            return "Đạt yêu cầu";
        }
        if (isNeedsRevision()) {
            return "Cần bổ sung";
        }
        return "Chưa đạt yêu cầu";
    }

    /** Trả tổng điểm định dạng vi-VN cho JSP hoặc lớp dữ liệu. */
    public String getTotalDisplay() {
        return TaskRubric.formatNumber(totalPoints);
    }

    /** Trả ngày chấm theo giờ Việt Nam cho JSP hoặc lớp dữ liệu. */
    public String getReviewedDisplay() {
        return TaskRubric.formatDate(reviewedAt);
    }

    /** Trả ngày sửa chấm theo giờ Việt Nam cho JSP hoặc lớp dữ liệu. */
    public String getReplacedDisplay() {
        return TaskRubric.formatDate(replacedAt);
    }

    /** Trả mô tả mức giải thích cho JSP hoặc lớp dữ liệu. */
    public String getExplanationLabel() {
        return new TaskRubric().explanationLabel(explanationLevel);
    }

    /** Kiểm các điều kiện hợp lệ của dữ liệu nghiệp vụ. */
    public void validate(
            PracticeTask task,
            TaskSubmission submission,
            int count,
            TaskRound next,
            Date now,
            boolean editing) {
        TaskRubric rubric = new TaskRubric();
        explanationPoints = rubric.explanationPoints(explanationLevel);
        totalPoints = rubric.totalPoints(submission.getAutomaticPoints(), explanationLevel);
        if (!isPassed() && !isNeedsRevision() && !isNotPassed()) {
            throw new IllegalArgumentException("Kết luận không hợp lệ.");
        }
        TaskRubric.requireText(strengths, 0, 4000, "Điểm mạnh");
        TaskRubric.requireText(improvements, 0, 4000, "Cần cải thiện");
        TaskRubric.requireText(retryGuidance, 0, 4000, "Hướng làm lại");
        TaskRubric.requireText(changeReason, editing ? 1 : 0, 4000, "Lý do sửa");
        if (isPassed()
                && !rubric.canPass(
                        totalPoints, task.getPassThreshold(), submission.getSessionId() > 0)) {
            throw new IllegalArgumentException(
                    "Điểm dưới ngưỡng đạt hoặc thiếu bằng chứng lắp ráp.");
        }
        if (!isPassed() && (improvements.trim().isEmpty() || retryGuidance.trim().isEmpty())) {
            throw new IllegalArgumentException("Cần ghi rõ phần cần cải thiện và hướng làm lại.");
        }
        if (isNeedsRevision() && !rubric.canRequestRevision(task, count, now)) {
            throw new IllegalArgumentException(
                    "Không thể yêu cầu bổ sung: đã hết lượt, nhiệm vụ đóng hoặc cần "
                            + "gia hạn trước.");
        }
        if (editing && !rubric.canEditReview(next)) {
            throw new IllegalArgumentException(
                    "Vòng kế tiếp đã có hoạt động; không được sửa lần chấm.");
        }
    }
}
