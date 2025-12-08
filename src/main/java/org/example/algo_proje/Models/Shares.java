package org.example.algo_proje.Models;

import java.sql.Timestamp;

public class Shares {
    private int Id;
    private int UserId;
    private String title;
    private String description;
    private String path;
    private boolean isImage;
    private Timestamp createdAt;
    private boolean isDeleted;


    public int getId() { return Id; }
    public void setId(int id) { Id = id; }

    public int getUserId() { return UserId; }
    public void setUserId(int userId) { UserId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public boolean isImage() { return isImage; }
    public void setImage(boolean image) { isImage = image; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { isDeleted = deleted; }
}
