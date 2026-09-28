package vn.edu.webpro.robotlab.business;

import java.io.Serializable;
import vn.edu.webpro.robotlab.util.JsonUtil;

/** Một lựa chọn của một câu hỏi trắc nghiệm — JavaBean theo Chapter 6 slide 6. */
public class QuizOption implements Serializable {
    private String id;
    private String questionId;
    private String label;
    private boolean correct;
    private int optionOrder;

    public QuizOption() {
        id = "";
        questionId = "";
        label = "";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getQuestionId() {
        return questionId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean isCorrect() {
        return correct;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
    }

    public int getOptionOrder() {
        return optionOrder;
    }

    public void setOptionOrder(int optionOrder) {
        this.optionOrder = optionOrder;
    }

    /** Dùng cho trang quản trị: có kèm isCorrect để admin biết đáp án đúng. */
    public String toJson() {
        return "{\"id\":" + JsonUtil.quote(id)
                + ",\"questionId\":" + JsonUtil.quote(questionId)
                + ",\"label\":" + JsonUtil.quote(label)
                + ",\"isCorrect\":" + correct
                + ",\"optionOrder\":" + optionOrder + "}";
    }

    /** Dùng cho trang làm bài trước khi nộp: không lộ đáp án đúng. */
    public String toPublicJson() {
        return "{\"id\":" + JsonUtil.quote(id)
                + ",\"label\":" + JsonUtil.quote(label) + "}";
    }
}
