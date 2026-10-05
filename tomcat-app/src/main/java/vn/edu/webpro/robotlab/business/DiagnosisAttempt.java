package vn.edu.webpro.robotlab.business;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.math.BigDecimal;
import java.math.RoundingMode;

/** Dữ kiện và luật nghiệp vụ của DiagnosisAttempt. */
public class DiagnosisAttempt implements Serializable {
    private long id;
    private long scenarioId;
    private long userId;
    private String mode = "PRACTICE";
    private String state = "IN_PROGRESS";
    private Date startedAt;
    private Date submittedAt;
    private long causeOptionId;
    private long actionOptionId;
    private int requiredDone;
    private int requiredTotal;
    private boolean causeCorrect;
    private boolean actionCorrect;
    private DiagnosisScenario scenario;

    /** Khởi tạo JavaBean. */
    public DiagnosisAttempt() {
    }

    /** Chốt lựa chọn và điểm từ nhật ký server, không nhận danh sách đã kiểm tra từ form. */
    public void conclude(long causeId, long actionId) {
        if (!canConclude() || scenario == null) {
            throw new IllegalArgumentException("Lượt đã chốt hoặc không hợp lệ.");
        }
        DiagnosisOption cause = null;
        DiagnosisOption action = null;
        for (DiagnosisOption option : scenario.getOptions()) {
            if (option.getId() == causeId && option.isCause()) {
                cause = option;
            }
            if (option.getId() == actionId && option.isAction()) {
                action = option;
            }
        }
        if (cause == null || action == null) {
            throw new IllegalArgumentException("Lựa chọn không thuộc đúng nhóm của tình huống.");
        }
        requiredDone = 0;
        requiredTotal = 0;
        for (DiagnosisCheck check : scenario.getChecks()) {
            if (check.isRequired()) {
                requiredTotal++;
                if (check.isChosen()) {
                    requiredDone++;
                }
            }
        }
        causeOptionId = causeId;
        actionOptionId = actionId;
        causeCorrect = cause.isCorrect();
        actionCorrect = action.isCorrect();
        getScoreExact();
        state = "SUBMITTED";
    }

    /** Đọc id. */
    public long getId() {
        return id;
    }

    /** Gán id. */
    public void setId(long id) {
        this.id = id;
    }

    /** Đọc scenarioId. */
    public long getScenarioId() {
        return scenarioId;
    }

    /** Gán scenarioId. */
    public void setScenarioId(long scenarioId) {
        this.scenarioId = scenarioId;
    }

    /** Đọc userId. */
    public long getUserId() {
        return userId;
    }

    /** Gán userId. */
    public void setUserId(long userId) {
        this.userId = userId;
    }

    /** Đọc mode. */
    public String getMode() {
        return mode;
    }

    /** Gán mode. */
    public void setMode(String mode) {
        this.mode = mode;
    }

    /** Đọc state. */
    public String getState() {
        return state;
    }

    /** Gán state. */
    public void setState(String state) {
        this.state = state;
    }

    /** Đọc startedAt. */
    public Date getStartedAt() {
        return startedAt;
    }

    /** Gán startedAt. */
    public void setStartedAt(Date startedAt) {
        this.startedAt = startedAt;
    }

    /** Đọc submittedAt. */
    public Date getSubmittedAt() {
        return submittedAt;
    }

    /** Gán submittedAt. */
    public void setSubmittedAt(Date submittedAt) {
        this.submittedAt = submittedAt;
    }

    /** Đọc causeOptionId. */
    public long getCauseOptionId() {
        return causeOptionId;
    }

    /** Gán causeOptionId. */
    public void setCauseOptionId(long causeOptionId) {
        this.causeOptionId = causeOptionId;
    }

    /** Đọc actionOptionId. */
    public long getActionOptionId() {
        return actionOptionId;
    }

    /** Gán actionOptionId. */
    public void setActionOptionId(long actionOptionId) {
        this.actionOptionId = actionOptionId;
    }

    /** Đọc requiredDone. */
    public int getRequiredDone() {
        return requiredDone;
    }

    /** Gán requiredDone. */
    public void setRequiredDone(int requiredDone) {
        this.requiredDone = requiredDone;
    }

    /** Đọc requiredTotal. */
    public int getRequiredTotal() {
        return requiredTotal;
    }

    /** Gán requiredTotal. */
    public void setRequiredTotal(int requiredTotal) {
        this.requiredTotal = requiredTotal;
    }

    /** Đọc causeCorrect. */
    public boolean isCauseCorrect() {
        return causeCorrect;
    }

