package vn.edu.webpro.robotlab.business;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/** Bản chụp bằng chứng và nội dung giải thích khi nộp bài. */
public class TaskSubmission implements Serializable {
    private long id;
    private long recipientId;
    private long roundId;
    private long sessionId;
    private long quizAttemptId;
    private int submissionNo;
    private int quizScore;
    private int quizTotal;
    private int quizReusedFrom;
    private String robotName = "";
    private Date assemblyCompletedAt;
    private Date quizSubmittedAt;
    private Date submittedAt;
    private BigDecimal assemblyPoints = new BigDecimal("40.0");
    private BigDecimal quizPoints = BigDecimal.ZERO;
    private BigDecimal automaticPoints = BigDecimal.ZERO;
    private boolean late;
    private String problem = "";
    private String reasoning = "";
    private String improvement = "";
    private String state = "SUBMITTED";
    private List<TaskReview> reviews = new ArrayList<>();
    private long diagnosisAttemptId;
    private String diagnosisTitle = "";
    private int diagRequiredDone;
    private int diagRequiredTotal;
    private boolean diagCauseCorrect;
    private boolean diagActionCorrect;
    private BigDecimal diagnosisScore = BigDecimal.ZERO;
    private BigDecimal diagnosisPoints = BigDecimal.ZERO;
    private int diagnosisReusedFrom;
    private Date diagnosisSubmittedAt;



    /** Khởi tạo JavaBean với giá trị mặc định. */
    public TaskSubmission() {
    }

    /** Trả mã bản ghi cho JSP hoặc lớp dữ liệu. */
    public long getId() {
        return id;
    }

    /** Gán mã bản ghi vào JavaBean. */
    public void setId(long value) {
        id = value;
    }

    /** Trả mã người được giao cho JSP hoặc lớp dữ liệu. */
    public long getRecipientId() {
        return recipientId;
    }

    /** Gán mã người được giao vào JavaBean. */
    public void setRecipientId(long value) {
        recipientId = value;
    }

    /** Trả mã vòng nộp cho JSP hoặc lớp dữ liệu. */
    public long getRoundId() {
        return roundId;
    }

    /** Gán mã vòng nộp vào JavaBean. */
    public void setRoundId(long value) {
        roundId = value;
    }

    /** Trả mã phiên lắp ráp cho JSP hoặc lớp dữ liệu. */
    public long getSessionId() {
        return sessionId;
    }

    /** Gán mã phiên lắp ráp vào JavaBean. */
    public void setSessionId(long value) {
        sessionId = value;
    }

    /** Trả mã lượt quiz tính điểm cho JSP hoặc lớp dữ liệu. */
    public long getQuizAttemptId() {
        return quizAttemptId;
    }

    /** Gán mã lượt quiz tính điểm vào JavaBean. */
    public void setQuizAttemptId(long value) {
        quizAttemptId = value;
    }

    /** Trả số thứ tự bài nộp cho JSP hoặc lớp dữ liệu. */
    public int getSubmissionNo() {
        return submissionNo;
    }

    /** Gán số thứ tự bài nộp vào JavaBean. */
    public void setSubmissionNo(int value) {
        submissionNo = value;
    }

    /** Trả số câu quiz đúng cho JSP hoặc lớp dữ liệu. */
    public int getQuizScore() {
        return quizScore;
    }

    /** Gán số câu quiz đúng vào JavaBean. */
    public void setQuizScore(int value) {
        quizScore = value;
    }

    /** Trả tổng số câu quiz cho JSP hoặc lớp dữ liệu. */
    public int getQuizTotal() {
        return quizTotal;
    }

    /** Gán tổng số câu quiz vào JavaBean. */
    public void setQuizTotal(int value) {
        quizTotal = value;
    }

    /** Trả lần nộp có quiz được dùng lại cho JSP hoặc lớp dữ liệu. */
    public int getQuizReusedFrom() {
        return quizReusedFrom;
    }

    /** Gán lần nộp có quiz được dùng lại vào JavaBean. */
    public void setQuizReusedFrom(int value) {
        quizReusedFrom = value;
    }

    /** Trả tên mẫu robot cho JSP hoặc lớp dữ liệu. */
    public String getRobotName() {
        return robotName;
    }

    /** Gán tên mẫu robot vào JavaBean. */
    public void setRobotName(String value) {
        robotName = value;
    }

    /** Trả thời điểm hoàn tất lắp ráp cho JSP hoặc lớp dữ liệu. */
    public Date getAssemblyCompletedAt() {
        return assemblyCompletedAt;
    }

    /** Gán thời điểm hoàn tất lắp ráp vào JavaBean. */
    public void setAssemblyCompletedAt(Date value) {
        assemblyCompletedAt = value;
    }

    /** Trả thời điểm nộp quiz cho JSP hoặc lớp dữ liệu. */
    public Date getQuizSubmittedAt() {
        return quizSubmittedAt;
    }

    /** Gán thời điểm nộp quiz vào JavaBean. */
    public void setQuizSubmittedAt(Date value) {
        quizSubmittedAt = value;
    }

