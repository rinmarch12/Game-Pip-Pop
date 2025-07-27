import view.MainMenu;
import model.DatabaseManager;

// Titik awal program
import javax.swing.*;
/*
    Saya Ririn Marchelina dengan NIM 2303662 mengerjakan evaluasi Tugas Masa Depan
    dalam mata kuliah Desain dan Pemrograman Berorientasi Objek untuk keberkahan-Nya. 
    Maka saya tidak melakukan kecurangan seperti yang telah dispesifikasikan. Aamiin.
*/

/* Lisensi Suara
   backsound game = https://www.chosic.com/download-audio/25863/ (Carefree-Kevin MacLeod)
   collect ice cream = https://mixkit.co/free-sound-effects/ (mixkit-achievement-completed-2068)
   gameover =  https://mixkit.co/free-sound-effects (mixkit-achievement-completed-2068)
*/

/* Lisensi Gambar
   ice cream vanila = https://cdn.pixabay.com/photo/2012/04/13/12/00/ice-cream-cone-32094_1280.png (Clker-Free-vector-Images)
   ice cream strowbery = https://cdn.pixabay.com/photo/2019/09/02/06/29/ice-cream-4446627_1280.png (Snoy_My)
   ice cream coklat = https://cdn.pixabay.com/photo/2013/07/13/12/30/ice-159744_1280.png (OpenClipart-Vectors)
   ice cream pelangi = https://cdn.pixabay.com/photo/2017/07/18/21/09/ice-cream-2517064_1280.png (julieta_masc)
   pinguin = https://cdn.pixabay.com/photo/2017/03/21/21/23/christmas-2163381_1280.png
   freezer = https://cdn.pixabay.com/photo/2012/04/14/14/14/ice-34075_1280.png (Clker-Free-vector-Images)
   timer = https://cdn.pixabay.com/photo/2022/08/03/16/49/alarm-clock-7363031_1280.png (ArtDream)
   background : https://cdn.pixabay.com/photo/2023/11/30/17/34/background-8422123_1280.png (aalmeidah)
*/

public class Main {
    public static void main(String[] args) {
        // Mengatur tampilan agar konsisten di semua sistem operasi
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Inisialisasi koneksi database (test koneksi)
        DatabaseManager.initialize();

        // Jalankan menu utama dalam thread GUI
        SwingUtilities.invokeLater(() -> {
            MainMenu.getInstance().setVisible(true);
        });
    }
}
