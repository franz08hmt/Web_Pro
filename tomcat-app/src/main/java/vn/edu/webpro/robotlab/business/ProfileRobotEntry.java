package vn.edu.webpro.robotlab.business;

import java.io.Serializable;

/** Kết quả hiển thị của một robot trong hồ sơ học tập cá nhân. */
public class ProfileRobotEntry implements Serializable {
    private Robot robot;
    private String statusKey;
    private String statusLabel;
    private int completedCount;
    private long completionSessionId;
    private String completionDate;
    private String latestSessionStatusLabel;
    private String latestSessionDate;
    private boolean quizDataAvailable;
    private int quizAttemptCount;
    private int bestQuizScore;
    private int bestQuizTotalQuestions;
    private String bestQuizDate;
    private int latestQuizScore;
    private int latestQuizTotalQuestions;
    private String latestQuizDate;

    public ProfileRobotEntry() {
        robot = new Robot();
        statusKey = "NO_DATA";
        statusLabel = "Chưa có dữ liệu";
        completionDate = "";
        latestSessionStatusLabel = "";
        latestSessionDate = "";
        bestQuizDate = "";
        latestQuizDate = "";
    }

    public Robot getRobot() {
        return robot;
    }

    public void setRobot(Robot robot) {
        this.robot = robot;
    }

    public String getStatusKey() {
        return statusKey;
    }

    public void setStatusKey(String statusKey) {
        this.statusKey = statusKey;
    }

    public String getStatusLabel() {
        return statusLabel;
    }

    public void setStatusLabel(String statusLabel) {
        this.statusLabel = statusLabel;
    }

    public int getCompletedCount() {
        return completedCount;
    }

    public void setCompletedCount(int completedCount) {
        this.completedCount = completedCount;
    }

    public long getCompletionSessionId() {
        return completionSessionId;
    }

    public void setCompletionSessionId(long completionSessionId) {
        this.completionSessionId = completionSessionId;
    }

    public String getCompletionDate() {
        return completionDate;
    }

    public void setCompletionDate(String completionDate) {
        this.completionDate = completionDate;
    }

    public String getLatestSessionStatusLabel() {
        return latestSessionStatusLabel;
    }

    public void setLatestSessionStatusLabel(String latestSessionStatusLabel) {
        this.latestSessionStatusLabel = latestSessionStatusLabel;
    }

    public String getLatestSessionDate() {
        return latestSessionDate;
    }

    public void setLatestSessionDate(String latestSessionDate) {
        this.latestSessionDate = latestSessionDate;
    }

    public boolean isQuizDataAvailable() {
        return quizDataAvailable;
    }

    public void setQuizDataAvailable(boolean quizDataAvailable) {
        this.quizDataAvailable = quizDataAvailable;
    }

    public int getQuizAttemptCount() {
        return quizAttemptCount;
    }

    public void setQuizAttemptCount(int quizAttemptCount) {
        this.quizAttemptCount = quizAttemptCount;
    }

    public int getBestQuizScore() {
        return bestQuizScore;
    }

    public void setBestQuizScore(int bestQuizScore) {
        this.bestQuizScore = bestQuizScore;
    }

    public int getBestQuizTotalQuestions() {
        return bestQuizTotalQuestions;
    }

    public void setBestQuizTotalQuestions(int bestQuizTotalQuestions) {
        this.bestQuizTotalQuestions = bestQuizTotalQuestions;
    }

    public String getBestQuizDate() {
        return bestQuizDate;
    }

    public void setBestQuizDate(String bestQuizDate) {
        this.bestQuizDate = bestQuizDate;
    }

    public int getLatestQuizScore() {
        return latestQuizScore;
    }

    public void setLatestQuizScore(int latestQuizScore) {
        this.latestQuizScore = latestQuizScore;
    }

    public int getLatestQuizTotalQuestions() {
        return latestQuizTotalQuestions;
    }

    public void setLatestQuizTotalQuestions(int latestQuizTotalQuestions) {
        this.latestQuizTotalQuestions = latestQuizTotalQuestions;
    }

    public String getLatestQuizDate() {
        return latestQuizDate;
    }

    public void setLatestQuizDate(String latestQuizDate) {
        this.latestQuizDate = latestQuizDate;
    }

    public String getBestQuizDisplay() {
        return quizDataAvailable ? bestQuizScore + "/" + bestQuizTotalQuestions : "Chưa có dữ liệu";
    }

    public String getLatestQuizDisplay() {
        return quizDataAvailable ? latestQuizScore + "/" + latestQuizTotalQuestions : "Chưa có dữ liệu";
    }
}
