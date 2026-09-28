package vn.edu.webpro.robotlab.business;

import java.io.Serializable;

/** Điểm kiểm tra tốt nhất/gần nhất của một tài khoản cho một mẫu robot — dùng ở trang tổng kết học tập. */
public class QuizRobotScore implements Serializable {
    private String robotId;
    private String robotName;
    private int bestScore;
    private int latestScore;
    private int totalQuestions;
    private int attemptCount;

    public QuizRobotScore() {
        robotId = "";
        robotName = "";
    }

    public String getRobotId() {
        return robotId;
    }

    public void setRobotId(String robotId) {
        this.robotId = robotId;
    }

    public String getRobotName() {
        return robotName;
    }

    public void setRobotName(String robotName) {
        this.robotName = robotName;
    }

    public int getBestScore() {
        return bestScore;
    }

    public void setBestScore(int bestScore) {
        this.bestScore = bestScore;
    }

    public int getLatestScore() {
        return latestScore;
    }

    public void setLatestScore(int latestScore) {
        this.latestScore = latestScore;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public int getAttemptCount() {
        return attemptCount;
    }

    public void setAttemptCount(int attemptCount) {
        this.attemptCount = attemptCount;
    }
}
