package vn.edu.webpro.robotlab.service;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Lưu mật khẩu bằng PBKDF2 có sẵn trong JDK, không cần thư viện ngoài.
 *
 * Chuỗi lưu trong cột password_hash có dạng:
 *     pbkdf2-sha256$<số vòng>$<salt base64>$<hash base64>
 * Ghi kèm số vòng để sau này tăng tham số mà vẫn đọc được hash cũ.
 */
public final class PasswordService {
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final String PREFIX = "pbkdf2-sha256";
    private static final int ITERATIONS = 210000;
    private static final int BITS = 256;
    private static final int SALT_BYTES = 16;
    private static final int FIELD_COUNT = 4;

    public String hash(String password) {
        byte[] salt = new byte[SALT_BYTES];
        new SecureRandom().nextBytes(salt);
        return encode(salt, derive(password, salt));
    }

    public boolean verify(String password, String encoded) {
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

            // So sánh hằng thời gian: không để thời gian phản hồi tiết lộ
            // hash đúng được bao nhiêu byte đầu.
            return MessageDigest.isEqual(expected, actual);
        } catch (RuntimeException exception) {
            // Hash hỏng hoặc sai định dạng thì coi như không khớp.
            return false;
        }
    }

    private byte[] derive(String password, byte[] salt) {
        try {
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, BITS);
            return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).getEncoded();
        } catch (Exception exception) {
            throw new IllegalStateException("Không thể băm mật khẩu.", exception);
        }
    }

    private String encode(byte[] salt, byte[] hash) {
        return PREFIX + "$" + ITERATIONS
                + "$" + Base64.getEncoder().encodeToString(salt)
                + "$" + Base64.getEncoder().encodeToString(hash);
    }
}
