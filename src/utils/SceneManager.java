package utils;

import javafx.animation.FadeTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;

/** Quản lý chuyển cảnh giao diện. */
public class SceneManager {

    private static Stage primaryStage;

    private SceneManager() {
    }

    public static void init(Stage stage) {
        primaryStage = stage;
        primaryStage.setTitle("KTX Manager — Quản lý Ký túc xá");
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);
    }

    // Các phương thức hiển thị màn hình

    /** Hiện màn hình đăng nhập */
    public static void showLogin() throws Exception {
        FXMLLoader loader = new FXMLLoader(
                SceneManager.class.getResource("/view/fxml/login.fxml"));
        Scene newScene = new Scene(loader.load());
        transitionTo(newScene, () -> {
            primaryStage.setResizable(false);
            primaryStage.centerOnScreen();
        });
    }

    /** Hiện màn hình đăng ký */
    public static void showRegister() throws Exception {
        FXMLLoader loader = new FXMLLoader(
                SceneManager.class.getResource("/view/fxml/register.fxml"));
        Scene newScene = new Scene(loader.load());
        transitionTo(newScene, () -> {
            primaryStage.setResizable(false);
            primaryStage.centerOnScreen();
        });
    }

    /** Hiện màn hình chính */
    public static void showMain() throws Exception {
        FXMLLoader loader = new FXMLLoader(
                SceneManager.class.getResource("/view/fxml/main.fxml"));
        Scene newScene = new Scene(loader.load());
        transitionTo(newScene, () -> {
            primaryStage.setResizable(true);
            primaryStage.setMaximized(true);
        });
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    // Bộ xử lý chuyển cảnh

    /** Chuyển cảnh mượt mà. */
    private static void transitionTo(Scene newScene, Runnable afterSwap) {
        Scene currentScene = primaryStage.getScene();

        if (currentScene != null && currentScene.getRoot() != null) {
            // Mờ dần cảnh cũ
            Node oldRoot = currentScene.getRoot();
            FadeTransition fadeOut = new FadeTransition(Duration.millis(160), oldRoot);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(e -> {
                // Đổi cảnh và cấu hình stage
                primaryStage.setScene(newScene);
                if (afterSwap != null)
                    afterSwap.run();
                primaryStage.show();
                // Hiện cảnh mới
                Node newRoot = newScene.getRoot();
                newRoot.setOpacity(0);
                FadeTransition fadeIn = new FadeTransition(Duration.millis(220), newRoot);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            });
            fadeOut.play();
        } else {
            // Tải cảnh lần đầu
            primaryStage.setScene(newScene);
            if (afterSwap != null)
                afterSwap.run();
            primaryStage.show();
            Node newRoot = newScene.getRoot();
            newRoot.setOpacity(0);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(280), newRoot);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        }
    }
}
