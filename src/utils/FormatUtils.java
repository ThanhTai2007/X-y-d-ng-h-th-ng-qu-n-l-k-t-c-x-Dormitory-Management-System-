package utils;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

// Tiện ích format tiền, ngày, trạng thái
public class FormatUtils {

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final NumberFormat CURRENCY =
            NumberFormat.getInstance(new Locale("vi", "VN"));

    private FormatUtils() {}

    // Định dạng tiền
    public static String formatCurrency(double amount) {
        return CURRENCY.format((long) amount) + " đ";
    }

    // Định dạng ngày
    public static String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_FMT) : "-";
    }

    // Chuyển chuỗi thành ngày
    public static LocalDate parseDate(String text) {
        try {
            return LocalDate.parse(text, DATE_FMT);
        } catch (Exception e) {
            return null;
        }
    }

    // Trạng thái hợp đồng sang tiếng Việt
    public static String trangThaiHopDong(String status) {
        return switch (status == null ? "" : status) {
            case "Active"     -> "Đang hiệu lực";
            case "Expired"    -> "Đã hết hạn";
            case "Terminated" -> "Đã chấm dứt";
            case "Pending"    -> "Chờ duyệt";
            default           -> status;
        };
    }

    // Trạng thái thanh toán sang tiếng Việt
    public static String trangThaiThanhToan(String status) {
        return switch (status == null ? "" : status) {
            case "DaThanhToan"   -> "Đã thanh toán";
            case "ChuaThanhToan" -> "Chưa thanh toán";
            case "TreHan"        -> "Trễ hạn";
            default              -> status;
        };
    }

    // Trạng thái phòng sang tiếng Việt
    public static String trangThaiPhong(String status) {
        return switch (status == null ? "" : status) {
            case "Trong"  -> "Còn trống";
            case "Day"    -> "Đã đầy";
            case "BaoTri" -> "Bảo trì";
            default       -> status;
        };
    }
}
