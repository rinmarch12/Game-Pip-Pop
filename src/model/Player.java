package model;

// Data Pemain
public class Player {
    // Nama pengguna
    private String username;
    // Total skor yang dikumpulkan
    private int totalScore;
    // Jumlah es krim yang dikumpulkan
    private int iceCreamCount;

    /// Konstruktor untuk pemain baru (skor dan count 0)
    public Player(String username) {
        this.username = username;
        this.totalScore = 0;
        this.iceCreamCount = 0;
    }

    /// Konstruktor untuk pemain dari database (sudah punya skor dan jumlah)
    public Player(String username, int totalScore, int iceCreamCount) {
        this.username = username;
        this.totalScore = totalScore;
        this.iceCreamCount = iceCreamCount;
    }

    // Getter untuk username
    public String getUsername() { return username; }

    // Getter untuk total skor
    public int getTotalScore() { return totalScore; }

    // Getter untuk jumlah es krim
    public int getIceCreamCount() { return iceCreamCount; }

    /// Menambah skor dan count
    public void addScore(int points) {
        this.totalScore += points;
        this.iceCreamCount++;
    }

    // Format tampilkan di leaderboard
    @Override
    public String toString() {
        return String.format("%-15s | Score: %-6d | Count: %d",
                username, totalScore, iceCreamCount);
    }
}
