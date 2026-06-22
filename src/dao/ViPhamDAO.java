package dao;

import model.ViPham;
import utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// DAO xử lý Vi phạm
public class ViPhamDAO implements IGenericDAO<ViPham, String> {

    // ── Hằng số SQL ─────────────────────────────────────────────

    private static final String SQL_INSERT =
        "INSERT INTO ViPham " +
        "(MaViPham, MaSinhVien, MaHopDong, LoaiViPham, MoTa, NgayViPham, MucPhat, TrangThai, NguoiGhiNhanID) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, 'ChuaXuLy', ?)";

    private static final String SQL_UPDATE_STATUS =
        "UPDATE ViPham SET TrangThai=?, NgayCapNhat=GETDATE() WHERE MaViPham=?";

    private static final String SQL_DELETE =
        "DELETE FROM ViPham WHERE MaViPham=?";

    private static final String SQL_FIND_BY_ID =
        "SELECT vp.*, sv.HoTen AS HoTenSinhVien, p.TenPhong " +
        "FROM ViPham vp " +
        "INNER JOIN SinhVien sv ON vp.MaSinhVien = sv.MaSinhVien " +
        "LEFT JOIN  HopDong  hd ON vp.MaHopDong  = hd.MaHopDong " +
        "LEFT JOIN  Phong    p  ON hd.MaPhong    = p.MaPhong " +
        "WHERE vp.MaViPham=?";

    private static final String SQL_FIND_ALL =
        "SELECT vp.*, sv.HoTen AS HoTenSinhVien, p.TenPhong " +
        "FROM ViPham vp " +
        "INNER JOIN SinhVien sv ON vp.MaSinhVien = sv.MaSinhVien " +
        "LEFT JOIN  HopDong  hd ON vp.MaHopDong  = hd.MaHopDong " +
        "LEFT JOIN  Phong    p  ON hd.MaPhong    = p.MaPhong " +
        "ORDER BY vp.NgayTao DESC";

    private static final String SQL_FIND_BY_SV =
        "SELECT vp.*, sv.HoTen AS HoTenSinhVien, p.TenPhong " +
        "FROM ViPham vp " +
        "INNER JOIN SinhVien sv ON vp.MaSinhVien = sv.MaSinhVien " +
        "LEFT JOIN  HopDong  hd ON vp.MaHopDong  = hd.MaHopDong " +
        "LEFT JOIN  Phong    p  ON hd.MaPhong    = p.MaPhong " +
        "WHERE vp.MaSinhVien=? ORDER BY vp.NgayTao DESC";

    private static final String SQL_FIND_BY_STATUS =
        "SELECT vp.*, sv.HoTen AS HoTenSinhVien, p.TenPhong " +
        "FROM ViPham vp " +
        "INNER JOIN SinhVien sv ON vp.MaSinhVien = sv.MaSinhVien " +
        "LEFT JOIN  HopDong  hd ON vp.MaHopDong  = hd.MaHopDong " +
        "LEFT JOIN  Phong    p  ON hd.MaPhong    = p.MaPhong " +
        "WHERE vp.TrangThai=? ORDER BY vp.NgayTao DESC";

    private static final String SQL_SEARCH =
        "SELECT vp.*, sv.HoTen AS HoTenSinhVien, p.TenPhong " +
        "FROM ViPham vp " +
        "INNER JOIN SinhVien sv ON vp.MaSinhVien = sv.MaSinhVien " +
        "LEFT JOIN  HopDong  hd ON vp.MaHopDong  = hd.MaHopDong " +
        "LEFT JOIN  Phong    p  ON hd.MaPhong    = p.MaPhong " +
        "WHERE sv.HoTen LIKE ? OR vp.MaSinhVien LIKE ? OR vp.MaViPham LIKE ? " +
        "ORDER BY vp.NgayTao DESC";

    private static final String SQL_COUNT =
        "SELECT COUNT(*) FROM ViPham";

    // ── Thành phần CRUD ───────────────────────────────────────────

