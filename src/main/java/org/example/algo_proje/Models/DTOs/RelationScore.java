package org.example.algo_proje.Models.DTOs;

public class RelationScore {
    public String id1, name1, id2, name2;
    public int puan,id;
    public RelationScore(int id,String id1, String name1, String id2, String name2, int puan) {
        this.id = id;
        this.id1 = id1; this.name1 = name1; this.id2 = id2; this.name2 = name2; this.puan = puan;
    }
}
