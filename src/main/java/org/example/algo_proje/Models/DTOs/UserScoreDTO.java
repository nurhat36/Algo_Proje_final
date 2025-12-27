package org.example.algo_proje.Models.DTOs;

public class UserScoreDTO {
    private int userId;
    private String username;
    private String fullName;
    private double totalScore;
    private String relationshipStatus; // Yakın Arkadaş, Arkadaş, Arkadaş Değil

    public UserScoreDTO(int userId, String username, String fullName, double totalScore, String relationshipStatus) {
        this.userId = userId;
        this.username = username;
        this.fullName = fullName;
        this.totalScore = totalScore;
        this.relationshipStatus = relationshipStatus;
    }

    // Getter Metotları
    public int getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getFullName() { return fullName; }
    public double getTotalScore() { return totalScore; }
    public String getRelationshipStatus() { return relationshipStatus; }


}