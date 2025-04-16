package com.example.gestion.dao;

import com.example.gestion.DatabaseService;
import com.example.gestion.model.CommandeExterne;
import com.example.gestion.model.Fournisseur;
import com.example.gestion.model.LigneCommandeExterne;
import com.example.gestion.model.Produit;
import com.example.gestion.model.Mouvement;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CommandeExterneDAO {

    public static CommandeExterne getById(int id) {
        CommandeExterne commande = null;
        String query = "SELECT * FROM commandes_externes WHERE id = ?";

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                commande = extractCommandeFromResultSet(rs);

                // Charger les lignes de commande
                List<LigneCommandeExterne> lignes = getLignesCommande(commande.getId());
                commande.setLignes(lignes);
            }

            rs.close();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de la commande: " + e.getMessage());
            e.printStackTrace();
        }

        return commande;
    }

    public static List<CommandeExterne> getAll() {
        List<CommandeExterne> commandes = new ArrayList<>();
        String query = "SELECT * FROM commandes_externes ORDER BY date_commande DESC";

        try (Connection conn = DatabaseService.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                CommandeExterne commande = extractCommandeFromResultSet(rs);
                commandes.add(commande);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des commandes: " + e.getMessage());
            e.printStackTrace();
        }

        return commandes;
    }

    public static List<CommandeExterne> searchCommandes(String reference, String statut, Integer fournisseurId) {
        List<CommandeExterne> commandes = new ArrayList<>();
        StringBuilder queryBuilder = new StringBuilder("SELECT * FROM commandes_externes WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (reference != null && !reference.isEmpty()) {
            queryBuilder.append(" AND reference LIKE ?");
            params.add("%" + reference + "%");
        }

        if (statut != null && !statut.isEmpty()) {
            queryBuilder.append(" AND statut = ?");
            params.add(statut);
        }

        if (fournisseurId != null) {
            queryBuilder.append(" AND fournisseur_id = ?");
            params.add(fournisseurId);
        }

        queryBuilder.append(" ORDER BY date_commande DESC");

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(queryBuilder.toString())) {

            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                CommandeExterne commande = extractCommandeFromResultSet(rs);
                commandes.add(commande);
            }

            rs.close();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche des commandes: " + e.getMessage());
            e.printStackTrace();
        }

        return commandes;
    }

    private static CommandeExterne extractCommandeFromResultSet(ResultSet rs) throws SQLException {
        CommandeExterne commande = new CommandeExterne();
        commande.setId(rs.getInt("id"));
        commande.setReference(rs.getString("reference"));

        String dateStr = rs.getString("date_commande");
        if (dateStr != null && !dateStr.isEmpty()) {
            commande.setDateCommande(LocalDate.parse(dateStr));
        }

        int fournisseurId = rs.getInt("fournisseur_id");
        Fournisseur fournisseur = FournisseurDAO.getById(fournisseurId);
        commande.setFournisseur(fournisseur);

        commande.setStatut(rs.getString("statut"));

        return commande;
    }

    public static List<LigneCommandeExterne> getLignesCommande(int commandeId) {
        List<LigneCommandeExterne> lignes = new ArrayList<>();
        String query = "SELECT * FROM details_commandes_externes WHERE commande_id = ?";

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, commandeId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                LigneCommandeExterne ligne = new LigneCommandeExterne();
                ligne.setId(rs.getInt("id"));

                CommandeExterne commande = new CommandeExterne();
                commande.setId(commandeId);
                ligne.setCommande(commande);

                int produitId = rs.getInt("produit_id");
                Produit produit = ProduitDAO.getById(produitId);
                ligne.setProduit(produit);

                ligne.setQuantite(rs.getInt("quantite"));
                ligne.setPrixUnitaire(rs.getDouble("prix_unitaire"));

                lignes.add(ligne);
            }

            rs.close();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des lignes de commande: " + e.getMessage());
            e.printStackTrace();
        }

        return lignes;
    }

    public static boolean insert(CommandeExterne commande) {
        String query = "INSERT INTO commandes_externes (reference, date_commande, fournisseur_id, statut) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, commande.getReference());
            pstmt.setString(2, commande.getDateCommande().toString());
            pstmt.setInt(3, commande.getFournisseur().getId());
            pstmt.setString(4, commande.getStatut());

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    commande.setId(rs.getInt(1));
                }
                rs.close();
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'insertion de la commande: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static boolean update(CommandeExterne commande) {
        String query = "UPDATE commandes_externes SET reference = ?, date_commande = ?, fournisseur_id = ?, statut = ? WHERE id = ?";

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, commande.getReference());
            pstmt.setString(2, commande.getDateCommande().toString());
            pstmt.setInt(3, commande.getFournisseur().getId());
            pstmt.setString(4, commande.getStatut());
            pstmt.setInt(5, commande.getId());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour de la commande: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static boolean delete(int id) {
        // D'abord supprimer les lignes de commande
        String deleteDetailsQuery = "DELETE FROM details_commandes_externes WHERE commande_id = ?";
        String deleteCommandeQuery = "DELETE FROM commandes_externes WHERE id = ?";

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement pstmtDetails = conn.prepareStatement(deleteDetailsQuery);
             PreparedStatement pstmtCommande = conn.prepareStatement(deleteCommandeQuery)) {

            // Supprimer les lignes de commande
            pstmtDetails.setInt(1, id);
            pstmtDetails.executeUpdate();

            // Supprimer la commande
            pstmtCommande.setInt(1, id);
            int rowsAffected = pstmtCommande.executeUpdate();

            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression de la commande: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static boolean insertLigneCommande(LigneCommandeExterne ligne) {
        String query = "INSERT INTO details_commandes_externes (commande_id, produit_id, quantite, prix_unitaire) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, ligne.getCommande().getId());
            pstmt.setInt(2, ligne.getProduit().getId());
            pstmt.setInt(3, ligne.getQuantite());
            pstmt.setDouble(4, ligne.getPrixUnitaire());

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    ligne.setId(rs.getInt(1));
                }
                rs.close();
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'insertion de la ligne de commande: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateLigneCommande(LigneCommandeExterne ligne) {
        String query = "UPDATE details_commandes_externes SET produit_id = ?, quantite = ?, prix_unitaire = ? WHERE id = ?";

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, ligne.getProduit().getId());
            pstmt.setInt(2, ligne.getQuantite());
            pstmt.setDouble(3, ligne.getPrixUnitaire());
            pstmt.setInt(4, ligne.getId());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour de la ligne de commande: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteLigneCommande(int id) {
        String query = "DELETE FROM details_commandes_externes WHERE id = ?";

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, id);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression de la ligne de commande: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static boolean receptionnerCommande(CommandeExterne commande) {
        Connection conn = null;
        try {
            conn = DatabaseService.getConnection();
            conn.setAutoCommit(false);

            // Mettre à jour le statut de la commande
            String updateCommandeQuery = "UPDATE commandes_externes SET statut = ? WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(updateCommandeQuery)) {
                pstmt.setString(1, "Réceptionnée");
                pstmt.setInt(2, commande.getId());
                pstmt.executeUpdate();
            }

            // Récupérer les lignes de commande
            List<LigneCommandeExterne> lignes = getLignesCommande(commande.getId());

            // Pour chaque ligne, mettre à jour le stock et créer un mouvement
            for (LigneCommandeExterne ligne : lignes) {
                // Mettre à jour le stock du produit
                Produit produit = ligne.getProduit();
                produit.setQuantite(produit.getQuantite() + ligne.getQuantite());

                String updateProduitQuery = "UPDATE produits SET quantite = ? WHERE id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(updateProduitQuery)) {
                    pstmt.setInt(1, produit.getQuantite());
                    pstmt.setInt(2, produit.getId());
                    pstmt.executeUpdate();
                }

                // Créer un mouvement d'entrée
                Mouvement mouvement = new Mouvement();
                mouvement.setProduit(produit);
                mouvement.setType("entrée");
                mouvement.setQuantite(ligne.getQuantite());
                mouvement.setDateMouvement(LocalDate.now());
                mouvement.setReferenceCommande(commande.getReference());

                String insertMouvementQuery = "INSERT INTO mouvements (produit_id, type, quantite, date_mouvement, reference_commande) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(insertMouvementQuery)) {
                    pstmt.setInt(1, produit.getId());
                    pstmt.setString(2, mouvement.getType());
                    pstmt.setInt(3, mouvement.getQuantite());
                    pstmt.setString(4, mouvement.getDateMouvement().toString());
                    pstmt.setString(5, mouvement.getReferenceCommande());
                    pstmt.executeUpdate();
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la réception de la commande: " + e.getMessage());
            e.printStackTrace();

            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }

            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
