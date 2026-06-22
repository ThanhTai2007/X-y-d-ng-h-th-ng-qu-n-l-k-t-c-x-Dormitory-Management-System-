package controller;

import dao.DashboardDAO;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.util.Duration;
import utils.FormatUtils;
import utils.UIUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DashboardController {

    // ── Ràng buộc FXML ───────────────────────────────────────────────
    @FXML private Label    lblTongSinhVien;
    @FXML private Label    lblPhongTrong;
    @FXML private Label    lblTongPhong;
    @FXML private Label    lblDoanhThuThang;
    @FXML private Label    lblDoanhThuNam;
    @FXML private Label    lblHoaDonTreHan;
    @FXML private Label    lblViPhamChuaXuLy;
    @FXML private Label    lblHopDongActive;
    @FXML private BarChart<String, Number> chartDoanhThu;
    @FXML private LineChart<String, Number> chartRevenueTrend;
    @FXML private PieChart chartPhong;
    @FXML private VBox  paneHopDongSapHetHan;
    @FXML private VBox  paneHoaDonQuaHan;
    @FXML private Label lblBadgeHopDong;
    @FXML private Label lblBadgeHoaDon;
    @FXML private Region barOccupancy;
    @FXML private Label  lblOccupancyPct;
    @FXML private Region barContract;
    @FXML private Label  lblContractRate;
    @FXML private Label  lblOverdueInsight;
    @FXML private Label  lblChartYearBadge;
    @FXML private Label  lblChartOccupancyBadge;
    @FXML private VBox   emptyRevenueChart;
    @FXML private VBox   emptyRevenueTrendChart;
    @FXML private VBox   emptyRoomChart;

    // ── Thẻ hành động nhanh ───────────────────────────────────────
    @FXML private VBox qaCardSV;
    @FXML private VBox qaCardPhong;
    @FXML private VBox qaCardHD;
    @FXML private VBox qaCardTT;
    @FXML private VBox qaCardVP;

    // ── Dòng thời gian hoạt động ─────────────────────────────────────
    @FXML private VBox paneTimeline;

    private final DashboardDAO dao = new DashboardDAO();

    // Dữ liệu cache
    private List<Map<String, Object>> cachedExpiring = new ArrayList<>();
    private List<Map<String, Object>> cachedOverdue  = new ArrayList<>();

    @FXML
    public void initialize() {
        loadStats();
        loadCharts();
        loadAlertPanels();       // nạp dữ liệu cảnh báo
        loadTimeline();          // nạp dòng thời gian
        animateQuickActions();   // hiệu ứng xuất hiện thẻ
        installPremiumHoverMotion();
    }

    // ── Tải thống kê tổng quan ─────────────────────────────────────
    private void loadStats() {
        try {
            Map<String, Object> stats = dao.getStats();

            int tongSV     = ((Number) stats.getOrDefault("tongSinhVien",        0)).intValue();
            int phongTrong = ((Number) stats.getOrDefault("soPhongTrong",        0)).intValue();
            int tongPhong  = ((Number) stats.getOrDefault("tongSoPhong",         0)).intValue();
            int hopDong    = ((Number) stats.getOrDefault("hopDongDangHoatDong", 0)).intValue();
            int treHan     = ((Number) stats.getOrDefault("hoaDonTreHan",        0)).intValue();
            int viPham     = ((Number) stats.getOrDefault("viPhamChuaXuLy",      0)).intValue();
            double thang   = ((Number) stats.getOrDefault("doanhThuThang",       0L)).doubleValue();
            double nam     = ((Number) stats.getOrDefault("doanhThuNam",         0L)).doubleValue();

            // Nhãn văn bản
            lblTongPhong.setText("/ " + tongPhong + " phòng");
            lblViPhamChuaXuLy.setText(viPham + " vi phạm chưa xử lý");
            lblDoanhThuNam.setText("Năm: " + FormatUtils.formatCurrency(nam));
            lblDoanhThuThang.setText(FormatUtils.formatCurrency(thang));

            // Đếm số có hiệu ứng
            animateCount(lblTongSinhVien, tongSV, "");
            animateCount(lblPhongTrong,   phongTrong, "");
            animateCount(lblHopDongActive, hopDong, "");
            animateCount(lblHoaDonTreHan,  treHan, "");

            // Tổng doanh thu
            if (lblChartYearBadge != null)
                lblChartYearBadge.setText("Cả năm: " + FormatUtils.formatCurrency(nam));

            // Thanh tiến trình lấp đầy phòng
            int occupied     = tongPhong - phongTrong;
            int occupancyPct = tongPhong > 0
                    ? (int) Math.round(occupied * 100.0 / tongPhong) : 0;

            if (lblOccupancyPct != null) lblOccupancyPct.setText(occupancyPct + "% lấp đầy");
            if (lblChartOccupancyBadge != null) lblChartOccupancyBadge.setText(occupancyPct + "% đầy");
            if (barOccupancy != null) {
                Platform.runLater(() -> {
                    if (barOccupancy.getParent() instanceof StackPane sp)
                        barOccupancy.prefWidthProperty().bind(
                                sp.widthProperty().multiply(occupancyPct / 100.0));
                });
            }

            // Thanh tiến trình tỷ lệ hợp đồng
            int contractPct = tongSV > 0
                    ? (int) Math.round(hopDong * 100.0 / tongSV) : 0;

            if (lblContractRate != null) lblContractRate.setText(contractPct + "% có HĐ");
            if (barContract != null) {
                Platform.runLater(() -> {
                    if (barContract.getParent() instanceof StackPane sp)
                        barContract.prefWidthProperty().bind(
                                sp.widthProperty().multiply(Math.min(contractPct, 100) / 100.0));
                });
            }

            // Thẻ cảnh báo quá hạn
            if (lblOverdueInsight != null) {
                lblOverdueInsight.getStyleClass().removeAll("insight-up", "insight-warn");
                if (treHan == 0) {
                    lblOverdueInsight.setText("✓ Không có quá hạn");
                    lblOverdueInsight.getStyleClass().add("insight-up");
                } else {
                    lblOverdueInsight.setText("⚠ " + treHan + " hóa đơn quá hạn");
                    lblOverdueInsight.getStyleClass().add("insight-warn");
                }
                lblOverdueInsight.setVisible(true);
                lblOverdueInsight.setManaged(true);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ── Biểu đồ ─────────────────────────────────────────────────────
    private void loadCharts() {
        // Biểu đồ cột
        try {
            Map<String, Long> revenue = dao.getDoanhThu12Thang();
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Doanh thu (VNĐ)");
            revenue.forEach((k, v) -> series.getData().add(new XYChart.Data<>(k, v)));
            var chartData = FXCollections.<XYChart.Series<String, Number>>observableArrayList();
            chartData.add(series);
            chartDoanhThu.setData(chartData);
            chartDoanhThu.setLegendVisible(false);
            boolean hasRevenue = revenue.values().stream().anyMatch(v -> v != null && v > 0);
            setChartEmpty(emptyRevenueChart, chartDoanhThu, !hasRevenue);

            XYChart.Series<String, Number> trendSeries = new XYChart.Series<>();
            trendSeries.setName("Xu hướng doanh thu");
            revenue.forEach((k, v) -> trendSeries.getData().add(new XYChart.Data<>(k, v)));
            var trendData = FXCollections.<XYChart.Series<String, Number>>observableArrayList();
            trendData.add(trendSeries);
            chartRevenueTrend.setData(trendData);
            chartRevenueTrend.setLegendVisible(false);
            setChartEmpty(emptyRevenueTrendChart, chartRevenueTrend, !hasRevenue);
        } catch (Exception e) { e.printStackTrace(); }

        // Biểu đồ tròn
        try {
            Map<String, Object> stats = dao.getStats();
            int trong  = ((Number) stats.getOrDefault("soPhongTrong",  0)).intValue();
            int day    = ((Number) stats.getOrDefault("soPhongDay",    0)).intValue();
            int baoTri = ((Number) stats.getOrDefault("soPhongBaoTri", 0)).intValue();

            boolean hasRooms = (trong + day + baoTri) > 0;
            chartPhong.setData(hasRooms
                    ? FXCollections.observableArrayList(
                        new PieChart.Data("Còn trống ("  + trong  + ")", trong),
                        new PieChart.Data("Đã đầy ("     + day    + ")", day),
                        new PieChart.Data("Bảo trì ("    + baoTri + ")", baoTri))
                    : FXCollections.observableArrayList());
            chartPhong.setLegendVisible(hasRooms);
            setChartEmpty(emptyRoomChart, chartPhong, !hasRooms);
        } catch (Exception e) { e.printStackTrace(); }

        Platform.runLater(this::installChartInteractions);
    }

    private void setChartEmpty(VBox emptyState, Chart chart, boolean empty) {
        if (emptyState != null) {
            emptyState.setVisible(empty);
            emptyState.setManaged(empty);
        }
        if (chart != null) {
            chart.setOpacity(empty ? 0.18 : 1.0);
            chart.setMouseTransparent(empty);
        }
    }

    private void installChartInteractions() {
        if (chartDoanhThu != null) {
            for (XYChart.Series<String, Number> series : chartDoanhThu.getData()) {
                for (XYChart.Data<String, Number> data : series.getData()) {
                    Node node = data.getNode();
                    if (node == null) continue;
                    Tooltip tip = new Tooltip(data.getXValue() + "\n" +
                            FormatUtils.formatCurrency(data.getYValue().doubleValue()));
                    tip.getStyleClass().add("premium-chart-tooltip");
                    Tooltip.install(node, tip);
                    installScaleHover(node, 1.08, -4);
                }
            }
        }

        if (chartPhong != null) {
            for (PieChart.Data data : chartPhong.getData()) {
                Node node = data.getNode();
                if (node == null) continue;
                Tooltip tip = new Tooltip(data.getName());
                tip.getStyleClass().add("premium-chart-tooltip");
                Tooltip.install(node, tip);
                installScaleHover(node, 1.06, 0);
            }
        }

        if (chartRevenueTrend != null) {
            for (XYChart.Series<String, Number> series : chartRevenueTrend.getData()) {
                for (XYChart.Data<String, Number> data : series.getData()) {
                    Node node = data.getNode();
                    if (node == null) continue;
                    Tooltip tip = new Tooltip(data.getXValue() + "\n" +
                            FormatUtils.formatCurrency(data.getYValue().doubleValue()));
                    tip.getStyleClass().add("premium-chart-tooltip");
                    Tooltip.install(node, tip);
                    installScaleHover(node, 1.16, -2);
                }
            }
        }
    }

    private void installScaleHover(Node node, double scale, double translateY) {
        node.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(150), node);
            st.setToX(scale);
            st.setToY(scale);
            TranslateTransition tt = new TranslateTransition(Duration.millis(150), node);
            tt.setToY(translateY);
            new ParallelTransition(st, tt).play();
        });
        node.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(170), node);
            st.setToX(1.0);
            st.setToY(1.0);
            TranslateTransition tt = new TranslateTransition(Duration.millis(170), node);
            tt.setToY(0);
            new ParallelTransition(st, tt).play();
        });
    }

    // ── Bảng cảnh báo ─────────────────────────────────────────────────
    private void loadAlertPanels() {
        loadExpiringContracts();
        loadOverdueBills();
    }

    private void loadExpiringContracts() {
        paneHopDongSapHetHan.getChildren().clear();
        try {
            cachedExpiring = dao.getHopDongSapHetHan(30);
            lblBadgeHopDong.setText(String.valueOf(cachedExpiring.size()));

            if (cachedExpiring.isEmpty()) {
                paneHopDongSapHetHan.getChildren().add(
                    UIUtils.buildEmptyState("✅", "Không có hợp đồng sắp hết hạn",
                                            "Tất cả hợp đồng vẫn còn hiệu lực dài hạn"));
                return;
            }
            for (Map<String, Object> row : cachedExpiring)
                paneHopDongSapHetHan.getChildren().add(buildExpiringRow(row));

        } catch (Exception e) {
            lblBadgeHopDong.setText("!");
            paneHopDongSapHetHan.getChildren().add(
                UIUtils.buildEmptyState("⚠", "Không thể tải dữ liệu", e.getMessage()));
        }
    }

    private void loadOverdueBills() {
        paneHoaDonQuaHan.getChildren().clear();
        try {
            cachedOverdue = dao.getHoaDonQuaHan();
            lblBadgeHoaDon.setText(String.valueOf(cachedOverdue.size()));

            if (cachedOverdue.isEmpty()) {
                paneHoaDonQuaHan.getChildren().add(
                    UIUtils.buildEmptyState("✅", "Không có hóa đơn quá hạn",
                                            "Tất cả khoản thu đã được thanh toán"));
                return;
            }
            for (Map<String, Object> row : cachedOverdue)
                paneHoaDonQuaHan.getChildren().add(buildOverdueRow(row));

        } catch (Exception e) {
            lblBadgeHoaDon.setText("!");
            paneHoaDonQuaHan.getChildren().add(
                UIUtils.buildEmptyState("⚠", "Không thể tải dữ liệu", e.getMessage()));
        }
    }

    // ── Xây dựng hàng cảnh báo ────────────────────────────────────
    private HBox buildExpiringRow(Map<String, Object> data) {
        int soNgay    = ((Number) data.getOrDefault("soNgayConLai", 0)).intValue();
        String hoTen  = String.valueOf(data.getOrDefault("hoTen",     "—"));
        String maPhong = String.valueOf(data.getOrDefault("maPhong",  "—"));
        String maHD    = String.valueOf(data.getOrDefault("maHopDong", ""));
        Object ktObj   = data.get("ngayKetThuc");
        String ngayKT  = (ktObj instanceof LocalDate d) ? FormatUtils.formatDate(d) : "—";

        String badgeStyle = soNgay <= 7 ? "badge-danger" : soNgay <= 15 ? "badge-warning" : "badge-info";
        Label badge = new Label(soNgay + " ngày");
        badge.getStyleClass().addAll("badge", badgeStyle);

        VBox info = new VBox(2);
        Label lblName = new Label(hoTen);
        lblName.getStyleClass().add("alert-item-title");
        Label lblSub = new Label(maHD + "  ·  Phòng " + maPhong + "  ·  HH: " + ngayKT);
        lblSub.getStyleClass().add("alert-item-sub");
        info.getChildren().addAll(lblName, lblSub);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox row = new HBox(10, info, spacer, badge);
        row.getStyleClass().add("alert-item-expiring");
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private HBox buildOverdueRow(Map<String, Object> data) {
        int soNgayTre = ((Number) data.getOrDefault("soNgayTre", 0)).intValue();
        long soTien   = ((Number) data.getOrDefault("soTien",    0)).longValue();
        String hoTen  = String.valueOf(data.getOrDefault("hoTen",  "—"));
        String maBill = String.valueOf(data.getOrDefault("maBill", "—"));
        Object hanObj = data.get("hanThanhToan");
        String hanTT  = (hanObj instanceof LocalDate d) ? FormatUtils.formatDate(d) : "—";

        Label badge = new Label("Trễ " + soNgayTre + " ngày");
        badge.getStyleClass().addAll("badge", "badge-danger");

        VBox info = new VBox(2);
        Label lblName = new Label(hoTen + "  ·  " + FormatUtils.formatCurrency(soTien));
        lblName.getStyleClass().add("alert-item-title");
        Label lblSub = new Label(maBill + "  ·  Hạn: " + hanTT);
        lblSub.getStyleClass().add("alert-item-sub");
        info.getChildren().addAll(lblName, lblSub);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox row = new HBox(10, info, spacer, badge);
        row.getStyleClass().add("alert-item-overdue");
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    // ── Dòng thời gian hoạt động ──────────────────────────────────────
    private void loadTimeline() {
        if (paneTimeline == null) return;
        paneTimeline.getChildren().clear();

        List<HBox> items = new ArrayList<>();
        int idx = 0;

        // Hóa đơn quá hạn
        for (int i = 0; i < Math.min(cachedOverdue.size(), 3); i++) {
            Map<String, Object> row = cachedOverdue.get(i);
            String hoTen = String.valueOf(row.getOrDefault("hoTen", "—"));
            long soTien  = ((Number) row.getOrDefault("soTien", 0)).longValue();
            int soNgayTre = ((Number) row.getOrDefault("soNgayTre", 0)).intValue();
            items.add(buildTimelineCard(
                "timeline-dot-red", "💸",
                "Hóa đơn quá hạn: " + hoTen,
                FormatUtils.formatCurrency(soTien) + "  ·  Trễ " + soNgayTre + " ngày",
                "Quá hạn", idx++
            ));
        }

        // Hợp đồng sắp hết hạn
        for (int i = 0; i < Math.min(cachedExpiring.size(), 3); i++) {
            Map<String, Object> row = cachedExpiring.get(i);
            String hoTen = String.valueOf(row.getOrDefault("hoTen", "—"));
            int soNgay   = ((Number) row.getOrDefault("soNgayConLai", 0)).intValue();
            String dot   = soNgay <= 7 ? "timeline-dot-red" : "timeline-dot-amber";
            String label = soNgay <= 7 ? "Khẩn cấp" : "Sắp hết hạn";
            items.add(buildTimelineCard(
                dot, "📅",
                "Hợp đồng sắp hết hạn: " + hoTen,
                "Còn " + soNgay + " ngày  ·  Cần gia hạn",
                label, idx++
            ));
        }

        if (items.isEmpty()) {
            paneTimeline.getChildren().add(
                UIUtils.buildEmptyState("✅", "Không có hoạt động nổi bật",
                                        "Hệ thống hoạt động bình thường"));
        } else {
            paneTimeline.getChildren().addAll(items);
        }
    }

    private HBox buildTimelineCard(String dotStyle, String icon, String title,
                                    String sub, String timeLabel, int idx) {
        HBox card = new HBox(12);
        card.getStyleClass().add("timeline-card");
        card.setAlignment(Pos.CENTER_LEFT);

        Region dot = new Region();
        dot.getStyleClass().add(dotStyle);

        Label iconLbl = new Label(icon);
        iconLbl.setStyle("-fx-font-size: 18px;");

        VBox text = new VBox(3);
        HBox.setHgrow(text, Priority.ALWAYS);
        Label titleLbl = new Label(title);
        titleLbl.getStyleClass().add("timeline-title");
        Label subLbl = new Label(sub);
        subLbl.getStyleClass().add("timeline-sub");
        text.getChildren().addAll(titleLbl, subLbl);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label timeLbl = new Label(timeLabel);
        timeLbl.getStyleClass().add("timeline-time");

        card.getChildren().addAll(dot, iconLbl, text, spacer, timeLbl);

        // Hiệu ứng fade-in
        card.setOpacity(0);
        FadeTransition ft = new FadeTransition(Duration.millis(220), card);
        ft.setToValue(1.0);
        ft.setDelay(Duration.millis(idx * 85L));
        ft.play();

        return card;
    }

    // ── Xử lý hành động nhanh ────────────────────────────────────────
    @FXML private void handleQuickSinhVien(MouseEvent e)   { navigateTo("#btnSinhVien");  }
    @FXML private void handleQuickPhong(MouseEvent e)      { navigateTo("#btnPhong");     }
    @FXML private void handleQuickHopDong(MouseEvent e)    { navigateTo("#btnHopDong");   }
    @FXML private void handleQuickThanhToan(MouseEvent e)  { navigateTo("#btnThanhToan"); }
    @FXML private void handleQuickViPham(MouseEvent e)     { navigateTo("#btnViPham");    }

    // Kích hoạt nút sidebar
    private void navigateTo(String btnId) {
        try {
            if (lblTongSinhVien == null) return;
            Scene scene = lblTongSinhVien.getScene();
            if (scene == null) return;
            Button btn = (Button) scene.lookup(btnId);
            if (btn != null) btn.fire();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // Hiệu ứng xuất hiện thẻ
    private void animateQuickActions() {
        List<VBox> cards = new ArrayList<>();
        if (qaCardSV    != null) cards.add(qaCardSV);
        if (qaCardPhong != null) cards.add(qaCardPhong);
        if (qaCardHD    != null) cards.add(qaCardHD);
        if (qaCardTT    != null) cards.add(qaCardTT);
        if (qaCardVP    != null) cards.add(qaCardVP);

        for (int i = 0; i < cards.size(); i++) {
            VBox card = cards.get(i);
            card.setOpacity(0);
            card.setTranslateY(12);

            FadeTransition ft = new FadeTransition(Duration.millis(270), card);
            ft.setToValue(1.0);
            ft.setDelay(Duration.millis(i * 65L));

            TranslateTransition tt = new TranslateTransition(Duration.millis(270), card);
            tt.setToY(0);
            tt.setInterpolator(Interpolator.EASE_OUT);
            tt.setDelay(Duration.millis(i * 65L));

            new ParallelTransition(ft, tt).play();
        }
    }

    // ── Hiệu ứng hover và đếm số có hiệu ứng ──────────────────────────
    private void installPremiumHoverMotion() {
        List<VBox> cards = new ArrayList<>();
        if (qaCardSV    != null) cards.add(qaCardSV);
        if (qaCardPhong != null) cards.add(qaCardPhong);
        if (qaCardHD    != null) cards.add(qaCardHD);
        if (qaCardTT    != null) cards.add(qaCardTT);
        if (qaCardVP    != null) cards.add(qaCardVP);

        for (VBox card : cards) {
            card.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> animateLift(card, -6, 1.015));
            card.addEventHandler(MouseEvent.MOUSE_EXITED, e -> animateLift(card, 0, 1.0));
        }
    }

    private void animateLift(Node node, double y, double scale) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(180), node);
        tt.setToY(y);
        tt.setInterpolator(Interpolator.EASE_OUT);
        ScaleTransition st = new ScaleTransition(Duration.millis(180), node);
        st.setToX(scale);
        st.setToY(scale);
        st.setInterpolator(Interpolator.EASE_OUT);
        new ParallelTransition(tt, st).play();
    }

    private void animateCount(Label label, int target, String suffix) {
        if (label == null) return;
        if (target <= 0) { label.setText("0" + suffix); return; }

        int frames = 20;
        Duration totalDuration = Duration.millis(550);
        Timeline timeline = new Timeline();
        for (int i = 1; i <= frames; i++) {
            final int val = (int) Math.round(target * (i / (double) frames));
            timeline.getKeyFrames().add(
                new KeyFrame(totalDuration.multiply(i / (double) frames),
                    e -> label.setText(val + suffix)));
        }
        timeline.play();
    }
}
