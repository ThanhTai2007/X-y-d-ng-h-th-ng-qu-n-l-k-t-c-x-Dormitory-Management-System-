package service;

import dao.PhongDAO;
import model.Phong;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PhongService {

    private final PhongDAO dao = new PhongDAO();

    public void themPhong(Phong p) throws Exception {
        validate(p, true);
        if (!dao.insert(p)) throw new Exception("Lỗi khi thêm phòng.");
    }

    public void capNhatPhong(Phong p) throws Exception {
        validate(p, false);
        if (!dao.update(p)) throw new Exception("Không tìm thấy phòng để cập nhật.");
    }

    public void xoaPhong(String maPhong) throws Exception {
        // Kiểm tra điều kiện xóa
        try {
            if (!dao.delete(maPhong)) throw new Exception("Không tìm thấy phòng.");
        } catch (SQLException e) {
            if (e.getErrorCode() == 547) // FK violation
                throw new Exception("Không thể xoá: phòng đang có sinh viên hoặc hợp đồng liên kết.");
            throw e;
        }
    }

    public void capNhatTrangThai(String maPhong, String trangThai) throws Exception {
        Optional<Phong> opt = dao.findById(maPhong);
        if (opt.isEmpty()) throw new Exception("Không tìm thấy phòng " + maPhong);
        Phong p = opt.get();
        if ("BaoTri".equals(trangThai) && p.getSoNguoiHienTai() > 0)
            throw new Exception("Không thể bảo trì: phòng đang có " + p.getSoNguoiHienTai() + " người ở.");
        dao.updateTrangThai(maPhong, trangThai);
    }

    public List<Phong> layTatCa() throws SQLException { return dao.findAll(); }
    public List<Phong> layPhongTrong() throws SQLException { return dao.findAvailable(); }

    public List<Phong> timKiem(String keyword, String trangThai) throws SQLException {
        List<Phong> list = dao.findAll();
        return list.stream().filter(p -> {
            boolean okKw = keyword == null || keyword.isBlank()
                    || p.getMaPhong().toLowerCase().contains(keyword.toLowerCase())
                    || p.getTenPhong().toLowerCase().contains(keyword.toLowerCase());
            boolean okSt = trangThai == null || "Tất cả".equals(trangThai)
                    || trangThai.equals(p.getTrangThai());
            return okKw && okSt;
        }).collect(Collectors.toList());
    }

    private void validate(Phong p, boolean isNew) throws Exception {
        if (p.getMaPhong() == null || p.getMaPhong().isBlank())
            throw new IllegalArgumentException("Mã phòng không được để trống.");
        if (p.getTenPhong() == null || p.getTenPhong().isBlank())
            throw new IllegalArgumentException("Tên phòng không được để trống.");
        if (p.getSucChua() <= 0 || p.getSucChua() > 20)
            throw new IllegalArgumentException("Sức chứa phải từ 1 đến 20.");
        if (p.getGiaThue() <= 0)
            throw new IllegalArgumentException("Giá thuê phải lớn hơn 0.");
        if (isNew && dao.findById(p.getMaPhong()).isPresent())
            throw new IllegalArgumentException("Mã phòng " + p.getMaPhong() + " đã tồn tại.");
    }
}
