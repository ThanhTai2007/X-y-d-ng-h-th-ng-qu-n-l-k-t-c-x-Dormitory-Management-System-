package utils;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.util.Duration;

import java.util.LinkedList;
import java.util.Queue;

/** Hệ thống thông báo trung tâm. */
public class NotificationManager {

    private static NotificationManager instance;
    private final Queue<NotificationPopup> notificationQueue = new LinkedList<>();
    private Node anchorNode; // Parent node for positioning
    private static final int MARGIN = 20;
    private static final int NOTIFICATION_WIDTH = 380;
    private static final int ANIMATION_DURATION = 300;

    // Loại thông báo
    public enum NotificationType {
        SUCCESS("success-notif", "#DCFCE7", "#166534"),  // Green
        ERROR("error-notif", "#FEE2E2", "#991B1B"),      // Red
        INFO("info-notif", "#DBEAFE", "#0D47A1");        // Blue

        public final String styleClass;
        public final String bgColor;
        public final String textColor;

        NotificationType(String styleClass, String bgColor, String textColor) {
            this.styleClass = styleClass;
            this.bgColor = bgColor;
            this.textColor = textColor;
        }
    }

    private NotificationManager() {}

    /** Lấy instance duy nhất. */
    public static NotificationManager getInstance() {
        if (instance == null) {
            instance = new NotificationManager();
        }
        return instance;
    }

    /** Khởi tạo NotificationManager. */
    public static void init(Node anchorNode) {
        getInstance().anchorNode = anchorNode;
    }

    /** Thông báo thành công. */
    public static void showSuccess(String title, String message) {
        getInstance().show(NotificationType.SUCCESS, title, message);
    }

    /** Thông báo thành công với thời gian. */
    public static void showSuccess(String title, String message, Duration duration) {
        getInstance().show(NotificationType.SUCCESS, title, message, duration);
    }

    /** Thông báo lỗi. */
    public static void showError(String title, String message) {
        getInstance().show(NotificationType.ERROR, title, message);
    }

    /** Thông báo lỗi với thời gian. */
    public static void showError(String title, String message, Duration duration) {
        getInstance().show(NotificationType.ERROR, title, message, duration);
    }

    /** Thông báo thông tin. */
    public static void showInfo(String title, String message) {
        getInstance().show(NotificationType.INFO, title, message);
    }

    /** Thông báo thông tin với thời gian. */
    public static void showInfo(String title, String message, Duration duration) {
        getInstance().show(NotificationType.INFO, title, message, duration);
    }

    /** Hiển thị thông báo. */
    private void show(NotificationType type, String title, String message) {
        show(type, title, message, Duration.seconds(4));
    }

    /** Hiển thị thông báo với thời gian. */
    private void show(NotificationType type, String title, String message, Duration duration) {
        if (anchorNode == null) {
            System.err.println("NotificationManager not initialized. Call NotificationManager.init(node) first.");
            return;
        }

        Platform.runLater(() -> {
            NotificationPopup popup = new NotificationPopup(type, title, message, duration);
            notificationQueue.add(popup);
            popup.setOnDismissed(() -> {
                notificationQueue.remove(popup);
                displayNextNotification();
            });
            displayNextNotification();
        });
    }

    /** Hiển thị thông báo tiếp theo. */
    private void displayNextNotification() {
        if (notificationQueue.isEmpty()) return;

        NotificationPopup notification = notificationQueue.peek();
        if (notification.isShowing()) return;

        // Tạo nội dung popup
        VBox content = createNotificationContent(notification.type, notification.title, notification.message, notification);

        // Tạo popup JavaFX
        Popup popup = new Popup();
        popup.setAutoHide(false);
        popup.getContent().add(content);
        popup.setOnHidden(e -> notification.onDismissedCallback.run());

        // Đặt vị trí trên cùng bên phải
        Bounds anchorBounds = anchorNode.localToScene(anchorNode.getBoundsInLocal());
        double x = anchorBounds.getCenterX() - NOTIFICATION_WIDTH / 2 + (anchorBounds.getWidth() / 2) - NOTIFICATION_WIDTH - MARGIN;
        double y = anchorBounds.getMinY() + MARGIN;

        popup.show(anchorNode.getScene().getWindow(), x, y);
        notification.setPopup(popup);
        notification.setShowing(true);

        // Hiệu ứng xuất hiện
        animateIn(content, popup, notification);
    }

    /** Tạo giao diện thông báo. */
    private VBox createNotificationContent(NotificationType type, String title, String message, NotificationPopup notification) {
        VBox root = new VBox(10);
        root.setStyle("-fx-padding: 16 16 16 16; -fx-spacing: 6;");
        root.getStyleClass().add(type.styleClass);

        // Tiêu đề và nút đóng
        HBox header = new HBox();
        header.setStyle("-fx-spacing: 8; -fx-alignment: center-left;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + type.textColor + ";");

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        Label closeBtn = new Label("✕");
        closeBtn.setStyle("-fx-font-size: 16px; -fx-text-fill: " + type.textColor + "; -fx-cursor: hand;");
        closeBtn.setOnMouseClicked(e -> {
            notification.dismiss();
        });

        header.getChildren().addAll(titleLabel, spacer, closeBtn);

        // Nội dung
        Label messageLabel = new Label(message);
        messageLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + type.textColor + "; -fx-wrap-text: true;");
        messageLabel.setWrapText(true);

        root.getChildren().addAll(header, messageLabel);
        root.setPrefWidth(NOTIFICATION_WIDTH);
        return root;
    }

    /** Hiệu ứng thông báo. */
    private void animateIn(VBox content, Popup popup, NotificationPopup notification) {
        content.setOpacity(0);
        content.setTranslateY(-20);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(ANIMATION_DURATION), content);
        fadeIn.setToValue(1);

        TranslateTransition slideIn = new TranslateTransition(Duration.millis(ANIMATION_DURATION), content);
        slideIn.setToY(0);

        ParallelTransition transition = new ParallelTransition(fadeIn, slideIn);

        transition.setOnFinished(e -> {
            // Tự động đóng
            PauseTransition pause = new PauseTransition(notification.duration);
            pause.setOnFinished(f -> notification.dismiss());
            pause.play();
        });

        transition.play();
    }

    /** Model thông báo. */
    static class NotificationPopup {
        NotificationType type;
        String title;
        String message;
        Duration duration;
        Popup popup;
        boolean showing = false;
        Runnable onDismissedCallback = () -> {};

        NotificationPopup(NotificationType type, String title, String message, Duration duration) {
            this.type = type;
            this.title = title;
            this.message = message;
            this.duration = duration;
        }

        void setPopup(Popup p) { this.popup = p; }
        void setShowing(boolean s) { this.showing = s; }
        boolean isShowing() { return showing; }

        void dismiss() {
            if (popup != null && popup.isShowing()) {
                // Hiệu ứng biến mất
                Node content = popup.getContent().get(0);
                
                FadeTransition fadeOut = new FadeTransition(Duration.millis(ANIMATION_DURATION), content);
                fadeOut.setToValue(0);

                TranslateTransition slideOut = new TranslateTransition(Duration.millis(ANIMATION_DURATION), content);
                slideOut.setToY(-20);

                ParallelTransition transition = new ParallelTransition(fadeOut, slideOut);
                
                transition.setOnFinished(e -> {
                    popup.hide();
                    showing = false;
                    onDismissedCallback.run();
                });
                transition.play();
            }
        }

        void setOnDismissed(Runnable callback) {
            this.onDismissedCallback = callback;
        }
    }
}
