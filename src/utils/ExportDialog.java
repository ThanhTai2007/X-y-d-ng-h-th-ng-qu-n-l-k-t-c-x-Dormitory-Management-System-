package utils;

import dao.HopDongDAO;
import dao.PhongDAO;
import dao.SinhVienDAO;
import dao.ThanhToanDAO;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.HopDong;
import model.SinhVien;
import model.ThanhToan;

import java.io.File;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/** Hộp thoại xuất Excel. */
public class ExportDialog {

    private ExportDialog() {}

    /** Hiển thị hộp thoại xuất Excel. */
    public static void show(Stage owner) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.getDialogPane().getStylesheets().add(ExportDialog.class.getResource("/view/css/style.css").toExternalForm());
        dialog.getDialogPane().getStyleClass().add("root");
        dialog.setTitle("📊  Xuất báo cáo Excel");
        dialog.setHeaderText("Chọn loại báo cáo cần xuất");
        dialog.getDialogPane().setPrefWidth(500);

        
        ToggleGroup tg = new ToggleGroup();
        RadioButton rbSV   = radio("👤  Danh sách Sinh viên", tg);
        RadioButton rbPhong = radio("🏠  Danh sách Phòng", tg);
        RadioButton rbHD   = radio("📄  Danh sách Hợp đồng", tg);
        RadioButton rbTT   = radio("💸  Danh sách Thanh toán", tg);
        RadioButton rbAll  = radio("📊  Báo cáo Tổng hợp (tất cả sheet)", tg);
        rbAll.setSelected(true);

        
        ComboBox<Integer> cboThang = new ComboBox<>();
        cboThang.setItems(javafx.collections.FXCollections.observableArrayList(
                IntStream.rangeClosed(1, 12).boxed().collect(Collectors.toList())));
        cboThang.setValue(LocalDate.now().getMonthValue());
        cboThang.setPrefWidth(90);

        ComboBox<Integer> cboNam = new ComboBox<>();
        int yr = Year.now().getValue();
        cboNam.setItems(javafx.collections.FXCollections.observableArrayList(
                IntStream.rangeClosed(yr - 2, yr + 1).boxed().collect(Collectors.toList())));
        cboNam.setValue(yr);
        cboNam.setPrefWidth(90);

        CheckBox chkLocThang = new CheckBox("Lọc theo tháng/năm:");
        HBox filterRow = new HBox(10, chkLocThang,
                new Label("Tháng:"), cboThang,
                new Label("Năm:"), cboNam);
        filterRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        cboThang.setDisable(true); cboNam.setDisable(true);
        chkLocThang.setOnAction(e -> {
            boolean on = chkLocThang.isSelected();
            cboThang.setDisable(!on); cboNam.setDisable(!on);
        });

        // Hiện filter khi chọn Thanh toán
        rbTT   .setOnAction(e -> { chkLocThang.setDisable(false); });
        rbSV   .setOnAction(e -> { chkLocThang.setDisable(true); chkLocThang.setSelected(false); cboThang.setDisable(true); cboNam.setDisable(true); });
        rbPhong.setOnAction(e -> { chkLocThang.setDisable(true); chkLocThang.setSelected(false); cboThang.setDisable(true); cboNam.setDisable(true); });
        rbHD   .setOnAction(e -> { chkLocThang.setDisable(true); chkLocThang.setSelected(false); cboThang.setDisable(true); cboNam.setDisable(true); });
        rbAll  .setOnAction(e -> { chkLocThang.setDisable(true); chkLocThang.setSelected(false); cboThang.setDisable(true); cboNam.setDisable(true); });
        chkLocThang.setDisable(true); // default: disabled

        
        VBox content = new VBox(12,
                section("Chọn loại báo cáo:", new VBox(8, rbAll, rbSV, rbPhong, rbHD, rbTT)),
                new Separator(),
                filterRow,
                new Label("💡  File sẽ được lưu dưới dạng .xlsx (Microsoft Excel)")
                        {{ setStyle("-fx-font-size: 11px; -fx-text-fill: #6B7280;"); }}
        );
        content.setPadding(new Insets(20));
        dialog.getDialogPane().setContent(content);

