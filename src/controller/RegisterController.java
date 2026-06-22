package controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import model.TaiKhoan;
import service.AuthService;
import utils.AnimationUtils;
import utils.SceneManager;

// Quản lý màn hình đăng ký
public class RegisterController {

    // ── Ràng buộc FXML ───────────────────────────────────────────
    @FXML private TextField     txtUsername;
    @FXML private TextField     txtHoTen;
    @FXML private PasswordField txtPassword;
    @FXML private PasswordField txtConfirmPassword;
    @FXML private Label         lblError;
    @FXML private Label         lblSuccess;
    @FXML private Button        btnRegister;

    // ── Binding animation ─────────────────────────────────────────
    @FXML private HBox loginCard;   // thẻ kính (glass card)
    @FXML private VBox formPanel;   // panel form bên phải

    private final AuthService authService = new AuthService();

    @FXML
    public void initialize() {
        lblError.setVisible(false);
        lblError.setManaged(false);
        lblSuccess.setVisible(false);
        lblSuccess.setManaged(false);

        // Hiệu ứng xuất hiện
        Platform.runLater(() -> {
            txtUsername.requestFocus();
            if (loginCard != null) {
                AnimationUtils.scaleIn(loginCard, 480);
            }
        });
    }

    // Xử lý đăng ký
    @FXML
    public void handleRegister() {
        hideMessages();

        String username = txtUsername.getText().trim();
        String hoTen    = txtHoTen.getText().trim();
        String password = txtPassword.getText();
        String confirm  = txtConfirmPassword.getText();

        // Kiểm tra mật khẩu xác nhận
        if (!password.equals(confirm)) {
            showError("Mật khẩu xác nhận không khớp.");
            // Lắc trường mật khẩu
            AnimationUtils.shakeError(txtConfirmPassword);
            txtConfirmPassword.clear();
            txtConfirmPassword.requestFocus();
            return;
        }

        btnRegister.setDisable(true);
        btnRegister.setText("Đang đăng ký...");

        // Chạy trên luồng nền
        new Thread(() -> {
            try {
                TaiKhoan newUser = authService.register(username, password, hoTen);

                Platform.runLater(() -> {
                    showSuccess("✅ Đăng ký thành công! Tài khoản: " + newUser.getUsername()
                            + " [" + newUser.getRoleName() + "]");
                    clearForm();
                    btnRegister.setDisable(false);
                    btnRegister.setText("📝  Đăng Ký Tài Khoản");
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    showError(e.getMessage());
                    // Lắc trường mật khẩu
                    AnimationUtils.shakeError(txtPassword);
                    btnRegister.setDisable(false);
                    btnRegister.setText("📝  Đăng Ký Tài Khoản");
                    txtPassword.clear();
                    txtConfirmPassword.clear();
                    txtPassword.requestFocus();
                });
            }
        }).start();
    }

    // Trở về đăng nhập
    @FXML
    public void handleBackToLogin() {
        try {
            SceneManager.showLogin();
        } catch (Exception e) {
            showError("Không thể quay lại màn hình đăng nhập: " + e.getMessage());
        }
    }

    // --- Helpers ---

    private void clearForm() {
        txtUsername.clear();
        txtHoTen.clear();
        txtPassword.clear();
        txtConfirmPassword.clear();
        txtUsername.requestFocus();
    }

    private void showError(String msg) {
        lblError.setText(msg);
        lblError.setVisible(true);
        lblError.setManaged(true);
        lblSuccess.setVisible(false);
        lblSuccess.setManaged(false);
        // Hiệu ứng lỗi
        AnimationUtils.fadeIn(lblError, 220);
    }

    private void showSuccess(String msg) {
        lblSuccess.setText(msg);
        lblSuccess.setVisible(true);
        lblSuccess.setManaged(true);
        lblError.setVisible(false);
        lblError.setManaged(false);
        // Hiệu ứng thành công
        AnimationUtils.pulse(lblSuccess);
    }

    private void hideMessages() {
        lblError.setVisible(false);
        lblError.setManaged(false);
        lblSuccess.setVisible(false);
        lblSuccess.setManaged(false);
    }
}
