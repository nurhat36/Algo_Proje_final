package org.example.algo_proje.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.algo_proje.Models.Users;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class TxtOperationsController {
    @FXML private TextField txtSourceID, txtTargetID;
    @FXML private TextArea txtDisplayArea;
    @FXML private Label lblStatus;

    private Users loggedUser;

    // Veri Yapıları
    private Map<String, String[]> usersMap = new HashMap<>(); // ID -> [Ad, Cinsiyet] [cite: 132, 210]
    private Map<String, Map<String, Integer>> adjacencyMatrix = new HashMap<>(); // Graf
    private List<String[]> begeniList = new ArrayList<>(); // [cite: 138, 209]
    private List<String[]> yorumList = new ArrayList<>(); // [cite: 140, 208]
    private List<String> userIDs = new ArrayList<>(); // Matris indeksleri için ID listesi
    private int[][] adjacencyMatrix2; // İlişki durumlarını (0,1,2) tutan matris [cite: 27, 28]

    public void setLoggedUser(Users user) {
        this.loggedUser = user;
    }

    @FXML
    public void handleLoadAllData() {
        try {
            txtDisplayArea.clear();
            loadKisiler();
            loadIliskiler();
            loadBegeniler();
            loadYorumlar();
            lblStatus.setText("Veriler ve Graf başarıyla yüklendi.");
            txtDisplayArea.appendText("Sistem: Tüm .txt dosyaları belleğe alındı ve Graf oluşturuldu.\n");
        } catch (Exception e) {
            showAlert("Hata", "Dosyalar okunurken bir hata oluştu: " + e.getMessage());
        }
    }

    private void loadKisiler() throws IOException {
        Files.lines(Paths.get("src/main/resources/static/data/Kisiler.txt")).forEach(line -> {
            String[] parts = line.split(",");
            if (parts.length >= 3) usersMap.put(parts[0], new String[]{parts[1], parts[2]});
        });
    }

    private void loadIliskiler() throws IOException {
        // 1. Önce ID'leri sıralı bir listeye alalım (İndeks yönetimi için)
        userIDs = usersMap.keySet().stream().sorted().collect(Collectors.toList());
        int size = userIDs.size();
        adjacencyMatrix2 = new int[size][size];

        // 2. Dosyayı oku
        Path path = Paths.get("src/main/resources/static/data/Iliski.txt");
        List<String> lines = Files.readAllLines(path);

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) continue;

            // Virgüllere göre ayır ve "-" olan (kendiyle ilişkisi) yerleri 0 kabul et [cite: 134, 137]
            String[] values = line.split(",");
            for (int j = 0; j < values.length; j++) {
                if (j < size) {
                    String val = values[j].trim();
                    if (val.equals("-")) {
                        adjacencyMatrix2[i][j] = 0;
                    } else {
                        try {
                            adjacencyMatrix2[i][j] = Integer.parseInt(val);
                        } catch (NumberFormatException e) {
                            adjacencyMatrix2[i][j] = 0;
                        }
                    }
                }
            }
        }
    }

    private void loadBegeniler() throws IOException {
        begeniList = Files.lines(Paths.get("src/main/resources/static/data/Begeni.txt"))
                .map(line -> line.split(",")).collect(Collectors.toList());
    }

    private void loadYorumlar() throws IOException {
        yorumList = Files.lines(Paths.get("src/main/resources/static/data/Yorum.txt"))
                .map(line -> line.split(",")).collect(Collectors.toList());
    }

    // FORMÜL 1: İlişki Puanı Hesaplama [cite: 67, 149]
    private int hesaplaIliskiPuani(String i, String j) {
        int puan = 0;

        // 1. İlişki Durumu Puanı [cite: 70, 78]
        int durum = getIliskiDurumu(i, j);
        if (durum == 1) puan += 15; // Arkadaş [cite: 77]
        else if (durum == 2) puan += 30; // Yakın Arkadaş [cite: 78]

        // 2. Beğeni Puanları [cite: 79, 89]
        for (String[] b : begeniList) {
            if (b[0].startsWith(i + "-") && b[1].equals(j)) {
                puan += b[2].equals("1") ? 5 : -5;
            }
        }

        // 3. Yorum Puanları [cite: 80, 95]
        for (String[] y : yorumList) {
            if (y[0].startsWith(i + "-") && y[1].equals(j)) {
                if (y[2].equals("1")) puan += 10; // Olumlu [cite: 91]
                else if (y[2].equals("2")) puan += -5; // Olumsuz [cite: 95]
                else puan += 5; // Nötr [cite: 85]
            }
        }
        return puan;
    }


    @FXML
    private void handleAreFriends() {
        String sID = txtSourceID.getText().trim();
        String tID = txtTargetID.getText().trim();

        if (sID.isEmpty() || tID.isEmpty()) {
            showAlert("Uyarı", "Lütfen hem Kaynak hem de Hedef ID giriniz.");
            return;
        }

        // Null kontrolü ekliyoruz
        if (!usersMap.containsKey(sID) || !usersMap.containsKey(tID)) {
            showAlert("Hata", "Girilen ID'lerden biri veya her ikisi sistemde kayıtlı değil.");
            return;
        }

        int durum = getIliskiDurumu(sID, tID);
        String msg = (durum == 2) ? "Yakın Arkadaştır" : (durum == 1) ? "Arkadaştır" : "Arkadaş Değildir";

        // Güvenli erişim
        String sName = usersMap.get(sID)[0];
        String tName = usersMap.get(tID)[0];

        txtDisplayArea.setText(sName + " (ID: " + sID + ") ile " +
                tName + " (ID: " + tID + ") " + msg);
    }

    @FXML
    private void handleShowFriends() {
        String id = txtSourceID.getText().trim();
        if (id.isEmpty()) {
            showAlert("Uyarı", "Lütfen bir Kaynak ID giriniz.");
            return;
        }

        if (!usersMap.containsKey(id)) {
            showAlert("Hata", "ID bulunamadı: " + id);
            return;
        }

        String userName = usersMap.get(id)[0];
        StringBuilder sb = new StringBuilder("--- " + userName + " (ID: " + id + ") Arkadaş Listesi ---\n\n");

        // Burada arkadaşları listeleme mantığınız çalışacak
        // Örnek: calculateAndShowFriends(id, sb);

        txtDisplayArea.setText(sb.toString());
    }

    @FXML
    private void handleRecommendFriend() {
        String id = txtSourceID.getText();
        if (id.isEmpty()) return;

        // FORMÜL 2: oneriPuani hesapla [cite: 112, 116]
        // En yüksek 3 kişiyi bul [cite: 111, 156]
        txtDisplayArea.setText(id + " için Arkadaş Önerileri (İlk 3)\n");
    }

    @FXML
    private void handleGeneralRanking() {
        // Tüm ikili ilişkileri hesapla ve sırala [cite: 127, 168]
        txtDisplayArea.setText("GENEL SIRALAMA\nKişi ID | Ad | Kişi ID | Ad | İlişki Puanı\n");
    }

    private int getIliskiDurumu(String s, String t) {
        // Matristen veya dosyadan ilişki tipini döndür (0, 1, 2) [cite: 137]
        return 0;
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}