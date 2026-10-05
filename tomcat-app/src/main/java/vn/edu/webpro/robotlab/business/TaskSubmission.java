package vn.edu.webpro.robotlab.business;
import java.io.Serializable;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;
public class TaskSubmission implements Serializable {
    private long id;
    private long recipientId;
    private long roundId;
    private long sessionId;
    private long quizAttemptId;
    private int submissionNo;
    private int quizScore;
    private int quizTotal;
    private int quizReusedFrom;
    private String robotName = "";
    private Date assemblyCompletedAt;
    private Date quizSubmittedAt;
    private Date submittedAt;
    private BigDecimal assemblyPoints = new BigDecimal("40.0");
    private BigDecimal quizPoints = BigDecimal.ZERO;
    private BigDecimal automaticPoints = BigDecimal.ZERO;
    private boolean late;
    private String problem = "";
    private String reasoning = "";
    private String improvement = "";
    private String state = "SUBMITTED";
    private List<TaskReview> reviews = new ArrayList<>();
    public TaskSubmission() {  }
    public long getId() { return id; }
    public void setId(long value) { id = value; }
    public long getRecipientId() { return recipientId; }
    public void setRecipientId(long value) { recipientId = value; }
    public long getRoundId() { return roundId; }
    public void setRoundId(long value) { roundId = value; }
    public long getSessionId() { return sessionId; }
    public void setSessionId(long value) { sessionId = value; }
    public long getQuizAttemptId() { return quizAttemptId; }
    public void setQuizAttemptId(long value) { quizAttemptId = value; }
    public int getSubmissionNo() { return submissionNo; }
    public void setSubmissionNo(int value) { submissionNo = value; }
    public int getQuizScore() { return quizScore; }
    public void setQuizScore(int value) { quizScore = value; }
    public int getQuizTotal() { return quizTotal; }
    public void setQuizTotal(int value) { quizTotal = value; }
    public int getQuizReusedFrom() { return quizReusedFrom; }
    public void setQuizReusedFrom(int value) { quizReusedFrom = value; }
    public String getRobotName() { return robotName; }
    public void setRobotName(String value) { robotName = value; }
    public Date getAssemblyCompletedAt() { return assemblyCompletedAt; }
    public void setAssemblyCompletedAt(Date value) { assemblyCompletedAt = value; }
    public Date getQuizSubmittedAt() { return quizSubmittedAt; }
    public void setQuizSubmittedAt(Date value) { quizSubmittedAt = value; }
    public Date getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(Date value) { submittedAt = value; }
    public BigDecimal getAssemblyPoints() { return assemblyPoints; }
    public void setAssemblyPoints(BigDecimal value) { assemblyPoints = value; }
    public BigDecimal getQuizPoints() { return quizPoints; }
    public void setQuizPoints(BigDecimal value) { quizPoints = value; }
    public BigDecimal getAutomaticPoints() { return automaticPoints; }
    public void setAutomaticPoints(BigDecimal value) { automaticPoints = value; }
    public boolean isLate() { return late; }
    public void setLate(boolean value) { late = value; }
    public String getProblem() { return problem; }
    public void setProblem(String value) { problem = value; }
    public String getReasoning() { return reasoning; }
    public void setReasoning(String value) { reasoning = value; }
    public String getImprovement() { return improvement; }
    public void setImprovement(String value) { improvement = value; }
    public String getState() { return state; }
    public void setState(String value) { state = value; }
    public List<TaskReview> getReviews() { return reviews; }
    public void setReviews(List<TaskReview> value) { reviews = value; }

    public boolean isReviewed() { return "REVIEWED".equals(state); }
    public boolean isWaiting() { return "SUBMITTED".equals(state); }
    public TaskReview getCurrentReview() { if (reviews.isEmpty()) return null; return reviews.get(reviews.size() - 1); }
    public String getAutomaticDisplay() { return TaskRubric.formatNumber(automaticPoints); }
    public String getQuizPointsDisplay() { return TaskRubric.formatNumber(quizPoints); }
    public String getAssemblyCompletedDisplay() { return TaskRubric.formatDate(assemblyCompletedAt); }
    public String getQuizSubmittedDisplay() { return TaskRubric.formatDate(quizSubmittedAt); }
    public String getSubmittedDisplay() { return TaskRubric.formatDate(submittedAt); }
    public boolean isReusedQuiz() { return quizReusedFrom > 0; }
    public void validateExplanation() {
        TaskRubric.requireText(problem, 20, 1500, "Vấn đề gặp phải");
        TaskRubric.requireText(reasoning, 20, 1500, "Lý do kiểm tra/xử lý");
        TaskRubric.requireText(improvement, 20, 1500, "Điều sẽ thay đổi");
    }

}
