package vn.edu.webpro.robotlab.util;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Băm và kiểm tra mật khẩu bằng PBKDF2 có sẵn trong JDK.
 *
 * Chuỗi lưu ở cột users.password_hash có dạng
 *     pbkdf2-sha256$<số vòng>$<salt base64>$<hash base64>
 * nên không bao giờ lưu mật khẩu gốc trong database.
 */
public final class PasswordUtil {
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final String PREFIX = "pbkdf2-sha256";
    private static final int ITERATIONS = 210000;
    private static final int BITS = 256;
    private static final int SALT_BYTES = 16;
    private static final int FIELD_COUNT = 4;

    private PasswordUtil() {
    }

    public static String hashPassword(String password) {
        byte[] salt = new byte[SALT_BYTES];
        new SecureRandom().nextBytes(salt);
        return encode(salt, derive(password, salt));
    }

    public static boolean verifyPassword(String password, String encoded) {
        if (password == null || encoded == null) return false;
        try {
            String[] fields = encoded.split("\\$");
            if (fields.length != FIELD_COUNT
                    || !PREFIX.equals(fields[0])
                    || Integer.parseInt(fields[1]) != ITERATIONS) {
                return false;
            }

            byte[] salt = Base64.getDecoder().decode(fields[2]);
            byte[] expected = Base64.getDecoder().decode(fields[3]);
            byte[] actual = derive(password, salt);

            // So sánh hằng thời gian để thời gian phản hồi không tiết lộ hash đúng bao nhiêu byte.
            return MessageDigest.isEqual(expected, actual);
        } catch (RuntimeException e) {
            return false;
        }
    }

    private static byte[] derive(String password, byte[] salt) {
        try {
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, BITS);
            return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).getEncoded();
        } catch (Exception e) {
            throw new IllegalStateException("Không thể băm mật khẩu.", e);
        }
    }

    private static String encode(byte[] salt, byte[] hash) {
        return PREFIX + "$" + ITERATIONS
                + "$" + Base64.getEncoder().encodeToString(salt)
                + "$" + Base64.getEncoder().encodeToString(hash);
    }
}
