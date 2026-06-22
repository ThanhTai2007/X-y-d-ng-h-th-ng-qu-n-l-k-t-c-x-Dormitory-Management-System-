package model;

import java.time.LocalDate;
import java.time.LocalDateTime;

// Model ThanhToan
public class ThanhToan {
    private String        maThanhToan;
    private String        maHopDong;
    private int           thang;
    private int           nam;
    private double        soTien;
    private LocalDate     ngayTaoBill;
    private LocalDate     hanThanhToan;
    private LocalDate     ngayThanhToan;
    private String        trangThai;
    private String        phuongThucTT;
    private String        ghiChu;
    private Integer       nguoiXuLyID;
    private LocalDateTime ngayTao;

    // Các trường hiển thị
    private String hoTenSinhVien;
    private String tenPhong;

    public ThanhToan() {}

    // Kiểm tra trễ hạn
    public boolean isOverdue() {
        return "TreHan".equals(trangThai);
    }

    // Tính số ngày trễ hạn
    public long getSoNgayTreHan() {
        if (hanThanhToan == null) return 0;
        return java.time.temporal.ChronoUnit.DAYS.between(hanThanhToan, LocalDate.now());
    }

    public String getMaThanhToan()              { return maThanhToan; }
    public void   setMaThanhToan(String v)      { this.maThanhToan = v; }
    public String getMaHopDong()                { return maHopDong; }
    public void   setMaHopDong(String v)        { this.maHopDong = v; }
    public int    getThang()                    { return thang; }
    public void   setThang(int v)               { this.thang = v; }
    public int    getNam()                      { return nam; }
    public void   setNam(int v)                 { this.nam = v; }
    public double getSoTien()                   { return soTien; }
    public void   setSoTien(double v)           { this.soTien = v; }
    public LocalDate getNgayTaoBill()               { return ngayTaoBill; }
    public void      setNgayTaoBill(LocalDate v)    { this.ngayTaoBill = v; }
    public LocalDate getHanThanhToan()              { return hanThanhToan; }
    public void      setHanThanhToan(LocalDate v)   { this.hanThanhToan = v; }
    public LocalDate getNgayThanhToan()             { return ngayThanhToan; }
    public void      setNgayThanhToan(LocalDate v)  { this.ngayThanhToan = v; }
    public String getTrangThai()                { return trangThai; }
    public void   setTrangThai(String v)        { this.trangThai = v; }
    public String getPhuongThucTT()             { return phuongThucTT; }
    public void   setPhuongThucTT(String v)     { this.phuongThucTT = v; }
    public String getGhiChu()                   { return ghiChu; }
    public void   setGhiChu(String v)           { this.ghiChu = v; }
    public Integer getNguoiXuLyID()             { return nguoiXuLyID; }
    public void    setNguoiXuLyID(Integer v)    { this.nguoiXuLyID = v; }
    public LocalDateTime getNgayTao()                { return ngayTao; }
    public void          setNgayTao(LocalDateTime v) { this.ngayTao = v; }
    public String getHoTenSinhVien()            { return hoTenSinhVien; }
    public void   setHoTenSinhVien(String v)    { this.hoTenSinhVien = v; }
    public String getTenPhong()                 { return tenPhong; }
    public void   setTenPhong(String v)         { this.tenPhong = v; }
}
