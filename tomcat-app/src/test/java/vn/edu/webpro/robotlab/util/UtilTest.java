package vn.edu.webpro.robotlab.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;
import org.junit.jupiter.api.Test;
import vn.edu.webpro.robotlab.business.User;

/** Các lớp XxxUtil chỉ chứa logic thuần nên kiểm thử được mà không cần Tomcat hay MySQL. */
final class UtilTest {

    @Test
    void passwordsAreStoredOnlyAsSaltedHashes() {
        String hash = PasswordUtil.hashPassword("MatKhauAnToan123!");

        assertTrue(hash.startsWith("pbkdf2-sha256$"));
        assertFalse(hash.contains("MatKhauAnToan123!"));
        assertTrue(PasswordUtil.verifyPassword("MatKhauAnToan123!", hash));
        assertFalse(PasswordUtil.verifyPassword("sai-mat-khau", hash));
        assertFalse(PasswordUtil.verifyPassword("MatKhauAnToan123!", null));
        assertFalse(PasswordUtil.verifyPassword(null, hash));
        assertFalse(hash.equals(PasswordUtil.hashPassword("MatKhauAnToan123!")),
                "Salt ngẫu nhiên: cùng mật khẩu nhưng hai lần băm phải khác nhau");
    }

    @Test
    void registrationInputRulesMatchTheAccountForm() {
        assertEquals("an@example.com", ValidationUtil.normalizeEmail("  AN@Example.com "));
        assertTrue(ValidationUtil.isValidEmail("an@example.com"));
        assertFalse(ValidationUtil.isValidEmail("khong-phai-email"));

        assertTrue(ValidationUtil.isValidFullName("  An  "));
        assertFalse(ValidationUtil.isValidFullName(" A "));
        assertFalse(ValidationUtil.isValidFullName("x".repeat(101)));

        assertTrue(ValidationUtil.isValidPassword("12345678"));
        assertFalse(ValidationUtil.isValidPassword("1234567"));
        assertFalse(ValidationUtil.isValidPassword("x".repeat(73)));

        assertTrue(ValidationUtil.isSlug("line-follower"));
        assertFalse(ValidationUtil.isSlug("Line Follower"));
        assertFalse(ValidationUtil.isSlug("x' OR '1'='1"));
    }

    @Test
    void paginationParametersAreBoundedPositiveNumbers() {
        assertEquals(1, ValidationUtil.parsePositiveInt(null, 1, 100));
        assertEquals(7, ValidationUtil.parsePositiveInt("7", 1, 100));
        assertThrows(IllegalArgumentException.class, () -> ValidationUtil.parsePositiveInt("0", 1, 100));
        assertThrows(IllegalArgumentException.class, () -> ValidationUtil.parsePositiveInt("abc", 1, 100));
        assertThrows(IllegalArgumentException.class, () -> ValidationUtil.parsePositiveInt("101", 1, 100));
    }

    @Test
    void anOldSessionStopsWorkingOnceTheAccountVersionChanges() {
        User original = new User(5, "An", "an@example.com", User.ROLE_USER, "", 0);
        User promoted = new User(5, "An", "an@example.com", User.ROLE_ADMIN, "", 1);

        assertTrue(SessionUtil.isCurrent(original, original));
        assertFalse(SessionUtil.isCurrent(original, promoted), "Đổi quyền phải làm phiên cũ hết hiệu lực");
        assertFalse(SessionUtil.isCurrent(original, null), "Tài khoản bị xóa thì phiên hết hiệu lực");
    }

    @Test
    void jsonHelpersReadFlatBodiesAndNestedValues() throws Exception {
        String body = "{\"name\":\"Robot \\\"mini\\\"\",\"specs\":{\"a\":{\"b\":1}},"
                + "\"wiring\":[{\"pin\":\"D5\"},{\"pin\":\"D6\"}]}";

        assertEquals("Robot \"mini\"", JsonUtil.stringField(body, "name"));
        assertEquals("{\"a\":{\"b\":1}}", JsonUtil.objectField(body, "specs"));
        assertEquals("[{\"pin\":\"D5\"},{\"pin\":\"D6\"}]", JsonUtil.arrayField(body, "wiring"),
                "Mảng chứa object phải được đọc trọn vẹn, không bị cắt còn phần tử đầu");
        assertThrows(IllegalArgumentException.class, () -> JsonUtil.objectField(body, "wiring"));
        assertThrows(IllegalArgumentException.class, () -> JsonUtil.stringField(body, "missing"));

        assertEquals("\"a\\\"b\\n\"", JsonUtil.quote("a\"b\n"));
        assertEquals("null", JsonUtil.quote(null));
        assertThrows(IllegalArgumentException.class,
                () -> JsonUtil.readBody(new StringReader("x".repeat(8193)), 8192));
    }
}
