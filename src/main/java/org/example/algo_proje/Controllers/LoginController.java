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

public class LoginController {

    @FXML private TextField txtUser;
    @FXML private PasswordField txtPassword;


    private MainController mainController;
    public void setMainController(MainController m) {
        this.mainController = m;
    }

    @FXML
    public void loginClick(javafx.event.ActionEvent event) {

        boolean ok = UserService.login(
                txtUser.getText(),
                txtPassword.getText()
        );

        if (!ok) {
            System.out.println("Hatalı kullanıcı adı/şifre!");
            return;
        }

        System.out.println("Giriş başarılı!");

        // kullanı bilgilerini çek
        Users loggedUser = UserService.getUserByUsername(txtUser.getText());

        try {
            FXMLLoader loader = new FXMLLoader(LoginController.class.getResource("/org/example/algo_proje/Views/CompleteProfile.fxml"));

            Parent root = loader.load();

            // ProfileController’a kullanıcıyı gönder
            ProfileController controller = loader.getController();
            controller.setUser(loggedUser);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Hata", "Profil ekranı yüklenemedi!");
        }
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