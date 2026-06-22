-- ╔══════════════════════════════════════════════════════════════╗
-- ║        DORMITORY MANAGEMENT SYSTEM — KTX MANAGER            ║
-- ║        Database Design  |  SQL Server 2019+                 ║
-- ║        Chuẩn: 3NF  |  Phiên bản: 2.0                       ║
-- ╚══════════════════════════════════════════════════════════════╝
--
--  Tác giả  : [Tên sinh viên]
--  MSSV     : [Mã sinh viên]
--  Môn học  : [Tên môn]
--  Ngày tạo : 2026-04-02
--
--  HƯỚNG DẪN CHẠY:
--    Mở SQL Server Management Studio (SSMS)
--    Chạy toàn bộ script này (F5)
--    Database DormitoryDB sẽ được tạo tự động
-- ═══════════════════════════════════════════════════════════════

USE master;
GO

-- ───────────────────────────────────────────────────────────────
-- BƯỚC 1: Tạo DATABASE
-- ───────────────────────────────────────────────────────────────
IF EXISTS (SELECT name FROM sys.databases WHERE name = 'DormitoryDB')
BEGIN
    ALTER DATABASE DormitoryDB SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE DormitoryDB;
END
GO

CREATE DATABASE DormitoryDB
    COLLATE Vietnamese_CI_AS;   -- Hỗ trợ tiếng Việt
GO

USE DormitoryDB;
GO

PRINT N'[1/6] Tạo database DormitoryDB thành công.';
GO

-- ═══════════════════════════════════════════════════════════════
--  PHẦN I — TẠO CÁC BẢNG (TABLES)
-- ═══════════════════════════════════════════════════════════════

-- ───────────────────────────────────────────────────────────────
-- BẢNG 1: Role — Vai trò trong hệ thống
-- ───────────────────────────────────────────────────────────────
-- Ý nghĩa: Lưu các loại vai trò (Admin, Staff).
--          Tách riêng để dễ mở rộng sau này (Superadmin, Viewer...).
-- Quan hệ: 1 Role → N TaiKhoan
-- ───────────────────────────────────────────────────────────────
CREATE TABLE Role (
    RoleID      INT           NOT NULL IDENTITY(1,1),
    RoleName    NVARCHAR(50)  NOT NULL,
    MoTa        NVARCHAR(255) NULL,
    NgayTao     DATETIME      NOT NULL DEFAULT GETDATE(),

    CONSTRAINT PK_Role     PRIMARY KEY (RoleID),
    CONSTRAINT UQ_RoleName UNIQUE (RoleName),
    CONSTRAINT CK_RoleName CHECK (RoleName IN (N'Admin', N'Staff'))
);
GO

-- ───────────────────────────────────────────────────────────────
-- BẢNG 2: TaiKhoan — Tài khoản đăng nhập hệ thống
-- ───────────────────────────────────────────────────────────────
-- Ý nghĩa: Xác thực và phân quyền người dùng.
--          PasswordHash dùng BCrypt (cost 12) — không lưu plain text.
-- Quan hệ: N TaiKhoan → 1 Role
-- ───────────────────────────────────────────────────────────────
CREATE TABLE TaiKhoan (
    TaiKhoanID   INT           NOT NULL IDENTITY(1,1),
    Username     VARCHAR(50)   NOT NULL,
    PasswordHash VARCHAR(255)  NOT NULL,
    HoTen        NVARCHAR(100) NOT NULL,
    Email        VARCHAR(100)  NULL,
    SoDienThoai  VARCHAR(15)   NULL,
    RoleID       INT           NOT NULL,
    TrangThai    VARCHAR(10)   NOT NULL DEFAULT 'Active',
    NgayTao      DATETIME      NOT NULL DEFAULT GETDATE(),
    NgayCapNhat  DATETIME      NULL,

    CONSTRAINT PK_TaiKhoan         PRIMARY KEY (TaiKhoanID),
    CONSTRAINT UQ_TaiKhoan_User    UNIQUE (Username),
    CONSTRAINT UQ_TaiKhoan_Email   UNIQUE (Email),
    CONSTRAINT FK_TaiKhoan_Role    FOREIGN KEY (RoleID)
                                   REFERENCES Role(RoleID),
    CONSTRAINT CK_TaiKhoan_Status  CHECK (TrangThai IN ('Active', 'Inactive')),
    CONSTRAINT CK_TaiKhoan_Phone   CHECK (
        SoDienThoai IS NULL
        OR (LEN(SoDienThoai) BETWEEN 9 AND 15
            AND SoDienThoai NOT LIKE '%[^0-9]%')
    )
);
GO

-- ───────────────────────────────────────────────────────────────
-- BẢNG 3: SinhVien — Hồ sơ sinh viên
-- ───────────────────────────────────────────────────────────────
-- Ý nghĩa: Thông tin đầy đủ của sinh viên đăng ký ở KTX.
--          CCCD UNIQUE tránh trùng hồ sơ.
--          AnhDaiDien lưu đường dẫn file ảnh (không nhúng binary).
-- Quan hệ: 1 SinhVien → N HopDong, N ViPham
-- ───────────────────────────────────────────────────────────────
CREATE TABLE SinhVien (
    MaSinhVien   VARCHAR(20)   NOT NULL,
    HoTen        NVARCHAR(100) NOT NULL,
    NgaySinh     DATE          NOT NULL,
    GioiTinh     NVARCHAR(10)  NOT NULL,
    CCCD         VARCHAR(12)   NOT NULL,
    SoDienThoai  VARCHAR(15)   NULL,
    Email        VARCHAR(100)  NULL,
    DiaChi       NVARCHAR(300) NULL,
    Khoa         NVARCHAR(100) NULL,
    NamHoc       VARCHAR(10)   NULL,
    AnhDaiDien   NVARCHAR(500) NULL,
    TrangThai    NVARCHAR(20)  NOT NULL DEFAULT N'DangHoc',
    NgayTao      DATETIME      NOT NULL DEFAULT GETDATE(),
    NgayCapNhat  DATETIME      NULL,

    CONSTRAINT PK_SinhVien          PRIMARY KEY (MaSinhVien),
    CONSTRAINT UQ_SinhVien_CCCD     UNIQUE (CCCD),
    CONSTRAINT UQ_SinhVien_Email    UNIQUE (Email),
    CONSTRAINT CK_SinhVien_GioiTinh CHECK (GioiTinh IN (N'Nam', N'Nữ', N'Khác')),
    CONSTRAINT CK_SinhVien_TrangThai CHECK (
        TrangThai IN (N'DangHoc', N'DaNghiHoc', N'TotNghiep')
    ),
    CONSTRAINT CK_SinhVien_CCCD     CHECK (LEN(CCCD) = 12),
    CONSTRAINT CK_SinhVien_NgaySinh CHECK (
        NgaySinh <= CAST(GETDATE() AS DATE)
        AND NgaySinh >= '1990-01-01'
    )
);
GO

