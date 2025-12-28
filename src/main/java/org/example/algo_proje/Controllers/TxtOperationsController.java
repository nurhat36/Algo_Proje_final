package org.example.algo_proje.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.algo_proje.Models.Users;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class TxtOperationsController {
    @FXML private TextField txtSourceID, txtTargetID;
    @FXML private TextArea txtDisplayArea;
    @FXML private Label lblStatus;
    @FXML private Button btnLoadFiles;

    // Veri Yapıları
    private Map<String, String[]> usersMap = new HashMap<>();
    private List<String[]> begeniList = new ArrayList<>();
    private List<String[]> yorumList = new ArrayList<>();
    private List<String> userIDs = new ArrayList<>();
    private int[][] adjacencyMatrix;

    private Users loggedUser;

    public void setLoggedUser(Users user) {
        this.loggedUser = user;
    }

    @FXML
    public void handleLoadAllData() {
        try {
            txtDisplayArea.clear();
            dosyaOku();
            grafOlustur();
            lblStatus.setText("Veriler ve Graf başarıyla yüklendi.");
            txtDisplayArea.appendText("=== Sistem Hazır ===\n");
            txtDisplayArea.appendText("Tüm dosyalar yüklendi.\n");
            txtDisplayArea.appendText("Graf yapısı oluşturuldu.\n");
            txtDisplayArea.appendText("Toplam " + usersMap.size() + " kullanıcı bulundu.\n");
        } catch (Exception e) {
            showAlert("Hata", "Dosyalar okunurken hata: " + e.getMessage());
            e.printStackTrace();
        }
    }

    //  DOSYA OKUMA
    private void dosyaOku() throws IOException {
        usersMap.clear();
        userIDs.clear();

        List<String> lines = Files.readAllLines(Paths.get("src/main/resources/static/data/Kisiler.txt"));
        for (String line : lines) {
            String[] parts = line.split(",");
            if (parts.length >= 3) {
                String id = parts[0].trim();
                String ad = parts[1].trim();
                String cinsiyet = parts[2].trim();
                usersMap.put(id, new String[]{ad, cinsiyet});
                userIDs.add(id);
            }
        }

        quickSortStrings(userIDs, 0, userIDs.size() - 1);
    }

    private void grafOlustur() throws IOException {
        int size = userIDs.size();
        adjacencyMatrix = new int[size][size];

        List<String> lines = Files.readAllLines(Paths.get("src/main/resources/static/data/Iliski.txt"));

        for (int i = 0; i < lines.size() && i < size; i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) continue;

            String[] values = line.split(",");
            for (int j = 0; j < values.length && j < size; j++) {
                String val = values[j].trim();
                if (val.equals("-") || val.isEmpty()) {
                    adjacencyMatrix[i][j] = 0;
                } else {
                    try {
                        adjacencyMatrix[i][j] = Integer.parseInt(val);
                    } catch (NumberFormatException e) {
                        adjacencyMatrix[i][j] = 0;
                    }
                }
            }
        }

        loadBegeniler();
        loadYorumlar();
    }

    private void loadBegeniler() throws IOException {
        begeniList.clear();
        List<String> lines = Files.readAllLines(Paths.get("src/main/resources/static/data/Begeni.txt"));
        for (String line : lines) {
            begeniList.add(line.split(","));
        }
    }

    private void loadYorumlar() throws IOException {
        yorumList.clear();
        List<String> lines = Files.readAllLines(Paths.get("src/main/resources/static/data/Yorum.txt"));
        for (String line : lines) {
            yorumList.add(line.split(","));
        }
    }

    //  ALGORİTMALAR MANUEL SORT SEARCH

    private int binarySearch(List<String> list, String key) {
        int left = 0, right = list.size() - 1;
        int keyNum = Integer.parseInt(key);

        while (left <= right) {
            int mid = left + (right - left) / 2;
            int midNum = Integer.parseInt(list.get(mid));

            if (midNum == keyNum) return mid;
            if (midNum < keyNum) left = mid + 1;
            else right = mid - 1;
        }
        return -1;
    }

    private void quickSortStrings(List<String> list, int low, int high) {
        if (low < high) {
            int pi = partitionStrings(list, low, high);
            quickSortStrings(list, low, pi - 1);
            quickSortStrings(list, pi + 1, high);
        }
    }

    private int partitionStrings(List<String> list, int low, int high) {
        int pivot = Integer.parseInt(list.get(high));
        int i = (low - 1);
        for (int j = low; j < high; j++) {
            if (Integer.parseInt(list.get(j)) < pivot) {
                i++;
                String temp = list.get(i);
                list.set(i, list.get(j));
                list.set(j, temp);
            }
        }
        String temp = list.get(i + 1);
        list.set(i + 1, list.get(high));
        list.set(high, temp);
        return i + 1;
    }

    private void quickSortFriendScore(List<FriendScore> list, int low, int high, boolean descending) {
        if (low < high) {
            int pi = partitionFriendScore(list, low, high, descending);
            quickSortFriendScore(list, low, pi - 1, descending);
            quickSortFriendScore(list, pi + 1, high, descending);
        }
    }

    private int partitionFriendScore(List<FriendScore> list, int low, int high, boolean descending) {
        FriendScore pivot = list.get(high);
        int i = (low - 1);
        for (int j = low; j < high; j++) {
            boolean condition;
            if (descending) {
                condition = list.get(j).puan > pivot.puan;
            } else {
                condition = list.get(j).puan < pivot.puan;
            }

            if (condition) {
                i++;
                FriendScore temp = list.get(i);
                list.set(i, list.get(j));
                list.set(j, temp);
            }
        }
        FriendScore temp = list.get(i + 1);
        list.set(i + 1, list.get(high));
        list.set(high, temp);
        return i + 1;
    }

    private void quickSortRelationScore(List<RelationScore> list, int low, int high) {
        if (low < high) {
            int pi = partitionRelationScore(list, low, high);
            quickSortRelationScore(list, low, pi - 1);
            quickSortRelationScore(list, pi + 1, high);
        }
    }

    private int partitionRelationScore(List<RelationScore> list, int low, int high) {
        RelationScore pivot = list.get(high);
        int i = (low - 1);
        for (int j = low; j < high; j++) {
            if (list.get(j).puan > pivot.puan) {
                i++;
                RelationScore temp = list.get(i);
                list.set(i, list.get(j));
                list.set(j, temp);
            }
        }
        RelationScore temp = list.get(i + 1);
        list.set(i + 1, list.get(high));
        list.set(high, temp);
        return i + 1;
    }

    @FXML
    private void handleAreFriends() {
        String sID = txtSourceID.getText().trim();
        String tID = txtTargetID.getText().trim();

        if (sID.isEmpty() || tID.isEmpty()) {
            showAlert("Uyarı", "Lütfen Kaynak ve Hedef ID giriniz.");
            return;
        }

        if (!usersMap.containsKey(sID) || !usersMap.containsKey(tID)) {
            showAlert("Hata", "Kullanıcı bulunamadı.");
            return;
        }

        int durum = getIliskiDurumu(sID, tID);
        String sName = usersMap.get(sID)[0];
        String tName = usersMap.get(tID)[0];
        String msg = (durum == 2) ? "Yakın Arkadaştır" : (durum == 1) ? "Arkadaştır" : "Arkadaş Değildir";

        txtDisplayArea.clear();
        txtDisplayArea.appendText("=== ARKADAŞLIK DURUMU ===\n");
        txtDisplayArea.appendText(sName + " ve " + tName + ": " + msg + "\n");
    }

    private int getIliskiDurumu(String s, String t) {
        int sIdx = binarySearch(userIDs, s);
        int tIdx = binarySearch(userIDs, t);

        if (sIdx == -1 || tIdx == -1) return 0;
        return adjacencyMatrix[sIdx][tIdx];
    }

    private int iliskiPuaniHesapla(String i, String j) {
        int puan = 0;
        int durum = getIliskiDurumu(i, j);
        if (durum == 1) puan += 15;
        else if (durum == 2) puan += 30;

        for (String[] b : begeniList) {
            if (b.length >= 3) {
                String paylasimID = b[0].trim();
                String begenenID = b[1].trim();
                String begeniTuru = b[2].trim();

                if (paylasimID.startsWith(i + "-") && begenenID.equals(j)) {
                    puan += begeniTuru.equals("1") ? 5 : -5;
                }
            }
        }

        for (String[] y : yorumList) {
            if (y.length >= 3) {
                String paylasimID = y[0].trim();
                String yorumYapanID = y[1].trim();
                String yorumTuru = y[2].trim();

                if (paylasimID.startsWith(i + "-") && yorumYapanID.equals(j)) {
                    if (yorumTuru.equals("1")) puan += 10;
                    else if (yorumTuru.equals("2")) puan -= 5;
                    else puan += 5;
                }
            }
        }
        return puan;
    }

    @FXML
    private void handleShowFriends() {
        String id = txtSourceID.getText().trim();
        if (id.isEmpty()) { showAlert("Uyarı", "ID giriniz."); return; }
        arkadasGoster(id);
    }

    private void arkadasGoster(String kisiID) {
        if (!usersMap.containsKey(kisiID)) return;

        int idx = binarySearch(userIDs, kisiID);
        List<FriendScore> yakin = new ArrayList<>();
        List<FriendScore> arkadas = new ArrayList<>();

        for (int i = 0; i < adjacencyMatrix[idx].length; i++) {
            int durum = adjacencyMatrix[idx][i];
            if (durum > 0) {
                String friendID = userIDs.get(i);
                int puan = iliskiPuaniHesapla(kisiID, friendID);
                FriendScore fs = new FriendScore(friendID, usersMap.get(friendID)[0], puan, durum);
                if (durum == 2) yakin.add(fs);
                else arkadas.add(fs);
            }
        }

        if (yakin.size() > 1) quickSortFriendScore(yakin, 0, yakin.size() - 1, true);
        if (arkadas.size() > 1) quickSortFriendScore(arkadas, 0, arkadas.size() - 1, true);

        StringBuilder sb = new StringBuilder();
        sb.append("=== ").append(usersMap.get(kisiID)[0]).append(" ARKADAŞ LİSTESİ ===\n\n");
        sb.append("YAKIN ARKADAŞLAR:\n");
        for (FriendScore fs : yakin) sb.append(String.format("  %s (%s) - Puan: %d\n", fs.name, fs.id, fs.puan));
        sb.append("\nARKADAŞLAR:\n");
        for (FriendScore fs : arkadas) sb.append(String.format("  %s (%s) - Puan: %d\n", fs.name, fs.id, fs.puan));

        txtDisplayArea.setText(sb.toString());
    }

    @FXML
    private void handleRecommendFriend() {
        String id = txtSourceID.getText().trim();
        if (id.isEmpty()) return;
        arkadasOner(id);
    }

    private void arkadasOner(String kisiID) {
        int idx = binarySearch(userIDs, kisiID);
        if (idx == -1) return;

        Set<String> arkadaslar = new HashSet<>();
        arkadaslar.add(kisiID);
        for (int i = 0; i < adjacencyMatrix[idx].length; i++) {
            if (adjacencyMatrix[idx][i] > 0) arkadaslar.add(userIDs.get(i));
        }

        List<FriendScore> oneriler = new ArrayList<>();

        for (String aday : userIDs) {
            if (!arkadaslar.contains(aday)) {
                int oneriPuani = iliskiPuaniHesapla(kisiID, aday);
                int adayIdx = binarySearch(userIDs, aday);
                if (adayIdx != -1) {
                    for(int k=0; k<adjacencyMatrix[idx].length; k++) {
                        if(adjacencyMatrix[idx][k] > 0) {
                            String arkadasID = userIDs.get(k);
                            oneriPuani += iliskiPuaniHesapla(aday, arkadasID);
                        }
                    }
                }
                oneriler.add(new FriendScore(aday, usersMap.get(aday)[0], oneriPuani, 0));
            }
        }

        if (oneriler.size() > 1) quickSortFriendScore(oneriler, 0, oneriler.size() - 1, true);

        StringBuilder sb = new StringBuilder();
        sb.append("=== ÖNERİLEN ARKADAŞLAR ===\n");
        for (int i = 0; i < Math.min(3, oneriler.size()); i++) {
            FriendScore fs = oneriler.get(i);
            sb.append(String.format("%d. %s (ID: %s) - Puan: %d\n", i + 1, fs.name, fs.id, fs.puan));
        }
        txtDisplayArea.setText(sb.toString());
    }

    @FXML
    private void handleRemoveFriend() {
        String id = txtSourceID.getText().trim();
        if (id.isEmpty()) return;
        arkadasCikarmaOner(id);
    }

    private void arkadasCikarmaOner(String kisiID) {
        int idx = binarySearch(userIDs, kisiID);
        List<FriendScore> arkadaslar = new ArrayList<>();

        for (int i = 0; i < adjacencyMatrix[idx].length; i++) {
            if (adjacencyMatrix[idx][i] > 0) {
                String fID = userIDs.get(i);
                int puan = iliskiPuaniHesapla(kisiID, fID);
                arkadaslar.add(new FriendScore(fID, usersMap.get(fID)[0], puan, adjacencyMatrix[idx][i]));
            }
        }

        if (arkadaslar.size() > 1) quickSortFriendScore(arkadaslar, 0, arkadaslar.size() - 1, false);

        StringBuilder sb = new StringBuilder();
        sb.append("=== ARKADAŞLIKTAN ÇIKARILMASI ÖNERİLENLER ===\n");
        for (int i = 0; i < Math.min(3, arkadaslar.size()); i++) {
            FriendScore fs = arkadaslar.get(i);
            sb.append(String.format("%d. %s (ID: %s) - Puan: %d\n", i + 1, fs.name, fs.id, fs.puan));
        }
        txtDisplayArea.setText(sb.toString());
    }

    @FXML
    private void handleGeneralRanking() {
        genelSiralama();
    }

    private void genelSiralama() {
        List<RelationScore> list = new ArrayList<>();
        for (int i = 0; i < userIDs.size(); i++) {
            for (int j = i + 1; j < userIDs.size(); j++) {
                String u1 = userIDs.get(i);
                String u2 = userIDs.get(j);
                list.add(new RelationScore(u1, usersMap.get(u1)[0], u2, usersMap.get(u2)[0], iliskiPuaniHesapla(u1, u2)));
            }
        }

        if (list.size() > 1) quickSortRelationScore(list, 0, list.size() - 1);

        StringBuilder sb = new StringBuilder("=== GENEL SIRALAMA ===\n");
        sb.append(String.format("%-10s %-10s %-10s %-10s %s\n", "ID1", "Ad1", "ID2", "Ad2", "Puan"));
        for (RelationScore rs : list) {
            sb.append(String.format("%-10s %-10s %-10s %-10s %d\n", rs.id1, rs.name1, rs.id2, rs.name2, rs.puan));
        }
        txtDisplayArea.setText(sb.toString());
    }

    @FXML private void handleMostLiked() { showStats(true); }
    @FXML private void handleMostCommented() { showStats(false); }

    private void showStats(boolean isLike) {
        Map<String, Integer> counts = new HashMap<>();
        List<String[]> sourceList = isLike ? begeniList : yorumList;

        for (String[] item : sourceList) {
            if (item.length < 3) continue;
            if (isLike && !item[2].trim().equals("1")) continue;

            String paylasimID = item[0].trim();
            String userID = paylasimID.split("-")[0];
            counts.put(userID, counts.getOrDefault(userID, 0) + 1);
        }

        List<FriendScore> stats = new ArrayList<>();
        for (String uid : counts.keySet()) {
            if (usersMap.containsKey(uid)) {
                stats.add(new FriendScore(uid, usersMap.get(uid)[0], counts.get(uid), 0));
            }
        }

        if (stats.size() > 1) quickSortFriendScore(stats, 0, stats.size() - 1, true);

        StringBuilder sb = new StringBuilder(isLike ? "=== EN ÇOK BEĞENİ ALANLAR ===\n" : "=== EN ÇOK YORUM ALANLAR ===\n");
        for (int i = 0; i < Math.min(3, stats.size()); i++) {
            sb.append(String.format("%d. %s - Sayı: %d\n", i + 1, stats.get(i).name, stats.get(i).puan));
        }
        txtDisplayArea.setText(sb.toString());
    }

    @FXML
    private void handleShowGraph() {
        if (userIDs.isEmpty()) {
            showAlert("Uyarı", "Lütfen önce verileri yükleyin!");
            return;
        }
        grafGorselGoster();
    }

    private void grafGorselGoster() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== BINGÖL SOSYAL PAYLAŞIM AĞI - GRAF YAPISI ===\n\n");

        sb.append("Düğümler (Kullanıcılar):\n");
        sb.append("─".repeat(80)).append("\n");

        for (String userID : userIDs) {
            String userName = usersMap.get(userID)[0];
            sb.append(String.format("● %s (%s)\n", userName, userID));
        }

        sb.append("\n").append("═".repeat(80)).append("\n\n");
        sb.append("Bağlantılar (İlişkiler):\n");
        sb.append("─".repeat(80)).append("\n");

        int yakinArkadas = 0, arkadas = 0;

        for (int i = 0; i < userIDs.size(); i++) {
            for (int j = i + 1; j < userIDs.size(); j++) {
                int durum = adjacencyMatrix[i][j];
                if (durum > 0) {
                    String user1 = usersMap.get(userIDs.get(i))[0];
                    String user2 = usersMap.get(userIDs.get(j))[0];
                    String id1 = userIDs.get(i);
                    String id2 = userIDs.get(j);
                    int puan = iliskiPuaniHesapla(id1, id2);

                    if (durum == 2) {
                        sb.append(String.format("  %s (%s) ═══════ %s (%s)  [Yakın Arkadaş - Puan: %d]\n",
                                user1, id1, user2, id2, puan));
                        yakinArkadas++;
                    } else if (durum == 1) {
                        sb.append(String.format("  %s (%s) ─────── %s (%s)  [Arkadaş - Puan: %d]\n",
                                user1, id1, user2, id2, puan));
                        arkadas++;
                    }
                }
            }
        }

        sb.append("\n").append("═".repeat(80)).append("\n\n");
        sb.append("İSTATİSTİKLER:\n");
        sb.append(String.format("  • Toplam Kullanıcı: %d\n", userIDs.size()));
        sb.append(String.format("  • Yakın Arkadaş Bağlantısı: %d\n", yakinArkadas));
        sb.append(String.format("  • Arkadaş Bağlantısı: %d\n", arkadas));

        txtDisplayArea.setText(sb.toString());
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private static class FriendScore {
        String id, name;
        int puan, durum;
        FriendScore(String id, String name, int puan, int durum) {
            this.id = id; this.name = name; this.puan = puan; this.durum = durum;
        }
    }

    private static class RelationScore {
        String id1, name1, id2, name2;
        int puan;
        RelationScore(String id1, String name1, String id2, String name2, int puan) {
            this.id1 = id1; this.name1 = name1; this.id2 = id2; this.name2 = name2; this.puan = puan;
        }
    }
}