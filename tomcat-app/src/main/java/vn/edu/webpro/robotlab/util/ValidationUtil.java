package vn.edu.webpro.robotlab.util;

import java.util.Locale;

/**
 * Các quy tắc kiểm tra dữ liệu người dùng gửi lên.
 *
 * Servlet gọi các hàm này trước khi gọi lớp XxxDB, giống bước "validate the
 * parameters" của EmailListServlet trong Chapter 12 slide 44.
 */
public final class ValidationUtil {
    public static final int MAX_PAGE = 100000;
    public static final int MAX_LIMIT = 100;

    private static final String EMAIL_PATTERN = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$";
    private static final String SLUG_PATTERN = "[a-z0-9]+(?:-[a-z0-9]+)*";
    private static final int EMAIL_MAX = 254;
    private static final int NAME_MIN = 2;
    private static final int NAME_MAX = 100;
    private static final int PASSWORD_MIN = 8;
    private static final int PASSWORD_MAX = 72;

    private ValidationUtil() {
    }

    /** Email không phân biệt hoa thường nên luôn lưu và so sánh ở dạng chữ thường. */
    public static String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    public static boolean isValidEmail(String email) {
        return email != null && email.length() <= EMAIL_MAX && email.matches(EMAIL_PATTERN);
    }

    public static boolean isValidFullName(String fullName) {
        if (fullName == null) return false;
        int length = fullName.trim().length();
        return length >= NAME_MIN && length <= NAME_MAX;
    }

    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= PASSWORD_MIN && password.length() <= PASSWORD_MAX;
    }

    /** ID nội dung dạng chữ thường nối bằng gạch ngang, ví dụ line-follower. */
    public static boolean isSlug(String value) {
        return value != null && value.matches(SLUG_PATTERN);
    }

    /**
     * Đọc tham số số nguyên dương từ query string. Thiếu tham số thì dùng giá trị
     * mặc định; sai định dạng hoặc vượt giới hạn thì ném IllegalArgumentException.
     */
    public static int parsePositiveInt(String raw, int fallback, int max) {
        if (raw == null) return fallback;
        if (!raw.matches("[1-9][0-9]{0,8}")) throw new IllegalArgumentException("pagination");

        int value = Integer.parseInt(raw);
        if (value > max) throw new IllegalArgumentException("pagination");
        return value;
    }
}