-- ───────────────────────────────────────────────────────────────
-- BẢNG 4: Phong — Thông tin phòng ký túc xá
-- ───────────────────────────────────────────────────────────────
-- Ý nghĩa: Mỗi phòng có sức chứa tối đa.
--          TrangThai tự động cập nhật qua trigger khi HopDong thay đổi.
-- Business: SoNguoiHienTai <= SucChua (enforce tại DB layer)
-- Quan hệ : 1 Phong → N HopDong
-- ───────────────────────────────────────────────────────────────
CREATE TABLE Phong (
    MaPhong          VARCHAR(20)   NOT NULL,
    TenPhong         NVARCHAR(100) NOT NULL,
    Tang             INT           NOT NULL,
    LoaiPhong        NVARCHAR(20)  NOT NULL,
    SucChua          INT           NOT NULL,
    SoNguoiHienTai   INT           NOT NULL DEFAULT 0,
    GiaThue          DECIMAL(12,0) NOT NULL,
    MoTa             NVARCHAR(500) NULL,
    TrangThai        NVARCHAR(20)  NOT NULL DEFAULT N'Trong',
    NgayTao          DATETIME      NOT NULL DEFAULT GETDATE(),
    NgayCapNhat      DATETIME      NULL,

    CONSTRAINT PK_Phong           PRIMARY KEY (MaPhong),
    CONSTRAINT CK_Phong_SucChua   CHECK (SucChua BETWEEN 1 AND 20),
    CONSTRAINT CK_Phong_HienTai   CHECK (SoNguoiHienTai >= 0),
    CONSTRAINT CK_Phong_Overflow  CHECK (SoNguoiHienTai <= SucChua),
    CONSTRAINT CK_Phong_GiaThue   CHECK (GiaThue > 0),
    CONSTRAINT CK_Phong_Tang      CHECK (Tang BETWEEN 1 AND 30),
    CONSTRAINT CK_Phong_LoaiPhong CHECK (LoaiPhong IN (N'Don', N'Doi', N'Tap-the')),
    CONSTRAINT CK_Phong_TrangThai CHECK (TrangThai IN (N'Trong', N'Day', N'BaoTri'))
);
GO

-- ───────────────────────────────────────────────────────────────
-- BẢNG 5: HopDong — Hợp đồng thuê phòng  *** BẢNG TRUNG TÂM ***
-- ───────────────────────────────────────────────────────────────
-- Ý nghĩa: Ràng buộc sinh viên với phòng theo khoảng thời gian.
--          GiaThue lưu giá tại thời điểm ký (lịch sử giá – 3NF).
-- Business: Filtered Unique Index đảm bảo 1 SV chỉ có 1 HĐ Active.
-- Quan hệ : N HopDong → 1 SinhVien, 1 Phong, 1 TaiKhoan (người tạo)
--           1 HopDong → N ThanhToan, N ViPham
-- ───────────────────────────────────────────────────────────────
CREATE TABLE HopDong (
    MaHopDong    VARCHAR(20)   NOT NULL,
    MaSinhVien   VARCHAR(20)   NOT NULL,
    MaPhong      VARCHAR(20)   NOT NULL,
    NgayBatDau   DATE          NOT NULL,
    NgayKetThuc  DATE          NOT NULL,
    TienCoc      DECIMAL(12,0) NOT NULL DEFAULT 0,
    GiaThue      DECIMAL(12,0) NOT NULL,
    TrangThai    NVARCHAR(20)  NOT NULL DEFAULT N'Active',
    GhiChu       NVARCHAR(500) NULL,
    NguoiTaoID   INT           NOT NULL,
    NgayTao      DATETIME      NOT NULL DEFAULT GETDATE(),
    NgayCapNhat  DATETIME      NULL,

    CONSTRAINT PK_HopDong          PRIMARY KEY (MaHopDong),
    CONSTRAINT FK_HopDong_SinhVien FOREIGN KEY (MaSinhVien)
                                   REFERENCES SinhVien(MaSinhVien),
    CONSTRAINT FK_HopDong_Phong    FOREIGN KEY (MaPhong)
                                   REFERENCES Phong(MaPhong),
    CONSTRAINT FK_HopDong_NguoiTao FOREIGN KEY (NguoiTaoID)
                                   REFERENCES TaiKhoan(TaiKhoanID),
    CONSTRAINT CK_HopDong_NgayHD   CHECK (NgayKetThuc > NgayBatDau),
    CONSTRAINT CK_HopDong_TienCoc  CHECK (TienCoc >= 0),
    CONSTRAINT CK_HopDong_GiaThue  CHECK (GiaThue > 0),
    CONSTRAINT CK_HopDong_Status   CHECK (
        TrangThai IN (N'Active', N'Expired', N'Terminated', N'Pending')
    )
);
GO

