package org.example.algo_proje.Services;

import org.example.algo_proje.Models.Comments;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CommentService {
    public boolean addComment(int shareId, int userId, String content, Integer parentId) {
        String sql = "INSERT INTO Comments (ShareId, UserId, Content, ParentCommentId) VALUES (?, ?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, shareId);
            ps.setInt(2, userId);
            ps.setString(3, content);
            if (parentId != null) ps.setInt(4, parentId); else ps.setNull(4, java.sql.Types.INTEGER);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public List<Comments> getCommentsByShareId(int shareId) {
        List<Comments> list = new ArrayList<>();

        // SQL Sorgusu:
        // u1 -> Yorumu yapan kişi (Author)
        // u2 -> Yanıt verilen yorumun sahibi (Parent Author) - LEFT JOIN çünkü ana yorumların parent'ı yoktur.
        String sql = "SELECT c.*, " +
                "u1.FullName AS AuthorName, u1.Username AS AuthorUsername, " +
                "u2.FullName AS ParentAuthorName, u2.Username AS ParentAuthorUsername " +
                "FROM Comments c " +
                "JOIN Users u1 ON c.UserId = u1.UserId " +
                "LEFT JOIN Comments c_parent ON c.ParentCommentId = c_parent.Id " +
                "LEFT JOIN Users u2 ON c_parent.UserId = u2.UserId " +
                "WHERE c.ShareId = ? AND ISNULL(c.IsDeleted, 0) = 0 " +
                "ORDER BY c.CreatedAt ASC";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, shareId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Comments comment = new Comments();

                    // 1. Temel Bilgiler
                    comment.setId(rs.getInt("Id"));
                    comment.setShareId(rs.getInt("ShareId"));
                    comment.setUserId(rs.getInt("UserId"));
                    comment.setContent(rs.getString("Content"));
                    comment.setCreatedAt(rs.getTimestamp("CreatedAt"));

                    // 2. Parent ID Kontrolü
                    int parentId = rs.getInt("ParentCommentId");
                    if (!rs.wasNull()) {
                        comment.setParentCommentId(parentId);
                    } else {
                        comment.setParentCommentId(0); // Modelde Integer kullanıyorsan null daha iyidir
                    }

                    // 3. Yorumu Yapan Kişinin Adı (AuthorName)
                    String fName = rs.getString("AuthorName");
                    String uName = rs.getString("AuthorUsername");
                    comment.setAuthorName((fName != null && !fName.isEmpty()) ? fName : uName);

                    // 4. Yanıt Verilen Kişinin Adı (ParentAuthorName)
                    // Bu değer sadece cevaplar (reply) için dolu gelecek, ana yorumlar için null kalacaktır.
                    String pfName = rs.getString("ParentAuthorName");
                    String puName = rs.getString("ParentAuthorUsername");

                    if (pfName != null && !pfName.isEmpty()) {
                        comment.setParentAuthorName(pfName);
                    } else if (puName != null && !puName.isEmpty()) {
                        comment.setParentAuthorName(puName);
                    } else {
                        comment.setParentAuthorName(null);
                    }

                    list.add(comment);
                }
            }
        } catch (SQLException e) {
            System.err.println("Yorumlar çekilirken SQL hatası: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
}
