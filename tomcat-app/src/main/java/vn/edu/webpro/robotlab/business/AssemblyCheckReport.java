package vn.edu.webpro.robotlab.business;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import vn.edu.webpro.robotlab.util.JsonUtil;

/** Dữ kiện và luật lắp ráp mô phỏng do server xác thực. */
public class AssemblyCheckReport implements Serializable {
    private int correctCount;
    private int requiredCount;
    private List<String> trayLabels = new ArrayList<>();
    private List<String> missingSlots = new ArrayList<>();
    private List<String> errors = new ArrayList<>();

    /** Tạo bean rỗng theo quy ước môn học. */
    public AssemblyCheckReport() {
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

    /** Đọc dữ kiện trayLabels. */
    public List<String> getTrayLabels() {
        return trayLabels;
    }

    /** Ghi dữ kiện trayLabels. */
    public void setTrayLabels(List<String> trayLabels) {
        this.trayLabels = trayLabels;
    }

    /** Đọc dữ kiện missingSlots. */
    public List<String> getMissingSlots() {
        return missingSlots;
    }

    /** Ghi dữ kiện missingSlots. */
    public void setMissingSlots(List<String> missingSlots) {
        this.missingSlots = missingSlots;
    }

    /** Đọc dữ kiện errors. */
    public List<String> getErrors() {
        return errors;
    }

    /** Ghi dữ kiện errors. */
    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    /** Chỉ đủ toàn bộ vật thể đúng và không lỗi mới được hoàn tất. */
    public boolean isComplete() {
        return requiredCount > 0 && correctCount == requiredCount && errors.isEmpty();
    }

    /** Phần trăm chỉ để hiển thị, tính từ dữ liệu server. */
    public int getPercent() {
        return requiredCount == 0 ? 0 : correctCount * 100 / requiredCount;
    }

}