-- ─── Filtered Unique Index ─────────────────────────────────────
-- Đảm bảo: 1 sinh viên CHỈ CÓ 1 hợp đồng ACTIVE tại 1 thời điểm
-- (Cho phép nhiều hợp đồng Expired/Terminated với cùng sinh viên)
-- ───────────────────────────────────────────────────────────────
CREATE UNIQUE INDEX UX_HopDong_1SV_1Active
    ON HopDong (MaSinhVien)
    WHERE TrangThai = N'Active';
GO

-- ───────────────────────────────────────────────────────────────
-- BẢNG 6: ThanhToan — Hóa đơn thanh toán hàng tháng
-- ───────────────────────────────────────────────────────────────
-- Ý nghĩa: Mỗi tháng phát sinh 1 hóa đơn cho mỗi hợp đồng.
--          UNIQUE (MaHopDong, Thang, Nam) tránh thu 2 lần/tháng.
--          TrangThai tự chuyển TreHan qua trigger khi quá hạn.
-- Quan hệ : N ThanhToan → 1 HopDong
-- ───────────────────────────────────────────────────────────────
CREATE TABLE ThanhToan (
    MaThanhToan   VARCHAR(20)   NOT NULL,
    MaHopDong     VARCHAR(20)   NOT NULL,
    Thang         INT           NOT NULL,
    Nam           INT           NOT NULL,
    SoTien        DECIMAL(12,0) NOT NULL,
    NgayTaoBill   DATE          NOT NULL DEFAULT CAST(GETDATE() AS DATE),
    HanThanhToan  DATE          NOT NULL,
    NgayThanhToan DATE          NULL,
    TrangThai     NVARCHAR(20)  NOT NULL DEFAULT N'ChuaThanhToan',
    PhuongThucTT  NVARCHAR(30)  NULL,
    GhiChu        NVARCHAR(500) NULL,
    NguoiXuLyID   INT           NULL,
    NgayTao       DATETIME      NOT NULL DEFAULT GETDATE(),
    NgayCapNhat   DATETIME      NULL,

    CONSTRAINT PK_ThanhToan             PRIMARY KEY (MaThanhToan),
    CONSTRAINT FK_ThanhToan_HopDong     FOREIGN KEY (MaHopDong)
                                        REFERENCES HopDong(MaHopDong),
    CONSTRAINT FK_ThanhToan_NguoiXuLy  FOREIGN KEY (NguoiXuLyID)
                                        REFERENCES TaiKhoan(TaiKhoanID),
    CONSTRAINT UQ_ThanhToan_KyThang    UNIQUE (MaHopDong, Thang, Nam),
    CONSTRAINT CK_ThanhToan_Thang      CHECK (Thang BETWEEN 1 AND 12),
    CONSTRAINT CK_ThanhToan_Nam        CHECK (Nam BETWEEN 2000 AND 2100),
    CONSTRAINT CK_ThanhToan_SoTien     CHECK (SoTien > 0),
    CONSTRAINT CK_ThanhToan_HanTT      CHECK (HanThanhToan >= NgayTaoBill),
    CONSTRAINT CK_ThanhToan_TrangThai  CHECK (
        TrangThai IN (N'DaThanhToan', N'ChuaThanhToan', N'TreHan')
    ),
    CONSTRAINT CK_ThanhToan_PhuongThuc CHECK (
        PhuongThucTT IS NULL
        OR PhuongThucTT IN (N'TienMat', N'ChuyenKhoan', N'The', N'Khac')
    )
);
GO

-- ───────────────────────────────────────────────────────────────
-- BẢNG 7: ViPham — Ghi nhận vi phạm nội quy
-- ───────────────────────────────────────────────────────────────
-- Ý nghĩa: Hệ thống theo dõi, cảnh báo và xử lý vi phạm.
--          MaHopDong có thể NULL nếu vi phạm ghi sau khi HĐ kết thúc.
-- Quan hệ : N ViPham → 1 SinhVien, 0..1 HopDong
-- ───────────────────────────────────────────────────────────────
CREATE TABLE ViPham (
    MaViPham         VARCHAR(20)    NOT NULL,
    MaSinhVien       VARCHAR(20)    NOT NULL,
    MaHopDong        VARCHAR(20)    NULL,
    LoaiViPham       NVARCHAR(50)   NOT NULL,
    MoTa             NVARCHAR(1000) NOT NULL,
    NgayViPham       DATE           NOT NULL,
    MucPhat          DECIMAL(12,0)  NOT NULL DEFAULT 0,
    TrangThai        NVARCHAR(20)   NOT NULL DEFAULT N'ChuaXuLy',
    NguoiGhiNhanID   INT            NOT NULL,
    NgayTao          DATETIME       NOT NULL DEFAULT GETDATE(),
    NgayCapNhat      DATETIME       NULL,

    CONSTRAINT PK_ViPham               PRIMARY KEY (MaViPham),
    CONSTRAINT FK_ViPham_SinhVien      FOREIGN KEY (MaSinhVien)
                                       REFERENCES SinhVien(MaSinhVien),
    CONSTRAINT FK_ViPham_HopDong       FOREIGN KEY (MaHopDong)
                                       REFERENCES HopDong(MaHopDong),
    CONSTRAINT FK_ViPham_NguoiGhiNhan  FOREIGN KEY (NguoiGhiNhanID)
                                       REFERENCES TaiKhoan(TaiKhoanID),
    CONSTRAINT CK_ViPham_MucPhat       CHECK (MucPhat >= 0),
    CONSTRAINT CK_ViPham_NgayVP        CHECK (NgayViPham <= CAST(GETDATE() AS DATE)),
    CONSTRAINT CK_ViPham_TrangThai     CHECK (
        TrangThai IN (N'ChuaXuLy', N'DaXuLy', N'DaKhangCao')
    ),
    CONSTRAINT CK_ViPham_LoaiViPham    CHECK (
        LoaiViPham IN (
            N'VeTreeHan', N'PhaHoaiTaiSan', N'GayOnAo',
            N'SuDungChatCam', N'TroChuaPhep', N'MangKhach',
            N'MatVeSinh', N'Khac'
        )
    )
);
GO

