package model;

import java.time.LocalDate;
import java.time.LocalDateTime;

// Model ViPham
public class ViPham {

    private String        maViPham;
    private String        maSinhVien;
    private String        maHopDong;      // Nullable
    private String        loaiViPham;
    private String        moTa;
    private LocalDate     ngayViPham;
    private double        mucPhat;
    private String        trangThai;      // ChuaXuLy | DaXuLy | DaKhangCao
    private int           nguoiGhiNhanID;
    private LocalDateTime ngayTao;
    private LocalDateTime ngayCapNhat;

    // Các trường hiển thị
    private String hoTenSinhVien;
    private String tenPhong;

    public ViPham() {}



    public String getMaViPham()                { return maViPham; }
    public void   setMaViPham(String v)        { this.maViPham = v; }

    public String getMaSinhVien()              { return maSinhVien; }
    public void   setMaSinhVien(String v)      { this.maSinhVien = v; }

    public String getMaHopDong()               { return maHopDong; }
    public void   setMaHopDong(String v)       { this.maHopDong = v; }

    public String getLoaiViPham()              { return loaiViPham; }
    public void   setLoaiViPham(String v)      { this.loaiViPham = v; }

    public String getMoTa()                    { return moTa; }
    public void   setMoTa(String v)            { this.moTa = v; }

    public LocalDate getNgayViPham()           { return ngayViPham; }
    public void      setNgayViPham(LocalDate v){ this.ngayViPham = v; }

    public double getMucPhat()                 { return mucPhat; }
    public void   setMucPhat(double v)         { this.mucPhat = v; }

    public String getTrangThai()               { return trangThai; }
    public void   setTrangThai(String v)       { this.trangThai = v; }

    public int  getNguoiGhiNhanID()            { return nguoiGhiNhanID; }
    public void setNguoiGhiNhanID(int v)       { this.nguoiGhiNhanID = v; }

    public LocalDateTime getNgayTao()                { return ngayTao; }
    public void          setNgayTao(LocalDateTime v) { this.ngayTao = v; }

    public LocalDateTime getNgayCapNhat()                { return ngayCapNhat; }
    public void          setNgayCapNhat(LocalDateTime v) { this.ngayCapNhat = v; }

    public String getHoTenSinhVien()              { return hoTenSinhVien; }
    public void   setHoTenSinhVien(String v)      { this.hoTenSinhVien = v; }

    public String getTenPhong()                   { return tenPhong; }
    public void   setTenPhong(String v)           { this.tenPhong = v; }
}
