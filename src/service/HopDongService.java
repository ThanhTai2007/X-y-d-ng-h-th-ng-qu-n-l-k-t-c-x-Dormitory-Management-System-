package service;

import dao.HopDongDAO;
import dao.PhongDAO;
import dao.SinhVienDAO;
import dao.ThanhToanDAO;
import model.HopDong;
import model.Phong;
import model.SinhVien;
import model.ThanhToan;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

// Dịch vụ quản lý hợp đồng
public class HopDongService {

    private final HopDongDAO   hopDongDAO   = new HopDongDAO();
    private final PhongDAO     phongDAO     = new PhongDAO();
    private final SinhVienDAO  sinhVienDAO  = new SinhVienDAO();
    private final ThanhToanDAO thanhToanDAO = new ThanhToanDAO();

    // Tạo hợp đồng mới
    public void taoHopDong(HopDong hd) throws Exception {
        // Xác thực đầu vào
        if (hd.getMaSinhVien() == null || hd.getMaSinhVien().isBlank())
            throw new IllegalArgumentException("Chưa chọn sinh viên.");
        if (hd.getMaPhong() == null || hd.getMaPhong().isBlank())
            throw new IllegalArgumentException("Chưa chọn phòng.");
        if (hd.getNgayBatDau() == null)
            throw new IllegalArgumentException("Ngày bắt đầu không được để trống.");
        if (hd.getNgayKetThuc() == null)
            throw new IllegalArgumentException("Ngày kết thúc không được để trống.");
        if (!hd.getNgayKetThuc().isAfter(hd.getNgayBatDau()))
            throw new IllegalArgumentException("Ngày kết thúc phải sau ngày bắt đầu.");
        if (hd.getTienCoc() < 0)
            throw new IllegalArgumentException("Tiền cọc không được âm.");

        // Kiểm tra sinh viên đang học
        Optional<SinhVien> svOpt = sinhVienDAO.findById(hd.getMaSinhVien());
        if (svOpt.isEmpty())
            throw new IllegalArgumentException("Sinh viên không tồn tại.");
        if (!"DangHoc".equals(svOpt.get().getTrangThai()))
            throw new IllegalArgumentException("Sinh viên không còn đang học — không thể tạo hợp đồng.");

        // Kiểm tra hợp đồng hiện tại
        Optional<HopDong> existing = hopDongDAO.findActiveBySinhVien(hd.getMaSinhVien());
        if (existing.isPresent())
            throw new IllegalArgumentException(
                "Sinh viên " + svOpt.get().getHoTen() +
                " đã có hợp đồng đang hiệu lực (Mã: " + existing.get().getMaHopDong() + ").");

        // Kiểm tra tình trạng phòng
        Optional<Phong> phongOpt = phongDAO.findById(hd.getMaPhong());
        if (phongOpt.isEmpty())
            throw new IllegalArgumentException("Phòng không tồn tại.");
        Phong phong = phongOpt.get();
        if ("BaoTri".equals(phong.getTrangThai()))
            throw new IllegalArgumentException("Phòng " + phong.getMaPhong() + " đang bảo trì — không thể tạo hợp đồng.");
        if (!phong.conCho())
            throw new IllegalArgumentException(
                "Phòng " + phong.getTenPhong() + " đã đầy (" + phong.getSoNguoiHienTai() + "/" + phong.getSucChua() + " người).");

        // Tạo mã hợp đồng tự động
        if (hd.getMaHopDong() == null || hd.getMaHopDong().isBlank())
            hd.setMaHopDong(hopDongDAO.generateNextId());

        // Lấy giá thuê từ phòng
        hd.setGiaThue(phong.getGiaThue());

        // Gắn ID người tạo
        if (AuthService.getCurrentUser() != null)
            hd.setNguoiTaoID(AuthService.getCurrentUser().getTaiKhoanID());

        // Lưu vào cơ sở dữ liệu
        hopDongDAO.insert(hd); // Ném SQLException nếu SP báo lỗi

        // Tạo hóa đơn tháng đầu
        taoHoaDonThangDau(hd);
    }

