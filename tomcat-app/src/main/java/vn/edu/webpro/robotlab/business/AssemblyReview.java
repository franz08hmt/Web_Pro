package vn.edu.webpro.robotlab.business;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.TimeZone;

/** Dữ kiện và quy tắc trình bày của AssemblyReview. */
public class AssemblyReview implements Serializable {
    private Robot robot = new Robot();
    private AssemblySession session = new AssemblySession();
    private List<AssemblyEvent> events = new ArrayList<>();
    private List<AssemblyEvent> activeEvents = new ArrayList<>();
    private List<AssemblyEvent> rejectionSummary = new ArrayList<>();
    private List<QuizAttempt> quizAttempts = new ArrayList<>();
    private boolean oldJournal;
    private boolean hasQuiz;
    private boolean hasQuizQuestions;
    private int correctCount;
    private int requiredCount;
    private int bestQuizCorrect;
    private int latestQuizCorrect;
    private int bestQuizTotal;
    private int latestQuizTotal;

    /** Tạo JavaBean rỗng theo quy ước môn học. */
    public AssemblyReview() {
    }

    /** Đọc dữ kiện robot. */
    public Robot getRobot() {
        return robot;
    }

    /** Ghi dữ kiện robot. */
    public void setRobot(Robot robot) {
        this.robot = robot;
    }

    /** Đọc dữ kiện session. */
    public AssemblySession getSession() {
        return session;
    }

    /** Ghi dữ kiện session. */
    public void setSession(AssemblySession session) {
        this.session = session;
    }

    /** Đọc dữ kiện events. */
    public List<AssemblyEvent> getEvents() {
        return events;
    }

    /** Ghi dữ kiện events. */
    public void setEvents(List<AssemblyEvent> events) {
        this.events = events;
    }

    /** Đọc dữ kiện activeEvents. */
    public List<AssemblyEvent> getActiveEvents() {
        return activeEvents;
    }

    /** Ghi dữ kiện activeEvents. */
    public void setActiveEvents(List<AssemblyEvent> activeEvents) {
        this.activeEvents = activeEvents;
    }

    /** Đọc dữ kiện rejectionSummary. */
    public List<AssemblyEvent> getRejectionSummary() {
        return rejectionSummary;
    }

    /** Ghi dữ kiện rejectionSummary. */
    public void setRejectionSummary(List<AssemblyEvent> rejectionSummary) {
        this.rejectionSummary = rejectionSummary;
    }

    /** Đọc dữ kiện quizAttempts. */
    public List<QuizAttempt> getQuizAttempts() {
        return quizAttempts;
    }

    /** Ghi dữ kiện quizAttempts. */
    public void setQuizAttempts(List<QuizAttempt> quizAttempts) {
        this.quizAttempts = quizAttempts;
    }

    /** Đọc dữ kiện oldJournal. */
    public boolean isOldJournal() {
        return oldJournal;
    }

    /** Ghi dữ kiện oldJournal. */
    public void setOldJournal(boolean oldJournal) {
        this.oldJournal = oldJournal;
    }

    /** Đọc dữ kiện hasQuiz. */
    public boolean isHasQuiz() {
        return hasQuiz;
    }

    /** Ghi dữ kiện hasQuiz. */
    public void setHasQuiz(boolean hasQuiz) {
        this.hasQuiz = hasQuiz;
    }

    /** Đọc dữ kiện hasQuizQuestions. */
    public boolean isHasQuizQuestions() {
        return hasQuizQuestions;
    }

    /** Ghi dữ kiện hasQuizQuestions. */
    public void setHasQuizQuestions(boolean hasQuizQuestions) {
        this.hasQuizQuestions = hasQuizQuestions;
    }

    /** Đọc dữ kiện correctCount. */
    public int getCorrectCount() {
        return correctCount;
    }

    /** Ghi dữ kiện correctCount. */
    public void setCorrectCount(int correctCount) {
        this.correctCount = correctCount;
    }

    /** Đọc dữ kiện requiredCount. */
    public int getRequiredCount() {
        return requiredCount;
    }

    /** Ghi dữ kiện requiredCount. */
    public void setRequiredCount(int requiredCount) {
        this.requiredCount = requiredCount;
    }

    /** Đọc dữ kiện bestQuizCorrect. */
    public int getBestQuizCorrect() {
        return bestQuizCorrect;
    }

    /** Ghi dữ kiện bestQuizCorrect. */
    public void setBestQuizCorrect(int bestQuizCorrect) {
        this.bestQuizCorrect = bestQuizCorrect;
    }

    /** Đọc dữ kiện latestQuizCorrect. */
    public int getLatestQuizCorrect() {
        return latestQuizCorrect;
    }

    /** Ghi dữ kiện latestQuizCorrect. */
    public void setLatestQuizCorrect(int latestQuizCorrect) {
        this.latestQuizCorrect = latestQuizCorrect;
    }

    /** Đọc dữ kiện bestQuizTotal. */
    public int getBestQuizTotal() {
        return bestQuizTotal;
    }

    /** Ghi dữ kiện bestQuizTotal. */
    public void setBestQuizTotal(int bestQuizTotal) {
        this.bestQuizTotal = bestQuizTotal;
    }

