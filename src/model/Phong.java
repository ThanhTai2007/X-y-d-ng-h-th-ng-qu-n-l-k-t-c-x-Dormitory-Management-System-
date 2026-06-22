package model;

import java.time.LocalDateTime;

// Model Phong
public class Phong {

    private String        maPhong;
    private String        tenPhong;
    private int           tang;
    private String        loaiPhong;
    private int           sucChua;
    private int           soNguoiHienTai;
    private double        giaThue;
    private String        moTa;
    private String        trangThai;
    private LocalDateTime ngayTao;
    private LocalDateTime ngayCapNhat;

    public Phong() {}

    public Phong(String maPhong, String tenPhong, int tang,
                 String loaiPhong, int sucChua, double giaThue, String moTa) {
        this.maPhong    = maPhong;
        this.tenPhong   = tenPhong;
        this.tang       = tang;
        this.loaiPhong  = loaiPhong;
        this.sucChua    = sucChua;
        this.giaThue    = giaThue;
        this.moTa       = moTa;
        this.soNguoiHienTai = 0;
        this.trangThai  = "Trong";
    }

    // Tính số chỗ trống
    public int getSoChoTrong() {
        return sucChua - soNguoiHienTai;
    }

    // Kiểm tra còn chỗ
    public boolean conCho() {
        return soNguoiHienTai < sucChua && !"BaoTri".equals(trangThai);
    }

    // Tính phần trăm lấp đầy
    public double getPhanTramLap() {
        if (sucChua == 0) return 0;
        return (soNguoiHienTai * 100.0) / sucChua;
    }

    public String getMaPhong()              { return maPhong; }
    public void   setMaPhong(String v)      { this.maPhong = v; }

    public String getTenPhong()             { return tenPhong; }
    public void   setTenPhong(String v)     { this.tenPhong = v; }

    public int  getTang()                   { return tang; }
    public void setTang(int v)              { this.tang = v; }

    public String getLoaiPhong()            { return loaiPhong; }
    public void   setLoaiPhong(String v)    { this.loaiPhong = v; }

    public int  getSucChua()                { return sucChua; }
    public void setSucChua(int v)           { this.sucChua = v; }

    public int  getSoNguoiHienTai()         { return soNguoiHienTai; }
    public void setSoNguoiHienTai(int v)    { this.soNguoiHienTai = v; }

    public double getGiaThue()              { return giaThue; }
    public void   setGiaThue(double v)      { this.giaThue = v; }

    public String getMoTa()                 { return moTa; }
    public void   setMoTa(String v)         { this.moTa = v; }

    public String getTrangThai()            { return trangThai; }
    public void   setTrangThai(String v)    { this.trangThai = v; }

    public LocalDateTime getNgayTao()                { return ngayTao; }
    public void          setNgayTao(LocalDateTime v) { this.ngayTao = v; }

    public LocalDateTime getNgayCapNhat()                { return ngayCapNhat; }
    public void          setNgayCapNhat(LocalDateTime v) { this.ngayCapNhat = v; }

    @Override
    public String toString() {
        return tenPhong + " [" + soNguoiHienTai + "/" + sucChua + "] - " + trangThai;
    }
}