    // Chấm dứt hợp đồng
    public void chamDutHopDong(String maHopDong, String lyDo) throws Exception {
        Optional<HopDong> opt = hopDongDAO.findById(maHopDong);
        if (opt.isEmpty())
            throw new Exception("Hợp đồng không tồn tại.");
        if (!"Active".equals(opt.get().getTrangThai()))
            throw new Exception("Hợp đồng không đang hoạt động — không thể chấm dứt.");
        if (lyDo == null || lyDo.isBlank())
            throw new IllegalArgumentException("Vui lòng nhập lý do chấm dứt hợp đồng.");

        boolean ok = hopDongDAO.terminate(maHopDong, lyDo);
        if (!ok) throw new Exception("Lỗi khi cập nhật hợp đồng. Vui lòng thử lại.");
    }

    // Gia hạn hợp đồng
    public void giaHanHopDong(String maHopDong, LocalDate ngayKetThucMoi) throws Exception {
        Optional<HopDong> opt = hopDongDAO.findById(maHopDong);
        if (opt.isEmpty())
            throw new Exception("Hợp đồng không tồn tại.");

        HopDong hd = opt.get();
        if (!"Active".equals(hd.getTrangThai()))
            throw new Exception("Chỉ có thể gia hạn hợp đồng đang Active.");
        if (!ngayKetThucMoi.isAfter(hd.getNgayKetThuc()))
            throw new IllegalArgumentException("Ngày gia hạn phải sau ngày kết thúc hiện tại (" +
                hd.getNgayKetThuc() + ").");

        hd.setNgayKetThuc(ngayKetThucMoi);
        hd.setGhiChu("Gia hạn đến " + ngayKetThucMoi);
        hopDongDAO.update(hd);
    }

    public List<HopDong> layTatCa() throws SQLException {
        return hopDongDAO.findAll();
    }

    public List<HopDong> layTheoTrangThai(String trangThai) throws SQLException {
        if (trangThai == null || "Tất cả".equals(trangThai))
            return hopDongDAO.findAll();
        return hopDongDAO.findAll().stream()
                .filter(hd -> trangThai.equals(hd.getTrangThai()))
                .collect(Collectors.toList());
    }

    public List<HopDong> timKiem(String keyword) throws SQLException {
        String kw = keyword == null ? "" : keyword.trim().toLowerCase();
        if (kw.isEmpty()) return hopDongDAO.findAll();
        return hopDongDAO.findAll().stream().filter(hd ->
            (hd.getMaHopDong()  != null && hd.getMaHopDong().toLowerCase().contains(kw)) ||
            (hd.getMaSinhVien() != null && hd.getMaSinhVien().toLowerCase().contains(kw)) ||
            (hd.getTenSinhVien()!= null && hd.getTenSinhVien().toLowerCase().contains(kw)) ||
            (hd.getMaPhong()    != null && hd.getMaPhong().toLowerCase().contains(kw))
        ).collect(Collectors.toList());
    }

    public Optional<HopDong> timActiveTheoSinhVien(String maSV) throws SQLException {
        return hopDongDAO.findActiveBySinhVien(maSV);
    }

    // Cập nhật hợp đồng hết hạn
    public void capNhatHetHan() throws SQLException {
        hopDongDAO.capNhatHopDongHetHan();
    }

    // Tạo hóa đơn tháng đầu
    private void taoHoaDonThangDau(HopDong hd) {
        try {
            LocalDate batDau = hd.getNgayBatDau();
            ThanhToan tt = new ThanhToan();
            tt.setMaThanhToan(thanhToanDAO.generateNextId());
            tt.setMaHopDong  (hd.getMaHopDong());
            tt.setThang      (batDau.getMonthValue());
            tt.setNam        (batDau.getYear());
            tt.setSoTien     (hd.getGiaThue());
            tt.setNgayTaoBill(LocalDate.now());
            // Hạn thanh toán: ngày 10 của tháng tiếp theo
            LocalDate han = batDau.withDayOfMonth(1).plusMonths(1).withDayOfMonth(10);
            tt.setHanThanhToan(han);
            thanhToanDAO.insert(tt);
        } catch (Exception e) {
            // Không throw – hóa đơn tháng đầu có thể tạo lại sau
            System.err.println("[HopDongService] Cảnh báo: Không tạo được hóa đơn tháng đầu: " + e.getMessage());
        }
    }
}