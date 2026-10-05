package vn.edu.webpro.robotlab.business;
import java.io.Serializable;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;
public class TaskRound implements Serializable {
    private long id;
    private long recipientId;
    private int roundNo = 1;
    private String state = "ACTIVE";
    private Date startedAt;
    private long quizAttemptId;
    public TaskRound() {  }
    public long getId() { return id; }
    public void setId(long value) { id = value; }
    public long getRecipientId() { return recipientId; }
    public void setRecipientId(long value) { recipientId = value; }
    public int getRoundNo() { return roundNo; }
    public void setRoundNo(int value) { roundNo = value; }
    public String getState() { return state; }
    public void setState(String value) { state = value; }
    public Date getStartedAt() { return startedAt; }
    public void setStartedAt(Date value) { startedAt = value; }
    public long getQuizAttemptId() { return quizAttemptId; }
    public void setQuizAttemptId(long value) { quizAttemptId = value; }

    public boolean isActive() { return "ACTIVE".equals(state); }
    public boolean isSubmitted() { return "SUBMITTED".equals(state); }
    public boolean isCancelled() { return "CANCELLED".equals(state); }
    public boolean isHasQuizAttempt() { return quizAttemptId > 0; }
    public boolean isHasActivity() { return quizAttemptId > 0 || isSubmitted(); }
    public String getStartedDisplay() { return TaskRubric.formatDate(startedAt); }

}
