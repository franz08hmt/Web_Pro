package vn.edu.webpro.robotlab.util;

import vn.edu.webpro.robotlab.business.AssemblyLayout;

/** Giữ layout đã kiểm trong transaction để trả đúng lời giải thích, không truy vấn lại. */
public class AssemblyPlacementException extends IllegalArgumentException {
    private final AssemblyLayout layout;

    /** Gói mã từ chối và chính dữ kiện vừa được JavaBean kiểm. */
    public AssemblyPlacementException(String code, AssemblyLayout layout) {
        super(code);
        this.layout = layout;
    }

    /** Trả layout thuộc phiên đã được kiểm quyền trong transaction. */
    public AssemblyLayout getLayout() {
        return layout;
    }
}
