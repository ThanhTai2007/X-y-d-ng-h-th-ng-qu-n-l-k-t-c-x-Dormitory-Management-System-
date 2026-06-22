package dao;

import model.HopDong;
import utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// DAO xử lý Hợp đồng
public class HopDongDAO implements IGenericDAO<HopDong, String> {

    private static final String SQL_INSERT_SP =
        "{CALL sp_TaoHopDong(?, ?, ?, ?, ?, ?, ?, ?, ?)}";

    private static final String SQL_UPDATE =
        "UPDATE HopDong SET TrangThai=?, NgayKetThuc=?, GhiChu=? " +
        "WHERE MaHopDong=?";

    private static final String SQL_DELETE =
        "DELETE FROM HopDong WHERE MaHopDong=?";

    private static final String SQL_FIND_BY_ID =
        "SELECT hd.*, sv.HoTen AS TenSinhVien, p.TenPhong " +
        "FROM HopDong hd " +
        "INNER JOIN SinhVien sv ON hd.MaSinhVien = sv.MaSinhVien " +
        "INNER JOIN Phong    p  ON hd.MaPhong    = p.MaPhong " +
        "WHERE hd.MaHopDong=?";

    private static final String SQL_FIND_ALL =
        "SELECT hd.*, sv.HoTen AS TenSinhVien, p.TenPhong " +
        "FROM HopDong hd " +
        "INNER JOIN SinhVien sv ON hd.MaSinhVien = sv.MaSinhVien " +
        "INNER JOIN Phong    p  ON hd.MaPhong    = p.MaPhong " +
        "ORDER BY hd.NgayTao DESC";

    private static final String SQL_COUNT =
        "SELECT COUNT(*) FROM HopDong";

    private static final String SQL_FIND_ACTIVE_BY_SV =
        "SELECT hd.*, sv.HoTen AS TenSinhVien, p.TenPhong " +
        "FROM HopDong hd " +
        "INNER JOIN SinhVien sv ON hd.MaSinhVien = sv.MaSinhVien " +
        "INNER JOIN Phong    p  ON hd.MaPhong    = p.MaPhong " +
        "WHERE hd.MaSinhVien=? AND hd.TrangThai='Active'";

    private static final String SQL_FIND_ACTIVE_ALL =
        "SELECT hd.*, sv.HoTen AS TenSinhVien, p.TenPhong " +
        "FROM HopDong hd " +
        "INNER JOIN SinhVien sv ON hd.MaSinhVien = sv.MaSinhVien " +
        "INNER JOIN Phong    p  ON hd.MaPhong    = p.MaPhong " +
        "WHERE hd.TrangThai='Active' ORDER BY hd.NgayBatDau";

    private static final String SQL_UPDATE_EXPIRED =
        "EXEC sp_CapNhat_HopDongHetHan";

    private static final String SQL_TERMINATE =
        "UPDATE HopDong SET TrangThai='Terminated', GhiChu=?, NgayCapNhat=GETDATE() " +
        "WHERE MaHopDong=? AND TrangThai='Active'";

    // Tạo hợp đồng
    @Override
    public boolean insert(HopDong hd) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement cs = conn.prepareCall(SQL_INSERT_SP)) {

            cs.setString(1, hd.getMaHopDong());
            cs.setString(2, hd.getMaSinhVien());
            cs.setString(3, hd.getMaPhong());
            cs.setDate  (4, Date.valueOf(hd.getNgayBatDau()));
            cs.setDate  (5, Date.valueOf(hd.getNgayKetThuc()));
            cs.setDouble(6, hd.getTienCoc());
            cs.setInt   (7, hd.getNguoiTaoID());
            cs.registerOutParameter(8, Types.NVARCHAR);   // @Message OUTPUT
            cs.registerOutParameter(9, Types.BIT);        // @Success OUTPUT

            cs.execute();

            boolean success = cs.getBoolean(9);
            if (!success) {
                // Báo lỗi từ database
                throw new SQLException(cs.getString(8));
            }
            return true;
        }
    }

    @Override
    public boolean update(HopDong hd) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_UPDATE)) {

            ps.setString(1, hd.getTrangThai());
            ps.setDate  (2, Date.valueOf(hd.getNgayKetThuc()));
            ps.setString(3, hd.getGhiChu());
            ps.setString(4, hd.getMaHopDong());

            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(String maHopDong) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_DELETE)) {

            ps.setString(1, maHopDong);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public Optional<HopDong> findById(String maHopDong) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_ID)) {

            ps.setString(1, maHopDong);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    @Override
    public List<HopDong> findAll() throws SQLException {
        List<HopDong> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_ALL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(mapRow(rs));
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

    // Lấy hợp đồng đang hiệu lực của sinh viên
    public Optional<HopDong> findActiveBySinhVien(String maSinhVien) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_ACTIVE_BY_SV)) {

            ps.setString(1, maSinhVien);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    // Lấy tất cả hợp đồng đang hiệu lực
    public List<HopDong> findAllActive() throws SQLException {
        List<HopDong> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_ACTIVE_ALL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    // Chấm dứt hợp đồng
    public boolean terminate(String maHopDong, String lyDo) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_TERMINATE)) {

            ps.setString(1, lyDo);
            ps.setString(2, maHopDong);
            return ps.executeUpdate() > 0;
        }
    }

    // Cập nhật hợp đồng hết hạn
    public void capNhatHopDongHetHan() throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement()) {
            st.execute(SQL_UPDATE_EXPIRED);
        }
    }

    // Tự động tạo mã hợp đồng
    public String generateNextId() throws SQLException {
        String sql = "SELECT 'HD' + FORMAT(YEAR(GETDATE()), '0000') + " +
                     "RIGHT('000' + CAST(ISNULL(MAX(CAST(RIGHT(MaHopDong,3) AS INT)),0)+1 AS VARCHAR),3) " +
                     "FROM HopDong WHERE LEFT(MaHopDong,6)='HD'+FORMAT(YEAR(GETDATE()),'0000')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next() && rs.getString(1) != null) return rs.getString(1);
        }
        return "HD" + java.time.Year.now().getValue() + "001";
    }

    // Ánh xạ dữ liệu
    private HopDong mapRow(ResultSet rs) throws SQLException {
        HopDong hd = new HopDong();
        hd.setMaHopDong  (rs.getString("MaHopDong"));
        hd.setMaSinhVien (rs.getString("MaSinhVien"));
        hd.setMaPhong    (rs.getString("MaPhong"));

        Date bd = rs.getDate("NgayBatDau");
        if (bd != null) hd.setNgayBatDau(bd.toLocalDate());

        Date kt = rs.getDate("NgayKetThuc");
        if (kt != null) hd.setNgayKetThuc(kt.toLocalDate());

        hd.setTienCoc   (rs.getDouble("TienCoc"));
        hd.setGiaThue   (rs.getDouble("GiaThue"));
        hd.setTrangThai (rs.getString("TrangThai"));
        hd.setGhiChu    (rs.getString("GhiChu"));
        hd.setNguoiTaoID(rs.getInt   ("NguoiTaoID"));

        Timestamp ngayTao = rs.getTimestamp("NgayTao");
        if (ngayTao != null) hd.setNgayTao(ngayTao.toLocalDateTime());

        // Các trường hiển thị
        try { hd.setTenSinhVien(rs.getString("TenSinhVien")); } catch (Exception ignored) {}
        try { hd.setTenPhong   (rs.getString("TenPhong"));    } catch (Exception ignored) {}

        return hd;
    }
}