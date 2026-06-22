package dao;

import model.Phong;
import utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// DAO xử lý Phòng
public class PhongDAO implements IGenericDAO<Phong, String> {

    private static final String SQL_INSERT =
        "INSERT INTO Phong (MaPhong, TenPhong, Tang, LoaiPhong, SucChua, GiaThue, MoTa, TrangThai) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_UPDATE =
        "UPDATE Phong SET TenPhong=?, Tang=?, LoaiPhong=?, SucChua=?, " +
        "GiaThue=?, MoTa=?, TrangThai=? WHERE MaPhong=?";

    private static final String SQL_DELETE =
        "DELETE FROM Phong WHERE MaPhong=?";

    private static final String SQL_FIND_BY_ID =
        "SELECT * FROM Phong WHERE MaPhong=?";

    private static final String SQL_FIND_ALL =
        "SELECT * FROM Phong ORDER BY Tang, MaPhong";

    private static final String SQL_FIND_AVAILABLE =
        "SELECT * FROM Phong WHERE TrangThai='Trong' ORDER BY Tang, MaPhong";

    private static final String SQL_COUNT =
        "SELECT COUNT(*) FROM Phong";

    private static final String SQL_COUNT_BY_STATUS =
        "SELECT COUNT(*) FROM Phong WHERE TrangThai=?";

    private static final String SQL_UPDATE_TRANG_THAI =
        "UPDATE Phong SET TrangThai=? WHERE MaPhong=?";

    @Override
    public boolean insert(Phong p) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_INSERT)) {
            ps.setString(1, p.getMaPhong());
            ps.setString(2, p.getTenPhong());
            ps.setInt   (3, p.getTang());
            ps.setString(4, p.getLoaiPhong());
            ps.setInt   (5, p.getSucChua());
            ps.setDouble(6, p.getGiaThue());
            ps.setString(7, p.getMoTa());
            ps.setString(8, p.getTrangThai() != null ? p.getTrangThai() : "Trong");
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean update(Phong p) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_UPDATE)) {
            ps.setString(1, p.getTenPhong());
            ps.setInt   (2, p.getTang());
            ps.setString(3, p.getLoaiPhong());
            ps.setInt   (4, p.getSucChua());
            ps.setDouble(5, p.getGiaThue());
            ps.setString(6, p.getMoTa());
            ps.setString(7, p.getTrangThai());
            ps.setString(8, p.getMaPhong());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(String maPhong) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_DELETE)) {
            ps.setString(1, maPhong);
            return ps.executeUpdate() > 0;
            // Bị chặn nếu còn hợp đồng
        }
    }

    @Override
    public Optional<Phong> findById(String maPhong) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_ID)) {
            ps.setString(1, maPhong);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Phong> findAll() throws SQLException {
        List<Phong> list = new ArrayList<>();
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

    // Lấy phòng còn chỗ
    public List<Phong> findAvailable() throws SQLException {
        List<Phong> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_AVAILABLE);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    // Đếm số phòng theo trạng thái
    public int countByTrangThai(String trangThai) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_COUNT_BY_STATUS)) {
            ps.setString(1, trangThai);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    // Cập nhật trạng thái
    public boolean updateTrangThai(String maPhong, String trangThai) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_UPDATE_TRANG_THAI)) {
            ps.setString(1, trangThai);
            ps.setString(2, maPhong);
            return ps.executeUpdate() > 0;
        }
    }

    // Ánh xạ dữ liệu
    private Phong mapRow(ResultSet rs) throws SQLException {
        Phong p = new Phong();
        p.setMaPhong         (rs.getString("MaPhong"));
        p.setTenPhong        (rs.getString("TenPhong"));
        p.setTang            (rs.getInt   ("Tang"));
        p.setLoaiPhong       (rs.getString("LoaiPhong"));
        p.setSucChua         (rs.getInt   ("SucChua"));
        p.setSoNguoiHienTai  (rs.getInt   ("SoNguoiHienTai"));
        p.setGiaThue         (rs.getDouble("GiaThue"));
        p.setMoTa            (rs.getString("MoTa"));
        p.setTrangThai       (rs.getString("TrangThai"));
        Timestamp ngayTao = rs.getTimestamp("NgayTao");
        if (ngayTao != null) p.setNgayTao(ngayTao.toLocalDateTime());
        return p;
    }
}
