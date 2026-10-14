package vn.edu.webpro.robotlab.business;

import java.io.Serializable;

/** Dữ kiện và quy tắc trình bày của DesignLearningError. */
public class DesignLearningError implements Serializable {
    private String code = "";
    private int count;

    /** Tạo JavaBean rỗng theo quy ước môn học. */
    public DesignLearningError() {
    }

    /** Đọc dữ kiện code. */
    public String getCode() {
        return code;
    }

    /** Ghi dữ kiện code. */
    public void setCode(String code) {
        this.code = code;
    }

    /** Đọc dữ kiện count. */
    public int getCount() {
        return count;
    }

    /** Ghi dữ kiện count. */
    public void setCount(int count) {
        this.count = count;
    }

    /** Chỉ dịch nhãn; danh sách mã được đếm nằm ở AssemblyReview.isLearningError. */
    public String getLabel() {
        if ("MOUNT_INCOMPATIBLE".equals(code)) {
            return "Loại điểm gắn không phù hợp";
        }
        if ("MOUNT_OCCUPIED".equals(code)) {
            return "Điểm gắn đã có vật thể";
        }
        if ("HOST_MISSING".equals(code)) {
            return "Thiếu vật thể cha";
        }
        if ("NOT_SUPPORTED".equals(code)) {
            return "Chưa hỗ trợ kiểm chứng linh kiện";
        }
        AssemblyEvent event = new AssemblyEvent();
        event.setReasonCode(code);
        return event.getReasonLabel();
    }

}
