package vn.edu.webpro.robotlab.business;
import java.io.Serializable;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;
public class TaskRecipient implements Serializable {
    private long id;
    private long taskId;
    private long userId;
    private String fullName = "";
    private PracticeTask task;
    private TaskRound currentRound;
    private TaskSubmission latestSubmission;
    private int submissionCount;
    public TaskRecipient() {  }
    public long getId() { return id; }
    public void setId(long value) { id = value; }
    public long getTaskId() { return taskId; }
    public void setTaskId(long value) { taskId = value; }
    public long getUserId() { return userId; }
    public void setUserId(long value) { userId = value; }
    public String getFullName() { return fullName; }
    public void setFullName(String value) { fullName = value; }
    public PracticeTask getTask() { return task; }
    public void setTask(PracticeTask value) { task = value; }
    public TaskRound getCurrentRound() { return currentRound; }
    public void setCurrentRound(TaskRound value) { currentRound = value; }
    public TaskSubmission getLatestSubmission() { return latestSubmission; }
    public void setLatestSubmission(TaskSubmission value) { latestSubmission = value; }
    public int getSubmissionCount() { return submissionCount; }
    public void setSubmissionCount(int value) { submissionCount = value; }

    public String statusLabel(Date now) {
        if (latestSubmission == null) {
            if (task != null && task.isLate(now)) return "Chưa nộp — Quá hạn";
            return "Chưa nộp";
        }
        if (latestSubmission.isWaiting()) return "Chờ chấm";
        TaskReview review = latestSubmission.getCurrentReview();
        if (review == null) return "Chờ chấm";
        return review.getConclusionLabel();
    }
    public String getStatusLabel() { return statusLabel(new Date()); }
    public boolean canSubmit(Date now) { return task != null && task.canReceive(now) && currentRound != null
        && currentRound.isActive() && submissionCount < task.getMaxSubmissions()
        && (latestSubmission == null || latestSubmission.isReviewed()); }
    public boolean isCanSubmit() { return canSubmit(new Date()); }
    public boolean isCanTakeQuiz() { return isCanSubmit() && currentRound.getQuizAttemptId() == 0; }
    public boolean ownsRound(long roundId) { return currentRound != null && currentRound.getId() == roundId; }

}
