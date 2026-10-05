package vn.edu.webpro.robotlab.business;

import java.io.Serializable;
import java.util.*;
import java.math.*;
import java.text.*;

/** Dữ kiện hoặc luật nối dây phục vụ JDBC và JSP. */
public class WiringExercise implements Serializable {
    private long id = 0;
    private String code = "";
    private String title = "";
    private String robotId = "";
    private String robotName = "";
    private String objective = "";
    private String scopeText = "";
    private String state = "DRAFT";
    private long createdBy = 0;
    private Date createdAt = null;
    private Date publishedAt = null;
    private ArrayList<WiringTerminal> terminals = new ArrayList<>();
    private ArrayList<WiringRule> rules = new ArrayList<>();

    /** Dữ kiện hoặc luật nối dây phục vụ JDBC và JSP. */
    public WiringExercise() {
    }

    /** Đọc id. */
    public long getId() {
        return id;
    }

    /** Gán id. */
    public void setId(long id) {
        this.id = id;
    }

    /** Đọc code. */
    public String getCode() {
        return code;
    }

    /** Gán code. */
    public void setCode(String code) {
        this.code = code;
    }

    /** Đọc title. */
    public String getTitle() {
        return title;
    }

    /** Gán title. */
    public void setTitle(String title) {
        this.title = title;
    }

    /** Đọc robotId. */
    public String getRobotId() {
        return robotId;
    }

    /** Gán robotId. */
    public void setRobotId(String robotId) {
        this.robotId = robotId;
    }

    /** Đọc robotName. */
    public String getRobotName() {
        return robotName;
    }

    /** Gán robotName. */
    public void setRobotName(String robotName) {
        this.robotName = robotName;
    }

    /** Đọc objective. */
    public String getObjective() {
        return objective;
    }

    /** Gán objective. */
    public void setObjective(String objective) {
        this.objective = objective;
    }

    /** Đọc scopeText. */
    public String getScopeText() {
        return scopeText;
    }

    /** Gán scopeText. */
    public void setScopeText(String scopeText) {
        this.scopeText = scopeText;
    }

    /** Đọc state. */
    public String getState() {
        return state;
    }

    /** Gán state. */
    public void setState(String state) {
        this.state = state;
    }

    /** Đọc createdBy. */
    public long getCreatedBy() {
        return createdBy;
    }

    /** Gán createdBy. */
    public void setCreatedBy(long createdBy) {
        this.createdBy = createdBy;
    }

    /** Đọc createdAt. */
    public Date getCreatedAt() {
        return createdAt;
    }

    /** Gán createdAt. */
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    /** Đọc publishedAt. */
    public Date getPublishedAt() {
        return publishedAt;
    }

    /** Gán publishedAt. */
    public void setPublishedAt(Date publishedAt) {
        this.publishedAt = publishedAt;
    }

    /** Đọc terminals. */
    public ArrayList<WiringTerminal> getTerminals() {
        return terminals;
    }

    /** Gán terminals. */
    public void setTerminals(ArrayList<WiringTerminal> terminals) {
        this.terminals = terminals;
    }

    /** Đọc rules. */
    public ArrayList<WiringRule> getRules() {
        return rules;
    }

    /** Gán rules. */
    public void setRules(ArrayList<WiringRule> rules) {
        this.rules = rules;
    }

    /** Dữ kiện hoặc luật nối dây phục vụ JDBC và JSP. */
    public boolean isDraft() {
        return "DRAFT".equals(state);
    }

    /** Dữ kiện hoặc luật nối dây phục vụ JDBC và JSP. */
    public boolean isPublished() {
        return "PUBLISHED".equals(state);
    }

    /** Dữ kiện hoặc luật nối dây phục vụ JDBC và JSP. */
    public String getStateLabel() {
        if (isDraft()) {
            return "Nháp";
        } else if (isPublished()) {
            return "Đã công bố";
        }
        return "Đã lưu trữ";
    }

    /** Nội dung sau công bố bất biến; muốn sửa phải nhân bản. */
    public void requireDraft() {
        if (!isDraft()) {
            throw new IllegalArgumentException("Bài đã công bố bất biến; hãy nhân bản để sửa.");
        }
    }

    /** Dữ kiện hoặc luật nối dây phục vụ JDBC và JSP. */
    public WiringTerminal terminal(long terminalId) {
        for (WiringTerminal value : terminals) {
            if (value.getId() == terminalId) {
                return value;
            }
        }
        throw new IllegalArgumentException("Đầu nối không thuộc bài này.");
    }

