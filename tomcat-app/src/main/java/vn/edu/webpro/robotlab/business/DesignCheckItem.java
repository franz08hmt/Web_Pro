package vn.edu.webpro.robotlab.business;

import java.io.Serializable;

/** Dữ kiện và quy tắc trình bày của DesignCheckItem. */
public class DesignCheckItem implements Serializable {
    private String layer = "";
    private String result = "PASS";
    private String message = "";

    /** Tạo JavaBean rỗng theo quy ước môn học. */
    public DesignCheckItem() {
    }

    /** Đọc dữ kiện layer. */
    public String getLayer() {
        return layer;
    }

    /** Ghi dữ kiện layer. */
    public void setLayer(String layer) {
        this.layer = layer;
    }

    /** Đọc dữ kiện result. */
    public String getResult() {
        return result;
    }

    /** Ghi dữ kiện result. */
    public void setResult(String result) {
        this.result = result;
    }

    /** Đọc dữ kiện message. */
    public String getMessage() {
        return message;
    }

    /** Ghi dữ kiện message. */
    public void setMessage(String message) {
        this.message = message;
    }

    /** Nhãn có chữ, không chỉ dựa vào màu hoặc mã kỹ thuật. */
    public String getResultLabel() {
        if ("WARN".equals(result)) {
            return "Lưu ý";
        }
        if ("FAIL".equals(result)) {
            return "Cần sửa";
        }
        if ("UNSUPPORTED".equals(result)) {
            return "Chưa hỗ trợ kiểm chứng";
        }
        return "Đạt";
    }

}
