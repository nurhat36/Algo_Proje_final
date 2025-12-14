package org.example.algo_proje.Models;

import java.sql.Timestamp;

public class Notifications {
    private int Id;
    private int ReceiverId;     // Bildirimi alacak kullanıcı (Giriş yapan kullanıcı)
    private int SenderId;       // Bildirimi tetikleyen kullanıcı (Örn: İstek gönderen)
    private int NotificationType; // 1: Arkadaşlık İsteği, 2: Beğeni, 3: Yorum vb.
    private String Content;     // Bildirim metni (Örn: "X kişisi sana arkadaşlık isteği gönderdi")
    private boolean IsRead;     // Okundu/Okunmadı durumu
    private Timestamp CreatedAt;

    // --- CONSTRUCTOR ---
    public Notifications() {
    }

    // --- GETTERS ---
    public int getId() {
        return Id;
    }

    public int getReceiverId() {
        return ReceiverId;
    }

    public int getSenderId() {
        return SenderId;
    }

    public int getNotificationType() {
        return NotificationType;
    }

    public String getContent() {
        return Content;
    }

    public boolean isIsRead() {
        return IsRead;
    }

    public Timestamp getCreatedAt() {
        return CreatedAt;
    }

    // --- SETTERS ---
    public void setId(int id) {
        Id = id;
    }

    public void setReceiverId(int receiverId) {
        ReceiverId = receiverId;
    }

    public void setSenderId(int senderId) {
        SenderId = senderId;
    }

    public void setNotificationType(int notificationType) {
        NotificationType = notificationType;
    }

    public void setContent(String content) {
        Content = content;
    }

    public void setIsRead(boolean isRead) {
        IsRead = isRead;
    }

    public void setCreatedAt(Timestamp createdAt) {
        CreatedAt = createdAt;
    }
}