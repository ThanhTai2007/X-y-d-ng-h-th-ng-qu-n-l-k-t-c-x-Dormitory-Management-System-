package controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import model.Phong;
import service.PhongService;
import utils.AlertUtils;
import utils.DialogUtils;
import utils.ExportDialog;
import utils.FormatUtils;
import utils.UIUtils;

import java.util.List;
import java.util.Optional;

public class PhongController {

    @FXML private TextField          txtSearch;
    @FXML private ComboBox<String>   cboFilter;
    @FXML private Label              lblCount;
    @FXML private TableView<Phong>   tblPhong;
    @FXML private TableColumn<Phong,String>  colMaPhong;
    @FXML private TableColumn<Phong,String>  colTenPhong;
    @FXML private TableColumn<Phong,Integer> colTang;
    @FXML private TableColumn<Phong,String>  colLoaiPhong;
    @FXML private TableColumn<Phong,Integer> colSucChua;
    @FXML private TableColumn<Phong,Integer> colHienTai;
    @FXML private TableColumn<Phong,String>  colChoTrong;
    @FXML private TableColumn<Phong,String>  colGiaThue;
    @FXML private TableColumn<Phong,String>  colTrangThai;

    private final PhongService service = new PhongService();
    private final ObservableList<Phong> masterList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTable();
        setupFilter();
        loadData();
    }

    private void setupTable() {
        colMaPhong  .setCellValueFactory(new PropertyValueFactory<>("maPhong"));
        colTenPhong .setCellValueFactory(new PropertyValueFactory<>("tenPhong"));
        colTang     .setCellValueFactory(new PropertyValueFactory<>("tang"));
        colLoaiPhong.setCellValueFactory(new PropertyValueFactory<>("loaiPhong"));
        colSucChua  .setCellValueFactory(new PropertyValueFactory<>("sucChua"));

        // Cột "Hiện tại"
        colHienTai.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Integer val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null); return;
                }
                Phong p = (Phong) getTableRow().getItem();
                int cur = p.getSoNguoiHienTai();
                int cap = p.getSucChua();
                double ratio = cap > 0 ? (double) cur / cap : 0;

                ProgressBar pb = new ProgressBar(ratio);
                pb.setPrefWidth(70);
                pb.setPrefHeight(7);
                pb.getStyleClass().add("progress-bar-room");
                if (ratio >= 1.0)       pb.getStyleClass().add("progress-bar-room-full");
                else if (ratio >= 0.75) pb.getStyleClass().add("progress-bar-room-warn");

                Label lbl = new Label(cur + "/" + cap);
                lbl.getStyleClass().add("occupancy-text");

                HBox cell = new HBox(6, pb, lbl);
                cell.setAlignment(Pos.CENTER_LEFT);
                cell.getStyleClass().add("occupancy-cell");
                setGraphic(cell);
                setText(null);
            }
        });
        colHienTai.setCellValueFactory(new PropertyValueFactory<>("soNguoiHienTai"));

        // Cột "Còn trống"
        colChoTrong.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getSoChoTrong() + " chỗ"));

        // Định dạng giá thuê
        colGiaThue.setCellValueFactory(c ->
                new SimpleStringProperty(FormatUtils.formatCurrency(c.getValue().getGiaThue())));

        // Huy hiệu trạng thái
        colTrangThai.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) { setGraphic(null); return; }
                Label b = new Label(FormatUtils.trangThaiPhong(s));
                b.getStyleClass().addAll("badge", switch (s) {
                    case "Trong"  -> "badge-success";
                    case "Day"    -> "badge-danger";
                    case "BaoTri" -> "badge-warning";
                    default       -> "badge-gray";
                });
                setGraphic(b); setText(null);
            }
        });

        tblPhong.setItems(masterList);
        tblPhong.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        UIUtils.setEmptyPlaceholder(tblPhong, "🏠", "Chưa có phòng nào",
                "Thêm phòng đầu tiên để bắt đầu quản lý");
    }

    private void setupFilter() {
        cboFilter.setItems(FXCollections.observableArrayList("Tất cả", "Trong", "Day", "BaoTri"));
        cboFilter.setValue("Tất cả");
        cboFilter.setOnAction(e -> handleSearch());
    }

    private void loadData() {
        try {
            List<Phong> list = service.layTatCa();
            masterList.setAll(list);
            lblCount.setText("Tổng: " + list.size() + " phòng");
        } catch (Exception e) {
            AlertUtils.showError("Lỗi", e.getMessage());
        }
    }

    @FXML public void handleSearch() {
        try {
            String kw = txtSearch.getText().trim();
            String st = cboFilter.getValue();
            List<Phong> result = service.timKiem(kw, st);
            masterList.setAll(result);
            lblCount.setText("Kết quả: " + result.size() + " phòng");
        } catch (Exception e) {
            AlertUtils.showError("Lỗi", e.getMessage());
        }
    }

    @FXML public void handleAdd()    { showForm(null); }
    @FXML public void handleRefresh(){ txtSearch.clear(); cboFilter.setValue("Tất cả"); loadData(); }
    @FXML public void handleExport() { ExportDialog.show((javafx.stage.Stage) tblPhong.getScene().getWindow()); }

    @FXML public void handleEdit() {
        Phong sel = tblPhong.getSelectionModel().getSelectedItem();
        if (sel == null) { AlertUtils.showWarning("Chưa chọn", "Vui lòng chọn phòng cần sửa."); return; }
        showForm(sel);
    }

    @FXML public void handleDelete() {
        Phong sel = tblPhong.getSelectionModel().getSelectedItem();
        if (sel == null) { AlertUtils.showWarning("Chưa chọn", "Vui lòng chọn phòng cần xoá."); return; }
        if (sel.getSoNguoiHienTai() > 0) {
            AlertUtils.showError("Không thể xoá", "Phòng " + sel.getMaPhong() + " đang có sinh viên ở. Không thể xoá.");
            return;
        }
        if (DialogUtils.showDangerConfirm("Xác nhận xoá phòng",
                "Bạn chắc chắn muốn xoá phòng \"" + sel.getTenPhong() + "\"?\n" +
                "Hành động này không thể hoàn tác.", "🗑  Xoá phòng")) {
            try {
                service.xoaPhong(sel.getMaPhong());
                AlertUtils.showSuccess("Thành công", "Đã xoá phòng " + sel.getTenPhong());
                loadData();
            } catch (Exception e) {
                AlertUtils.showError("Lỗi", e.getMessage());
            }
        }
    }

    @FXML public void handleBaoTri() {
        Phong sel = tblPhong.getSelectionModel().getSelectedItem();
        if (sel == null) { AlertUtils.showWarning("Chưa chọn", "Vui lòng chọn phòng."); return; }
        String newStatus = "BaoTri".equals(sel.getTrangThai()) ? "Trong" : "BaoTri";
        String title = "BaoTri".equals(newStatus) ? "Chuyển sang Bảo trì" : "Kích hoạt lại phòng";
        String msg   = "BaoTri".equals(newStatus)
                ? "Phòng " + sel.getMaPhong() + " sẽ được chuyển sang trạng thái Bảo trì."
                : "Phòng " + sel.getMaPhong() + " sẽ được kích hoạt lại (Còn trống).";
        if (DialogUtils.showConfirm(title, msg)) {
            try {
                service.capNhatTrangThai(sel.getMaPhong(), newStatus);
                loadData();
            } catch (Exception e) {
                AlertUtils.showError("Lỗi", e.getMessage());
            }
        }
    }

    private void showForm(Phong existing) {
        boolean isEdit = existing != null;
        Dialog<Phong> dialog = DialogUtils.buildStyledDialog(
                isEdit ? "Sửa thông tin Phòng" : "Thêm Phòng mới",
                isEdit ? "Cập nhật thông tin phòng " + existing.getMaPhong() : "Điền đầy đủ thông tin bên dưới",
                isEdit ? "✏" : "🏠");

        TextField tfMa    = new TextField(isEdit ? existing.getMaPhong()   : ""); tfMa.setPromptText("VD: P101"); if(isEdit) tfMa.setDisable(true);
        TextField tfTen   = new TextField(isEdit ? existing.getTenPhong()  : ""); tfTen.setPromptText("Tên phòng");
        Spinner<Integer> spTang = new Spinner<>(1, 30, isEdit ? existing.getTang() : 1);
        ComboBox<String> cboLoai = new ComboBox<>(FXCollections.observableArrayList("Don","Doi","Tap-the"));
        if(isEdit) cboLoai.setValue(existing.getLoaiPhong()); else cboLoai.setValue("Tap-the");
        Spinner<Integer> spSucChua = new Spinner<>(1, 20, isEdit ? existing.getSucChua() : 6);
        TextField tfGia   = new TextField(isEdit ? String.valueOf((long)existing.getGiaThue()) : ""); tfGia.setPromptText("VNĐ/tháng");
        TextField tfMoTa  = new TextField(isEdit ? existing.getMoTa() : "");

        // Áp dụng style form
        tfMa.getStyleClass().add("field-input");
        tfTen.getStyleClass().add("field-input");
        tfGia.getStyleClass().add("field-input");
        tfMoTa.getStyleClass().add("field-input");
        cboLoai.getStyleClass().add("combo-box");

        GridPane g = new GridPane(); g.setHgap(12); g.setVgap(10); g.setPadding(new Insets(4, 0, 4, 0));
        g.setMinWidth(360);
        int r = 0;
        addR(g,r++,"Mã phòng *:",  tfMa);   addR(g,r++,"Tên phòng *:", tfTen);
        addR(g,r++,"Tầng:",        spTang);  addR(g,r++,"Loại phòng:", cboLoai);
        addR(g,r++,"Sức chứa:",    spSucChua); addR(g,r++,"Giá thuê/th:", tfGia);
        addR(g,r,  "Mô tả:",       tfMoTa);

        dialog.getDialogPane().setContent(DialogUtils.buildDialogBody(g));
        ButtonType btnSave = new ButtonType(isEdit ? "Cập nhật" : "Thêm mới", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSave, ButtonType.CANCEL);

        // Style nút OK
        Button okBtn = (Button) dialog.getDialogPane().lookupButton(btnSave);
        okBtn.getStyleClass().add("btn-primary");

        dialog.setResultConverter(bt -> {
            if (bt != btnSave) return null;
            Phong p = isEdit ? existing : new Phong();
            p.setMaPhong(tfMa.getText().trim().toUpperCase());
            p.setTenPhong(tfTen.getText().trim());
            p.setTang(spTang.getValue());
            p.setLoaiPhong(cboLoai.getValue());
            p.setSucChua(spSucChua.getValue());
            p.setGiaThue(Double.parseDouble(tfGia.getText().trim().isEmpty() ? "0" : tfGia.getText().trim()));
            p.setMoTa(tfMoTa.getText().trim());
            return p;
        });

        Optional<Phong> res = dialog.showAndWait();
        res.ifPresent(p -> {
            try {
                if (isEdit) { service.capNhatPhong(p); AlertUtils.showSuccess("OK", "Đã cập nhật phòng " + p.getMaPhong()); }
                else        { service.themPhong(p);   AlertUtils.showSuccess("OK", "Đã thêm phòng " + p.getMaPhong()); }
                loadData();
            } catch (Exception e) { AlertUtils.showError("Lỗi", e.getMessage()); showForm(p); }
        });
    }

    private void addR(GridPane g, int r, String label, javafx.scene.Node ctrl) {
        Label l = new Label(label); l.getStyleClass().add("form-label"); l.setPrefWidth(110);
        g.add(l,0,r); g.add(ctrl,1,r);
    }
}
