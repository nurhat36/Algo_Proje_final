package org.example.algo_proje.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.algo_proje.Attribute.PhotoAttribute;
import org.example.algo_proje.Models.Comments;
import org.example.algo_proje.Models.DTOs.PostDTO;
import org.example.algo_proje.Models.Users;
import org.example.algo_proje.Models.Shares;
import org.example.algo_proje.Services.CommentService;
import org.example.algo_proje.Services.Database;
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
    private final ShareService shareService = new ShareService();

    @FXML
    public void initialize() {
        // Olay işleyicilerini bağla
        btnShare.setOnAction(e -> onShareButtonClick());
        btnAddPhoto.setOnAction(e -> onAddPhotoClick());
        // initData çağrılana kadar akış yüklenmez.
    }

    /**
     * Ana kontrolcüden kullanıcı verisini alır ve akışı yükler.
     */
    public void setLoggedUser(Users user) {
        this.loggedUser = user;
        if (loggedUser != null) {
            loadFeedPostsFromDatabase();
        }
    }


    // ------------------ PAYLAŞIM EKLEME ------------------

    private void onAddPhotoClick() {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Fotoğraf Seç");
        fileChooser.getExtensionFilters().add(
                new javafx.stage.FileChooser.ExtensionFilter("Resim Dosyaları", "*.png", "*.jpg", "*.jpeg")
        );

        // Pencereyi almak için herhangi bir FXML bileşeninin Scene'ini kullanabiliriz
        java.io.File file = fileChooser.showOpenDialog(btnAddPhoto.getScene().getWindow());

        if (file != null) {
            selectedShareFile = file;
            showAlert("Fotoğraf seçildi: " + file.getName());
        } else {
            selectedShareFile = null;
        }
    }

    private void onShareButtonClick() {
        String text = txtShareContent.getText() == null ? "" : txtShareContent.getText().trim();
        if (text.isEmpty() && selectedShareFile == null) {
            showAlert("Boş paylaşım gönderemezsin.");
            return;
        }

        String savedImagePath = null;

        // --- 1. GÖRSELİ STATİK KLASÖRE TAŞIMA (PhotoAttribute Kullanımı) ---
        if (selectedShareFile != null) {
            savedImagePath = PhotoAttribute.saveImageToStaticFolder(selectedShareFile, "src/main/resources/static/Images/Shares_Pics/");

            if (savedImagePath == null) {
                showAlert("Resim kaydedilirken hata oluştu. Paylaşım yapılamadı.");
                return;
            }
        }

        // --- 2. PAYLAŞIM NESNESİNİ OLUŞTURMA ---
        Shares share = new Shares();
        share.setUserId(loggedUser.getUserId());
        share.setTitle(null);
        share.setDescription(text);
        share.setPath(savedImagePath);
        share.setImage(savedImagePath != null);

        // --- 3. SERVİS İLE VERİTABANINA KAYDETME ---
        boolean ok = shareService.addShare(share);

        if (ok) {
            txtShareContent.clear();
            selectedShareFile = null;
            loadFeedPostsFromDatabase(); // Akışı yenile
        } else {
            showAlert("Paylaşım kaydedilirken hata oluştu.");
        }
    }

    // ------------------ AKIŞ YÜKLEME VE POST OLUŞTURMA ------------------

    private void loadFeedPostsFromDatabase() {
        feedContainer.getChildren().clear();

        List<PostDTO> posts = new ArrayList<>();

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

                byte[] avatarBytes = null;
                try { avatarBytes = rs.getBytes("ProfilePhoto"); } catch (Exception ignored) {}
                dto.authorAvatarBytes = avatarBytes;

                posts.add(dto);
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Paylaşımlar yüklenirken hata: " + e.getMessage());
            return;
        }

        for (PostDTO p : posts) {
            feedContainer.getChildren().add(buildPostCard(p));
        }
    }

    private VBox buildPostCard(PostDTO p) {
        // KART OLUŞTURMA MANTIĞI (Mevcut kontrolcünüzden kopyalandı)
        VBox card = new VBox();
        card.setSpacing(8);
        card.setPadding(new Insets(12));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 8, 0, 0, 2);");

        // Header
        HBox header = new HBox();
        header.setSpacing(10);
        header.setPadding(new Insets(4, 0, 4, 0));

        // Avatar
        ImageView avatar = new ImageView();
        avatar.setFitWidth(44);
        avatar.setFitHeight(44);
        avatar.setPreserveRatio(true);
        avatar.setSmooth(true);
        Image avatarImage = getAvatarImage(p.authorAvatarBytes, p.path /* fallback - not ideal */);
        if (avatarImage != null) avatar.setImage(avatarImage);
        else avatar.setImage(new Image(getClass().getResourceAsStream("/static/Images/profile_pics/default.png")));

        VBox nameBox = new VBox();
        Label nameLbl = new Label(p.authorFullName != null && !p.authorFullName.isEmpty() ? p.authorFullName : p.authorUsername);
        nameLbl.setStyle("-fx-font-weight: 600; -fx-font-size: 13;");
        Label timeLbl = new Label(p.createdAt != null ? p.createdAt.toString() : "");
        timeLbl.setStyle("-fx-font-size: 11; -fx-text-fill: #777777;");
        nameBox.getChildren().addAll(nameLbl, timeLbl);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(avatar, nameBox, spacer);
        card.getChildren().add(header);

        // Content
        Label contentLbl = new Label(p.description == null ? "" : p.description);
        contentLbl.setWrapText(true);
        contentLbl.setStyle("-fx-font-size: 13;");
        card.getChildren().add(contentLbl);


        // Image Content
        if (p.isImage && p.path != null && !p.path.isEmpty()) {
            try {
                Image iv = PhotoAttribute.loadImageFromResources(
                        p.path,
                        "/Static/Images/Shares_Pics/",
                        getClass()
                );

                ImageView iview = new ImageView(iv);
                iview.setPreserveRatio(true);
                iview.setFitWidth(640);
                iview.setStyle("-fx-background-radius: 8;");
                VBox.setMargin(iview, new Insets(6, 0, 0, 0));
                card.getChildren().add(iview);
            } catch (Exception ex) {
                // Hata yutulabilir
            }
        }

        // Actions (Like Buttons)
        HBox actions = new HBox();
        actions.setSpacing(12);
        actions.setPadding(new Insets(8, 0, 0, 0));

        int currentLikeCount = getLikeCount(p.shareId);
        Button btnLike = new Button("❤ " + currentLikeCount);
        btnLike.setCursor(Cursor.HAND);

        btnLike.setStyle("-fx-background-radius: 6; -fx-padding: 6 10;");

        boolean alreadyLiked = checkIfUserLiked(p.shareId, loggedUser.getUserId());
        if (alreadyLiked) btnLike.setStyle(btnLike.getStyle() + "-fx-background-color: rgba(255,0,0,0.08);");

        btnLike.setOnAction(evt -> {
            boolean nowLiked = toggleLike(p.shareId, loggedUser.getUserId());
            int newCount = getLikeCount(p.shareId);
            btnLike.setText("❤ " + newCount);
            // Stili dinamik güncelle
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
        // buildPostCard içine eklenecek buton
        Button btnComment = new Button("💬 ");
        btnComment.setStyle("-fx-background-radius: 6; -fx-padding: 6 10;");

        btnComment.setOnAction(evt -> {
            showCommentPopup(p); // PostDTO p parametresini kullanır
        });

        actions.getChildren().addAll(btnLike, btnLikers,btnComment);
        card.getChildren().add(actions);

        return card;
    }
    private void showCommentPopup(PostDTO post) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/algo_proje/Views/CommentView.fxml"));
            Parent root = loader.load();

            // CSS dosyasını ekle
            root.getStylesheets().add(getClass().getResource("/org/example/algo_proje/Styles/comment.css").toExternalForm());

            CommentController controller = loader.getController();
            controller.initData(post, loggedUser);

            Stage stage = new Stage();
            stage.setTitle(post.authorUsername + " - Paylaşım Yorumları");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL); // Ana pencereyi kilitler
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    // ------------------ BEĞENİ MANTIKLARI VE AVATAR YÜKLEME ------------------

    private Image getAvatarImage(byte[] avatarBytes, String fallbackPath) {
        // Mevcut kontrolcünüzdeki getAvatarImage metodu
        try {
            if (avatarBytes != null && avatarBytes.length > 0) {
                return new Image(new ByteArrayInputStream(avatarBytes));
            }
        } catch (Exception ignored) {}
        try {
            if (fallbackPath != null && !fallbackPath.isEmpty()) {
                // Not: Bu kısım normalde profile path değil, paylaşım path'idir.
                // Eğer avatar path'iniz veritabanında byte değil de string olarak tutuluyorsa bu mantık kullanılabilir.
                return new Image("file:" + fallbackPath, 44, 44, true, true);
            }
        } catch (Exception ignored) {}
        return null;
    }

    private int getLikeCount(int shareId) {
        // Mevcut kontrolcünüzdeki getLikeCount metodu
        String sql = "SELECT COUNT(*) AS c FROM Likes WHERE ShareId = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, shareId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("c");
            }
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    private boolean checkIfUserLiked(int shareId, int userId) {
        // Mevcut kontrolcünüzdeki checkIfUserLiked metodu
        String sql = "SELECT 1 FROM Likes WHERE ShareId = ? AND UserId = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, shareId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    private boolean toggleLike(int shareId, int userId) {
        // Mevcut kontrolcünüzdeki toggleLike metodu
        try (Connection conn = Database.getConnection()) {
            String check = "SELECT 1 FROM Likes WHERE ShareId = ? AND UserId = ?";
            try (PreparedStatement ps = conn.prepareStatement(check)) {
                ps.setInt(1, shareId);
                ps.setInt(2, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        // already liked -> remove
                        String del = "DELETE FROM Likes WHERE ShareId = ? AND UserId = ?";
                        try (PreparedStatement delSt = conn.prepareStatement(del)) {
                            delSt.setInt(1, shareId);
                            delSt.setInt(2, userId);
                            delSt.executeUpdate();
                        }
                        return false;
                    }
                }
            }

            String insert = "INSERT INTO Likes (ShareId, UserId, LikedAt) VALUES (?, ?, GETDATE())";
            try (PreparedStatement ins = conn.prepareStatement(insert)) {
                ins.setInt(1, shareId);
                ins.setInt(2, userId);
                ins.executeUpdate();
                return true;
            }

        } catch (SQLIntegrityConstraintViolationException dup) {
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private List<String> getLikersNames(int shareId) {
        // Mevcut kontrolcünüzdeki getLikersNames metodu
        List<String> list = new ArrayList<>();
        String sql = """
                 SELECT u.UserId, u.Username, u.FullName
                 FROM Likes l
                 JOIN Users u ON u.UserId = l.UserId
                 WHERE l.ShareId = ?
                 ORDER BY l.LikedAt DESC
                 """;
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
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
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}