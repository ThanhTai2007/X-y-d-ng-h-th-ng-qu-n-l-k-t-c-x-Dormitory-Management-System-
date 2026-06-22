package service;

import dao.SinhVienDAO;
import model.SinhVien;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

// Dịch vụ quản lý sinh viên
public class SinhVienService {

    private final SinhVienDAO sinhVienDAO = new SinhVienDAO();

    // Thêm sinh viên mới
    public void themSinhVien(SinhVien sv) throws Exception {
        validateSinhVien(sv, true);

        if (sv.getMaSinhVien() == null || sv.getMaSinhVien().isBlank()) {
            sv.setMaSinhVien(sinhVienDAO.generateNextId());
        }

        boolean ok = sinhVienDAO.insert(sv);
        if (!ok) throw new Exception("Lỗi khi thêm sinh viên. Vui lòng thử lại.");
    }

    // Cập nhật sinh viên
    public void capNhatSinhVien(SinhVien sv) throws Exception {
        validateSinhVien(sv, false);

        boolean ok = sinhVienDAO.update(sv);
        if (!ok) throw new Exception("Không tìm thấy sinh viên để cập nhật.");
    }

    // Xóa sinh viên
    public void xoaSinhVien(String maSinhVien) throws Exception {
        try {
            boolean ok = sinhVienDAO.delete(maSinhVien);
            if (!ok) throw new Exception("Không tìm thấy sinh viên để xoá.");
        } catch (SQLException e) {
            // Báo lỗi khóa ngoại
            if (e.getMessage().contains("REFERENCE") || e.getErrorCode() == 547) {
                throw new Exception("Không thể xoá sinh viên đang có hợp đồng trong hệ thống.");
            }
            throw e;
        }
    }

    public Optional<SinhVien> timTheoMa(String maSinhVien) throws SQLException {
        return sinhVienDAO.findById(maSinhVien);
    }

    public List<SinhVien> layTatCa() throws SQLException {
        return sinhVienDAO.findAll();
    }

    public List<SinhVien> timKiem(String keyword) throws SQLException {
        if (keyword == null || keyword.isBlank()) {
            return sinhVienDAO.findAll();
        }
        return sinhVienDAO.search(keyword.trim());
    }

    public List<SinhVien> locTheoTrangThai(String trangThai) throws SQLException {
        if ("Tất cả".equals(trangThai) || trangThai == null) {
            return sinhVienDAO.findAll();
        }
        return sinhVienDAO.findByTrangThai(trangThai);
    }

    public List<SinhVien> laySinhVienTheoPhong(String maPhong) throws SQLException {
        return sinhVienDAO.findByPhong(maPhong);
    }

    public int demTong() throws SQLException {
        return sinhVienDAO.count();
    }

    // Xác thực dữ liệu sinh viên
    private void validateSinhVien(SinhVien sv, boolean isNew) throws Exception {
        // Tên
        if (sv.getHoTen() == null || sv.getHoTen().isBlank()) {
            throw new IllegalArgumentException("Họ tên sinh viên không được để trống.");
        }
        if (sv.getHoTen().trim().length() < 2) {
            throw new IllegalArgumentException("Họ tên phải có ít nhất 2 ký tự.");
        }

        // Ngày sinh
        if (sv.getNgaySinh() == null) {
            throw new IllegalArgumentException("Ngày sinh không được để trống.");
        }
        if (sv.getNgaySinh().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Ngày sinh không thể là ngày tương lai.");
        }
        if (sv.getNgaySinh().isBefore(LocalDate.of(1990, 1, 1))) {
            throw new IllegalArgumentException("Ngày sinh không hợp lệ (trước 1990).");
        }

        // Giới tính
        if (sv.getGioiTinh() == null || sv.getGioiTinh().isBlank()) {
            throw new IllegalArgumentException("Vui lòng chọn giới tính.");
        }

        // CCCD
        if (sv.getCccd() == null || sv.getCccd().isBlank()) {
            throw new IllegalArgumentException("CCCD không được để trống.");
        }
        if (sv.getCccd().length() != 12) {
            throw new IllegalArgumentException("CCCD phải đúng 12 số.");
        }
        if (!sv.getCccd().matches("\\d{12}")) {
            throw new IllegalArgumentException("CCCD chỉ được chứa số.");
        }

        // Email format
        if (sv.getEmail() != null && !sv.getEmail().isBlank()) {
            if (!sv.getEmail().matches("^[\\w.-]+@[\\w.-]+\\.[a-z]{2,}$")) {
                throw new IllegalArgumentException("Định dạng email không hợp lệ.");
            }
        }

        // Số điện thoại
        if (sv.getSoDienThoai() != null && !sv.getSoDienThoai().isBlank()) {
            if (!sv.getSoDienThoai().matches("^[0-9]{9,15}$")) {
                throw new IllegalArgumentException("Số điện thoại không hợp lệ (9-15 số).");
            }
        }

        // Kiểm tra trùng CCCD
        String maSVHienTai = isNew ? "" : sv.getMaSinhVien();
        if (sinhVienDAO.isCCCDDuplicated(sv.getCccd(), maSVHienTai)) {
            throw new IllegalArgumentException("CCCD " + sv.getCccd() + " đã được đăng ký cho sinh viên khác.");
        }

        // Kiểm tra trùng Email
        if (sv.getEmail() != null && !sv.getEmail().isBlank()) {
            if (sinhVienDAO.isEmailDuplicated(sv.getEmail(), maSVHienTai)) {
                throw new IllegalArgumentException("Email " + sv.getEmail() + " đã được sử dụng.");
            }
        }
    }
}