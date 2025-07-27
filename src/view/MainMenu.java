package view;

import model.DatabaseManager;
import model.Player;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.border.TitledBorder;
import javax.swing.table.JTableHeader;
import javax.sound.sampled.*; 
import java.io.IOException;
import java.net.URL;

// Menu utama dengan leaderboard
public class MainMenu extends JFrame {
    private JTextField usernameField;
    private JButton playButton;
    private JButton quitButton;
    private JTable leaderboardTable;
    private static Clip backgroundClip;         // Audio clip untuk backsound
    private static boolean musicStarted = false; // Flag agar musik tidak diputar dua kali
    private static MainMenu instance;           // Singleton untuk MainMenu

    // Memastikan hanya satu instance MainMenu yang dibuat selama program berjalan
    public static MainMenu getInstance() {
        if (instance == null) {
            instance = new MainMenu();
        }
        return instance;
    }

    // Konstruktor private untuk Singleton
    private MainMenu() {
        initComponents();      // Inisialisasi UI
        setupLayout();         // Atur tata letak komponen
        loadLeaderboard();     // Tampilkan data leaderboard dari database
        playBackgroundMusic(); // Putar musik latar belakang
    }

    /// Inisialisasi semua komponen GUI (input, tombol, tabel)
    private void initComponents() {
        setTitle("Pip Pop");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setSize(800, 600);
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(224, 242, 255));

        // Input username
        usernameField = new JTextField(20);
        usernameField.setFont(new Font("Arial", Font.PLAIN, 14));
        usernameField.setPreferredSize(new Dimension(400, 30));

        // Tombol PLAY
        playButton = new JButton("PLAY");
        playButton.setFont(new Font("Arial", Font.BOLD, 15));
        playButton.setPreferredSize(new Dimension(250, 30));
        playButton.setBackground(new Color(76, 175, 80)); 
        playButton.setForeground(Color.BLACK);
        playButton.setFocusPainted(false);
        playButton.setBorder(null);
        playButton.setOpaque(true);
        playButton.addActionListener(new StartGameListener());

        // Tombol QUIT
        quitButton = new JButton("QUIT");
        quitButton.setFont(new Font("Arial", Font.BOLD, 15));
        quitButton.setPreferredSize(new Dimension(250, 30));
        quitButton.setBackground(new Color(244, 67, 54));  
        quitButton.setForeground(Color.BLACK);
        quitButton.setFocusPainted(false);
        quitButton.setBorder(null);
        quitButton.setOpaque(true);
        quitButton.addActionListener(e -> {
            // Berhentiin musik sebelum keluar
            if (backgroundClip != null && backgroundClip.isRunning()) {
                backgroundClip.stop();
                backgroundClip.close();
            }
            System.exit(0);
        });

