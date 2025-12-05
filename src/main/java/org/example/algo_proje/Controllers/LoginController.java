package org.example.algo_proje.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.algo_proje.Services.UserService;

public class LoginController {
    @FXML private TextField txtUser;
    @FXML
    private PasswordField txtPassword;

    public void loginClick() {
        boolean ok = UserService.login(
                txtUser.getText(),
                txtPassword.getText()
        );

        if (ok)
            System.out.println("Giriş başarılı!");
        else
            System.out.println("Hatalı kullanıcı adı/şifre!");
    }
    @FXML
    private void openRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/algo_proje/Views/Register.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Kayıt Ol");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
