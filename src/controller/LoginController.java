package controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import model.TaiKhoan;
import service.AuthService;
import utils.AnimationUtils;
import utils.SceneManager;

public class LoginController {

    // ── Ràng buộc FXML ───────────────────────────────────────────
    @FXML private TextField     txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Label         lblError;
    @FXML private Button        btnLogin;
    @FXML private Button        btnRegister;

    // ── Binding animation ─────────────────────────────────────────
    @FXML private HBox loginCard;   // thẻ kính (glass card)
    @FXML private VBox formPanel;   // panel form bên phải
    @FXML private StackPane loginRoot;

    private final AuthService authService = new AuthService();

    @FXML
    public void initialize() {
        // Ẩn thông báo lỗi
        lblError.setVisible(false);
        lblError.setManaged(false);
        applyCampusBackgroundIfAvailable();

        // Hiệu ứng xuất hiện
        Platform.runLater(() -> {
            txtUsername.requestFocus();
            if (loginCard != null) {
                AnimationUtils.scaleIn(loginCard, 480);
            }
        });
    }

    @FXML
    public void handleLogin() {
        hideError();
        btnLogin.setDisable(true);
        btnLogin.setText("Đang đăng nhập...");

        // Chạy trên luồng nền
        new Thread(() -> {
            try {
                authService.login(
                        txtUsername.getText().trim(),
                        txtPassword.getText());

                Platform.runLater(() -> {
                    try {
                        SceneManager.showMain();
                    } catch (Exception e) {
                        showError("Không thể mở màn hình chính: " + e.getMessage());
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    showError(e.getMessage());
                    // Lắc trường mật khẩu
                    AnimationUtils.shakeError(txtPassword);
                    btnLogin.setDisable(false);
                    btnLogin.setText("🔐  Đăng Nhập");
                    txtPassword.clear();
                    txtPassword.requestFocus();
                });
            }
        }).start();
    }

    // Chuyển màn hình đăng ký
    @FXML
    public void handleRegister() {
        try {
            SceneManager.showRegister();
        } catch (Exception e) {
            showError("Không thể mở màn hình đăng ký: " + e.getMessage());
        }
    }

    private void showError(String msg) {
        lblError.setText(msg);
        lblError.setVisible(true);
        lblError.setManaged(true);
        // Hiệu ứng lỗi
        AnimationUtils.fadeIn(lblError, 220);
    }

    private void hideError() {
        lblError.setVisible(false);
        lblError.setManaged(false);
    }

    private void applyCampusBackgroundIfAvailable() {
        if (loginRoot == null) return;
        var bg = getClass().getResource("/view/assets/login-campus.jpg");
        if (bg == null) bg = getClass().getResource("/view/assets/login-campus.png");
        if (bg == null) return;

        loginRoot.getStyleClass().add("login-scene-root-image");
        loginRoot.setStyle("-fx-background-image: url('" + bg.toExternalForm() + "');" +
                "-fx-background-size: cover; -fx-background-position: center center;" +
                "-fx-background-repeat: no-repeat;");
    }
}
