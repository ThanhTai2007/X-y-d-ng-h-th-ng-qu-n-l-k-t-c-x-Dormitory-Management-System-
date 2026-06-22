package utils;

import org.mindrot.jbcrypt.BCrypt;

// Tiện ích mã hoá mật khẩu BCrypt
public class PasswordUtils {

    private static final int BCRYPT_COST = 12;

    private PasswordUtils() {}

    // Mã hoá mật khẩu
    public static String hash(String plainText) {
        if (plainText == null || plainText.isBlank()) {
            throw new IllegalArgumentException("Mật khẩu không được rỗng.");
        }
        return BCrypt.hashpw(plainText, BCrypt.gensalt(BCRYPT_COST));
    }

    // Xác minh mật khẩu
    public static boolean verify(String plainText, String storedHash) {
        if (plainText == null || storedHash == null) return false;
        try {
            return BCrypt.checkpw(plainText, storedHash);
        } catch (IllegalArgumentException e) {
            System.err.println("[PASSWORD] Hash không hợp lệ: " + e.getMessage());
            return false;
        }
    }
}
