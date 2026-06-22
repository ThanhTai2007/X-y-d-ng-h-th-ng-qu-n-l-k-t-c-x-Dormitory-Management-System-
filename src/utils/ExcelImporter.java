package utils;

import dao.SinhVienDAO;
import model.SinhVien;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.time.LocalDate;

/** Tiện ích import sinh viên từ Excel. */
public class ExcelImporter {

    public static void main(String[] args) {
        // Đường dẫn file mẫu
        String filePath = "c:\\Users\\ADMIN\\OneDrive\\Desktop\\DS Đăng ký, bố trí  ở Ký túc xá HKII (2025-2026) - 11h00 ngày 29.01.2026 Mới nhất\\DS Đăng ký, bố trí  ở Ký túc xá HKII (2025-2026) - 11h00 ngày 29.01.2026 Mới nhất.xlsx";
        importSinhVien(filePath);
    }

    public static void importSinhVien(String filePath) {
        System.out.println("Bắt đầu đọc dữ liệu từ: " + filePath);
        SinhVienDAO sinhVienDAO = new SinhVienDAO();
        DataFormatter formatter = new DataFormatter();
        
        int successCount = 0;
        int duplicateCount = 0;
        int errorCount = 0;

        File file = new File(filePath);
        if (!file.exists()) {
            System.err.println("Không tìm thấy file: " + filePath);
            return;
        }

        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            
            // Bỏ qua dòng tiêu đề (index 0)
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    // Ánh xạ các cột theo file mẫu
                    // Cột 1: Họ và tên
                    Cell cellHoTen = row.getCell(1);
                    String hoTen = cellHoTen == null ? "" : formatter.formatCellValue(cellHoTen).trim();
                    if (hoTen.isEmpty()) continue; // Dòng trống
                    
                    // Cột 2: Mã Sinh Viên
                    String maSinhVien = "";
                    Cell maSvCell = row.getCell(2);
                    if (maSvCell != null) {
                        if (maSvCell.getCellType() == CellType.NUMERIC) {
                            // Xử lý khi mã là số
                            maSinhVien = String.format("%.0f", maSvCell.getNumericCellValue());
                        } else {
                            maSinhVien = formatter.formatCellValue(maSvCell).trim();
                        }
                    }
                    if (maSinhVien.isEmpty()) {
                        maSinhVien = sinhVienDAO.generateNextId(); // Sinh mã nếu thiếu
                    }

                    // Cột 7: Số CCCD
                    Cell cellCccd = row.getCell(7);
                    String cccd = cellCccd == null ? "" : formatter.formatCellValue(cellCccd).trim();
                    // Tạo CCCD giả nếu thiếu
                    if (cccd.length() != 12) {
                        cccd = "000" + String.format("%09d", (int)(Math.random() * 1000000000));
                    }
                    
                    // Cột 9: Địa chỉ
                    Cell cellDiaChi = row.getCell(9);
                    String diaChi = cellDiaChi == null ? "" : formatter.formatCellValue(cellDiaChi).trim();
                    if (diaChi.isEmpty()) diaChi = "Chưa cập nhật";
                    
                    // Cột 10: Số điện thoại
                    Cell cellSdt = row.getCell(10);
                    String soDienThoai = cellSdt == null ? "" : formatter.formatCellValue(cellSdt).trim();
                    // Ràng buộc số điện thoại
                    if (soDienThoai.isEmpty() || soDienThoai.length() < 9 || soDienThoai.length() > 15) {
                        soDienThoai = null;
                    }

                    // Thiết lập giá trị mặc định
                    LocalDate ngaySinh = LocalDate.of(2005, 1, 1);
                    String gioiTinh = "Khác";
                    String email = maSinhVien.toLowerCase() + "@student.edu.vn";
                    String khoa = "Công nghệ thông tin";
                    String namHoc = "2025-2026";

                    // Kiểm tra trùng lặp ID an toàn
                    if (sinhVienDAO.findById(maSinhVien).isPresent()) {
                        System.out.println("[-] Bỏ qua SV trùng lặp ID: " + maSinhVien + " (" + hoTen + ")");
                        duplicateCount++;
                        continue;
                    }

                    // Tạo đối tượng và chèn vào DB
                    SinhVien sv = new SinhVien(maSinhVien, hoTen, ngaySinh, gioiTinh, cccd, 
                                               soDienThoai, email, diaChi, khoa, namHoc);
                    sv.setTrangThai("DangHoc");

                    if (sinhVienDAO.insert(sv)) {
                        successCount++;
                        System.out.println("[+] Đã thêm thành công: " + maSinhVien + " - " + hoTen);
                    } else {
                        errorCount++;
                        System.err.println("[x] Lỗi thêm SV: " + maSinhVien);
                    }

                } catch (Exception e) {
                    System.err.println("Lỗi tại dòng " + (i + 1) + ": " + e.getMessage());
                    errorCount++;
                }
            }

            System.out.println("\n=== KẾT QUẢ IMPORT ===");
            System.out.println("Thành công: " + successCount);
            System.out.println("Trùng lặp (Bỏ qua): " + duplicateCount);
            System.out.println("Lỗi: " + errorCount);

        } catch (Exception e) {
            System.err.println("Lỗi hệ thống khi đọc file Excel: " + e.getMessage());
        }
    }
}