    @Override
    public boolean insert(ViPham vp) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_INSERT)) {

            ps.setString(1, vp.getMaViPham());
            ps.setString(2, vp.getMaSinhVien());
            if (vp.getMaHopDong() != null && !vp.getMaHopDong().isBlank())
                ps.setString(3, vp.getMaHopDong());
            else
                ps.setNull(3, Types.VARCHAR);
            ps.setString(4, vp.getLoaiViPham());
            ps.setString(5, vp.getMoTa());
            ps.setDate  (6, Date.valueOf(vp.getNgayViPham()));
            ps.setDouble(7, vp.getMucPhat());
            ps.setInt   (8, vp.getNguoiGhiNhanID());

            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean update(ViPham vp) throws SQLException {
        // Cập nhật trạng thái (nghiệp vụ chính)
        return updateTrangThai(vp.getMaViPham(), vp.getTrangThai());
    }

    // Cập nhật trạng thái vi phạm
    public boolean updateTrangThai(String maViPham, String trangThai) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_UPDATE_STATUS)) {

            ps.setString(1, trangThai);
            ps.setString(2, maViPham);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(String maViPham) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_DELETE)) {

            ps.setString(1, maViPham);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public Optional<ViPham> findById(String maViPham) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_ID)) {

            ps.setString(1, maViPham);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    @Override
    public List<ViPham> findAll() throws SQLException {
        List<ViPham> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_ALL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    // Lọc vi phạm theo trạng thái
    public List<ViPham> findByTrangThai(String trangThai) throws SQLException {
        List<ViPham> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_STATUS)) {

            ps.setString(1, trangThai);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    // Tìm kiếm vi phạm
    public List<ViPham> search(String keyword) throws SQLException {
        List<ViPham> list = new ArrayList<>();
        String pattern = "%" + keyword + "%";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_SEARCH)) {

            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    // Lấy vi phạm theo sinh viên
    public List<ViPham> findBySinhVien(String maSinhVien) throws SQLException {
        List<ViPham> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_SV)) {

            ps.setString(1, maSinhVien);
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

    // Tự động tạo mã vi phạm
    public synchronized String generateNextId() throws SQLException {
        String sql = "SELECT 'VP' + FORMAT(YEAR(GETDATE()),'0000') + " +
                     "RIGHT('000' + CAST(ISNULL(MAX(CAST(RIGHT(MaViPham,3) AS INT)),0)+1 AS VARCHAR),3) " +
                     "FROM ViPham WHERE LEFT(MaViPham,6)='VP'+FORMAT(YEAR(GETDATE()),'0000')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next() && rs.getString(1) != null) return rs.getString(1);
        }
        return "VP" + java.time.Year.now().getValue() + "001";
    }

    // Ánh xạ dữ liệu
    private ViPham mapRow(ResultSet rs) throws SQLException {
        ViPham vp = new ViPham();
        vp.setMaViPham      (rs.getString("MaViPham"));
        vp.setMaSinhVien    (rs.getString("MaSinhVien"));
        vp.setMaHopDong     (rs.getString("MaHopDong"));
        vp.setLoaiViPham    (rs.getString("LoaiViPham"));
        vp.setMoTa          (rs.getString("MoTa"));

        Date ngayVP = rs.getDate("NgayViPham");
        if (ngayVP != null) vp.setNgayViPham(ngayVP.toLocalDate());

        vp.setMucPhat       (rs.getDouble("MucPhat"));
        vp.setTrangThai     (rs.getString("TrangThai"));
        vp.setNguoiGhiNhanID(rs.getInt("NguoiGhiNhanID"));

        Timestamp ngayTao = rs.getTimestamp("NgayTao");
        if (ngayTao != null) vp.setNgayTao(ngayTao.toLocalDateTime());

        // Các trường hiển thị
        try { vp.setHoTenSinhVien(rs.getString("HoTenSinhVien")); } catch (Exception ignored) {}
        try { vp.setTenPhong     (rs.getString("TenPhong"));      } catch (Exception ignored) {}
        return vp;
    }
}
