package controller;

import dao.PhongDAO;
import dao.SinhVienDAO;
import dao.ThanhToanDAO;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import model.HopDong;
import model.Phong;
import model.SinhVien;
import model.ThanhToan;
import service.HopDongService;
import utils.AlertUtils;
import utils.FormatUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class HopDongController {

    // ── Ràng buộc FXML ──────────────────────────────────────────
    @FXML private TextField           txtSearch;
    @FXML private ComboBox<String>    cboFilter;
    @FXML private Label               lblTongHD;
    @FXML private Label               lblHDActive;
    @FXML private Label               lblHDExpired;
    @FXML private Label               lblHDTerminated;

    @FXML private TableView<HopDong>            tblHopDong;
    @FXML private TableColumn<HopDong,String>   colMaHD;
    @FXML private TableColumn<HopDong,String>   colMaSV;
    @FXML private TableColumn<HopDong,String>   colTenSV;
    @FXML private TableColumn<HopDong,String>   colPhong;
    @FXML private TableColumn<HopDong,LocalDate> colNgayBD;
    @FXML private TableColumn<HopDong,LocalDate> colNgayKT;
    @FXML private TableColumn<HopDong,String>   colGiaThue;
    @FXML private TableColumn<HopDong,String>   colTienCoc;
    @FXML private TableColumn<HopDong,String>   colConLai;
    @FXML private TableColumn<HopDong,String>   colTrangThai;

    private final HopDongService  service     = new HopDongService();
    private final SinhVienDAO     svDAO       = new SinhVienDAO();
    private final PhongDAO        phongDAO    = new PhongDAO();
    private final ObservableList<HopDong> masterList = FXCollections.observableArrayList();

    // Khởi tạo
    @FXML
    public void initialize() {
        setupTable();
        setupFilter();
        loadData();
    }

    // Cấu hình bảng
    private void setupTable() {
        colMaHD.setCellValueFactory(new PropertyValueFactory<>("maHopDong"));
        colMaSV.setCellValueFactory(new PropertyValueFactory<>("maSinhVien"));

        // Trường join
        colTenSV.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getTenSinhVien()));
        colPhong.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getMaPhong()));

        // Định dạng ngày
        colNgayBD.setCellValueFactory(new PropertyValueFactory<>("ngayBatDau"));
        colNgayBD.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(LocalDate d, boolean empty) {
                super.updateItem(d, empty);
                setText(empty || d == null ? null : FormatUtils.formatDate(d));
            }
        });
        colNgayKT.setCellValueFactory(new PropertyValueFactory<>("ngayKetThuc"));
        colNgayKT.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(LocalDate d, boolean empty) {
                super.updateItem(d, empty);
                if (empty || d == null) { setText(null); return; }
                // Highlight đỏ
                setText(FormatUtils.formatDate(d));
                long days = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), d);
                if (days >= 0 && days <= 30) setStyle("-fx-text-fill: #DC2626; -fx-font-weight: bold;");
                else setStyle("");
            }
        });

        // Định dạng tiền
        colGiaThue.setCellValueFactory(c ->
                new SimpleStringProperty(FormatUtils.formatCurrency(c.getValue().getGiaThue())));
        colTienCoc.setCellValueFactory(c ->
                new SimpleStringProperty(FormatUtils.formatCurrency(c.getValue().getTienCoc())));

        // Số tháng còn lại
        colConLai.setCellValueFactory(c -> {
            HopDong hd = c.getValue();
            if (!"Active".equals(hd.getTrangThai())) return new SimpleStringProperty("-");
            long months = hd.getSoThangConLai();
            String txt = months >= 0 ? months + " tháng" : "Đã hết hạn";
            return new SimpleStringProperty(txt);
        });

        // Huy hiệu trạng thái
        colTrangThai.setCellValueFactory(new PropertyValueFactory<>("trangThai"));
        colTrangThai.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) { setGraphic(null); return; }
                Label b = new Label(FormatUtils.trangThaiHopDong(s));
                b.getStyleClass().addAll("badge", switch (s) {
                    case "Active"     -> "badge-success";
                    case "Expired"    -> "badge-gray";
                    case "Terminated" -> "badge-danger";
                    case "Pending"    -> "badge-warning";
                    default           -> "badge-info";
                });
                setGraphic(b); setText(null);
            }
        });

        tblHopDong.setItems(masterList);
        tblHopDong.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Double-click
        tblHopDong.setRowFactory(tv -> {
            TableRow<HopDong> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !row.isEmpty())
                    showDetailDialog(row.getItem());
            });
            return row;
        });
    }

    private void setupFilter() {
        cboFilter.setItems(FXCollections.observableArrayList(
                "Tất cả", "Active", "Expired", "Terminated", "Pending"));
        cboFilter.setValue("Tất cả");
        cboFilter.setOnAction(e -> applyFilter());
    }

    // Tải dữ liệu
    private void loadData() {
        try {
            List<HopDong> list = service.layTatCa();
            masterList.setAll(list);
            updateSummary(list);
        } catch (Exception e) {
            AlertUtils.showError("Lỗi tải dữ liệu", e.getMessage());
        }
    }

    private void applyFilter() {
        try {
            String kw = txtSearch.getText().trim();
            String st = cboFilter.getValue();
            List<HopDong> base = kw.isEmpty() ? service.layTatCa() : service.timKiem(kw);
            List<HopDong> result = "Tất cả".equals(st)
                    ? base
                    : base.stream().filter(h -> st.equals(h.getTrangThai())).collect(Collectors.toList());
            masterList.setAll(result);
            updateSummary(service.layTatCa()); // summary dùng toàn bộ dữ liệu
        } catch (Exception e) {
            AlertUtils.showError("Lỗi", e.getMessage());
        }
    }

    private void updateSummary(List<HopDong> allList) {
        long total      = allList.size();
        long active     = allList.stream().filter(h -> "Active".equals(h.getTrangThai())).count();
        long expired    = allList.stream().filter(h -> "Expired".equals(h.getTrangThai())).count();
        long terminated = allList.stream().filter(h -> "Terminated".equals(h.getTrangThai())).count();

        lblTongHD.setText("Tổng: " + total + " hợp đồng");
        lblHDActive.setText("🟢 Active: " + active);
        lblHDExpired.setText("⚪ Hết hạn: " + expired);
        lblHDTerminated.setText("🔴 Chấm dứt: " + terminated);
    }

    // ── Action Handlers ───────────────────────────────────────
    @FXML public void handleSearch()  { applyFilter(); }
    @FXML public void handleRefresh() { txtSearch.clear(); cboFilter.setValue("Tất cả"); loadData(); }

    @FXML
    public void handleAdd() {
        showCreateForm();
    }

    @FXML
    public void handleDetail() {
        HopDong sel = getSelected("Chi tiết");
        if (sel != null) showDetailDialog(sel);
    }

    @FXML
    public void handleGiaHan() {
        HopDong sel = getSelected("Gia hạn");
        if (sel == null) return;
        if (!"Active".equals(sel.getTrangThai())) {
            AlertUtils.showWarning("Không thể gia hạn", "Chỉ gia hạn hợp đồng đang Active.");
            return;
        }
        showGiaHanDialog(sel);
    }

    @FXML
    public void handleTerminate() {
        HopDong sel = getSelected("Chấm dứt");
        if (sel == null) return;
        if (!"Active".equals(sel.getTrangThai())) {
            AlertUtils.showWarning("Không hợp lệ", "Chỉ có thể chấm dứt hợp đồng đang Active.");
            return;
        }
        showTerminateDialog(sel);
    }

    // Form tạo hợp đồng
    private void showCreateForm() {
        Dialog<HopDong> dialog = new Dialog<>();
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/view/css/style.css").toExternalForm());
        dialog.getDialogPane().getStyleClass().add("root");
        dialog.setTitle("➕  Tạo Hợp Đồng Mới");
        dialog.setHeaderText("Điền đầy đủ thông tin bên dưới");
        dialog.getDialogPane().setPrefWidth(560);

        // Sinh viên
        ComboBox<SinhVien> cboSV = new ComboBox<>();
        cboSV.setPromptText("Chọn sinh viên...");
        cboSV.setPrefWidth(350);
        try {
            // Chỉ SV đang học
            List<SinhVien> svList = svDAO.findByTrangThai("DangHoc");
            cboSV.setItems(FXCollections.observableArrayList(svList));
            cboSV.setCellFactory(lv -> new ListCell<>() {
                @Override protected void updateItem(SinhVien sv, boolean empty) {
                    super.updateItem(sv, empty);
                    setText(empty || sv == null ? null : sv.getMaSinhVien() + " — " + sv.getHoTen());
                }
            });
            cboSV.setButtonCell(new ListCell<>() {
                @Override protected void updateItem(SinhVien sv, boolean empty) {
                    super.updateItem(sv, empty);
                    setText(empty || sv == null ? null : sv.getMaSinhVien() + " — " + sv.getHoTen());
                }
            });
        } catch (Exception e) {
            AlertUtils.showError("Lỗi", "Không tải được danh sách sinh viên: " + e.getMessage());
        }

        // Phòng còn trống
        ComboBox<Phong> cboPhong = new ComboBox<>();
        cboPhong.setPromptText("Chọn phòng...");
        cboPhong.setPrefWidth(350);
        Label lblGiaThueTuDong = new Label("—");
        lblGiaThueTuDong.setStyle("-fx-font-weight: bold; -fx-text-fill: #1565C0;");

        try {
            List<Phong> phongList = phongDAO.findAvailable();
            cboPhong.setItems(FXCollections.observableArrayList(phongList));
            cboPhong.setCellFactory(lv -> new ListCell<>() {
                @Override protected void updateItem(Phong p, boolean empty) {
                    super.updateItem(p, empty);
                    if (empty || p == null) { setText(null); return; }
                    setText(p.getMaPhong() + " — " + p.getTenPhong()
                            + "  [" + p.getSoNguoiHienTai() + "/" + p.getSucChua() + " người]"
                            + "  " + FormatUtils.formatCurrency(p.getGiaThue()) + "/th");
                }
            });
            cboPhong.setButtonCell(new ListCell<>() {
                @Override protected void updateItem(Phong p, boolean empty) {
                    super.updateItem(p, empty);
                    setText(empty || p == null ? null : p.getMaPhong() + " — " + p.getTenPhong());
                }
            });
            // Tự điền giá thuê
            cboPhong.setOnAction(e -> {
                Phong sel = cboPhong.getValue();
                if (sel != null)
                    lblGiaThueTuDong.setText(FormatUtils.formatCurrency(sel.getGiaThue()) + "/tháng");
            });
        } catch (Exception e) {
            AlertUtils.showError("Lỗi", "Không tải được danh sách phòng: " + e.getMessage());
        }

        DatePicker dpBD = new DatePicker(LocalDate.now());
        DatePicker dpKT = new DatePicker(LocalDate.now().plusMonths(12));
        TextField  tfCoc = new TextField("0");
        tfCoc.setPromptText("VNĐ (0 nếu không có)");

        // Bố cục form
        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(14);
        grid.setPadding(new Insets(20));

        lbl(grid, 0, "Sinh viên *:");    grid.add(cboSV,    1, 0);
        lbl(grid, 1, "Phòng *:");        grid.add(cboPhong, 1, 1);
        lbl(grid, 2, "Giá thuê:");
        grid.add(lblGiaThueTuDong,       1, 2);
        lbl(grid, 3, "Ngày bắt đầu *:"); grid.add(dpBD,     1, 3);
        lbl(grid, 4, "Ngày kết thúc *:");grid.add(dpKT,     1, 4);
        lbl(grid, 5, "Tiền cọc:");       grid.add(tfCoc,    1, 5);

        // Hộp thông tin hướng dẫn
        Label infoBox = new Label(
            "ℹ️  Hệ thống sẽ tự động:\n" +
            "  • Cập nhật số người trong phòng\n" +
            "  • Tạo hóa đơn tháng đầu tiên\n" +
            "  • Kiểm tra 1 SV chỉ có 1 hợp đồng Active");
        infoBox.setStyle("-fx-background-color: #EFF6FF; -fx-text-fill: #1E40AF; " +
                         "-fx-padding: 12; -fx-background-radius: 8; -fx-font-size: 12px;");
        infoBox.setWrapText(true);
        grid.add(infoBox, 0, 6, 2, 1);

        dialog.getDialogPane().setContent(grid);
        ButtonType btnTao = new ButtonType("✅  Tạo hợp đồng", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnTao, ButtonType.CANCEL);

        // Xác nhận
        Button btnOk = (Button) dialog.getDialogPane().lookupButton(btnTao);
        btnOk.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (cboSV.getValue() == null) {
                AlertUtils.showWarning("Thiếu thông tin", "Vui lòng chọn sinh viên.");
                event.consume(); return;
            }
            if (cboPhong.getValue() == null) {
                AlertUtils.showWarning("Thiếu thông tin", "Vui lòng chọn phòng.");
                event.consume(); return;
            }
            if (dpKT.getValue() == null || !dpKT.getValue().isAfter(dpBD.getValue())) {
                AlertUtils.showWarning("Ngày không hợp lệ", "Ngày kết thúc phải sau ngày bắt đầu.");
                event.consume();
            }
        });

        dialog.setResultConverter(bt -> {
            if (bt != btnTao) return null;
            HopDong hd = new HopDong();
            hd.setMaSinhVien (cboSV.getValue().getMaSinhVien());
            hd.setMaPhong    (cboPhong.getValue().getMaPhong());
            hd.setNgayBatDau (dpBD.getValue());
            hd.setNgayKetThuc(dpKT.getValue());
            try { hd.setTienCoc(Double.parseDouble(tfCoc.getText().trim())); }
            catch (NumberFormatException ex) { hd.setTienCoc(0); }
            return hd;
        });

        dialog.showAndWait().ifPresent(hd -> {
            try {
                service.taoHopDong(hd);
                AlertUtils.showSuccess("Tạo hợp đồng thành công",
                        "Hợp đồng " + hd.getMaHopDong() + " đã được tạo.\n" +
                        "Hóa đơn tháng đầu đã được tạo tự động.");
                loadData();
            } catch (Exception e) {
                AlertUtils.showError("Lỗi tạo hợp đồng", e.getMessage());
                showCreateForm(); // Mở lại form nếu lỗi
            }
        });
    }

    // Dialog chi tiết
    private void showDetailDialog(HopDong hd) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Chi tiết Hợp đồng");
        alert.setHeaderText("Mã HĐ: " + hd.getMaHopDong());

        // Thông tin chung
        StringBuilder sb = new StringBuilder();
        sb.append("👤 Sinh viên : ").append(hd.getMaSinhVien())
          .append("  (").append(hd.getTenSinhVien() != null ? hd.getTenSinhVien() : "—").append(")\n");
        sb.append("🏠 Phòng     : ").append(hd.getMaPhong())
          .append("  (").append(hd.getTenPhong() != null ? hd.getTenPhong() : "—").append(")\n");
        sb.append("📅 Thời hạn  : ").append(FormatUtils.formatDate(hd.getNgayBatDau()))
          .append(" → ").append(FormatUtils.formatDate(hd.getNgayKetThuc())).append("\n");
        sb.append("💰 Giá thuê  : ").append(FormatUtils.formatCurrency(hd.getGiaThue())).append("/tháng\n");
        sb.append("🏦 Tiền cọc  : ").append(FormatUtils.formatCurrency(hd.getTienCoc())).append("\n");
        sb.append("📌 Trạng thái: ").append(FormatUtils.trangThaiHopDong(hd.getTrangThai())).append("\n");
        if (hd.getGhiChu() != null && !hd.getGhiChu().isBlank())
            sb.append("📝 Ghi chú   : ").append(hd.getGhiChu()).append("\n");

        // Lịch sử hóa đơn
        try {
            ThanhToanDAO ttDAO = new ThanhToanDAO();
            List<ThanhToan> bills = ttDAO.findByHopDong(hd.getMaHopDong());
            sb.append("\n── Lịch sử hóa đơn (").append(bills.size()).append(") ──\n");
            bills.forEach(tt -> sb.append("  T").append(tt.getThang()).append("/").append(tt.getNam())
                    .append(" | ").append(FormatUtils.formatCurrency(tt.getSoTien()))
                    .append(" | ").append(FormatUtils.trangThaiThanhToan(tt.getTrangThai()))
                    .append("\n"));
        } catch (Exception ignored) {}

        alert.setContentText(sb.toString());
        alert.getDialogPane().setPrefWidth(500);
        alert.showAndWait();
    }

    // Dialog gia hạn
    private void showGiaHanDialog(HopDong hd) {
        Dialog<LocalDate> dialog = new Dialog<>();
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/view/css/style.css").toExternalForm());
        dialog.getDialogPane().getStyleClass().add("root");
        dialog.setTitle("⏳  Gia hạn Hợp đồng");
        dialog.setHeaderText("HĐ: " + hd.getMaHopDong() + "  |  Kết thúc hiện tại: "
                + FormatUtils.formatDate(hd.getNgayKetThuc()));

        DatePicker dp = new DatePicker(hd.getNgayKetThuc().plusMonths(6));
        VBox box = new VBox(10,
                new Label("Ngày kết thúc mới:"), dp,
                new Label("(Phải sau " + FormatUtils.formatDate(hd.getNgayKetThuc()) + ")"){{
                    setStyle("-fx-font-size: 11px; -fx-text-fill: #9CA3AF;");
                }});
        box.setPadding(new Insets(20));
        dialog.getDialogPane().setContent(box);

        ButtonType btnGH = new ButtonType("✅  Xác nhận gia hạn", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnGH, ButtonType.CANCEL);

        dialog.setResultConverter(bt -> bt == btnGH ? dp.getValue() : null);
        dialog.showAndWait().ifPresent(newDate -> {
            try {
                service.giaHanHopDong(hd.getMaHopDong(), newDate);
                AlertUtils.showSuccess("Gia hạn thành công",
                        "Hợp đồng " + hd.getMaHopDong() + " đã được gia hạn đến "
                        + FormatUtils.formatDate(newDate));
                loadData();
            } catch (Exception e) {
                AlertUtils.showError("Lỗi gia hạn", e.getMessage());
            }
        });
    }

    // Dialog chấm dứt
    private void showTerminateDialog(HopDong hd) {
        Dialog<String> dialog = new Dialog<>();
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/view/css/style.css").toExternalForm());
        dialog.getDialogPane().getStyleClass().add("root");
        dialog.setTitle("❌  Chấm dứt Hợp đồng");
        dialog.setHeaderText("Hành động này không thể hoàn tác!");

        TextArea taLyDo = new TextArea();
        taLyDo.setPromptText("Nhập lý do chấm dứt hợp đồng...");
        taLyDo.setPrefRowCount(3);
        taLyDo.setWrapText(true);

        Label warn = new Label(
            "⚠️  Khi chấm dứt:\n" +
            "  • HĐ chuyển sang Terminated\n" +
            "  • Số người phòng tự giảm\n" +
            "  • Tiền cọc cần xử lý riêng");
        warn.setStyle("-fx-text-fill: #92400E; -fx-background-color: #FEF3C7; " +
                      "-fx-padding: 10; -fx-background-radius: 6; -fx-font-size: 12px;");
        warn.setWrapText(true);

        VBox box = new VBox(10,
                new Label("Hợp đồng: " + hd.getMaHopDong() + " — " + hd.getTenSinhVien()),
                warn,
                new Label("Lý do chấm dứt *:"), taLyDo);
        box.setPadding(new Insets(20));
        box.setPrefWidth(460);

        dialog.getDialogPane().setContent(box);
        ButtonType btnXN = new ButtonType("❌  Xác nhận chấm dứt", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnXN, ButtonType.CANCEL);

        // Style nút xác nhận
        Button btnOk = (Button) dialog.getDialogPane().lookupButton(btnXN);
        btnOk.setStyle("-fx-background-color: #C62828; -fx-text-fill: white; -fx-font-weight: bold;");
        btnOk.addEventFilter(javafx.event.ActionEvent.ACTION, evt -> {
            if (taLyDo.getText().trim().isEmpty()) {
                AlertUtils.showWarning("Thiếu thông tin", "Vui lòng nhập lý do chấm dứt.");
                evt.consume();
            }
        });

        dialog.setResultConverter(bt -> bt == btnXN ? taLyDo.getText().trim() : null);
        dialog.showAndWait().ifPresent(lyDo -> {
            try {
                service.chamDutHopDong(hd.getMaHopDong(), lyDo);
                AlertUtils.showSuccess("Chấm dứt thành công",
                        "Hợp đồng " + hd.getMaHopDong() + " đã được chấm dứt.");
                loadData();
            } catch (Exception e) {
                AlertUtils.showError("Lỗi", e.getMessage());
            }
        });
    }

    private HopDong getSelected(String action) {
        HopDong sel = tblHopDong.getSelectionModel().getSelectedItem();
        if (sel == null) AlertUtils.showWarning("Chưa chọn", "Vui lòng chọn hợp đồng để " + action + ".");
        return sel;
    }

    private void lbl(GridPane g, int row, String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #374151;");
        l.setPrefWidth(130);
        g.add(l, 0, row);
    }
}
