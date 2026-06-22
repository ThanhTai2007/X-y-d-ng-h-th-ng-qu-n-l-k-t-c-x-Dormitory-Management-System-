package controller;

import dao.SinhVienDAO;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import model.SinhVien;
import model.ViPham;
import service.AuthService;
import service.ViPhamService;
import utils.AlertUtils;
import utils.FormatUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

// Quản lý vi phạm
public class ViPhamController {

    @FXML private TextField        txtSearch;
    @FXML private ComboBox<String> cboFilter;
    @FXML private Label            lblTong;
    @FXML private Label            lblChuaXuLy;
    @FXML private Label            lblDaXuLy;
    @FXML private Label            lblKhangCao;
    @FXML private Button           btnAdd;
    @FXML private Button           btnDelete;

    @FXML private TableView<ViPham>            tblViPham;
    @FXML private TableColumn<ViPham,String>   colMaVP;
    @FXML private TableColumn<ViPham,String>   colSinhVien;
    @FXML private TableColumn<ViPham,String>   colLoai;
    @FXML private TableColumn<ViPham,LocalDate> colNgay;
    @FXML private TableColumn<ViPham,String>   colMucPhat;
    @FXML private TableColumn<ViPham,String>   colMoTa;
    @FXML private TableColumn<ViPham,String>   colTrangThai;

    private final ViPhamService   service    = new ViPhamService();
    private final SinhVienDAO     sinhVienDAO = new SinhVienDAO();
    private final ObservableList<ViPham> masterList = FXCollections.observableArrayList();

    // Danh sách vi phạm
    private static final String[] LOAI_VP_VALUES = {
        "VeTreeHan","PhaHoaiTaiSan","GayOnAo","SuDungChatCam","TroChuaPhep","MangKhach","MatVeSinh","Khac"
    };

    @FXML
    public void initialize() {
        setupTable();
        setupFilter();
        applyRBAC();
        loadData();
    }

    // ── RBAC ─────────────────────────
    private void applyRBAC() {
        boolean isAdmin = AuthService.isAdmin();
        btnDelete.setVisible(isAdmin);
        btnDelete.setManaged(isAdmin);
    }

