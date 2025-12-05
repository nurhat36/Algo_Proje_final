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
}
