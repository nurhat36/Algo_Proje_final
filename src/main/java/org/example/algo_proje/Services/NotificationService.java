package org.example.algo_proje.Services;

import org.example.algo_proje.Models.Notifications;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class NotificationService {
    public static boolean createNotification(int receiverId, int senderId, int type, String content) {
        String sql = "INSERT INTO Notifications (ReceiverId, SenderId, NotificationType, Content, IsRead, CreatedAt) VALUES (?, ?, ?, ?, 0, GETDATE())";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, receiverId);
            ps.setInt(2, senderId);
            ps.setInt(3, type);
            ps.setString(4, content);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    public List<Notifications> getUnreadNotifications(int userId) {
        List<Notifications> notifications = new ArrayList<>();

        // SQL: ReceiverId'si eşleşen ve IsRead=0 olan kayıtları en yeniden eskiye sırala.
        String sql = "SELECT * FROM Notifications WHERE ReceiverId = ? AND IsRead = 0 ORDER BY CreatedAt DESC";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Notifications notif = new Notifications();

                    // Model alanlarını doldurma (Sizin Notifications modelinizdeki getter/setter'lara uygun)
                    notif.setId(rs.getInt("Id"));
                    notif.setReceiverId(rs.getInt("ReceiverId"));
                    notif.setSenderId(rs.getInt("SenderId"));
                    notif.setNotificationType(rs.getInt("NotificationType"));
                    notif.setContent(rs.getString("Content"));
                    notif.setIsRead(rs.getBoolean("IsRead"));
                    notif.setCreatedAt(rs.getTimestamp("CreatedAt"));

                    notifications.add(notif);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Bildirimler yüklenirken hata oluştu.");
        }
        return notifications;
    }

    /**
     * Belirtilen bildirimi okundu (IsRead = 1) olarak işaretler.
     *
     * @param notificationId Okundu olarak işaretlenecek bildirimin ID'si.
     * @return Başarılıysa true.
     */
    public boolean markAsRead(int notificationId) {
        String sql = "UPDATE Notifications SET IsRead = 1 WHERE Id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, notificationId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
