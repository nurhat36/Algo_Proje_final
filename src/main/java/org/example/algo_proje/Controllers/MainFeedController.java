package org.example.algo_proje.Controllers;



import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import org.example.algo_proje.Models.Users;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class MainFeedController {

    // FXML Elemanları (Sidebar)
    @FXML
    private ImageView userAvatar;
    @FXML
    private Label lblUserName;

    // FXML Elemanları (Feed)
    @FXML
    private VBox feedContainer;

    // Uygulama genelinde kullanılacak kullanıcı nesnesi
    private Users loggedUser;

    /**
     * Controller yüklendiğinde otomatik olarak çağrılır.
     */
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Genellikle burada FXML elemanlarına başlangıç değerleri atanır.
        // Ancak bu Controller'da, veriler LoginController'dan geleceği için
        // asıl yükleme işlemi 'initData' metodunda yapılacaktır.
    }

    /**
     * LoginController'dan kullanıcı verisi almak için özel metot.
     * Ekran yüklendikten hemen sonra çağrılır.
     * @param user Giriş yapan kullanıcı nesnesi.
     */
    public void initData(Users user) {
        this.loggedUser = user;

        if (loggedUser != null) {
            // Kullanıcı bilgilerini Sidebar'a yükle
            loadSidebarData();

            // Paylaşımları yükle (Şimdilik örnek veriler)
            loadFeedPosts();
        } else {
            System.err.println("HATA: Oturum açmış kullanıcı bilgisi yok!");
            // Hata durumunda giriş ekranına yönlendirme yapılabilir.
        }
    }

    /**
     * Oturum açmış kullanıcının adını ve avatarını Sidebar'a yükler.
     */
    private void loadSidebarData() {
        // Kullanıcı Adı/Tam Adı yükle
        lblUserName.setText(loggedUser.getFullName() != null ? loggedUser.getFullName() : loggedUser.getUsername());

        // Profil Fotoğrafını yükle
        loadProfileImage(loggedUser.getProfilePhoto());
    }

    /**
     * Statik klasördeki fotoğrafı ImageView'a yükler.
     * Bu metot, LoginController'dan gelen aynı mantığı kullanır.
     */
    private void loadProfileImage(String uniqueFileName) {
        if (uniqueFileName == null || uniqueFileName.isEmpty()) {
            // Varsayılan fotoğrafı yükle
            // userAvatar.setImage(new Image("/static/profile_pics/default.png"));
            return;
        }

        // JavaFX kaynak yolu: /static/profile_pics/a1b2c3d4.jpg
        String resourcePath = "/static/Images/profile_pics/" + uniqueFileName;

        try {
            Image image = new Image(getClass().getResourceAsStream(resourcePath));

            if (image.isError() || image.getWidth() <= 0) {
                System.err.println("HATA: Kaynak dosya bulunamadı veya yüklenemedi: " + resourcePath);
                // Varsayılan resme düşme (fallback)
                // userAvatar.setImage(new Image("/static/profile_pics/default.png"));
            } else {
                userAvatar.setImage(image);
            }
        } catch (Exception e) {
            System.err.println("Resim yükleme hatası: " + e.getMessage());
            // Hata durumunda varsayılan resme düşme
        }
    }

    /**
     * Örnek paylaşımları VBox'a (FeedContainer) ekler.
     * Gerçek uygulamada veritabanından çekilmelidir.
     */
    private void loadFeedPosts() {
        // Mevcut örnek Label'ları temizleyelim
        feedContainer.getChildren().clear();

        // Gerçek bir uygulamada burada döngü ile veritabanından çekilen Post/Share listesi işlenir.
        for (int i = 1; i <= 5; i++) {
            // Basit bir Post Kartı oluşturma simülasyonu
            Label post = new Label("Post #" + i + ": " + loggedUser.getUsername() + " tarafından paylaşılan örnek içerik.");
            post.setStyle("-fx-background-color: white; -fx-padding: 20px; -fx-background-radius: 10px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 2);");
            post.setPrefWidth(Double.MAX_VALUE); // Genişliği doldur

            feedContainer.getChildren().add(post);
        }
    }

    // --- Diğer Aksiyon Metotları ---

    @FXML
    private void handleLogout() {
        // Çıkış yapma mantığı
        System.out.println("Kullanıcı çıkış yaptı.");
        // Gerekirse giriş ekranına geri yönlendirme
    }
}