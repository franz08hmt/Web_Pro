package vn.edu.webpro.robotlab.util;

import java.io.IOException;
import java.io.Reader;

/**
 * Đọc và ghi JSON đơn giản cho các API mà JavaScript của giao diện gọi tới.
 *
 * Thân request của dự án chỉ là object phẳng như {"email":"...","password":"..."},
 * nên không cần thư viện JSON bên ngoài.
 */
public final class JsonUtil {
    private JsonUtil() {
    }

    /** Đọc thân request, từ chối nếu dài quá maxChars để tránh request khổng lồ. */
    public static String readBody(Reader reader, int maxChars) throws IOException {
        StringBuilder body = new StringBuilder();
        char[] buffer = new char[1024];
        int count;
        while ((count = reader.read(buffer)) != -1) {
            if (body.length() + count > maxChars) {
                throw new IllegalArgumentException("body-too-large");
            }
            body.append(buffer, 0, count);
        }
        return body.toString();
    }

    /** Bọc chuỗi trong dấu nháy và escape ký tự đặc biệt; null thành null. */
    public static String quote(String value) {
        if (value == null) return "null";

        StringBuilder json = new StringBuilder("\"");
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            switch (character) {
                case '\\' -> json.append("\\\\");
                case '"' -> json.append("\\\"");
                case '\b' -> json.append("\\b");
                case '\f' -> json.append("\\f");
                case '\n' -> json.append("\\n");
                case '\r' -> json.append("\\r");
                case '\t' -> json.append("\\t");
                default -> {
                    if (character < 0x20) {
                        json.append(String.format("\\u%04x", (int) character));
                    } else {
                        json.append(character);
                    }
                }
            }
        }
        return json.append('"').toString();
    }

    /** Cột JSON dạng object (specs, illustration); giá trị hỏng trả về {}. */
    public static String objectOrEmpty(String value) {
        if (value == null) return "{}";
        String trimmed = value.trim();
        return trimmed.startsWith("{") && trimmed.endsWith("}") ? trimmed : "{}";
    }

    /** Cột JSON dạng mảng (wiring); giá trị hỏng trả về []. */
    public static String arrayOrEmpty(String value) {
        if (value == null) return "[]";
        String trimmed = value.trim();
        return trimmed.startsWith("[") && trimmed.endsWith("]") ? trimmed : "[]";
    }

    /** Lấy giá trị chuỗi của một khóa trong object JSON phẳng. */
    public static String stringField(String body, String key) {
        if (body == null) throw new IllegalArgumentException("invalid-json");

        String marker = "\"" + key + "\"";
        int keyStart = body.indexOf(marker);
        if (keyStart < 0) throw new IllegalArgumentException("missing-" + key);

        int colon = body.indexOf(':', keyStart + marker.length());
        int quote = colon < 0 ? -1 : body.indexOf('"', colon + 1);
        if (quote < 0) throw new IllegalArgumentException("invalid-json");

        StringBuilder value = new StringBuilder();
        boolean escaped = false;
        for (int index = quote + 1; index < body.length(); index++) {
            char character = body.charAt(index);
            if (escaped) {
                value.append(character);
                escaped = false;
            } else if (character == '\\') {
                escaped = true;
            } else if (character == '"') {
                return value.toString();
            } else {
                value.append(character);
            }
        }
        throw new IllegalArgumentException("invalid-json");
    }

    /** Lấy nguyên văn một object JSON lồng bên trong, ví dụ "specs":{...}. */
    public static String objectField(String body, String key) {
        return nestedField(body, key, '{', '}');
    }

    /** Lấy nguyên văn một mảng JSON lồng bên trong, ví dụ "wiring":[...]. */
    public static String arrayField(String body, String key) {
        return nestedField(body, key, '[', ']');
    }

    /* Giá trị ngay sau dấu hai chấm phải mở bằng đúng ký tự open; nếu không,
       một mảng chứa object sẽ bị đọc nhầm thành object đầu tiên của mảng. */
    private static String nestedField(String body, String key, char open, char close) {
        String marker = "\"" + key + "\"";
        int keyStart = body == null ? -1 : body.indexOf(marker);
        int colon = keyStart < 0 ? -1 : body.indexOf(':', keyStart + marker.length());
        int start = colon < 0 ? -1 : colon + 1;
        while (start >= 0 && start < body.length() && Character.isWhitespace(body.charAt(start))) {
            start++;
        }
        if (start < 0 || start >= body.length() || body.charAt(start) != open) {
            throw new IllegalArgumentException("missing-" + key);
        }

        int depth = 0;
        boolean quoted = false;
        boolean escaped = false;
        for (int index = start; index < body.length(); index++) {
            char character = body.charAt(index);
            if (quoted) {
                if (escaped) {
                    escaped = false;
                } else if (character == '\\') {
                    escaped = true;
                } else if (character == '"') {
                    quoted = false;
                }
                continue;
            }
            if (character == '"') {
                quoted = true;
            } else if (character == open) {
                depth++;
            } else if (character == close && --depth == 0) {
                return body.substring(start, index + 1);
            }
        }
        throw new IllegalArgumentException("invalid-json");
    }
}
