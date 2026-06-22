package service;

import dao.HopDongDAO;
import dao.ThanhToanDAO;
import model.HopDong;
import model.ThanhToan;


import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// Dịch vụ quản lý thanh toán
public class ThanhToanService {

    private final ThanhToanDAO ttDAO    = new ThanhToanDAO();
    private final HopDongDAO   hdDAO    = new HopDongDAO();

    // Tạo hóa đơn thủ công
    public ThanhToan taoHoaDon(String maHopDong, int thang, int nam,
                               double soTien, LocalDate hanThanhToan) throws Exception {
        // Xác thực đầu vào
        if (maHopDong == null || maHopDong.isBlank())
            throw new IllegalArgumentException("Mã hợp đồng không được để trống.");
        if (thang < 1 || thang > 12)
            throw new IllegalArgumentException("Tháng không hợp lệ (1–12).");
        if (nam < 2000 || nam > 2100)
            throw new IllegalArgumentException("Năm không hợp lệ.");
        if (soTien <= 0)
            throw new IllegalArgumentException("Số tiền phải lớn hơn 0.");
        if (hanThanhToan == null)
            throw new IllegalArgumentException("Hạn thanh toán không được để trống.");

        // Kiểm tra trùng hóa đơn
        List<ThanhToan> existing = ttDAO.findByHopDong(maHopDong);
        boolean trung = existing.stream().anyMatch(
                tt -> tt.getThang() == thang && tt.getNam() == nam);
        if (trung)
            throw new IllegalArgumentException(
                "Hóa đơn tháng " + thang + "/" + nam + " cho hợp đồng này đã tồn tại.");

        // Kiểm tra hợp đồng
        var hdOpt = hdDAO.findById(maHopDong);
        if (hdOpt.isEmpty())
            throw new IllegalArgumentException("Hợp đồng " + maHopDong + " không tồn tại.");
        if (!"Active".equals(hdOpt.get().getTrangThai()))
            throw new IllegalArgumentException("Hợp đồng không còn hiệu lực — không thể tạo hóa đơn.");

        // Tạo hóa đơn
        ThanhToan tt = new ThanhToan();
        tt.setMaThanhToan (ttDAO.generateNextId());
        tt.setMaHopDong   (maHopDong);
        tt.setThang       (thang);
        tt.setNam         (nam);
        tt.setSoTien      (soTien);
        tt.setNgayTaoBill (LocalDate.now());
        tt.setHanThanhToan(hanThanhToan);
        tt.setTrangThai   ("ChuaThanhToan");

        ttDAO.insert(tt);
        return tt;
    }

    // Tạo hóa đơn hàng loạt
    public int taoHoaDonHangLoat(int thang, int nam) throws SQLException {
        List<HopDong> activeList = hdDAO.findAllActive();
        int count = 0;
        List<String> errors = new ArrayList<>();

        // Hạn thanh toán: ngày 10 tháng sau
        LocalDate han = LocalDate.of(nam, thang, 1)
                                  .plusMonths(1)
                                  .withDayOfMonth(10);

        for (HopDong hd : activeList) {
            // Kiểm tra ngày kết thúc
            if (hd.getNgayKetThuc().isBefore(LocalDate.of(nam, thang, 1))) continue;

            // Kiểm tra hóa đơn hiện tại
            List<ThanhToan> bills = ttDAO.findByHopDong(hd.getMaHopDong());
            boolean daCoTháng = bills.stream()
                    .anyMatch(tt -> tt.getThang() == thang && tt.getNam() == nam);
            if (daCoTháng) continue;

            try {
                ThanhToan tt = new ThanhToan();
                tt.setMaThanhToan (ttDAO.generateNextId());
                tt.setMaHopDong   (hd.getMaHopDong());
                tt.setThang       (thang);
                tt.setNam         (nam);
                tt.setSoTien      (hd.getGiaThue());
                tt.setNgayTaoBill (LocalDate.now());
                tt.setHanThanhToan(han);
                tt.setTrangThai   ("ChuaThanhToan");
                ttDAO.insert(tt);
                count++;
            } catch (Exception e) {
                errors.add(hd.getMaHopDong() + ": " + e.getMessage());
            }
        }

        if (!errors.isEmpty())
            System.err.println("[ThanhToan] Một số HĐ không tạo được bill: " + errors);

        return count;
    }

    // Ghi nhận thanh toán
    public void ghiNhanThanhToan(String maThanhToan, String phuongThuc) throws Exception {
        if (maThanhToan == null || maThanhToan.isBlank())
            throw new IllegalArgumentException("Mã thanh toán không hợp lệ.");
        if (phuongThuc == null || phuongThuc.isBlank())
            throw new IllegalArgumentException("Vui lòng chọn phương thức thanh toán.");

        var opt = ttDAO.findById(maThanhToan);
        if (opt.isEmpty())
            throw new Exception("Hóa đơn không tồn tại.");
        if ("DaThanhToan".equals(opt.get().getTrangThai()))
            throw new Exception("Hóa đơn này đã được thanh toán trước đó.");

        int nguoiXuLyID = AuthService.getCurrentUser() != null
                ? AuthService.getCurrentUser().getTaiKhoanID() : 1;

        ttDAO.thanhToan(maThanhToan, phuongThuc, nguoiXuLyID);
    }

    // Cập nhật hóa đơn trễ hạn
    public void capNhatTreHan() {
        try {
            ttDAO.capNhatTreHan();
        } catch (Exception e) {
            System.err.println("[ThanhToan] Cảnh báo cập nhật trễ hạn: " + e.getMessage());
        }
    }

    public List<ThanhToan> layTatCa() throws SQLException {
        return ttDAO.findAll();
    }

    public List<ThanhToan> locTheoTrangThai(String trangThai) throws SQLException {
        List<ThanhToan> all = ttDAO.findAll();
        if (trangThai == null || "Tất cả".equals(trangThai)) return all;
        return all.stream()
                  .filter(tt -> trangThai.equals(tt.getTrangThai()))
                  .collect(Collectors.toList());
    }

    public List<ThanhToan> timKiem(String keyword) throws SQLException {
        String kw = keyword == null ? "" : keyword.trim().toLowerCase();
        if (kw.isEmpty()) return ttDAO.findAll();
        return ttDAO.findAll().stream().filter(tt ->
            (tt.getMaThanhToan() != null && tt.getMaThanhToan().toLowerCase().contains(kw)) ||
            (tt.getMaHopDong()   != null && tt.getMaHopDong().toLowerCase().contains(kw)) ||
            (tt.getHoTenSinhVien()!= null && tt.getHoTenSinhVien().toLowerCase().contains(kw))
        ).collect(Collectors.toList());
    }

    public List<ThanhToan> layTheoHopDong(String maHopDong) throws SQLException {
        return ttDAO.findByHopDong(maHopDong);
    }

    // Tính tổng thanh toán
    public double tongDaThanhToan(List<ThanhToan> list) {
        return list.stream()
                   .filter(tt -> "DaThanhToan".equals(tt.getTrangThai()))
                   .mapToDouble(ThanhToan::getSoTien).sum();
    }

    public double tongChuaThanhToan(List<ThanhToan> list) {
        return list.stream()
                   .filter(tt -> !"DaThanhToan".equals(tt.getTrangThai()))
                   .mapToDouble(ThanhToan::getSoTien).sum();
    }

    public long demTreHan(List<ThanhToan> list) {
        return list.stream()
                   .filter(tt -> "TreHan".equals(tt.getTrangThai()))
                   .count();
    }
}
