package com.example.gestion.dao;

import com.example.gestion.DatabaseService;
import com.example.gestion.model.Mouvement;
import com.example.gestion.model.Produit;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MouvementDAO {

    public static Mouvement getById(int id) {
        Mouvement mouvement = null;
        String query = "SELECT * FROM mouvements WHERE id = ?";

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                mouvement = extractMouvementFromResultSet(rs);
            }

            rs.close();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération du mouvement: " + e.getMessage());
            e.printStackTrace();
        }

        return mouvement;
    }

    public static List<Mouvement> getAll() {
        List<Mouvement> mouvements = new ArrayList<>();
        String query = "SELECT * FROM mouvements ORDER BY date_mouvement DESC";

        try (Connection conn = DatabaseService.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Mouvement mouvement = extractMouvementFromResultSet(rs);
                mouvements.add(mouvement);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des mouvements: " + e.getMessage());
            e.printStackTrace();
        }

        return mouvements;
    }

    private static Mouvement extractMouvementFromResultSet(ResultSet rs) throws SQLException {
        Mouvement mouvement = new Mouvement();
        mouvement.setId(rs.getInt("id"));

        int produitId = rs.getInt("produit_id");
        Produit produit = ProduitDAO.getById(produitId);
        mouvement.setProduit(produit);

        mouvement.setType(rs.getString("type"));
        mouvement.setQuantite(rs.getInt("quantite"));

        String dateStr = rs.getString("date_mouvement");
        if (dateStr != null && !dateStr.isEmpty()) {
            mouvement.setDateMouvement(LocalDate.parse(dateStr));
        }

        mouvement.setReferenceCommande(rs.getString("reference_commande"));

        return mouvement;
    }

    public static boolean insert(Mouvement mouvement) {
        String query = "INSERT INTO mouvements (produit_id, type, quantite, date_mouvement, reference_commande) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, mouvement.getProduit().getId());
            pstmt.setString(2, mouvement.getType());
            pstmt.setInt(3, mouvement.getQuantite());
            pstmt.setString(4, mouvement.getDateMouvement().toString());
            pstmt.setString(5, mouvement.getReferenceCommande());

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    mouvement.setId(rs.getInt(1));
                }
                rs.close();
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'insertion du mouvement: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
