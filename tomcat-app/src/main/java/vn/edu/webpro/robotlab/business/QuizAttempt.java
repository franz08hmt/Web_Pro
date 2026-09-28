package vn.edu.webpro.robotlab.business;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import vn.edu.webpro.robotlab.util.JsonUtil;

/** Một lượt làm bài kiểm tra đã nộp và đã được chấm — JavaBean theo Chapter 6 slide 6. */
public class QuizAttempt implements Serializable {
    private long id;
    private long userId;
    private String robotId;
    private int score;
    private int totalQuestions;
    private String submittedAt;
    private List<QuizAttemptAnswer> answers;

    public QuizAttempt() {
        robotId = "";
        submittedAt = "";
        answers = new ArrayList<>();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getRobotId() {
        return robotId;
    }

    public void setRobotId(String robotId) {
        this.robotId = robotId;
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

    public String getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(String submittedAt) {
        this.submittedAt = submittedAt;
    }

    public List<QuizAttemptAnswer> getAnswers() {
        return answers;
    }

    public void setAnswers(List<QuizAttemptAnswer> answers) {
        this.answers = answers;
    }

    /** Dùng cho danh sách lịch sử: không kèm từng câu trả lời để phản hồi gọn. */
    public String toSummaryJson() {
        return "{\"id\":" + JsonUtil.quote(Long.toString(id))
                + ",\"robotId\":" + JsonUtil.quote(robotId)
                + ",\"score\":" + score
                + ",\"totalQuestions\":" + totalQuestions
                + ",\"submittedAt\":" + JsonUtil.quote(submittedAt) + "}";
    }

    /** Dùng cho trang xem một lượt làm bài: kèm đủ từng câu trả lời và giải thích. */
    public String toJson() {
        StringBuilder answersJson = new StringBuilder();
        for (QuizAttemptAnswer answer : answers) {
            if (answersJson.length() > 0) answersJson.append(',');
            answersJson.append(answer.toJson());
        }
        return "{\"id\":" + JsonUtil.quote(Long.toString(id))
                + ",\"robotId\":" + JsonUtil.quote(robotId)
                + ",\"score\":" + score
                + ",\"totalQuestions\":" + totalQuestions
                + ",\"submittedAt\":" + JsonUtil.quote(submittedAt)
                + ",\"answers\":[" + answersJson + "]}";
    }
}
