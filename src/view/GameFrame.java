package view;

import model.*;
import viewmodel.GameViewModel;

import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.sound.sampled.Clip;

// Layar utama game
public class GameFrame extends JFrame implements ActionListener, MouseListener, KeyListener {

    // Info pemain
    private String username;                    // Nama pemain
    private GameViewModel gameViewModel;        // Logika game
    private String playerUsername;              // Username untuk ditampilkan

    // Timer untuk mengatur jalannya game
    private Timer gameTimer;                    // Timer utama - mengatur kecepatan game (seperti jam)
    private Timer spawnTimer;                   // Timer untuk memunculkan ice cream baru
    private Timer lassoTimer;                   // Timer untuk animasi tali lasso

    // Ukuran layar game
    private static final int WINDOW_WIDTH = 1000;   // Lebar layar
    private static final int WINDOW_HEIGHT = 700;   // Tinggi layar

    // Posisi pinguin (bisa bergerak)
    private int pipX = WINDOW_WIDTH / 2 - 50;       // Posisi pinguin dari kiri (X)
    private int pipY = WINDOW_HEIGHT / 2 - 100;     // Posisi pinguin dari atas (Y)

    // Seberapa cepat pinguin bergerak
    private static final int PENGUIN_SPEED = 10;

    // Batas area pinguin boleh bergerak (tidak boleh keluar layar)
    private static final int PENGUIN_MIN_X = 0;                    // Paling kiri
    private static final int PENGUIN_MAX_X = WINDOW_WIDTH - 150;   // Paling kanan
    private static final int PENGUIN_MIN_Y = 0;                    // Paling atas
    private static final int PENGUIN_MAX_Y = WINDOW_HEIGHT - 180;  // Paling bawah

    // Status tombol keyboard (apakah sedang ditekan?)
    private boolean upPressed = false;      // Tombol panah atas
    private boolean downPressed = false;    // Tombol panah bawah
    private boolean leftPressed = false;    // Tombol panah kiri
    private boolean rightPressed = false;   // Tombol panah kanan

    // Lokasi jalur tempat ice cream muncul
    private static final int TOP_TRACK_Y = 150;     // Jalur atas
    private static final int BOTTOM_TRACK_Y = 450;  // Jalur bawah

    // Posisi kotak skor (freezer) di layar
    private static final int FREEZER_X = 790;   // Dari kiri
    private static final int FREEZER_Y = 20;    // Dari atas

    // Tempat menyimpan semua gambar
    private Map<String, BufferedImage> images;      // Kumpulan gambar
    private BufferedImage backgroundImage;          // Gambar latar belakang

    // Variabel untuk efek tali lasso (saat klik mouse)
    private boolean showLasso = false;      // Apakah lasso sedang ditampilkan?
    private int lassoTargetX, lassoTargetY; // Posisi target lasso
    private float lassoAlpha = 1.0f;       // Transparansi lasso (1.0 = tidak transparan)

    // File suara untuk efek audio
    private Clip backgroundClip;    // Musik latar belakang
    private Clip gameOverClip;      // Suara saat game over
    private Clip iceCreamClip;      // Suara saat dapat ice cream

    // Panel untuk menggambar game
    private GameDrawingPanel drawingPanel;

