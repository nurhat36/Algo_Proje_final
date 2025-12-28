package org.example.algo_proje.Controllers;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import org.example.algo_proje.Models.DTOs.RecommendationDTO;
import org.example.algo_proje.Models.DTOs.UserScoreDTO;
import org.example.algo_proje.Models.Raws.CommentRaw;
import org.example.algo_proje.Models.Raws.LikeRaw;
import org.example.algo_proje.Models.Raws.RelationRaw;
import org.example.algo_proje.Models.Users;
import org.example.algo_proje.Services.*;

import java.util.*;

public class FriendsController {

    @FXML private VBox contentArea;
    @FXML private Button btnRelationshipScore;
    @FXML private Button btnShowGraph;
    @FXML private Button btnRecommend;

    // Servisler
    private final RelationService relationService = new RelationService();
    private final UserService userService = new UserService();
    private final RecommendationService recommendationService = new RecommendationService(); // Yeni Servis

    private Users loggedUser;

    /**
     * MainFeedController'dan gelen veriyi initialize eder.
     */
    public void initData(Users user) {
        this.loggedUser = user;
        handleScoreTab(); // Sayfa açıldığında otomatik puanları yükle
    }

    // ------------------------------------------------------------------------
    // 1. SEKME: İLİŞKİ PUANLARI (Mevcut Etkileşimler)
    // ------------------------------------------------------------------------
    @FXML
    private void handleScoreTab() {
        contentArea.getChildren().clear();
        updateButtonStyles(btnRelationshipScore);

        List<UserScoreDTO> scores = calculateRelationshipScores();

        // Puanlara göre büyükten küçüğe sırala
        scores.sort(Comparator.comparingDouble(UserScoreDTO::getTotalScore).reversed());

        if (scores.isEmpty()) {
            contentArea.getChildren().add(new Label("Henüz etkileşimde bulunduğunuz kimse yok."));
        } else {
            for (UserScoreDTO dto : scores) {
                contentArea.getChildren().add(buildScoreRow(dto));
            }
        }
    }

    // Mevcut ilişkiler ve etkileşimler için hesaplama (Servisten bağımsız kalabilir veya oraya taşınabilir)
    private List<UserScoreDTO> calculateRelationshipScores() {
        UserService userService = new UserService();
        ShareService shareService = new ShareService();
        RelationService relationService = new RelationService();

        List<Users> allUsers = userService.getAllUsersExceptMe(loggedUser.getUserId());
        List<RelationRaw> allRelations = relationService.getAllRelationsRaw();
        List<LikeRaw> allLikes = shareService.getAllLikesRaw();
        List<CommentRaw> allComments = shareService.getAllCommentsRaw();

        List<UserScoreDTO> scoreList = new ArrayList<>();

        for (Users otherUser : allUsers) {
            int otherId = otherUser.getUserId();
            int myId = loggedUser.getUserId();

            double currentTotalScore = 0;
            String statusText = "Tanıdık";

            // A. İLİŞKİ DURUMU
            int iliskiDurumu = 0;
            for (RelationRaw rel : allRelations) {
                if ((rel.user1 == myId && rel.user2 == otherId) ||
                        (rel.user2 == myId && rel.user1 == otherId)) {
                    iliskiDurumu = rel.status;
                    break;
                }
            }

            if (iliskiDurumu == 1) {
                currentTotalScore += 15;
                statusText = "Arkadaş";
            } else if (iliskiDurumu == 2) {
                currentTotalScore += 30;
                statusText = "Yakın Arkadaş";
            }

            // B. BEĞENİ PUANI (+5)
            for (LikeRaw like : allLikes) {
                if (like.postOwnerId == myId && like.likerId == otherId) {
                    currentTotalScore += 5;
                }
            }

            // C. YORUM PUANI (+10)
            for (CommentRaw comment : allComments) {
                if (comment.postOwnerId == myId && comment.commenterId == otherId) {
                    currentTotalScore += 10;
                }
            }

            if (currentTotalScore > 0 || iliskiDurumu > 0) {
                scoreList.add(new UserScoreDTO(
                        otherId,
                        otherUser.getUsername(),
                        otherUser.getFullName(),
                        currentTotalScore,
                        statusText
                ));
            }
        }
        return scoreList;
    }

    // ------------------------------------------------------------------------
    // 2. SEKME: ARKADAŞ ÖNERİLERİ (RecommendationService Kullanılarak Güncellendi)
    // ------------------------------------------------------------------------
    @FXML
    private void handleRecommendationTab() {
        contentArea.getChildren().clear();
        updateButtonStyles(btnRecommend);

        // ARTIK MANUEL HESAPLAMA YOK, SERVİSTEN ÇAĞIRIYORUZ
        List<RecommendationDTO> serviceRecommendations = recommendationService.getTopRecommendations(loggedUser.getUserId());

        if (serviceRecommendations.isEmpty()) {
            contentArea.getChildren().add(new Label("Şu an için size uygun bir arkadaş önerisi bulunamadı."));
        } else {
            // İlk 10 öneriyi gösterelim
            int limit = Math.min(serviceRecommendations.size(), 10);

            for (int i = 0; i < limit; i++) {
                RecommendationDTO rec = serviceRecommendations.get(i);

                // RecommendationDTO'yu UserScoreDTO'ya dönüştürüyoruz ki 'buildScoreRow' metodunu kullanabilelim.
                // mutualFriendNames bilgisini 'status' alanına yazıyoruz.
                UserScoreDTO dto = new UserScoreDTO(
                        rec.userId,
                        rec.username,
                        rec.fullName,
                        rec.score,
                        rec.mutualFriendNames // "Ahmet ile arkadaş" bilgisini buraya basıyoruz
                );

                contentArea.getChildren().add(buildScoreRow(dto));
            }
        }
    }

