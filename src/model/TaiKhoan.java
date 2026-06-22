package model;

import java.time.LocalDateTime;

// Model TaiKhoan
public class TaiKhoan {
    private int           taiKhoanID;
    private String        username;
    private String        passwordHash;
    private String        hoTen;
    private String        email;
    private String        soDienThoai;
    private int           roleID;
    private String        roleName;
    private String        trangThai;
    private LocalDateTime ngayTao;

    public TaiKhoan() {}

    public int    getTaiKhoanID()              { return taiKhoanID; }
    public void   setTaiKhoanID(int v)         { this.taiKhoanID = v; }
    public String getUsername()                { return username; }
    public void   setUsername(String v)        { this.username = v; }
    public String getPasswordHash()            { return passwordHash; }
    public void   setPasswordHash(String v)    { this.passwordHash = v; }
    public String getHoTen()                   { return hoTen; }
    public void   setHoTen(String v)           { this.hoTen = v; }
    public String getEmail()                   { return email; }
    public void   setEmail(String v)           { this.email = v; }
    public String getSoDienThoai()             { return soDienThoai; }
    public void   setSoDienThoai(String v)     { this.soDienThoai = v; }
    public int    getRoleID()                  { return roleID; }
    public void   setRoleID(int v)             { this.roleID = v; }
    public String getRoleName()                { return roleName; }
    public void   setRoleName(String v)        { this.roleName = v; }
    public String getTrangThai()               { return trangThai; }
    public void   setTrangThai(String v)       { this.trangThai = v; }
    public LocalDateTime getNgayTao()          { return ngayTao; }
    public void setNgayTao(LocalDateTime v)    { this.ngayTao = v; }

    // Kiểm tra quyền Admin
    public boolean isAdmin() { return "Admin".equalsIgnoreCase(roleName); }
}
