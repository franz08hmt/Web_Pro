package vn.edu.webpro.robotlab.business;

import java.io.Serializable;

/** Dữ kiện quiz gộp theo mẫu đã lắp; JavaBean tổng kết quyết định có gợi ý hay không. */
public class DashboardQuizCandidate implements Serializable {
    private String robotId = "";
    private String robotName = "";
    private int questionCount;
    private int attemptCount;

    /** Tạo dữ kiện rỗng theo quy ước JavaBean. */
    public DashboardQuizCandidate() {
    }

    /** Đọc mã mẫu cho liên kết đúng ngữ cảnh. */
    public String getRobotId() { return robotId; }
    /** Ghi mã mẫu từ JDBC. */
    public void setRobotId(String robotId) { this.robotId = robotId; }
    /** Đọc tên mẫu. */
    public String getRobotName() { return robotName; }
    /** Ghi tên mẫu. */
    public void setRobotName(String robotName) { this.robotName = robotName; }
    /** Đọc số câu hỏi hiện có. */
    public int getQuestionCount() { return questionCount; }
    /** Ghi số câu hỏi hiện có. */
    public void setQuestionCount(int questionCount) { this.questionCount = questionCount; }
    /** Đọc số lượt đã làm của chính tài khoản. */
    public int getAttemptCount() { return attemptCount; }
    /** Ghi số lượt đã làm. */
    public void setAttemptCount(int attemptCount) { this.attemptCount = attemptCount; }
}
