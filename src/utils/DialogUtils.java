package utils;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.Node;

/**
 * Tiện ích tạo hộp thoại giao diện đẹp.
 */
public class DialogUtils {

    private DialogUtils() {}

    // Đường dẫn CSS
    private static final String CSS = "/view/css/style.css";

    /** Tạo hộp thoại. */
    public static <T> Dialog<T> buildStyledDialog(String title, String subtitle, String icon) {
        Dialog<T> dialog = new Dialog<>();
        dialog.getDialogPane().getStylesheets().add(
                DialogUtils.class.getResource(CSS).toExternalForm());
        dialog.getDialogPane().getStyleClass().add("root");
        dialog.getDialogPane().setStyle("-fx-padding: 0;");
        dialog.setTitle(title);

        // Header bar
        VBox header = new VBox(4);
        header.getStyleClass().add("dialog-header-bar");

        HBox titleRow = new HBox(10);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        if (icon != null && !icon.isBlank()) {
            Label lblIcon = new Label(icon);
            lblIcon.setStyle("-fx-font-size: 18px; -fx-text-fill: white;");
            titleRow.getChildren().add(lblIcon);
        }
        Label lblTitle = new Label(title);
        lblTitle.getStyleClass().add("dialog-header-title");
        titleRow.getChildren().add(lblTitle);
        header.getChildren().add(titleRow);

        if (subtitle != null && !subtitle.isBlank()) {
            Label lblSub = new Label(subtitle);
            lblSub.getStyleClass().add("dialog-header-subtitle");
            header.getChildren().add(lblSub);
        }

        dialog.getDialogPane().setHeader(header);
        return dialog;
    }

    /** Tạo dòng nhập liệu. */
    public static HBox buildFormRow(String label, Node control) {
        Label lbl = new Label(label);
        lbl.getStyleClass().add("form-label");
        lbl.setPrefWidth(130);
        lbl.setMinWidth(130);

        HBox row = new HBox(12, lbl, control);
        row.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(control, Priority.ALWAYS);
        return row;
    }

    /** Tạo phần thân hộp thoại. */
    public static VBox buildDialogBody(Node... children) {
        VBox body = new VBox(12);
        body.getStyleClass().add("dialog-body");
        body.getChildren().addAll(children);
        return body;
    }

    /** Tạo nhãn. */
    public static Label buildSectionLabel(String text) {
        Label lbl = new Label(text.toUpperCase());
        lbl.getStyleClass().add("dialog-section-label");
        return lbl;
    }

    /** Tạo TextField. */
    public static TextField styledField(String value, String prompt) {
        TextField tf = new TextField(value != null ? value : "");
        tf.setPromptText(prompt);
        tf.getStyleClass().add("field-input");
        tf.setPrefWidth(260);
        return tf;
    }

    /** Tạo ComboBox. */
    public static <T> ComboBox<T> styledCombo(double width) {
        ComboBox<T> cb = new ComboBox<>();
        cb.getStyleClass().add("combo-box");
        cb.setPrefWidth(width);
        return cb;
    }

    /** Tạo banner thông báo. */
    public static Label buildInfoBanner(String message, String type) {
        Label lbl = new Label(message);
        lbl.setWrapText(true);
        lbl.setPadding(new Insets(10, 14, 10, 14));
        String style = switch (type) {
            case "warning" -> "-fx-background-color: #FEF3C7; -fx-text-fill: #92400E; -fx-background-radius: 8;";
            case "danger"  -> "-fx-background-color: #FEE2E2; -fx-text-fill: #991B1B; -fx-background-radius: 8;";
            default        -> "-fx-background-color: #EFF6FF; -fx-text-fill: #1E40AF; -fx-background-radius: 8;";
        };
        lbl.setStyle("-fx-font-size: 12px; " + style);
        return lbl;
    }

    /** Hiển thị hộp thoại xác nhận. */
    public static boolean showConfirmDialog(String title, String message,
                                            String confirmText, boolean isDanger) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.getDialogPane().getStylesheets().add(
                DialogUtils.class.getResource(CSS).toExternalForm());
        dialog.getDialogPane().getStyleClass().add("root");
        dialog.getDialogPane().setStyle("-fx-padding: 0;");
        dialog.setTitle(title);

        String icon = isDanger ? "⚠️" : "❓";

        VBox body = new VBox(14);
        body.getStyleClass().add("confirm-dialog-body");
        body.setAlignment(Pos.CENTER);
        body.setPrefWidth(380);

        Label lblIcon = new Label(icon);
        lblIcon.getStyleClass().add("confirm-dialog-icon");

        Label lblTitle = new Label(title);
        lblTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #111827;");
        lblTitle.setAlignment(Pos.CENTER);

        Label lblMsg = new Label(message);
        lblMsg.getStyleClass().add("confirm-dialog-message");
        lblMsg.setMaxWidth(320);

        body.getChildren().addAll(lblIcon, lblTitle, lblMsg);
        dialog.getDialogPane().setContent(body);
        dialog.getDialogPane().setHeader(new Region());

        ButtonType btnConfirm = new ButtonType(confirmText, ButtonBar.ButtonData.OK_DONE);
        ButtonType btnCancel  = new ButtonType("Huỷ",       ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(btnConfirm, btnCancel);

        Button okBtn = (Button) dialog.getDialogPane().lookupButton(btnConfirm);
        if (isDanger) {
            okBtn.setStyle("-fx-background-color: #DC2626; -fx-text-fill: white; " +
                           "-fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 8 20;");
        } else {
            okBtn.setStyle("-fx-background-color: #2563EB; -fx-text-fill: white; " +
                           "-fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 8 20;");
        }

        var result = dialog.showAndWait();
        return result.isPresent() && result.get() == btnConfirm;
    }

    /** Hộp thoại xác nhận thường. */
    public static boolean showConfirm(String title, String message) {
        return showConfirmDialog(title, message, "Xác nhận", false);
    }

    /** Hộp thoại xác nhận nguy hiểm. */
    public static boolean showDangerConfirm(String title, String message, String confirmText) {
        return showConfirmDialog(title, message, confirmText, true);
    }
}
