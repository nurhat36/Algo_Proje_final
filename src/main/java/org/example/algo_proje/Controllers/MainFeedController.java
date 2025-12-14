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

    // SIDEBAR FXML
    @FXML public ImageView userAvatar;
    @FXML public Label lblUserName;
    @FXML public Button btnHome;
    @FXML public Button btnExplore;
    @FXML public Button btnNotifications;
    @FXML public Button btnSettings;
    @FXML public Button btnLogout;

    // DYNAMIC CONTENT AREA
    @FXML public VBox centerContentArea; // Orta alanın VBox'ı

    private Users loggedUser;

    @FXML
    public void initialize() {
        // Butonlara aksiyonları bağlama
        btnHome.setOnAction(e -> loadCenterContent("/org/example/algo_proje/Views/FeedContent.fxml"));

        // Örnek: Keşfet butonu için (Ayrı bir FXML ve Controller olmalı)
        btnExplore.setOnAction(e -> {
            // Örnek: Keşfet FXML yolu
            loadCenterContent("/org/example/algo_proje/Views/ExploreContent.fxml");

        });

        // Çıkış yap butonu için örnek bir aksiyon:
        btnLogout.setOnAction(e -> {
            // Çıkış yapma veya login ekranına dönme mantığı buraya gelir
            showAlert("Oturum Kapatıldı.");
            // Stage'i kapatma, vb.
        });
    }

    // LoginController'dan çağrılacak
    public void initData(Users user) {
        this.loggedUser = user;
        if (loggedUser != null) {
            loadSidebarData();
            // Uygulama açılışında Anasayfa içeriğini yükle
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

        // Profil Fotoğrafını yükle (Mevcut loadProfileImage metodu bu kontrolcüde kalmalı)
        String photoFileName = loggedUser.getProfilePhoto();
        if (photoFileName != null && !photoFileName.isEmpty()) {
            loadProfileImage(photoFileName);
        }
    }

    // ... loadProfileImage metodu buraya taşınabilir veya ayrı bir Manager sınıfında kalabilir.
    private void loadProfileImage(String uniqueFileName) {
        // Fotoğraf yükleme mantığı
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
    // ...

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

            // Kontrolcü Tipine göre ayarları yap
            if (controller instanceof FeedContentController) {
                FeedContentController feedController = (FeedContentController) controller;
                // Kullanıcı verisini Feed Controller'a aktar
                feedController.setLoggedUser(loggedUser);
            }
            if (controller instanceof ExploreContentController) {
                ExploreContentController exploreController = (ExploreContentController) controller;

                // 3. Kullanıcı verisi aktarılır (İşte burası çağrı noktası!)
                exploreController.setLoggedUser(loggedUser);
            }
            // Keşfet için de aynı mantık uygulanabilir:
            // else if (controller instanceof ExploreContentController) {
            //     ((ExploreContentController) controller).setLoggedUser(loggedUser);
            // }

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("İçerik yüklenirken hata oluştu: " + fxmlPath + "\nLütfen dosya yolunu kontrol edin.");
        }
    }

    private void showAlert(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}