PRINT N'[2/6] Tạo 7 bảng thành công.';
GO

-- ═══════════════════════════════════════════════════════════════
--  PHẦN II — TRIGGERS (Tự động hoá nghiệp vụ)
-- ═══════════════════════════════════════════════════════════════

-- ───────────────────────────────────────────────────────────────
-- TRIGGER 1: Tăng SoNguoiHienTai khi thêm HopDong Active
-- ───────────────────────────────────────────────────────────────
CREATE OR ALTER TRIGGER trg_HD_Insert_UpdatePhong
ON HopDong
AFTER INSERT
AS
BEGIN
    SET NOCOUNT ON;

    -- Chỉ xử lý khi HĐ có trạng thái Active
    UPDATE P
    SET    P.SoNguoiHienTai = P.SoNguoiHienTai + 1,
           P.TrangThai      = CASE
                                  WHEN P.SoNguoiHienTai + 1 >= P.SucChua THEN N'Day'
                                  ELSE N'Trong'
                              END,
           P.NgayCapNhat    = GETDATE()
    FROM   Phong P
    INNER JOIN inserted I ON P.MaPhong = I.MaPhong
    WHERE  I.TrangThai = N'Active';
END;
GO

-- ───────────────────────────────────────────────────────────────
-- TRIGGER 2: Giảm SoNguoiHienTai khi HopDong chuyển Expired/Terminated
-- ───────────────────────────────────────────────────────────────
CREATE OR ALTER TRIGGER trg_HD_Update_UpdatePhong
ON HopDong
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;

    IF UPDATE(TrangThai)
    BEGIN
        UPDATE P
        SET    P.SoNguoiHienTai = P.SoNguoiHienTai - 1,
               P.TrangThai      = CASE
                                      WHEN (P.SoNguoiHienTai - 1) <= 0 THEN N'Trong'
                                      WHEN (P.SoNguoiHienTai - 1) < P.SucChua THEN N'Trong'
                                      ELSE N'Day'
                                  END,
               P.NgayCapNhat    = GETDATE()
        FROM   Phong P
        INNER JOIN inserted D ON P.MaPhong = D.MaPhong
        INNER JOIN deleted  O ON O.MaHopDong = D.MaHopDong
        WHERE  O.TrangThai = N'Active'
          AND  D.TrangThai IN (N'Expired', N'Terminated');
    END
END;
GO

-- ───────────────────────────────────────────────────────────────
-- TRIGGER 3: Tự động cập nhật NgayCapNhat cho TaiKhoan
-- ───────────────────────────────────────────────────────────────
CREATE OR ALTER TRIGGER trg_TaiKhoan_UpdateTimestamp
ON TaiKhoan
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE TaiKhoan
    SET NgayCapNhat = GETDATE()
    WHERE TaiKhoanID IN (SELECT TaiKhoanID FROM inserted);
END;
GO

-- ───────────────────────────────────────────────────────────────
-- TRIGGER 4: Tự động cập nhật NgayCapNhat cho SinhVien
-- ───────────────────────────────────────────────────────────────
CREATE OR ALTER TRIGGER trg_SinhVien_UpdateTimestamp
ON SinhVien
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE SinhVien
    SET NgayCapNhat = GETDATE()
    WHERE MaSinhVien IN (SELECT MaSinhVien FROM inserted);
END;
GO

PRINT N'[3/6] Tạo 4 triggers thành công.';
GO

-- ═══════════════════════════════════════════════════════════════
--  PHẦN III — VIEWS (Phục vụ báo cáo & Dashboard)
-- ═══════════════════════════════════════════════════════════════

-- ───────────────────────────────────────────────────────────────
-- VIEW 1: Thống kê Dashboard tổng quan
-- ───────────────────────────────────────────────────────────────
CREATE OR ALTER VIEW vw_Dashboard
AS
SELECT
    (SELECT COUNT(*) FROM SinhVien WHERE TrangThai = N'DangHoc')         AS TongSinhVien,
    (SELECT COUNT(*) FROM Phong    WHERE TrangThai = N'Trong')           AS SoPhongTrong,
    (SELECT COUNT(*) FROM Phong    WHERE TrangThai = N'Day')             AS SoPhongDay,
    (SELECT COUNT(*) FROM Phong    WHERE TrangThai = N'BaoTri')          AS SoPhongBaoTri,
    (SELECT COUNT(*) FROM Phong)                                          AS TongSoPhong,
    (SELECT COUNT(*) FROM HopDong  WHERE TrangThai = N'Active')          AS HopDongDangHoatDong,
    (SELECT ISNULL(SUM(SoTien), 0)
     FROM ThanhToan
     WHERE TrangThai = N'DaThanhToan'
       AND Nam = YEAR(GETDATE()))                                          AS DoanhThuNam,
    (SELECT ISNULL(SUM(SoTien), 0)
     FROM ThanhToan
     WHERE TrangThai = N'DaThanhToan'
       AND Thang = MONTH(GETDATE())
       AND Nam = YEAR(GETDATE()))                                          AS DoanhThuThang,
    (SELECT COUNT(*) FROM ThanhToan WHERE TrangThai = N'TreHan')         AS HoaDonTreHan,
    (SELECT COUNT(*) FROM ViPham   WHERE TrangThai = N'ChuaXuLy')        AS ViPhamChuaXuLy;
GO

-- ───────────────────────────────────────────────────────────────
-- VIEW 2: Danh sách sinh viên kèm thông tin phòng hiện tại
-- ───────────────────────────────────────────────────────────────
CREATE OR ALTER VIEW vw_SinhVien_PhongHienTai
AS
SELECT
    sv.MaSinhVien,
    sv.HoTen,
    sv.NgaySinh,
    sv.GioiTinh,
    sv.CCCD,
    sv.SoDienThoai,
    sv.Email,
    sv.Khoa,
    sv.NamHoc,
    sv.TrangThai       AS TrangThaiSV,
    hd.MaHopDong,
    hd.MaPhong,
    p.TenPhong,
    p.Tang,
    p.LoaiPhong,
    hd.NgayBatDau,
    hd.NgayKetThuc,
    hd.GiaThue,
    hd.TrangThai       AS TrangThaiHD
