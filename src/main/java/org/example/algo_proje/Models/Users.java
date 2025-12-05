package org.example.algo_proje.Models;

import java.sql.Date;
import java.sql.Timestamp;

public class Users {
    private int userId;

    // Kimlik Bilgileri
    private String fullName;
    private String username;
    private String email;
    private String phoneNumber;

    // Güvenlik
    private byte[] passwordHash;
    private byte[] passwordSalt;
    private boolean emailVerified;
    private boolean phoneVerified;
    private boolean twoFactorEnabled;

    // Profil
    private String bio;
    private byte[] profilePhoto;
    private byte[] coverPhoto;
    private String website;
    private String gender;
    private Date birthDate;

    // Aktivite
    private int followersCount;
    private int followingCount;
    private int postsCount;

    // Durum
    private boolean isPrivate;
    private boolean isBanned;
    private String bannedReason;
    private Date lastSeen;
    private String status;

    // Adres
    private String country;
    private String city;

    // Sistem
    private Date createdAt;
    private Date updatedAt;
    private Date lastLoginAt;
    private String lastLoginIP;

    // Silinme
    private boolean isDeleted;
    private Date deletedAt;

}
