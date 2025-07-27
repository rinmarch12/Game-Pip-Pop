package model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// Mengelola koneksi dan operasi database MySQL
public class DatabaseManager {
    // Konfigurasi koneksi ke database MySQL
    private static final String DB_HOST = "localhost";
    private static final String DB_PORT = "3306";
    private static final String DB_NAME = "game";
    private static final String DB_USERNAME = "root";
    private static final String DB_PASSWORD = "";

    // URL JDBC untuk koneksi ke database
    private static final String DB_URL = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME +
            "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

    /// Mengecek apakah koneksi database MySQL berhasil
    public static void initialize() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {
            System.out.println("Database connection successful!");
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /// Menyimpan score pemain: insert jika baru, update jika sudah ada serta proses CRUD
    public static void saveScore(String username, int score, int count) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {

            // Mengecek apakah username sudah ada di tabel
            String checkSql = "SELECT score, count FROM thasil WHERE username = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, username);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                // Jika sudah ada, ambil score dan count lama
                int existingScore = rs.getInt("score");
                int existingCount = rs.getInt("count");

                // Tambahkan score dan count baru ke yang lama
                int newScore = existingScore + score;
                int newCount = existingCount + count;

                // Update data pada baris yang ada
                String updateSql = "UPDATE thasil SET score = ?, count = ? WHERE username = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                updateStmt.setInt(1, newScore);
                updateStmt.setInt(2, newCount);
                updateStmt.setString(3, username);
                updateStmt.executeUpdate();
            } else {
                // Jika username belum ada, tambahkan data baru
                String insertSql = "INSERT INTO thasil (username, score, count) VALUES (?, ?, ?)";
                PreparedStatement insertStmt = conn.prepareStatement(insertSql);
                insertStmt.setString(1, username);
                insertStmt.setInt(2, score);
                insertStmt.setInt(3, count);
                insertStmt.executeUpdate();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /// Mengambil data leaderboard, diurutkan dari score tertinggi
    public static List<Player> getLeaderboard() {
        List<Player> leaderboard = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {
            // Query untuk mengambil semua data pemain dari tabel
            String sql = "SELECT username, score, count FROM thasil ORDER BY score DESC";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            // Membuat objek Player dari setiap baris dan menambahkannya ke list
            while (rs.next()) {
                Player player = new Player(
                        rs.getString("username"),
                        rs.getInt("score"),
                        rs.getInt("count")
                );
                leaderboard.add(player);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return leaderboard;
    }
}
