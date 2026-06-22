package utils;

import dao.DashboardDAO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Dịch vụ thông báo không lưu trữ. */
public class NotificationService {

    // Bản ghi thông báo (bất biến)
    public record NotifItem(
            String icon,
            String title,
            String subtitle,
            String dotStyle   // "notif-dot-red" | "notif-dot-amber" | "notif-dot-blue" | "notif-dot-green"
    ) {}

    private final DashboardDAO dao = new DashboardDAO();

    // Lấy danh sách thông báo sắp xếp theo mức ưu tiên
    public List<NotifItem> getNotifications() {
        List<NotifItem> items = new ArrayList<>();
        try {
            Map<String, Object> stats = dao.getStats();

            int treHan = ((Number) stats.getOrDefault("hoaDonTreHan",    0)).intValue();
            int viPham = ((Number) stats.getOrDefault("viPhamChuaXuLy",  0)).intValue();
            int baoTri = ((Number) stats.getOrDefault("soPhongBaoTri",   0)).intValue();

            // Quan trọng: hóa đơn trễ hạn
            if (treHan > 0) {
                items.add(new NotifItem(
                        "💸",
                        treHan + " hóa đơn quá hạn chưa thu",
                        "Cần xử lý ngay để tránh tổn thất doanh thu",
                        "notif-dot-red"
                ));
            }

            // Khẩn: hợp đồng hết hạn trong 7 ngày
            try {
                var expiring7 = dao.getHopDongSapHetHan(7);
                if (!expiring7.isEmpty()) {
                    items.add(new NotifItem(
                            "📅",
                            expiring7.size() + " hợp đồng hết hạn trong 7 ngày",
                            "Cần gia hạn hoặc thanh lý ngay",
                            "notif-dot-red"
                    ));
                }

                // Cảnh báo: hợp đồng hết hạn trong 8–30 ngày
                var expiring30 = dao.getHopDongSapHetHan(30);
                int between = expiring30.size() - expiring7.size();
                if (between > 0) {
                    items.add(new NotifItem(
                            "📋",
                            between + " hợp đồng hết hạn trong 30 ngày",
                            "Theo dõi và lên kế hoạch gia hạn",
                            "notif-dot-amber"
                    ));
                }
            } catch (Exception ignored) {}

            // Cảnh báo: vi phạm chưa xử lý
            if (viPham > 0) {
                items.add(new NotifItem(
                        "⚠️",
                        viPham + " vi phạm chưa được xử lý",
                        "Cần xét duyệt và phản hồi sinh viên",
                        "notif-dot-amber"
                ));
            }

            // Thông tin: phòng bảo trì
            if (baoTri > 0) {
                items.add(new NotifItem(
                        "🔧",
                        baoTri + " phòng đang trong chế độ bảo trì",
                        "Theo dõi tiến độ sửa chữa và bàn giao",
                        "notif-dot-blue"
                ));
            }

            // Tất cả bình thường
            if (items.isEmpty()) {
                items.add(new NotifItem(
                        "✅",
                        "Hệ thống hoạt động bình thường",
                        "Không có vấn đề nào cần chú ý",
                        "notif-dot-green"
                ));
            }

        } catch (Exception e) {
            items.add(new NotifItem(
                    "⚠️",
                    "Không thể tải thông báo",
                    "Kiểm tra kết nối cơ sở dữ liệu",
                    "notif-dot-amber"
            ));
        }
        return items;
    }

    // Đếm badge thông báo từ stats (nhanh, 1 lần gọi DB)
    public int getBadgeCount(Map<String, Object> stats) {
        int count = 0;
        int treHan = ((Number) stats.getOrDefault("hoaDonTreHan",   0)).intValue();
        int viPham = ((Number) stats.getOrDefault("viPhamChuaXuLy", 0)).intValue();
        int baoTri = ((Number) stats.getOrDefault("soPhongBaoTri",  0)).intValue();
        if (treHan > 0) count++;
        if (viPham > 0) count++;
        if (baoTri > 0) count++;
        return count;
    }
}