    /** Trả thời điểm nộp bài cho JSP hoặc lớp dữ liệu. */
    public Date getSubmittedAt() {
        return submittedAt;
    }

    /** Gán thời điểm nộp bài vào JavaBean. */
    public void setSubmittedAt(Date value) {
        submittedAt = value;
    }

    /** Trả điểm lắp ráp trong bản chụp cho JSP hoặc lớp dữ liệu. */
    public BigDecimal getAssemblyPoints() {
        return assemblyPoints;
    }

    /** Gán điểm lắp ráp trong bản chụp vào JavaBean. */
    public void setAssemblyPoints(BigDecimal value) {
        assemblyPoints = value;
    }

    /** Trả điểm quiz trong bản chụp cho JSP hoặc lớp dữ liệu. */
    public BigDecimal getQuizPoints() {
        return quizPoints;
    }

    /** Gán điểm quiz trong bản chụp vào JavaBean. */
    public void setQuizPoints(BigDecimal value) {
        quizPoints = value;
    }

    /** Trả điểm tự động đã làm tròn cho JSP hoặc lớp dữ liệu. */
    public BigDecimal getAutomaticPoints() {
        return automaticPoints;
    }

    /** Gán điểm tự động đã làm tròn vào JavaBean. */
    public void setAutomaticPoints(BigDecimal value) {
        automaticPoints = value;
    }

    /** Kiểm thời điểm đã quá hạn nộp để JSP chỉ trình bày. */
    public boolean isLate() {
        return late;
    }

    /** Gán cờ nộp muộn vào JavaBean. */
    public void setLate(boolean value) {
        late = value;
    }

    /** Trả nội dung vấn đề gặp phải cho JSP hoặc lớp dữ liệu. */
    public String getProblem() {
        return problem;
    }

    /** Gán nội dung vấn đề gặp phải vào JavaBean. */
    public void setProblem(String value) {
        problem = value;
    }

    /** Trả căn cứ chọn cách xử lý cho JSP hoặc lớp dữ liệu. */
    public String getReasoning() {
        return reasoning;
    }

    /** Gán căn cứ chọn cách xử lý vào JavaBean. */
    public void setReasoning(String value) {
        reasoning = value;
    }

    /** Trả nội dung cải thiện dự kiến cho JSP hoặc lớp dữ liệu. */
    public String getImprovement() {
        return improvement;
    }

    /** Gán nội dung cải thiện dự kiến vào JavaBean. */
    public void setImprovement(String value) {
        improvement = value;
    }

    /** Trả trạng thái cho JSP hoặc lớp dữ liệu. */
    public String getState() {
        return state;
    }

    /** Gán trạng thái vào JavaBean. */
    public void setState(String value) {
        state = value;
    }

    /** Trả lịch sử chấm cho JSP hoặc lớp dữ liệu. */
    public List<TaskReview> getReviews() {
        return reviews;
    }

    /** Gán lịch sử chấm vào JavaBean. */
    public void setReviews(List<TaskReview> value) {
        reviews = value;
    }

    /** Kiểm bài đã được chấm để JSP chỉ trình bày. */
    public boolean isReviewed() {
        return "REVIEWED".equals(state);
    }

    /** Kiểm bài nộp đang chờ chấm để JSP chỉ trình bày. */
    public boolean isWaiting() {
        return "SUBMITTED".equals(state);
    }

    /** Trả lần chấm hiện hành trong lịch sử cho JSP hoặc lớp dữ liệu. */
    public TaskReview getCurrentReview() {
        if (reviews.isEmpty()) {
            return null;
        }
        return reviews.get(reviews.size() - 1);
    }

    /** Trả điểm tự động định dạng vi-VN cho JSP hoặc lớp dữ liệu. */
    public String getAutomaticDisplay() {
        return TaskRubric.formatNumber(automaticPoints);
    }

    /** Trả điểm quiz định dạng vi-VN cho JSP hoặc lớp dữ liệu. */
    public String getQuizPointsDisplay() {
        return TaskRubric.formatNumber(quizPoints);
    }

    /** Trả ngày hoàn tất theo giờ Việt Nam cho JSP hoặc lớp dữ liệu. */
    public String getAssemblyCompletedDisplay() {
        return TaskRubric.formatDate(assemblyCompletedAt);
    }

    /** Trả ngày nộp quiz theo giờ Việt Nam cho JSP hoặc lớp dữ liệu. */
    public String getQuizSubmittedDisplay() {
        return TaskRubric.formatDate(quizSubmittedAt);
    }

    /** Trả ngày nộp bài theo giờ Việt Nam cho JSP hoặc lớp dữ liệu. */
    public String getSubmittedDisplay() {
        return TaskRubric.formatDate(submittedAt);
    }

    /** Kiểm bài nộp dùng lại quiz của lần trước để JSP chỉ trình bày. */
    public boolean isReusedQuiz() {
        return quizReusedFrom > 0;
    }

