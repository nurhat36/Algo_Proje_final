package org.example.algo_proje.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.example.algo_proje.Attribute.PhotoAttribute;
import org.example.algo_proje.Models.Users;

import java.io.IOException;

public class MainFeedController {

    // SIDEBAR FXML ELEMENTLERİ
    @FXML public ImageView userAvatar;
    @FXML public Label lblUserName;
    @FXML public Button btnHome;
    @FXML public Button btnExplore;
    @FXML public Button btnNotifications;
    @FXML public Button btnSettings;
    @FXML public Button btnLogout;
    @FXML public Button btnRelationships;
    @FXML public Button btnTxtOperations; // FXML'deki fx:id ile aynı olmalı

    // DYNAMIC CONTENT AREA
    @FXML public VBox centerContentArea; // Orta alanın VBox'ı

    private Users loggedUser;

    @FXML
    public void initialize() {
        // Butonlara aksiyonları bağlama
        btnHome.setOnAction(e -> loadCenterContent("/org/example/algo_proje/Views/FeedContent.fxml"));

        // TxtOperations (Algoritma Proje) Ekranı
        btnTxtOperations.setOnAction(e -> loadCenterContent("/org/example/algo_proje/Views/TxtOperations.fxml"));

        // Keşfet butonu
        btnExplore.setOnAction(e -> loadCenterContent("/org/example/algo_proje/Views/ExploreContent.fxml"));

        // Bildirimler butonu
        btnNotifications.setOnAction(e -> loadCenterContent("/org/example/algo_proje/Views/NotificationsContent.fxml"));

        // Arkadaş/İlişki butonu
        btnRelationships.setOnAction(e -> loadCenterContent("/org/example/algo_proje/Views/friends.fxml"));

        // Çıkış yap butonu
        btnLogout.setOnAction(e -> {
            showAlert("Oturum Kapatıldı.");
            // Burada sahne kapatma veya login ekranına dönüş kodları olabilir
        });
    }

    // LoginController'dan çağrılacak veri aktarım metodu
    public void initData(Users user) {
        this.loggedUser = user;
        if (loggedUser != null) {
            loadSidebarData();
            // Uygulama açılışında varsayılan olarak Anasayfa içeriğini yükle
            loadCenterContent("/org/example/algo_proje/Views/FeedContent.fxml");
        }
    }

    private void loadSidebarData() {
        // Kullanıcı Adı/Tam Adı yükle
        lblUserName.setText(
                loggedUser.getFullName() != null && !loggedUser.getFullName().isEmpty()
                        ? loggedUser.getFullName()
                        : loggedUser.getUsername()
        );

        // Profil Fotoğrafını yükle
        String photoFileName = loggedUser.getProfilePhoto();
        if (photoFileName != null && !photoFileName.isEmpty()) {
            loadProfileImage(photoFileName);
        }
    }

    private void loadProfileImage(String uniqueFileName) {
        if (uniqueFileName == null || uniqueFileName.isEmpty()) {
            userAvatar.setImage(null);
            return;
        }

        Image image = PhotoAttribute.loadImageFromResources(
                uniqueFileName,
                "/static/Images/profile_pics/",
                getClass()
        );

        if (image != null) {
            userAvatar.setImage(image);
        } else {
            System.err.println("Avatar yüklenemedi: " + uniqueFileName);
            userAvatar.setImage(null);
        }
    }

    /**
     * Orta alanı temizler ve belirtilen FXML içeriğini yükler.
     * @param fxmlPath Yüklenecek FXML dosyasının kaynak yolu.
     */
    private void loadCenterContent(String fxmlPath) {
        centerContentArea.getChildren().clear(); // Önceki içeriği temizle

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent content = loader.load();

            // Yeni içeriği VBox'a ekle ve genişlemesini sağla
            centerContentArea.getChildren().add(content);
            VBox.setVgrow(content, Priority.ALWAYS);

            // Controller'a eriş ve kullanıcı bilgisini aktar
            Object controller = loader.getController();

            // 1. Feed Content
            if (controller instanceof FeedContentController) {
                ((FeedContentController) controller).setLoggedUser(loggedUser);
            }
            // 2. Explore Content
            else if (controller instanceof ExploreContentController) {
                ((ExploreContentController) controller).setLoggedUser(loggedUser);
            }
            // 3. Friends Content
            else if (controller instanceof FriendsController) {
                ((FriendsController) controller).initData(loggedUser);
            }
            // 4. Notifications Content
            else if (controller instanceof NotificationsContentController) {
                ((NotificationsContentController) controller).setLoggedUser(loggedUser);
            }
            // 5. TxtOperations Content (DÜZELTİLEN KISIM BURASI)
            else if (controller instanceof TxtOperationsController) {
                // TxtOperationsController içinde setLoggedUser metodunu oluşturduğumuz için
                // artık bu satır hata vermeyecektir.
                ((TxtOperationsController) controller).setLoggedUser(loggedUser);
            }

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("İçerik yüklenirken hata oluştu: " + fxmlPath + "\nLütfen dosya yolunu kontrol edin.");
        }
    }

    private void showAlert(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Bilgi");
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}