    /** Đọc dữ kiện latestQuizTotal. */
    public int getLatestQuizTotal() {
        return latestQuizTotal;
    }

    /** Ghi dữ kiện latestQuizTotal. */
    public void setLatestQuizTotal(int latestQuizTotal) {
        this.latestQuizTotal = latestQuizTotal;
    }

    /** Tách trình tự đang có hiệu lực và đếm các từ chối từ nhật ký server. */
    public void summarizeEvents() {
        activeEvents = new ArrayList<>();
        rejectionSummary = new ArrayList<>();
        HashMap<String, AssemblyEvent> grouped = new HashMap<>();
        for (AssemblyEvent event : events) {
            if (event.isActiveStep()) {
                activeEvents.add(event);
            }
            if ("REJECTED".equals(event.getOutcome()) && isLearningError(event.getReasonCode())) {
                AssemblyEvent summary = grouped.get(event.getReasonCode());
                if (summary == null) {
                    summary = new AssemblyEvent();
                    summary.setReasonCode(event.getReasonCode());
                    grouped.put(event.getReasonCode(), summary);
                    rejectionSummary.add(summary);
                }
                summary.setRepeatCount(summary.getRepeatCount() + 1);
            }
        }
    }

    /** Chỉ lỗi gắn/tháo của người học; lỗi tab, trạng thái và request chỉ dùng truy vết. */
    public boolean isLearningError(String code) {
        return "WRONG_COMPONENT".equals(code) || "SLOT_OCCUPIED".equals(code)
                || "ORIENTATION_NOT_ALLOWED".equals(code) || "PARENT_MISSING".equals(code)
                || "ALREADY_PLACED".equals(code) || "HAS_DEPENDENTS".equals(code)
                || "MOUNT_INCOMPATIBLE".equals(code) || "MOUNT_OCCUPIED".equals(code)
                || "HOST_MISSING".equals(code) || "NOT_SUPPORTED".equals(code);
    }

    /** So kết quả bằng tỷ lệ câu đúng, giữ giá trị điểm đã lưu để hiển thị. */
    public void summarizeQuiz() {
        hasQuiz = !quizAttempts.isEmpty();
        bestQuizCorrect = 0;
        bestQuizTotal = 0;
        if (!hasQuiz) {
            return;
        }
        QuizAttempt latest = quizAttempts.get(0);
        latestQuizCorrect = latest.getScore();
        latestQuizTotal = latest.getTotalQuestions();
        for (QuizAttempt attempt : quizAttempts) {
            if (attempt.getTotalQuestions() > 0 && (bestQuizTotal == 0
                    || (long) attempt.getScore() * bestQuizTotal
                    > (long) bestQuizCorrect * attempt.getTotalQuestions())) {
                bestQuizCorrect = attempt.getScore();
                bestQuizTotal = attempt.getTotalQuestions();
            }
        }
    }

    /** Kết quả quiz theo cùng thang câu hỏi của bài, không thêm thang điểm. */
    public String getBestQuizLabel() {
        return bestQuizCorrect + "/" + bestQuizTotal;
    }

    /** Kết quả gần nhất lấy từ thứ tự JDBC theo ngày nộp. */
    public String getLatestQuizLabel() {
        return latestQuizCorrect + "/" + latestQuizTotal;
    }

    /** Chỉ phát lại khi có nhật ký của mô hình chi tiết đã hoàn tất. */
    public boolean isCanReplay() {
        if (!session.isCompleted() || !session.isDetailed()) {
            return false;
        }
        for (AssemblyEvent event : events) {
            if (event.isAccepted()) {
                return true;
            }
        }
        return false;
    }

    /** Lối tiếp tục theo vòng đời cũ, không tạo phiên bằng GET. */
    public String getContinuePage() {
        return session.isInPreparation() ? "lap-rap.html" : "lap-rap-3d.html";
    }

    /** Nhãn chế độ không lộ enum. */
    public String getModeLabel() {
        return session.isDetailed() ? "Lắp chi tiết" : "Lắp theo nhóm linh kiện";
    }

    /** Chỉ dùng ảnh cục bộ trong assets/images, không nhận URL hoặc markup tùy ý. */
    public String getImagePath() {
        String image = robot.getImage();
        if (image == null) {
            return "";
        }
        if (image.startsWith("/assets/")) {
            image = image.substring(8);
        } else if (image.startsWith("assets/")) {
            image = image.substring(7);
        }
        return image.matches("images/[A-Za-z0-9_./-]+") && !image.contains("..") ? image : "";
    }

    /** Ngày bắt đầu theo giờ Việt Nam, không thay giá trị nguồn. */
    public String getStartedAtLabel() {
        return formatDate(session.getCreatedAt());
    }

    /** Ngày hoàn tất theo giờ Việt Nam, không thay giá trị nguồn. */
    public String getCompletedAtLabel() {
        return formatDate(session.getCompletedAt());
    }

    private String formatDate(String value) {
        if (value == null || value.isEmpty()) {
            return "Chưa có";
        }
        try {
            SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            input.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = input.parse(value);
            SimpleDateFormat output = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            output.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
            return output.format(date);
        } catch (ParseException e) {
            return "Chưa xác định";
        }
    }
}
