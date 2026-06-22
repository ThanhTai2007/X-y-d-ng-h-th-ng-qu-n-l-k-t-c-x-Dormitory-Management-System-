package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// Kết nối SQL Server
public class DatabaseConnection {

    private static final String SERVER   = "localhost";
    private static final String PORT     = "1433";
    private static final String DB_NAME  = "DormitoryDB";
    private static final String USERNAME = "sa";
    private static final String PASSWORD = "123456";

    private static final String URL =
            "jdbc:sqlserver://" + SERVER + ":" + PORT + ";"
            + "databaseName=" + DB_NAME + ";"
            + "encrypt=true;"
            + "trustServerCertificate=true;";

    private static Connection connection = null;

    private DatabaseConnection() {}

    // Lấy kết nối duy nhất
    public static synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
                System.out.println("[DB] Kết nối SQL Server thành công!");
            } catch (ClassNotFoundException e) {
                throw new SQLException("Không tìm thấy JDBC Driver.", e);
            }
        }
        return connection;
    }

    // Kiểm tra kết nối
    public static synchronized boolean testConnection() {
        try {
            Connection conn = getConnection();
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("[DB] Kết nối thất bại: " + e.getMessage());
            return false;
        }
    }

    // Đóng kết nối
    public static synchronized void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("[DB] Đã đóng kết nối.");
            } catch (SQLException e) {
                System.err.println("[DB] Lỗi đóng kết nối: " + e.getMessage());
            } finally {
                connection = null;
            }
        }
    }
}
