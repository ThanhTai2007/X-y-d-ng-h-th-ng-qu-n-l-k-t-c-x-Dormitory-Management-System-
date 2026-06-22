package controller;

import dao.TaiKhoanDAO;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import model.TaiKhoan;
import service.AuthService;
import utils.AlertUtils;

import java.time.format.DateTimeFormatter;
import java.util.List;

// Quản lý tài khoản (Admin)
public class TaiKhoanController {

    @FXML private TextField             txtSearch;
    @FXML private Label                 lblCount;

    @FXML private TableView<TaiKhoan>            tblTaiKhoan;
    @FXML private TableColumn<TaiKhoan, Integer> colID;
    @FXML private TableColumn<TaiKhoan, String>  colUsername;
    @FXML private TableColumn<TaiKhoan, String>  colHoTen;
    @FXML private TableColumn<TaiKhoan, String>  colRole;
    @FXML private TableColumn<TaiKhoan, String>  colEmail;
    @FXML private TableColumn<TaiKhoan, String>  colPhone;
    @FXML private TableColumn<TaiKhoan, String>  colStatus;
    @FXML private TableColumn<TaiKhoan, String>  colNgayTao;

    private final TaiKhoanDAO dao = new TaiKhoanDAO();
    private final ObservableList<TaiKhoan> masterList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTable();
        loadData();
        // Sửa nhanh (nhấp đúp)
        tblTaiKhoan.setRowFactory(tv -> {
            TableRow<TaiKhoan> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !row.isEmpty())
                    handleEdit();
            });
            return row;
        });
    }

    // ── Cấu hình cột bảng ───────────────────────────────────────
    private void setupTable() {
        colID      .setCellValueFactory(new PropertyValueFactory<>("taiKhoanID"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colHoTen   .setCellValueFactory(new PropertyValueFactory<>("hoTen"));
        colRole    .setCellValueFactory(new PropertyValueFactory<>("roleName"));
        colEmail   .setCellValueFactory(new PropertyValueFactory<>("email"));
        colPhone   .setCellValueFactory(new PropertyValueFactory<>("soDienThoai"));

        // Huy hiệu trạng thái
        colStatus.setCellValueFactory(new PropertyValueFactory<>("trangThai"));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) { setGraphic(null); setText(null); return; }
                Label b = new Label(s);
                b.getStyleClass().addAll("badge",
                        "Active".equals(s) ? "badge-success" : "badge-danger");
                setGraphic(b); setText(null);
            }
        });

        // Ngày tạo
        colNgayTao.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getNgayTao() != null
                ? c.getValue().getNgayTao()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                : "—"));

        tblTaiKhoan.setItems(masterList);
        tblTaiKhoan.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    // ── Tải dữ liệu ─────────────────────────────────────────────
    private void loadData() {
        try {
            List<TaiKhoan> list = dao.findAll();
            masterList.setAll(list);
            lblCount.setText("Tổng: " + list.size() + " tài khoản");
        } catch (Exception e) {
            AlertUtils.showError("Lỗi tải dữ liệu", e.getMessage());
        }
    }

    // ── Tìm kiếm ────────────────────────────────────────────────
    @FXML
    public void handleSearch() {
        String kw = txtSearch.getText().trim().toLowerCase();
        if (kw.isEmpty()) { loadData(); return; }
        List<TaiKhoan> filtered = masterList.filtered(tk ->
                (tk.getUsername() != null && tk.getUsername().toLowerCase().contains(kw)) ||
                (tk.getHoTen()    != null && tk.getHoTen()   .toLowerCase().contains(kw)) ||
                (tk.getEmail()    != null && tk.getEmail()    .toLowerCase().contains(kw)));
        tblTaiKhoan.setItems(FXCollections.observableArrayList(filtered));
        lblCount.setText("Tổng: " + filtered.size() + " tài khoản");
    }

    // ── Làm mới ──────────────────────────────────────────────────
    @FXML
    public void handleRefresh() {
        txtSearch.clear();
        tblTaiKhoan.setItems(masterList);
        loadData();
    }

    // ── Sửa thông tin / đổi Role ────────────────────────────────
    @FXML
    public void handleEdit() {
        if (!AuthService.isAdmin()) {
            AlertUtils.showWarning("Không có quyền", "Chỉ Admin mới được chỉnh sửa tài khoản.");
            return;
        }
        TaiKhoan sel = getSelected("chỉnh sửa");
        if (sel == null) return;

        Dialog<TaiKhoan> dialog = new Dialog<>();
        dialog.getDialogPane().getStylesheets().add(
                getClass().getResource("/view/css/style.css").toExternalForm());
        dialog.setTitle("✏️  Sửa tài khoản – " + sel.getUsername());
        dialog.getDialogPane().setPrefWidth(460);

        TextField tfHoTen   = new TextField(sel.getHoTen());
        TextField tfEmail   = new TextField(sel.getEmail() != null ? sel.getEmail() : "");
        TextField tfPhone   = new TextField(sel.getSoDienThoai() != null ? sel.getSoDienThoai() : "");
        ComboBox<String> cboRole = new ComboBox<>(
                FXCollections.observableArrayList("Admin", "Staff"));
        cboRole.setValue(sel.getRoleName());

        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(12); grid.setPadding(new Insets(20));
        addLabel(grid, 0, "Họ tên *:");      grid.add(tfHoTen, 1, 0);
        addLabel(grid, 1, "Email:");         grid.add(tfEmail,  1, 1);
        addLabel(grid, 2, "SĐT:");           grid.add(tfPhone,  1, 2);
        addLabel(grid, 3, "Vai trò *:");     grid.add(cboRole,  1, 3);

        dialog.getDialogPane().setContent(grid);
        ButtonType btnLuu = new ButtonType("💾  Lưu", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnLuu, ButtonType.CANCEL);

        // Xác thực dữ liệu
        Button btnOk = (Button) dialog.getDialogPane().lookupButton(btnLuu);
        btnOk.addEventFilter(javafx.event.ActionEvent.ACTION, evt -> {
            if (tfHoTen.getText().trim().isEmpty()) {
                AlertUtils.showWarning("Thiếu thông tin", "Họ tên không được để trống.");
                evt.consume();
            }
        });

        dialog.setResultConverter(bt -> {
            if (bt != btnLuu) return null;
            TaiKhoan updated = new TaiKhoan();
            updated.setTaiKhoanID (sel.getTaiKhoanID());
            updated.setHoTen      (tfHoTen.getText().trim());
            updated.setEmail      (tfEmail.getText().trim());
            updated.setSoDienThoai(tfPhone.getText().trim());
            updated.setRoleID     ("Admin".equals(cboRole.getValue()) ? 1 : 2);
            updated.setTrangThai  (sel.getTrangThai()); // giữ nguyên trạng thái
            return updated;
        });

        dialog.showAndWait().ifPresent(updated -> {
            try {
                dao.update(updated);
                AlertUtils.showSuccess("Thành công", "Đã cập nhật tài khoản.");
                loadData();
            } catch (Exception e) {
                AlertUtils.showError("Lỗi cập nhật", e.getMessage());
            }
        });
    }

    // ── Đặt lại mật khẩu ────────────────────────────────────────
    @FXML
    public void handleResetPassword() {
        if (!AuthService.isAdmin()) {
            AlertUtils.showWarning("Không có quyền", "Chỉ Admin mới được đặt lại mật khẩu.");
            return;
        }
        TaiKhoan sel = getSelected("đặt lại mật khẩu");
        if (sel == null) return;

        TextInputDialog dlg = new TextInputDialog("123456");
        dlg.setTitle("🔑  Đặt lại mật khẩu");
        dlg.setHeaderText("Tài khoản: " + sel.getUsername());
        dlg.setContentText("Mật khẩu mới (tối thiểu 6 ký tự):");

        dlg.showAndWait().ifPresent(newPwd -> {
            if (newPwd.length() < 6) {
                AlertUtils.showWarning("Mật khẩu quá ngắn", "Mật khẩu phải ít nhất 6 ký tự.");
                return;
            }
            try {
                String hashed = utils.PasswordUtils.hash(newPwd);
                dao.changePassword(sel.getTaiKhoanID(), hashed);
                AlertUtils.showSuccess("Thành công",
                        "Đã đặt lại mật khẩu cho tài khoản " + sel.getUsername() + ".");
            } catch (Exception e) {
                AlertUtils.showError("Lỗi", e.getMessage());
            }
        });
    }

    // ── Kích hoạt / Khoá tài khoản ──────────────────────────────
    @FXML
    public void handleToggleStatus() {
        if (!AuthService.isAdmin()) {
            AlertUtils.showWarning("Không có quyền", "Chỉ Admin mới được thay đổi trạng thái tài khoản.");
            return;
        }
        TaiKhoan sel = getSelected("thay đổi trạng thái");
        if (sel == null) return;

        // Chặn khóa chính mình
        TaiKhoan current = AuthService.getCurrentUser();
        if (current != null && current.getTaiKhoanID() == sel.getTaiKhoanID()) {
            AlertUtils.showWarning("Không hợp lệ", "Bạn không thể khoá chính tài khoản của mình.");
            return;
        }

        String newStatus = "Active".equals(sel.getTrangThai()) ? "Inactive" : "Active";
        String action    = "Active".equals(newStatus) ? "kích hoạt" : "khoá";

        if (AlertUtils.showConfirmation("Xác nhận",
                "Bạn muốn " + action + " tài khoản " + sel.getUsername() + "?")) {
            try {
                TaiKhoan upd = new TaiKhoan();
                upd.setTaiKhoanID (sel.getTaiKhoanID());
                upd.setHoTen      (sel.getHoTen());
                upd.setEmail      (sel.getEmail());
                upd.setSoDienThoai(sel.getSoDienThoai());
                upd.setRoleID     (sel.getRoleID());
                upd.setTrangThai  (newStatus);
                dao.update(upd);
                AlertUtils.showSuccess("Thành công",
                        "Tài khoản " + sel.getUsername() + " đã được " + action + ".");
                loadData();
            } catch (Exception e) {
                AlertUtils.showError("Lỗi", e.getMessage());
            }
        }
    }

    // ── Helpers ─────────────────────────────────────────────────
    private TaiKhoan getSelected(String action) {
        TaiKhoan sel = tblTaiKhoan.getSelectionModel().getSelectedItem();
        if (sel == null)
            AlertUtils.showWarning("Chưa chọn", "Vui lòng chọn tài khoản để " + action + ".");
        return sel;
    }

    private void addLabel(GridPane g, int row, String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #374151;");
        l.setPrefWidth(120);
        g.add(l, 0, row);
    }
}
