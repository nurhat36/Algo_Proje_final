package org.example.algo_proje.Models.DTOs;

import java.sql.Timestamp;

public class PostDTO {
    public int shareId;
    public int shareUserId;
    public String description;
    public String path;
    public boolean isImage;
    public Timestamp createdAt;

    public int authorUserId;
    public String authorUsername;
    public String authorFullName;
    public byte[] authorAvatarBytes;
}
