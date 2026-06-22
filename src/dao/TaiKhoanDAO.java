package dao;

import model.TaiKhoan;
import utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// DAO xử lý Tài khoản
public class TaiKhoanDAO implements IGenericDAO<TaiKhoan, Integer> {

    private static final String SQL_LOGIN =
        "SELECT tk.*, r.RoleName FROM TaiKhoan tk " +
        "INNER JOIN Role r ON tk.RoleID = r.RoleID " +
        "WHERE tk.Username=? AND tk.TrangThai='Active'";

    private static final String SQL_INSERT =
        "INSERT INTO TaiKhoan (Username, PasswordHash, HoTen, Email, SoDienThoai, RoleID, TrangThai) " +
        "VALUES (?, ?, ?, ?, ?, ?, 'Active')";

    private static final String SQL_UPDATE =
        "UPDATE TaiKhoan SET HoTen=?, Email=?, SoDienThoai=?, RoleID=?, TrangThai=? " +
        "WHERE TaiKhoanID=?";

    private static final String SQL_CHANGE_PASSWORD =
        "UPDATE TaiKhoan SET PasswordHash=? WHERE TaiKhoanID=?";

    private static final String SQL_DELETE =
        "UPDATE TaiKhoan SET TrangThai='Inactive' WHERE TaiKhoanID=?";  // Xóa mềm

    private static final String SQL_FIND_BY_ID =
        "SELECT tk.*, r.RoleName FROM TaiKhoan tk " +
        "INNER JOIN Role r ON tk.RoleID = r.RoleID " +
        "WHERE tk.TaiKhoanID=?";

    private static final String SQL_FIND_ALL =
        "SELECT tk.*, r.RoleName FROM TaiKhoan tk " +
        "INNER JOIN Role r ON tk.RoleID = r.RoleID " +
        "ORDER BY tk.NgayTao DESC";

    private static final String SQL_COUNT =
        "SELECT COUNT(*) FROM TaiKhoan WHERE TrangThai='Active'";

    private static final String SQL_EXISTS_USERNAME =
        "SELECT COUNT(*) FROM TaiKhoan WHERE Username=? AND TaiKhoanID<>?";

    private static final String SQL_EXISTS_USERNAME_NEW =
        "SELECT COUNT(*) FROM TaiKhoan WHERE Username=?";

    // Xác thực đăng nhập
    public Optional<TaiKhoan> findByUsername(String username) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_LOGIN)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    // Đổi mật khẩu
    public boolean changePassword(int taiKhoanID, String newHash) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_CHANGE_PASSWORD)) {
            ps.setString(1, newHash);
            ps.setInt   (2, taiKhoanID);
            return ps.executeUpdate() > 0;
        }
    }

    // Kiểm tra trùng tên đăng nhập (khi cập nhật)
    public boolean isUsernameDuplicated(String username, int taiKhoanID) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_EXISTS_USERNAME)) {
            ps.setString(1, username);
            ps.setInt   (2, taiKhoanID);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    // Kiểm tra trùng tên đăng nhập (khi tạo mới)
    public boolean isUsernameExists(String username) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_EXISTS_USERNAME_NEW)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    @Override
    public boolean insert(TaiKhoan tk) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_INSERT)) {
            ps.setString(1, tk.getUsername());
            ps.setString(2, tk.getPasswordHash());
            ps.setString(3, tk.getHoTen());
            ps.setString(4, tk.getEmail());
            ps.setString(5, tk.getSoDienThoai());
            ps.setInt   (6, tk.getRoleID());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean update(TaiKhoan tk) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_UPDATE)) {
            ps.setString(1, tk.getHoTen());
            ps.setString(2, tk.getEmail());
            ps.setString(3, tk.getSoDienThoai());
            ps.setInt   (4, tk.getRoleID());
            ps.setString(5, tk.getTrangThai());
            ps.setInt   (6, tk.getTaiKhoanID());
            return ps.executeUpdate() > 0;
        }
    }

    // Xóa mềm tài khoản
    @Override
    public boolean delete(Integer id) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_DELETE)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public Optional<TaiKhoan> findById(Integer id) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_ID)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    @Override
    public List<TaiKhoan> findAll() throws SQLException {
        List<TaiKhoan> list = new ArrayList<>();
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

    // Ánh xạ dữ liệu
    private TaiKhoan mapRow(ResultSet rs) throws SQLException {
        TaiKhoan tk = new TaiKhoan();
        tk.setTaiKhoanID  (rs.getInt   ("TaiKhoanID"));
        tk.setUsername    (rs.getString ("Username"));
        tk.setPasswordHash(rs.getString ("PasswordHash"));
        tk.setHoTen       (rs.getString ("HoTen"));
        tk.setEmail       (rs.getString ("Email"));
        tk.setSoDienThoai (rs.getString ("SoDienThoai"));
        tk.setRoleID      (rs.getInt    ("RoleID"));
        tk.setTrangThai   (rs.getString ("TrangThai"));
        try { tk.setRoleName(rs.getString("RoleName")); } catch (Exception ignored) {}
        Timestamp ngayTao = rs.getTimestamp("NgayTao");
        if (ngayTao != null) tk.setNgayTao(ngayTao.toLocalDateTime());
        return tk;
    }
}
