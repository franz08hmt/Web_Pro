package vn.edu.webpro.robotlab.business;

import java.io.Serializable;
import java.util.Date;

/** Một lượt làm bài thật của tài khoản, dùng để tính tốt nhất và gần nhất trong JavaBean. */
public class ProfileQuizAttempt implements Serializable {
    private String robotId;
    private long attemptId;
    private int score;
    private int totalQuestions;
    private Date submittedAt;

    public ProfileQuizAttempt() {
        robotId = "";
        submittedAt = null;
    }

    public String getRobotId() {
        return robotId;
    }

    public void setRobotId(String robotId) {
        this.robotId = robotId;
    }

    public long getAttemptId() {
        return attemptId;
    }

    public void setAttemptId(long attemptId) {
        this.attemptId = attemptId;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public Date getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(Date submittedAt) {
        this.submittedAt = submittedAt;
    }
}
