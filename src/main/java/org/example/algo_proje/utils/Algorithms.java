package org.example.algo_proje.utils;

import org.example.algo_proje.Models.DTOs.FriendScore;
import org.example.algo_proje.Models.DTOs.RelationScore;

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

}
