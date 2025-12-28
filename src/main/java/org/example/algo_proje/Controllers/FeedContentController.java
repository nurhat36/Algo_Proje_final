package org.example.algo_proje.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.algo_proje.Attribute.PhotoAttribute;
import org.example.algo_proje.Models.DTOs.PostDTO;
import org.example.algo_proje.Models.DTOs.RecommendationDTO;
import org.example.algo_proje.Models.Users;
import org.example.algo_proje.Models.Shares;
import org.example.algo_proje.Services.Database;
import org.example.algo_proje.Services.RecommendationService;
import org.example.algo_proje.Services.ShareService;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FeedContentController {

    // FEED FXML
    @FXML public VBox feedContainer;
    @FXML public TextArea txtShareContent;
    @FXML public Button btnShare;
    @FXML public Button btnAddPhoto;

    private Users loggedUser;
    private File selectedShareFile = null;

    // Servisler
    private final ShareService shareService = new ShareService();
    private final RecommendationService recommendationService = new RecommendationService(); // Yeni servis

    @FXML
    public void initialize() {
        btnShare.setOnAction(e -> onShareButtonClick());
        btnAddPhoto.setOnAction(e -> onAddPhotoClick());
    }

    public void setLoggedUser(Users user) {
        this.loggedUser = user;
        if (loggedUser != null) {
            loadFeedPostsFromDatabase();
        }
    }

    // ... (Fotoğraf ekleme ve Paylaşım buton kodları aynı kalıyor) ...

    private void onAddPhotoClick() {
        // ... (Mevcut kod aynen kalacak) ...
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Fotoğraf Seç");
        fileChooser.getExtensionFilters().add(
                new javafx.stage.FileChooser.ExtensionFilter("Resim Dosyaları", "*.png", "*.jpg", "*.jpeg")
        );
        java.io.File file = fileChooser.showOpenDialog(btnAddPhoto.getScene().getWindow());
        if (file != null) {
            selectedShareFile = file;
            showAlert("Fotoğraf seçildi: " + file.getName());
        } else {
            selectedShareFile = null;
        }
    }

    private void onShareButtonClick() {
        // ... (Mevcut kod aynen kalacak) ...
        String text = txtShareContent.getText() == null ? "" : txtShareContent.getText().trim();
        if (text.isEmpty() && selectedShareFile == null) {
            showAlert("Boş paylaşım gönderemezsin.");
            return;
        }
        String savedImagePath = null;
        if (selectedShareFile != null) {
            savedImagePath = PhotoAttribute.saveImageToStaticFolder(selectedShareFile, "src/main/resources/static/Images/Shares_Pics/");
            if (savedImagePath == null) {
                showAlert("Resim kaydedilirken hata oluştu. Paylaşım yapılamadı.");
                return;
            }
        }
        Shares share = new Shares();
        share.setUserId(loggedUser.getUserId());
        share.setTitle(null);
        share.setDescription(text);
        share.setPath(savedImagePath);
        share.setImage(savedImagePath != null);

        boolean ok = shareService.addShare(share);
        if (ok) {
            txtShareContent.clear();
            selectedShareFile = null;
            loadFeedPostsFromDatabase();
        } else {
            showAlert("Paylaşım kaydedilirken hata oluştu.");
        }
    }

    // ------------------ AKIŞ YÜKLEME VE POST OLUŞTURMA (GÜNCELLENDİ) ------------------

    private void loadFeedPostsFromDatabase() {
        feedContainer.getChildren().clear();

        List<PostDTO> posts = new ArrayList<>();

        // 1. Postları Çek
        String sql = """
                 SELECT s.Id, s.UserId AS ShareUserId, s.Description, s.Path, s.IsImage, s.CreatedAt,
                        u.UserId AS UserId, u.Username, u.FullName, u.ProfilePhoto
                 FROM Shares s
                 JOIN Users u ON u.UserId = s.UserId
                 WHERE ISNULL(s.IsDeleted,0) = 0
                 ORDER BY s.CreatedAt DESC
                 """;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                PostDTO dto = new PostDTO();
                dto.shareId = rs.getInt("Id");
                dto.shareUserId = rs.getInt("ShareUserId");
                dto.description = rs.getString("Description");
                dto.path = rs.getString("Path");
                dto.isImage = rs.getBoolean("IsImage");
                dto.createdAt = rs.getTimestamp("CreatedAt");

                dto.authorUserId = rs.getInt("UserId");
                dto.authorUsername = rs.getString("Username");
                dto.authorFullName = rs.getString("FullName");


                dto.authorAvatarBytes = rs.getString("ProfilePhoto");;

                posts.add(dto);
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Paylaşımlar yüklenirken hata: " + e.getMessage());
            return;
        }

        // 2. Önerileri Çek (YENİ)
        List<RecommendationDTO> recommendations = recommendationService.getTopRecommendations(loggedUser.getUserId());

        // 3. Postları ve Öneri Bandını Ekle (YENİ MANTIK)
        int postCounter = 0;
        for (PostDTO p : posts) {
            // Postu ekle
            feedContainer.getChildren().add(buildPostCard(p));
            postCounter++;

            // 2. Posttan sonra öneri bandını ekle (Eğer öneri varsa)
            if (postCounter == 2 && !recommendations.isEmpty()) {
                feedContainer.getChildren().add(createRecommendationBanner(recommendations));
            }
        }

        // Eğer hiç post yoksa veya 2'den az post varsa ama öneri varsa, en sona ekle
        if (postCounter < 2 && !recommendations.isEmpty()) {
            feedContainer.getChildren().add(createRecommendationBanner(recommendations));
        }
    }

    // ... (buildPostCard ve diğer mevcut metodlar aynen kalacak) ...
    // (Kodun çok uzamaması için buildPostCard, getAvatarImage vb. metodları tekrar yazmıyorum,
    //  onlar senin verdiğin koddaki gibi kalmalı. Aşağıya SADECE YENİ METODLARI ekliyorum)

    private VBox buildPostCard(PostDTO p) {
        // ... (Senin mevcut buildPostCard kodun buraya gelecek) ...
        // ... (Kısa tutmak için burayı atlıyorum, sen kendi kodunu koru) ...
        // Sadece örnek olması açısından yapıyı koruyorum:
        VBox card = new VBox();
        card.setSpacing(8);
        card.setPadding(new Insets(12));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 8, 0, 0, 2);");

        // Header
        HBox header = new HBox();
        header.setSpacing(10);
        header.setPadding(new Insets(4, 0, 4, 0));
        // --- AVATAR YÜKLEME KISMI (GÜNCELLENDİ) ---
        ImageView avatar = new ImageView();
        avatar.setFitWidth(44);
        avatar.setFitHeight(44);
        avatar.setPreserveRatio(true);

        // Varsayılan resim yolu
        String defaultPath = "/static/Images/profile_pics/default.png";
        // Veritabanından gelen isim
        String photoName = p.authorAvatarBytes;

        Image img = null;

        try {
            // Eğer veritabanında isim varsa yüklemeyi dene
            if (photoName != null && !photoName.trim().isEmpty()) {
                // "src/main/resources" altındaki yol: /static/Images/profile_pics/resim.jpg
                String fullPath = "/static/Images/profile_pics/" + photoName;

                // Kaynağı kontrol et (NullPointerException yememek için)
                if (getClass().getResource(fullPath) != null) {
                    img = new Image(getClass().getResourceAsStream(fullPath));
                } else {
                    System.out.println("Resim bulunamadı, varsayılan yükleniyor: " + fullPath);
                }
            }

            // Eğer resim yüklenemediyse (veya isim null ise) varsayılanı yükle
            if (img == null) {
                if (getClass().getResource(defaultPath) != null) {
                    img = new Image(getClass().getResourceAsStream(defaultPath));
                }
            }
        } catch (Exception e) {
            System.out.println("Avatar yükleme hatası: " + e.getMessage());
        }

        if (img != null) avatar.setImage(img);
        VBox nameBox = new VBox();
        Label nameLbl = new Label(p.authorFullName != null && !p.authorFullName.isEmpty() ? p.authorFullName : p.authorUsername);
        nameLbl.setStyle("-fx-font-weight: 600; -fx-font-size: 13;");
        Label timeLbl = new Label(p.createdAt != null ? p.createdAt.toString() : "");
        timeLbl.setStyle("-fx-font-size: 11; -fx-text-fill: #777777;");
        nameBox.getChildren().addAll(nameLbl, timeLbl);
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(avatar, nameBox, spacer);
        card.getChildren().add(header);

        // Content
        Label contentLbl = new Label(p.description == null ? "" : p.description);
        contentLbl.setWrapText(true);
        contentLbl.setStyle("-fx-font-size: 13;-fx-text-fill: black;");
        card.getChildren().add(contentLbl);

        // Image
        if (p.isImage && p.path != null && !p.path.isEmpty()) {
            try {
                Image iv = PhotoAttribute.loadImageFromResources(p.path, "/Static/Images/Shares_Pics/", getClass());
                ImageView iview = new ImageView(iv);
                iview.setPreserveRatio(true); iview.setFitWidth(640);
                iview.setStyle("-fx-background-radius: 8;");
                VBox.setMargin(iview, new Insets(6, 0, 0, 0));
                card.getChildren().add(iview);
            } catch (Exception ex) { }
        }

        // Actions
        HBox actions = new HBox(12);
        actions.setPadding(new Insets(8, 0, 0, 0));
        int currentLikeCount = getLikeCount(p.shareId);
        Button btnLike = new Button("❤ " + currentLikeCount);
        btnLike.setCursor(Cursor.HAND);
        btnLike.setStyle("-fx-background-radius: 6; -fx-padding: 6 10;");
        if (checkIfUserLiked(p.shareId, loggedUser.getUserId())) btnLike.setStyle(btnLike.getStyle() + "-fx-background-color: rgba(255,0,0,0.08);");
        btnLike.setOnAction(evt -> {
            boolean nowLiked = toggleLike(p.shareId, loggedUser.getUserId());
            int newCount = getLikeCount(p.shareId);
            btnLike.setText("❤ " + newCount);
            if (nowLiked) btnLike.setStyle("-fx-background-radius: 6; -fx-padding: 6 10; -fx-background-color: rgba(255,0,0,0.08);");
            else btnLike.setStyle("-fx-background-radius: 6; -fx-padding: 6 10;");
        });
        Button btnLikers = new Button("Kimler beğendi");
        btnLikers.setOnAction(evt -> {
            List<String> likers = getLikersNames(p.shareId);
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setHeaderText("Beğenenler");
            a.setContentText(likers.isEmpty() ? "Henüz kimse beğenmedi." : String.join("\n", likers));
            a.showAndWait();
        });
        Button btnComment = new Button("💬 ");
        btnComment.setStyle("-fx-background-radius: 6; -fx-padding: 6 10;");
        btnComment.setOnAction(evt -> showCommentPopup(p));
        actions.getChildren().addAll(btnLike, btnLikers, btnComment);
        card.getChildren().add(actions);

        return card;
    }

    // ------------------ YENİ EKLENEN METODLAR (ÖNERİ BANDI İÇİN) ------------------

    private VBox createRecommendationBanner(List<RecommendationDTO> recs) {
        VBox bannerContainer = new VBox(10);
        bannerContainer.setPadding(new Insets(15, 0, 15, 0));
        // Hafif gri arka plan ile ayrıştırma
        bannerContainer.setStyle("-fx-background-color: #fafafa; -fx-background-radius: 8; -fx-border-color: #efefef; -fx-border-width: 1px 0 1px 0;");

        // Başlık
        Label header = new Label("Sizin İçin Önerilenler");
        header.setStyle("-fx-font-weight: bold; -fx-text-fill: #262626; -fx-font-size: 14px; -fx-padding: 0 0 0 10;");

        // Yatay Kaydırma Alanı
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToHeight(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        HBox cardBox = new HBox(15);
        cardBox.setPadding(new Insets(5, 10, 5, 10));

        // En fazla 5 öneri göster
        int limit = Math.min(recs.size(), 5);
        for (int i = 0; i < limit; i++) {
            cardBox.getChildren().add(createSingleRecommendationCard(recs.get(i)));
        }

        scrollPane.setContent(cardBox);
        bannerContainer.getChildren().addAll(header, scrollPane);

        return bannerContainer;
    }

    private VBox createSingleRecommendationCard(RecommendationDTO user) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(15));
        card.setPrefWidth(160);
        card.setMinWidth(160);

        // Kart tasarımı: Beyaz, gölgeli, yuvarlak köşeli
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 0); " +
                "-fx-border-color: #dbdbdb; -fx-border-radius: 10;");

        // 1. Profil Resmi (Yuvarlak)
        Circle avatar = new Circle(30, Color.web("#f0f2f5"));
        avatar.setStroke(Color.web("#dbdbdb"));
        // İsim baş harfi (Görsel yoksa)
        Label initials = new Label(user.username.substring(0, 1).toUpperCase());
        initials.setStyle("-fx-font-weight: bold; -fx-text-fill: #555;");
        StackPane avatarStack = new StackPane(avatar, initials);


        // 2. İsimler
        Label nameLbl = new Label(user.fullName != null && !user.fullName.isEmpty() ? user.fullName : user.username);
        nameLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;-fx-text-fill: black;");
        nameLbl.setWrapText(true);
        nameLbl.setAlignment(Pos.CENTER);
        nameLbl.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        // 3. Ortak Arkadaş Bilgisi
        Label mutualLbl = new Label(user.mutualFriendNames);
        mutualLbl.setStyle("-fx-text-fill: #8e8e8e; -fx-font-size: 11px;");
        mutualLbl.setWrapText(true);
        mutualLbl.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        mutualLbl.setPrefHeight(35); // Sabit yükseklik, kaymayı önler

        // 4. Puan Bilgisi
        Label scoreLbl = new Label("Öneri Puanı: " + (int)user.score);
        scoreLbl.setStyle("-fx-text-fill: #0095f6; -fx-font-size: 10px; -fx-font-weight: bold;");

        // 5. Takip Et Butonu
        Button followBtn = new Button("Takip Et");
        followBtn.setMaxWidth(Double.MAX_VALUE);
        followBtn.setStyle("-fx-background-color: #0095f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-cursor: hand;");

        followBtn.setOnAction(e -> {
            // Buraya "Arkadaş Ekle" servis çağrısı eklenebilir.
            // Şimdilik sadece görsel tepki veriyoruz:
            followBtn.setText("İstek Gönderildi");
            followBtn.setStyle("-fx-background-color: white; -fx-text-fill: black; -fx-border-color: #dbdbdb; -fx-border-radius: 5;");
        });

        card.getChildren().addAll(avatarStack, nameLbl, mutualLbl, scoreLbl, followBtn);
        return card;
    }


    // ------------------ DİĞER YARDIMCI METODLAR (MEVCUT KODLARINIZ) ------------------

    private void showCommentPopup(PostDTO post) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/algo_proje/Views/CommentView.fxml"));
            Parent root = loader.load();
            root.getStylesheets().add(getClass().getResource("/org/example/algo_proje/Styles/comment.css").toExternalForm());
            CommentController controller = loader.getController();
            controller.initData(post, loggedUser);
            Stage stage = new Stage();
            stage.setTitle(post.authorUsername + " - Paylaşım Yorumları");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private Image getAvatarImage(byte[] avatarBytes, String fallbackPath) {
        try {
            if (avatarBytes != null && avatarBytes.length > 0) return new Image(new ByteArrayInputStream(avatarBytes));
        } catch (Exception ignored) {}
        // Fallback (Burada normalde default resim veya path kullanılır)
        return null;
    }

    private int getLikeCount(int shareId) {
        String sql = "SELECT COUNT(*) AS c FROM Likes WHERE ShareId = ?";
        try (Connection conn = Database.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, shareId);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return rs.getInt("c"); }
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    private boolean checkIfUserLiked(int shareId, int userId) {
        String sql = "SELECT 1 FROM Likes WHERE ShareId = ? AND UserId = ?";
        try (Connection conn = Database.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, shareId); ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    private boolean toggleLike(int shareId, int userId) {
        try (Connection conn = Database.getConnection()) {
            String check = "SELECT 1 FROM Likes WHERE ShareId = ? AND UserId = ?";
            try (PreparedStatement ps = conn.prepareStatement(check)) {
                ps.setInt(1, shareId); ps.setInt(2, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String del = "DELETE FROM Likes WHERE ShareId = ? AND UserId = ?";
                        try (PreparedStatement delSt = conn.prepareStatement(del)) {
                            delSt.setInt(1, shareId); delSt.setInt(2, userId); delSt.executeUpdate();
                        }
                        return false;
                    }
                }
            }
            String insert = "INSERT INTO Likes (ShareId, UserId, LikedAt) VALUES (?, ?, GETDATE())";
            try (PreparedStatement ins = conn.prepareStatement(insert)) {
                ins.setInt(1, shareId); ins.setInt(2, userId); ins.executeUpdate();
                return true;
            }
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    private List<String> getLikersNames(int shareId) {
        List<String> list = new ArrayList<>();
        String sql = "SELECT u.UserId, u.Username, u.FullName FROM Likes l JOIN Users u ON u.UserId = l.UserId WHERE l.ShareId = ? ORDER BY l.LikedAt DESC";
        try (Connection conn = Database.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, shareId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String name = rs.getString("FullName");
                    if (name == null || name.isEmpty()) name = rs.getString("Username");
                    list.add(name);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    private void showAlert(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}