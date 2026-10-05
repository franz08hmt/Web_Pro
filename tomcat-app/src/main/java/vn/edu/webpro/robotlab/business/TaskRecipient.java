package vn.edu.webpro.robotlab.business;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/** Người được giao và trạng thái suy ra từ vòng nộp hiện tại. */
public class TaskRecipient implements Serializable {

    private long id;
    private long taskId;
    private long userId;
    private String fullName = "";
    private PracticeTask task;
    private TaskRound currentRound;
    private TaskSubmission latestSubmission;
    private int submissionCount;

    /** Khởi tạo JavaBean với giá trị mặc định. */
    public TaskRecipient() {
    }

    /** Trả mã bản ghi cho JSP hoặc lớp dữ liệu. */
    public long getId() {
        return id;
    }

    /** Gán mã bản ghi vào JavaBean. */
    public void setId(long value) {
        id = value;
    }

    /** Trả mã nhiệm vụ cho JSP hoặc lớp dữ liệu. */
    public long getTaskId() {
        return taskId;
    }

    /** Gán mã nhiệm vụ vào JavaBean. */
    public void setTaskId(long value) {
        taskId = value;
    }

    /** Trả mã người học cho JSP hoặc lớp dữ liệu. */
    public long getUserId() {
        return userId;
    }

    /** Gán mã người học vào JavaBean. */
    public void setUserId(long value) {
        userId = value;
    }

    /** Trả họ tên người học cho JSP hoặc lớp dữ liệu. */
    public String getFullName() {
        return fullName;
    }

    /** Gán họ tên người học vào JavaBean. */
    public void setFullName(String value) {
        fullName = value;
    }

    /** Trả nhiệm vụ được giao cho JSP hoặc lớp dữ liệu. */
    public PracticeTask getTask() {
        return task;
    }

    /** Gán nhiệm vụ được giao vào JavaBean. */
    public void setTask(PracticeTask value) {
        task = value;
    }

    /** Trả vòng hiện tại cho JSP hoặc lớp dữ liệu. */
    public TaskRound getCurrentRound() {
        return currentRound;
    }

    /** Gán vòng hiện tại vào JavaBean. */
    public void setCurrentRound(TaskRound value) {
        currentRound = value;
    }

    /** Trả bài nộp mới nhất cho JSP hoặc lớp dữ liệu. */
    public TaskSubmission getLatestSubmission() {
        return latestSubmission;
    }

    /** Gán bài nộp mới nhất vào JavaBean. */
    public void setLatestSubmission(TaskSubmission value) {
        latestSubmission = value;
    }

    /** Trả số bài đã nộp cho JSP hoặc lớp dữ liệu. */
    public int getSubmissionCount() {
        return submissionCount;
    }

    /** Gán số bài đã nộp vào JavaBean. */
    public void setSubmissionCount(int value) {
        submissionCount = value;
    }

    /** Suy ra trạng thái người học từ bài nộp và lần chấm mới nhất. */
    public String statusLabel(Date now) {
        if (latestSubmission == null) {
            if (task != null && task.isLate(now)) {
                return "Chưa nộp — Quá hạn";
            }
            return "Chưa nộp";
        }
        if (latestSubmission.isWaiting()) {
            return "Chờ chấm";
        }
        TaskReview review = latestSubmission.getCurrentReview();
        if (review == null) {
            return "Chờ chấm";
        }
        return review.getConclusionLabel();
    }

    /** Trả nhãn trạng thái suy ra từ bài nộp cho JSP hoặc lớp dữ liệu. */
    public String getStatusLabel() {
        return statusLabel(new Date());
    }

    /** Kiểm vòng ACTIVE, hạn nộp, bài chờ và số lần nộp. */
    public boolean canSubmit(Date now) {
        return task != null
                && task.canReceive(now)
                && currentRound != null
                && currentRound.isActive()
                && submissionCount < task.getMaxSubmissions()
                && (latestSubmission == null || latestSubmission.isReviewed());
    }

    /** Kiểm người học còn được nộp trong vòng hiện tại để JSP chỉ trình bày. */
    public boolean isCanSubmit() {
        return canSubmit(new Date());
    }

    /** Kiểm vòng còn nhận quiz và chưa chốt lượt để JSP chỉ trình bày. */
    public boolean isCanTakeQuiz() {
        return isCanSubmit() && currentRound.getQuizAttemptId() == 0;
    }

    /** Chỉ trình bày quiz của vòng hiện tại khi vòng còn ACTIVE. */
    public boolean isShowCurrentRoundQuiz() {
        return currentRound != null && currentRound.isActive();
    }

    /** Kiểm mã vòng thuộc người được giao trong nhiệm vụ này. */
    public boolean ownsRound(long roundId) {
        return currentRound != null && currentRound.getId() == roundId;
    }
    /** Chỉ bắt đầu hoặc tiếp tục chẩn đoán trong vòng đang được nộp. */
    public boolean isCanStartDiagnosis() {
        return task != null && task.isRubricB() && canSubmit(new Date());
    }
}
