package model;

// Pengontrol status permainan (berjalan/berhenti)
public class GameState {
    // Waktu tersisa dalam detik
    private int timeLeft;
    // Status permainan sedang berjalan atau tidak
    private boolean isGameRunning;

    // Durasi total permainan (dalam detik)
    public static final int GAME_DURATION = 60;

    // Konstruktor: setel ulang waktu dan status game
    public GameState() {
        reset();
    }

    /// Mengatur ulang waktu ke awal dan menghentikan permainan
    public void reset() {
        this.timeLeft = GAME_DURATION;
        this.isGameRunning = false;
    }

    /// Memulai permainan
    public void start() {
        this.isGameRunning = true;
    }

    /// Menghentikan permainan
    public void stop() {
        this.isGameRunning = false;
    }

    /// Mengurangi waktu jika game sedang berjalan hentikan otomatis jika habis
    public void updateTimer() {
        if (isGameRunning && timeLeft > 0) {
            timeLeft--;
            // Hentikan game jika waktu habis
            if (timeLeft <= 0) {
                stop();
            }
        }
    }

    /// Mengembalikan waktu dalam format mm:ss
    public String getTimeString() {
        int minutes = timeLeft / 60;
        int seconds = timeLeft % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    // Getter untuk status permainan
    public boolean isGameRunning() { return isGameRunning; }

    // Mengecek apakah permainan sudah berakhir
    public boolean isGameOver() { return !isGameRunning; }
}
