package org.example.algo_proje.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.control.*;

import org.example.algo_proje.Models.Users;
import org.example.algo_proje.Services.UserService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
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
    @FXML private ImageView profileImageView;

    // Controller alanları (Controller sınıfının en üstüne ekleyin)
    private String selectedPhotoPath; // Veritabanına kaydedilecek benzersiz dosya adı
    private File selectedFile;       // Geçici olarak seçilen dosya

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
            selectedPhotoPath = loggedUser.getProfilePhoto();
            loadProfileImage(selectedPhotoPath);
        }
    }
    private void loadProfileImage(String uniqueFileName) {
        if (uniqueFileName == null || uniqueFileName.isEmpty()) {
            // Varsayılan fotoğrafı yükle
            profileImageView.setImage(null); // Veya varsayılan bir Image nesnesi
            return;
        }

        // JavaFX'te resources klasöründeki yolu yüklemek için ClassLoader kullanılır.
        // Yolu şu formata çevirmeliyiz: "/static/profile_pics/a1b2c3d4.jpg"
        String resourcePath = "/static/Images/profile_pics/" + uniqueFileName;

        // Resim nesnesini oluştur
        Image image = new Image(getClass().getResourceAsStream(resourcePath));

        if (image.isError()) {
            System.err.println("HATA: Kaynak dosya bulunamadı veya yüklenemedi: " + resourcePath);
            // Varsayılan fotoğrafı yükle
            profileImageView.setImage(null);
        } else {
            // ImageView'a yükle
            profileImageView.setImage(image);
        }
    }

    /**
     * Fotoğraf seçme işlemi: Kullanıcıdan dosyayı alır ve ekranda önizler.
     */
    private void selectImage() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Profil Fotoğrafı Seç");

        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Resim Dosyaları", "*.png", "*.jpg", "*.jpeg")
        );

        // Pencereyi aç ve dosya seçimi bekle
        selectedFile = chooser.showOpenDialog(btnSelectImage.getScene().getWindow());

        if (selectedFile != null) {
            System.out.println("Fotoğraf seçildi: " + selectedFile.getName());

            // Seçilen resmi ImageView'da önizle (ÖNEMLİ: Önizleme için dosya yolunu kullanır)
            try {
                Image image = new Image(selectedFile.toURI().toString());
                profileImageView.setImage(image);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    /**
     * Seçilen resmi benzersiz bir isimle projenin statik klasörüne kaydeder
     * ve veritabanına kaydedilecek dosya adını döndürür.
     */
    private String saveImageToStaticFolder() {
        if (selectedFile == null) {
            // Dosya seçilmemişse null veya varsayılan bir değer döndür
            return null;
        }

        // 1. Dosya Uzantısını Al
        String fileName = selectedFile.getName();
        String extension = "";
        int lastIndexOfDot = fileName.lastIndexOf('.');
        if (lastIndexOfDot > 0) {
            extension = fileName.substring(lastIndexOfDot);
        }

        // 2. Benzersiz Dosya Adı Oluştur (Örn: UUID kullanarak)
        String uniqueID = java.util.UUID.randomUUID().toString();
        String uniqueFileName = uniqueID + extension;

        // 3. Hedef Klasörü Tanımla (Proje yapısına göre bu yolu ayarlamanız gerekebilir)
        // Örnek: Projenin 'resources' klasöründeki 'static/profile_pics' klasörü
        String staticFolderPath = "src/main/resource/static/Images/profile_pics/";
        File targetDir = new File(staticFolderPath);

        if (!targetDir.exists()) {
            targetDir.mkdirs(); // Klasör yoksa oluştur
        }

        // 4. Dosyayı Hedefe Kopyala
        File targetFile = new File(targetDir, uniqueFileName);

        try {
            Files.copy(selectedFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Resim başarıyla kaydedildi: " + targetFile.getAbsolutePath());

            // Veritabanına kaydedilecek benzersiz adı döndür
            return uniqueFileName;

        } catch (IOException e) {
            System.err.println("Resim kaydetme hatası: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Profili veritabanına kaydetme
     */
    private void saveProfile() {
        // 1. Resmi Kaydet ve Benzersiz Yolu Al
        selectedPhotoPath = saveImageToStaticFolder();

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


        if (selectedPhotoPath != null) {
            loggedUser.setProfilePhoto(selectedPhotoPath);
            // Kayıt başarılı: profile.setPhotoPath(selectedPhotoPath);
            System.out.println("Veritabanına kaydedilen fotoğraf yolu: " + selectedPhotoPath);
        } else {
            // Kullanıcı fotoğraf seçmediyse veya kaydetme hatası oluştuysa
            System.out.println("Fotoğraf seçilmedi veya kaydetme başarısız. Diğer bilgileri kaydet.");
        }

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
