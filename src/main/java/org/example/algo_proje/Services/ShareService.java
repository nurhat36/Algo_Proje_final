package org.example.algo_proje.Services;

import org.example.algo_proje.Models.DTOs.PostDTO;
import org.example.algo_proje.Models.Raws.CommentRaw;
import org.example.algo_proje.Models.Raws.LikeRaw;
import org.example.algo_proje.Models.Shares;
import org.example.algo_proje.Models.Users;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ShareService {

    public static boolean addShare(Shares s) {
        String sql = "INSERT INTO Shares (UserId, Title, Description, Path, IsImage) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, s.getUserId());
            ps.setString(2, s.getTitle());
            ps.setString(3, s.getDescription());
            ps.setString(4, s.getPath());
            ps.setBoolean(5, s.isImage());

            int ok = ps.executeUpdate();
            if (ok == 0) return false;

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) s.setId(keys.getInt(1));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<Shares> getAllSharesWithLikeCount() {
        List<Shares> list = new ArrayList<>();
        String sql = """
            SELECT s.*, ISNULL(l.LikeCount,0) AS LikeCount
            FROM Shares s
            LEFT JOIN (
                SELECT ShareId, COUNT(*) AS LikeCount
                FROM ShareLikes
                GROUP BY ShareId
            ) l ON l.ShareId = s.Id
            WHERE s.IsDeleted = 0
            ORDER BY s.CreatedAt DESC
        """;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Shares s = new Shares();
                s.setId(rs.getInt("Id"));
                s.setUserId(rs.getInt("UserID"));
                s.setTitle(rs.getString("Title"));
                s.setDescription(rs.getString("Description"));
                s.setPath(rs.getString("Path"));
                s.setImage(rs.getBoolean("IsImage"));
                s.setCreatedAt(rs.getTimestamp("CreatedAt"));
                s.setDeleted(rs.getBoolean("IsDeleted"));

                list.add(s);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static Shares getShareById(int id) {
        String sql = "SELECT * FROM Shares WHERE Id = ? AND IsDeleted = 0";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Shares s = new Shares();
                s.setId(rs.getInt("Id"));
                s.setUserId(rs.getInt("UserId"));
                s.setTitle(rs.getString("Title"));
                s.setDescription(rs.getString("Description"));
                s.setPath(rs.getString("Path"));
                s.setImage(rs.getBoolean("IsImage"));
                s.setCreatedAt(rs.getTimestamp("CreatedAt"));
                s.setDeleted(rs.getBoolean("IsDeleted"));
                return s;
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public static boolean updateShare(Shares s) {
        String sql = "UPDATE Shares SET Title=?, Description=?, Path=?, IsImage=? WHERE Id=?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, s.getTitle());
            ps.setString(2, s.getDescription());
            ps.setString(3, s.getPath());
            ps.setBoolean(4, s.isImage());
            ps.setInt(5, s.getId());
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public static boolean deleteShare(int id) {
        String sql = "UPDATE Shares SET IsDeleted = 1 WHERE Id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public static boolean likeShare(int shareId, int userId) {
        String sql = "INSERT INTO ShareLikes (ShareId, UserId) VALUES (?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, shareId);
            ps.setInt(2, userId);
            ps.executeUpdate();
            return true;
        } catch (SQLIntegrityConstraintViolationException dup) {

            return false;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }


    public static boolean unlikeShare(int shareId, int userId) {
        String sql = "DELETE FROM ShareLikes WHERE ShareId=? AND UserId=?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, shareId);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }


    public static int getLikeCount(int shareId) {
        String sql = "SELECT COUNT(*) FROM ShareLikes WHERE ShareId=?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, shareId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }


    public static List<Users> getLikers(int shareId) {
        List<Users> list = new ArrayList<>();
        String sql = "SELECT u.* FROM ShareLikes pl JOIN Users u ON u.UserId = pl.UserId WHERE pl.ShareId=? ORDER BY pl.LikedAt DESC";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, shareId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Users u = new Users();
                u.setUserId(rs.getInt("UserId"));
                u.setUsername(rs.getString("Username"));
                u.setFullName(rs.getString("FullName"));
                // set diğer alanlar istersen
                list.add(u);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }
    //Keşfet
    public List<PostDTO> getExplorePosts() {

        List<PostDTO> posts = new ArrayList<>();

        String sql = "SELECT * FROM Shares WHERE IsDeleted = 0";

        try (
                Connection conn = Database.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {

            while (rs.next()) {

                PostDTO post = new PostDTO();

                post.shareId     = rs.getInt("Id");
                post.shareUserId = rs.getInt("UserID");
                post.description = rs.getString("Description");
                post.path        = rs.getString("Path");
                post.isImage     = rs.getBoolean("IsImage");
                post.createdAt   = rs.getTimestamp("CreatedAt");

                Users author = UserService.getUserById(post.shareUserId);

                if (author != null) {
                    post.authorUserId       = author.getUserId();
                    post.authorUsername     = author.getUsername();
                    post.authorFullName     = author.getFullName();
                    post.authorAvatarBytes  = author.getAvatar();
                }

                posts.add(post);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return posts;
    }

    // Beğeni ham verileri
    public List<LikeRaw> getAllLikesRaw() {
        List<LikeRaw> list = new ArrayList<>();
        // S -> Paylaşım sahibi, L -> Beğenen kişi
        String sql = "SELECT s.UserId as OwnerId, l.UserId as LikerId FROM Likes l " +
                "JOIN Shares s ON l.ShareId = s.Id";
        try (Connection conn = Database.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                LikeRaw lr = new LikeRaw();
                lr.postOwnerId = rs.getInt("OwnerId");
                lr.likerId = rs.getInt("LikerId");
                list.add(lr);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // Yorum ham verileri
    public List<CommentRaw> getAllCommentsRaw() {
        List<CommentRaw> list = new ArrayList<>();
        // S -> Paylaşım sahibi, C -> Yorum yapan kişi
        String sql = "SELECT s.UserId as OwnerId, c.UserId as CommenterId FROM Comments c " +
                "JOIN Shares s ON c.ShareId = s.Id";
        try (Connection conn = Database.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                CommentRaw cr = new CommentRaw();
                cr.postOwnerId = rs.getInt("OwnerId");
                cr.commenterId = rs.getInt("CommenterId");
                list.add(cr);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }
}
