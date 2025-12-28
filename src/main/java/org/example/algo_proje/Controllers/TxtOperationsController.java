package org.example.algo_proje.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.algo_proje.Models.Users;
import org.example.algo_proje.utils.Algorithms;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import org.example.algo_proje.Models.DTOs.FriendScore;
import org.example.algo_proje.Models.DTOs.RelationScore;

public class TxtOperationsController {
    @FXML private TextField txtSourceID, txtTargetID;
    @FXML private TextArea txtDisplayArea;
    @FXML private Label lblStatus;
    @FXML private Button btnLoadFiles;

    // --- VERİ YAPILARI (HashMap yerine List Yapısı) ---
    // [0]: ID, [1]: Ad, [2]: Cinsiyet
    private List<String[]> usersList = new ArrayList<>();
    private List<String[]> begeniList = new ArrayList<>();
    private List<String[]> yorumList = new ArrayList<>();

    // Binary Search ve Matris indekslemesi için ID listesi
    private List<String> userIDs = new ArrayList<>();

    private Algorithms alg = new Algorithms();
    private int[][] adjacencyMatrix;

    private Users loggedUser;

    public void setLoggedUser(Users user) {
        this.loggedUser = user;
    }

    // --- YARDIMCI METOT: Manuel Arama ---
    // HashMap.get() yerine bu metodu kullanıyoruz.
    // Binary Search ile ID'nin indeksini bulup, usersList'ten veriyi çeker.
    private String[] findUserById(String id) {
        if (userIDs.isEmpty()) return null;

        // Algorithms sınıfındaki binarySearch metodunu kullanıyoruz
        int index = alg.binarySearch(userIDs, id);

        if (index != -1 && index < usersList.size()) {
            return usersList.get(index);
        }
        return null;
    }