FROM SinhVien  sv
LEFT JOIN HopDong hd ON sv.MaSinhVien = hd.MaSinhVien
                     AND hd.TrangThai = N'Active'
LEFT JOIN Phong   p  ON hd.MaPhong = p.MaPhong;
GO

-- ───────────────────────────────────────────────────────────────
-- VIEW 3: Doanh thu theo tháng (cho biểu đồ)
-- ───────────────────────────────────────────────────────────────
CREATE OR ALTER VIEW vw_DoanhThu_TheoThang
AS
SELECT
    Nam,
    Thang,
    SUM(SoTien)               AS TongDoanhThu,
    COUNT(*)                  AS SoHoaDon,
    SUM(CASE WHEN TrangThai = N'DaThanhToan'  THEN SoTien ELSE 0 END) AS DaThanhToan,
    SUM(CASE WHEN TrangThai = N'ChuaThanhToan' THEN SoTien ELSE 0 END) AS ChuaThanhToan,
    SUM(CASE WHEN TrangThai = N'TreHan'        THEN SoTien ELSE 0 END) AS TreHan
FROM ThanhToan
GROUP BY Nam, Thang;
GO

-- ───────────────────────────────────────────────────────────────
-- VIEW 4: Trạng thái sức chứa từng phòng
-- ───────────────────────────────────────────────────────────────
CREATE OR ALTER VIEW vw_Phong_TrangThai
AS
SELECT
    p.MaPhong,
    p.TenPhong,
    p.Tang,
    p.LoaiPhong,
    p.SucChua,
    p.SoNguoiHienTai,
    (p.SucChua - p.SoNguoiHienTai)   AS SoChoTrong,
    CAST(
        p.SoNguoiHienTai * 100.0 / p.SucChua
    AS DECIMAL(5,1))                  AS PhanTramLap,
    p.GiaThue,
    p.TrangThai
FROM Phong p;
GO

-- ───────────────────────────────────────────────────────────────
-- VIEW 5: Hóa đơn cần thu & quá hạn
-- ───────────────────────────────────────────────────────────────
CREATE OR ALTER VIEW vw_HoaDon_CanThu
AS
SELECT
    tt.MaThanhToan,
    tt.MaHopDong,
    sv.MaSinhVien,
    sv.HoTen,
    sv.SoDienThoai,
    p.TenPhong,
    tt.Thang,
    tt.Nam,
    tt.SoTien,
    tt.HanThanhToan,
    tt.TrangThai,
    DATEDIFF(DAY, tt.HanThanhToan, GETDATE()) AS SoNgayTreHan
FROM ThanhToan tt
INNER JOIN HopDong  hd ON tt.MaHopDong = hd.MaHopDong
INNER JOIN SinhVien sv ON hd.MaSinhVien = sv.MaSinhVien
INNER JOIN Phong    p  ON hd.MaPhong    = p.MaPhong
WHERE tt.TrangThai IN (N'ChuaThanhToan', N'TreHan');
GO

PRINT N'[4/6] Tạo 5 views thành công.';
GO

-- ═══════════════════════════════════════════════════════════════
--  PHẦN IV — STORED PROCEDURES
-- ═══════════════════════════════════════════════════════════════

-- ───────────────────────────────────────────────────────────────
-- SP 1: Tạo hợp đồng mới (có kiểm tra business rules)
-- ───────────────────────────────────────────────────────────────
CREATE OR ALTER PROCEDURE sp_TaoHopDong
    @MaHopDong   VARCHAR(20),
    @MaSinhVien  VARCHAR(20),
    @MaPhong     VARCHAR(20),
    @NgayBatDau  DATE,
    @NgayKetThuc DATE,
    @TienCoc     DECIMAL(12,0),
    @NguoiTaoID  INT,
    @Message     NVARCHAR(500) OUTPUT,
    @Success     BIT           OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    SET @Success = 0;

    -- Rule 1: Sinh viên đã có hợp đồng Active chưa?
    IF EXISTS (
        SELECT 1 FROM HopDong
        WHERE MaSinhVien = @MaSinhVien
          AND TrangThai  = N'Active'
    )
    BEGIN
        SET @Message = N'Sinh viên đã có hợp đồng đang hoạt động. Không thể tạo thêm.';
        RETURN;
    END

    -- Rule 2: Phòng còn chỗ không?
    IF NOT EXISTS (
        SELECT 1 FROM Phong
        WHERE MaPhong = @MaPhong
          AND SoNguoiHienTai < SucChua
          AND TrangThai != N'BaoTri'
    )
    BEGIN
        SET @Message = N'Phòng đã đầy hoặc đang bảo trì. Không thể tạo hợp đồng.';
        RETURN;
    END

    -- Rule 3: Ngày kết thúc phải sau ngày bắt đầu
    IF @NgayKetThuc <= @NgayBatDau
    BEGIN
        SET @Message = N'Ngày kết thúc phải sau ngày bắt đầu.';
        RETURN;
    END

    -- Lấy giá thuê từ Phong
    DECLARE @GiaThue DECIMAL(12,0);
    SELECT @GiaThue = GiaThue FROM Phong WHERE MaPhong = @MaPhong;

    -- Tạo hợp đồng
    INSERT INTO HopDong (
        MaHopDong, MaSinhVien, MaPhong,
        NgayBatDau, NgayKetThuc,
        TienCoc, GiaThue, TrangThai, NguoiTaoID
    )
    VALUES (
        @MaHopDong, @MaSinhVien, @MaPhong,
        @NgayBatDau, @NgayKetThuc,
        @TienCoc, @GiaThue, N'Active', @NguoiTaoID
    );

    SET @Success = 1;
    SET @Message = N'Tạo hợp đồng thành công.';
