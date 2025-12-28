package org.example.algo_proje.Models.DTOs;

public class RecommendationDTO {
    public int userId;
    public String username;
    public String fullName;
    public double score;
    public String mutualFriendNames; // "Ahmet, Mehmet" gibi

    public RecommendationDTO(int userId, String username, String fullName, double score, String mutualFriendNames) {
        this.userId = userId;
        this.username = username;
        this.fullName = fullName;
        this.score = score;
        this.mutualFriendNames = mutualFriendNames;
    }
}