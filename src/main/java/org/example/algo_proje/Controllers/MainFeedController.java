package org.example.algo_proje.Controllers;

import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import org.example.algo_proje.Models.Users;
import org.example.algo_proje.Models.Shares;
import org.example.algo_proje.Services.Database;

import java.io.ByteArrayInputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MainFeedController {

    // SIDEBAR
    public ImageView userAvatar;
    public Label lblUserName;

    // FEED
    public VBox feedContainer;

    // PAYLAŞIM GİRİŞİ
    public TextArea txtShareContent;
    public Button btnShare;
    public Button btnAddPhoto;

    private Users loggedUser;
    private String selectedPhotoPath = null;

    public void initialize() {
        btnShare.setOnAction(e -> onShareButtonClick());
        btnAddPhoto.setOnAction(e -> onAddPhotoClick());
    }

    // LoginController'dan çağrılacak
    public void initData(Users user) {
        this.loggedUser = user;
        if (loggedUser != null) {
            loadSidebarData();
            loadFeedPostsFromDatabase();
        }
    }

    private void loadSidebarData() {
        lblUserName.setText(
                loggedUser.getFullName() != null && !loggedUser.getFullName().isEmpty()
                        ? loggedUser.getFullName()
                        : loggedUser.getUsername()
        );

        // loggedUser.getProfilePhoto() modeline göre byte[] veya path olabilir —
        // burada sadece gösterme amaçlı basit kontrol yapıyoruz.
        try {
            Object pf = null;
            try {
                // Eğer profil fotoğrafı byte[] ise
                pf = loggedUser.getClass().getMethod("getProfilePhoto").invoke(loggedUser);
            } catch (NoSuchMethodException ignore) { /* yoksa atla */ }

            if (pf instanceof byte[]) {
                byte[] data = (byte[]) pf;
                if (data != null && data.length > 0) {
                    Image img = new Image(new ByteArrayInputStream(data));
                    userAvatar.setImage(img);
                }
            } else if (pf instanceof String) {
                String path = (String) pf;
                if (path != null && !path.isEmpty()) {
                    try { userAvatar.setImage(new Image("file:" + path)); } catch (Exception ex){}
                }
            }
        } catch (Exception ignored){}
    }



    // ------------------ PAYLAŞIM EKLEME ------------------

    private void onAddPhotoClick() {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Fotoğraf Seç");
        fileChooser.getExtensionFilters().add(
                new javafx.stage.FileChooser.ExtensionFilter("Resim Dosyaları", "*.png", "*.jpg", "*.jpeg")
        );

        java.io.File file = fileChooser.showOpenDialog(btnAddPhoto.getScene().getWindow());
        if (file != null) {
            selectedPhotoPath = file.getAbsolutePath();
            showAlert("Fotoğraf seçildi: " + selectedPhotoPath);
        } else {
            selectedPhotoPath = null;
        }
    }

    private void onShareButtonClick() {
        String text = txtShareContent.getText() == null ? "" : txtShareContent.getText().trim();
        if (text.isEmpty() && selectedPhotoPath == null) {
            showAlert("Boş paylaşım gönderemezsin.");
            return;
        }

        Shares share = new Shares();
        share.setUserId(loggedUser.getUserId());
        share.setTitle(null);
        share.setDescription(text);
        share.setPath(selectedPhotoPath);
        share.setImage(selectedPhotoPath != null);

        boolean ok = saveShareToDatabase(share);
        if (ok) {
            txtShareContent.clear();
            selectedPhotoPath = null;
            loadFeedPostsFromDatabase();
        } else {
            showAlert("Paylaşım kaydedilirken hata oluştu.");
        }
    }

    private boolean saveShareToDatabase(Shares share) {
        String sql = "INSERT INTO Shares (UserId, Title, Description, Path, IsImage, CreatedAt) VALUES (?, ?, ?, ?, ?, GETDATE())";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, share.getUserId());
            ps.setString(2, share.getTitle());
            ps.setString(3, share.getDescription());
            ps.setString(4, share.getPath());
            ps.setBoolean(5, share.isImage());

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

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

                // profile photo: try bytes first
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
        // Outer card
        VBox card = new VBox();
        card.setSpacing(8);
        card.setPadding(new Insets(12));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 8, 0, 0, 2);");

        // Header: avatar + name + time
        HBox header = new HBox();
        header.setSpacing(10);
        header.setPadding(new Insets(4, 0, 4, 0));

        ImageView avatar = new ImageView();
        avatar.setFitWidth(44);
        avatar.setFitHeight(44);
        avatar.setPreserveRatio(true);
        avatar.setSmooth(true);
        Image avatarImage = getAvatarImage(p.authorAvatarBytes, p.path /* fallback - not ideal */);
        if (avatarImage != null) avatar.setImage(avatarImage);
        else avatar.setImage(new Image(getClass().getResourceAsStream("/static/Images/profile_pics/default.png"))); // ensure default exists

        VBox nameBox = new VBox();
        Label nameLbl = new Label(p.authorFullName != null && !p.authorFullName.isEmpty() ? p.authorFullName : p.authorUsername);
        nameLbl.setStyle("-fx-font-weight: 600; -fx-font-size: 13;");
        Label timeLbl = new Label(p.createdAt != null ? p.createdAt.toString() : "");
        timeLbl.setStyle("-fx-font-size: 11; -fx-text-fill: #777777;");
        nameBox.getChildren().addAll(nameLbl, timeLbl);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(avatar, nameBox, spacer);

        Label contentLbl = new Label(p.description == null ? "" : p.description);
        contentLbl.setWrapText(true);
        contentLbl.setStyle("-fx-font-size: 13;");

        card.getChildren().addAll(header, contentLbl);

        if (p.isImage && p.path != null && !p.path.isEmpty()) {
            try {
                Image iv = new Image("file:" + p.path, 640, 0, true, true);
                ImageView iview = new ImageView(iv);
                iview.setPreserveRatio(true);
                iview.setFitWidth(640);
                iview.setStyle("-fx-background-radius: 8;");
                VBox.setMargin(iview, new Insets(6, 0, 0, 0));
                card.getChildren().add(iview);
            } catch (Exception ex) {
                // swallow
            }
        }

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
            if (nowLiked) btnLike.setStyle(btnLike.getStyle() + "-fx-background-color: rgba(255,0,0,0.08);");
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

        actions.getChildren().addAll(btnLike, btnLikers);
        card.getChildren().add(actions);

        return card;
    }

    private Image getAvatarImage(byte[] avatarBytes, String fallbackPath) {
        try {
            if (avatarBytes != null && avatarBytes.length > 0) {
                return new Image(new ByteArrayInputStream(avatarBytes));
            }
        } catch (Exception ignored) {}
        try {
            if (fallbackPath != null && !fallbackPath.isEmpty()) {
                return new Image("file:" + fallbackPath, 44, 44, true, true);
            }
        } catch (Exception ignored) {}
        return null;
    }

    private int getLikeCount(int shareId) {
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

    private static class PostDTO {
        int shareId;
        int shareUserId;
        String description;
        String path;
        boolean isImage;
        Timestamp createdAt;

        int authorUserId;
        String authorUsername;
        String authorFullName;
        byte[] authorAvatarBytes;
    }
}
