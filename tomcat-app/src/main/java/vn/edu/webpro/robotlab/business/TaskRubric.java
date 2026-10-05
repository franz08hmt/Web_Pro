package vn.edu.webpro.robotlab.business;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/** Tiêu chí mẫu A/B, cách làm tròn điểm và điều kiện đánh giá. */
public class TaskRubric implements Serializable {
    private String template = "A";


    public static final int ASSEMBLY_WEIGHT = 40;

    public static final int QUIZ_WEIGHT = 40;

    public static final int EXPLANATION_WEIGHT = 20;

    /** Khởi tạo JavaBean với giá trị mặc định. */
    public TaskRubric() {
    }

    /** Tính điểm tự động mẫu A và làm tròn HALF_UP để lưu. */
    public BigDecimal automaticPoints(int score, int total) {
        if ("B".equals(template)) {
            throw new IllegalArgumentException("Mẫu B cần điểm từ lượt chẩn đoán đã chốt.");
        }
        if (total <= 0 || score < 0 || score > total) {
            throw new IllegalArgumentException("Lượt quiz không hợp lệ.");
        }
        // BigDecimal + HALF_UP giữ đúng giá trị lưu và so ngưỡng, tránh sai số nhị phân.
        return roundAutomatic(
                new BigDecimal(score)
                        .multiply(new BigDecimal(QUIZ_WEIGHT))
                        .divide(new BigDecimal(total), 12, RoundingMode.HALF_UP)
                        .add(new BigDecimal(ASSEMBLY_WEIGHT)));
    }

    /** Làm tròn điểm tự động đến một chữ số thập phân. */
    public BigDecimal roundAutomatic(BigDecimal points) {
        return points.setScale(1, RoundingMode.HALF_UP);
    }

    /** Cộng điểm giải thích vào điểm tự động đã lưu. */
    public BigDecimal totalPoints(BigDecimal automatic, int level) {
        return automatic.add(new BigDecimal(explanationPoints(level)));
    }

    /** Trả điểm của mức giải thích cố định từ 0 đến 4. */
    public int explanationPoints(int level) {
        if (level < 0 || level > 4) {
            throw new IllegalArgumentException("Mức giải thích phải từ 0 đến 4.");
        }
        return level * 5;
    }

    /** Trả mô tả của mức giải thích trong mẫu A. */
    public String explanationLabel(int level) {
        explanationPoints(level);
        if (level == 0) {
            return "Không có nội dung hoặc không liên quan";
        }
        if (level == 1) {
            return "Kể lại thao tác nhưng chưa giải thích";
        }
        if (level == 2) {
            return "Giải thích đúng một phần, còn thiếu căn cứ";
        }
        if (level == 3) {
            return "Lập luận đúng, liên hệ với kết quả quan sát";
        }
        return "Lập luận rõ, có căn cứ và đề xuất cải thiện hợp lý";
    }

    /** Kiểm ngưỡng đạt trên tổng đã lưu và bằng chứng hoàn tất. */
    public boolean canPass(BigDecimal total, int threshold, boolean completed) {
        return completed && total.compareTo(new BigDecimal(threshold)) >= 0;
    }

    /** Kiểm hạn nộp, trạng thái nhiệm vụ và số lần còn lại. */
    public boolean canRequestRevision(PracticeTask task, int count, Date now) {
        return task.canReceive(now) && count < task.getMaxSubmissions();
    }

    /** Chỉ cho sửa chấm khi vòng kế tiếp chưa có hoạt động. */
    public boolean canEditReview(TaskRound next) {
        return next == null || !next.isHasActivity();
    }

    /** Ưu tiên quiz của vòng; vòng sau được dùng lại lượt trước. */
    public long chooseQuizAttempt(int round, long own, long previous) {
        if (own > 0) {
            return own;
        }
        if (round > 1) {
            return previous;
        }
        return 0;
    }

    /** Kiểm quyền sở hữu, robot, hoàn tất và mốc vòng đầu. */
    public boolean validAssemblyEvidence(
            boolean owner,
            boolean sameRobot,
            boolean completed,
            Date completedAt,
            Date firstStart,
            boolean prior) {
        return owner
                && sameRobot
                && completed
                && completedAt != null
                && firstStart != null
                && (prior || !completedAt.before(firstStart));
    }