    // ── Cấu hình bảng ────────────────────────────────────────────
    private void setupTable() {
        colMaVP.setCellValueFactory(new PropertyValueFactory<>("maViPham"));

        colSinhVien.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getMaSinhVien() +
                (c.getValue().getHoTenSinhVien() != null ? " — " + c.getValue().getHoTenSinhVien() : "")));

        colLoai.setCellValueFactory(c ->
                new SimpleStringProperty(loaiViPhamLabel(c.getValue().getLoaiViPham())));

        colNgay.setCellValueFactory(new PropertyValueFactory<>("ngayViPham"));
        colNgay.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(LocalDate d, boolean empty) {
                super.updateItem(d, empty);
                setText(empty || d == null ? null : FormatUtils.formatDate(d));
            }
        });

        colMucPhat.setCellValueFactory(c ->
                new SimpleStringProperty(
                        c.getValue().getMucPhat() > 0
                        ? FormatUtils.formatCurrency(c.getValue().getMucPhat())
                        : "—"));

        colMoTa.setCellValueFactory(new PropertyValueFactory<>("moTa"));

        // Huy hiệu trạng thái vi phạm
        colTrangThai.setCellValueFactory(new PropertyValueFactory<>("trangThai"));
        colTrangThai.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) { setGraphic(null); return; }
                Label b = new Label(trangThaiLabel(s));
                b.getStyleClass().addAll("badge", switch (s) {
                    case "ChuaXuLy"  -> "badge-danger";
                    case "DaXuLy"    -> "badge-success";
                    case "DaKhangCao"-> "badge-warning";
                    default          -> "badge-gray";
                });
                setGraphic(b); setText(null);
            }
        });

        tblViPham.setItems(masterList);
        tblViPham.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Double-click
        tblViPham.setRowFactory(tv -> {
            TableRow<ViPham> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !row.isEmpty())
                    showDetail(row.getItem());
            });
            return row;
        });
    }

    private void setupFilter() {
        cboFilter.setItems(FXCollections.observableArrayList(
                "Tất cả", "ChuaXuLy", "DaXuLy", "DaKhangCao"));
        cboFilter.setValue("Tất cả");
        cboFilter.setOnAction(e -> handleSearch());
    }

    // ── Data ──────────────────────────────────────────────────────
    private void loadData() {
        try {
            List<ViPham> list = service.layTatCa();
            masterList.setAll(list);
            updateSummary(list);
        } catch (Exception e) {
            AlertUtils.showError("Lỗi tải dữ liệu", e.getMessage());
        }
    }

    private void updateSummary(List<ViPham> list) {
        long total    = list.size();
        long chua     = list.stream().filter(v -> "ChuaXuLy"  .equals(v.getTrangThai())).count();
        long da       = list.stream().filter(v -> "DaXuLy"    .equals(v.getTrangThai())).count();
        long khang    = list.stream().filter(v -> "DaKhangCao".equals(v.getTrangThai())).count();
        lblTong      .setText("Tổng: " + total);
        lblChuaXuLy  .setText("🔴  Chưa xử lý: " + chua);
        lblDaXuLy    .setText("🟢  Đã xử lý: " + da);
        lblKhangCao  .setText("🟡  Kháng cáo: " + khang);
    }

    // ── Handlers ─────────────────────────────────────────────────
    @FXML
    public void handleSearch() {
        try {
            String kw     = txtSearch.getText().trim();
            String status = cboFilter.getValue();
            List<ViPham> result = kw.isEmpty()
                    ? service.locTheoTrangThai(status)
                    : service.timKiem(kw);
            // Lọc phía client
            if (!kw.isEmpty() && !"Tất cả".equals(status))
                result = result.stream()
                        .filter(v -> status.equals(v.getTrangThai()))
                        .toList();
            masterList.setAll(result);
            updateSummary(result);
        } catch (Exception e) {
            AlertUtils.showError("Lỗi tìm kiếm", e.getMessage());
        }
    }

    @FXML
    public void handleRefresh() {
        txtSearch.clear();
        cboFilter.setValue("Tất cả");
        loadData();
    }

    @FXML
    public void handleAdd() { showAddForm(); }

    @FXML
    public void handleXuLy() {
        ViPham sel = getSelected("Đánh dấu đã xử lý");
        if (sel == null) return;
        if ("DaXuLy".equals(sel.getTrangThai())) {
            AlertUtils.showWarning("Đã xử lý", "Vi phạm này đã được xử lý rồi.");
            return;
        }
        if (AlertUtils.showConfirmation("Xác nhận", "Đánh dấu vi phạm " + sel.getMaViPham() + " là Đã xử lý?")) {
            try {
                service.capNhatTrangThai(sel.getMaViPham(), "DaXuLy");
                AlertUtils.showSuccess("Thành công", "Đã cập nhật trạng thái vi phạm.");
                loadData();
            } catch (Exception e) { AlertUtils.showError("Lỗi", e.getMessage()); }
        }
    }

    @FXML
    public void handleKhangCao() {
        ViPham sel = getSelected("Kháng cáo");
        if (sel == null) return;
        if ("DaXuLy".equals(sel.getTrangThai())) {
            AlertUtils.showWarning("Không hợp lệ", "Vi phạm đã được xử lý, không thể kháng cáo.");
            return;
        }
        if (AlertUtils.showConfirmation("Xác nhận", "Chuyển vi phạm " + sel.getMaViPham() + " sang trạng thái Kháng cáo?")) {
            try {
                service.capNhatTrangThai(sel.getMaViPham(), "DaKhangCao");
                AlertUtils.showSuccess("Thành công", "Đã cập nhật trạng thái.");
                loadData();
            } catch (Exception e) { AlertUtils.showError("Lỗi", e.getMessage()); }
        }
    }

    @FXML
    public void handleDelete() {
        if (!AuthService.isAdmin()) {
            AlertUtils.showWarning("Không có quyền", "Chỉ Admin mới có thể xoá vi phạm.");
            return;
        }
        ViPham sel = getSelected("Xoá");
        if (sel == null) return;
        if (AlertUtils.showConfirmation("Xác nhận xoá",
                "Xoá vi phạm " + sel.getMaViPham() + "? Hành động này không thể hoàn tác.")) {
            try {
                service.xoaViPham(sel.getMaViPham());
                AlertUtils.showSuccess("Đã xoá", "Vi phạm đã được xoá khỏi hệ thống.");
                loadData();
            } catch (Exception e) { AlertUtils.showError("Lỗi", e.getMessage()); }
        }
    }

    // ── Form thêm vi phạm ─────────────────────────────────
    private void showAddForm() {
        Dialog<ViPham> dialog = new Dialog<>();
        dialog.getDialogPane().getStylesheets().add(
                getClass().getResource("/view/css/style.css").toExternalForm());
        dialog.getDialogPane().getStyleClass().add("root");
        dialog.setTitle("➕  Ghi nhận Vi phạm Mới");
        dialog.getDialogPane().setPrefWidth(540);

        // Sinh viên
        ComboBox<SinhVien> cboSV = new ComboBox<>();
        cboSV.setPromptText("Chọn sinh viên...");
        cboSV.setPrefWidth(380);
        try {
            List<SinhVien> list = sinhVienDAO.findAll();
            cboSV.setItems(FXCollections.observableArrayList(list));
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

        // Loại vi phạm
        ComboBox<String> cboLoai = new ComboBox<>();
        cboLoai.setPrefWidth(380);
        cboLoai.setItems(FXCollections.observableArrayList(LOAI_VP_VALUES));
        cboLoai.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                setText(empty || s == null ? null : loaiViPhamLabel(s));
            }
        });
        cboLoai.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                setText(empty || s == null ? null : loaiViPhamLabel(s));
            }
        });

        DatePicker dpNgay = new DatePicker(LocalDate.now());
        TextArea   taMoTa = new TextArea();
        taMoTa.setPromptText("Mô tả chi tiết vi phạm (ít nhất 10 ký tự)...");
        taMoTa.setPrefRowCount(3);
        taMoTa.setWrapText(true);

        TextField tfMucPhat = new TextField("0");
        tfMucPhat.setPromptText("VNĐ (0 nếu chỉ cảnh cáo)");

        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(12); grid.setPadding(new Insets(20));
        lbl(grid, 0, "Sinh viên *:");    grid.add(cboSV,    1, 0);
        lbl(grid, 1, "Loại vi phạm *:"); grid.add(cboLoai,  1, 1);
        lbl(grid, 2, "Ngày vi phạm *:"); grid.add(dpNgay,   1, 2);
        lbl(grid, 3, "Mức phạt (VNĐ):"); grid.add(tfMucPhat,1, 3);
        lbl(grid, 4, "Mô tả *:");        grid.add(taMoTa,   1, 4);

        dialog.getDialogPane().setContent(grid);
        ButtonType btnLuu = new ButtonType("✅  Ghi nhận", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnLuu, ButtonType.CANCEL);

        // Xác thực dữ liệu
        Button btnOk = (Button) dialog.getDialogPane().lookupButton(btnLuu);
        btnOk.addEventFilter(javafx.event.ActionEvent.ACTION, evt -> {
            if (cboSV.getValue() == null) {
                AlertUtils.showWarning("Thiếu thông tin", "Vui lòng chọn sinh viên."); evt.consume(); return;
            }
            if (cboLoai.getValue() == null) {
                AlertUtils.showWarning("Thiếu thông tin", "Vui lòng chọn loại vi phạm."); evt.consume(); return;
            }
            if (taMoTa.getText().trim().length() < 10) {
                AlertUtils.showWarning("Mô tả quá ngắn", "Mô tả vi phạm phải có ít nhất 10 ký tự."); evt.consume();
            }
        });

        dialog.setResultConverter(bt -> {
            if (bt != btnLuu) return null;
            ViPham vp = new ViPham();
            vp.setMaSinhVien(cboSV.getValue().getMaSinhVien());
            vp.setLoaiViPham(cboLoai.getValue());
            vp.setNgayViPham(dpNgay.getValue());
            vp.setMoTa(taMoTa.getText().trim());
            try { vp.setMucPhat(Double.parseDouble(tfMucPhat.getText().trim())); }
            catch (NumberFormatException ex) { vp.setMucPhat(0); }
            return vp;
        });

        dialog.showAndWait().ifPresent(vp -> {
            try {
                service.ghiNhanViPham(vp);
                AlertUtils.showSuccess("Ghi nhận thành công",
                        "Vi phạm đã được lưu vào hệ thống.\nMã: " + vp.getMaViPham());
                loadData();
            } catch (Exception e) {
                AlertUtils.showError("Lỗi ghi nhận vi phạm", e.getMessage());
                showAddForm();
            }
        });
    }

    // ── Chi tiết vi phạm ──────────────────────────────────────────
    private void showDetail(ViPham vp) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Chi tiết Vi phạm");
        alert.setHeaderText("Mã VP: " + vp.getMaViPham());
        alert.setContentText(
            "👤 Sinh viên  : " + vp.getMaSinhVien() +
            (vp.getHoTenSinhVien() != null ? " (" + vp.getHoTenSinhVien() + ")" : "") + "\n" +
            "🚨 Loại VP    : " + loaiViPhamLabel(vp.getLoaiViPham()) + "\n" +
            "📅 Ngày VP    : " + (vp.getNgayViPham() != null ? FormatUtils.formatDate(vp.getNgayViPham()) : "—") + "\n" +
            "💰 Mức phạt  : " + (vp.getMucPhat() > 0 ? FormatUtils.formatCurrency(vp.getMucPhat()) : "Không phạt tiền") + "\n" +
            "📌 Trạng thái : " + trangThaiLabel(vp.getTrangThai()) + "\n\n" +
            "📝 Mô tả:\n" + vp.getMoTa()
        );
        alert.getDialogPane().setPrefWidth(480);
        alert.showAndWait();
    }

    // ── Helpers ───────────────────────────────────────────────────
    private ViPham getSelected(String action) {
        ViPham sel = tblViPham.getSelectionModel().getSelectedItem();
        if (sel == null) AlertUtils.showWarning("Chưa chọn", "Vui lòng chọn vi phạm để " + action + ".");
        return sel;
    }

    private void lbl(GridPane g, int row, String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #374151;");
        l.setPrefWidth(130);
        g.add(l, 0, row);
    }

    // Ánh xạ nhãn tiếng Việt
    static String loaiViPhamLabel(String loai) {
        if (loai == null) return "—";
        return switch (loai) {
            case "VeTreeHan"       -> "🕐 Về trễ hạn";
            case "PhaHoaiTaiSan"   -> "🔨 Phá hoại tài sản";
            case "GayOnAo"         -> "📢 Gây ồn ào";
            case "SuDungChatCam"   -> "🚬 Sử dụng chất cấm";
            case "TroChuaPhep"     -> "🏠 Trọ chưa phép";
            case "MangKhach"       -> "👥 Mang khách không đăng ký";
            case "MatVeSinh"       -> "🧹 Mất vệ sinh";
            case "Khac"            -> "📋 Khác";
            default                -> loai;
        };
    }

    static String trangThaiLabel(String tt) {
        if (tt == null) return "—";
        return switch (tt) {
            case "ChuaXuLy"   -> "Chưa xử lý";
            case "DaXuLy"     -> "Đã xử lý";
            case "DaKhangCao" -> "Đang kháng cáo";
            default           -> tt;
        };
    }
}
