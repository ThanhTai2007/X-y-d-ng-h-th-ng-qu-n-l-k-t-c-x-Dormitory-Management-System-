-- ====================================================
-- KTX Manager — Quick Setup & Verification Script
-- Chạy script này SAU KHI đã chạy schema.sql
-- ====================================================

USE DormitoryDB;
GO

-- ── 1. Kiểm tra các bảng đã tồn tại chưa ─────────────
PRINT '=== Kiểm tra Tables ===';
SELECT
    TABLE_NAME,
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS c WHERE c.TABLE_NAME = t.TABLE_NAME) AS SoCot
FROM INFORMATION_SCHEMA.TABLES t
WHERE TABLE_TYPE = 'BASE TABLE'
ORDER BY TABLE_NAME;

-- ── 2. Kiểm tra dữ liệu seed ──────────────────────────
PRINT '';
PRINT '=== Kiểm tra Seed Data ===';
PRINT 'Số Role: '      + CAST((SELECT COUNT(*) FROM Role)     AS VARCHAR);
PRINT 'Số TaiKhoan: '  + CAST((SELECT COUNT(*) FROM TaiKhoan) AS VARCHAR);
PRINT 'Số Phong: '     + CAST((SELECT COUNT(*) FROM Phong)    AS VARCHAR);
PRINT 'Số SinhVien: '  + CAST((SELECT COUNT(*) FROM SinhVien) AS VARCHAR);
PRINT 'Số HopDong: '   + CAST((SELECT COUNT(*) FROM HopDong)  AS VARCHAR);
PRINT 'Số ThanhToan: ' + CAST((SELECT COUNT(*) FROM ThanhToan) AS VARCHAR);

-- ── 3. Kiểm tra Views ─────────────────────────────────
PRINT '';
PRINT '=== Test View vw_Dashboard ===';
SELECT * FROM vw_Dashboard;

-- ── 4. Tạo tài khoản Admin nếu chưa có ───────────────
-- Password: admin123 (BCrypt hash)
-- Lưu ý: Hash dưới đây là BCrypt của "admin123"
PRINT '';
PRINT '=== Đảm bảo có tài khoản Admin ===';

IF NOT EXISTS (SELECT 1 FROM TaiKhoan WHERE Username = 'admin')
BEGIN
    -- BCrypt hash của "admin123" với cost factor 12
    INSERT INTO TaiKhoan (Username, PasswordHash, HoTen, Email, RoleID, TrangThai)
    VALUES (
        'admin',
        '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBPj1Qzp2fyGPG',
        N'Quản trị viên',
        'admin@ktx.edu.vn',
        1,
        'Active'
    );
    PRINT 'Đã tạo tài khoản admin / admin123';
END
ELSE
    PRINT 'Tài khoản admin đã tồn tại.';

-- Tài khoản staff
IF NOT EXISTS (SELECT 1 FROM TaiKhoan WHERE Username = 'staff')
BEGIN
    INSERT INTO TaiKhoan (Username, PasswordHash, HoTen, Email, RoleID, TrangThai)
    VALUES (
        'staff',
        '$2a$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
        N'Nhân viên KTX',
        'staff@ktx.edu.vn',
        2,
        'Active'
    );
    PRINT 'Đã tạo tài khoản staff / password';
END

-- ── 5. Kiểm tra các tài khoản ────────────────────────
PRINT '';
PRINT '=== Tài khoản hệ thống ===';
SELECT
    tk.TaiKhoanID,
    tk.Username,
    tk.HoTen,
    r.RoleName AS VaiTro,
    tk.TrangThai
FROM TaiKhoan tk
INNER JOIN Role r ON tk.RoleID = r.RoleID;

PRINT '';
PRINT '=== BẮT ĐẦU ỨNG DỤNG ===';
PRINT 'Đăng nhập với: admin / admin123';
PRINT 'Hoặc:         staff / password';
GO