    /** Kiểm độ dài văn bản theo giới hạn của trường. */
    public static void requireText(String value, int min, int max, String label) {
        if (value == null || value.trim().length() < min || value.length() > max) {
            throw new IllegalArgumentException(label + ": yêu cầu " + min + "–" + max + " ký tự.");
        }
    }

    /** Định dạng ngày giờ Việt Nam bằng formatter mới mỗi lần. */
    public static String formatDate(Date value) {
        return formatDate(value, "dd/MM/yyyy HH:mm");
    }

    /** Định dạng ngày giờ Việt Nam bằng formatter mới mỗi lần. */
    public static String formatDate(Date value, String pattern) {
        if (value == null) {
            return "Chưa có dữ liệu";
        }
        SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.ENGLISH);
        format.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        return format.format(value);
    }

    /** Định dạng điểm vi-VN với một chữ số thập phân. */
    public static String formatNumber(BigDecimal value) {
        NumberFormat format = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        format.setMinimumFractionDigits(1);
        format.setMaximumFractionDigits(1);
        return format.format(value);
    }

    /** Đọc template. */
    public String getTemplate() {
        return template;
    }

    /** Gán template. */
    public void setTemplate(String template) {
        this.template = template;
    }


    /** Trọng số lắp ráp theo mẫu đã chọn. */
    public int getAssemblyWeight() {
        if ("B".equals(template)) {
            return 30;
        }
        return 40;
    }

    /** Trọng số quiz theo mẫu đã chọn. */
    public int getQuizWeight() {
        if ("B".equals(template)) {
            return 25;
        }
        return 40;
    }

    /** Trọng số chẩn đoán, mẫu A không sử dụng. */
    public int getDiagnosisWeight() {
        if ("B".equals(template)) {
            return 25;
        }
        return 0;
    }

    /** Điểm tự động tối đa của cả hai mẫu. */
    public int getAutomaticMaximum() {
        return 80;
    }

    /** Nhãn tiêu chí giữ nguyên văn bản mẫu A. */
    public String getLabel() {
        if ("B".equals(template)) {
            return getTemplateBLabel();
        }
        return getTemplateALabel();
    }

    /** Nhãn cố định mẫu A cho lựa chọn trong form. */
    public String getTemplateALabel() {
        return "Mẫu A: lắp ráp 40 + quiz 40 + giải thích 20 = 100";
    }

    /** Nhãn cố định mẫu B cho lựa chọn trong form. */
    public String getTemplateBLabel() {
        return "Mẫu B: lắp ráp 30 + quiz 25 + chẩn đoán 25 + giải thích 20 = 100";
    }

    /** Tính đóng góp quiz theo mẫu, chưa làm tròn tổng. */
    public BigDecimal quizContribution(int score, int total) {
        if (total <= 0 || score < 0 || score > total) {
            throw new IllegalArgumentException("Lượt quiz không hợp lệ.");
        }
        return new BigDecimal(score).multiply(new BigDecimal(getQuizWeight()))
                .divide(new BigDecimal(total), 24, RoundingMode.HALF_UP);
    }

    /** Tính đóng góp chẩn đoán từ điểm chưa làm tròn trên thang 10. */
    public BigDecimal diagnosisContribution(BigDecimal score) {
        if (score == null || score.signum() < 0 || score.compareTo(BigDecimal.TEN) > 0) {
            throw new IllegalArgumentException("Điểm chẩn đoán không hợp lệ.");
        }
        return score.multiply(new BigDecimal(getDiagnosisWeight())).divide(BigDecimal.TEN);
    }

    /** Mẫu B làm tròn một lần sau khi cộng ba phần tự động. */
    public BigDecimal automaticPoints(int score, int total, BigDecimal diagnosis) {
        if (!"B".equals(template)) {
            return automaticPoints(score, total);
        }
        return roundAutomatic(new BigDecimal(getAssemblyWeight()).add(quizContribution(score, total))
                .add(diagnosisContribution(diagnosis)));
    }

    /** Chọn lượt riêng nếu đã bắt đầu, không bỏ lượt để dùng lại lượt cũ. */
    public long chooseDiagnosisAttempt(int round, long own, long previous) {
        return chooseQuizAttempt(round, own, previous);
    }
}
