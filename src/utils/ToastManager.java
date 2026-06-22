package utils;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

/** Hệ thống thông báo toast. */
public class ToastManager {

    public enum Type { SUCCESS, ERROR, WARNING, INFO }

    private static StackPane rootPane;
    // Số lượng toast đang hiển thị
    private static int activeCount = 0;

    /** Khởi tạo ToastManager. */
    public static void init(StackPane root) {
        rootPane = root;
    }

    public static void showSuccess(String message) { show("✅  " + message, Type.SUCCESS); }
    public static void showError  (String message) { show("❌  " + message, Type.ERROR);   }
    public static void showWarning(String message) { show("⚠️  " + message, Type.WARNING); }
    public static void showInfo   (String message) { show("ℹ️  " + message, Type.INFO);    }

    private static void show(String message, Type type) {
        if (rootPane == null) return; // safety guard
        Platform.runLater(() -> buildAndAnimate(message, type));
    }

    private static void buildAndAnimate(String message, Type type) {
        // Tạo giao diện toast
        Label lblIcon = new Label(iconFor(type));
        lblIcon.getStyleClass().add("toast-icon");

        Label lblMsg = new Label(message);
        lblMsg.getStyleClass().add("toast-message");
        lblMsg.setWrapText(true);
        lblMsg.setMaxWidth(240);

        HBox toast = new HBox(10, lblIcon, lblMsg);
        toast.getStyleClass().addAll("toast-base", styleFor(type));
        toast.setAlignment(Pos.CENTER_LEFT);
        toast.setPadding(new Insets(12, 18, 12, 16));
        toast.setMaxWidth(340);

        // Vị trí dưới cùng bên phải
        int slot = activeCount;
        activeCount++;
        double bottomOffset = 24 + slot * 66.0;
        StackPane.setAlignment(toast, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(toast, new Insets(0, 24, bottomOffset, 0));
        toast.setOpacity(0);
        toast.setTranslateX(60);  // start to the right of final position

        rootPane.getChildren().add(toast);

        // Nhấn để đóng
        toast.setOnMouseClicked(e -> dismiss(toast));

        // Hiệu ứng xuất hiện
        TranslateTransition slideIn = new TranslateTransition(Duration.millis(240), toast);
        slideIn.setToX(0);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(220), toast);
        fadeIn.setToValue(1.0);

        PauseTransition hold = new PauseTransition(Duration.seconds(3.5));

        // Hiệu ứng biến mất
        FadeTransition fadeOut = new FadeTransition(Duration.millis(260), toast);
        fadeOut.setToValue(0);

        TranslateTransition slideOut = new TranslateTransition(Duration.millis(220), toast);
        slideOut.setToX(60);

        SequentialTransition seq = new SequentialTransition(
                slideIn, hold, new javafx.animation.ParallelTransition(fadeOut, slideOut)
        );
        fadeIn.play();
        seq.play();

        seq.setOnFinished(e -> {
            rootPane.getChildren().remove(toast);
            activeCount = Math.max(0, activeCount - 1);
        });
    }

    private static void dismiss(HBox toast) {
        FadeTransition ft = new FadeTransition(Duration.millis(180), toast);
        ft.setToValue(0);
        ft.setOnFinished(e -> {
            rootPane.getChildren().remove(toast);
            activeCount = Math.max(0, activeCount - 1);
        });
        ft.play();
    }

    private static String iconFor(Type type) {
        return switch (type) {
            case SUCCESS -> "✅";
            case ERROR   -> "❌";
            case WARNING -> "⚠";
            case INFO    -> "ℹ";
        };
    }

    private static String styleFor(Type type) {
        return switch (type) {
            case SUCCESS -> "toast-success";
            case ERROR   -> "toast-error";
            case WARNING -> "toast-warning";
            case INFO    -> "toast-info";
        };
    }
}
