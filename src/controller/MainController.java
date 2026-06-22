package controller;


import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Popup;
import javafx.util.Duration;
import model.TaiKhoan;
import service.AuthService;
import utils.AlertUtils;
import utils.NotificationManager;
import utils.NotificationService;
import utils.SceneManager;
import utils.ToastManager;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import java.util.Objects;

public class MainController {

    // ── Ràng buộc FXML ───────────────────────────────────────────
    @FXML
    private BorderPane rootPane;
    @FXML
    private StackPane contentArea;
    @FXML
    private Label lblPageTitle;
    @FXML
    private Label lblUserName;
    @FXML
    private Label lblUserRole;
    @FXML
    private Label lblCurrentUser;
    @FXML
    private Label lblAvatar;
    @FXML
    private Label lblClock;

    @FXML
    private Label lblTopbarAvatar; // avatar
    @FXML
    private HBox topbarAccountRow;

    @FXML
    private Button btnDashboard;
    @FXML
    private Button btnSinhVien;
    @FXML
    private Button btnPhong;
    @FXML
    private Button btnHopDong;
    @FXML
    private Button btnThanhToan;
    @FXML
    private Button btnViPham;
    @FXML
    private Button btnTaiKhoan;

    // ── Chuông thông báo ─────────────────────────────────────────
    @FXML
    private Button btnBell;
    @FXML
    private Label lblNotifBadge;

    private Button activeBtn;
    private Timeline clockTimeline;
    private Popup activeNotifPopup;
    private Popup activeAccountPopup;
    private ThemeMode themeMode = ThemeMode.LIGHT;
    private LanguageMode languageMode = LanguageMode.VI;
    private String currentPageKey = "dashboard";

    private final NotificationService notifService = new NotificationService();

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter SHORT_TIME = DateTimeFormatter.ofPattern("HH:mm");

    private enum ThemeMode {
        LIGHT, DARK
    }

    private enum LanguageMode {
        VI, EN
    }

