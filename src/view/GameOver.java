package view;

import javax.swing.*;
import java.awt.*;

// Dialog Game Over untuk menampilkan hasil akhir permainan
public class GameOver extends JDialog {
    private String username;
    private int finalScore;
    private int iceCreamCount;
    private Frame parentFrame;

    // Konstruktor GameOver dialog
    public GameOver(Window parent, String username, int finalScore, int iceCreamCount) {
        super(parent, "", ModalityType.APPLICATION_MODAL); // Membuat dialog modal (fokus utama)
        this.username = username;
        this.finalScore = finalScore;
        this.iceCreamCount = iceCreamCount;
        this.parentFrame = (parent instanceof Frame) ? (Frame) parent : null;

        initComponents();   // Inisialisasi komponen dasar
        setupLayout();      // Menyusun layout tampilan
        pack();             // Menyesuaikan ukuran dengan isi
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize(); // Ambil ukuran layar
        Dimension dialogSize = getSize();
        int x = (screenSize.width - dialogSize.width) / 2;
        int y = (screenSize.height - dialogSize.height) / 2;
        setLocation(x, y);  // Tampilkan dialog di tengah layar
    }

    /// Set tampilan dasar dialog game over
    private void initComponents() {
        setUndecorated(true);                         // Hilangkan border default
        setResizable(false);                          // Tidak bisa diubah ukurannya
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);   // Tutup dialog saat tombol close ditekan
    }

    /// Mengatur tata letak komponen pada dialog game over
    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));

        add(createCustomTitleBar(), BorderLayout.NORTH); // Tambahkan bar judul

        // Panel ikon (sebelah kiri)
        JPanel iconPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        iconPanel.setOpaque(false);
        JLabel infoIcon = new JLabel(UIManager.getIcon("OptionPane.informationIcon"));
        iconPanel.add(infoIcon);

        // Panel teks (informasi username, skor, dan jumlah es krim)
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        textPanel.add(createInfoLabel("Username: " + username));
        textPanel.add(Box.createVerticalStrut(5));
        textPanel.add(createInfoLabel("Score: " + finalScore));
        textPanel.add(Box.createVerticalStrut(5));
        textPanel.add(createInfoLabel("Count: " + iceCreamCount));

        // Gabungkan ikon dan teks ke tengah dialog
        JPanel centerContent = new JPanel(new BorderLayout());
        centerContent.setOpaque(false);
        centerContent.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        centerContent.add(iconPanel, BorderLayout.WEST);
        centerContent.add(textPanel, BorderLayout.CENTER);

        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setOpaque(false);
        centerWrapper.add(centerContent, BorderLayout.CENTER);
        centerWrapper.add(new JSeparator(), BorderLayout.SOUTH);
        add(centerWrapper, BorderLayout.CENTER);

        // Panel tombol (Restart dan Back)
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setOpaque(false);

        JButton restartButton = new JButton("Restart");
        JButton backButton = new JButton("Back to Menu");

        // Gaya tombol
        restartButton.setPreferredSize(new Dimension(140, 30));
        restartButton.setBackground(new Color(224, 242, 255));
        restartButton.setForeground(Color.BLACK);
        restartButton.setFont(new Font("SansSerif", Font.BOLD, 13));
        restartButton.setFocusPainted(false);
        restartButton.setContentAreaFilled(true);
        restartButton.setOpaque(true);
        restartButton.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        restartButton.setEnabled(true);

        backButton.setPreferredSize(new Dimension(140, 30));
        backButton.setBackground(new Color(224, 242, 255));
        backButton.setForeground(Color.BLACK);
        backButton.setFont(new Font("SansSerif", Font.BOLD, 13));
        backButton.setFocusPainted(false);
        backButton.setContentAreaFilled(true);
        backButton.setOpaque(true);
        backButton.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        backButton.setEnabled(true);

        // Aksi saat tombol Restart diklik
        restartButton.addActionListener(e -> {
            dispose(); // Tutup dialog
            if (parentFrame instanceof GameFrame) {
                ((GameFrame) parentFrame).resetGame(); // Reset permainan
                parentFrame.setVisible(true);          // Tampilkan ulang game
            }
        });

        // Aksi saat tombol Back diklik
        backButton.addActionListener(e -> {
            dispose();                     // Tutup dialog
            if (parentFrame != null) parentFrame.dispose(); // Tutup jendela game

            // Method ini sekarang akan refresh leaderboard DAN mulai ulang musik
            MainMenu mainMenu = MainMenu.getInstance();
            mainMenu.refreshLeaderboard(); // Refresh leaderboard + restart musik
            mainMenu.setVisible(true);     // Tampilkan MainMenu
        });

        buttonPanel.add(restartButton);
        buttonPanel.add(backButton);
        add(buttonPanel, BorderLayout.SOUTH); // Tambahkan panel tombol ke bawah

        getContentPane().setBackground(Color.WHITE); // Warna latar dialog
    }

    // Membuat label teks untuk informasi pengguna
    private JLabel createInfoLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.PLAIN, 14));
        label.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
        return label;
    }

    /// Buat custom title bar khusus (judul dan tombol X)
    private JPanel createCustomTitleBar() {
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setBackground(new Color(50, 50, 50));

        JLabel titleLabel = new JLabel("  GAME OVER");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 14));

        JButton closeButton = new JButton("X");
        closeButton.setForeground(Color.WHITE);
        closeButton.setBackground(new Color(50, 50, 50));
        closeButton.setBorderPainted(false);
        closeButton.setFocusPainted(false);
        closeButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        closeButton.setPreferredSize(new Dimension(45, 25));
        closeButton.addActionListener(e -> dispose()); // Tutup dialog jika diklik

        titleBar.add(titleLabel, BorderLayout.WEST);
        titleBar.add(closeButton, BorderLayout.EAST);
        return titleBar;
    }
}