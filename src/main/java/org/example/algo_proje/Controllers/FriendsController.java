package org.example.algo_proje.Controllers;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import org.example.algo_proje.Models.DTOs.UserScoreDTO;
import org.example.algo_proje.Models.Raws.CommentRaw;
import org.example.algo_proje.Models.Raws.LikeRaw;
import org.example.algo_proje.Models.Raws.RelationRaw;
import org.example.algo_proje.Models.Users;
import org.example.algo_proje.Services.Database;
import org.example.algo_proje.Services.RelationService;
import org.example.algo_proje.Services.ShareService;
import org.example.algo_proje.Services.UserService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class FriendsController {

    @FXML private VBox contentArea;
    @FXML private Button btnRelationshipScore;
    @FXML private Button btnShowGraph;
    private RelationService relationService=new RelationService();
    private UserService userService=new UserService();

    private Users loggedUser;
    private MainFeedController mainController; // Gerekirse ana sayfaya dönmek için

    /**
     * MainFeedController'dan gelen veriyi initialize eder.
     */
    public void initData(Users user) {
        this.loggedUser = user;
        handleScoreTab(); // Sayfa açıldığında otomatik puanları yükle
    }

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

    private List<UserScoreDTO> calculateRelationshipScores() {
        // 1. ADIM: Tüm verileri DB'den ham olarak çek
        UserService userService = new UserService();
        ShareService shareService = new ShareService();
        RelationService relationService = new RelationService();

        // Servislerden ham verileri çek
        List<Users> allUsers = userService.getAllUsersExceptMe(loggedUser.getUserId());
        List<RelationRaw> allRelations = relationService.getAllRelationsRaw();
        List<LikeRaw> allLikes = shareService.getAllLikesRaw();
        List<CommentRaw> allComments = shareService.getAllCommentsRaw();

        List<UserScoreDTO> scoreList = new ArrayList<>();

        // 2. ADIM: İLKEL DÖNGÜ VE HESAPLAMA (İlkel dizi/liste mantığıyla)
        for (int i = 0; i < allUsers.size(); i++) {
            Users otherUser = allUsers.get(i);
            int otherId = otherUser.getUserId();

            double currentTotalScore = 0;
            String statusText = "Tanıdık";

            // A. İlişki Durumu Puanı (İlkel Döngü)
            for (int j = 0; j < allRelations.size(); j++) {
                RelationRaw rel = allRelations.get(j);
                if ((rel.user1 == loggedUser.getUserId() && rel.user2 == otherId) ||
                        (rel.user2 == loggedUser.getUserId() && rel.user1 == otherId)) {

                    if (rel.status == 2) {
                        currentTotalScore += 30; // Yakın Arkadaş
                        statusText = "Yakın Arkadaş";
                    } else if (rel.status == 1) {
                        currentTotalScore += 15; // Arkadaş
                        statusText = "Arkadaş";
                    }
                    break; // İlişki bulundu, döngüden çık
                }
            }

            // B. Beğeni Puanı (İlkel Döngü)
            // Benim paylaşımlarımı bu kullanıcı kaç kere beğenmiş?
            for (int k = 0; k < allLikes.size(); k++) {
                LikeRaw like = allLikes.get(k);
                if (like.postOwnerId == loggedUser.getUserId() && like.likerId == otherId) {
                    currentTotalScore += 1.0; // Her beğeni +1 puan
                }
            }

            // C. Yorum Puanı (İlkel Döngü)
            // Benim paylaşımlarıma bu kullanıcı kaç yorum yapmış?
            for (int m = 0; m < allComments.size(); m++) {
                CommentRaw comment = allComments.get(m);
                if (comment.postOwnerId == loggedUser.getUserId() && comment.commenterId == otherId) {
                    currentTotalScore += 1.0; // Her yorum +1 puan
                }
            }

            // Skor 0'dan büyükse listeye ekle
            if (currentTotalScore > 0) {
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

    private HBox buildScoreRow(UserScoreDTO dto) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12, 15, 12, 15));
        row.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-color: #f0f0f0; -fx-border-width: 0 0 1 0;");

        // Profil İkonu
        Circle circle = new Circle(18, Color.web("#E1E8ED"));
        StackPane avatar = new StackPane(circle);
        Label lblInitial = new Label(dto.getUsername().substring(0, 1).toUpperCase());
        lblInitial.setStyle("-fx-font-weight: bold; -fx-text-fill: #657786;");
        avatar.getChildren().add(lblInitial);

        // İsimler
        VBox nameBox = new VBox(2);
        Label lblName = new Label(dto.getFullName() != null && !dto.getFullName().isEmpty() ? dto.getFullName() : dto.getUsername());
        lblName.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        Label lblStatus = new Label(dto.getRelationshipStatus());
        lblStatus.setStyle("-fx-font-size: 11px; -fx-text-fill: #8e8e8e; -fx-font-style: italic;");
        nameBox.getChildren().addAll(lblName, lblStatus);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Puan
        Label lblScore = new Label(String.format("%.1f", dto.getTotalScore()));
        lblScore.setStyle("-fx-background-color: #EDF7FF; -fx-text-fill: #0095F6; -fx-font-weight: bold; -fx-padding: 5 12; -fx-background-radius: 15;");

        row.getChildren().addAll(avatar, nameBox, spacer, lblScore);
        return row;
    }

    private void updateButtonStyles(Button activeBtn) {
        // Tüm butonları pasif yap, sadece seçileni aktif stiline sok
        btnRelationshipScore.getStyleClass().removeAll("menu-button-active");
        btnRelationshipScore.getStyleClass().add("menu-button");

        activeBtn.getStyleClass().removeAll("menu-button");
        activeBtn.getStyleClass().add("menu-button-active");
    }


    @FXML
    private void handleGraphTab() {
        contentArea.getChildren().clear();
        updateButtonStyles(btnShowGraph);

        // Grafik alanı oluştur (ScrollPane içinde olduğu için geniş tutabiliriz)
        javafx.scene.canvas.Canvas canvas = new javafx.scene.canvas.Canvas(900, 700);
        javafx.scene.canvas.GraphicsContext gc = canvas.getGraphicsContext2D();

        // Servislerden ilkel verileri çek
        UserService userService = new UserService();
        RelationService relationService = new RelationService();

        List<Users> allUsers = userService.getAllUsersExceptMe(0); // Tüm sistem kullanıcıları
        List<RelationRaw> allRelations = relationService.getAllRelationsRaw();

        // 1. Düğümleri (Kullanıcıları) Matematiksel Yerleştir
        Map<Integer, javafx.geometry.Point2D> userPositions = new HashMap<>();
        double centerX = 450, centerY = 350, radius = 250;

        for (int i = 0; i < allUsers.size(); i++) {
            double angle = 2 * Math.PI * i / allUsers.size();
            double x = centerX + radius * Math.cos(angle);
            double y = centerY + radius * Math.sin(angle);
            userPositions.put(allUsers.get(i).getUserId(), new javafx.geometry.Point2D(x, y));
        }

        // 2. İlişkileri (Çizgileri) RENKLİ Çiz
        for (RelationRaw rel : allRelations) {
            javafx.geometry.Point2D p1 = userPositions.get(rel.user1);
            javafx.geometry.Point2D p2 = userPositions.get(rel.user2);

            if (p1 != null && p2 != null) {
                // İLİKSEL RENK MANTIĞI (İlkel If-Else Yapısı)
                if (rel.status == 2) {
                    // Yakın Arkadaş - Kalın ve Mavi
                    gc.setStroke(javafx.scene.paint.Color.BLUE);
                    gc.setLineWidth(3.0);
                } else if (rel.status == 1) {
                    // Normal Arkadaş - Orta ve Yeşil
                    gc.setStroke(javafx.scene.paint.Color.GREEN);
                    gc.setLineWidth(1.5);
                } else {
                    // Tanıdık/Beklemede - İnce ve Gri
                    gc.setStroke(javafx.scene.paint.Color.LIGHTGRAY);
                    gc.setLineWidth(1.0);
                }

                // Çizgiyi çiz
                gc.strokeLine(p1.getX(), p1.getY(), p2.getX(), p2.getY());
            }
        }

        // 3. Kullanıcı Düğüm Noktalarını Çiz
        for (Users user : allUsers) {
            javafx.geometry.Point2D p = userPositions.get(user.getUserId());

            // Eğer bu kullanıcı "Ben" isem rengim farklı olsun
            if (user.getUserId() == loggedUser.getUserId()) {
                gc.setFill(javafx.scene.paint.Color.GOLD); // Ben
            } else {
                gc.setFill(javafx.scene.paint.Color.WHITE);
            }

            gc.setStroke(javafx.scene.paint.Color.BLACK);
            gc.setLineWidth(1.0);

            // Daireyi ve İsmi çiz
            gc.fillOval(p.getX() - 15, p.getY() - 15, 30, 30);
            gc.strokeOval(p.getX() - 15, p.getY() - 15, 30, 30);

            gc.setFill(javafx.scene.paint.Color.BLACK);
            gc.setFont(javafx.scene.text.Font.font("System", 12));
            gc.fillText(user.getUsername(), p.getX() - 20, p.getY() - 20);
        }

        contentArea.getChildren().add(canvas);
    }
}

