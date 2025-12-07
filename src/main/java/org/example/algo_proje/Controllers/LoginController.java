package org.example.algo_proje.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.algo_proje.Models.Users;
import org.example.algo_proje.Services.UserService;

import java.io.IOException;

public class LoginController {

    @FXML private TextField txtUser;
    @FXML private PasswordField txtPassword;


    private MainController mainController;
    public void setMainController(MainController m) {
        this.mainController = m;
    }

    @FXML
    public void loginClick(javafx.event.ActionEvent event) {
        // 1. Giriş Kontrolü
        boolean ok = UserService.login(
                txtUser.getText(),
                txtPassword.getText()
        );

        if (!ok) {
            System.out.println("Hatalı kullanıcı adı/şifre!");
            // Kullanıcıya görsel bir hata mesajı göster (örn: Label veya Alert)
            showAlert(Alert.AlertType.ERROR, "Giriş Hatası", "Kullanıcı adı veya şifre yanlış.");
            return;
        }

        System.out.println("Giriş başarılı!");

        // 2. Kullanıcı Bilgilerini Çek
        Users loggedUser = UserService.getUserByUsername(txtUser.getText());

        try {
            // 3. Profil Durumu Kontrolü (Yeni Mantık)
            if (UserService.isProfileComplete(loggedUser)) {
                // Profil TAMAMLANMIŞSA: Ana Sayfaya (Paylaşımların olduğu sayfa) yönlendir
                System.out.println("Profil tamamlanmış. Ana sayfaya yönlendiriliyor.");
                loadMainPage(event, loggedUser);
            } else {
                // Profil TAMAMLANMAMIŞSA: CompleteProfile sayfasına yönlendir
                System.out.println("Profil eksik. Tamamlama sayfasına yönlendiriliyor.");
                loadCompleteProfilePage(event, loggedUser);
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Hata", "Ekran yüklenirken bir sorun oluştu!");
        }
    }
    private void loadCompleteProfilePage(javafx.event.ActionEvent event, Users loggedUser) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/algo_proje/Views/CompleteProfile.fxml"));
        Parent root = loader.load();

        ProfileController controller = loader.getController();
        controller.setUser(loggedUser); // Kullanıcı objesini ProfileController'a gönder

        Scene scene = ((Node) event.getSource()).getScene();
        Stage stage = (Stage) scene.getWindow();

        scene.setRoot(root);
        stage.setTitle("Profil Tamamlama");
        stage.setWidth(600);
        stage.setHeight(700);
    }
    // LoginController.java içindeki loadMainPage metodu (Güncellenmiş Hali)

    private void loadMainPage(javafx.event.ActionEvent event, Users loggedUser) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/algo_proje/Views/MainFeedView.fxml"));
        Parent root = loader.load();

        // Controller'ı al
        MainFeedController controller = loader.getController();

        // Kullanıcı verisini gönder
        controller.initData(loggedUser);

        Scene scene = ((Node) event.getSource()).getScene();
        Stage stage = (Stage) scene.getWindow();

        scene.setRoot(root);
        stage.setTitle("Ana Sayfa - Paylaşımlar");
        stage.setWidth(1000);
        stage.setHeight(800);
    }

    @FXML
    private void openRegister() {
        if (mainController != null)
            mainController.showRegister();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }


}