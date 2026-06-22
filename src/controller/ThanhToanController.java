package controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import model.ThanhToan;
import service.ThanhToanService;
import utils.AlertUtils;
import utils.ExportDialog;
import utils.FormatUtils;

import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ThanhToanController {

    
    @FXML private TextField          txtSearch;
    @FXML private ComboBox<String>   cboFilter;
    @FXML private ComboBox<Integer>  cboThang;
    @FXML private ComboBox<Integer>  cboNam;

    @FXML private Label lblDaThu;
    @FXML private Label lblChuaThu;
    @FXML private Label lblTreHan;
    @FXML private Label lblTongBill;

    @FXML private TableView<ThanhToan>            tblThanhToan;
    @FXML private TableColumn<ThanhToan,String>   colMaTT;
    @FXML private TableColumn<ThanhToan,String>   colMaHD;
    @FXML private TableColumn<ThanhToan,String>   colTenSV;
    @FXML private TableColumn<ThanhToan,String>   colPhong;
    @FXML private TableColumn<ThanhToan,String>   colThangNam;
    @FXML private TableColumn<ThanhToan,String>   colSoTien;
    @FXML private TableColumn<ThanhToan,LocalDate>colHanTT;
    @FXML private TableColumn<ThanhToan,LocalDate>colNgayTT;
    @FXML private TableColumn<ThanhToan,String>   colPhuongThuc;
    @FXML private TableColumn<ThanhToan,String>   colTrangThai;

    private final ThanhToanService service = new ThanhToanService();
    private final ObservableList<ThanhToan> masterList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Cập nhật trạng thái trễ hạn
        service.capNhatTreHan();
        setupTable();
        setupFilters();
        loadData();
    }

    // Cấu hình bảng
    private void setupTable() {
        colMaTT.setCellValueFactory(new PropertyValueFactory<>("maThanhToan"));
        colMaHD.setCellValueFactory(new PropertyValueFactory<>("maHopDong"));

        // Trường join
        colTenSV.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getHoTenSinhVien()));
        colPhong.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getTenPhong()));

        // Tháng/Năm
        colThangNam.setCellValueFactory(c -> {
            ThanhToan tt = c.getValue();
            return new SimpleStringProperty("T" + tt.getThang() + "/" + tt.getNam());
        });

        // Định dạng tiền
        colSoTien.setCellValueFactory(c ->
                new SimpleStringProperty(FormatUtils.formatCurrency(c.getValue().getSoTien())));

        // Hạn thanh toán
        colHanTT.setCellValueFactory(new PropertyValueFactory<>("hanThanhToan"));
        colHanTT.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(LocalDate d, boolean empty) {
                super.updateItem(d, empty);
                if (empty || d == null) { setText(null); setStyle(""); return; }
                setText(FormatUtils.formatDate(d));
                ThanhToan tt = getTableRow() != null ? (ThanhToan) getTableRow().getItem() : null;
                if (tt != null && "TreHan".equals(tt.getTrangThai()))
                    setStyle("-fx-text-fill: #DC2626; -fx-font-weight: bold;");
                else setStyle("");
            }
        });

        // Ngày thực tế thanh toán
        colNgayTT.setCellValueFactory(new PropertyValueFactory<>("ngayThanhToan"));
        colNgayTT.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(LocalDate d, boolean empty) {
                super.updateItem(d, empty);
                setText(empty || d == null ? "—" : FormatUtils.formatDate(d));
            }
        });

        // Phương thức thanh toán
        colPhuongThuc.setCellValueFactory(c -> {
            String pt = c.getValue().getPhuongThucTT();
            return new SimpleStringProperty(pt != null ? pt : "—");
        });

        // Huy hiệu trạng thái
        colTrangThai.setCellValueFactory(new PropertyValueFactory<>("trangThai"));
        colTrangThai.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) { setGraphic(null); return; }
                Label b = new Label(FormatUtils.trangThaiThanhToan(s));
                b.getStyleClass().addAll("badge", switch (s) {
                    case "DaThanhToan"   -> "badge-success";
                    case "ChuaThanhToan" -> "badge-warning";
                    case "TreHan"        -> "badge-danger";
                    default              -> "badge-gray";
                });
                setGraphic(b); setText(null);
            }
        });

        tblThanhToan.setItems(masterList);
        tblThanhToan.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Double-click
        tblThanhToan.setRowFactory(tv -> {
            TableRow<ThanhToan> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !row.isEmpty()) {
                    ThanhToan tt = row.getItem();
                    if (!"DaThanhToan".equals(tt.getTrangThai()))
                        showThanhToanDialog(tt);
                }
            });
            return row;
        });
    }

    private void setupFilters() {
        // Bộ lọc trạng thái
        cboFilter.setItems(FXCollections.observableArrayList(
                "Tất cả", "ChuaThanhToan", "DaThanhToan", "TreHan"));
        cboFilter.setValue("Tất cả");
        cboFilter.setOnAction(e -> applyFilter());

        // Tháng: 1–12
        cboThang.setItems(FXCollections.observableArrayList(
                IntStream.rangeClosed(1, 12).boxed().collect(Collectors.toList())));
        cboThang.setValue(LocalDate.now().getMonthValue());
        cboThang.setOnAction(e -> applyFilter());

        // Năm: năm hiện tại ± 3
        int currentYear = Year.now().getValue();
        cboNam.setItems(FXCollections.observableArrayList(
                IntStream.rangeClosed(currentYear - 2, currentYear + 1)
                         .boxed().collect(Collectors.toList())));
        cboNam.setValue(currentYear);
        cboNam.setOnAction(e -> applyFilter());
    }

    // Tải dữ liệu
    private void loadData() {
        try {
            List<ThanhToan> all = service.layTatCa();
            masterList.setAll(all);
            updateSummary(all);
        } catch (Exception e) {
            AlertUtils.showError("Lỗi tải dữ liệu", e.getMessage());
        }
    }

    private void applyFilter() {
        try {
            String kw     = txtSearch.getText().trim();
            String status = cboFilter.getValue();
            Integer thang = cboThang.getValue();
            Integer nam   = cboNam.getValue();

            List<ThanhToan> base = kw.isEmpty()
                    ? service.layTatCa()
                    : service.timKiem(kw);

            List<ThanhToan> result = base.stream().filter(tt -> {
                boolean okStatus = "Tất cả".equals(status) || status.equals(tt.getTrangThai());
                boolean okThang  = thang == null || tt.getThang() == thang;
                boolean okNam    = nam   == null || tt.getNam()   == nam;
                return okStatus && okThang && okNam;
            }).collect(Collectors.toList());

            masterList.setAll(result);
            updateSummary(result);

        } catch (Exception e) {
            AlertUtils.showError("Lỗi lọc dữ liệu", e.getMessage());
        }
    }

    private void updateSummary(List<ThanhToan> list) {
        lblTongBill.setText("Tổng: " + list.size() + " hóa đơn");
        lblDaThu.setText  (FormatUtils.formatCurrency(service.tongDaThanhToan(list)));
        lblChuaThu.setText(FormatUtils.formatCurrency(service.tongChuaThanhToan(list)));
        lblTreHan.setText (service.demTreHan(list) + " bill");
    }

    @FXML public void handleSearch()  { applyFilter(); }
    @FXML public void handleExport()  { ExportDialog.show((javafx.stage.Stage) tblThanhToan.getScene().getWindow()); }
    @FXML public void handleRefresh() {
        txtSearch.clear();
        cboFilter.setValue("Tất cả");
        cboThang.setValue(LocalDate.now().getMonthValue());
        cboNam.setValue(Year.now().getValue());
        loadData();
    }

    // Xử lý thanh toán hóa đơn
    @FXML
    public void handleThanhToan() {
        ThanhToan sel = tblThanhToan.getSelectionModel().getSelectedItem();
        if (sel == null) {
            AlertUtils.showWarning("Chưa chọn", "Vui lòng chọn hóa đơn cần thanh toán (double-click hoặc chọn rồi nhấn nút).");
            return;
        }
        if ("DaThanhToan".equals(sel.getTrangThai())) {
            AlertUtils.showWarning("Đã thanh toán", "Hóa đơn này đã được thanh toán rồi.");
            return;
        }
        showThanhToanDialog(sel);
    }

    // Tạo hóa đơn thủ công
    @FXML
    public void handleTaoBill() {
        showTaoBillDialog();
    }

    // Tạo bill hàng loạt
    @FXML
    public void handleHangLoat() {
        int thang = LocalDate.now().getMonthValue();
        int nam   = LocalDate.now().getYear();

        boolean confirm = AlertUtils.showConfirmation(
                "Tạo bill hàng loạt",
                "Tạo hóa đơn tháng " + thang + "/" + nam + " cho TẤT CẢ hợp đồng đang Active?\n" +
                "(Bỏ qua nếu đã có bill tháng này)");

        if (!confirm) return;

        try {
            int count = service.taoHoaDonHangLoat(thang, nam);
            if (count == 0)
                AlertUtils.showWarning("Không tạo thêm",
                        "Tất cả hợp đồng đã có hóa đơn tháng " + thang + "/" + nam + ".");
            else
                AlertUtils.showSuccess("Tạo bill hàng loạt",
                        "Đã tạo thành công " + count + " hóa đơn tháng " + thang + "/" + nam + ".");
            loadData();
        } catch (Exception e) {
            AlertUtils.showError("Lỗi", e.getMessage());
        }
    }

    // Dialog thanh toán
    private void showThanhToanDialog(ThanhToan tt) {
        Dialog<String> dialog = new Dialog<>();
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/view/css/style.css").toExternalForm());
        dialog.getDialogPane().getStyleClass().add("root");
        dialog.setTitle("💳  Ghi nhận Thanh toán");
        dialog.setHeaderText("Xác nhận thanh toán hóa đơn");
        dialog.getDialogPane().setPrefWidth(440);

        // Thông tin bill
        Label infoText = new Label(
            "📋 Mã Bill   : " + tt.getMaThanhToan() + "\n" +
            "👤 Sinh viên : " + (tt.getHoTenSinhVien() != null ? tt.getHoTenSinhVien() : tt.getMaHopDong()) + "\n" +
            "🏠 Phòng     : " + (tt.getTenPhong() != null ? tt.getTenPhong() : "—") + "\n" +
            "📅 Kỳ        : Tháng " + tt.getThang() + "/" + tt.getNam() + "\n" +
            "💰 Số tiền   : " + FormatUtils.formatCurrency(tt.getSoTien()) + "\n" +
            "📆 Hạn TT   : " + FormatUtils.formatDate(tt.getHanThanhToan()) +
            (tt.isOverdue() ? "  ⚠ TRỄ HẠN " + tt.getSoNgayTreHan() + " ngày" : "")
        );
        infoText.setStyle("-fx-font-size: 13px; -fx-padding: 8 12; " +
                          "-fx-background-color: #F8FAFC; -fx-background-radius: 8;");

        // Phương thức thanh toán
        // Các loại phương thức
        ComboBox<String> cboPT = new ComboBox<>(FXCollections.observableArrayList(
                "TienMat", "ChuyenKhoan", "The", "Khac"));
        cboPT.setValue("TienMat");
        cboPT.setPrefWidth(280);

        // Hiển thị tiếng Việt
        cboPT.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                setText(empty ? null : switch (s) {
                    case "TienMat"     -> "💵  Tiền mặt";
                    case "ChuyenKhoan" -> "🏦  Chuyển khoản";
                    case "The"         -> "💳  Thẻ ngân hàng / QR";
                    case "Khac"        -> "💡  Khác";
                    default -> s;
                });
            }
        });
        cboPT.setButtonCell(cboPT.getCellFactory().call(null));
        cboPT.setValue("TienMat");

        VBox box = new VBox(14, infoText,
                new Label("Phương thức thanh toán *:") {{ setStyle("-fx-font-weight: bold; -fx-font-size: 12px;"); }},
                cboPT);
        box.setPadding(new Insets(20));
        dialog.getDialogPane().setContent(box);

        ButtonType btnXN = new ButtonType("✅  Xác nhận thanh toán", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnXN, ButtonType.CANCEL);

        // Style nút xác nhận
        Button btnOk = (Button) dialog.getDialogPane().lookupButton(btnXN);
        btnOk.setStyle("-fx-background-color: #2E7D32; -fx-text-fill: white; -fx-font-weight: bold;");

        dialog.setResultConverter(bt -> bt == btnXN ? cboPT.getValue() : null);
        dialog.showAndWait().ifPresent(phuongThuc -> {
            try {
                service.ghiNhanThanhToan(tt.getMaThanhToan(), phuongThuc);
                AlertUtils.showSuccess("Thanh toán thành công",
                        "Đã ghi nhận thanh toán " + FormatUtils.formatCurrency(tt.getSoTien()) +
                        "\nPhương thức: " + phuongThuc);
                loadData();
            } catch (Exception e) {
                AlertUtils.showError("Lỗi thanh toán", e.getMessage());
            }
        });
    }

    // Dialog tạo bill
    private void showTaoBillDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/view/css/style.css").toExternalForm());
        dialog.getDialogPane().getStyleClass().add("root");
        dialog.setTitle("➕  Tạo hóa đơn thủ công");
        dialog.getDialogPane().setPrefWidth(440);

        TextField tfMaHD    = new TextField(); tfMaHD.setPromptText("VD: HD2024001");
        Spinner<Integer> spThang = new Spinner<>(1, 12, LocalDate.now().getMonthValue());
        Spinner<Integer> spNam   = new Spinner<>(2020, 2030, LocalDate.now().getYear());
        TextField tfSoTien  = new TextField(); tfSoTien.setPromptText("VNĐ");
        DatePicker dpHan    = new DatePicker(
                LocalDate.now().withDayOfMonth(1).plusMonths(1).withDayOfMonth(10));

        GridPane g = new GridPane(); g.setHgap(12); g.setVgap(12); g.setPadding(new Insets(20));
        lbl(g, 0, "Mã hợp đồng *:"); g.add(tfMaHD,   1, 0);
        lbl(g, 1, "Tháng *:");         g.add(spThang,  1, 1);
        lbl(g, 2, "Năm *:");           g.add(spNam,    1, 2);
        lbl(g, 3, "Số tiền *:");       g.add(tfSoTien, 1, 3);
        lbl(g, 4, "Hạn thanh toán:"); g.add(dpHan,    1, 4);

        dialog.getDialogPane().setContent(g);
        ButtonType btnTao = new ButtonType("Tạo hóa đơn", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnTao, ButtonType.CANCEL);

        // Xác thực trước khi lưu
        Button btnOk = (Button) dialog.getDialogPane().lookupButton(btnTao);
        btnOk.addEventFilter(javafx.event.ActionEvent.ACTION, evt -> {
            if (tfMaHD.getText().trim().isEmpty()) {
                AlertUtils.showWarning("Thiếu thông tin", "Vui lòng nhập mã hợp đồng.");
                evt.consume(); return;
            }
            double soTien;
            try { soTien = Double.parseDouble(tfSoTien.getText().trim()); }
            catch (NumberFormatException ex) {
                AlertUtils.showWarning("Số tiền không hợp lệ", "Vui lòng nhập số tiền hợp lệ.");
                evt.consume(); return;
            }
            if (soTien <= 0) {
                AlertUtils.showWarning("Số tiền không hợp lệ", "Số tiền phải lớn hơn 0.");
                evt.consume();
            }
        });

        dialog.showAndWait().ifPresent(bt -> {
            if (bt != btnTao) return;
            try {
                double soTien = Double.parseDouble(tfSoTien.getText().trim());
                ThanhToan created = service.taoHoaDon(
                        tfMaHD.getText().trim(),
                        spThang.getValue(),
                        spNam.getValue(),
                        soTien,
                        dpHan.getValue()
                );
                AlertUtils.showSuccess("Tạo bill thành công",
                        "Đã tạo hóa đơn " + created.getMaThanhToan() +
                        "\nTháng " + created.getThang() + "/" + created.getNam() +
                        " — " + FormatUtils.formatCurrency(soTien));
                loadData();
            } catch (Exception e) {
                AlertUtils.showError("Lỗi tạo hóa đơn", e.getMessage());
                showTaoBillDialog(); // Mở lại nếu lỗi
            }
        });
    }

    private void lbl(GridPane g, int row, String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #374151;");
        l.setPrefWidth(130);
        g.add(l, 0, row);
    }
}
