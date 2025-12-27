package org.example.algo_proje.Models;

import java.sql.Timestamp;

public class Comments {
    private int id;
    private int shareId;
    private int userId;
    private Integer parentCommentId; // Null olabilir
    private String content;
    private Timestamp createdAt;

    // Yazar bilgilerini UI'da göstermek için (DTO mantığıyla)
    private String authorName;
    private String parentAuthorName; // Yanıt verilen (Yeni alan)

    // Getter ve Setter
    public String getParentAuthorName() {
        return parentAuthorName;
    }

    public void setParentAuthorName(String parentAuthorName) {
        this.parentAuthorName = parentAuthorName;
    }

    public String getAuthorName() {
        return authorName;
    }

    public String getContent() {
        return content;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setShareId(int shareId) {
        this.shareId = shareId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setParentCommentId(int parentId) {
        this.parentCommentId = parentId;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public void setAuthorName(String s) {
        this.authorName = s;
    }

    public int getId() {
        return id;
    }

    public int getParentCommentId() {
        return parentCommentId;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }
}