    /** Kiểm robot, nội dung, đầu nối và tính nhất quán của quy tắc nháp. */
    public void validateDraft(boolean robotExists) {
        if (!robotExists || !code.matches("[A-Za-z0-9_.-]{3,64}")
                || title.trim().length() < 5 || title.length() > 150
                || objective.trim().length() < 10 || objective.length() > 2000
                || scopeText.trim().length() < 10 || scopeText.length() > 2000
                || terminals.size() > 40 || rules.size() > 80) {
            throw new IllegalArgumentException("Robot, nội dung hoặc số đầu nối/quy tắc không hợp lệ.");
        }
        HashSet<String> codes = new HashSet<>();
        HashSet<Long> ids = new HashSet<>();
        for (WiringTerminal value : terminals) {
            value.validate();
            if (!codes.add(value.getCode().toLowerCase(Locale.ENGLISH))
                    || value.getId() <= 0 || !ids.add(value.getId())) {
                throw new IllegalArgumentException("Mã đầu nối trong bài không được trùng.");
            }
        }
        HashSet<String> pairs = new HashSet<>();
        int required = 0;
        for (WiringRule rule : rules) {
            rule.normalize();
            terminal(rule.getTerminalA());
            terminal(rule.getTerminalB());
            if ((!rule.isRequired() && !rule.isForbidden()) || !pairs.add(rule.getPairKey())
                    || rule.getExplanation().trim().length() < 10 || rule.getExplanation().length() > 1000) {
                throw new IllegalArgumentException("Quy tắc trùng/xung đột, loại hoặc giải thích không hợp lệ.");
            }
            if (rule.isRequired()) {
                required++;
            }
        }

    }

    /** Công bố cần nháp hợp lệ và ít nhất một cặp bắt buộc. */
    public void validatePublication(boolean robotExists) {
        validateDraft(robotExists);
        boolean required = false;
        for (WiringRule rule : rules) {
            if (rule.isRequired()) {
                required = true;
            }
        }
        if (!required) {
            throw new IllegalArgumentException("Bài phải có ít nhất một cặp bắt buộc.");
        }
    }

    /** Công bố bản bất biến sau khi kiểm lại mọi dữ kiện. */
    public void publish(boolean robotExists) {
        requireDraft();
        validatePublication(robotExists);
        state = "PUBLISHED";
        publishedAt = new Date();
    }

    /** Ngăn lượt mới nhưng giữ nội dung và khả năng tiếp tục lượt cũ. */
    public void archive() {
        if (!isPublished()) {
            throw new IllegalArgumentException("Chỉ lưu trữ bài đang công bố.");
        }
        state = "ARCHIVED";
    }

    /** Sao chép sâu sang nháp mới, không sao chép lượt và kết quả cũ. */
    public WiringExercise copyDraft() {
        WiringExercise copy = new WiringExercise();
        copy.setCode("copy-" + id + "-" + System.currentTimeMillis());
        copy.setTitle(title);
        copy.setRobotId(robotId);
        copy.setRobotName(robotName);
        copy.setObjective(objective);
        copy.setScopeText(scopeText);
        for (WiringTerminal old : terminals) {
            WiringTerminal t = new WiringTerminal();
            t.setId(old.getId());
            t.setCode(old.getCode());
            t.setDeviceCode(old.getDeviceCode());
            t.setDeviceLabel(old.getDeviceLabel());
            t.setPinLabel(old.getPinLabel());
            t.setDisplayOrder(old.getDisplayOrder());
            t.setX(old.getX());
            t.setY(old.getY());
            copy.getTerminals().add(t);
        }
        for (WiringRule old : rules) {
            WiringRule r = new WiringRule();
            r.setTerminalA(old.getTerminalA());
            r.setTerminalB(old.getTerminalB());
            r.setKind(old.getKind());
            r.setExplanation(old.getExplanation());
            copy.getRules().add(r);
        }
        return copy;
    }