END;
GO

-- ───────────────────────────────────────────────────────────────
-- SP 2: Cập nhật hóa đơn quá hạn (chạy định kỳ)
-- ───────────────────────────────────────────────────────────────
CREATE OR ALTER PROCEDURE sp_CapNhat_HoaDonTreHan
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @Count INT;

    UPDATE ThanhToan
    SET    TrangThai   = N'TreHan',
           NgayCapNhat = GETDATE()
    WHERE  TrangThai   = N'ChuaThanhToan'
      AND  HanThanhToan < CAST(GETDATE() AS DATE);

    SET @Count = @@ROWCOUNT;
    PRINT CONCAT(N'Đã cập nhật ', @Count, N' hóa đơn sang trạng thái Trễ hạn.');
END;
GO

-- ───────────────────────────────────────────────────────────────
-- SP 3: Cập nhật hợp đồng hết hạn (chạy định kỳ)
-- ───────────────────────────────────────────────────────────────
CREATE OR ALTER PROCEDURE sp_CapNhat_HopDongHetHan
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @Count INT;

    -- Cập nhật HĐ đã hết hạn → Expired
    UPDATE HopDong
    SET    TrangThai   = N'Expired',
           NgayCapNhat = GETDATE()
    WHERE  TrangThai   = N'Active'
      AND  NgayKetThuc < CAST(GETDATE() AS DATE);

    SET @Count = @@ROWCOUNT;
    PRINT CONCAT(N'Đã cập nhật ', @Count, N' hợp đồng sang trạng thái Expired.');
END;
GO

-- ───────────────────────────────────────────────────────────────
-- SP 4: Thanh toán hóa đơn
-- ───────────────────────────────────────────────────────────────
CREATE OR ALTER PROCEDURE sp_ThanhToan_HoaDon
    @MaThanhToan  VARCHAR(20),
    @PhuongThucTT NVARCHAR(30),
    @NguoiXuLyID  INT,
    @Message      NVARCHAR(500) OUTPUT,
    @Success      BIT           OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    SET @Success = 0;

    -- Kiểm tra tồn tại & chưa thanh toán
    IF NOT EXISTS (
        SELECT 1 FROM ThanhToan
        WHERE MaThanhToan = @MaThanhToan
          AND TrangThai   IN (N'ChuaThanhToan', N'TreHan')
    )
    BEGIN
        SET @Message = N'Hóa đơn không tồn tại hoặc đã được thanh toán.';
        RETURN;
    END

    UPDATE ThanhToan
    SET    TrangThai     = N'DaThanhToan',
           NgayThanhToan = CAST(GETDATE() AS DATE),
           PhuongThucTT  = @PhuongThucTT,
           NguoiXuLyID   = @NguoiXuLyID,
           NgayCapNhat    = GETDATE()
    WHERE  MaThanhToan   = @MaThanhToan;

    SET @Success = 1;
    SET @Message = N'Thanh toán thành công.';
END;
GO

PRINT N'[5/6] Tạo 4 stored procedures thành công.';
GO

-- ═══════════════════════════════════════════════════════════════
--  PHẦN V — SEED DATA (Dữ liệu mẫu)
-- ═══════════════════════════════════════════════════════════════

-- ── Roles ────────────────────────────────────────────────────────
INSERT INTO Role (RoleName, MoTa) VALUES
    (N'Admin', N'Quản trị viên: toàn quyền hệ thống'),
    (N'Staff', N'Nhân viên: quản lý hợp đồng, thanh toán, vi phạm');
GO

-- ── TaiKhoan ─────────────────────────────────────────────────────
-- Lưu ý: PasswordHash dưới đây là BCrypt của "Admin@123" và "Staff@123"
-- Trong ứng dụng Java, dùng: BCrypt.hashpw("Admin@123", BCrypt.gensalt(12))
INSERT INTO TaiKhoan (Username, PasswordHash, HoTen, Email, SoDienThoai, RoleID) VALUES
    ('admin',   '$2a$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.',
                N'Nguyễn Quản Trị', 'admin@ktx.edu.vn', '0901234567', 1),
    ('staff01', '$2a$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.',
                N'Trần Thị Nhân Viên', 'staff01@ktx.edu.vn', '0912345678', 2),
    ('staff02', '$2a$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.',
                N'Lê Văn Hỗ Trợ', 'staff02@ktx.edu.vn', '0923456789', 2);
GO

-- ── Phong ─────────────────────────────────────────────────────────
INSERT INTO Phong (MaPhong, TenPhong, Tang, LoaiPhong, SucChua, GiaThue, MoTa) VALUES
    ('P101', N'Phòng 101', 1, N'Tap-the', 6, 800000,  N'Phòng tập thể tầng 1, ban công hướng Đông'),
    ('P102', N'Phòng 102', 1, N'Tap-the', 6, 800000,  N'Phòng tập thể tầng 1, có nhà vệ sinh riêng'),
    ('P103', N'Phòng 103', 1, N'Tap-the', 6, 800000,  N'Phòng tập thể tầng 1'),
    ('P201', N'Phòng 201', 2, N'Doi',     2, 1200000, N'Phòng đôi tầng 2, điều hòa, nóng lạnh'),
    ('P202', N'Phòng 202', 2, N'Doi',     2, 1200000, N'Phòng đôi tầng 2'),
    ('P203', N'Phòng 203', 2, N'Doi',     2, 1200000, N'Phòng đôi tầng 2, view đẹp'),
    ('P301', N'Phòng 301', 3, N'Don',     1, 1800000, N'Phòng đơn tầng 3, đầy đủ tiện nghi'),
    ('P302', N'Phòng 302', 3, N'Don',     1, 1800000, N'Phòng đơn tầng 3'),
    ('P303', N'Phòng 303', 3, N'Don',     1, 1800000, N'Đang bảo trì — sửa chữa hệ thống điện');
GO

