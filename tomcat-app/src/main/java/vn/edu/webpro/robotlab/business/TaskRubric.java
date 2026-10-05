package vn.edu.webpro.robotlab.business;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
public class TaskRubric implements Serializable {
    public static final int ASSEMBLY_WEIGHT = 40;
    public static final int QUIZ_WEIGHT = 40;
    public static final int EXPLANATION_WEIGHT = 20;
    public TaskRubric() { }
    public BigDecimal automaticPoints(int score, int total) {
        if (total <= 0 || score < 0 || score > total) throw new IllegalArgumentException("Lượt quiz không hợp lệ.");
        // BigDecimal + HALF_UP giữ đúng giá trị lưu và so ngưỡng, tránh sai số nhị phân.
        return roundAutomatic(new BigDecimal(score).multiply(new BigDecimal(QUIZ_WEIGHT))
                .divide(new BigDecimal(total), 12, RoundingMode.HALF_UP).add(new BigDecimal(ASSEMBLY_WEIGHT)));
    }
    public BigDecimal roundAutomatic(BigDecimal points) { return points.setScale(1, RoundingMode.HALF_UP); }
    public BigDecimal totalPoints(BigDecimal automatic, int level) { return automatic.add(new BigDecimal(explanationPoints(level))); }
    public int explanationPoints(int level) {
        if (level < 0 || level > 4) throw new IllegalArgumentException("Mức giải thích phải từ 0 đến 4.");
        return level * 5;
    }
    public String explanationLabel(int level) {
        explanationPoints(level);
        if (level == 0) return "Không có nội dung hoặc không liên quan";
        if (level == 1) return "Kể lại thao tác nhưng chưa giải thích";
        if (level == 2) return "Giải thích đúng một phần, còn thiếu căn cứ";
        if (level == 3) return "Lập luận đúng, liên hệ với kết quả quan sát";
        return "Lập luận rõ, có căn cứ và đề xuất cải thiện hợp lý";
    }
    public boolean canPass(BigDecimal total, int threshold, boolean completed) {
        return completed && total.compareTo(new BigDecimal(threshold)) >= 0;
    }
    public boolean canRequestRevision(PracticeTask task, int count, Date now) {
        return task.canReceive(now) && count < task.getMaxSubmissions();
    }
    public boolean canEditReview(TaskRound next) { return next == null || !next.isHasActivity(); }
    public long chooseQuizAttempt(int round, long own, long previous) {
        if (own > 0) return own;
        if (round > 1) return previous;
        return 0;
    }
    public boolean validAssemblyEvidence(boolean owner, boolean sameRobot, boolean completed,
            Date completedAt, Date firstStart, boolean prior) {
        return owner && sameRobot && completed && completedAt != null && firstStart != null
            && (prior || !completedAt.before(firstStart));
    }
    public static void requireText(String value, int min, int max, String label) {
        if (value == null || value.trim().length() < min || value.length() > max)
            throw new IllegalArgumentException(label + ": yêu cầu " + min + "–" + max + " ký tự.");
    }
    public static String formatDate(Date value) { return formatDate(value, "dd/MM/yyyy HH:mm"); }
    public static String formatDate(Date value, String pattern) {
        if (value == null) return "Chưa có dữ liệu";
        SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.ENGLISH);
        format.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        return format.format(value);
    }
    public static String formatNumber(BigDecimal value) {
        NumberFormat format = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        format.setMinimumFractionDigits(1);
        format.setMaximumFractionDigits(1);
        return format.format(value);
    }
}
