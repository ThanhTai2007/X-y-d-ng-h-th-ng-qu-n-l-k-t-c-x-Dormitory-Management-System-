
import javafx.application.Application;
import javafx.stage.Stage;
import utils.DatabaseConnection;
import utils.SceneManager;

// Điểm khởi động
public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        SceneManager.init(primaryStage);

        // Cập nhật hợp đồng hết hạn
        try {
            new dao.HopDongDAO().capNhatHopDongHetHan();
        } catch (Exception ignored) {
        }

        SceneManager.showLogin();
    }

    @Override
    public void stop() {
        // Đóng kết nối DB
        DatabaseConnection.closeConnection();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
