package dao;

import utils.DatabaseConnection;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

// DAO thống kê bảng điều khiển
public class DashboardDAO {

    // Lấy các chỉ số thống kê
    public Map<String, Object> getStats() throws SQLException {
        Map<String, Object> stats = new HashMap<>();
        String sql = "SELECT * FROM vw_Dashboard";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                stats.put("tongSinhVien",        rs.getInt ("TongSinhVien"));
                stats.put("soPhongTrong",         rs.getInt ("SoPhongTrong"));
                stats.put("soPhongDay",           rs.getInt ("SoPhongDay"));
                stats.put("soPhongBaoTri",        rs.getInt ("SoPhongBaoTri"));
                stats.put("tongSoPhong",          rs.getInt ("TongSoPhong"));
                stats.put("hopDongDangHoatDong",  rs.getInt ("HopDongDangHoatDong"));
                stats.put("doanhThuNam",          rs.getLong("DoanhThuNam"));
                stats.put("doanhThuThang",        rs.getLong("DoanhThuThang"));
                stats.put("hoaDonTreHan",         rs.getInt ("HoaDonTreHan"));
                stats.put("viPhamChuaXuLy",       rs.getInt ("ViPhamChuaXuLy"));
            }
        }
        return stats;
    }

    // Lấy doanh thu 12 tháng
    public Map<String, Long> getDoanhThu12Thang() throws SQLException {
        Map<String, Long> data = new java.util.LinkedHashMap<>();
        String sql = "SELECT TOP 12 Nam, Thang, TongDoanhThu " +
                     "FROM vw_DoanhThu_TheoThang " +
                     "ORDER BY Nam DESC, Thang DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            java.util.List<Map.Entry<String,Long>> list = new java.util.ArrayList<>();
            while (rs.next()) {
                String key = "T" + rs.getInt("Thang") + "/" + rs.getInt("Nam");
                list.add(Map.entry(key, rs.getLong("TongDoanhThu")));
            }
            // Đảo thứ tự thời gian
            java.util.Collections.reverse(list);
            list.forEach(e -> data.put(e.getKey(), e.getValue()));
        }
        return data;
    }

    // Lấy hợp đồng sắp hết hạn
    public java.util.List<Map<String, Object>> getHopDongSapHetHan(int days) throws SQLException {
        java.util.List<Map<String, Object>> list = new java.util.ArrayList<>();
        String sql =
            "SELECT TOP 10 hd.MaHopDong, hd.MaSinhVien, sv.HoTen, hd.MaPhong, hd.NgayKetThuc, " +
            "       DATEDIFF(day, GETDATE(), hd.NgayKetThuc) AS SoNgayConLai " +
            "FROM HopDong hd " +
            "LEFT JOIN SinhVien sv ON hd.MaSinhVien = sv.MaSinhVien " +
            "WHERE hd.TrangThai = 'DangHoatDong' " +
            "  AND hd.NgayKetThuc >= CAST(GETDATE() AS DATE) " +
            "  AND hd.NgayKetThuc <= DATEADD(day, ?, CAST(GETDATE() AS DATE)) " +
            "ORDER BY hd.NgayKetThuc ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, days);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("maHopDong",    rs.getString("MaHopDong"));
                    row.put("maSinhVien",   rs.getString("MaSinhVien"));
                    row.put("hoTen",        rs.getString("HoTen"));
                    row.put("maPhong",      rs.getString("MaPhong"));
                    row.put("ngayKetThuc",  rs.getDate("NgayKetThuc") != null
                                            ? rs.getDate("NgayKetThuc").toLocalDate() : null);
                    row.put("soNgayConLai", rs.getInt("SoNgayConLai"));
                    list.add(row);
                }
            }
        }
        return list;
    }

    // Lấy hóa đơn quá hạn
    public java.util.List<Map<String, Object>> getHoaDonQuaHan() throws SQLException {
        java.util.List<Map<String, Object>> list = new java.util.ArrayList<>();
        String sql =
            "SELECT TOP 10 tt.MaThanhToan, tt.MaHopDong, sv.MaSinhVien, sv.HoTen, " +
            "       tt.SoTien, tt.HanThanhToan, " +
            "       DATEDIFF(day, tt.HanThanhToan, GETDATE()) AS SoNgayTre " +
            "FROM ThanhToan tt " +
            "LEFT JOIN HopDong hd ON tt.MaHopDong = hd.MaHopDong " +
            "LEFT JOIN SinhVien sv ON hd.MaSinhVien = sv.MaSinhVien " +
            "WHERE tt.TrangThai IN ('ChuaThanhToan','QuaHan') " +
            "  AND tt.HanThanhToan < CAST(GETDATE() AS DATE) " +
            "ORDER BY tt.HanThanhToan ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("maBill",       rs.getString("MaThanhToan"));
                row.put("maSinhVien",   rs.getString("MaSinhVien"));
                row.put("hoTen",        rs.getString("HoTen"));
                row.put("soTien",       rs.getLong("SoTien"));
                row.put("hanThanhToan", rs.getDate("HanThanhToan") != null
                                        ? rs.getDate("HanThanhToan").toLocalDate() : null);
                row.put("soNgayTre",    rs.getInt("SoNgayTre"));
                list.add(row);
            }
        }
        return list;
    }
}

