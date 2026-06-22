package utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import model.HopDong;
import model.Phong;
import model.SinhVien;
import model.ThanhToan;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFColor;

/** Xuất dữ liệu ra file Excel. */
public final class ExcelExporter {

    private ExcelExporter() {}

    public static void exportSinhVien(List<SinhVien> list, String filePath) throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook(); FileOutputStream fos = new FileOutputStream(filePath)) {
            XSSFSheet sheet = wb.createSheet("Danh sách Sinh viên");
            Styles s = new Styles(wb);
            int rowIdx = 0;
            rowIdx = writeTitle(sheet, s, "BÁO CÁO DANH SÁCH SINH VIÊN", 10, rowIdx);
            rowIdx = writeMeta(sheet, s, rowIdx);
            String[] headers = {"STT", "Mã SV", "Họ tên", "Ngày sinh", "Giới tính", "CCCD", "Số điện thoại", "Email", "Khoa", "Năm học", "Trạng thái"};
            writeHeader(sheet, s, headers, rowIdx++);
            for (int i = 0; i < list.size(); i++) {
                SinhVien sv = list.get(i);
                XSSFRow row = sheet.createRow(rowIdx++);
                XSSFCellStyle rowStyle = (i % 2 == 0) ? s.rowEven : s.rowOdd;
                cell(row, 0, i + 1, s.rowNumber);
                cell(row, 1, sv.getMaSinhVien(), rowStyle);
                cell(row, 2, sv.getHoTen(), rowStyle);
                cell(row, 3, fmt(sv.getNgaySinh()), rowStyle);
                cell(row, 4, sv.getGioiTinh(), rowStyle);
                cell(row, 5, sv.getCccd(), rowStyle);
                cell(row, 6, sv.getSoDienThoai(), rowStyle);
                cell(row, 7, sv.getEmail(), rowStyle);
                cell(row, 8, sv.getKhoa(), rowStyle);
                cell(row, 9, sv.getNamHoc(), rowStyle);
                cell(row, 10, FormatUtils.trangThaiThanhToan(sv.getTrangThai()), statusStyle(wb, s, sv.getTrangThai()));
            }
            int[] widths = {5, 12, 22, 12, 10, 14, 14, 24, 18, 12, 12};
            for (int i = 0; i < widths.length; i++) {
                sheet.setColumnWidth(i, widths[i] * 256);
            }
            wb.write(fos);
        }
    }

    public static void exportPhong(List<Phong> list, String filePath) throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook(); FileOutputStream fos = new FileOutputStream(filePath)) {
            XSSFSheet sheet = wb.createSheet("Danh sách Phòng");
            Styles s = new Styles(wb);
            int rowIdx = 0;
            rowIdx = writeTitle(sheet, s, "BÁO CÁO DANH SÁCH PHÒNG", 9, rowIdx);
            rowIdx = writeMeta(sheet, s, rowIdx);
            String[] headers = {"STT", "Mã phòng", "Tên phòng", "Tầng", "Loại phòng",
                                 "Sức chứa", "Hiện tại", "Còn trống", "Giá thuê/tháng", "Trạng thái"};
            writeHeader(sheet, s, headers, rowIdx++);
            for (int i = 0; i < list.size(); i++) {
                Phong p = list.get(i);
                XSSFRow row = sheet.createRow(rowIdx++);
                XSSFCellStyle rowStyle = (i % 2 == 0) ? s.rowEven : s.rowOdd;
                cell(row, 0, i + 1, s.rowNumber);
                cell(row, 1, p.getMaPhong(), rowStyle);
                cell(row, 2, p.getTenPhong(), rowStyle);
                cell(row, 3, p.getTang(), rowStyle);
                cell(row, 4, p.getLoaiPhong(), rowStyle);
                cell(row, 5, p.getSucChua(), rowStyle);
                cell(row, 6, p.getSoNguoiHienTai(), rowStyle);
                cell(row, 7, p.getSoChoTrong(), rowStyle);
                cellCurrency(row, 8, p.getGiaThue(), s.currency);
                cell(row, 9, FormatUtils.trangThaiPhong(p.getTrangThai()), phongStatusStyle(s, p.getTrangThai()));
            }
            int[] widths = {5, 10, 18, 7, 12, 10, 10, 10, 18, 14};
            for (int i = 0; i < widths.length; i++) {
                sheet.setColumnWidth(i, widths[i] * 256);
            }
            wb.write(fos);
        }
    }

    public static void exportHopDong(List<HopDong> list, String filePath) throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook(); FileOutputStream fos = new FileOutputStream(filePath)) {
            XSSFSheet sheet = wb.createSheet("Danh sách Hợp đồng");
            Styles s = new Styles(wb);
            int rowIdx = 0;
            rowIdx = writeTitle(sheet, s, "BÁO CÁO DANH SÁCH HỢP ĐỒNG", 10, rowIdx);
            rowIdx = writeMeta(sheet, s, rowIdx);
            String[] headers = {"STT", "Mã HĐ", "Mã SV", "Tên sinh viên", "Phòng", "Ngày bắt đầu", "Ngày kết thúc", "Giá thuê/th", "Tiền cọc", "Còn lại (th)", "Trạng thái"};
            writeHeader(sheet, s, headers, rowIdx++);
            for (int i = 0; i < list.size(); i++) {
                HopDong hd = list.get(i);
                XSSFRow row = sheet.createRow(rowIdx++);
                XSSFCellStyle rowStyle = (i % 2 == 0) ? s.rowEven : s.rowOdd;
                cell(row, 0, i + 1, s.rowNumber);
                cell(row, 1, hd.getMaHopDong(), rowStyle);
                cell(row, 2, hd.getMaSinhVien(), rowStyle);
                cell(row, 3, hd.getTenSinhVien(), rowStyle);
                cell(row, 4, hd.getMaPhong(), rowStyle);
                cell(row, 5, fmt(hd.getNgayBatDau()), rowStyle);
                cell(row, 6, fmt(hd.getNgayKetThuc()), rowStyle);
                cellCurrency(row, 7, hd.getGiaThue(), s.currency);
                cellCurrency(row, 8, hd.getTienCoc(), s.currency);
                cell(row, 9, hd.isActive() ? String.valueOf(hd.getSoThangConLai()) : "0", rowStyle);
                cell(row, 10, FormatUtils.trangThaiHopDong(hd.getTrangThai()), hopDongStatusStyle(wb, s, hd.getTrangThai()));
            }
            int[] widths = {5, 12, 12, 20, 8, 13, 13, 16, 14, 14, 14};
            for (int i = 0; i < widths.length; i++) {
                sheet.setColumnWidth(i, widths[i] * 256);
            }
            wb.write(fos);
        }
    }

    public static void exportThanhToan(List<ThanhToan> list, String filePath) throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook(); FileOutputStream fos = new FileOutputStream(filePath)) {
            XSSFSheet sheet = wb.createSheet("Hóa đơn Thanh toán");
            Styles s = new Styles(wb);
            int rowIdx = 0;
            rowIdx = writeTitle(sheet, s, "BÁO CÁO HÓA ĐƠN THANH TOÁN", 10, rowIdx);
            rowIdx = writeMeta(sheet, s, rowIdx);
            String[] headers = {"STT", "Mã Bill", "Mã HĐ", "Sinh viên", "Phòng", "Tháng", "Năm", "Số tiền", "Hạn TT", "Ngày TT", "Phương thức", "Trạng thái"};
            writeHeader(sheet, s, headers, rowIdx++);
            double total = 0;
            for (int i = 0; i < list.size(); i++) {
                ThanhToan tt = list.get(i);
                XSSFRow row = sheet.createRow(rowIdx++);
                XSSFCellStyle rowStyle = (i % 2 == 0) ? s.rowEven : s.rowOdd;
                cell(row, 0, i + 1, s.rowNumber);
                cell(row, 1, tt.getMaThanhToan(), rowStyle);
                cell(row, 2, tt.getMaHopDong(), rowStyle);
                cell(row, 3, tt.getHoTenSinhVien(), rowStyle);
                cell(row, 4, tt.getTenPhong(), rowStyle);
                cell(row, 5, tt.getThang(), rowStyle);
                cell(row, 6, tt.getNam(), rowStyle);
                cellCurrency(row, 7, tt.getSoTien(), s.currency);
                cell(row, 8, fmt(tt.getHanThanhToan()), rowStyle);
                cell(row, 9, fmt(tt.getNgayThanhToan()), rowStyle);
                cell(row, 10, tt.getPhuongThucTT(), rowStyle);
                cell(row, 11, FormatUtils.trangThaiThanhToan(tt.getTrangThai()), thanhToanStatusStyle(wb, s, tt.getTrangThai()));
                total += tt.getSoTien();
            }
            rowIdx++;
            writeSummaryRow(sheet, s, rowIdx, "Tổng đã thu:", total, 7);
            int[] widths = {5, 12, 12, 20, 8, 7, 6, 16, 12, 12, 14, 16};
            for (int i = 0; i < widths.length; i++) {
                sheet.setColumnWidth(i, widths[i] * 256);
            }
            wb.write(fos);
        }
    }

    public static void exportBaoCaoTongHop(
            List<SinhVien> svList,
            List<HopDong> hdList,
            List<ThanhToan> ttList,
            String filePath) throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook(); FileOutputStream fos = new FileOutputStream(filePath)) {
            Styles s = new Styles(wb);
            buildDashboardSheet(wb, s, svList, hdList, ttList);
            buildSinhVienSheet(wb, s, svList);
            buildHopDongSheet(wb, s, hdList);
            buildThanhToanSheet(wb, s, ttList);
            wb.write(fos);
        }
    }

    private static void buildDashboardSheet(XSSFWorkbook wb, Styles s,
            List<SinhVien> svList, List<HopDong> hdList, List<ThanhToan> ttList) {
        XSSFSheet sheet = wb.createSheet("📊 Tổng hợp");
        int rowIdx = 0;
        rowIdx = writeTitle(sheet, s, "BÁO CÁO TỔNG HỢP — KTX MANAGER", 3, rowIdx);
        rowIdx = writeMeta(sheet, s, rowIdx);
        rowIdx++;
        String[][] kpis = {
                {"Tổng sinh viên", String.valueOf(svList.size())},
                {"Hợp đồng đang Active", String.valueOf(hdList.stream().filter(HopDong::isActive).count())},
                {"Tổng hóa đơn", String.valueOf(ttList.size())},
                {"Tổng đã thu", FormatUtils.formatCurrency(ttList.stream().mapToDouble(ThanhToan::getSoTien).sum())}
        };
        writeHeader(sheet, s, new String[]{"Chỉ số", "Giá trị"}, rowIdx++);
        for (int i = 0; i < kpis.length; i++) {
            XSSFRow row = sheet.createRow(rowIdx++);
            cell(row, 0, kpis[i][0], (i % 2 == 0) ? s.rowEven : s.rowOdd);
            cell(row, 1, kpis[i][1], (i % 2 == 0) ? s.rowEven : s.rowOdd);
        }
        sheet.setColumnWidth(0, 35 * 256);
        sheet.setColumnWidth(1, 20 * 256);
    }

    private static void buildSinhVienSheet(XSSFWorkbook wb, Styles s, List<SinhVien> list) {
        XSSFSheet sheet = wb.createSheet("👤 Sinh viên");
        int rowIdx = 0;
        rowIdx = writeTitle(sheet, s, "DANH SÁCH SINH VIÊN", 10, rowIdx);
        rowIdx = writeMeta(sheet, s, rowIdx);
        writeHeader(sheet, s, new String[]{"STT", "Mã SV", "Họ tên", "Ngày sinh", "Giới tính", "CCCD", "SĐT", "Email", "Khoa", "Năm học", "TT"}, rowIdx++);
        for (int i = 0; i < list.size(); i++) {
            SinhVien sv = list.get(i);
            XSSFRow row = sheet.createRow(rowIdx++);
            XSSFCellStyle rowStyle = (i % 2 == 0) ? s.rowEven : s.rowOdd;
            cell(row, 0, i + 1, s.rowNumber);
            cell(row, 1, sv.getMaSinhVien(), rowStyle);
            cell(row, 2, sv.getHoTen(), rowStyle);
            cell(row, 3, fmt(sv.getNgaySinh()), rowStyle);
            cell(row, 4, sv.getGioiTinh(), rowStyle);
            cell(row, 5, sv.getCccd(), rowStyle);
            cell(row, 6, sv.getSoDienThoai(), rowStyle);
            cell(row, 7, sv.getEmail(), rowStyle);
            cell(row, 8, sv.getKhoa(), rowStyle);
            cell(row, 9, sv.getNamHoc(), rowStyle);
            cell(row, 10, FormatUtils.trangThaiHopDong(sv.getTrangThai()), rowStyle);
        }
        int[] widths = {5, 12, 22, 12, 10, 14, 14, 24, 18, 12, 12};
        for (int i = 0; i < widths.length; i++) {
            sheet.setColumnWidth(i, widths[i] * 256);
        }
    }

    private static void buildHopDongSheet(XSSFWorkbook wb, Styles s, List<HopDong> list) {
        XSSFSheet sheet = wb.createSheet("📄 Hợp đồng");
        int rowIdx = 0;
        rowIdx = writeTitle(sheet, s, "DANH SÁCH HỢP ĐỒNG", 9, rowIdx);
        rowIdx = writeMeta(sheet, s, rowIdx);
        writeHeader(sheet, s, new String[]{"STT", "Mã HĐ", "Mã SV", "Tên SV", "Phòng", "Ngày BD", "Ngày KT", "Giá thuê", "Tiền cọc", "Trạng thái"}, rowIdx++);
        for (int i = 0; i < list.size(); i++) {
            HopDong hd = list.get(i);
            XSSFRow row = sheet.createRow(rowIdx++);
            XSSFCellStyle rowStyle = (i % 2 == 0) ? s.rowEven : s.rowOdd;
            cell(row, 0, i + 1, s.rowNumber);
            cell(row, 1, hd.getMaHopDong(), rowStyle);
            cell(row, 2, hd.getMaSinhVien(), rowStyle);
            cell(row, 3, hd.getTenSinhVien(), rowStyle);
            cell(row, 4, hd.getMaPhong(), rowStyle);
            cell(row, 5, fmt(hd.getNgayBatDau()), rowStyle);
            cell(row, 6, fmt(hd.getNgayKetThuc()), rowStyle);
            cellCurrency(row, 7, hd.getGiaThue(), s.currency);
            cellCurrency(row, 8, hd.getTienCoc(), s.currency);
            cell(row, 9, FormatUtils.trangThaiHopDong(hd.getTrangThai()), hopDongStatusStyle(wb, s, hd.getTrangThai()));
        }
        int[] widths = {5, 12, 12, 20, 8, 13, 13, 16, 14, 16};
        for (int i = 0; i < widths.length; i++) {
            sheet.setColumnWidth(i, widths[i] * 256);
        }
    }

    private static void buildThanhToanSheet(XSSFWorkbook wb, Styles s, List<ThanhToan> list) {
        XSSFSheet sheet = wb.createSheet("💸 Thanh toán");
        int rowIdx = 0;
        rowIdx = writeTitle(sheet, s, "DANH SÁCH HÓA ĐƠN", 10, rowIdx);
        rowIdx = writeMeta(sheet, s, rowIdx);
        writeHeader(sheet, s, new String[]{"STT", "Mã Bill", "Mã HĐ", "Sinh viên", "Phòng", "Tháng", "Năm", "Số tiền", "Hạn TT", "Ngày TT", "TT"}, rowIdx++);
        for (int i = 0; i < list.size(); i++) {
            ThanhToan tt = list.get(i);
            XSSFRow row = sheet.createRow(rowIdx++);
            XSSFCellStyle rowStyle = (i % 2 == 0) ? s.rowEven : s.rowOdd;
            cell(row, 0, i + 1, s.rowNumber);
            cell(row, 1, tt.getMaThanhToan(), rowStyle);
            cell(row, 2, tt.getMaHopDong(), rowStyle);
            cell(row, 3, tt.getHoTenSinhVien(), rowStyle);
            cell(row, 4, tt.getTenPhong(), rowStyle);
            cell(row, 5, tt.getThang(), rowStyle);
            cell(row, 6, tt.getNam(), rowStyle);
            cellCurrency(row, 7, tt.getSoTien(), s.currency);
            cell(row, 8, fmt(tt.getHanThanhToan()), rowStyle);
            cell(row, 9, fmt(tt.getNgayThanhToan()), rowStyle);
            cell(row, 10, FormatUtils.trangThaiThanhToan(tt.getTrangThai()), thanhToanStatusStyle(wb, s, tt.getTrangThai()));
        }
        int[] widths = {5, 12, 12, 20, 8, 7, 6, 16, 12, 12, 14};
        for (int i = 0; i < widths.length; i++) {
            sheet.setColumnWidth(i, widths[i] * 256);
        }
    }

    private static int writeTitle(XSSFSheet sheet, Styles s, String title, int mergeUpTo, int rowIdx) {
        XSSFRow row1 = sheet.createRow(rowIdx++);
        row1.setHeight((short) 800);
        XSSFCell c1 = row1.createCell(0);
        c1.setCellValue("KTX MANAGER — HỆ THỐNG QUẢN LÝ KÝ TÚC XÁ");
        c1.setCellStyle(s.titleMain);
        sheet.addMergedRegion(new CellRangeAddress(row1.getRowNum(), row1.getRowNum(), 0, mergeUpTo));

        XSSFRow row2 = sheet.createRow(rowIdx++);
        row2.setHeight((short) 600);
        XSSFCell c2 = row2.createCell(0);
        c2.setCellValue(title);
        c2.setCellStyle(s.titleSub);
        sheet.addMergedRegion(new CellRangeAddress(row2.getRowNum(), row2.getRowNum(), 0, mergeUpTo));
        return rowIdx;
    }

    private static int writeMeta(XSSFSheet sheet, Styles s, int rowIdx) {
        XSSFRow row = sheet.createRow(rowIdx++);
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        XSSFCell c = row.createCell(0);
        c.setCellValue("Ngày xuất báo cáo: " + now + "     |     Xuất bởi: KTX Manager v1.0");
        c.setCellStyle(s.meta);
        rowIdx++;
        return rowIdx;
    }

    private static void writeHeader(XSSFSheet sheet, Styles s, String[] headers, int rowIdx) {
        XSSFRow row = sheet.createRow(rowIdx);
        row.setHeight((short) 500);
        for (int i = 0; i < headers.length; i++) {
            XSSFCell cell = row.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(s.header);
        }
    }

    private static void writeSummaryRow(XSSFSheet sheet, Styles s, int rowIdx, String label, double value, int valueCol) {
        XSSFRow row = sheet.createRow(rowIdx);
        XSSFCell lbl = row.createCell(0);
        lbl.setCellValue(label);
        lbl.setCellStyle(s.summary);
        XSSFCell val = row.createCell(valueCol);
        val.setCellValue(FormatUtils.formatCurrency(value));
        val.setCellStyle(s.summary);
    }

    private static void cell(XSSFRow row, int col, Object value, XSSFCellStyle style) {
        XSSFCell c = row.createCell(col);
        if (value instanceof Integer i) {
            c.setCellValue(i);
        } else if (value instanceof Double d) {
            c.setCellValue(d);
        } else {
            c.setCellValue(value != null ? value.toString() : "—");
        }
        c.setCellStyle(style);
    }

    private static void cellCurrency(XSSFRow row, int col, double value, XSSFCellStyle style) {
        XSSFCell c = row.createCell(col);
        c.setCellValue(value);
        c.setCellStyle(style);
    }

    private static XSSFCellStyle statusStyle(XSSFWorkbook wb, Styles s, String status) {
        return switch (status == null ? "" : status) {
            case "DangHoc" -> s.rowGreen;
            case "DaNghiHoc" -> s.rowYellow;
            default -> s.rowOdd;
        };
    }

    private static XSSFCellStyle hopDongStatusStyle(XSSFWorkbook wb, Styles s, String status) {
        return switch (status == null ? "" : status) {
            case "Active" -> s.rowGreen;
            case "Terminated" -> s.rowRed;
            case "Expired" -> s.rowOdd;
            default -> s.rowYellow;
        };
    }

    private static XSSFCellStyle phongStatusStyle(Styles s, String status) {
        return switch (status == null ? "" : status) {
            case "Trong"  -> s.rowGreen;
            case "Day"    -> s.rowRed;
            case "BaoTri" -> s.rowYellow;
            default       -> s.rowOdd;
        };
    }

    private static XSSFCellStyle thanhToanStatusStyle(XSSFWorkbook wb, Styles s, String status) {
        return switch (status == null ? "" : status) {
            case "DaThanhToan" -> s.rowGreen;
            case "TreHan" -> s.rowRed;
            default -> s.rowYellow;
        };
    }

    private static String fmt(LocalDate d) {
        return d != null ? d.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "—";
    }

    private static byte[] hexToRgb(String hex) {
        hex = hex.replace("#", "");
        return new byte[]{
            (byte) Integer.parseInt(hex.substring(0, 2), 16),
            (byte) Integer.parseInt(hex.substring(2, 4), 16),
            (byte) Integer.parseInt(hex.substring(4, 6), 16)
        };
    }

    private static class Styles {
        private final XSSFCellStyle titleMain;
        private final XSSFCellStyle titleSub;
        private final XSSFCellStyle meta;
        private final XSSFCellStyle header;
        private final XSSFCellStyle rowEven;
        private final XSSFCellStyle rowOdd;
        private final XSSFCellStyle rowNumber;
        private final XSSFCellStyle rowGreen;
        private final XSSFCellStyle rowRed;
        private final XSSFCellStyle rowYellow;
        private final XSSFCellStyle currency;
        private final XSSFCellStyle summary;

        Styles(XSSFWorkbook wb) {
            XSSFFont fontTitle = wb.createFont();
            fontTitle.setBold(true);
            fontTitle.setFontHeightInPoints((short) 14);
            fontTitle.setColor(IndexedColors.WHITE.getIndex());

            XSSFFont fontSub = wb.createFont();
            fontSub.setBold(true);
            fontSub.setFontHeightInPoints((short) 12);

            XSSFFont fontMeta = wb.createFont();
            fontMeta.setFontHeightInPoints((short) 10);

            XSSFFont fontHeader = wb.createFont();
            fontHeader.setBold(true);

            XSSFFont fontDefault = wb.createFont();
            fontDefault.setFontHeightInPoints((short) 10);

            titleMain = wb.createCellStyle();
            titleMain.setFillForegroundColor(new XSSFColor(hexToRgb("#1565C0"), null));
            titleMain.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            titleMain.setAlignment(HorizontalAlignment.CENTER);
            titleMain.setVerticalAlignment(VerticalAlignment.CENTER);
            titleMain.setFont(fontTitle);

            titleSub = wb.createCellStyle();
            titleSub.setAlignment(HorizontalAlignment.CENTER);
            titleSub.setVerticalAlignment(VerticalAlignment.CENTER);
            titleSub.setFont(fontSub);

            meta = wb.createCellStyle();
            meta.setFont(fontMeta);

            header = wb.createCellStyle();
            header.setFillForegroundColor(new XSSFColor(hexToRgb("#1565C0"), null));
            header.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            header.setFont(fontHeader);
            header.setBorderBottom(BorderStyle.THIN);
            header.setBorderTop(BorderStyle.THIN);
            header.setBorderLeft(BorderStyle.THIN);
            header.setBorderRight(BorderStyle.THIN);
            header.setAlignment(HorizontalAlignment.CENTER);
            header.setVerticalAlignment(VerticalAlignment.CENTER);

            rowEven = wb.createCellStyle();
            rowEven.setFillForegroundColor(new XSSFColor(hexToRgb("#F0F7FF"), null));
            rowEven.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            rowEven.setFont(fontDefault);
            rowEven.setBorderBottom(BorderStyle.THIN);
            rowEven.setBorderTop(BorderStyle.THIN);
            rowEven.setBorderLeft(BorderStyle.THIN);
            rowEven.setBorderRight(BorderStyle.THIN);

            rowOdd = wb.createCellStyle();
            rowOdd.setFillForegroundColor(new XSSFColor(hexToRgb("#FFFFFF"), null));
            rowOdd.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            rowOdd.setFont(fontDefault);
            rowOdd.setBorderBottom(BorderStyle.THIN);
            rowOdd.setBorderTop(BorderStyle.THIN);
            rowOdd.setBorderLeft(BorderStyle.THIN);
            rowOdd.setBorderRight(BorderStyle.THIN);

            rowNumber = wb.createCellStyle();
            rowNumber.cloneStyleFrom(rowOdd);
            rowNumber.setAlignment(HorizontalAlignment.CENTER);

            rowGreen = wb.createCellStyle();
            rowGreen.cloneStyleFrom(rowOdd);
            rowGreen.setFillForegroundColor(new XSSFColor(hexToRgb("#D1FAE5"), null));
            rowGreen.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            rowRed = wb.createCellStyle();
            rowRed.cloneStyleFrom(rowOdd);
            rowRed.setFillForegroundColor(new XSSFColor(hexToRgb("#FEE2E2"), null));
            rowRed.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            rowYellow = wb.createCellStyle();
            rowYellow.cloneStyleFrom(rowOdd);
            rowYellow.setFillForegroundColor(new XSSFColor(hexToRgb("#FFF9C4"), null));
            rowYellow.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            currency = wb.createCellStyle();
            currency.cloneStyleFrom(rowOdd);
            currency.setDataFormat(wb.createDataFormat().getFormat("#,##0_);[Red](#,##0)"));

            summary = wb.createCellStyle();
            summary.setFillForegroundColor(new XSSFColor(hexToRgb("#1565C0"), null));
            summary.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            summary.setFont(fontTitle);
        }
    }
}
