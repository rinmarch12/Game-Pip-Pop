package viewmodel;

import model.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

// Mengelola logika permainan
public class GameViewModel {
    // Objek untuk menyimpan status permainan (running, game over, dll.)
    private GameState gameState;
    // Objek untuk mencatat skor permainan
    private GameScore gameScore;
    // Objek pemain
    private Player player;
    // List ice cream yang aktif di layar
    private List<IceCream> iceCreams;
    // Untuk menghasilkan nilai acak
    private Random random;
    // Waktu terakhir update untuk timer detik
    private long lastSecondUpdate;

    // Flag untuk memastikan skor hanya disimpan sekali
    private boolean scoreSaved = false;

    // Koordinat target (tujuan ice cream saat dikoleksi)
    private int freezerX = 800; // X posisi freezer
    private int freezerY = 0;   // Y posisi freezer

    // Konstruktor untuk inisialisasi data permainan
    public GameViewModel(String username) {
        this.player = new Player(username);
        this.gameState = new GameState();
        this.gameScore = new GameScore();
        this.iceCreams = new ArrayList<>();
        this.random = new Random();
        this.lastSecondUpdate = System.currentTimeMillis();
        this.scoreSaved = false;
    }

    /// Memulai permainan baru dan mereset semu daya
    public void startGame() {
        gameState.start();              // Set status permainan menjadi aktif
        gameScore.reset();             // Reset skor
        iceCreams.clear();             // Kosongkan ice cream
        lastSecondUpdate = System.currentTimeMillis(); // Reset timer
        scoreSaved = false;            // Reset flag simpan skor
    }

    /// Update posisi ice cream dan timer game setiap detik
    public void updateGame() {
        if (!gameState.isGameRunning()) {
            return; // Jika game tidak berjalan, tidak perlu update
        }

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastSecondUpdate >= 1000) {
            gameState.updateTimer(); // Update waktu setiap 1 detik
            lastSecondUpdate = currentTime;
        }

        // Iterasi dan update semua ice cream
        Iterator<IceCream> iterator = iceCreams.iterator();
        while (iterator.hasNext()) {
            IceCream iceCream = iterator.next();
            iceCream.update(); // Update posisi ice cream

            if (iceCream.isBeingCollected()) {
                // Cek apakah ice cream sudah sampai ke freezer
                if (hasReachedFreezer(iceCream)) {
                    int points = iceCream.getPoints();
                    gameScore.addIceCream(points); // Tambahkan skor ke total
                    player.addScore(points);       // Tambahkan skor ke pemain
                    iterator.remove();             // Hapus ice cream dari layar

                    // Debug: tampilkan skor setelah pengumpulan
                    System.out.println("Ice cream collected! Current score: " +
                            gameScore.getScore() + " Count: " + gameScore.getCount());
                }
            } else {
                // Hapus ice cream yang keluar dari layar
                if (iceCream.isOffScreen(1000)) {
                    iterator.remove();
                }
            }
        }
    }

    /// Cek apakah ice cream telah mencapai posisi freezer
    private boolean hasReachedFreezer(IceCream iceCream) {
        int dx = iceCream.getX() - freezerX;
        int dy = iceCream.getY() - freezerY;
        double distance = Math.sqrt(dx * dx + dy * dy);
        return distance <= 10; // Toleransi jarak 10px
    }

    /// Memunculkan ice cream secara acak di jalur atas/bawah
    public void spawnRandomIceCream(int screenWidth, int topTrackY, int bottomTrackY) {
        if (!gameState.isGameRunning()) {
            return; // Tidak spawn jika game tidak berjalan
        }

        IceCream.Type[] types = IceCream.Type.values();
        IceCream.Type randomType = types[random.nextInt(types.length)];

        boolean isTopTrack = random.nextBoolean();
        int y = isTopTrack ? topTrackY : bottomTrackY;

        int x;
        boolean movingRight;

        // Tentukan arah dan posisi awal berdasarkan track
        if (isTopTrack) {
            x = screenWidth;
            movingRight = false; // Bergerak ke kiri
        } else {
            x = -IceCream.getWidth();
            movingRight = true; // Bergerak ke kanan
        }

        IceCream newIceCream = new IceCream(x, y, randomType, movingRight);
        iceCreams.add(newIceCream); // Tambahkan ke daftar
    }

    /// Mendeteksi apakah ice cream berhasil ditangkap oleh pemain
    public boolean catchIceCream(int mouseX, int mouseY) {
        for (IceCream iceCream : iceCreams) {
            if (!iceCream.isBeingCollected() && iceCream.contains(mouseX, mouseY)) {
                iceCream.startCollecting(freezerX, freezerY); // Mulai animasi koleksi
                return true;
            }
        }
        return false;
    }

    /// Menyimpan skor pemain ke database
    public void saveScore() {
        if (!scoreSaved) { // Cegah penyimpanan ganda
            DatabaseManager.saveScore(
                    player.getUsername(),
                    gameScore.getScore(),
                    gameScore.getCount()
            );
            scoreSaved = true; // Tandai sebagai sudah disimpan

            // Debug: tampilkan konfirmasi penyimpanan
            System.out.println("Skor disimpan ke database untuk user: " + player.getUsername() +
                    " Score: " + gameScore.getScore() + " Count: " + gameScore.getCount());
        } else {
            // Debug: skor sudah pernah disimpan
            System.out.println("Skor sudah pernah disimpan untuk session ini");
        }
    }

    // Mengecek apakah skor sudah disimpan
    public boolean isScoreSaved() {
        return scoreSaved;
    }

    // Tandai bahwa skor sudah disimpan (opsional jika perlu panggil secara eksplisit)
    public void markScoreAsSaved() {
        scoreSaved = true;
    }

    // Menghitung skor akhir
    public int calculateFinalScore() {
        return gameScore.getScore();
    }

    // Getter untuk objek-objek permainan
    public GameState getGameState() { return gameState; }
    public GameScore getGameScore() { return gameScore; }
    public List<IceCream> getIceCreams() { return iceCreams; }

    // Cek status permainan
    public boolean isGameRunning() { return gameState.isGameRunning(); }
    public boolean isGameOver() { return gameState.isGameOver(); }
}
