package org.example.algo_proje.Controllers;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.example.algo_proje.Models.Users;
import org.example.algo_proje.Services.RelationService;
import org.example.algo_proje.Services.UserService;

import java.util.List;

public class ExploreContentController {

    @FXML public VBox exploreUserContainer;
    private Users loggedUser;
    private final UserService userService = new UserService();
    private final RelationService relationService = new RelationService();

    // Sadece FXML yüklendiğinde çalışır, initData'dan sonra yükleme başlatılır.
    @FXML
    public void initialize() {
        // ... (Eğer burada FXML'e ait statik bağlamalar varsa kalır)
    }

    public void setLoggedUser(Users user) {
        this.loggedUser = user;
        if (loggedUser != null) {
            loadDiscoverableUsers(); // Sayfa açıldığında kullanıcıları yükle
        }
    }

    /**
     * Keşfedilebilir kullanıcıları yükler ve ilişki durumlarına göre kartları hazırlar.
     */
    private void loadDiscoverableUsers() {
        exploreUserContainer.getChildren().clear();

        // 1. Giriş yapan kullanıcı hariç tüm kullanıcıları çek
        List<Users> usersToDiscover = UserService.getAllUsersExcept(loggedUser.getUserId());

        for (Users targetUser : usersToDiscover) {

            // 2. Kullanıcı ile Hedef arasındaki ilişki durumunu iki yönde de kontrol et
            int status = relationService.checkRelationStatus(loggedUser.getUserId(), targetUser.getUserId());
            int inverseStatus = relationService.checkRelationStatus(targetUser.getUserId(), loggedUser.getUserId());

            // 3. İlişki kartını oluştur ve butonu durumuna göre yapılandır
            HBox userCard = buildUserCard(targetUser, status, inverseStatus);
            if (userCard != null) {
                exploreUserContainer.getChildren().add(userCard);
            }
        }
    }

    /**
     * Kullanıcı kartını oluşturur ve butonun metnini/aksiyonunu ilişki durumuna göre ayarlar.
     * @param targetUser Kartı oluşturulacak hedef kullanıcı.
     * @param status loggedUser'dan targetUser'a doğru olan ilişki durumu.
     * @param inverseStatus targetUser'dan loggedUser'a doğru olan ilişki durumu.
     */
    private HBox buildUserCard(Users targetUser, int status, int inverseStatus) {
        // Zaten arkadaş olanları veya engellenmiş olanları filtrele (Eğer checkRelationStatus tüm bu durumları yönetmiyorsa)
        if (status == RelationService.STATUS_APPROVED || inverseStatus == RelationService.STATUS_APPROVED) {
            // Zaten arkadaşsa gösterme
            return null;
        }

        HBox card = new HBox(10);
        card.setPadding(new Insets(10));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: #f7f7f7; -fx-background-radius: 5;");

        // --- Kartın Sol Tarafı (Avatar ve İsim) ---
        ImageView avatar = new ImageView();
        avatar.setFitWidth(40);
        avatar.setFitHeight(40);
        // TODO: Avatar yükleme mantığı buraya gelir
        // Örn: avatar.setImage(ImageManager.loadAvatar(targetUser.getProfilePhoto()));

        VBox info = new VBox(2);
        Label name = new Label(targetUser.getFullName());
        name.setStyle("-fx-font-weight: bold;");
        Label username = new Label("@" + targetUser.getUsername());
        info.getChildren().addAll(name, username);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // --- Kartın Sağ Tarafı (Buton Mantığı) ---
        Button actionButton = new Button();
        actionButton.setStyle("-fx-background-radius: 5;");

        // **Durumları Kontrol Etme ve Buton Ayarı**

        if (status == RelationService.STATUS_PENDING) { // loggedUser, targetUser'a istek göndermiş
            actionButton.setText("İstek Gönderildi");
            actionButton.setDisable(true);
            actionButton.setStyle("-fx-background-color: #ccc; -fx-text-fill: #333;");

        } else if (inverseStatus == RelationService.STATUS_PENDING) { // targetUser, loggedUser'a istek göndermiş
            actionButton.setText("Onayla");
            actionButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");

            actionButton.setOnAction(e -> {
                // targetUser'ın gönderdiği isteği onaylıyoruz
                boolean success = relationService.acceptFriendRequest(loggedUser.getUserId(), targetUser.getUserId());
                if (success) {
                    actionButton.setText("Arkadaşınız");
                    actionButton.setDisable(true);
                    showAlert("Başarılı", targetUser.getFullName()+" artık arkadaşınız.");
                            // İsteği onayladıktan sonra listeyi yenilemek mantıklı olabilir.
                            loadDiscoverableUsers();
                } else {
                    showAlert("Hata", "İstek onaylanırken bir sorun oluştu.");
                }
            });

        } else { // Hiçbir ilişki yok (STATUS_NOT_FRIENDS)
            actionButton.setText("Arkadaş Ekle");
            actionButton.setStyle("-fx-background-color: #007bff; -fx-text-fill: white;");

            actionButton.setOnAction(e -> {
                boolean success = relationService.sendFriendRequest(loggedUser.getUserId(), targetUser.getUserId());
                if (success) {
                    actionButton.setText("İstek Gönderildi");
                    actionButton.setDisable(true);
                    actionButton.setStyle("-fx-background-color: #ccc; -fx-text-fill: #333;");
                    showAlert("Başarılı", targetUser.getFullName()+" kişisine arkadaşlık isteği gönderildi.");
                } else {
                    showAlert("Hata", "İstek gönderilirken bir sorun oluştu.");
                }
            });
        }

        card.getChildren().addAll(avatar, info, spacer, actionButton);
        return card;
    }

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}