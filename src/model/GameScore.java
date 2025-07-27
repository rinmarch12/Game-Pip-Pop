package model;

// Sistem penilaian untuk game
public class GameScore {
    // Menyimpan total skor
    private int score;
    // Menyimpan jumlah item (misalnya, ice cream) yang dikumpulkan
    private int count;

    // Konstruktor default, inisialisasi skor dan jumlah ke 0
    public GameScore() {
        this.score = 0;
        this.count = 0;
    }

    /// Menambahkan score dan count ice cream
    public void addIceCream(int points) {
        this.score += points;
        this.count++;
    }

    /// Mengatur ulang score dan count ke 0 untuk game baru
    public void reset() {
        this.score = 0;
        this.count = 0;
    }

    /// Mengembalikan tampilan score misal "Score: 100"
    public String getScoreDisplay() {
        return String.format("Score: %d", score);
    }

    /// Mengembalikan tampilan count misal "Count: 5"
    public String getCountDisplay() {
        return String.format("Count: %d", count);
    }

    // Getter untuk skor
    public int getScore() { return score; }

    // Getter untuk jumlah item
    public int getCount() { return count; }
}