    public GameFrame(String username, Clip backgroundClip) {
        // Simpan info pemain
        this.username = username;
        this.playerUsername = username;
        this.gameViewModel = new GameViewModel(username);
        this.backgroundClip = backgroundClip; // Pakai musik yang sama dari menu utama

        // Setup jendela game
        setTitle("Pip Pop - Bermain sebagai: " + username);  // Judul jendela
        setDefaultCloseOperation(EXIT_ON_CLOSE);             // Tutup program saat jendela ditutup
        setResizable(false);                                 // Tidak bisa diubah ukurannya

        // Setup kontrol keyboard - supaya bisa menerima input tombol
        this.addKeyListener(this);          // Dengarkan keyboard
        this.setFocusable(true);           // Bisa menerima input
        this.requestFocusInWindow();       // Minta fokus keyboard

        // Setup panel untuk menggambar
        drawingPanel = new GameDrawingPanel();
        add(drawingPanel);  // Tambahkan panel ke jendela
        pack();             // Sesuaikan ukuran jendela

        setLocationRelativeTo(null);  // Letakkan jendela di tengah layar

        // Siapkan semua komponen game
        loadImages();  // Muat semua gambar
        loadSounds();  // Muat semua suara
        setupTimers(); // Siapkan timer
        startGame();   // Mulai game

        setVisible(true);  // Tampilkan jendela

        // Tangani saat jendela ditutup - bersihkan memori
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                stopAllTimers();  // Hentikan semua timer supaya tidak jalan terus
            }
        });
    }

    private void stopAllTimers() {
        // Hentikan timer utama game
        if (gameTimer != null && gameTimer.isRunning()) {
            gameTimer.stop();
        }
        // Hentikan timer spawn ice cream
        if (spawnTimer != null && spawnTimer.isRunning()) {
            spawnTimer.stop();
        }
        // Hentikan timer animasi lasso
        if (lassoTimer != null && lassoTimer.isRunning()) {
            lassoTimer.stop();
        }
    }

    public void resetGame() {
        // Hentikan semua timer dulu
        stopAllTimers();

        // Kembalikan semua ke kondisi awal
        gameViewModel = new GameViewModel(username);        // Reset logika game
        pipX = WINDOW_WIDTH / 2 - 50;                      // Pinguin kembali ke tengah
        pipY = WINDOW_HEIGHT / 2 - 100;
        upPressed = downPressed = leftPressed = rightPressed = false;  // Reset tombol
        showLasso = false;                                 // Hilangkan lasso
        lassoAlpha = 1.0f;                                // Reset transparansi

        // Mulai ulang game
        setupTimers();
        startGame();

        drawingPanel.requestFocusInWindow();  // Minta fokus keyboard lagi
        repaint();                           // Gambar ulang layar
    }

    /// Memuat semua gambar (pinguin, es krim, background)
    private void loadImages() {
        images = new HashMap<>();  // Buat tempat penyimpanan gambar

        // Daftar nama file gambar yang dibutuhkan
        String[] imageFiles = {
                "pinguin.png",       // Gambar pinguin
                "ice_vanila.png",    // Ice cream vanila
                "ice_strowbery.png", // Ice cream strawberry
                "ice_coklat.png",    // Ice cream coklat
                "ice_pelangi.png",   // Ice cream pelangi
                "freezer.png",       // Gambar freezer (kotak skor)
                "background.png",    // Gambar latar belakang
                "time.png"           // Icon waktu
        };

        // Muat setiap gambar satu per satu
        for (String filename : imageFiles) {
            try {
                // Coba baca file gambar dari folder assets
                InputStream is = getClass().getResourceAsStream("/assets/" + filename);
                if (is != null) {
                    BufferedImage img = ImageIO.read(is);  // Baca gambar
                    images.put(filename, img);             // Simpan ke penyimpanan
                } else {
                    System.err.println("Tidak dapat memuat gambar: " + filename);
                }
            } catch (IOException e) {
                System.err.println("Error memuat gambar " + filename + ": " + e.getMessage());
            }
        }

        // Simpan gambar latar belakang secara terpisah untuk kemudahan akses
        backgroundImage = images.get("background.png");
    }

    /// Memuat semua file audio
    private void loadSounds() {
        gameOverClip = loadSound("game_over.wav");    // Suara saat game over
        iceCreamClip = loadSound("ice_cream.wav");    // Suara saat dapat ice cream
    }

    private Clip loadSound(String filename) {
        try {
            // Baca file audio
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(getClass().getResource("/assets/" + filename));
            Clip clip = AudioSystem.getClip();  // Buat player suara
            clip.open(audioIn);                 // Masukkan audio ke player
            return clip;                        // Kembalikan player
        } catch (Exception e) {
            System.err.println("Error memuat suara: " + filename + " - " + e.getMessage());
            return null;  // Kalau gagal kembalikan null
        }
    }

    private void playSound(Clip clip, boolean loop) {
        if (clip != null) {
            if (clip.isRunning()) clip.stop();  // Hentikan dulu kalau masih jalan
            clip.setFramePosition(0);           // Kembali ke awal
            if (loop) {
                clip.loop(Clip.LOOP_CONTINUOUSLY);  // Putar terus menerus
            } else {
                clip.start();                       // Putar sekali
            }
        }
    }

    /// Mengatur 3 timer utama game
    private void setupTimers() {
        // Timer utama untuk game loop
        gameTimer = new Timer(16, this);

        // Timer untuk memunculkan ice cream baru setiap 2.5 detik
        spawnTimer = new Timer(2500, e ->
                gameViewModel.spawnRandomIceCream(WINDOW_WIDTH, TOP_TRACK_Y, BOTTOM_TRACK_Y)
        );

        // Timer untuk animasi fade lasso (efek transparansi)
        lassoTimer = new Timer(40, e -> {
            if (showLasso) {
                lassoAlpha -= 0.1f;  // Kurangi transparansi (makin transparan)
                if (lassoAlpha <= 0f) {
                    lassoAlpha = 0f;
                    showLasso = false;   // Hilangkan lasso
                    lassoTimer.stop();   // Hentikan timer
                }
                repaint();  // Gambar ulang untuk menampilkan perubahan
            }
        });
    }

    private void startGame() {
        gameViewModel.startGame();  // Mulai logika game
        gameTimer.start();          // Mulai timer utama
        spawnTimer.start();         // Mulai timer spawn ice cream

        // Pastikan musik latar belakang tetap berjalan
        if (backgroundClip != null && !backgroundClip.isRunning()) {
            backgroundClip.start();
        }

        drawingPanel.requestFocusInWindow();  // Minta fokus keyboard
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == gameTimer) {
            gameLoop();  // Panggil game loop setiap detak timer
        }
    }

    ///  3 loop utama game yang dijalankan secara terus-menerus selama game masih berjalan
    private void gameLoop() {
        // Update posisi pinguin berdasarkan tombol yang ditekan
        updatePenguinMovement();

        // Update logika game (update skor, waktu, ice cream)
        gameViewModel.updateGame();

        // Cek apakah game sudah berakhir (waktu habis/kondisi game over)
        if (gameViewModel.isGameOver()) {
            endGame();
        }

        // Gambar ulang layar dengan kondisi terbaru
        repaint();
    }

    /// Menggerakkan pinguin sesuai input keyboard
    private void updatePenguinMovement() {
        // Hanya gerakkan pinguin kalau game sedang berjalan
        if (gameViewModel.isGameRunning()) {
            int newX = pipX;  // Posisi X baru
            int newY = pipY;  // Posisi Y baru

            // Cek pergerakan horizontal (kiri-kanan)
            if (leftPressed && !rightPressed) {
                newX -= PENGUIN_SPEED;  // Gerak ke kiri
            } else if (rightPressed && !leftPressed) {
                newX += PENGUIN_SPEED;  // Gerak ke kanan
            }

            // Cek pergerakan vertikal (atas-bawah)
            if (upPressed && !downPressed) {
                newY -= PENGUIN_SPEED;  // Gerak ke atas
            } else if (downPressed && !upPressed) {
                newY += PENGUIN_SPEED;  // Gerak ke bawah
            }

            // Pastikan pinguin tidak keluar layar
            // Seperti invisible wall di game
            newX = Math.max(PENGUIN_MIN_X, Math.min(PENGUIN_MAX_X, newX));
            newY = Math.max(PENGUIN_MIN_Y, Math.min(PENGUIN_MAX_Y, newY));

            // Terapkan posisi baru
            pipX = newX;
            pipY = newY;
        }
    }

    private void endGame() {
        // Hentikan semua timer
        stopAllTimers();

        // Kalau tidak, kedua suara akan campur dan jadi bising
        if (backgroundClip != null && backgroundClip.isRunning()) {
            backgroundClip.stop();
            System.out.println("Music latar dihentikan saat game over");
        }

        // Sekarang putar suara game over (tanpa music latar mengganggu)
        playSound(gameOverClip, false);

        // Ambil data game dan skor
        GameScore gameScore = gameViewModel.getGameScore();

        // Simpan skor ke database/file (hanya sekali per game)
        if (!gameViewModel.isScoreSaved()) {
            gameViewModel.saveScore();
            gameViewModel.markScoreAsSaved();

            System.out.println("Skor disimpan: " + gameScore.getScore() +
                    " Jumlah ice cream: " + gameScore.getCount());
        }

        // Tampilkan dialog game over dengan hasil skor
        GameOver gameOverDialog = new GameOver(
                this,
                playerUsername,
                gameScore.getScore(),
                gameScore.getCount()
        );
        gameOverDialog.setVisible(true);
    }

    private void drawUI(Graphics2D g2d) {
        GameState gameState = gameViewModel.getGameState();
        GameScore gameScore = gameViewModel.getGameScore();

        // Gambar kotak timer di pojok kiri atas
        int timerBoxY = 20;
        g2d.setColor(Color.WHITE);                                      // Warna putih
        g2d.fillRoundRect(20, timerBoxY, 120, 40, 10, 10);            // Kotak dengan sudut bulat
        g2d.setColor(Color.BLACK);                                      // Warna hitam untuk border
        g2d.drawRoundRect(20, timerBoxY, 120, 40, 10, 10);            // Garis tepi kotak

        // Gambar icon waktu di dalam kotak
        BufferedImage timeIcon = images.get("time.png");
        if (timeIcon != null) {
            g2d.drawImage(timeIcon, 25, timerBoxY + 8, 20, 20, null);  // Gambar icon kecil
        }

        // Gambar teks waktu
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.drawString("Waktu: " + gameState.getTimeString(), 50, timerBoxY + 25);

        // Gambar freezer (kotak skor) di pojok kanan atas
        BufferedImage freezerImage = images.get("freezer.png");
        int freezerWidth = 190;
        int freezerHeight = 110;

        if (freezerImage != null) {
            // Gambar freezer dari file gambar
            g2d.drawImage(freezerImage, FREEZER_X, FREEZER_Y, freezerWidth, freezerHeight, null);
        } else {
            // Gambar freezer sederhana kalau file gambar tidak ada
            g2d.setColor(Color.WHITE);
            g2d.fillRoundRect(FREEZER_X, FREEZER_Y, freezerWidth, freezerHeight, 20, 20);
            g2d.setColor(Color.BLACK);
            g2d.drawRoundRect(FREEZER_X, FREEZER_Y, freezerWidth - 1, freezerHeight - 1, 20, 20);
        }

        // Gambar teks skor dan jumlah ice cream di dalam freezer
        g2d.setFont(new Font("Arial", Font.PLAIN, 18));
        g2d.setColor(Color.BLACK);
        g2d.drawString("Score: " + gameScore.getScore(), FREEZER_X + 10, FREEZER_Y + 40);
        g2d.drawString("Count: " + gameScore.getCount(), FREEZER_X + 10, FREEZER_Y + 70);
    }

    /// Menangani klik mouse untuk menangkap es krim
    @Override
    public void mouseClicked(MouseEvent e) {
        // Hanya proses klik kalau game sedang berjalan
        if (gameViewModel.isGameRunning()) {
            // Hindari klik pada area kontrol UI (supaya tidak mengganggu)
            if (e.getX() < 220 && e.getY() > WINDOW_HEIGHT - 120) return;

            // Tampilkan efek lasso (tali) dari pinguin ke posisi klik
            showLasso = true;
            lassoAlpha = 1.0f;              // Mulai tidak transparan
            lassoTargetX = e.getX();        // Posisi klik X
            lassoTargetY = e.getY();        // Posisi klik Y

            // Mulai animasi fade lasso
            if (!lassoTimer.isRunning()) {
                lassoTimer.start();
            }

            // Coba tangkap ice cream di posisi yang diklik
            boolean caught = gameViewModel.catchIceCream(e.getX(), e.getY());
            if (caught) {
                playSound(iceCreamClip, false); // Putar suara "berhasil" sekali
            }
        }
    }

    /// Menangani input keyboard
    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();  // Ambil kode tombol yang ditekan

        switch (keyCode) {
            case KeyEvent.VK_UP:        // ke atas untuk bergerak ke atas
                upPressed = true;
                break;
            case KeyEvent.VK_DOWN:      // ke bawah untuk bergerak ke bawah
                downPressed = true;
                break;
            case KeyEvent.VK_LEFT:      // ke kiri untuk bergerak ke kiri
                leftPressed = true;
                break;
            case KeyEvent.VK_RIGHT:     // ke kanan untuk bergerak ke kanan
                rightPressed = true;
                break;
            case KeyEvent.VK_SPACE:     // [Spasi] untuk keluar dari game
                System.out.println("Tombol spasi ditekan - keluar dari game");

                // Simpan skor sebelum keluar
                if (gameViewModel != null) {
                    // Hentikan game dulu
                    if (gameViewModel.isGameRunning()) {
                        gameViewModel.getGameState().stop();
                    }

                    // Ambil skor saat ini
                    int currentScore = gameViewModel.getGameScore().getScore();
                    int currentCount = gameViewModel.getGameScore().getCount();

                    System.out.println("Skor sebelum disimpan: " + currentScore +
                            " Jumlah: " + currentCount);

                    // Simpan ke database/file
                    gameViewModel.saveScore();

                    System.out.println("Skor berhasil disimpan!");
                }

                // Bersihkan memori dan kembali ke menu utama
                stopAllTimers();     // Hentikan semua timer
                this.dispose();      // Tutup jendela game

                // Tampilkan menu utama lagi
                MainMenu mainMenu = MainMenu.getInstance();
                mainMenu.refreshLeaderboard();  // Refresh daftar skor
                mainMenu.setVisible(true);      // Tampilkan menu
                break;
        }
    }

    /// Menangani saat tombol dilepas
    @Override
    public void keyReleased(KeyEvent e) {
        int keyCode = e.getKeyCode();

        // Set flag tombol menjadi false (tidak ditekan)
        switch (keyCode) {
            case KeyEvent.VK_UP:     // Lepas ke atas
                upPressed = false;
                break;
            case KeyEvent.VK_DOWN:   // Lepas ke bawah
                downPressed = false;
                break;
            case KeyEvent.VK_LEFT:   // Lepas ke kiri
                leftPressed = false;
                break;
            case KeyEvent.VK_RIGHT:  // Lepas ke kanan
                rightPressed = false;
                break;
        }
    }

    // Method yang harus ada karena implement interface, tapi tidak digunakan
    @Override
    public void keyTyped(KeyEvent e) {
        // Tidak digunakan
    }

    @Override
    public void mousePressed(MouseEvent e) {}
    @Override
    public void mouseReleased(MouseEvent e) {}
    @Override
    public void mouseEntered(MouseEvent e) {}
    @Override
    public void mouseExited(MouseEvent e) {}

    private class GameDrawingPanel extends JPanel {

        public GameDrawingPanel() {
            setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));  // Set ukuran panel
            setFocusable(true);                  // Bisa menerima input
            addMouseListener(GameFrame.this);    // Dengarkan klik mouse
            addKeyListener(GameFrame.this);      // Dengarkan keyboard
            setDoubleBuffered(true);             // Untuk rendering yang halus (anti kedip)
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);  // Bersihkan kanvas
            Graphics2D g2d = (Graphics2D) g;  // Upgrade ke Graphics2D untuk fitur lebih banyak

            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

            // Gambar latar belakang dulu (paling belakang)
            if (backgroundImage != null) {
                g2d.drawImage(backgroundImage, 0, 0, WINDOW_WIDTH, WINDOW_HEIGHT, null);
            } else {
                // Warna latar belakang cadangan kalau gambar tidak ada
                g2d.setColor(new Color(135, 206, 235));  // Warna biru langit
                g2d.fillRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
            }

            // Gambar pinguin di posisi yang bisa berubah
            BufferedImage pipImage = images.get("pinguin.png");
            if (pipImage != null) {
                // Gambar dari file
                g2d.drawImage(pipImage, pipX, pipY, 150, 180, null);
            } else {
                // Gambar pinguin sederhana kalau file tidak ada
                g2d.setColor(Color.BLACK);                           // Warna hitam untuk badan
                g2d.fillOval(pipX, pipY, 50, 60);                   // Badan pinguin
                g2d.setColor(Color.WHITE);                           // Warna putih untuk perut
                g2d.fillOval(pipX + 10, pipY + 15, 30, 35);         // Perut pinguin
                g2d.setColor(Color.RED);                             // Warna merah untuk paruh
                g2d.fillRect(pipX + 5, pipY + 25, 40, 8);           // Paruh pinguin
            }

            // Gambar semua ice cream yang ada di layar
            if (gameViewModel != null) {
                for (IceCream iceCream : gameViewModel.getIceCreams()) {
                    // Ambil gambar ice cream sesuai jenisnya
                    BufferedImage iceCreamImage = images.get(iceCream.getType().getFilename());
                    if (iceCreamImage != null) {
                        // Gambar ice cream dari file
                        g2d.drawImage(
                                iceCreamImage,
                                iceCream.getX(),        // Posisi X
                                iceCream.getY(),        // Posisi Y
                                IceCream.getWidth(),    // Lebar
                                IceCream.getHeight(),   // Tinggi
                                null
                        );
                    } else {
                        // Gambar kotak sederhana kalau file tidak ada
                        g2d.setColor(Color.PINK);                    // Warna pink untuk ice cream
                        g2d.fillRect(iceCream.getX(), iceCream.getY(),
                                IceCream.getWidth(), IceCream.getHeight());
                        g2d.setColor(Color.BLACK);                   // Warna hitam untuk garis tepi
                        g2d.drawRect(iceCream.getX(), iceCream.getY(),
                                IceCream.getWidth(), IceCream.getHeight());
                    }
                }
            }

            // Gambar efek lasso (tali) dengan transparansi alpha
            if (showLasso) {
                // Simpan setting asli dulu
                Composite originalComposite = g2d.getComposite();
                Stroke originalStroke = g2d.getStroke();

                // Set transparansi sesuai lassoAlpha (makin kecil makin transparan)
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, lassoAlpha));
                g2d.setColor(Color.YELLOW);                    // Warna kuning untuk lasso
                g2d.setStroke(new BasicStroke(3));             // Garis tebal 3 pixel

                // Gambar garis lasso dari pinguin ke posisi target
                g2d.drawLine(pipX + 40, pipY + 135, lassoTargetX, lassoTargetY);

                // Gambar lingkaran kecil di ujung lasso
                g2d.fillOval(lassoTargetX - 5, lassoTargetY - 5, 10, 10);

                // Kembalikan setting asli
                g2d.setStroke(originalStroke);
                g2d.setComposite(originalComposite);
            }

            // Gambar elemen UI (timer, skor, dll) - paling depan
            if (gameViewModel != null) {
                drawUI(g2d);
            }
        }
    }
}