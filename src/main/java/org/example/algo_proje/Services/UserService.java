package org.example.algo_proje.Services;

import org.example.algo_proje.Models.Users;
import org.example.algo_proje.utils.Security;

import java.sql.*;

public class UserService {

    // Kayıt Ol
    public static boolean register(String fullName, String username, String email, String password) {

        byte[] salt = Security.generateSalt();
        byte[] hash = Security.hashPassword(password, salt);

        try (Connection conn = Database.getConnection()) {

            String sql = "INSERT INTO Users (FullName, Username, Email, PasswordHash, PasswordSalt) " +
                    "VALUES (?, ?, ?, ?, ?)";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, fullName);
            ps.setString(2, username);
            ps.setString(3, email);
            ps.setBytes(4, hash);
            ps.setBytes(5, salt);

            ps.executeUpdate();
            return true;

        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("Kullanıcı adı veya email zaten var.");
            return false;

        } catch (Exception e) {
            System.out.println("REGISTER ERROR: " + e.getMessage());
            return false;
        }
    }

    // Giriş Yap
    public static boolean login(String username, String password) {

        try (Connection conn = Database.getConnection()) {

            String sql = "SELECT PasswordHash, PasswordSalt FROM Users WHERE Username=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);

            ResultSet rs = ps.executeQuery();

            if (!rs.next()) return false;

            byte[] hash = rs.getBytes("PasswordHash");
            byte[] salt = rs.getBytes("PasswordSalt");

            return Security.verifyPassword(password, salt, hash);

        } catch (Exception e) {
            System.out.println("LOGIN ERROR: " + e.getMessage());
            return false;
        }
    }


    // Profil güncelleme
    public static boolean updateUserProfile(Users user) {

        String sql = "UPDATE Users SET Bio=?, Website=?, PhoneNumber=?, Gender=?, BirthDate=?, "
                + "Country=?, City=?, ProfilePhoto=? WHERE UserId=?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getBio());
            stmt.setString(2, user.getWebsite());
            stmt.setString(3, user.getPhoneNumber());
            stmt.setString(4, user.getGender());
            stmt.setDate(5, user.getBirthDate());
            stmt.setString(6, user.getCountry());
            stmt.setString(7, user.getCity());
            stmt.setString(8, user.getProfilePhoto());
            stmt.setInt(9, user.getUserId());

            int updated = stmt.executeUpdate();
            return updated > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // UserService.java (Örnek)

    public static boolean isProfileComplete(Users user) {
        // Kontrol edilecek zorunlu alanlar:
        if (user == null) {
            return false;
        }

        // 1. Biyografi zorunlu mu?
        if (user.getBio() == null || user.getBio().trim().isEmpty()) {
            return false;
        }

        // 2. Doğum tarihi zorunlu mu?
        if (user.getBirthDate() == null) {
            return false;
        }

        // 3. Telefon zorunlu mu?
        if (user.getPhoneNumber() == null || user.getPhoneNumber().trim().isEmpty()) {
            return false;
        }

        // Örnek: Ülke alanı da zorunlu olsun
        if (user.getCountry() == null || user.getCountry().trim().isEmpty()) {
            return false;
        }

        // Eğer tüm zorunlu alanlar doluysa
        return true;
    }
    public static int loginAndGetUserId(String username, String password) {
        try (Connection conn = Database.getConnection()) {

            String sql = "SELECT UserId, PasswordHash, PasswordSalt FROM Users WHERE Username=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);

            ResultSet rs = ps.executeQuery();

            if (!rs.next()) return -1;

            byte[] hash = rs.getBytes("PasswordHash");
            byte[] salt = rs.getBytes("PasswordSalt");

            boolean valid = Security.verifyPassword(password, salt, hash);

            if (valid)
                return rs.getInt("UserId");

            return -1;

        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
    public static Users getUserById(int userId) {
        String sql = "SELECT * FROM Users WHERE UserId=?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Users user = new Users();

                user.setUserId(rs.getInt("UserId"));
                user.setFullName(rs.getString("FullName"));
                user.setUsername(rs.getString("Username"));
                user.setEmail(rs.getString("Email"));
                user.setPhoneNumber(rs.getString("PhoneNumber"));
                user.setBio(rs.getString("Bio"));
                user.setWebsite(rs.getString("Website"));
                user.setGender(rs.getString("Gender"));
                user.setCity(rs.getString("City"));
                user.setCountry(rs.getString("Country"));
                user.setBirthDate(rs.getDate("BirthDate"));
                user.setProfilePhoto(rs.getString("ProfilePhoto"));

                return user;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
    public static Users getUserByUsername(String username) {
        String sql = "SELECT * FROM Users WHERE Username=?";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Users u = new Users();
                u.setUserId(rs.getInt("userId"));
                u.setFullName(rs.getString("fullName"));
                u.setUsername(rs.getString("username"));
                u.setEmail(rs.getString("email"));
                u.setBio(rs.getString("bio"));
                u.setWebsite(rs.getString("website"));
                u.setPhoneNumber(rs.getString("phoneNumber"));
                u.setCity(rs.getString("city"));
                u.setCountry(rs.getString("country"));
                u.setGender(rs.getString("gender"));
                u.setBirthDate(rs.getDate("birthDate"));
                u.setProfilePhoto(rs.getString("profilePhoto"));

                return u;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

}
