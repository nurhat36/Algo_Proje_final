package org.example.algo_proje.utils;

import org.example.algo_proje.Models.DTOs.FriendScore;
import org.example.algo_proje.Models.DTOs.RelationScore;

import java.util.ArrayList;
import java.util.List;

public class Algorithms {
    public int binarySearch(List<String> list, String key) {
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
    public void quickSortStrings(List<String> list, int low, int high) {
        if (low < high) {
            int pi = partitionStrings(list, low, high);
            quickSortStrings(list, low, pi - 1);
            quickSortStrings(list, pi + 1, high);
        }
    }

    public int partitionStrings(List<String> list, int low, int high) {
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

    public void quickSortFriendScore(List<FriendScore> list, int low, int high, boolean descending) {
        if (low < high) {
            int pi = partitionFriendScore(list, low, high, descending);
            quickSortFriendScore(list, low, pi - 1, descending);
            quickSortFriendScore(list, pi + 1, high, descending);
        }
    }

    public int partitionFriendScore(List<FriendScore> list, int low, int high, boolean descending) {
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

    public void quickSortRelationScore(List<RelationScore> list, int low, int high) {
        if (low < high) {
            int pi = partitionRelationScore(list, low, high);
            quickSortRelationScore(list, low, pi - 1);
            quickSortRelationScore(list, pi + 1, high);
        }
    }

    public int partitionRelationScore(List<RelationScore> list, int low, int high) {
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


    // === MERGE SORT BAŞLANGIÇ (List<String[]> için) ===

    // Ana çağırma metodu
    public void mergeSortUsers(List<String[]> list) {
        if (list.size() <= 1) {
            return; // Liste 1 elemanlıysa zaten sıralıdır
        }

        // 1. BÖL (Divide)
        int mid = list.size() / 2;

        // Sol ve Sağ alt listeleri oluştur
        List<String[]> left = new ArrayList<>();
        List<String[]> right = new ArrayList<>();

        for (int i = 0; i < mid; i++) left.add(list.get(i));
        for (int i = mid; i < list.size(); i++) right.add(list.get(i));

        // 2. YÖNET (Conquer) - Rekürsif çağrı
        mergeSortUsers(left);
        mergeSortUsers(right);

        // 3. BİRLEŞTİR (Merge)
        merge(list, left, right);
    }

    // Yardımcı Birleştirme Metodu
    private void merge(List<String[]> result, List<String[]> left, List<String[]> right) {
        int i = 0, j = 0, k = 0;

        // İki listeyi karşılaştırarak ana listeye (result) geri yaz
        while (i < left.size() && j < right.size()) {
            String idLeft = left.get(i)[0];   // Kullanıcı ID'si (String)
            String idRight = right.get(j)[0]; // Kullanıcı ID'si (String)

            // String karşılaştırması (compareTo)
            // Eğer ID'ler sayısal ise ve sayısal sıralama isteniyorsa:
            // Integer.parseInt(idLeft) < Integer.parseInt(idRight) yapılabilir.
            // Ancak şimdilik String (lexicographical) karşılaştırma yapıyoruz:
            if (idLeft.compareTo(idRight) <= 0) {
                result.set(k++, left.get(i++));
            } else {
                result.set(k++, right.get(j++));
            }
        }

        // Geriye kalan elemanları ekle
        while (i < left.size()) {
            result.set(k++, left.get(i++));
        }
        while (j < right.size()) {
            result.set(k++, right.get(j++));
        }
    }
    // === MERGE SORT BİTİŞ ===

}
