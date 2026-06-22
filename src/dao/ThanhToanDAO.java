package dao;

import model.ThanhToan;
import utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// DAO xử lý Thanh toán
public class ThanhToanDAO implements IGenericDAO<ThanhToan, String> {

    private static final String SQL_INSERT =
        "INSERT INTO ThanhToan " +
        "(MaThanhToan, MaHopDong, Thang, Nam, SoTien, NgayTaoBill, HanThanhToan, TrangThai) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, 'ChuaThanhToan')";

    private static final String SQL_UPDATE_THANHTOAN =
        "{CALL sp_ThanhToan_HoaDon(?, ?, ?, ?, ?)}";

    private static final String SQL_FIND_BY_HOPDONG =
        "SELECT tt.*, sv.HoTen AS HoTenSinhVien, p.TenPhong " +
        "FROM ThanhToan tt " +
        "INNER JOIN HopDong  hd ON tt.MaHopDong  = hd.MaHopDong " +
        "INNER JOIN SinhVien sv ON hd.MaSinhVien = sv.MaSinhVien " +
        "INNER JOIN Phong    p  ON hd.MaPhong    = p.MaPhong " +
        "WHERE tt.MaHopDong=? ORDER BY tt.Nam DESC, tt.Thang DESC";

    private static final String SQL_FIND_ALL =
        "SELECT tt.*, sv.HoTen AS HoTenSinhVien, p.TenPhong " +
        "FROM ThanhToan tt " +
        "INNER JOIN HopDong  hd ON tt.MaHopDong  = hd.MaHopDong " +
        "INNER JOIN SinhVien sv ON hd.MaSinhVien = sv.MaSinhVien " +
        "INNER JOIN Phong    p  ON hd.MaPhong    = p.MaPhong " +
        "ORDER BY tt.NgayTao DESC";

    private static final String SQL_COUNT   = "SELECT COUNT(*) FROM ThanhToan";
    private static final String SQL_DELETE  = "DELETE FROM ThanhToan WHERE MaThanhToan=?";

    private static final String SQL_FIND_BY_ID =
        "SELECT tt.*, sv.HoTen AS HoTenSinhVien, p.TenPhong " +
        "FROM ThanhToan tt " +
        "INNER JOIN HopDong hd ON tt.MaHopDong = hd.MaHopDong " +
        "INNER JOIN SinhVien sv ON hd.MaSinhVien = sv.MaSinhVien " +
        "INNER JOIN Phong p ON hd.MaPhong = p.MaPhong WHERE tt.MaThanhToan=?";

    private static final String SQL_UPDATE_TREHAN =
        "EXEC sp_CapNhat_HoaDonTreHan";

    @Override
    public boolean insert(ThanhToan tt) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_INSERT)) {
            ps.setString(1, tt.getMaThanhToan());
            ps.setString(2, tt.getMaHopDong());
            ps.setInt   (3, tt.getThang());
            ps.setInt   (4, tt.getNam());
            ps.setDouble(5, tt.getSoTien());
            ps.setDate  (6, Date.valueOf(tt.getNgayTaoBill() != null ? tt.getNgayTaoBill() : LocalDate.now()));
            ps.setDate  (7, Date.valueOf(tt.getHanThanhToan()));
            return ps.executeUpdate() > 0;
        }
    }

    // Ghi nhận thanh toán
    public boolean thanhToan(String maThanhToan, String phuongThuc, int nguoiXuLyID)
            throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement cs = conn.prepareCall(SQL_UPDATE_THANHTOAN)) {
            cs.setString(1, maThanhToan);
            cs.setString(2, phuongThuc);
            cs.setInt   (3, nguoiXuLyID);
            cs.registerOutParameter(4, Types.NVARCHAR);
            cs.registerOutParameter(5, Types.BIT);
            cs.execute();
            if (!cs.getBoolean(5)) throw new SQLException(cs.getString(4));
            return true;
        }
    }

    // Cập nhật hóa đơn trễ hạn
    public void capNhatTreHan() throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement()) {
            st.execute(SQL_UPDATE_TREHAN);
        }
    }

    @Override
    public boolean update(ThanhToan tt) throws SQLException { return false; } // Dùng SP

    @Override
    public boolean delete(String id) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_DELETE)) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public Optional<ThanhToan> findById(String id) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_ID)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    @Override
    public List<ThanhToan> findAll() throws SQLException {
        List<ThanhToan> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    // Lấy hóa đơn theo hợp đồng
    public List<ThanhToan> findByHopDong(String maHopDong) throws SQLException {
        List<ThanhToan> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_HOPDONG)) {
            ps.setString(1, maHopDong);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    @Override
    public int count() throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_COUNT);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    // Tự động tạo mã thanh toán
    public String generateNextId() throws SQLException {
        String sql = "SELECT 'TT' + FORMAT(YEAR(GETDATE()),'0000') + " +
                     "RIGHT('000' + CAST(ISNULL(MAX(CAST(RIGHT(MaThanhToan,3) AS INT)),0)+1 AS VARCHAR),3) " +
                     "FROM ThanhToan WHERE LEFT(MaThanhToan,6)='TT'+FORMAT(YEAR(GETDATE()),'0000')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next() && rs.getString(1) != null) return rs.getString(1);
        }
        return "TT" + java.time.Year.now().getValue() + "001";
    }

    // Ánh xạ dữ liệu
    private ThanhToan mapRow(ResultSet rs) throws SQLException {
        ThanhToan tt = new ThanhToan();
        tt.setMaThanhToan(rs.getString("MaThanhToan"));
        tt.setMaHopDong  (rs.getString("MaHopDong"));
        tt.setThang      (rs.getInt   ("Thang"));
        tt.setNam        (rs.getInt   ("Nam"));
        tt.setSoTien     (rs.getDouble("SoTien"));
        Date bill = rs.getDate("NgayTaoBill");
        if (bill != null) tt.setNgayTaoBill(bill.toLocalDate());
        Date han = rs.getDate("HanThanhToan");
        if (han != null) tt.setHanThanhToan(han.toLocalDate());
        Date tt2 = rs.getDate("NgayThanhToan");
        if (tt2 != null) tt.setNgayThanhToan(tt2.toLocalDate());
        tt.setTrangThai   (rs.getString("TrangThai"));
        tt.setPhuongThucTT(rs.getString("PhuongThucTT"));
        tt.setGhiChu      (rs.getString("GhiChu"));
        int nxl = rs.getInt("NguoiXuLyID");
        if (!rs.wasNull()) tt.setNguoiXuLyID(nxl);
        try { tt.setHoTenSinhVien(rs.getString("HoTenSinhVien")); } catch (Exception ignored) {}
        try { tt.setTenPhong     (rs.getString("TenPhong"));      } catch (Exception ignored) {}
        return tt;
    }
}