package com.example.gestion.dao;

import com.example.gestion.DatabaseService;
import com.example.gestion.model.Local;
import com.example.gestion.model.Produit;
import com.example.gestion.model.Stockage;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StockageDAO {

    public static Stockage getById(int id) {
        Stockage stockage = null;
        String query = "SELECT * FROM stockage WHERE id = ?";

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                stockage = extractStockageFromResultSet(rs);
            }

            rs.close();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération du stockage: " + e.getMessage());
            e.printStackTrace();
        }

        return stockage;
    }

    public static List<Stockage> getAll() {
        List<Stockage> stockages = new ArrayList<>();
        String query = "SELECT * FROM stockage";

        try (Connection conn = DatabaseService.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Stockage stockage = extractStockageFromResultSet(rs);
                stockages.add(stockage);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des stockages: " + e.getMessage());
            e.printStackTrace();
        }

        return stockages;
    }

    public static List<Stockage> getByLocal(int localId) {
        List<Stockage> stockages = new ArrayList<>();
        String query = "SELECT * FROM stockage WHERE local_id = ?";

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, localId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Stockage stockage = extractStockageFromResultSet(rs);
                stockages.add(stockage);
            }

            rs.close();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des stockages par local: " + e.getMessage());
            e.printStackTrace();
        }

        return stockages;
    }

    public static List<Stockage> getByProduit(int produitId) {
        List<Stockage> stockages = new ArrayList<>();
        String query = "SELECT * FROM stockage WHERE produit_id = ?";

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, produitId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Stockage stockage = extractStockageFromResultSet(rs);
                stockages.add(stockage);
            }

            rs.close();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des stockages par produit: " + e.getMessage());
            e.printStackTrace();
        }

        return stockages;
    }

    public static List<Stockage> searchByProduit(String keyword) {
        List<Stockage> stockages = new ArrayList<>();
        String query = "SELECT s.* FROM stockage s " +
                "JOIN produits p ON s.produit_id = p.id " +
                "WHERE p.reference LIKE ? OR p.designation LIKE ?";

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, "%" + keyword + "%");
            pstmt.setString(2, "%" + keyword + "%");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Stockage stockage = extractStockageFromResultSet(rs);
                stockages.add(stockage);
            }

            rs.close();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche des stockages par produit: " + e.getMessage());
            e.printStackTrace();
        }

        return stockages;
    }

    public static List<Stockage> searchByLocalAndProduit(int localId, String keyword) {
        List<Stockage> stockages = new ArrayList<>();
        String query = "SELECT s.* FROM stockage s " +
                "JOIN produits p ON s.produit_id = p.id " +
                "WHERE s.local_id = ? AND (p.reference LIKE ? OR p.designation LIKE ?)";

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, localId);
            pstmt.setString(2, "%" + keyword + "%");
            pstmt.setString(3, "%" + keyword + "%");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Stockage stockage = extractStockageFromResultSet(rs);
                stockages.add(stockage);
            }

            rs.close();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche des stockages par local et produit: " + e.getMessage());
            e.printStackTrace();
        }

        return stockages;
    }

    private static Stockage extractStockageFromResultSet(ResultSet rs) throws SQLException {
        Stockage stockage = new Stockage();
        stockage.setId(rs.getInt("id"));

        int produitId = rs.getInt("produit_id");
        Produit produit = ProduitDAO.getById(produitId);
        stockage.setProduit(produit);

        int localId = rs.getInt("local_id");
        Local local = LocalDAO.getById(localId);
        stockage.setLocal(local);

        stockage.setQuantite(rs.getInt("quantite"));

        return stockage;
    }

    public static boolean insert(Stockage stockage) {
        // Vérifier si un stockage existe déjà pour ce produit et ce local
        String checkQuery = "SELECT id FROM stockage WHERE produit_id = ? AND local_id = ?";
        String insertQuery = "INSERT INTO stockage (produit_id, local_id, quantite) VALUES (?, ?, ?)";
        String updateQuery = "UPDATE stockage SET quantite = quantite + ? WHERE produit_id = ? AND local_id = ?";

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
             PreparedStatement insertStmt = conn.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {

            checkStmt.setInt(1, stockage.getProduit().getId());
            checkStmt.setInt(2, stockage.getLocal().getId());
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                // Un stockage existe déjà, mettre à jour la quantité
                int id = rs.getInt("id");
                stockage.setId(id);

                updateStmt.setInt(1, stockage.getQuantite());
                updateStmt.setInt(2, stockage.getProduit().getId());
                updateStmt.setInt(3, stockage.getLocal().getId());

                int rowsAffected = updateStmt.executeUpdate();
                return rowsAffected > 0;
            } else {
                // Insérer un nouveau stockage
                insertStmt.setInt(1, stockage.getProduit().getId());
                insertStmt.setInt(2, stockage.getLocal().getId());
                insertStmt.setInt(3, stockage.getQuantite());

                int rowsAffected = insertStmt.executeUpdate();

                if (rowsAffected > 0) {
                    ResultSet generatedKeys = insertStmt.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        stockage.setId(generatedKeys.getInt(1));
                    }
                    generatedKeys.close();
                    return true;
                }
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'insertion du stockage: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static boolean update(Stockage stockage) {
        String query = "UPDATE stockage SET produit_id = ?, local_id = ?, quantite = ? WHERE id = ?";

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, stockage.getProduit().getId());
            pstmt.setInt(2, stockage.getLocal().getId());
            pstmt.setInt(3, stockage.getQuantite());
            pstmt.setInt(4, stockage.getId());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour du stockage: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static boolean delete(int id) {
        String query = "DELETE FROM stockage WHERE id = ?";

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, id);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression du stockage: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
