package vn.edu.webpro.robotlab.business;

import java.io.Serializable;

/** Kết quả kiểm tra tổng hợp của toàn hệ thống cho một mẫu robot — dùng ở trang thống kê quản trị. */
public class QuizRobotAggregate implements Serializable {
    private String robotId;
    private String robotName;
    private long attemptCount;
    private int averageScorePercent;

    public QuizRobotAggregate() {
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

    public long getAttemptCount() {
        return attemptCount;
    }

    public void setAttemptCount(long attemptCount) {
        this.attemptCount = attemptCount;
    }

    public int getAverageScorePercent() {
        return averageScorePercent;
    }

    public void setAverageScorePercent(int averageScorePercent) {
        this.averageScorePercent = averageScorePercent;
    }
}
