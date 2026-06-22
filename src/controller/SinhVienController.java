package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import model.SinhVien;
import service.SinhVienService;
import utils.AlertUtils;
import utils.ExportDialog;
import utils.FormatUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class SinhVienController {

    @FXML
    private TextField txtSearch;
    @FXML
    private ComboBox<String> cboFilter;
    @FXML
    private Label lblCount;
    @FXML
    private Label chipTongSV;
    @FXML
    private Label chipDangHoc;
    @FXML
    private Label chipDaNghi;
    @FXML
    private Label chipTotNghiep;
    @FXML
    private TableView<SinhVien> tblSinhVien;
    @FXML
    private TableColumn<SinhVien, String> colMa;
    @FXML
    private TableColumn<SinhVien, String> colHoTen;
    @FXML
    private TableColumn<SinhVien, LocalDate> colNgaySinh;
    @FXML
    private TableColumn<SinhVien, String> colGioiTinh;
    @FXML
    private TableColumn<SinhVien, String> colCCCD;
    @FXML
    private TableColumn<SinhVien, String> colSoDienThoai;
    @FXML
    private TableColumn<SinhVien, String> colKhoa;
    @FXML
    private TableColumn<SinhVien, String> colTrangThai;

    private final SinhVienService service = new SinhVienService();
    private final ObservableList<SinhVien> masterList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTable();
        setupFilter();
        loadData();
    }

    // Cấu hình bảng
    private void setupTable() {
        colMa.setCellValueFactory(new PropertyValueFactory<>("maSinhVien"));
        colHoTen.setCellValueFactory(new PropertyValueFactory<>("hoTen"));
        colNgaySinh.setCellValueFactory(new PropertyValueFactory<>("ngaySinh"));
        colGioiTinh.setCellValueFactory(new PropertyValueFactory<>("gioiTinh"));
        colCCCD.setCellValueFactory(new PropertyValueFactory<>("cccd"));
        colSoDienThoai.setCellValueFactory(new PropertyValueFactory<>("soDienThoai"));
        colKhoa.setCellValueFactory(new PropertyValueFactory<>("khoa"));
        colTrangThai.setCellValueFactory(new PropertyValueFactory<>("trangThai"));

        // Định dạng ngày sinh
        colNgaySinh.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate d, boolean empty) {
                super.updateItem(d, empty);
                setText(empty || d == null ? null : FormatUtils.formatDate(d));
            }
        });

        // Huy hiệu trạng thái
        colTrangThai.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null);
                    return;
                }
                Label badge = new Label(status);
                badge.getStyleClass().add("badge");
                badge.getStyleClass().add(switch (status) {
                    case "DangHoc" -> "badge-success";
                    case "DaNghiHoc" -> "badge-warning";
                    case "TotNghiep" -> "badge-gray";
                    default -> "badge-info";
                });
                setGraphic(badge);
                setText(null);
            }
        });

        tblSinhVien.setItems(masterList);
        tblSinhVien.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    // Cài đặt bộ lọc
    private void setupFilter() {
        cboFilter.setItems(FXCollections.observableArrayList(
                "Tất cả", "DangHoc", "DaNghiHoc", "TotNghiep"));
        cboFilter.setValue("Tất cả");
        cboFilter.setOnAction(e -> handleSearch());
    }

    // Tải dữ liệu
    private void loadData() {
        try {
            List<SinhVien> list = service.layTatCa();
            masterList.setAll(list);
            lblCount.setText("Hiển thị: " + list.size() + " sinh viên");
            updateChips(list);
        } catch (Exception e) {
            AlertUtils.showError("Lỗi", "Không tải được dữ liệu: " + e.getMessage());
        }
    }

    // Cập nhật thẻ tóm tắt
    private void updateChips(List<SinhVien> list) {
        long dangHoc = list.stream().filter(s -> "DangHoc".equals(s.getTrangThai())).count();
        long daNghi = list.stream().filter(s -> "DaNghiHoc".equals(s.getTrangThai())).count();
        long totNghiep = list.stream().filter(s -> "TotNghiep".equals(s.getTrangThai())).count();
        chipTongSV.setText("Tất cả: " + list.size());
        chipDangHoc.setText("Đang học: " + dangHoc);
        chipDaNghi.setText("Đã nghỉ: " + daNghi);
        chipTotNghiep.setText("Tốt nghiệp: " + totNghiep);
    }

    @FXML
    public void handleSearch() {
        try {
            String keyword = txtSearch.getText().trim();
            String filter = cboFilter.getValue();

            List<SinhVien> result;
            if (!keyword.isEmpty()) {
                result = service.timKiem(keyword);
            } else {
                result = service.locTheoTrangThai(filter);
            }
            masterList.setAll(result);
            lblCount.setText("Hiển thị: " + result.size() + " sinh viên");
        } catch (Exception e) {
            AlertUtils.showError("Lỗi tìm kiếm", e.getMessage());
        }
    }

    @FXML
    public void handleAdd() {
        showForm(null);
    }

    @FXML
    public void handleExport() {
        ExportDialog.show((javafx.stage.Stage) tblSinhVien.getScene().getWindow());
    }

    @FXML
    public void handleEdit() {
        SinhVien selected = tblSinhVien.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtils.showWarning("Chưa chọn", "Vui lòng chọn sinh viên cần sửa.");
            return;
        }
        showForm(selected);
    }

    @FXML
    public void handleDelete() {
        SinhVien selected = tblSinhVien.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtils.showWarning("Chưa chọn", "Vui lòng chọn sinh viên cần xoá.");
            return;
        }
        if (AlertUtils.showConfirmation("Xác nhận xoá",
                "Bạn chắc chắn muốn xoá sinh viên " + selected.getHoTen() + "?")) {
            try {
                service.xoaSinhVien(selected.getMaSinhVien());
                AlertUtils.showSuccess("Thành công", "Đã xoá sinh viên " + selected.getHoTen());
                loadData();
            } catch (Exception e) {
                AlertUtils.showError("Không thể xoá", e.getMessage());
            }
        }
    }

    @FXML
    public void handleRefresh() {
        txtSearch.clear();
        cboFilter.setValue("Tất cả");
        loadData();
    }

    // Form sinh viên
    private void showForm(SinhVien existing) {
        boolean isEdit = existing != null;
        Dialog<SinhVien> dialog = new Dialog<>();
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/view/css/style.css").toExternalForm());
        dialog.getDialogPane().getStyleClass().add("root");
        dialog.setTitle(isEdit ? "Sửa thông tin Sinh viên" : "Thêm Sinh viên mới");

        TextField tfMa = field(isEdit ? existing.getMaSinhVien() : "", "Tự động sinh");
        tfMa.setDisable(true);
        TextField tfHoTen = field(isEdit ? existing.getHoTen() : "", "Họ và tên *");
        DatePicker dpNS = new DatePicker(isEdit ? existing.getNgaySinh() : null);
        dpNS.setPromptText("dd/MM/yyyy");
        ComboBox<String> cboGT = new ComboBox<>(FXCollections.observableArrayList("Nam", "Nữ", "Khác"));
        if (isEdit)
            cboGT.setValue(existing.getGioiTinh());
        TextField tfCCCD = field(isEdit ? existing.getCccd() : "", "12 chữ số");
        TextField tfPhone = field(isEdit ? existing.getSoDienThoai() : "", "Số điện thoại");
        TextField tfEmail = field(isEdit ? existing.getEmail() : "", "Email");
        TextField tfKhoa = field(isEdit ? existing.getKhoa() : "", "Tên khoa/ngành");
        TextField tfNamHoc = field(isEdit ? existing.getNamHoc() : "", "VD: 2022-2026");

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        grid.setMinWidth(500);

        addRow(grid, 0, "Mã Sinh viên:", tfMa);
        addRow(grid, 1, "Họ tên *:", tfHoTen);
        addRow(grid, 2, "Ngày sinh *:", dpNS);
        addRow(grid, 3, "Giới tính *:", cboGT);
        addRow(grid, 4, "CCCD *:", tfCCCD);
        addRow(grid, 5, "Điện thoại:", tfPhone);
        addRow(grid, 6, "Email:", tfEmail);
        addRow(grid, 7, "Khoa:", tfKhoa);
        addRow(grid, 8, "Năm học:", tfNamHoc);

        dialog.getDialogPane().setContent(grid);
        ButtonType btnSave = new ButtonType(isEdit ? "Cập nhật" : "Thêm mới", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSave, ButtonType.CANCEL);

        dialog.setResultConverter(bt -> {
            if (bt != btnSave)
                return null;
            SinhVien sv = isEdit ? existing : new SinhVien();
            sv.setHoTen(tfHoTen.getText().trim());
            sv.setNgaySinh(dpNS.getValue());
            sv.setGioiTinh(cboGT.getValue());
            sv.setCccd(tfCCCD.getText().trim());
            sv.setSoDienThoai(tfPhone.getText().trim());
            sv.setEmail(tfEmail.getText().trim());
            sv.setKhoa(tfKhoa.getText().trim());
            sv.setNamHoc(tfNamHoc.getText().trim());
            return sv;
        });

        Optional<SinhVien> result = dialog.showAndWait();
        result.ifPresent(sv -> {
            try {
                if (isEdit) {
                    service.capNhatSinhVien(sv);
                    AlertUtils.showSuccess("Thành công", "Đã cập nhật sinh viên " + sv.getHoTen());
                } else {
                    service.themSinhVien(sv);
                    AlertUtils.showSuccess("Thành công", "Đã thêm sinh viên " + sv.getHoTen());
                }
                loadData();
            } catch (Exception e) {
                AlertUtils.showError("Lỗi lưu dữ liệu", e.getMessage());
                showForm(sv);
            }
        });
    }

    private TextField field(String value, String prompt) {
        TextField tf = new TextField(value);
        tf.setPromptText(prompt);
        tf.setPrefWidth(280);
        return tf;
    }

    private void addRow(GridPane g, int row, String label, javafx.scene.Node control) {
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #374151;");
        lbl.setPrefWidth(120);
        g.add(lbl, 0, row);
        g.add(control, 1, row);
    }
}