-- Đặt P303 vào chế độ bảo trì
UPDATE Phong SET TrangThai = N'BaoTri' WHERE MaPhong = 'P303';
GO

-- ── SinhVien ─────────────────────────────────────────────────────
INSERT INTO SinhVien (MaSinhVien, HoTen, NgaySinh, GioiTinh, CCCD, SoDienThoai, Email, DiaChi, Khoa, NamHoc, TrangThai) VALUES
    ('SV2024001', N'Nguyễn Văn An',        '2005-03-15', N'Nam', '001205012345', '0911111111', 'an.nguyen@sv.edu.vn',      N'Hà Nội',    N'Khoa Khoa học Máy tính',                        '2023-2027', N'DangHoc'),
    ('SV2024002', N'Trần Thị Bình',        '2006-08-20', N'Nữ',  '001206056789', '0922222222', 'binh.tran@sv.edu.vn',      N'Hà Nội',    N'Khoa Kinh tế số và Thương mại điện tử',         '2024-2028', N'DangHoc'),
    ('SV2024003', N'Lê Minh Châu',         '2005-12-01', N'Nam', '001205098765', '0933333333', 'chau.le@sv.edu.vn',        N'Thái Bình', N'Khoa Kỹ thuật Máy tính và Điện tử',             '2023-2027', N'DangHoc'),
    ('SV2024004', N'Phạm Thị Dung',        '2007-03-25', N'Nữ',  '001207011223', '0944444444', 'dung.pham@sv.edu.vn',      N'Nam Định',  N'Khoa Khoa học Máy tính',                        '2024-2028', N'DangHoc'),
    ('SV2024005', N'Hoàng Văn Em',         '2006-07-10', N'Nam', '001206034567', '0955555555', 'em.hoang@sv.edu.vn',       N'Hải Phòng', N'Khoa Kỹ thuật Máy tính và Điện tử',             '2024-2028', N'DaNghiHoc'),
    ('SV2024006', N'Vũ Thị Phương',        '2005-11-30', N'Nữ',  '001205078901', '0966666666', 'phuong.vu@sv.edu.vn',      N'Hà Nam',    N'Khoa Kinh tế số và Thương mại điện tử',         '2023-2027', N'TotNghiep'),
    ('SV2024007', N'Đinh Quốc Hùng',       '2004-01-05', N'Nam', '001204001234', '0977777777', 'hung.dinh@sv.edu.vn',      N'Bắc Ninh',  N'Khoa Khoa học Máy tính',                        '2022-2026', N'TotNghiep'),
    ('SV2024008', N'Ngô Thị Kim Linh',     '2007-09-18', N'Nữ',  '001207089012', '0988888888', 'linh.ngo@sv.edu.vn',       N'Ninh Bình', N'Khoa Kỹ thuật Máy tính và Điện tử',             '2024-2028', N'DangHoc'),
    ('SV2024009', N'Bùi Thanh Tuấn',       '2006-02-14', N'Nam', '001206021456', '0901234501', 'tuan.bui@sv.edu.vn',       N'Hưng Yên',  N'Khoa Kinh tế số và Thương mại điện tử',         '2024-2028', N'DangHoc'),
    ('SV2024010', N'Đỗ Thị Hồng Nhung',   '2005-06-22', N'Nữ',  '001205063789', '0901234502', 'nhung.do@sv.edu.vn',       N'Vĩnh Phúc', N'Khoa Khoa học Máy tính',                        '2023-2027', N'DangHoc'),
    ('SV2024011', N'Trịnh Văn Khải',       '2007-10-09', N'Nam', '001207101234', '0901234503', 'khai.trinh@sv.edu.vn',     N'Phú Thọ',   N'Khoa Kỹ thuật Máy tính và Điện tử',             '2024-2028', N'DangHoc'),
    ('SV2024012', N'Lý Thị Mai Anh',       '2004-04-17', N'Nữ',  '001204042567', '0901234504', 'maianh.ly@sv.edu.vn',      N'Bắc Giang', N'Khoa Kinh tế số và Thương mại điện tử',         '2022-2026', N'TotNghiep'),
    ('SV2024013', N'Phan Minh Đức',        '2006-12-28', N'Nam', '001206128901', '0901234505', 'duc.phan@sv.edu.vn',       N'Hải Dương', N'Khoa Khoa học Máy tính',                        '2024-2028', N'DangHoc'),
    ('SV2024014', N'Nguyễn Thị Thu Hà',   '2005-08-03', N'Nữ',  '001205083456', '0901234506', 'thuha.nguyen@sv.edu.vn',   N'Quảng Ninh',N'Khoa Kỹ thuật Máy tính và Điện tử',             '2023-2027', N'DaNghiHoc'),
    ('SV2024015', N'Cao Xuân Trường',      '2007-05-11', N'Nam', '001207050123', '0901234507', 'truong.cao@sv.edu.vn',     N'Thái Nguyên',N'Khoa Kinh tế số và Thương mại điện tử',        '2024-2028', N'DangHoc'),
    ('SV2024016', N'Lê Ngọc Bảo Châu',    '2004-07-30', N'Nữ',  '001204073012', '0901234508', 'baochau.le@sv.edu.vn',     N'Hòa Bình',  N'Khoa Khoa học Máy tính',                        '2022-2026', N'TotNghiep');
GO