        // Tabel Leaderboard
        String[] columnNames = {"Rank", "Username", "Score", "Count"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Semua sel non-editable
            }
        };

        leaderboardTable = new JTable(tableModel);
        leaderboardTable.getColumnModel().getColumn(0).setPreferredWidth(50);  // Rank
        leaderboardTable.getColumnModel().getColumn(1).setPreferredWidth(200); // Username
        leaderboardTable.getColumnModel().getColumn(2).setPreferredWidth(100); // Score
        leaderboardTable.getColumnModel().getColumn(3).setPreferredWidth(100); // Count

        // Styling tabel
        leaderboardTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        leaderboardTable.setRowHeight(28);
        leaderboardTable.setBackground(Color.WHITE);
        leaderboardTable.setForeground(Color.BLACK);
        leaderboardTable.setSelectionBackground(new Color(200, 230, 255));
        leaderboardTable.setSelectionForeground(Color.BLACK);
        leaderboardTable.setShowGrid(true);
        leaderboardTable.setGridColor(new Color(220, 220, 220));

        // Klik pada baris leaderboard -> autofill username
        leaderboardTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int selectedRow = leaderboardTable.getSelectedRow();
                if (selectedRow != -1) {
                    Object usernameObj = leaderboardTable.getValueAt(selectedRow, 1);
                    if (usernameObj != null && !usernameObj.toString().contains("No scores")) {
                        usernameField.setText(usernameObj.toString());
                    }
                }
            }
        });

        // Header tabel
        JTableHeader header = leaderboardTable.getTableHeader();
        header.setBackground(new Color(230, 230, 230));
        header.setForeground(Color.BLACK);
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setReorderingAllowed(false);
    }

    /// Menyusun layout panel utama
    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));

        // Judul
        JLabel titleLabel = new JLabel("PIP POP", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 30, 0));
        add(titleLabel, BorderLayout.NORTH);

        // Panel tengah (username + leaderboard)
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBackground(new Color(224, 242, 255));

        // Panel input username
        JPanel usernamePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        usernamePanel.setBackground(new Color(224, 242, 255));
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        usernamePanel.add(usernameLabel);
        usernamePanel.add(usernameField);
        centerPanel.add(usernamePanel, BorderLayout.NORTH);

        // Panel leaderboard
        JPanel leaderboardPanel = new JPanel(new BorderLayout());
        leaderboardPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEmptyBorder(),
                "LEADERBOARD",
                TitledBorder.CENTER,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14)
        ));
        leaderboardPanel.setBackground(new Color(224, 242, 255));

        JScrollPane scrollPane = new JScrollPane(leaderboardTable);
        scrollPane.setPreferredSize(new Dimension(580, 220));
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        JPanel tableWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        tableWrapper.setBackground(new Color(224, 242, 255));
        tableWrapper.add(scrollPane);
        leaderboardPanel.add(tableWrapper, BorderLayout.CENTER);
        centerPanel.add(leaderboardPanel, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // Panel tombol bawah
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 30));
        buttonPanel.setBackground(new Color(224, 242, 255));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 30, 0));
        buttonPanel.add(playButton);
        buttonPanel.add(quitButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    /// Memuat data leaderboard dari database
    private void loadLeaderboard() {
        List<Player> leaderboard = DatabaseManager.getLeaderboard();
        DefaultTableModel model = (DefaultTableModel) leaderboardTable.getModel();
        model.setRowCount(0); // Hapus data lama

        for (int i = 0; i < leaderboard.size(); i++) {
            Player player = leaderboard.get(i);
            Object[] row = {
                    i + 1,
                    player.getUsername(),
                    player.getTotalScore(),
                    player.getIceCreamCount()
            };
            model.addRow(row);
        }

        if (leaderboard.isEmpty()) {
            // Jika kosong, tampilkan placeholder
            Object[] emptyRow = {"No scores", "recorded yet.", "Be the first!", ""};
            model.addRow(emptyRow);
        }
    }

    /// Validasi input nama pemain
    public String validateUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return "Username cannot be empty";
        }
        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            return "Username can only contain letters, numbers, and underscores";
        }
        return null;
    }

    // Listener tombol PLAY
    private class StartGameListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String username = usernameField.getText().trim();
            String validationError = validateUsername(username);

            if (validationError != null) {
                JOptionPane.showMessageDialog(MainMenu.this,
                        validationError,
                        "Invalid Username",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Buka jendela game baru
            GameFrame gameFrame = new GameFrame(username, backgroundClip);
            gameFrame.setVisible(true);

            // Sembunyikan MainMenu (tidak dispose agar bisa balik lagi)
            MainMenu.this.setVisible(false);
        }
    }

    /// Untuk memulai ulang musik ketika kembali dari game
    public void resumeBackgroundMusic() {
        System.out.println("Mencoba memulai ulang background music...");

        // Cek apakah clip masih ada dan bisa digunakan
        if (backgroundClip != null) {
            if (!backgroundClip.isRunning()) {
                try {
                    // Reset posisi ke awal dan mulai ulang
                    backgroundClip.setFramePosition(0);
                    backgroundClip.loop(Clip.LOOP_CONTINUOUSLY);
                    backgroundClip.start();
                    System.out.println("Background music berhasil dimulai ulang");
                } catch (Exception e) {
                    System.err.println("Error saat memulai ulang musik: " + e.getMessage());
                    // Jika gagal, coba load ulang file audio
                    reloadBackgroundMusic();
                }
            } else {
                System.out.println("Background music sudah berjalan");
            }
        } else {
            System.out.println("Background clip null, loading ulang...");
            reloadBackgroundMusic();
        }
    }

    // Untuk reload file audio jika clip rusak
    private void reloadBackgroundMusic() {
        try {
            URL soundURL = getClass().getResource("/assets/backsound.wav");
            if (soundURL == null) {
                System.err.println("File backsound.wav tidak ditemukan di /assets");
                return;
            }

            // Tutup clip lama jika ada
            if (backgroundClip != null) {
                backgroundClip.close();
            }

            AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundURL);
            backgroundClip = AudioSystem.getClip();
            backgroundClip.open(audioStream);
            backgroundClip.loop(Clip.LOOP_CONTINUOUSLY);
            backgroundClip.start();
            System.out.println("Background music berhasil di-reload dan dimulai");
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException ex) {
            System.err.println("Error saat reload background music: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /// Memutar musik backsound
    private void playBackgroundMusic() {
        if (musicStarted) return;

        try {
            URL soundURL = getClass().getResource("/assets/backsound.wav");
            if (soundURL == null) {
                System.err.println("File backsound.wav tidak ditemukan di /assets");
                return;
            }

            AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundURL);
            backgroundClip = AudioSystem.getClip();
            backgroundClip.open(audioStream);
            backgroundClip.loop(Clip.LOOP_CONTINUOUSLY);
            backgroundClip.start();
            musicStarted = true;
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException ex) {
            ex.printStackTrace();
        }
    }

    // Untuk me-refresh leaderboard dan musik saat kembali dari permainan
    public void refreshLeaderboard() {
        loadLeaderboard();
        resumeBackgroundMusic(); // Mulai ulang musik background

        // Kosongkan input username setelah kembali dari game
        usernameField.setText("");

        // Hilangkan selection pada tabel leaderboard juga
        leaderboardTable.clearSelection();

    }
}