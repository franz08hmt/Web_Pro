package vn.edu.webpro.robotlab.business;

import java.io.Serializable;
import vn.edu.webpro.robotlab.util.JsonUtil;

/**
 * Một câu trả lời đã chấm, lưu dưới dạng BẢN CHỤP tại thời điểm nộp bài.
 *
 * Các trường *Snapshot lấy nguyên văn nội dung câu hỏi/lựa chọn lúc chấm, nên
 * admin sửa quiz_questions/quiz_options sau đó không làm đổi kết quả cũ.
 */
public class QuizAttemptAnswer implements Serializable {
    private String questionId;
    private String questionPromptSnapshot;
    private String selectedOptionId;
    private String selectedOptionLabelSnapshot;
    private String correctOptionLabelSnapshot;
    private boolean correct;
    private String explanationSnapshot;

    public QuizAttemptAnswer() {
        questionId = "";
        questionPromptSnapshot = "";
        selectedOptionId = "";
        selectedOptionLabelSnapshot = "";
        correctOptionLabelSnapshot = "";
        explanationSnapshot = "";
    }

    public String getQuestionId() {
        return questionId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    public String getQuestionPromptSnapshot() {
        return questionPromptSnapshot;
    }

    public void setQuestionPromptSnapshot(String questionPromptSnapshot) {
        this.questionPromptSnapshot = questionPromptSnapshot;
    }

    public String getSelectedOptionId() {
        return selectedOptionId;
    }

    public void setSelectedOptionId(String selectedOptionId) {
        this.selectedOptionId = selectedOptionId;
    }

    public String getSelectedOptionLabelSnapshot() {
        return selectedOptionLabelSnapshot;
    }

    public void setSelectedOptionLabelSnapshot(String selectedOptionLabelSnapshot) {
        this.selectedOptionLabelSnapshot = selectedOptionLabelSnapshot;
    }

    public String getCorrectOptionLabelSnapshot() {
        return correctOptionLabelSnapshot;
    }

    public void setCorrectOptionLabelSnapshot(String correctOptionLabelSnapshot) {
        this.correctOptionLabelSnapshot = correctOptionLabelSnapshot;
    }

    public boolean isCorrect() {
        return correct;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
    }

    public String getExplanationSnapshot() {
        return explanationSnapshot;
    }

    public void setExplanationSnapshot(String explanationSnapshot) {
        this.explanationSnapshot = explanationSnapshot;
    }

    public String toJson() {
        return "{\"questionId\":" + JsonUtil.quote(questionId)
                + ",\"questionPrompt\":" + JsonUtil.quote(questionPromptSnapshot)
                + ",\"selectedOptionId\":" + JsonUtil.quote(selectedOptionId)
                + ",\"selectedOptionLabel\":" + JsonUtil.quote(selectedOptionLabelSnapshot)
                + ",\"correctOptionLabel\":" + JsonUtil.quote(correctOptionLabelSnapshot)
                + ",\"isCorrect\":" + correct
                + ",\"explanation\":" + JsonUtil.quote(explanationSnapshot) + "}";
    }
}
