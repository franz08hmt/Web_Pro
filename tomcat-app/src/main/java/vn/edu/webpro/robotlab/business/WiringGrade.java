package vn.edu.webpro.robotlab.business;

import java.io.Serializable;
import java.util.*;
import java.math.*;
import java.text.*;

/** Dữ kiện hoặc luật nối dây phục vụ JDBC và JSP. */
public class WiringGrade implements Serializable {
    private int requiredCount = 0;
    private int correctCount = 0;
    private int wrongCount = 0;
    private int missingCount = 0;
    private BigDecimal score = null;
    private ArrayList<WiringConnection> rows = new ArrayList<>();

    /** Dữ kiện hoặc luật nối dây phục vụ JDBC và JSP. */
    public WiringGrade() {
    }

    /** Đọc requiredCount. */
    public int getRequiredCount() {
        return requiredCount;
    }

    /** Gán requiredCount. */
    public void setRequiredCount(int requiredCount) {
        this.requiredCount = requiredCount;
    }

    /** Đọc correctCount. */
    public int getCorrectCount() {
        return correctCount;
    }

    /** Gán correctCount. */
    public void setCorrectCount(int correctCount) {
        this.correctCount = correctCount;
    }

    /** Đọc wrongCount. */
    public int getWrongCount() {
        return wrongCount;
    }

    /** Gán wrongCount. */
    public void setWrongCount(int wrongCount) {
        this.wrongCount = wrongCount;
    }

    /** Đọc missingCount. */
    public int getMissingCount() {
        return missingCount;
    }

    /** Gán missingCount. */
    public void setMissingCount(int missingCount) {
        this.missingCount = missingCount;
    }

    /** Đọc score. */
    public BigDecimal getScore() {
        return score;
    }

    /** Gán score. */
    public void setScore(BigDecimal score) {
        this.score = score;
    }

    /** Đọc rows. */
    public ArrayList<WiringConnection> getRows() {
        return rows;
    }

    /** Gán rows. */
    public void setRows(ArrayList<WiringConnection> rows) {
        this.rows = rows;
    }

    /** Tính C/W/M/N và điểm BigDecimal, làm tròn HALF_UP một lần. */
    public void calculate(int n, int c, int w) {
        if (n <= 0 || c < 0 || c > n || w < 0) {
            throw new IllegalArgumentException("Không thể chấm khi dữ kiện kết nối không hợp lệ.");
        }
        requiredCount = n;
        correctCount = c;
        wrongCount = w;
        missingCount = n - c;
        int net = c - w;
        if (net < 0) {
            net = 0;
        }
        // BigDecimal giữ điểm chính xác; chỉ làm tròn HALF_UP tại kết quả cuối.
        score = new BigDecimal(net).multiply(new BigDecimal("100"))
                .divide(new BigDecimal(n), 1, RoundingMode.HALF_UP);
    }

    /** Dữ kiện hoặc luật nối dây phục vụ JDBC và JSP. */
    public boolean isEntirelyCorrect() {
        return requiredCount > 0 && correctCount == requiredCount && wrongCount == 0;
    }

    /** Dữ kiện hoặc luật nối dây phục vụ JDBC và JSP. */
    public String getResultLabel() {
        if (isEntirelyCorrect()) {
            return "Sơ đồ đúng toàn bộ";
        }
        return "Cần sửa sơ đồ";
    }

    /** Định dạng điểm đã tính/lưu; nháp chưa chấm không có số điểm giả. */
    public String getScoreDisplay() {
        if (score == null) {
            return "Chưa chấm";
        }
        NumberFormat format = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        format.setMinimumFractionDigits(1);
        format.setMaximumFractionDigits(1);
        return format.format(score);
    }

}
