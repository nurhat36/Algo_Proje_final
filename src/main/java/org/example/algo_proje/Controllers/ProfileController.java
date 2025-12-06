package org.example.algo_proje.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.control.*;

import org.example.algo_proje.Models.Users;
import org.example.algo_proje.Services.UserService;

import java.io.File;
import java.nio.file.Files;
import java.sql.Date;

public class ProfileController {

    @FXML private Button btnSelectImage;
    @FXML private Button btnSave;

    @FXML private TextArea txtBio;
    @FXML private TextField txtWebsite;
    @FXML private TextField txtPhone;
    @FXML private DatePicker birthDatePicker;
    @FXML private ComboBox<String> genderBox;
    @FXML private TextField txtCountry;
    @FXML private TextField txtCity;

    private byte[] selectedProfilePhoto;

    // Giriş yapmış kullanıcı
    private Users loggedUser;

    // Service instance
    private final UserService userService = new UserService();

    @FXML
    public void initialize() {
        genderBox.getItems().addAll("Erkek", "Kadın", "Diğer");

        btnSelectImage.setOnAction(e -> selectImage());
        btnSave.setOnAction(e -> saveProfile());
    }

    /**
     * Login ekranından veya MainController'dan kullanıcı atanacak
     */
    public void setUser(Users user) {
        this.loggedUser = user;

        // Kullanıcı verilerini ekrana doldur
        loadUserData();
    }

    private void loadUserData() {
        if (loggedUser == null) return;

        txtBio.setText(loggedUser.getBio());
        txtWebsite.setText(loggedUser.getWebsite());
        txtPhone.setText(loggedUser.getPhoneNumber());
        txtCountry.setText(loggedUser.getCountry());
        txtCity.setText(loggedUser.getCity());

        if (loggedUser.getBirthDate() != null) {
            birthDatePicker.setValue(loggedUser.getBirthDate().toLocalDate());
        }

        if (loggedUser.getGender() != null) {
            genderBox.setValue(loggedUser.getGender());
        }

        if (loggedUser.getProfilePhoto() != null) {
            selectedProfilePhoto = loggedUser.getProfilePhoto();
        }
    }

    /**
     * Fotoğraf seçme işlemi
     */
    private void selectImage() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Profil Fotoğrafı Seç");

        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Resim Dosyaları", "*.png", "*.jpg", "*.jpeg")
        );

        File file = chooser.showOpenDialog(btnSelectImage.getScene().getWindow());

        if (file != null) {
            try {
                selectedProfilePhoto = Files.readAllBytes(file.toPath());
                System.out.println("Fotoğraf seçildi: " + file.getName());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Profili veritabanına kaydetme
     */
    private void saveProfile() {

        if (loggedUser == null) {
            System.out.println("HATA: loggedUser = null!");
            return;
        }

        loggedUser.setBio(txtBio.getText());
        loggedUser.setWebsite(txtWebsite.getText());
        loggedUser.setPhoneNumber(txtPhone.getText());
        loggedUser.setCity(txtCity.getText());
        loggedUser.setCountry(txtCountry.getText());

        if (birthDatePicker.getValue() != null) {
            loggedUser.setBirthDate(Date.valueOf(birthDatePicker.getValue()));
        }

        if (genderBox.getValue() != null) {
            loggedUser.setGender(genderBox.getValue());
        }

        loggedUser.setProfilePhoto(selectedProfilePhoto);

        // PROFİL GÜNCELLE
        boolean ok = userService.updateUserProfile(loggedUser);

        if (ok) {
            System.out.println("Profil başarıyla güncellendi.");

            try {
                Parent root = FXMLLoader.load(getClass().getResource("/org/example/algo_proje/Views/main.fxml"));
                Stage stage = (Stage) btnSave.getScene().getWindow();
                stage.setScene(new Scene(root));
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            System.out.println("Profil güncellenirken hata oluştu.");
        }
    }
}
