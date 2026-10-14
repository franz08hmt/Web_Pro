package vn.edu.webpro.robotlab.util;

/** Mã HTTP cho controller; thông báo đã được bean tạo, không chứa SQL. */
public class DesignException extends IllegalArgumentException {
    private final String code;

    /** Giữ riêng mã máy và câu cho người học. */
    public DesignException(String code, String message) {
        super(message);
        this.code = code;
    }

    /** Đọc mã để Servlet chọn status, không chọn luật nghiệp vụ. */
    public String getCode() {
        return code;
    }
}
