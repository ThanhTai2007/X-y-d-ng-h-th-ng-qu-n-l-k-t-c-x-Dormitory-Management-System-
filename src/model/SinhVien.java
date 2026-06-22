package model;

import java.time.LocalDate;
import java.time.LocalDateTime;

// Model SinhVien
public class SinhVien {

    private String        maSinhVien;
    private String        hoTen;
    private LocalDate     ngaySinh;
    private String        gioiTinh;
    private String        cccd;
    private String        soDienThoai;
    private String        email;
    private String        diaChi;
    private String        khoa;
    private String        namHoc;
    private String        trangThai;
    private LocalDateTime ngayTao;
    private LocalDateTime ngayCapNhat;

    public SinhVien() {}

    public SinhVien(String maSinhVien, String hoTen, LocalDate ngaySinh,
                    String gioiTinh, String cccd, String soDienThoai,
                    String email, String diaChi, String khoa, String namHoc) {
        this.maSinhVien  = maSinhVien;
        this.hoTen       = hoTen;
        this.ngaySinh    = ngaySinh;
        this.gioiTinh    = gioiTinh;
        this.cccd        = cccd;
        this.soDienThoai = soDienThoai;
        this.email       = email;
        this.diaChi      = diaChi;
        this.khoa        = khoa;
        this.namHoc      = namHoc;
        this.trangThai   = "DangHoc";
    }

    public String getMaSinhVien()              { return maSinhVien; }
    public void   setMaSinhVien(String v)      { this.maSinhVien = v; }

    public String getHoTen()                   { return hoTen; }
    public void   setHoTen(String v)           { this.hoTen = v; }

    public LocalDate getNgaySinh()             { return ngaySinh; }
    public void      setNgaySinh(LocalDate v)  { this.ngaySinh = v; }

    public String getGioiTinh()                { return gioiTinh; }
    public void   setGioiTinh(String v)        { this.gioiTinh = v; }

    public String getCccd()                    { return cccd; }
    public void   setCccd(String v)            { this.cccd = v; }

    public String getSoDienThoai()             { return soDienThoai; }
    public void   setSoDienThoai(String v)     { this.soDienThoai = v; }

    public String getEmail()                   { return email; }
    public void   setEmail(String v)           { this.email = v; }

    public String getDiaChi()                  { return diaChi; }
    public void   setDiaChi(String v)          { this.diaChi = v; }

    public String getKhoa()                    { return khoa; }
    public void   setKhoa(String v)            { this.khoa = v; }

    public String getNamHoc()                  { return namHoc; }
    public void   setNamHoc(String v)          { this.namHoc = v; }

    public String getTrangThai()               { return trangThai; }
    public void   setTrangThai(String v)       { this.trangThai = v; }

    public LocalDateTime getNgayTao()               { return ngayTao; }
    public void          setNgayTao(LocalDateTime v){ this.ngayTao = v; }

    public LocalDateTime getNgayCapNhat()                { return ngayCapNhat; }
    public void          setNgayCapNhat(LocalDateTime v) { this.ngayCapNhat = v; }

    @Override
    public String toString() {
        return maSinhVien + " - " + hoTen;
    }
}
