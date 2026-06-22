package utils;

import javafx.animation.*;
import javafx.scene.Node;
import javafx.util.Duration;

/**
 * Tiện ích hiệu ứng chuyển động.
 */
public class AnimationUtils {

    private AnimationUtils() {}

    // Hiệu ứng xuất hiện

    /** Mờ dần (opacity 0 → 1). */
    public static void fadeIn(Node node, double durationMs) {
        node.setOpacity(0);
        FadeTransition ft = new FadeTransition(Duration.millis(durationMs), node);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ft.setInterpolator(Interpolator.EASE_OUT);
        ft.play();
    }

    /** Mờ dần và trượt lên. */
    public static void fadeInUp(Node node, double durationMs, double offsetY) {
        node.setOpacity(0);
        node.setTranslateY(offsetY);

        FadeTransition ft = new FadeTransition(Duration.millis(durationMs), node);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ft.setInterpolator(Interpolator.EASE_OUT);

        TranslateTransition tt = new TranslateTransition(Duration.millis(durationMs), node);
        tt.setFromY(offsetY);
        tt.setToY(0);
        tt.setInterpolator(Interpolator.EASE_OUT);

        new ParallelTransition(ft, tt).play();
    }

    /** Phóng to và mờ dần. */
    public static void scaleIn(Node node, double durationMs) {
        node.setOpacity(0);
        node.setScaleX(0.90);
        node.setScaleY(0.90);

        FadeTransition ft = new FadeTransition(Duration.millis(durationMs), node);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ft.setInterpolator(Interpolator.EASE_OUT);

        ScaleTransition st = new ScaleTransition(Duration.millis(durationMs), node);
        st.setFromX(0.90);
        st.setFromY(0.90);
        st.setToX(1.0);
        st.setToY(1.0);
        st.setInterpolator(Interpolator.EASE_OUT);

        new ParallelTransition(ft, st).play();
    }

    // Hiệu ứng cảnh báo

    /** Rung ngang báo lỗi. */
    public static void shakeError(Node node) {
        double startX = node.getTranslateX();
        TranslateTransition tt = new TranslateTransition(Duration.millis(55), node);
        tt.setFromX(startX);
        tt.setByX(9);
        tt.setCycleCount(6);
        tt.setAutoReverse(true);
        tt.setInterpolator(Interpolator.EASE_BOTH);
        tt.setOnFinished(e -> node.setTranslateX(startX));
        tt.play();
    }

    /** Phóng to thu nhỏ nhẹ. */
    public static void pulse(Node node) {
        ScaleTransition st = new ScaleTransition(Duration.millis(140), node);
        st.setFromX(1.0);
        st.setFromY(1.0);
        st.setToX(1.04);
        st.setToY(1.04);
        st.setCycleCount(2);
        st.setAutoReverse(true);
        st.setInterpolator(Interpolator.EASE_BOTH);
        st.play();
    }

    // Chuyển cảnh

    /** Làm mờ dần về 0. */
    public static void fadeOut(Node node, double durationMs, Runnable onFinished) {
        FadeTransition ft = new FadeTransition(Duration.millis(durationMs), node);
        ft.setToValue(0.0);
        ft.setInterpolator(Interpolator.EASE_IN);
        if (onFinished != null) ft.setOnFinished(e -> onFinished.run());
        ft.play();
    }
}
