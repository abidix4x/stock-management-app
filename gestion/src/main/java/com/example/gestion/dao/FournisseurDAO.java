package com.example.gestion.dao;

import com.example.gestion.DatabaseService;
import com.example.gestion.model.Fournisseur;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FournisseurDAO {

    public static Fournisseur getById(int id) {
        Fournisseur fournisseur = null;
        String query = "SELECT * FROM fournisseurs WHERE id = ?";

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                fournisseur = extractFournisseurFromResultSet(rs);
            }

            rs.close();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération du fournisseur: " + e.getMessage());
            e.printStackTrace();
        }

        return fournisseur;
    }

    public static List<Fournisseur> getAll() {
        List<Fournisseur> fournisseurs = new ArrayList<>();
        String query = "SELECT * FROM fournisseurs";

        try (Connection conn = DatabaseService.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Fournisseur fournisseur = extractFournisseurFromResultSet(rs);
                fournisseurs.add(fournisseur);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des fournisseurs: " + e.getMessage());
            e.printStackTrace();
        }

        return fournisseurs;
    }

    private static Fournisseur extractFournisseurFromResultSet(ResultSet rs) throws SQLException {
        Fournisseur fournisseur = new Fournisseur();
        fournisseur.setId(rs.getInt("id"));
        fournisseur.setNom(rs.getString("nom"));
        fournisseur.setAdresse(rs.getString("adresse"));
        fournisseur.setTelephone(rs.getString("telephone"));
        fournisseur.setEmail(rs.getString("email"));
        return fournisseur;
    }

    public static List<Fournisseur> searchFournisseurs(String keyword) {
        List<Fournisseur> fournisseurs = new ArrayList<>();
        String query = "SELECT * FROM fournisseurs WHERE nom LIKE ? OR adresse LIKE ?";

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, "%" + keyword + "%");
            pstmt.setString(2, "%" + keyword + "%");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Fournisseur fournisseur = extractFournisseurFromResultSet(rs);
                fournisseurs.add(fournisseur);
            }

            rs.close();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche des fournisseurs: " + e.getMessage());
            e.printStackTrace();
        }

        return fournisseurs;
    }

    public static boolean insert(Fournisseur fournisseur) {
        String query = "INSERT INTO fournisseurs (nom, adresse, telephone, email) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, fournisseur.getNom());
            pstmt.setString(2, fournisseur.getAdresse());
            pstmt.setString(3, fournisseur.getTelephone());
            pstmt.setString(4, fournisseur.getEmail());

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    fournisseur.setId(rs.getInt(1));
                }
                rs.close();
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'insertion du fournisseur: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static boolean update(Fournisseur fournisseur) {
        String query = "UPDATE fournisseurs SET nom = ?, adresse = ?, telephone = ?, email = ? WHERE id = ?";

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, fournisseur.getNom());
            pstmt.setString(2, fournisseur.getAdresse());
            pstmt.setString(3, fournisseur.getTelephone());
            pstmt.setString(4, fournisseur.getEmail());
            pstmt.setInt(5, fournisseur.getId());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour du fournisseur: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static boolean delete(int id) {
        String query = "DELETE FROM fournisseurs WHERE id = ?";

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, id);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression du fournisseur: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
