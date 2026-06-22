package model;

import java.time.LocalDate;
import java.time.LocalDateTime;

// Model HopDong
public class HopDong {

    private String        maHopDong;
    private String        maSinhVien;
    private String        maPhong;
    private LocalDate     ngayBatDau;
    private LocalDate     ngayKetThuc;
    private double        tienCoc;
    private double        giaThue;
    private String        trangThai;
    private String        ghiChu;
    private int           nguoiTaoID;
    private LocalDateTime ngayTao;
    private LocalDateTime ngayCapNhat;

    // Các trường hiển thị
    private String tenSinhVien;
    private String tenPhong;

    public HopDong() {}

    public HopDong(String maHopDong, String maSinhVien, String maPhong,
                   LocalDate ngayBatDau, LocalDate ngayKetThuc,
                   double tienCoc, double giaThue, int nguoiTaoID) {
        this.maHopDong   = maHopDong;
        this.maSinhVien  = maSinhVien;
        this.maPhong     = maPhong;
        this.ngayBatDau  = ngayBatDau;
        this.ngayKetThuc = ngayKetThuc;
        this.tienCoc     = tienCoc;
        this.giaThue     = giaThue;
        this.nguoiTaoID  = nguoiTaoID;
        this.trangThai   = "Active";
    }

    // Kiểm tra hiệu lực
    public boolean isActive() {
        return "Active".equals(trangThai);
    }

    // Tính số tháng còn lại
    public long getSoThangConLai() {
        if (ngayKetThuc == null) return 0;
        return java.time.temporal.ChronoUnit.MONTHS.between(
                LocalDate.now(), ngayKetThuc);
    }

    public String getMaHopDong()               { return maHopDong; }
    public void   setMaHopDong(String v)       { this.maHopDong = v; }

    public String getMaSinhVien()              { return maSinhVien; }
    public void   setMaSinhVien(String v)      { this.maSinhVien = v; }

    public String getMaPhong()                 { return maPhong; }
    public void   setMaPhong(String v)         { this.maPhong = v; }

    public LocalDate getNgayBatDau()                { return ngayBatDau; }
    public void      setNgayBatDau(LocalDate v)     { this.ngayBatDau = v; }

    public LocalDate getNgayKetThuc()               { return ngayKetThuc; }
    public void      setNgayKetThuc(LocalDate v)    { this.ngayKetThuc = v; }

    public double getGiaThue()                 { return giaThue; }
    public void   setGiaThue(double v)         { this.giaThue = v; }

    public double getTienCoc()                 { return tienCoc; }
    public void   setTienCoc(double v)         { this.tienCoc = v; }

    public String getTrangThai()               { return trangThai; }
    public void   setTrangThai(String v)       { this.trangThai = v; }

    public String getGhiChu()                  { return ghiChu; }
    public void   setGhiChu(String v)          { this.ghiChu = v; }

    public int  getNguoiTaoID()                { return nguoiTaoID; }
    public void setNguoiTaoID(int v)           { this.nguoiTaoID = v; }

    public LocalDateTime getNgayTao()                { return ngayTao; }
    public void          setNgayTao(LocalDateTime v) { this.ngayTao = v; }

    public LocalDateTime getNgayCapNhat()                { return ngayCapNhat; }
    public void          setNgayCapNhat(LocalDateTime v) { this.ngayCapNhat = v; }

    public String getTenSinhVien()             { return tenSinhVien; }
    public void   setTenSinhVien(String v)     { this.tenSinhVien = v; }

    public String getTenPhong()                { return tenPhong; }
    public void   setTenPhong(String v)        { this.tenPhong = v; }

    @Override
    public String toString() {
        return maHopDong + " | " + maSinhVien + " → " + maPhong;
    }
}