    /** Kiểm độ dài bắt buộc của ba đoạn giải thích. */
    public void validateExplanation() {
        TaskRubric.requireText(problem, 20, 1500, "Vấn đề gặp phải");
        TaskRubric.requireText(reasoning, 20, 1500, "Lý do kiểm tra/xử lý");
        TaskRubric.requireText(improvement, 20, 1500, "Điều sẽ thay đổi");
    }

    /** Đọc diagnosisAttemptId. */
    public long getDiagnosisAttemptId() {
        return diagnosisAttemptId;
    }

    /** Gán diagnosisAttemptId. */
    public void setDiagnosisAttemptId(long diagnosisAttemptId) {
        this.diagnosisAttemptId = diagnosisAttemptId;
    }


    /** Đọc diagnosisTitle. */
    public String getDiagnosisTitle() {
        return diagnosisTitle;
    }

    /** Gán diagnosisTitle. */
    public void setDiagnosisTitle(String diagnosisTitle) {
        this.diagnosisTitle = diagnosisTitle;
    }


    /** Đọc diagRequiredDone. */
    public int getDiagRequiredDone() {
        return diagRequiredDone;
    }

    /** Gán diagRequiredDone. */
    public void setDiagRequiredDone(int diagRequiredDone) {
        this.diagRequiredDone = diagRequiredDone;
    }


    /** Đọc diagRequiredTotal. */
    public int getDiagRequiredTotal() {
        return diagRequiredTotal;
    }

    /** Gán diagRequiredTotal. */
    public void setDiagRequiredTotal(int diagRequiredTotal) {
        this.diagRequiredTotal = diagRequiredTotal;
    }


    /** Đọc diagCauseCorrect. */
    public boolean isDiagCauseCorrect() {
        return diagCauseCorrect;
    }

    /** Gán diagCauseCorrect. */
    public void setDiagCauseCorrect(boolean diagCauseCorrect) {
        this.diagCauseCorrect = diagCauseCorrect;
    }


    /** Đọc diagActionCorrect. */
    public boolean isDiagActionCorrect() {
        return diagActionCorrect;
    }

    /** Gán diagActionCorrect. */
    public void setDiagActionCorrect(boolean diagActionCorrect) {
        this.diagActionCorrect = diagActionCorrect;
    }


    /** Đọc diagnosisScore. */
    public BigDecimal getDiagnosisScore() {
        return diagnosisScore;
    }

    /** Gán diagnosisScore. */
    public void setDiagnosisScore(BigDecimal diagnosisScore) {
        this.diagnosisScore = diagnosisScore;
    }


    /** Đọc diagnosisPoints. */
    public BigDecimal getDiagnosisPoints() {
        return diagnosisPoints;
    }

    /** Gán diagnosisPoints. */
    public void setDiagnosisPoints(BigDecimal diagnosisPoints) {
        this.diagnosisPoints = diagnosisPoints;
    }


    /** Đọc diagnosisReusedFrom. */
    public int getDiagnosisReusedFrom() {
        return diagnosisReusedFrom;
    }

    /** Gán diagnosisReusedFrom. */
    public void setDiagnosisReusedFrom(int diagnosisReusedFrom) {
        this.diagnosisReusedFrom = diagnosisReusedFrom;
    }


    /** Đọc diagnosisSubmittedAt. */
    public Date getDiagnosisSubmittedAt() {
        return diagnosisSubmittedAt;
    }

    /** Gán diagnosisSubmittedAt. */
    public void setDiagnosisSubmittedAt(Date diagnosisSubmittedAt) {
        this.diagnosisSubmittedAt = diagnosisSubmittedAt;
    }


    /** Bài mẫu B luôn có bản chụp chẩn đoán. */
    public boolean isHasDiagnosis() {
        return diagnosisAttemptId > 0;
    }

    /** Nhận biết bằng chứng dùng lại từ bài trước. */
    public boolean isReusedDiagnosis() {
        return diagnosisReusedFrom > 0;
    }

    /** Tiêu chí của bản chụp bài nộp. */
    public TaskRubric getRubric() {
        TaskRubric rubric = new TaskRubric();
        if (isHasDiagnosis()) {
            rubric.setTemplate("B");
        }
        return rubric;
    }

    /** Điểm chẩn đoán đã lưu trên thang 10. */
    public String getDiagnosisScoreDisplay() {
        return TaskRubric.formatNumber(diagnosisScore);
    }

    /** Điểm đóng góp chẩn đoán đã lưu. */
    public String getDiagnosisPointsDisplay() {
        return TaskRubric.formatNumber(diagnosisPoints);
    }

    /** Thời điểm chốt lượt chẩn đoán bất biến. */
    public String getDiagnosisSubmittedAtDisplay() {
        return TaskRubric.formatDate(diagnosisSubmittedAt);
    }

    /** Điểm lắp ráp đã lưu theo mẫu của bài nộp. */
    public String getAssemblyPointsDisplay() {
        return TaskRubric.formatNumber(assemblyPoints);
    }
}
