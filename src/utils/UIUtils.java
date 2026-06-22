package utils;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;

/** Tiện ích giao diện người dùng. */
public class UIUtils {

    private UIUtils() {}

    /** Tạo thông báo dữ liệu trống. */
    public static VBox buildEmptyState(String icon, String title, String subtitle) {
        Label lblIcon = new Label(icon);
        lblIcon.getStyleClass().add("empty-state-icon");

        Label lblTitle = new Label(title);
        lblTitle.getStyleClass().add("empty-state-title");
        lblTitle.setWrapText(true);
        lblTitle.setAlignment(javafx.geometry.Pos.CENTER);

        VBox box = new VBox(8, lblIcon, lblTitle);

        if (subtitle != null && !subtitle.isBlank()) {
            Label lblSub = new Label(subtitle);
            lblSub.getStyleClass().add("empty-state-sub");
            lblSub.setWrapText(true);
            lblSub.setAlignment(javafx.geometry.Pos.CENTER);
            box.getChildren().add(lblSub);
        }

        box.getStyleClass().add("empty-state");
        box.setAlignment(Pos.CENTER);
        return box;
    }

    /** Thiết lập thông báo trống cho bảng. */
    public static <T> void setEmptyPlaceholder(TableView<T> table,
                                                String icon, String title, String subtitle) {
        table.setPlaceholder(buildEmptyState(icon, title, subtitle));
    }

    /** Tạo dòng chi tiết. */
    public static javafx.scene.layout.HBox buildDetailRow(String label, String value) {
        Label lblKey = new Label(label);
        lblKey.getStyleClass().add("detail-field-label");

        Label lblVal = new Label(value != null && !value.isBlank() ? value : "—");
        lblVal.getStyleClass().add("detail-field-value");
        lblVal.setWrapText(true);

        javafx.scene.layout.HBox row = new javafx.scene.layout.HBox(8, lblKey, lblVal);
        row.getStyleClass().add("detail-field-row");
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    /** Tạo thông báo chưa chọn chi tiết. */
    public static VBox buildDetailPanelEmpty(String icon, String message) {
        Label lblIcon  = new Label(icon);
        lblIcon.setStyle("-fx-font-size: 32px; -fx-opacity: 0.35;");

        Label lblMsg = new Label(message);
        lblMsg.setStyle("-fx-font-size: 12px; -fx-text-fill: #9CA3AF; -fx-text-alignment: center;");
        lblMsg.setWrapText(true);
        lblMsg.setAlignment(Pos.CENTER);

        VBox box = new VBox(10, lblIcon, lblMsg);
        box.setAlignment(Pos.CENTER);
        box.getStyleClass().add("detail-empty");
        return box;
    }
}
