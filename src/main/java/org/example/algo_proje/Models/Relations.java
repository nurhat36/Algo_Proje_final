package org.example.algo_proje.Models;

public class Relations {
    private int Id;
    private int User1Id; // İstek gönderen
    private int User2Id; // İstek alan
    private int Status; // 0: Pending, 1: Approved, 2: Rejected/Blocked
    private int RelationshipType; // 1: Normal Friend, 2: Close Friend
}
