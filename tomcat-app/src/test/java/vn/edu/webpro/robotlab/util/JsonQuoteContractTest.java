package vn.edu.webpro.robotlab.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

/** Khóa chính xác escape JSON, gồm đủ32 ký tự điều khiển. */
class JsonQuoteContractTest {
    @Test
    void quoteKeepsNullUnicodeAndSpecialCharacters() {
        assertEquals("null", JsonUtil.quote(null));
        assertEquals("\"\"", JsonUtil.quote(""));
        assertEquals("\"Tài — robot tránh vật cản\"", JsonUtil.quote("Tài — robot tránh vật cản"));
        assertEquals("\"\\\"\\\\\\n\\t\\r\\b\\f\"", JsonUtil.quote("\"\\\n\t\r\b\f"));
        assertEquals("\"\u2028\u2029\"", JsonUtil.quote("\u2028\u2029"));
    }

    @Test
    void quoteKeepsEveryControlCharacterEncoding() {
        for (int code = 0; code < 32; code++) {
            String expected = String.format("\\u%04x", code);
            if (code == 8) {
                expected = "\\b";
            } else if (code == 9) {
                expected = "\\t";
            } else if (code == 10) {
                expected = "\\n";
            } else if (code == 12) {
                expected = "\\f";
            } else if (code == 13) {
                expected = "\\r";
            }
            assertEquals("\"" + expected + "\"", JsonUtil.quote(Character.toString((char) code)));
        }
    }
}
