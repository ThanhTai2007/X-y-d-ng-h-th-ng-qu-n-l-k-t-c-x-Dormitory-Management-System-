package dao;

import model.SinhVien;
import utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// DAO xử lý Sinh viên
public class SinhVienDAO implements IGenericDAO<SinhVien, String> {

    private static final String SQL_INSERT =
        "INSERT INTO SinhVien " +
        "(MaSinhVien, HoTen, NgaySinh, GioiTinh, CCCD, SoDienThoai, " +
        " Email, DiaChi, Khoa, NamHoc, TrangThai) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_UPDATE =
        "UPDATE SinhVien SET " +
        "HoTen=?, NgaySinh=?, GioiTinh=?, CCCD=?, SoDienThoai=?, " +
        "Email=?, DiaChi=?, Khoa=?, NamHoc=?, TrangThai=? " +
        "WHERE MaSinhVien=?";

    private static final String SQL_DELETE =
        "DELETE FROM SinhVien WHERE MaSinhVien=?";

    private static final String SQL_FIND_BY_ID =
        "SELECT * FROM SinhVien WHERE MaSinhVien=?";

    private static final String SQL_FIND_ALL =
        "SELECT * FROM SinhVien ORDER BY NgayTao DESC";

    private static final String SQL_COUNT =
        "SELECT COUNT(*) FROM SinhVien";

    private static final String SQL_SEARCH =
        "SELECT * FROM SinhVien " +
        "WHERE HoTen LIKE ? OR MaSinhVien LIKE ? OR CCCD LIKE ? " +
        "ORDER BY HoTen";

    private static final String SQL_FIND_BY_STATUS =
        "SELECT * FROM SinhVien WHERE TrangThai=? ORDER BY HoTen";

    private static final String SQL_EXISTS_CCCD =
        "SELECT COUNT(*) FROM SinhVien WHERE CCCD=? AND MaSinhVien<>?";

    private static final String SQL_EXISTS_EMAIL =
        "SELECT COUNT(*) FROM SinhVien WHERE Email=? AND MaSinhVien<>?";

    private static final String SQL_FIND_BY_PHONG =
        "SELECT sv.* FROM SinhVien sv " +
        "INNER JOIN HopDong hd ON sv.MaSinhVien = hd.MaSinhVien " +
        "WHERE hd.MaPhong=? AND hd.TrangThai='Active'";

    private static final String SQL_NEXT_ID =
        "SELECT 'SV' + FORMAT(YEAR(GETDATE()), '0000') + " +
        "RIGHT('000' + CAST(ISNULL(MAX(CAST(RIGHT(MaSinhVien,3) AS INT)),0)+1 AS VARCHAR),3) " +
        "FROM SinhVien WHERE LEFT(MaSinhVien,6) = 'SV' + FORMAT(YEAR(GETDATE()),'0000')";

    @Override
    public boolean insert(SinhVien sv) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_INSERT)) {

            ps.setString(1,  sv.getMaSinhVien());
            ps.setString(2,  sv.getHoTen());
            ps.setDate(3,    Date.valueOf(sv.getNgaySinh()));
            ps.setString(4,  sv.getGioiTinh());
            ps.setString(5,  sv.getCccd());
            ps.setString(6,  sv.getSoDienThoai());
            ps.setString(7,  sv.getEmail());
            ps.setString(8,  sv.getDiaChi());
            ps.setString(9,  sv.getKhoa());
            ps.setString(10, sv.getNamHoc());
            ps.setString(11, sv.getTrangThai() != null ? sv.getTrangThai() : "DangHoc");

            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean update(SinhVien sv) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_UPDATE)) {

            ps.setString(1,  sv.getHoTen());
            ps.setDate(2,    Date.valueOf(sv.getNgaySinh()));
            ps.setString(3,  sv.getGioiTinh());
            ps.setString(4,  sv.getCccd());
            ps.setString(5,  sv.getSoDienThoai());
            ps.setString(6,  sv.getEmail());
            ps.setString(7,  sv.getDiaChi());
            ps.setString(8,  sv.getKhoa());
            ps.setString(9,  sv.getNamHoc());
            ps.setString(10, sv.getTrangThai());
            ps.setString(11, sv.getMaSinhVien());

            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(String maSinhVien) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_DELETE)) {

            ps.setString(1, maSinhVien);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public Optional<SinhVien> findById(String maSinhVien) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_ID)) {

            ps.setString(1, maSinhVien);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    @Override
    public List<SinhVien> findAll() throws SQLException {
        List<SinhVien> list = new ArrayList<>();
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

    // Tìm kiếm sinh viên
    public List<SinhVien> search(String keyword) throws SQLException {
        List<SinhVien> list = new ArrayList<>();
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

    // Lọc sinh viên theo trạng thái
    public List<SinhVien> findByTrangThai(String trangThai) throws SQLException {
        List<SinhVien> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_STATUS)) {

            ps.setString(1, trangThai);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    // Lấy sinh viên theo phòng
    public List<SinhVien> findByPhong(String maPhong) throws SQLException {
        List<SinhVien> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_PHONG)) {

            ps.setString(1, maPhong);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    // Kiểm tra trùng CCCD
    public boolean isCCCDDuplicated(String cccd, String maSinhVienHienTai) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_EXISTS_CCCD)) {

            ps.setString(1, cccd);
            ps.setString(2, maSinhVienHienTai);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    // Kiểm tra trùng Email
    public boolean isEmailDuplicated(String email, String maSinhVienHienTai) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_EXISTS_EMAIL)) {

            ps.setString(1, email);
            ps.setString(2, maSinhVienHienTai);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    // Tự động tạo mã sinh viên
    public String generateNextId() throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_NEXT_ID);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) return rs.getString(1);
        }
        return "SV" + java.time.Year.now().getValue() + "001";
    }

    // Ánh xạ dữ liệu
    private SinhVien mapRow(ResultSet rs) throws SQLException {
        SinhVien sv = new SinhVien();
        sv.setMaSinhVien(rs.getString("MaSinhVien"));
        sv.setHoTen(rs.getString("HoTen"));

        Date ngaySinh = rs.getDate("NgaySinh");
        if (ngaySinh != null) sv.setNgaySinh(ngaySinh.toLocalDate());

        sv.setGioiTinh(rs.getString("GioiTinh"));
        sv.setCccd(rs.getString("CCCD"));
        sv.setSoDienThoai(rs.getString("SoDienThoai"));
        sv.setEmail(rs.getString("Email"));
        sv.setDiaChi(rs.getString("DiaChi"));
        sv.setKhoa(rs.getString("Khoa"));
        sv.setNamHoc(rs.getString("NamHoc"));
        sv.setTrangThai(rs.getString("TrangThai"));

        Timestamp ngayTao = rs.getTimestamp("NgayTao");
        if (ngayTao != null) sv.setNgayTao(ngayTao.toLocalDateTime());

        return sv;
    }
}
