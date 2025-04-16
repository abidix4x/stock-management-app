package com.example.gestion.dao;

import com.example.gestion.DatabaseService;
import com.example.gestion.model.Produit;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ProduitDAO {

    public static Produit getById(int id) {
        Produit produit = null;
        String query = "SELECT * FROM produits WHERE id = ?";

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                produit = extractProduitFromResultSet(rs);
            }

            rs.close();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération du produit: " + e.getMessage());
            e.printStackTrace();
        }

        return produit;
    }

    public static List<Produit> getAll() {
        List<Produit> produits = new ArrayList<>();
        String query = "SELECT * FROM produits";

        System.out.println("Récupération de tous les produits...");

        try (Connection conn = DatabaseService.getConnection();
             Statement stmt = conn.createStatement()) {

            System.out.println("Exécution de la requête: " + query);
            ResultSet rs = stmt.executeQuery(query);

            int count = 0;
            while (rs.next()) {
                count++;
                Produit produit = extractProduitFromResultSet(rs);
                produits.add(produit);

                // Débogage
                System.out.println("Produit #" + count + " chargé: ID=" + produit.getId() +
                        ", Ref=" + produit.getReference() +
                        ", Designation=" + produit.getDesignation());
            }

            rs.close();
            System.out.println("Total des produits récupérés: " + produits.size());
        } catch (SQLException e) {
            System.err.println("Erreur SQL lors de la récupération des produits: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Erreur générale lors de la récupération des produits: " + e.getMessage());
            e.printStackTrace();
        }

        return produits;
    }

    private static Produit extractProduitFromResultSet(ResultSet rs) throws SQLException {
        Produit produit = new Produit();
        produit.setId(rs.getInt("id"));
        produit.setReference(rs.getString("reference"));
        produit.setDesignation(rs.getString("designation"));
        produit.setType(rs.getString("type"));
        produit.setCategorie(rs.getString("categorie"));
        produit.setQuantite(rs.getInt("quantite"));
        produit.setStockMinimal(rs.getInt("stock_minimal"));

        String dateStr = rs.getString("date_peremption");
        if (dateStr != null && !dateStr.isEmpty()) {
            try {
                produit.setDatePeremption(LocalDate.parse(dateStr));
            } catch (Exception e) {
                System.err.println("Erreur lors du parsing de la date: " + dateStr);
            }
        }

        produit.setCritique(rs.getBoolean("critique"));
        return produit;
    }

    public static List<Produit> searchProduits(String keyword) {
        List<Produit> produits = new ArrayList<>();
        String query = "SELECT * FROM produits WHERE reference LIKE ? OR designation LIKE ?";

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, "%" + keyword + "%");
            pstmt.setString(2, "%" + keyword + "%");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Produit produit = extractProduitFromResultSet(rs);
                produits.add(produit);
            }

            rs.close();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche des produits: " + e.getMessage());
            e.printStackTrace();
        }

        return produits;
    }

    public static List<Produit> getProduitsEnAlerte() {
        List<Produit> produits = new ArrayList<>();
        String query = "SELECT * FROM produits WHERE quantite <= stock_minimal OR " +
                "(date_peremption IS NOT NULL AND date_peremption <= date('now', '+30 day'))";

        try (Connection conn = DatabaseService.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Produit produit = extractProduitFromResultSet(rs);
                produits.add(produit);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des produits en alerte: " + e.getMessage());
            e.printStackTrace();
        }

        return produits;
    }

    public static List<Produit> getProduitsCritiques() {
        List<Produit> produits = new ArrayList<>();
        String query = "SELECT * FROM produits WHERE critique = 1";

        try (Connection conn = DatabaseService.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Produit produit = extractProduitFromResultSet(rs);
                produits.add(produit);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des produits critiques: " + e.getMessage());
            e.printStackTrace();
        }

        return produits;
    }

    public static boolean insert(Produit produit) {
        String query = "INSERT INTO produits (reference, designation, type, categorie, quantite, stock_minimal, date_peremption, critique) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        System.out.println("Tentative d'insertion du produit: " + produit.getReference() + " - " + produit.getDesignation());

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, produit.getReference());
            pstmt.setString(2, produit.getDesignation());
            pstmt.setString(3, produit.getType());
            pstmt.setString(4, produit.getCategorie());
            pstmt.setInt(5, produit.getQuantite());
            pstmt.setInt(6, produit.getStockMinimal());

            if (produit.getDatePeremption() != null) {
                pstmt.setString(7, produit.getDatePeremption().toString());
                System.out.println("Date de péremption: " + produit.getDatePeremption().toString());
            } else {
                pstmt.setNull(7, Types.VARCHAR);
                System.out.println("Date de péremption: null");
            }

            pstmt.setBoolean(8, produit.isCritique());

            System.out.println("Exécution de la requête SQL: " + query);
            int rowsAffected = pstmt.executeUpdate();
            System.out.println("Lignes affectées: " + rowsAffected);

            if (rowsAffected > 0) {
                // Récupérer l'ID généré
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    int id = rs.getInt(1);
                    produit.setId(id);
                    System.out.println("ID généré: " + id);
                } else {
                    System.out.println("Aucun ID généré récupéré");
                }
                rs.close();
                System.out.println("Produit inséré avec succès: ID=" + produit.getId());
                return true;
            }
            System.out.println("Échec de l'insertion du produit");
            return false;
        } catch (SQLException e) {
            System.err.println("Erreur SQL lors de l'insertion du produit: " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            System.err.println("Erreur générale lors de l'insertion du produit: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static boolean update(Produit produit) {
        String query = "UPDATE produits SET reference = ?, designation = ?, type = ?, categorie = ?, " +
                "quantite = ?, stock_minimal = ?, date_peremption = ?, critique = ? WHERE id = ?";

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, produit.getReference());
            pstmt.setString(2, produit.getDesignation());
            pstmt.setString(3, produit.getType());
            pstmt.setString(4, produit.getCategorie());
            pstmt.setInt(5, produit.getQuantite());
            pstmt.setInt(6, produit.getStockMinimal());

            if (produit.getDatePeremption() != null) {
                pstmt.setString(7, produit.getDatePeremption().toString());
            } else {
                pstmt.setNull(7, Types.VARCHAR);
            }

            pstmt.setBoolean(8, produit.isCritique());
            pstmt.setInt(9, produit.getId());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour du produit: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static boolean delete(int id) {
        String query = "DELETE FROM produits WHERE id = ?";

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, id);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression du produit: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