    @FXML
    public void initialize() {
        // ── Thông tin người dùng hiện tại ────────────────────────────
        TaiKhoan user = AuthService.getCurrentUser();
        if (user != null) {
            String name = user.getHoTen() != null ? user.getHoTen() : "Admin";
            String abbr = initials(name);
            lblUserName.setText("Xin chào, " + name);
            lblUserRole.setText(user.getRoleName() != null ? user.getRoleName() : "");
            lblCurrentUser.setText(name);
            lblAvatar.setText(abbr);
            if (lblTopbarAvatar != null)
                lblTopbarAvatar.setText(abbr);
        }

        // ── Phân quyền Admin ──────
        if (!AuthService.isAdmin()) {
            btnTaiKhoan.setVisible(false);
            btnTaiKhoan.setManaged(false);
        }

        // ── Khởi tạo hệ thống toast ─────────────────────────────────
        ToastManager.init(contentArea);

        // ── Khởi tạo hệ thống thông báo popup ───────────────────────
        NotificationManager.init(contentArea);

        installSidebarMotion();
        applyTheme();
        updateLocalizedChrome();

        // ── Khởi tạo đồng hồ ──────────────────────────────────
        lblClock.setText(LocalDateTime.now().format(TIME_FMT));
        clockTimeline = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> lblClock.setText(LocalDateTime.now().format(TIME_FMT))));
        clockTimeline.setCycleCount(Animation.INDEFINITE);
        clockTimeline.play();



        // ── Mở trang dashboard mặc định ──────────────────────────────
        showDashboard();
    }

    // ── Điều hướng ──────────────────────────────────
    @FXML
    public void showDashboard() {
        openPage("dashboard", "/view/fxml/dashboard.fxml", btnDashboard);
    }

    @FXML
    public void showSinhVien() {
        load("/view/fxml/sinhvien.fxml", "Quản lý Sinh viên", btnSinhVien);
    }

    @FXML
    public void showPhong() {
        load("/view/fxml/phong.fxml", "Quản lý Phòng", btnPhong);
    }

    @FXML
    public void showHopDong() {
        load("/view/fxml/hopdong.fxml", "Hợp đồng", btnHopDong);
    }

    @FXML
    public void showThanhToan() {
        load("/view/fxml/thanhtoan.fxml", "Thanh toán", btnThanhToan);
    }

    @FXML
    public void showViPham() {
        load("/view/fxml/vipham.fxml", "Quản lý Vi phạm", btnViPham);
    }

    @FXML
    public void showTaiKhoan() {
        if (!AuthService.isAdmin()) {
            AlertUtils.showWarning("Không có quyền", "Chỉ Admin mới có thể quản lý tài khoản.");
            return;
        }
        load("/view/fxml/taikhoan.fxml", "Quản lý Tài khoản", btnTaiKhoan);
    }

    @FXML
    public void handleExport() {
        utils.ExportDialog.show(SceneManager.getPrimaryStage());
    }

    @FXML
    public void handleLogout() {
        if (AlertUtils.showConfirmation("Đăng xuất", "Bạn có chắc muốn đăng xuất không?")) {
            if (clockTimeline != null)
                clockTimeline.stop();
            new AuthService().logout();
            try {
                SceneManager.showLogin();
            } catch (Exception e) {
                AlertUtils.showError("Lỗi", e.getMessage());
            }
        }
    }

    // ── Xử lý hiển thị thông báo ──────────────────────────────────
    @FXML
    public void handleShowNotifications() {
        if (activeNotifPopup != null && activeNotifPopup.isShowing()) {
            closePopup(activeNotifPopup);
            return;
        }
        closePopup(activeAccountPopup);

        List<NotificationService.NotifItem> items = notifService.getNotifications();

        Popup popup = new Popup();
        popup.setAutoHide(true);
        popup.setAutoFix(true);
        activeNotifPopup = popup;

        VBox root = buildNotifPopup(items);
        root.getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/view/css/style.css"))
                        .toExternalForm());

        popup.getContent().add(root);

        // Đặt vị trí popup
        javafx.geometry.Bounds b = btnBell.localToScreen(btnBell.getBoundsInLocal());
        popup.show(btnBell.getScene().getWindow(), b.getMaxX() - 340, b.getMaxY() + 8);
        animatePopupIn(root);
    }

    // Xây dựng popup
    private VBox buildNotifPopup(List<NotificationService.NotifItem> items) {
        VBox root = new VBox(0);
        root.getStyleClass().add("notif-popup-root");
        root.setPrefWidth(340);
        root.setMaxWidth(340);

        // Tiêu đề popup
        HBox header = new HBox(0);
        header.getStyleClass().add("notif-popup-header");
        header.setAlignment(Pos.CENTER_LEFT);

        VBox headerText = new VBox(2);
        HBox.setHgrow(headerText, Priority.ALWAYS);
        Label titleLbl = new Label("🔔  Thông báo hệ thống");
        titleLbl.getStyleClass().add("notif-popup-title");
        Label subLbl = new Label(items.size() + " mục cần chú ý");
        subLbl.getStyleClass().add("notif-popup-sub");
        headerText.getChildren().addAll(titleLbl, subLbl);
        header.getChildren().add(headerText);
        root.getChildren().add(header);

        // Nội dung popup
        if (items.size() == 1 && "notif-dot-green".equals(items.get(0).dotStyle())) {
            VBox empty = new VBox(10);
            empty.setAlignment(Pos.CENTER);
            empty.setPadding(new Insets(24, 18, 24, 18));
            Label emIcon = new Label("✅");
            emIcon.setStyle("-fx-font-size: 30px;");
            Label emMsg = new Label("Tất cả bình thường!");
            emMsg.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #1E293B;");
            Label emHint = new Label("Không có vấn đề nào cần xử lý");
            emHint.setStyle("-fx-font-size: 11px; -fx-text-fill: #94A3B8;");
            empty.getChildren().addAll(emIcon, emMsg, emHint);
            root.getChildren().add(empty);
        } else {
            for (int i = 0; i < items.size(); i++) {
                root.getChildren().add(buildNotifRow(items.get(i), i));
            }
        }

        // Chân popup
        String time = LocalTime.now().format(SHORT_TIME);
        Label footer = new Label("Cập nhật lúc " + time + "  ·  Click bên ngoài để đóng");
        footer.setStyle("-fx-font-size: 10px; -fx-text-fill: #CBD5E1; " +
                "-fx-padding: 8 18; -fx-alignment: center;");
        footer.setMaxWidth(Double.MAX_VALUE);
        root.getChildren().add(footer);

        return root;
    }

    // Xây dựng hàng thông báo
    private HBox buildNotifRow(NotificationService.NotifItem item, int index) {
        HBox row = new HBox(12);
        row.getStyleClass().add("notif-item");
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12, 18, 12, 18));

        Region dot = new Region();
        dot.getStyleClass().add(item.dotStyle());

        Label icon = new Label(item.icon());
        icon.getStyleClass().add("notif-item-icon");

        VBox text = new VBox(3);
        HBox.setHgrow(text, Priority.ALWAYS);
        Label titleLbl = new Label(item.title());
        titleLbl.getStyleClass().add("notif-item-title");
        titleLbl.setWrapText(true);
        titleLbl.setMaxWidth(220);
        Label subLbl = new Label(item.subtitle());
        subLbl.getStyleClass().add("notif-item-sub");
        subLbl.setWrapText(true);
        subLbl.setMaxWidth(220);
        text.getChildren().addAll(titleLbl, subLbl);

        row.getChildren().addAll(dot, icon, text);

        row.setOpacity(0);
        FadeTransition ft = new FadeTransition(Duration.millis(150), row);
        ft.setToValue(1.0);
        ft.setDelay(Duration.millis(index * 55L));
        ft.play();

        return row;
    }

    // ── Tải trang với hiệu ứng fade ────────────────────────────────
    private void openPage(String pageKey, String fxmlPath, Button btn) {
        currentPageKey = pageKey;
        load(fxmlPath, titleFor(pageKey), btn);
    }

    private void load(String fxmlPath, String title, Button btn) {
        if (activeBtn != null)
            activeBtn.getStyleClass().remove("active");
        btn.getStyleClass().add("active");
        playActiveButtonMotion(btn);
        activeBtn = btn;
        lblPageTitle.setText(title);

        if (!contentArea.getChildren().isEmpty()) {
            Node current = contentArea.getChildren().get(0);
            FadeTransition fadeOut = new FadeTransition(Duration.millis(120), current);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setInterpolator(Interpolator.EASE_OUT);
            fadeOut.setOnFinished(e -> loadIntoPane(fxmlPath, title));
            fadeOut.play();
        } else {
            loadIntoPane(fxmlPath, title);
        }
    }

    private void loadIntoPane(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node page = loader.load();
            page.setOpacity(0);
            contentArea.getChildren().setAll(page);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(220), page);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.setInterpolator(Interpolator.EASE_OUT);
            fadeIn.play();

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError("Lỗi tải trang", "Không thể mở '" + title + "'.\n" + e.getMessage());
        }
    }

    // ── Hiệu ứng di động sidebar ──────────────────────────────────
    private void installSidebarMotion() {
        List<Button> buttons = List.of(btnDashboard, btnSinhVien, btnPhong, btnHopDong,
                btnThanhToan, btnViPham, btnTaiKhoan);
        for (Button btn : buttons) {
            if (btn == null)
                continue;
            btn.setOnMouseEntered(e -> {
                if (btn != activeBtn) {
                    TranslateTransition tt = new TranslateTransition(Duration.millis(150), btn);
                    tt.setToX(4);
                    tt.setInterpolator(Interpolator.EASE_OUT);
                    tt.play();
                }
            });
            btn.setOnMouseExited(e -> {
                if (btn != activeBtn) {
                    TranslateTransition tt = new TranslateTransition(Duration.millis(170), btn);
                    tt.setToX(0);
                    tt.setInterpolator(Interpolator.EASE_OUT);
                    tt.play();
                }
            });
        }
    }

    private void playActiveButtonMotion(Button btn) {
        if (btn == null)
            return;
        TranslateTransition tt = new TranslateTransition(Duration.millis(180), btn);
        tt.setFromX(Math.max(0, btn.getTranslateX()));
        tt.setToX(6);
        tt.setAutoReverse(true);
        tt.setCycleCount(2);
        tt.setInterpolator(Interpolator.EASE_OUT);
        tt.setOnFinished(e -> btn.setTranslateX(0));
        tt.play();
    }



    // ── Lấy chữ viết tắt tên ─────────────────────────────────
    private static String initials(String fullName) {
        if (fullName == null || fullName.isBlank())
            return "?";
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 1)
            return parts[0].substring(0, 1).toUpperCase();
        String last = parts[parts.length - 1];
        String first = parts[0];
        return (first.substring(0, 1) + last.substring(0, 1)).toUpperCase();
    }

    // ── Áp dụng theme (dự phòng) ───────────────────────────
    private void applyTheme() {
        // Dự phòng: áp dụng cài đặt giao diện
    }

    // ── Cập nhật giao diện theo ngôn ngữ (dự phòng) ──────────────────
    private void updateLocalizedChrome() {
        // Dự phòng: cập nhật nhãn theo ngôn ngữ
    }

    // ── Đóng popup ───────────────────────────────────────────────
    private void closePopup(Popup popup) {
        if (popup != null && popup.isShowing()) {
            popup.hide();
        }
    }

    // ── Hiệu ứng fade-in khi mở popup ───────────────────────────────
    private void animatePopupIn(VBox root) {
        root.setOpacity(0);
        FadeTransition ft = new FadeTransition(Duration.millis(180), root);
        ft.setToValue(1);
        ft.play();
    }

    // ── Lấy tiêu đề trang theo key ────────────────────────────────────
    private String titleFor(String pageKey) {
        return switch (pageKey) {
            case "dashboard" -> "Tổng Quan";
            case "sinhvien" -> "Quản Lý Sinh Viên";
            case "phong" -> "Quản Lý Phòng";
            case "hopdong" -> "Quản Lý Hợp Đồng";
            case "thanhtoan" -> "Quản Lý Thanh Toán";
            case "vipham" -> "Quản Lý Vi Phạm";
            case "taikhoan" -> "Quản Lý Tài Khoản";
            default -> "KTX Manager";
        };
    }

    // ── Menu dropdown tài khoản ─────────────────────────────────────
    @FXML
    private void handleShowAccountMenu(MouseEvent event) {
        if (activeAccountPopup != null && activeAccountPopup.isShowing()) {
            closePopup(activeAccountPopup);
            return;
        }
        closePopup(activeNotifPopup);

        Popup popup = new Popup();
        popup.setAutoHide(true);
        popup.setAutoFix(true);
        activeAccountPopup = popup;

        VBox dropdown = buildAccountDropdown();
        dropdown.getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/view/css/style.css"))
                        .toExternalForm());

        popup.getContent().add(dropdown);

        // Đặt vị trí popup
        javafx.geometry.Bounds b = topbarAccountRow.localToScreen(topbarAccountRow.getBoundsInLocal());
        popup.show(topbarAccountRow.getScene().getWindow(), b.getMaxX() - 220, b.getMaxY() + 8);
        animatePopupIn(dropdown);
    }

    private VBox buildAccountDropdown() {
        VBox root = new VBox(0);
        root.getStyleClass().add("account-dropdown");
        root.setPadding(new Insets(8, 0, 8, 0));

        // Mục menu
        String[][] items = {
                { "👤", "Hồ sơ", "#handleViewProfile" },
                { "⚙️", "Cài đặt", "#handleViewSettings" },
                { "🌙", "Chế độ tối", "#handleToggleDarkMode" },
                { "🌐", "Ngôn ngữ", "#handleChangeLanguage" },
        };

        for (String[] item : items) {
            root.getChildren().add(buildAccountMenuItem(item[0], item[1], item[2]));
        }

        // Đường phân cách
        Separator sep = new Separator();
        sep.getStyleClass().add("account-dropdown-separator");
        root.getChildren().add(sep);

        // Mục đăng xuất
        root.getChildren().add(buildAccountMenuItem("🚪", "Đăng xuất", "#handleLogout", true));

        return root;
    }

    private HBox buildAccountMenuItem(String icon, String label, String action) {
        return buildAccountMenuItem(icon, label, action, false);
    }

    private HBox buildAccountMenuItem(String icon, String label, String action, boolean isLogout) {
        HBox item = new HBox(10);
        item.getStyleClass().add(isLogout ? "account-menu-item-logout" : "account-menu-item");
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(10, 14, 10, 14));

        Label iconLabel = new Label(icon);
        iconLabel.getStyleClass().add("account-menu-icon");

        Label textLabel = new Label(label);
        textLabel.getStyleClass().add(isLogout ? "account-menu-text-logout" : "account-menu-text");

        item.getChildren().addAll(iconLabel, textLabel);

        // Xử lý sự kiện
        item.setOnMouseClicked(e -> {
            if (activeAccountPopup != null)
                closePopup(activeAccountPopup);

            switch (action) {
                case "#handleLogout" -> handleLogout();
                // Các mục khác chưa được xử lý
                default -> {
                }
            }
        });

        return item;
    }
}
