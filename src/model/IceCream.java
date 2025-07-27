package model;

// Objek Ice Cream
public class IceCream {
    /// Jenis es krim beserta poin dan file gambarnya
    public enum Type {
        VANILLA(10, "ice_vanila.png"),
        STRAWBERRY(20, "ice_strowbery.png"),
        CHOCOLATE(30, "ice_coklat.png"),
        RAINBOW(50, "ice_pelangi.png");

        private final int points;
        private final String filename;

        Type(int points, String filename) {
            this.points = points;
            this.filename = filename;
        }

        public int getPoints() {
            return points;
        }

        public String getFilename() {
            return filename;
        }
    }

    // Posisi dan arah gerak es krim
    private int x, y;
    private Type type;
    private boolean movingRight;

    // Konstanta ukuran dan kecepatan
    private static final int SPEED = 4;
    private static final int WIDTH = 50;
    private static final int HEIGHT = 70;

    // Status saat sedang dikoleksi
    private boolean isBeingCollected = false;
    private int targetX, targetY;
    private static final int COLLECT_SPEED = 8;
    private boolean isMovingToFreezerPhase = true;

    // Konstruktor: set posisi, jenis, dan arah
    public IceCream(int x, int y, Type type, boolean movingRight) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.movingRight = movingRight;
    }

    /// Mulai proses pemasukkan ice cream ke freezer
    public void startCollecting(int freezerX, int freezerY) {
        this.isBeingCollected = true;
        this.targetX = freezerX;
        this.targetY = freezerY - 40; // naik ke atas freezer dulu
        this.isMovingToFreezerPhase = true;
    }

    public boolean isBeingCollected() {
        return isBeingCollected;
    }

    /// Update posisi es krim (gerak kiri-kanan atau ke freezer)
    public void update() {
        if (isBeingCollected) {
            int dx = targetX - x;
            int dy = targetY - y;
            double distance = Math.sqrt(dx * dx + dy * dy);

            if (distance <= COLLECT_SPEED) {
                // Sampai tujuan pertama
                x = targetX;
                y = targetY;

                if (isMovingToFreezerPhase) {
                    // Lanjut ke fase masuk freezer (turun)
                    targetY += 40;
                    isMovingToFreezerPhase = false;
                }
                // Setelah fase kedua, objek akan dihapus di GameViewModel
            } else {
                // Gerak mendekati target
                x += (int)(COLLECT_SPEED * dx / distance);
                y += (int)(COLLECT_SPEED * dy / distance);
            }
        } else {
            // Gerak ke kanan atau kiri di layar
            x += movingRight ? SPEED : -SPEED;
        }
    }

    /// Cek jika es krim sudah keluar dari layar
    public boolean isOffScreen(int screenWidth) {
        return x < -WIDTH || x > screenWidth;
    }

    /// Cek apakah posisi mouse ada di dalam area es krim
    public boolean contains(int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + WIDTH &&
                mouseY >= y && mouseY <= y + HEIGHT;
    }

    // Getter posisi dan jenis
    public int getX() { return x; }
    public int getY() { return y; }
    public Type getType() { return type; }
    public int getPoints() { return type.getPoints(); }

    // Getter ukuran
    public static int getWidth() { return WIDTH; }
    public static int getHeight() { return HEIGHT; }
}