        ButtonType btnXuat = new ButtonType("📥  Xuất Excel", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnXuat, ButtonType.CANCEL);

        Button btnOk = (Button) dialog.getDialogPane().lookupButton(btnXuat);
        btnOk.setStyle("-fx-background-color: #1565C0; -fx-text-fill: white; -fx-font-weight: bold;");

        dialog.setResultConverter(bt -> {
            if (bt != btnXuat) return null;

            
            FileChooser fc = new FileChooser();
            fc.setTitle("Chọn vị trí lưu file Excel");
            fc.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Excel Files (*.xlsx)", "*.xlsx"));

            // Đặt tên file mặc định
            String defaultName = "KTX_";
            if      (rbSV.isSelected())    defaultName += "SinhVien";
            else if (rbPhong.isSelected()) defaultName += "Phong";
            else if (rbHD.isSelected())    defaultName += "HopDong";
            else if (rbTT.isSelected())    defaultName += "ThanhToan";
            else                           defaultName += "BaoCaoTongHop";
            defaultName += "_" + LocalDate.now().format(
                    java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx";
            fc.setInitialFileName(defaultName);
            File desktop = new File(System.getProperty("user.home") + "/Desktop");
            fc.setInitialDirectory(desktop.exists() && desktop.isDirectory() ? desktop : new File(System.getProperty("user.home")));

            File file = fc.showSaveDialog(owner);
            if (file == null) return null; // Bỏ qua

            String path = file.getAbsolutePath();
            if (!path.endsWith(".xlsx")) path += ".xlsx";

            
            try {
                if (rbSV.isSelected()) {
                    List<SinhVien> list = new SinhVienDAO().findAll();
                    ExcelExporter.exportSinhVien(list, path);
                    showDone(list.size() + " sinh viên", path);

                } else if (rbPhong.isSelected()) {
                    List<model.Phong> list = new PhongDAO().findAll();
                    ExcelExporter.exportPhong(list, path);
                    showDone(list.size() + " phòng", path);

                } else if (rbHD.isSelected()) {
                    List<HopDong> list = new HopDongDAO().findAll();
                    ExcelExporter.exportHopDong(list, path);
                    showDone(list.size() + " hợp đồng", path);

                } else if (rbTT.isSelected()) {
                    List<ThanhToan> list = new ThanhToanDAO().findAll();
                    if (chkLocThang.isSelected()) {
                        int thang = cboThang.getValue();
                        int nam   = cboNam.getValue();
                        list = list.stream()
                                   .filter(tt -> tt.getThang() == thang && tt.getNam() == nam)
                                   .collect(Collectors.toList());
                    }
                    ExcelExporter.exportThanhToan(list, path);
                    showDone(list.size() + " hóa đơn", path);

                } else {
                    // Tổng hợp
                    List<SinhVien>  svList = new SinhVienDAO().findAll();
                    List<HopDong>   hdList = new HopDongDAO().findAll();
                    List<ThanhToan> ttList = new ThanhToanDAO().findAll();
                    ExcelExporter.exportBaoCaoTongHop(svList, hdList, ttList, path);
                    showDone("4 sheet", path);
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                AlertUtils.showError("Lỗi xuất Excel",
                        "Không thể xuất file: " + ex.getMessage() +
                        "\n\nHãy đảm bảo:\n" +
                        "  • File không đang mở trong Excel\n" +
                        "  • Đã thêm thư viện Apache POI vào project");
            }

            return null; // Dialog tự đóng khi resultConverter return
        });

        dialog.showAndWait();
    }

    

    private static RadioButton radio(String text, ToggleGroup tg) {
        RadioButton rb = new RadioButton(text);
        rb.setToggleGroup(tg);
        rb.setStyle("-fx-font-size: 13px;");
        return rb;
    }

    private static VBox section(String title, javafx.scene.Node content) {
        Label lbl = new Label(title);
        lbl.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #374151;");
        VBox box = new VBox(8, lbl, content);
        return box;
    }

    private static void showDone(String info, String path) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Xuất Excel thành công");
        a.setHeaderText("✅  Xuất file thành công!");
        a.setContentText("Đã xuất: " + info + "\n\nFile lưu tại:\n" + path);
        a.showAndWait();
    }
}
