package service;

import dao.SinhVienDAO;
import dao.ViPhamDAO;
import model.ViPham;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

// Dịch vụ quản lý vi phạm
public class ViPhamService {

    private final ViPhamDAO   viPhamDAO   = new ViPhamDAO();
    private final SinhVienDAO sinhVienDAO = new SinhVienDAO();

    // Thêm vi phạm mới
    public void ghiNhanViPham(ViPham vp) throws Exception {
        validate(vp);

        // Tạo mã tự động
        if (vp.getMaViPham() == null || vp.getMaViPham().isBlank())
            vp.setMaViPham(viPhamDAO.generateNextId());

        // Gắn người ghi nhận
        if (AuthService.getCurrentUser() != null)
            vp.setNguoiGhiNhanID(AuthService.getCurrentUser().getTaiKhoanID());

        boolean ok = viPhamDAO.insert(vp);
        if (!ok) throw new Exception("Lỗi khi lưu vi phạm. Vui lòng thử lại.");
    }

    // Cập nhật trạng thái
    public void capNhatTrangThai(String maViPham, String trangThai) throws Exception {
        if (maViPham == null || maViPham.isBlank())
            throw new IllegalArgumentException("Mã vi phạm không hợp lệ.");

        List<String> hops = List.of("ChuaXuLy", "DaXuLy", "DaKhangCao");
        if (!hops.contains(trangThai))
            throw new IllegalArgumentException("Trạng thái không hợp lệ: " + trangThai);

        boolean ok = viPhamDAO.updateTrangThai(maViPham, trangThai);
        if (!ok) throw new Exception("Không tìm thấy vi phạm để cập nhật.");
    }

    // Xóa vi phạm
    public void xoaViPham(String maViPham) throws Exception {
        boolean ok = viPhamDAO.delete(maViPham);
        if (!ok) throw new Exception("Không tìm thấy vi phạm để xoá.");
    }

    public List<ViPham> layTatCa() throws SQLException {
        return viPhamDAO.findAll();
    }

    public List<ViPham> locTheoTrangThai(String trangThai) throws SQLException {
        if (trangThai == null || "Tất cả".equals(trangThai))
            return viPhamDAO.findAll();
        return viPhamDAO.findByTrangThai(trangThai);
    }

    public List<ViPham> timKiem(String keyword) throws SQLException {
        if (keyword == null || keyword.isBlank()) return viPhamDAO.findAll();
        return viPhamDAO.search(keyword.trim());
    }

    public List<ViPham> layTheoSinhVien(String maSinhVien) throws SQLException {
        return viPhamDAO.findBySinhVien(maSinhVien);
    }

    // Xác thực dữ liệu
    private void validate(ViPham vp) throws Exception {
        if (vp.getMaSinhVien() == null || vp.getMaSinhVien().isBlank())
            throw new IllegalArgumentException("Vui lòng chọn sinh viên vi phạm.");
        if (vp.getLoaiViPham() == null || vp.getLoaiViPham().isBlank())
            throw new IllegalArgumentException("Vui lòng chọn loại vi phạm.");
        if (vp.getMoTa() == null || vp.getMoTa().trim().length() < 10)
            throw new IllegalArgumentException("Mô tả vi phạm phải có ít nhất 10 ký tự.");
        if (vp.getNgayViPham() == null)
            throw new IllegalArgumentException("Vui lòng chọn ngày xảy ra vi phạm.");
        if (vp.getNgayViPham().isAfter(LocalDate.now()))
            throw new IllegalArgumentException("Ngày vi phạm không thể là ngày tương lai.");
        if (vp.getMucPhat() < 0)
            throw new IllegalArgumentException("Mức phạt không được âm.");

        // Kiểm tra sinh viên tồn tại
        if (sinhVienDAO.findById(vp.getMaSinhVien()).isEmpty())
            throw new IllegalArgumentException("Sinh viên " + vp.getMaSinhVien() + " không tồn tại.");
    }
}
