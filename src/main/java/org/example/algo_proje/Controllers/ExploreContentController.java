package org.example.algo_proje.Controllers;

import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.example.algo_proje.Models.DTOs.PostDTO;
import org.example.algo_proje.Models.Users;
import org.example.algo_proje.Services.RelationService;
import org.example.algo_proje.Services.ShareService;
import org.example.algo_proje.Services.UserService;

import java.io.InputStream;
import java.nio.file.Paths;
import java.util.List;

public class ExploreContentController {

    @FXML private TextField txtSearch;
    @FXML private TilePane explorePostContainer;

    // ScrollPane artık FXML'de yok, Search Popup ile hallediyoruz.

    private Users loggedUser;

    // Arama Popup'ı için değişkenler
    private Popup searchPopup;
    private VBox popupContentBox;

    private final UserService userService = new UserService();
    private final RelationService relationService = new RelationService();
    private final ShareService shareService = new ShareService();

    @FXML
    public void initialize() {
        // 1. Arama Popup'ını hazırla
        setupSearchPopup();

        // 2. Arama kutusu dinleyicileri
        if (txtSearch != null) {
            // Yazı yazıldıkça ara
            txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
                handleSearch(newValue);
            });

            // Tıklayınca (doluysa) aç
            txtSearch.setOnMouseClicked(e -> {
                if (!txtSearch.getText().isEmpty()) {
                    handleSearch(txtSearch.getText());
                }
            });
        }
    }

    public void setLoggedUser(Users user) {
        this.loggedUser = user;
        if (loggedUser != null) {
            loadExplorePosts();
        }
    }

    /* =======================================================
       🛠️ YARDIMCI METOT: GÜVENLİ RESİM YÜKLEYİCİ
       (Türkçe karakter, boşluk ve path sorunlarını çözer)
       ======================================================= */
    private Image loadImageSafely(String dbPath) {
        if (dbPath == null || dbPath.trim().isEmpty()) return null;

        try {
            // Sadece dosya ismini al (C:/Users/... kısmını at)
            String fileName = Paths.get(dbPath).getFileName().toString();
            String resourcePath = "/Static/Images/profile_pics/Shares_Pics/" + fileName;

            // 1. YÖNTEM: Stream (En Garantisi - Türkçe karakter/boşluk dostu)
            InputStream stream = getClass().getResourceAsStream(resourcePath);
            if (stream != null) {
                return new Image(stream);
            }

            // 2. YÖNTEM: Boşluk Düzeltme (%20)
            String encodedName = fileName.replace(" ", "%20");
            InputStream retryStream = getClass().getResourceAsStream("/Static/Images/profile_pics/Shares_Pics/" + encodedName);
            if (retryStream != null) {
                return new Image(retryStream);
            }

            // 3. YÖNTEM: Disk Yolu (Veritabanında eski kalan C:/ yolları için)
            if (dbPath.contains(":") || dbPath.startsWith("/")) {
                try {
                    return new Image("file:" + dbPath);
                } catch (Exception ignored) {}
            }

        } catch (Exception e) {
            System.out.println("Resim yükleme hatası (" + dbPath + "): " + e.getMessage());
        }
        return null; // Yüklenemezse null döner
    }

    /* =======================================================
       📸 KEŞFET POSTLARI (GRID)
       ======================================================= */
    private void loadExplorePosts() {
        if (explorePostContainer == null) return;

        explorePostContainer.getChildren().clear();

        // Instagram Tarzı Grid Ayarları
        explorePostContainer.setHgap(2);
        explorePostContainer.setVgap(2);
        explorePostContainer.setPadding(new Insets(0));
        explorePostContainer.setPrefColumns(3);
        explorePostContainer.setAlignment(Pos.TOP_CENTER);

        List<PostDTO> posts = shareService.getExplorePosts();

        for (PostDTO post : posts) {
            // Kartı oluşturmaya çalış
            StackPane card = buildExplorePostCard(post);

            // 🔥 Sadece başarıyla oluşan (resmi olan) kartları ekle
            // Böylece gri boş kutular asla oluşmaz.
            if (card != null) {
                explorePostContainer.getChildren().add(card);
            }
        }
    }

    private StackPane buildExplorePostCard(PostDTO post) {
        // Resim değilse baştan ele
        if (!post.isImage) return null;

        // Resmi Güvenli Yükle
        Image image = loadImageSafely(post.path);

        // Eğer resim dosyası bulunamadıysa KUTU OLUŞTURMA (null dön)
        if (image == null) return null;

        // 1. Resim Görünümü
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(200);
        imageView.setFitHeight(200);
        imageView.setPreserveRatio(false); // Kutuyu tam doldur
        imageView.setSmooth(true);

        // 2. Hover Efekti (Siyah Perde)
        VBox overlay = new VBox();
        overlay.setAlignment(Pos.CENTER);
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.4);"); // Yarı saydam
        overlay.setOpacity(0); // Gizli başla
        overlay.setPrefSize(200, 200);

        // İstersen overlay içine beğeni sayısı ekleyebilirsin:
        // Label likeLabel = new Label("❤ " + post.likeCount);
        // likeLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        // overlay.getChildren().add(likeLabel);

        // 3. Kartın Kendisi
        StackPane card = new StackPane(imageView, overlay);
        card.setStyle("-fx-cursor: hand;");

        // Mouse Olayları
        card.setOnMouseEntered(e -> overlay.setOpacity(1.0));
        card.setOnMouseExited(e -> overlay.setOpacity(0));

        // Tıklayınca Tam Ekran Aç
        card.setOnMouseClicked(e -> showFullImagePopup(post));

        return card;
    }

    /* =======================================================
       🔍 TAM EKRAN RESİM GÖRÜNTÜLEME (POPUP)
       ======================================================= */
    private void showFullImagePopup(PostDTO post) {
        Image fullImage = loadImageSafely(post.path);
        if (fullImage == null) return;

        Stage popupStage = new Stage();
        popupStage.initStyle(StageStyle.TRANSPARENT); // Çerçevesiz
        popupStage.initModality(Modality.APPLICATION_MODAL); // Ana ekranı kilitle

        ImageView imageView = new ImageView(fullImage);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);

        // Çok büyük resimler ekranı taşırmasın
        imageView.setFitWidth(800);
        imageView.setFitHeight(800);

        // Arka plan
        VBox layout = new VBox(imageView);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: rgba(0, 0, 0, 0.85);"); // Koyu arkaplan
        layout.setPadding(new Insets(20));

        // Kapatma Olayları
        layout.setOnMouseClicked(e -> popupStage.close()); // Tıklayınca kapat

        Scene scene = new Scene(layout);
        scene.setFill(Color.TRANSPARENT);

        // ESC tuşuyla kapat
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) popupStage.close();
        });

        popupStage.setScene(scene);

        // Ekranı Kapla
        popupStage.setWidth(javafx.stage.Screen.getPrimary().getBounds().getWidth());
        popupStage.setHeight(javafx.stage.Screen.getPrimary().getBounds().getHeight());

        popupStage.show();
    }

    /* =======================================================
       👤 ARAMA POPUP MANTIĞI
       ======================================================= */
    private void setupSearchPopup() {
        searchPopup = new Popup();
        searchPopup.setAutoHide(true);

        popupContentBox = new VBox(5);
        popupContentBox.setPadding(new Insets(10));
        popupContentBox.setStyle("-fx-background-color: white; " +
                "-fx-border-color: #ccc; " +
                "-fx-border-radius: 5; " +
                "-fx-background-radius: 5; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 5);");

        ScrollPane scrollWrapper = new ScrollPane(popupContentBox);
        scrollWrapper.setFitToWidth(true);
        scrollWrapper.setMaxHeight(300);
        scrollWrapper.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        searchPopup.getContent().add(scrollWrapper);
    }

    private void handleSearch(String query) {
        if (loggedUser == null) return;
        popupContentBox.getChildren().clear();

        if (query == null || query.trim().isEmpty()) {
            searchPopup.hide();
            return;
        }

        searchUsersAndAddToPopup(query.trim().toLowerCase());
    }

    private void searchUsersAndAddToPopup(String searchText) {
        // Tüm kullanıcıları çekip burada filtreliyoruz (En garantisi)
        List<Users> usersToDiscover = UserService.getAllUsersExcept(loggedUser.getUserId());
        if (usersToDiscover == null) return;

        boolean foundAny = false;

        for (Users targetUser : usersToDiscover) {
            boolean nameMatch = targetUser.getFullName().toLowerCase().contains(searchText);
            boolean usernameMatch = targetUser.getUsername().toLowerCase().contains(searchText);

            if (nameMatch || usernameMatch) {
                int status = relationService.checkRelationStatus(loggedUser.getUserId(), targetUser.getUserId());
                int inverseStatus = relationService.checkRelationStatus(targetUser.getUserId(), loggedUser.getUserId());

                // Zaten arkadaşsak aramada gösterme (Tercihe bağlı)
                if (status != RelationService.STATUS_APPROVED && inverseStatus != RelationService.STATUS_APPROVED) {
                    HBox userCard = buildUserCard(targetUser, status, inverseStatus);
                    if (userCard != null) {
                        popupContentBox.getChildren().add(userCard);
                        foundAny = true;
                    }
                }
            }
        }

        if (foundAny) showPopup();
        else searchPopup.hide();
    }

    private void showPopup() {
        if (!searchPopup.isShowing()) {
            Bounds bounds = txtSearch.localToScreen(txtSearch.getBoundsInLocal());
            popupContentBox.setPrefWidth(txtSearch.getWidth());
            searchPopup.show(txtSearch, bounds.getMinX(), bounds.getMaxY());
        }
    }

    private HBox buildUserCard(Users targetUser, int status, int inverseStatus) {
        HBox card = new HBox(10);
        card.setPadding(new Insets(10));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: #f9f9f9; -fx-background-radius: 5; -fx-border-color: #eaeaea; -fx-border-width: 0 0 1 0;");

        ImageView avatar = new ImageView();
        avatar.setFitWidth(40);
        avatar.setFitHeight(40);
        // Avatar ekleme kodu buraya gelebilir

        VBox info = new VBox(2);
        Label name = new Label(targetUser.getFullName());
        name.setStyle("-fx-font-weight: bold; -fx-text-fill: black;");
        Label username = new Label("@" + targetUser.getUsername());
        username.setStyle("-fx-text-fill: #333333;");
        info.getChildren().addAll(name, username);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button actionButton = new Button();
        actionButton.setStyle("-fx-background-radius: 5; -fx-text-fill: black;");

        if (status == RelationService.STATUS_PENDING) {
            actionButton.setText("İstek Gönderildi");
            actionButton.setDisable(true);
        } else if (inverseStatus == RelationService.STATUS_PENDING) {
            actionButton.setText("Onayla");
            actionButton.setOnAction(e -> {
                if (relationService.acceptFriendRequest(loggedUser.getUserId(), targetUser.getUserId())) {
                    handleSearch(txtSearch.getText());
                }
            });
        } else {
            actionButton.setText("Ekle");
            actionButton.setOnAction(e -> {
                if (relationService.sendFriendRequest(loggedUser.getUserId(), targetUser.getUserId())) {
                    actionButton.setText("Gönderildi");
                    actionButton.setDisable(true);
                }
            });
        }

        card.getChildren().addAll(avatar, info, spacer, actionButton);
        return card;
    }
}