    /** Chuẩn hóa cặp không hướng, từ chối tự nối và mã không hợp lệ. */
    public ArrayList<WiringConnection> normalize(ArrayList<WiringConnection> input) {
        if (input == null || input.size() > 80) {
            throw new IllegalArgumentException("Tối đa 80 dây; không lưu một phần request.");
        }
        ArrayList<WiringConnection> result = new ArrayList<>();
        HashSet<String> keys = new HashSet<>();
        for (WiringConnection old : input) {
            WiringConnection value = new WiringConnection();
            value.setTerminalA(old.getTerminalA());
            value.setTerminalB(old.getTerminalB());
            value.normalize();
            value.setFirst(terminal(value.getTerminalA()));
            value.setSecond(terminal(value.getTerminalB()));
            if (keys.add(value.getPairKey())) {
                result.add(value);
            }
        }
        return result;
    }

    /** Chấm cặp trực tiếp và giải thích đúng/sai/thiếu từ quy tắc của bài. */
    public WiringGrade grade(ArrayList<WiringConnection> input) {
        ArrayList<WiringConnection> wires = normalize(input);
        HashMap<String, WiringRule> byPair = new HashMap<>();
        int n = 0;
        for (WiringRule rule : rules) {
            byPair.put(rule.getPairKey(), rule);
            if (rule.isRequired()) {
                n++;
            }
        }
        int c = 0;
        int w = 0;
        HashSet<String> submitted = new HashSet<>();
        WiringGrade grade = new WiringGrade();
        for (WiringConnection wire : wires) {
            submitted.add(wire.getPairKey());
            WiringRule rule = byPair.get(wire.getPairKey());
            if (rule != null && rule.isRequired()) {
                c++;
                wire.setResultLabel("Đúng");
                wire.setExplanation(rule.getExplanation());
            } else {
                w++;
                if (rule != null && rule.isForbidden()) {
                    wire.setForbidden(true);
                    wire.setResultLabel("Cặp bị cấm");
                    wire.setExplanation(rule.getExplanation());
                } else {
                    wire.setResultLabel("Sai / thừa");
                    StringBuilder explanation = new StringBuilder("Hai đầu nối không tạo cặp bắt buộc trong bài.");
                    for (WiringRule related : rules) {
                        if (related.isRequired() && (related.getTerminalA() == wire.getTerminalA()
                                || related.getTerminalB() == wire.getTerminalA()
                                || related.getTerminalA() == wire.getTerminalB()
                                || related.getTerminalB() == wire.getTerminalB())) {
                            explanation.append(" Cặp liên quan: ");
                            explanation.append(terminal(related.getTerminalA()).getLabel());
                            explanation.append(" ↔ ");
                            explanation.append(terminal(related.getTerminalB()).getLabel());
                            explanation.append(".");
                        }
                    }
                    wire.setExplanation(explanation.toString());
                }
            }
            grade.getRows().add(wire);
        }
        for (WiringRule rule : rules) {
            if (rule.isRequired() && !submitted.contains(rule.getPairKey())) {
                WiringConnection missing = new WiringConnection();
                missing.setTerminalA(rule.getTerminalA());
                missing.setTerminalB(rule.getTerminalB());
                missing.setFirst(terminal(rule.getTerminalA()));
                missing.setSecond(terminal(rule.getTerminalB()));
                missing.setResultLabel("Thiếu");
                missing.setExplanation(rule.getExplanation());
                grade.getRows().add(missing);
            }
        }
        grade.calculate(n, c, w);
        return grade;
    }

    /** Sơ đồ tham khảo ADMIN, không đưa đáp án vào lượt DRAFT USER. */
    public ArrayList<WiringConnection> getReferenceConnections() {
        ArrayList<WiringConnection> values = new ArrayList<>();
        for (WiringRule rule : rules) {
            if (rule.isRequired()) {
                WiringConnection w = new WiringConnection();
                w.setTerminalA(rule.getTerminalA());
                w.setTerminalB(rule.getTerminalB());
                values.add(w);
            }
        }
        return normalize(values);
    }

    /** Ánh xạ ID đầu nối sang số ô trong form biên soạn nháp. */
    public ArrayList<WiringRule> getEditRules() {
        ArrayList<WiringRule> values = new ArrayList<>();
        for (WiringRule rule : rules) {
            WiringRule copy = new WiringRule();
            for (int i = 0; i < terminals.size(); i++) {
                if (terminals.get(i).getId() == rule.getTerminalA()) {
                    copy.setTerminalA(i + 1);
                }
                if (terminals.get(i).getId() == rule.getTerminalB()) {
                    copy.setTerminalB(i + 1);
                }
            }
            copy.setKind(rule.getKind());
            copy.setExplanation(rule.getExplanation());
            values.add(copy);
        }
        return values;
    }
}