-- ── HopDong ──────────────────────────────────────────────────────
-- Lưu ý: Dữ liệu mẫu phải đảm bảo SoNguoiHienTai khớp
INSERT INTO HopDong (MaHopDong, MaSinhVien, MaPhong, NgayBatDau, NgayKetThuc, TienCoc, GiaThue, TrangThai, NguoiTaoID) VALUES
    ('HD2024001', 'SV2024001', 'P101', '2024-09-01', '2025-06-30', 800000,  800000,  N'Active',  1),
    ('HD2024002', 'SV2024002', 'P201', '2024-09-01', '2025-06-30', 1200000, 1200000, N'Active',  1),
    ('HD2024003', 'SV2024003', 'P101', '2024-09-01', '2025-06-30', 800000,  800000,  N'Active',  2),
    ('HD2024004', 'SV2024004', 'P201', '2024-09-01', '2025-06-30', 1200000, 1200000, N'Active',  2),
    ('HD2024005', 'SV2024005', 'P102', '2024-09-01', '2025-06-30', 800000,  800000,  N'Active',  1),
    ('HD2024006', 'SV2024006', 'P301', '2024-09-01', '2025-06-30', 1800000, 1800000, N'Active',  1),
    -- HĐ đã kết thúc (lịch sử)
    ('HD2023001', 'SV2024007', 'P102', '2023-09-01', '2024-06-30', 800000,  750000,  N'Expired', 1);
GO

-- ── ThanhToan ────────────────────────────────────────────────────
INSERT INTO ThanhToan (MaThanhToan, MaHopDong, Thang, Nam, SoTien, NgayTaoBill, HanThanhToan, NgayThanhToan, TrangThai, PhuongThucTT, NguoiXuLyID) VALUES
    -- Tháng 9/2024 — Đã thanh toán
    ('TT2024001', 'HD2024001', 9, 2024, 800000,  '2024-09-01', '2024-09-10', '2024-09-08',  N'DaThanhToan',  N'ChuyenKhoan', 2),
    ('TT2024002', 'HD2024002', 9, 2024, 1200000, '2024-09-01', '2024-09-10', '2024-09-09',  N'DaThanhToan',  N'TienMat',     2),
    ('TT2024003', 'HD2024003', 9, 2024, 800000,  '2024-09-01', '2024-09-10', '2024-09-07',  N'DaThanhToan',  N'The',         2),
    -- Tháng 10/2024 — Một số trễ hạn
    ('TT2024004', 'HD2024001', 10, 2024, 800000, '2024-10-01', '2024-10-10', '2024-10-05',  N'DaThanhToan',  N'ChuyenKhoan', 2),
    ('TT2024005', 'HD2024002', 10, 2024, 1200000,'2024-10-01', '2024-10-10', NULL,           N'TreHan',       NULL,           NULL),
    -- Tháng 11/2024
    ('TT2024006', 'HD2024001', 11, 2024, 800000, '2024-11-01', '2024-11-10', '2024-11-08',  N'DaThanhToan',  N'TienMat',     2),
    -- Tháng hiện tại — Chưa thanh toán
    ('TT2025001', 'HD2024001', 3, 2026, 800000,  '2026-03-01', '2026-03-10', NULL,           N'ChuaThanhToan',NULL,           NULL),
    ('TT2025002', 'HD2024002', 3, 2026, 1200000, '2026-03-01', '2026-03-10', NULL,           N'TreHan',       NULL,           NULL),
    ('TT2025003', 'HD2024003', 3, 2026, 800000,  '2026-03-01', '2026-03-10', NULL,           N'ChuaThanhToan',NULL,           NULL);
GO

-- ── ViPham ────────────────────────────────────────────────────────
INSERT INTO ViPham (MaViPham, MaSinhVien, MaHopDong, LoaiViPham, MoTa, NgayViPham, MucPhat, TrangThai, NguoiGhiNhanID) VALUES
    ('VP2024001', 'SV2024003', 'HD2024003', N'GayOnAo',     N'Gây ồn ào sau 22h, các phòng xung quanh phàn nàn', '2024-10-15', 100000, N'DaXuLy',  2),
    ('VP2024002', 'SV2024005', 'HD2024005', N'VeTreeHan',   N'Về KTX sau 23h không xin phép 3 lần liên tiếp',    '2024-11-20', 150000, N'DaXuLy',  2),
    ('VP2024003', 'SV2024001', 'HD2024001', N'MatVeSinh',   N'Không giữ vệ sinh khu vực chung, nhắc nhở 2 lần',  '2025-01-10', 50000,  N'ChuaXuLy',2),
    ('VP2024004', 'SV2024002', 'HD2024002', N'TroChuaPhep', N'Cho người thân ở lại qua đêm không đăng ký',       '2025-02-05', 200000, N'ChuaXuLy',2);
GO

PRINT N'[6/6] Thêm dữ liệu mẫu thành công.';
GO

-- ═══════════════════════════════════════════════════════════════
--  THỐNG KÊ KẾT QUẢ
-- ═══════════════════════════════════════════════════════════════
PRINT N'';
PRINT N'══════════════════════════════════════════════';
PRINT N'  ✅ DormitoryDB khởi tạo hoàn tất!';
PRINT N'══════════════════════════════════════════════';
PRINT N'  📋 Bảng       : 7 (Role, TaiKhoan, SinhVien, Phong,';
PRINT N'                     HopDong, ThanhToan, ViPham)';
PRINT N'  ⚡ Trigger    : 4 triggers';
PRINT N'  👁  View       : 5 views';
PRINT N'  📦 Procedure  : 4 stored procedures';
PRINT N'  🔍 Index      : Filtered Unique Index (HĐ Active)';
PRINT N'  📊 Seed data  :';
PRINT N'     - 2 Roles  | 3 TaiKhoan';
PRINT N'     - 9 Phong  | 16 SinhVien';
PRINT N'     - 7 HopDong | 9 ThanhToan | 4 ViPham';
PRINT N'══════════════════════════════════════════════';
GO

-- ═══════════════════════════════════════════════════════════════
--  KIỂM TRA NHANH (Uncomment để chạy)
-- ═══════════════════════════════════════════════════════════════
-- SELECT * FROM vw_Dashboard;
-- SELECT * FROM vw_SinhVien_PhongHienTai;
-- SELECT * FROM vw_Phong_TrangThai;
-- SELECT * FROM vw_HoaDon_CanThu;
-- SELECT * FROM vw_DoanhThu_TheoThang ORDER BY Nam, Thang;
-- EXEC sp_CapNhat_HopDongHetHan;
-- EXEC sp_CapNhat_HoaDonTreHan;
GO
