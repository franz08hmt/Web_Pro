package vn.edu.webpro.robotlab.business;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import vn.edu.webpro.robotlab.util.JsonUtil;

/**
 * Một câu hỏi trắc nghiệm gắn với một mẫu robot, kèm danh sách lựa chọn.
 *
 * Tự chấm được câu trả lời của chính nó (isCorrectOption) và tự tìm lựa chọn
 * theo ID (findOption) — servlet/DB chỉ hỏi bean, không tự so sánh chuỗi, cùng
 * tinh thần với AssemblySession.canCompleteAssembly().
 */
public class QuizQuestion implements Serializable {
    private String id;
    private String robotId;
    private String prompt;
    private String explanation;
    private int questionOrder;
    private List<QuizOption> options;

    public QuizQuestion() {
        id = "";
        robotId = "";
        prompt = "";
        explanation = "";
        options = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public int getQuestionOrder() {
        return questionOrder;
    }

    public void setQuestionOrder(int questionOrder) {
        this.questionOrder = questionOrder;
    }

    public List<QuizOption> getOptions() {
        return options;
    }

    public void setOptions(List<QuizOption> options) {
        this.options = options;
    }

    /** Lựa chọn theo ID nếu thuộc câu hỏi này, ngược lại null (chặn optionId của câu khác). */
    public QuizOption findOption(String optionId) {
        for (QuizOption option : options) {
            if (option.getId().equals(optionId)) return option;
        }
        return null;
    }

    public QuizOption getCorrectOption() {
        for (QuizOption option : options) {
            if (option.isCorrect()) return option;
        }
        return null;
    }

    /** Chấm một lựa chọn đã chọn: đúng khi optionId thuộc câu này và được đánh dấu is_correct. */
    public boolean isCorrectOption(String optionId) {
        QuizOption option = findOption(optionId);
        return option != null && option.isCorrect();
    }

    /** Dùng cho trang quản trị: đủ cả lựa chọn đúng và giải thích. */
    public String toJson() {
        StringBuilder optionsJson = new StringBuilder();
        for (QuizOption option : options) {
            if (optionsJson.length() > 0) optionsJson.append(',');
            optionsJson.append(option.toJson());
        }
        return "{\"id\":" + JsonUtil.quote(id)
                + ",\"robotId\":" + JsonUtil.quote(robotId)
                + ",\"prompt\":" + JsonUtil.quote(prompt)
                + ",\"explanation\":" + JsonUtil.quote(explanation)
                + ",\"questionOrder\":" + questionOrder
                + ",\"options\":[" + optionsJson + "]}";
    }

    /** Dùng cho trang làm bài trước khi nộp: không có giải thích, lựa chọn không lộ đáp án đúng. */
    public String toPublicJson() {
        StringBuilder optionsJson = new StringBuilder();
        for (QuizOption option : options) {
            if (optionsJson.length() > 0) optionsJson.append(',');
            optionsJson.append(option.toPublicJson());
        }
        return "{\"id\":" + JsonUtil.quote(id)
                + ",\"prompt\":" + JsonUtil.quote(prompt)
                + ",\"options\":[" + optionsJson + "]}";
    }
}
