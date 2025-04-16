package com.example.gestion.dao;

import com.example.gestion.DatabaseService;
import com.example.gestion.model.Local;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LocalDAO {

    public static Local getById(int id) {
        Local local = null;
        String query = "SELECT * FROM locaux WHERE id = ?";

        try (Connection conn = DatabaseService.getConnection();

             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                local = new Local();
                local.setId(rs.getInt("id"));
                local.setCode(rs.getString("code"));
                local.setDesignation(rs.getString("designation"));
                local.setType(rs.getString("type"));
            }

            rs.close();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération du local: " + e.getMessage());
        }

        return local;
    }

    public static List<Local> getAll() {
        List<Local> locaux = new ArrayList<>();
        String query = "SELECT * FROM locaux";

        try (Connection conn = DatabaseService.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Local local = new Local();
                local.setId(rs.getInt("id"));
                local.setCode(rs.getString("code"));
                local.setDesignation(rs.getString("designation"));
                local.setType(rs.getString("type"));
                locaux.add(local);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des locaux: " + e.getMessage());
        }

        return locaux;
    }

    public static List<Local> searchLocaux(String keyword) {
        List<Local> locaux = new ArrayList<>();
        String query = "SELECT * FROM locaux WHERE code LIKE ? OR designation LIKE ?";

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, "%" + keyword + "%");
            pstmt.setString(2, "%" + keyword + "%");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Local local = new Local();
                local.setId(rs.getInt("id"));
                local.setCode(rs.getString("code"));
                local.setDesignation(rs.getString("designation"));
                local.setType(rs.getString("type"));
                locaux.add(local);
            }

            rs.close();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche des locaux: " + e.getMessage());
        }

        return locaux;
    }

    public static boolean insert(Local local) {
        String query = "INSERT INTO locaux (code, designation, type) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, local.getCode());
            pstmt.setString(2, local.getDesignation());
            pstmt.setString(3, local.getType());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'insertion du local: " + e.getMessage());
            return false;
        }
    }

    public static boolean update(Local local) {
        String query = "UPDATE locaux SET code = ?, designation = ?, type = ? WHERE id = ?";

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, local.getCode());
            pstmt.setString(2, local.getDesignation());
            pstmt.setString(3, local.getType());
            pstmt.setInt(4, local.getId());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour du local: " + e.getMessage());
            return false;
        }
    }

    public static boolean delete(int id) {
        String query = "DELETE FROM locaux WHERE id = ?";

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, id);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression du local: " + e.getMessage());
            return false;
        }
    }
}
