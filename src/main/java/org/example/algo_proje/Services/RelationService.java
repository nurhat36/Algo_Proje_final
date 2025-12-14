package org.example.algo_proje.Services;

import org.example.algo_proje.Models.Users;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RelationService {

    // İlişki Durumu Sabitleri (Constants)
    public static final int STATUS_PENDING = 0;   // İstek gönderildi, bekleniyor
    public static final int STATUS_APPROVED = 1;  // Arkadaşlık onaylandı
    public static final int STATUS_REJECTED = 2;  // İstek reddedildi/Engellendi

    // İlişki Türü Sabitleri (Relationship Types)
    public static final int TYPE_NORMAL_FRIEND = 1; // Normal arkadaş
    public static final int TYPE_CLOSE_FRIEND = 2;  // Yakın arkadaş

    /**
     * Kullanıcı A'dan Kullanıcı B'ye yeni bir arkadaşlık isteği gönderir.
     * Bu metot, her zaman isteği User1Id -> User2Id şeklinde tek yönlü kaydeder.
     *
     * @param senderId İstek gönderen (User1Id)
     * @param receiverId İstek alan (User2Id)
     * @return İşlem başarılıysa true, zaten ilişki varsa veya hata oluşursa false.
     */
    public boolean sendFriendRequest(int senderId, int receiverId) {
        if (senderId == receiverId) return false;

        // Önce, ilişkinin zaten mevcut olup olmadığını kontrol et
        if (checkRelationStatus(senderId, receiverId) != STATUS_NOT_FRIENDS) {
            // Zaten arkadaş, beklemede veya engelli
            System.out.println("HATA: İstek zaten gönderilmiş veya kullanıcılar zaten arkadaş.");
            return false;
        }

        String sql = "INSERT INTO Relations (User1Id, User2Id, Status, RelationshipType) VALUES (?, ?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, senderId);
            ps.setInt(2, receiverId);
            ps.setInt(3, STATUS_PENDING);
            ps.setInt(4, TYPE_NORMAL_FRIEND); // Varsayılan: Normal arkadaş

            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Bekleyen arkadaşlık isteğini onaylar (Status = 1).
     *
     * @param receiverId Onayı yapan (istek alan) kullanıcı ID'si
     * @param senderId İsteği gönderen kullanıcı ID'si
     * @return Başarılıysa true, hata varsa veya bekleyen istek yoksa false.
     */
    public boolean acceptFriendRequest(int receiverId, int senderId) {
        // SQL: receiverId'ye gelen ve Status'ü 0 (Pending) olan isteği bul
        String sql = "UPDATE Relations SET Status = ? WHERE User1Id = ? AND User2Id = ? AND Status = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, STATUS_APPROVED);
            ps.setInt(2, senderId);
            ps.setInt(3, receiverId);
            ps.setInt(4, STATUS_PENDING);

            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Arkadaşlık isteğini reddeder veya mevcut ilişkiyi siler (Reddedildi/Engellendi Status = 2).
     *
     * @param user1 İşlemi yapan kullanıcı ID'si
     * @param user2 İşlemin uygulandığı kullanıcı ID'si
     * @return Başarılıysa true.
     */
    public boolean rejectRelation(int user1, int user2) {
        // Reddetme veya engelleme, isteğin yönüne göre yapılmalıdır.
        // Burada basitçe var olan kaydı STATUS_REJECTED (2) yapalım.
        // Eğer zaten onaylanmış ilişkiyse, onu da STATUS_REJECTED yapar.
        String updateSql = "UPDATE Relations SET Status = ? WHERE (User1Id = ? AND User2Id = ?) OR (User1Id = ? AND User2Id = ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(updateSql)) {

            ps.setInt(1, STATUS_REJECTED);
            ps.setInt(2, user1);
            ps.setInt(3, user2);
            ps.setInt(4, user2);
            ps.setInt(5, user1);

            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Arkadaş Olmayan (Boş) durumu için özel bir değer
    public static final int STATUS_NOT_FRIENDS = -1;

    /**
     * İki kullanıcı arasındaki ilişki durumunu kontrol eder.
     * İlişkiler tablosu sadece tek bir kayıt tuttuğu için iki yönü de kontrol eder.
     *
     * @param id1 Birinci kullanıcı ID'si
     * @param id2 İkinci kullanıcı ID'si
     * @return İlişkinin Status kodu (0, 1, 2) veya hiçbiri yoksa -1.
     */
    public int checkRelationStatus(int id1, int id2) {
        String sql = "SELECT Status FROM Relations WHERE (User1Id = ? AND User2Id = ?) OR (User1Id = ? AND User2Id = ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id1);
            ps.setInt(2, id2);
            ps.setInt(3, id2); // Ters yönde de kontrol et
            ps.setInt(4, id1);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("Status");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return STATUS_NOT_FRIENDS; // İlişki yok
    }

    /**
     * Belirtilen kullanıcının onaylanmış tüm arkadaşlarını getirir.
     *
     * @param userId Arkadaşları getirilecek kullanıcı ID'si
     * @return Arkadaş Users nesnelerinin listesi
     */
    public List<Users> getFriends(int userId) {
        List<Users> friends = new ArrayList<>();
        // İlişkiyi hem User1Id hem de User2Id olarak kontrol et
        String sql = """
                 SELECT u.* FROM Relations r
                 JOIN Users u ON u.UserId = CASE 
                    WHEN r.User1Id = ? THEN r.User2Id 
                    ELSE r.User1Id 
                 END
                 WHERE (r.User1Id = ? OR r.User2Id = ?) AND r.Status = ?
                 """;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, userId);
            ps.setInt(3, userId);
            ps.setInt(4, STATUS_APPROVED);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Users friend = new Users();
                    // Users modelinize göre buradaki alanları doldurun
                    friend.setUserId(rs.getInt("UserId"));
                    friend.setUsername(rs.getString("Username"));
                    friend.setFullName(rs.getString("FullName"));
                    // ... Diğer alanlar ...
                    friends.add(friend);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return friends;
    }
}