    /** Gán causeCorrect. */
    public void setCauseCorrect(boolean causeCorrect) {
        this.causeCorrect = causeCorrect;
    }

    /** Đọc actionCorrect. */
    public boolean isActionCorrect() {
        return actionCorrect;
    }

    /** Gán actionCorrect. */
    public void setActionCorrect(boolean actionCorrect) {
        this.actionCorrect = actionCorrect;
    }

    /** Đọc scenario. */
    public DiagnosisScenario getScenario() {
        return scenario;
    }

    /** Gán scenario. */
    public void setScenario(DiagnosisScenario scenario) {
        this.scenario = scenario;
    }

    /** Lượt còn nhận thao tác kết luận. */
    public boolean canConclude() {
        return "IN_PROGRESS".equals(state);
    }

    /** Thuộc tính trình bày lượt đang làm. */
    public boolean isInProgress() {
        return canConclude();
    }

    /** Lượt đã chốt bất biến. */
    public boolean isSubmitted() {
        return "SUBMITTED".equals(state);
    }

    /** Lượt luyện tập không bao giờ thành bằng chứng nhiệm vụ. */
    public boolean isPractice() {
        return "PRACTICE".equals(mode);
    }

    /** Lượt gắn với nhiệm vụ. */
    public boolean isTask() {
        return "TASK".equals(mode);
    }

    /** Tính điểm quá trình với độ chính xác giữ đến bước làm tròn cuối. */
    public BigDecimal getScoreExact() {
        return getCollectionPoints().add(new BigDecimal(getCausePoints())).add(new BigDecimal(getActionPoints()));
    }

    /** Điểm thu thập đủ quan sát cần thiết, tối đa 4; không cộng phép không liên quan. */
    public BigDecimal getCollectionPoints() {
        if (requiredTotal <= 0 || requiredDone < 0 || requiredDone > requiredTotal) {
            throw new IllegalArgumentException("Số phép kiểm tra cần thiết không hợp lệ.");
        }
        // BigDecimal tránh sai số nhị phân; chỉ làm tròn HALF_UP khi hiển thị/lưu tổng cuối.
        return new BigDecimal(requiredDone).multiply(new BigDecimal("4"))
                .divide(new BigDecimal(requiredTotal), 24, RoundingMode.HALF_UP);
    }

    /** Điểm xác định nguyên nhân, chỉ 0 hoặc 3. */
    public int getCausePoints() {
        if (causeCorrect) {
            return 3;
        }
        return 0;
    }

    /** Điểm chọn cách xử lý, chỉ 0 hoặc 3. */
    public int getActionPoints() {
        if (actionCorrect) {
            return 3;
        }
        return 0;
    }

    /** Điểm quá trình đã định dạng, JSP chỉ trình bày. */
    public String getCollectionPointsDisplay() {
        return TaskRubric.formatNumber(getCollectionPoints());
    }

    /** Điểm chẩn đoán hiển thị trên thang 10. */
    public String getScoreDisplay() {
        return TaskRubric.formatNumber(getScoreExact().setScale(1, RoundingMode.HALF_UP));
    }

    /** Thời điểm chốt theo giờ Việt Nam. */
    public String getSubmittedAtDisplay() {
        return TaskRubric.formatDate(submittedAt);
    }

    /** Bằng chứng chỉ nhận đúng người, tình huống, chế độ và trạng thái. */
    public boolean validTaskEvidence(long owner, long expectedScenario) {
        return userId == owner && scenarioId == expectedScenario && isTask() && isSubmitted();
    }

    /** Danh sách phép cần thiết chưa chọn, chỉ trả sau khi chốt. */
    public List<DiagnosisCheck> getMissingChecks() {
        List<DiagnosisCheck> list = new ArrayList<>();
        if (isSubmitted() && scenario != null) {
            for (DiagnosisCheck check : scenario.getChecks()) {
                if (check.isRequired() && !check.isChosen()) {
                    list.add(check);
                }
            }
        }
        return list;
    }

    /** Lựa chọn nguyên nhân đã chốt. */
    public DiagnosisOption getChosenCause() {
        return chosenOption(causeOptionId);
    }

    /** Lựa chọn biện pháp đã chốt. */
    public DiagnosisOption getChosenAction() {
        return chosenOption(actionOptionId);
    }

    private DiagnosisOption chosenOption(long optionId) {
        if (isSubmitted() && scenario != null) {
            for (DiagnosisOption option : scenario.getOptions()) {
                if (option.getId() == optionId) {
                    return option;
                }
            }
        }
        return null;
    }

}
