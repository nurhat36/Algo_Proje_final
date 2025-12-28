package org.example.algo_proje.Services;

import org.example.algo_proje.Models.DTOs.RecommendationDTO;
import org.example.algo_proje.Models.Raws.*;
import org.example.algo_proje.Models.Users;

import java.util.*;
import java.util.stream.Collectors;

public class RecommendationService {

    public List<RecommendationDTO> getTopRecommendations(int myId) {
        UserService userService = new UserService();
        RelationService relationService = new RelationService();
        ShareService shareService = new ShareService();

        // Verileri Çek
        List<Users> allUsers = userService.getAllUsersExceptMe(myId);
        List<RelationRaw> allRelations = relationService.getAllRelationsRaw();
        List<LikeRaw> allLikes = shareService.getAllLikesRaw();
        List<CommentRaw> allComments = shareService.getAllCommentsRaw();

        // 1. Mevcut Arkadaşlarımı Bul (ID -> İsim haritası yapalım ki ismini yazabilelim)
        Map<Integer, String> myFriendsMap = new HashMap<>();

        for (RelationRaw rel : allRelations) {
            if (rel.status > 0) {
                if (rel.user1 == myId) myFriendsMap.put(rel.user2, getUserNameById(allUsers, rel.user2));
                else if (rel.user2 == myId) myFriendsMap.put(rel.user1, getUserNameById(allUsers, rel.user1));
            }
        }

        List<RecommendationDTO> results = new ArrayList<>();

        // 2. Adayları Gez
        for (Users candidate : allUsers) {
            int candidateId = candidate.getUserId();
            if (myFriendsMap.containsKey(candidateId) || candidateId == myId) continue;

            double score = 0;
            List<String> mutualFriends = new ArrayList<>();

            // A. Benimle Etkileşimi
            score += calculateInteraction(myId, candidateId, allRelations, allLikes, allComments);

            // B. Ortak Arkadaşlar (Dostumun Dostu)
            for (Map.Entry<Integer, String> entry : myFriendsMap.entrySet()) {
                int friendId = entry.getKey();
                String friendName = entry.getValue();

                double friendScore = calculateInteraction(friendId, candidateId, allRelations, allLikes, allComments);

                if (friendScore > 0) {
                    score += friendScore;
                    // Eğer bu arkadaşla adayın arası iyiyse, ortak arkadaş listesine ekle
                    mutualFriends.add(friendName);
                }
            }

            if (score > 0) {
                // Ortak arkadaşların ilk 2'sini al, gerisine "+2 diğer" yaz
                String mutualsText = formatMutuals(mutualFriends);

                results.add(new RecommendationDTO(
                        candidateId,
                        candidate.getUsername(),
                        candidate.getFullName(),
                        score,
                        mutualsText
                ));
            }
        }

        // Puana göre sırala
        results.sort((a, b) -> Double.compare(b.score, a.score));
        return results;
    }

    // Yardımcı: ID'den isim bulma
    private String getUserNameById(List<Users> users, int id) {
        return users.stream().filter(u -> u.getUserId() == id).findFirst()
                .map(Users::getFullName).orElse("Kullanıcı");
    }

    // Yardımcı: "Ahmet, Mehmet ve 3 diğer kişi" formatı
    private String formatMutuals(List<String> names) {
        if (names.isEmpty()) return "Ortak etkileşim";
        if (names.size() == 1) return names.get(0) + " ile arkadaş";
        if (names.size() == 2) return names.get(0) + " ve " + names.get(1) + " ile arkadaş";
        return names.get(0) + ", " + names.get(1) + " ve " + (names.size()-2) + " diğer kişiyle arkadaş";
    }

    // Yardımcı: Puan Hesapla
    private double calculateInteraction(int u1, int u2, List<RelationRaw> rels, List<LikeRaw> likes, List<CommentRaw> comments) {
        double s = 0;
        for (RelationRaw r : rels) {
            if ((r.user1 == u1 && r.user2 == u2) || (r.user2 == u1 && r.user1 == u2)) {
                s += (r.status == 2) ? 30 : 15;
            }
        }
        for (LikeRaw l : likes) {
            if ((l.postOwnerId == u1 && l.likerId == u2) || (l.postOwnerId == u2 && l.likerId == u1)) s += 5;
        }
        for (CommentRaw c : comments) {
            if ((c.postOwnerId == u1 && c.commenterId == u2) || (c.postOwnerId == u2 && c.commenterId == u1)) s += 10;
        }
        return s;
    }
}