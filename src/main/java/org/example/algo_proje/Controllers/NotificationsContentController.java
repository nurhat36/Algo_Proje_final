package org.example.algo_proje.Controllers;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.example.algo_proje.Models.Notifications;
import org.example.algo_proje.Models.Users;
import org.example.algo_proje.Services.RelationService;
import org.example.algo_proje.Services.NotificationService; // Varsayalım bu servisi de oluşturacağız.

import java.util.List;

public class NotificationsContentController {

    @FXML public VBox notificationContainer;
    private Users loggedUser;
    private final NotificationService notificationService = new NotificationService();
    private final RelationService relationService = new RelationService();

    public void setLoggedUser(Users user) {
        this.loggedUser = user;
        if (loggedUser != null) {
            loadNotifications();
        }
    }

    private void loadNotifications() {
        notificationContainer.getChildren().clear();

        // 1. Kullanıcının tüm okunmamış bildirimlerini çek (NotificationService'de olmalı)
        List<Notifications> notifications = notificationService.getUnreadNotifications(loggedUser.getUserId());

        if (notifications.isEmpty()) {
            notificationContainer.getChildren().add(new Label("Yeni bildiriminiz bulunmamaktadır."));
            return;
        }

        for (Notifications notification : notifications) {
            notificationContainer.getChildren().add(buildNotificationCard(notification));
        }
    }

    private HBox buildNotificationCard(Notifications notif) {
        HBox card = new HBox(10);
        card.setPadding(new Insets(10));
        card.setAlignment(Pos.CENTER_LEFT);

        // Okunmamış ise koyu renkte göster
        String cardStyle = notif.isIsRead() ?
                "-fx-background-color: white;" :
                "-fx-background-color: #eaf6ff; -fx-border-color: #007bff; -fx-border-width: 0 0 0 4;";

        card.setStyle("-fx-background-radius: 5; " + cardStyle);

        // Bildirim İçeriği
        Label content = new Label(notif.getContent());
        content.setWrapText(true);
        HBox.setHgrow(content, Priority.ALWAYS);

        card.getChildren().add(content);

        // Aksiyon Butonları (Sadece Arkadaşlık İstekleri için)
        if (notif.getNotificationType() == 1 /* FriendRequest */) {

            // İstek henüz onaylanmadıysa butonları göster
            if (relationService.checkRelationStatus(notif.getSenderId(), loggedUser.getUserId()) == RelationService.STATUS_PENDING) {

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                card.getChildren().add(spacer);

                Button btnAccept = new Button("Kabul Et");
                btnAccept.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-background-radius: 5;");

                btnAccept.setOnAction(e -> handleAccept(notif, card));

                Button btnReject = new Button("Reddet");
                btnReject.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-background-radius: 5;");

                btnReject.setOnAction(e -> handleReject(notif, card));

                card.getChildren().addAll(btnAccept, btnReject);
            }
        }

        // Okundu olarak işaretle (Karta tıklandığında veya görüntülendiğinde)
        // notificationService.markAsRead(notif.getId());

        return card;
    }

    private void handleAccept(Notifications notif, HBox card) {
        // İsteği gönderen (Sender) ile alıcı (loggedUser) arasındaki ilişkiyi onaylıyoruz
        boolean success = relationService.acceptFriendRequest(loggedUser.getUserId(), notif.getSenderId());
        if (success) {
            // Butonları kaldır ve durumu güncelle
            card.getChildren().clear();
            card.getChildren().add(new Label(notif.getContent() + " - Kabul Edildi."));
            card.setStyle("-fx-background-color: white; -fx-background-radius: 5;");
            // Bildirimi okundu olarak işaretle
            notificationService.markAsRead(notif.getId());
        } else {
            // Hata mesajı
        }
    }

    private void handleReject(Notifications notif, HBox card) {
        // İsteği reddetme/silme işlemi
        boolean success = relationService.rejectRelation(notif.getSenderId(), loggedUser.getUserId());
        if (success) {
            // Butonları kaldır ve durumu güncelle
            card.getChildren().clear();
            card.getChildren().add(new Label(notif.getContent() + " - Reddedildi."));
            card.setStyle("-fx-background-color: white; -fx-background-radius: 5;");
            // Bildirimi okundu olarak işaretle
            notificationService.markAsRead(notif.getId());
        } else {
            // Hata mesajı
        }
    }

    // ... gerekli diğer metotlar (NotificationService'in oluşturulması dahil) ...
}