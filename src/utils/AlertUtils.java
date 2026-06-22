package utils;

import java.util.Optional;

/**
 * Tiện ích hộp thoại thông báo tùy chỉnh.
 */
public class AlertUtils {

    private AlertUtils() {}

    /** Hiển thị hộp thoại lỗi. */
    public static void showError(String title, String message) {
        DialogUtils.showConfirmDialog(title, message, "Đóng", true);
    }

    /** Hiển thị hộp thoại thành công. */
    public static void showSuccess(String title, String message) {
        DialogUtils.showConfirmDialog(title, message, "OK", false);
    }

    /** Hiển thị hộp thoại cảnh báo. */
    public static void showWarning(String title, String message) {
        DialogUtils.showConfirmDialog(title, message, "Hiểu rồi", false);
    }

    /**
     * Hiển thị hộp thoại xác nhận.
     * Trả về true nếu người dùng chọn Xác nhận.
     */
    public static boolean showConfirmation(String title, String message) {
        return DialogUtils.showConfirmDialog(title, message, "Xác nhận", false);
    }
}
