package org.example.algo_proje.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

public class MainController {
    @FXML
    private StackPane rootStack;

    private Node loginNode;
    private Node registerNode;

    private LoginController loginController;
    private RegisterController registerController;

    @FXML
    public void initialize() {
        try {
            // Login yükle
            FXMLLoader loginLoader = new FXMLLoader(getClass().getResource("/org/example/algo_proje/Views/login.fxml"));
            loginNode = loginLoader.load();
            loginController = loginLoader.getController();
            loginController.setMainController(this); // child -> main referansı

            // Register yükle
            FXMLLoader regLoader = new FXMLLoader(getClass().getResource("/org/example/algo_proje/Views/register.fxml"));
            registerNode = regLoader.load();
            registerController = regLoader.getController();
            registerController.setMainController(this);

            // Başta sadece login göster
            rootStack.getChildren().addAll(registerNode, loginNode);
            registerNode.setVisible(false);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // Login görünür yap
    public void showLogin() {
        registerNode.setVisible(false);
        loginNode.setVisible(true);
    }

    // Register görünür yap
    public void showRegister() {
        loginNode.setVisible(false);
        registerNode.setVisible(true);
    }
}
