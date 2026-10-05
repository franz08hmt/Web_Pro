package vn.edu.webpro.robotlab.business;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/** Vòng nộp với một lượt quiz tính điểm được chốt. */
public class TaskRound implements Serializable {
    private long id;
    private long recipientId;
    private int roundNo = 1;
    private String state = "ACTIVE";
    private Date startedAt;
    private long quizAttemptId;
    private long diagnosisAttemptId;



    /** Khởi tạo JavaBean với giá trị mặc định. */
    public TaskRound() {
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

    /** Trả số thứ tự vòng cho JSP hoặc lớp dữ liệu. */
    public int getRoundNo() {
        return roundNo;
    }

    /** Gán số thứ tự vòng vào JavaBean. */
    public void setRoundNo(int value) {
        roundNo = value;
    }

    /** Trả trạng thái cho JSP hoặc lớp dữ liệu. */
    public String getState() {
        return state;
    }

    /** Gán trạng thái vào JavaBean. */
    public void setState(String value) {
        state = value;
    }

    /** Trả thời điểm bắt đầu vòng cho JSP hoặc lớp dữ liệu. */
    public Date getStartedAt() {
        return startedAt;
    }

    /** Gán thời điểm bắt đầu vòng vào JavaBean. */
    public void setStartedAt(Date value) {
        startedAt = value;
    }

    /** Trả mã lượt quiz tính điểm cho JSP hoặc lớp dữ liệu. */
    public long getQuizAttemptId() {
        return quizAttemptId;
    }

    /** Gán mã lượt quiz tính điểm vào JavaBean. */
    public void setQuizAttemptId(long value) {
        quizAttemptId = value;
    }

    /** Kiểm vòng đang nhận hoạt động để JSP chỉ trình bày. */
    public boolean isActive() {
        return "ACTIVE".equals(state);
    }

    /** Kiểm vòng đã nộp bài để JSP chỉ trình bày. */
    public boolean isSubmitted() {
        return "SUBMITTED".equals(state);
    }

    /** Kiểm vòng đã bị huỷ để JSP chỉ trình bày. */
    public boolean isCancelled() {
        return "CANCELLED".equals(state);
    }

    /** Kiểm vòng đã có lượt quiz tính điểm để JSP chỉ trình bày. */
    public boolean isHasQuizAttempt() {
        return quizAttemptId > 0;
    }

    /** Kiểm vòng đã có quiz hoặc bài nộp để JSP chỉ trình bày. */
    public boolean isHasActivity() {
        return quizAttemptId > 0 || diagnosisAttemptId > 0 || isSubmitted();
    }

    /** Trả ngày bắt đầu vòng theo giờ Việt Nam cho JSP hoặc lớp dữ liệu. */
    public String getStartedDisplay() {
        return TaskRubric.formatDate(startedAt);
    }

    /** Đọc diagnosisAttemptId. */
    public long getDiagnosisAttemptId() {
        return diagnosisAttemptId;
    }

    /** Gán diagnosisAttemptId. */
    public void setDiagnosisAttemptId(long diagnosisAttemptId) {
        this.diagnosisAttemptId = diagnosisAttemptId;
    }

    /** Nhận biết lượt chẩn đoán đã được gắn ngay khi bắt đầu. */
    public boolean isHasDiagnosisAttempt() {
        return diagnosisAttemptId > 0;
    }
}
