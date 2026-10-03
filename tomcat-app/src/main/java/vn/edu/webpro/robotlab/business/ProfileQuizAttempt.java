package vn.edu.webpro.robotlab.business;

import java.io.Serializable;

/** Một lượt làm bài thật của tài khoản, dùng để tính tốt nhất và gần nhất trong JavaBean. */
public class ProfileQuizAttempt implements Serializable {
    private String robotId;
    private long attemptId;
    private int score;
    private int totalQuestions;
    private String submittedAtUtc;

    public ProfileQuizAttempt() {
        robotId = "";
        submittedAtUtc = "";
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

    public String getSubmittedAtUtc() {
        return submittedAtUtc;
    }

    public void setSubmittedAtUtc(String submittedAtUtc) {
        this.submittedAtUtc = submittedAtUtc;
    }
}