    @FXML
    public void handleLoadAllData() {
        try {
            txtDisplayArea.clear();
            dosyaOku();
            grafOlustur();

            lblStatus.setText("Sistem başarıyla yüklendi.");
            txtDisplayArea.appendText("==================================================\n");
            txtDisplayArea.appendText("          BİNGÖL SOSYAL AĞ SİSTEMİ\n");
            txtDisplayArea.appendText("==================================================\n");
            txtDisplayArea.appendText(">> [OK] Kişiler dosyası okundu.\n");
            txtDisplayArea.appendText(">> [OK] İlişkiler matrise aktarıldı.\n");
            txtDisplayArea.appendText(">> [OK] Etkileşim verileri (Beğeni/Yorum) yüklendi.\n");
            txtDisplayArea.appendText("--------------------------------------------------\n");
            txtDisplayArea.appendText("TOPLAM KULLANICI SAYISI: " + usersList.size() + "\n");
            txtDisplayArea.appendText("==================================================\n");

        } catch (Exception e) {
            showAlert("Hata", "Dosyalar okunurken hata: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // --- DOSYA OKUMA VE SIRALAMA ---
    private void dosyaOku() throws IOException {
        usersList.clear();
        userIDs.clear();

        List<String> lines = Files.readAllLines(Paths.get("src/main/resources/static/data/Kisiler.txt"));
        for (String line : lines) {
            String[] parts = line.split(",");
            if (parts.length >= 3) {
                String id = parts[0].trim();
                String ad = parts[1].trim();
                String cinsiyet = parts[2].trim();

                // Listeye ekleme yapıyoruz
                usersList.add(new String[]{id, ad, cinsiyet});
                userIDs.add(id);
            }
        }

        // --- KRİTİK NOKTA ---
        // Binary Search yapabilmek için ID listesini sıralamalıyız.
        alg.quickSortStrings(userIDs, 0, userIDs.size() - 1);

        // usersList'i de aynı sıraya sokmalıyız ki indeksler (userIDs vs usersList) tutarlı olsun.
        // ID'ye (index 0) göre sıralıyoruz.
        usersList.sort(Comparator.comparing(u -> u[0]));
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

    // --- İŞLEM FONKSİYONLARI ---

    @FXML
    private void handleAreFriends() {
        String sID = txtSourceID.getText().trim();
        String tID = txtTargetID.getText().trim();

        if (sID.isEmpty() || tID.isEmpty()) {
            showAlert("Uyarı", "Lütfen Kaynak ve Hedef ID giriniz.");
            return;
        }

        // List üzerinden arama yapıyoruz
        String[] user1 = findUserById(sID);
        String[] user2 = findUserById(tID);

        if (user1 == null || user2 == null) {
            showAlert("Hata", "Girilen ID'ye sahip kullanıcı bulunamadı.");
            return;
        }

        int durum = getIliskiDurumu(sID, tID);
        String msg = (durum == 2) ? "YAKIN ARKADAŞ" : (durum == 1) ? "ARKADAŞ" : "ARKADAŞ DEĞİL";

        txtDisplayArea.clear();
        txtDisplayArea.appendText("------------ İLİŞKİ SORGULAMA ------------\n");
        txtDisplayArea.appendText(String.format("Kaynak Kişi : %-15s (ID: %s)\n", user1[1], sID));
        txtDisplayArea.appendText(String.format("Hedef Kişi  : %-15s (ID: %s)\n", user2[1], tID));
        txtDisplayArea.appendText("------------------------------------------\n");
        txtDisplayArea.appendText("SONUÇ       : " + msg + "\n");
    }

    private int getIliskiDurumu(String s, String t) {
        int sIdx = alg.binarySearch(userIDs, s);
        int tIdx = alg.binarySearch(userIDs, t);

        if (sIdx == -1 || tIdx == -1) return 0;
        return adjacencyMatrix[sIdx][tIdx];
    }

    private int iliskiPuaniHesapla(String i, String j) {
        int puan = 0;
        int durum = getIliskiDurumu(i, j);

        // Temel Puanlar
        if (durum == 1) puan += 15;
        else if (durum == 2) puan += 30;

        // Beğeni Puanları
        for (String[] b : begeniList) {
            if (b.length >= 3) {
                String paylasimID = b[0].trim(); // format: userID-postID
                String begenenID = b[1].trim();
                String begeniTuru = b[2].trim();

                if (paylasimID.startsWith(i + "-") && begenenID.equals(j)) {
                    puan += begeniTuru.equals("1") ? 5 : -5;
                }
            }
        }

        // Yorum Puanları
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
        String[] user = findUserById(kisiID);
        if (user == null) {
            showAlert("Hata", "Kullanıcı bulunamadı!");
            return;
        }

        int idx = alg.binarySearch(userIDs, kisiID);
        List<FriendScore> yakin = new ArrayList<>();
        List<FriendScore> arkadas = new ArrayList<>();

        for (int i = 0; i < adjacencyMatrix[idx].length; i++) {
            int durum = adjacencyMatrix[idx][i];
            if (durum > 0) {
                String friendID = userIDs.get(i);
                String[] friendData = findUserById(friendID);
                int puan = iliskiPuaniHesapla(kisiID, friendID);

                FriendScore fs = new FriendScore(friendID, friendData[1], puan, durum);
                if (durum == 2) yakin.add(fs);
                else arkadas.add(fs);
            }
        }

        // Puanlarına göre sırala
        if (yakin.size() > 1) alg.quickSortFriendScore(yakin, 0, yakin.size() - 1, true);
        if (arkadas.size() > 1) alg.quickSortFriendScore(arkadas, 0, arkadas.size() - 1, true);

        StringBuilder sb = new StringBuilder();
        sb.append("========= ").append(user[1].toUpperCase()).append(" (ID: ").append(kisiID).append(") =========\n\n");

        sb.append(">>> YAKIN ARKADAŞLAR\n");
        sb.append(String.format("%-8s %-20s %-10s\n", "ID", "İSİM", "PUAN"));
        sb.append("---------------------------------------\n");
        for (FriendScore fs : yakin)
            sb.append(String.format("%-8s %-20s %-10d\n", fs.id, fs.name, fs.puan));

        sb.append("\n>>> ARKADAŞLAR\n");
        sb.append(String.format("%-8s %-20s %-10s\n", "ID", "İSİM", "PUAN"));
        sb.append("---------------------------------------\n");
        for (FriendScore fs : arkadas)
            sb.append(String.format("%-8s %-20s %-10d\n", fs.id, fs.name, fs.puan));

        txtDisplayArea.setText(sb.toString());
    }

    @FXML
    private void handleRecommendFriend() {
        String id = txtSourceID.getText().trim();
        if (id.isEmpty()) return;
        arkadasOner(id);
    }

    private void arkadasOner(String kisiID) {
        if (findUserById(kisiID) == null) return;

        int idx = alg.binarySearch(userIDs, kisiID);

        // Mevcut arkadaşları Set'e atarak hızlı kontrol (Doğru)
        Set<String> arkadaslar = new HashSet<>();
        arkadaslar.add(kisiID); // Kendisini de ekle ki kendisine önermesin
        for (int i = 0; i < adjacencyMatrix[idx].length; i++) {
            if (adjacencyMatrix[idx][i] > 0) arkadaslar.add(userIDs.get(i));
        }

        List<FriendScore> oneriler = new ArrayList<>();

        for (String aday : userIDs) {
            // Zaten arkadaşı olmayanları ve kendisi olmayanları kontrol et
            if (!arkadaslar.contains(aday)) {

                // 1. KISIM: Aday ile Kişi arasındaki puan (Formüldeki ilk terim)
                // Resimdeki (a,b) sırasına uymak için (aday, kisiID) yaptık.
                int oneriPuani = iliskiPuaniHesapla(aday, kisiID);

                // 2. KISIM: Adayın, Kişinin Arkadaşlarıyla olan puanları toplamı (Sigma)
                int kisiIdx = alg.binarySearch(userIDs, kisiID); // idx değişkenin zaten var ama net olsun

                // Kişinin (Osman) tüm arkadaşarını (T) geziyoruz
                for(int k=0; k < adjacencyMatrix[idx].length; k++) {
                    if(adjacencyMatrix[idx][k] > 0) { // Eğer k, Osman'ın arkadaşıysa
                        String arkadasID = userIDs.get(k);

                        // Aday (Mustafa) ile Arkadaş (Ali) arasındaki puanı ekle
                        oneriPuani += iliskiPuaniHesapla(aday, arkadasID);
                    }
                }

                String[] adayData = findUserById(aday);
                oneriler.add(new FriendScore(aday, adayData[1], oneriPuani, 0));
            }
        }

        // Sıralama ve Yazdırma (Doğru)
        if (oneriler.size() > 1) alg.quickSortFriendScore(oneriler, 0, oneriler.size() - 1, true);

        StringBuilder sb = new StringBuilder("========== ARKADAŞ ÖNERİLERİ ==========\n\n");
        for (int i = 0; i < Math.min(5, oneriler.size()); i++) {
            FriendScore fs = oneriler.get(i);
            sb.append(String.format("%d. %-20s [Puan: %4d] (ID: %s)\n", i + 1, fs.name, fs.puan, fs.id));
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
        int idx = alg.binarySearch(userIDs, kisiID);
        if (idx == -1) return;

        List<FriendScore> arkadaslar = new ArrayList<>();

        for (int i = 0; i < adjacencyMatrix[idx].length; i++) {
            if (adjacencyMatrix[idx][i] > 0) {
                String fID = userIDs.get(i);
                String[] fData = findUserById(fID);
                int puan = iliskiPuaniHesapla(kisiID, fID);
                arkadaslar.add(new FriendScore(fID, fData[1], puan, adjacencyMatrix[idx][i]));
            }
        }

        // Puanı DÜŞÜK olandan yüksek olana doğru sıralama (false parametresi ile)
        if (arkadaslar.size() > 1) alg.quickSortFriendScore(arkadaslar, 0, arkadaslar.size() - 1, false);

        StringBuilder sb = new StringBuilder("=== ARKADAŞLIKTAN ÇIKARILMA ÖNERİSİ ===\n");
        sb.append("(En Düşük Puanlılar)\n\n");
        for (int i = 0; i < Math.min(3, arkadaslar.size()); i++) {
            FriendScore fs = arkadaslar.get(i);
            sb.append(String.format("%d. %-20s [Puan: %d] (ID: %s)\n", i + 1, fs.name, fs.puan, fs.id));
        }
        txtDisplayArea.setText(sb.toString());
    }

    @FXML
    private void handleGeneralRanking() {
        genelSiralama();
    }

    private void genelSiralama() {
        List<RelationScore> list = new ArrayList<>();
        int idCounter = 1;

        for (int i = 0; i < userIDs.size(); i++) {
            for (int j = 0; j < userIDs.size(); j++) {
                String u1ID = userIDs.get(i);
                String u2ID = userIDs.get(j);

                if(u1ID.equals(u2ID)) continue;

                String[] u1Data = findUserById(u1ID);
                String[] u2Data = findUserById(u2ID);

                int puan = iliskiPuaniHesapla(u1ID, u2ID);
                list.add(new RelationScore(idCounter++, u1ID, u1Data[1], u2ID, u2Data[1], puan));
            }
        }

        if (list.size() > 1) alg.quickSortRelationScore(list, 0, list.size() - 1);

        StringBuilder sb = new StringBuilder("========== GENEL AĞ SIRALAMASI ==========\n\n");
        sb.append(String.format("%-6s %-15s %-15s %-6s\n","NO", "KİŞİ 1", "KİŞİ 2", "PUAN"));
        sb.append("---------------------------------------------\n");

        // İlk 50 ilişkiyi gösterelim ki ekran dolmasın
        for (int k = 0; k < list.size(); k++) {
            RelationScore rs = list.get(k);
            sb.append(String.format("%-6d %-15s %-15s %-6d\n",
                    k+1, rs.name1, rs.name2, rs.puan));
        }
        txtDisplayArea.setText(sb.toString());
    }

    @FXML private void handleMostLiked() { showStats(true); }
    @FXML private void handleMostCommented() { showStats(false); }

    private void showStats(boolean isLike) {
        // ID -> Adet haritası (Frekans sayımı)
        Map<String, Integer> counts = new HashMap<>();
        List<String[]> sourceList = isLike ? begeniList : yorumList;

        for (String[] item : sourceList) {
            if (item.length < 3) continue;
            // Beğeni ise sadece "1" olanları (Like) say, Dislike (0) sayma
            if (isLike && !item[2].trim().equals("1")) continue;

            String paylasimID = item[0].trim();
            String userID = paylasimID.split("-")[0]; // "1-10" -> "1"
            counts.put(userID, counts.getOrDefault(userID, 0) + 1);
        }

        List<FriendScore> stats = new ArrayList<>();
        for (String uid : counts.keySet()) {
            String[] uData = findUserById(uid);
            if (uData != null) {
                stats.add(new FriendScore(uid, uData[1], counts.get(uid), 0));
            }
        }

        if (stats.size() > 1) alg.quickSortFriendScore(stats, 0, stats.size() - 1, true);

        StringBuilder sb = new StringBuilder(isLike ? "=== EN ÇOK BEĞENİ ALANLAR ===\n" : "=== EN ÇOK YORUM ALANLAR ===\n");
        for (int i = 0; i < Math.min(5, stats.size()); i++) {
            sb.append(String.format("%d. %-20s - Adet: %d\n", i + 1, stats.get(i).name, stats.get(i).puan));
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
        sb.append("========= BİNGÖL SOSYAL AĞ - GRAF YAPISI =========\n\n");

        sb.append(">>> DÜĞÜMLER (Users):\n");
        sb.append("--------------------------------------------------\n");
        int rowCount = 0;
        for (String userID : userIDs) {
            String userName = findUserById(userID)[1];
            sb.append(String.format("[%s:%s]  ", userID, userName));
            rowCount++;
            if(rowCount % 4 == 0) sb.append("\n"); // Her satırda 4 kişi göster
        }

        sb.append("\n\n>>> KENARLAR (Edges / İlişkiler):\n");
        sb.append("--------------------------------------------------\n");

        int yakinArkadas = 0, arkadas = 0;

        for (int i = 0; i < userIDs.size(); i++) {
            for (int j = i + 1; j < userIDs.size(); j++) {
                int durum = adjacencyMatrix[i][j];
                if (durum > 0) {
                    String id1 = userIDs.get(i);
                    String id2 = userIDs.get(j);
                    String u1 = findUserById(id1)[1];
                    String u2 = findUserById(id2)[1];

                    if (durum == 2) {
                        sb.append(String.format("  %-10s <=====YAKIN=====> %-10s\n", u1, u2));
                        yakinArkadas++;
                    } else if (durum == 1) {
                        sb.append(String.format("  %-10s <-----ARKADAŞ-----> %-10s\n", u1, u2));
                        arkadas++;
                    }
                }
            }
        }

        sb.append("\n==================================================\n");
        sb.append("GRAF İSTATİSTİKLERİ:\n");
        sb.append(String.format("  • Toplam Düğüm (User) : %d\n", userIDs.size()));
        sb.append(String.format("  • Yakın Arkadaş Kenarı: %d\n", yakinArkadas));
        sb.append(String.format("  • Normal Arkadaş Kenarı: %d\n", arkadas));

        txtDisplayArea.setText(sb.toString());
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}