    // ------------------------------------------------------------------------
    // 3. SEKME: GRAFİK GÖRÜNÜMÜ
    // ------------------------------------------------------------------------
    @FXML
    private void handleGraphTab() {
        contentArea.getChildren().clear();
        updateButtonStyles(btnShowGraph);

        javafx.scene.canvas.Canvas canvas = new javafx.scene.canvas.Canvas(900, 700);
        javafx.scene.canvas.GraphicsContext gc = canvas.getGraphicsContext2D();

        List<Users> allUsers = userService.getAllUsersExceptMe(0);
        List<RelationRaw> allRelations = relationService.getAllRelationsRaw();

        Map<Integer, javafx.geometry.Point2D> userPositions = new HashMap<>();
        double centerX = 450, centerY = 350, radius = 250;

        for (int i = 0; i < allUsers.size(); i++) {
            double angle = 2 * Math.PI * i / allUsers.size();
            double x = centerX + radius * Math.cos(angle);
            double y = centerY + radius * Math.sin(angle);
            userPositions.put(allUsers.get(i).getUserId(), new javafx.geometry.Point2D(x, y));
        }

        for (RelationRaw rel : allRelations) {
            javafx.geometry.Point2D p1 = userPositions.get(rel.user1);
            javafx.geometry.Point2D p2 = userPositions.get(rel.user2);

            if (p1 != null && p2 != null) {
                if (rel.status == 2) {
                    gc.setStroke(javafx.scene.paint.Color.BLUE);
                    gc.setLineWidth(3.0);
                } else if (rel.status == 1) {
                    gc.setStroke(javafx.scene.paint.Color.GREEN);
                    gc.setLineWidth(1.5);
                } else {
                    gc.setStroke(javafx.scene.paint.Color.LIGHTGRAY);
                    gc.setLineWidth(1.0);
                }
                gc.strokeLine(p1.getX(), p1.getY(), p2.getX(), p2.getY());
            }
        }

        for (Users user : allUsers) {
            javafx.geometry.Point2D p = userPositions.get(user.getUserId());
            if (user.getUserId() == loggedUser.getUserId()) {
                gc.setFill(javafx.scene.paint.Color.GOLD);
            } else {
                gc.setFill(javafx.scene.paint.Color.WHITE);
            }
            gc.setStroke(javafx.scene.paint.Color.BLACK);
            gc.setLineWidth(1.0);
            gc.fillOval(p.getX() - 15, p.getY() - 15, 30, 30);
            gc.strokeOval(p.getX() - 15, p.getY() - 15, 30, 30);
            gc.setFill(javafx.scene.paint.Color.BLACK);
            gc.setFont(javafx.scene.text.Font.font("System", 12));
            gc.fillText(user.getUsername(), p.getX() - 20, p.getY() - 20);
        }
        contentArea.getChildren().add(canvas);
    }

    // ------------------------------------------------------------------------
    // YARDIMCI METODLAR (UI OLUŞTURMA)
    // ------------------------------------------------------------------------

    private HBox buildScoreRow(UserScoreDTO dto) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12, 15, 12, 15));
        row.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-color: #f0f0f0; -fx-border-width: 0 0 1 0;");

        // Profil İkonu
        Circle circle = new Circle(18, Color.web("#E1E8ED"));
        StackPane avatar = new StackPane(circle);
        String initial = dto.getUsername() != null && !dto.getUsername().isEmpty() ? dto.getUsername().substring(0, 1).toUpperCase() : "?";
        Label lblInitial = new Label(initial);
        lblInitial.setStyle("-fx-font-weight: bold; -fx-text-fill: #657786;");
        avatar.getChildren().add(lblInitial);

        // İsimler
        VBox nameBox = new VBox(2);
        Label lblName = new Label(dto.getFullName() != null && !dto.getFullName().isEmpty() ? dto.getFullName() : dto.getUsername());
        lblName.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        // Status: İlişki durumunda "Arkadaş", Önerilerde "Ahmet ile arkadaş" yazar
        Label lblStatus = new Label(dto.getRelationshipStatus());
        lblStatus.setStyle("-fx-font-size: 11px; -fx-text-fill: #8e8e8e; -fx-font-style: italic;");
        nameBox.getChildren().addAll(lblName, lblStatus);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Puan
        Label lblScore = new Label(String.format("%.0f Puan", dto.getTotalScore()));
        lblScore.setStyle("-fx-background-color: #EDF7FF; -fx-text-fill: #0095F6; -fx-font-weight: bold; -fx-padding: 5 12; -fx-background-radius: 15;");

        row.getChildren().addAll(avatar, nameBox, spacer, lblScore);
        return row;
    }

    private void updateButtonStyles(Button activeBtn) {
        btnRelationshipScore.getStyleClass().removeAll("menu-button-active");
        btnRelationshipScore.getStyleClass().add("menu-button");

        btnShowGraph.getStyleClass().removeAll("menu-button-active");
        btnShowGraph.getStyleClass().add("menu-button");

        btnRecommend.getStyleClass().removeAll("menu-button-active");
        btnRecommend.getStyleClass().add("menu-button");

        activeBtn.getStyleClass().removeAll("menu-button");
        activeBtn.getStyleClass().add("menu-button-active");
    }
}