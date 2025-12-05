package org.example.algo_proje.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.algo_proje.Services.UserService;


public class RegisterController {
    @FXML
    private TextField txtFullName;
    @FXML private TextField txtUsername;
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;

    private MainController mainController;

    public void setMainController(MainController m) {
        this.mainController = m;
    }

    public void registerClick() {
        boolean ok = UserService.register(
                txtFullName.getText(),
                txtUsername.getText(),
                txtEmail.getText(),
                txtPassword.getText()
        );

        if (ok)
            System.out.println("Kayıt başarılı!");
        else
            System.out.println("Kayıt başarısız!");
    }
    @FXML
    private void openLogin() {
        if (mainController != null) mainController.showLogin();
    }
}
