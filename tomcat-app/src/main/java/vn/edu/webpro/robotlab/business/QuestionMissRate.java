package vn.edu.webpro.robotlab.business;

import java.io.Serializable;

/**
 * Tỷ lệ trả lời sai của một câu hỏi trên toàn hệ thống — dùng ở trang thống kê
 * quản trị để chỉ ra câu hỏi có thể đang khó hiểu hoặc có nội dung chưa rõ ràng.
 */
public class QuestionMissRate implements Serializable {
    private String questionId;
    private String robotId;
    private String prompt;
    private int attemptCount;
    private int missRatePercent;

    public QuestionMissRate() {
        questionId = "";
        robotId = "";
        prompt = "";
    }

    public String getQuestionId() {
        return questionId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    public String getRobotId() {
        return robotId;
    }

    public void setRobotId(String robotId) {
        this.robotId = robotId;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    /** Số lượt câu hỏi này từng được trả lời — mẫu càng nhỏ, tỷ lệ càng ít đáng tin. */
    public int getAttemptCount() {
        return attemptCount;
    }

    public void setAttemptCount(int attemptCount) {
        this.attemptCount = attemptCount;
    }

    public int getMissRatePercent() {
        return missRatePercent;
    }

    public void setMissRatePercent(int missRatePercent) {
        this.missRatePercent = missRatePercent;
    }
}
