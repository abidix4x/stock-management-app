package com.example.gestion.dao;

import com.example.gestion.DatabaseService;
import com.example.gestion.model.CommandeInterne;
import com.example.gestion.model.LigneCommandeInterne;
import com.example.gestion.model.Produit;
import com.example.gestion.model.Service;
import com.example.gestion.model.Mouvement;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CommandeInterneDAO {

    public static CommandeInterne getById(int id) {
        CommandeInterne commande = null;
        String query = "SELECT * FROM commandes_internes WHERE id = ?";

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                commande = extractCommandeFromResultSet(rs);

                // Charger les lignes de commande
                List<LigneCommandeInterne> lignes = getLignesCommande(commande.getId());
                commande.setLignes(lignes);
            }

            rs.close();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de la commande: " + e.getMessage());
            e.printStackTrace();
        }

        return commande;
    }

    public static List<CommandeInterne> getAll() {
        List<CommandeInterne> commandes = new ArrayList<>();
        String query = "SELECT * FROM commandes_internes ORDER BY date_commande DESC";

        try (Connection conn = DatabaseService.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                CommandeInterne commande = extractCommandeFromResultSet(rs);
                commandes.add(commande);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des commandes: " + e.getMessage());
            e.printStackTrace();
        }

        return commandes;
    }

    public static List<CommandeInterne> searchCommandes(String reference, String statut, Integer serviceId) {
        List<CommandeInterne> commandes = new ArrayList<>();
        StringBuilder queryBuilder = new StringBuilder("SELECT * FROM commandes_internes WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (reference != null && !reference.isEmpty()) {
            queryBuilder.append(" AND reference LIKE ?");
            params.add("%" + reference + "%");
        }

        if (statut != null && !statut.isEmpty()) {
            queryBuilder.append(" AND statut = ?");
            params.add(statut);
        }

        if (serviceId != null) {
            queryBuilder.append(" AND service_id = ?");
            params.add(serviceId);
        }

        queryBuilder.append(" ORDER BY date_commande DESC");

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(queryBuilder.toString())) {

            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                CommandeInterne commande = extractCommandeFromResultSet(rs);
                commandes.add(commande);
            }

            rs.close();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche des commandes: " + e.getMessage());
            e.printStackTrace();
        }

        return commandes;
    }

    private static CommandeInterne extractCommandeFromResultSet(ResultSet rs) throws SQLException {
        CommandeInterne commande = new CommandeInterne();
        commande.setId(rs.getInt("id"));
        commande.setReference(rs.getString("reference"));

        String dateStr = rs.getString("date_commande");
        if (dateStr != null && !dateStr.isEmpty()) {
            commande.setDateCommande(LocalDate.parse(dateStr));
        }

        int serviceId = rs.getInt("service_id");
        Service service = ServiceDAO.getById(serviceId);
        commande.setService(service);

        commande.setStatut(rs.getString("statut"));

        return commande;
    }

    public static List<LigneCommandeInterne> getLignesCommande(int commandeId) {
        List<LigneCommandeInterne> lignes = new ArrayList<>();
        String query = "SELECT * FROM details_commandes_internes WHERE commande_id = ?";

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, commandeId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                LigneCommandeInterne ligne = new LigneCommandeInterne();
                ligne.setId(rs.getInt("id"));

                CommandeInterne commande = new CommandeInterne();
                commande.setId(commandeId);
                ligne.setCommande(commande);

                int produitId = rs.getInt("produit_id");
                Produit produit = ProduitDAO.getById(produitId);
                ligne.setProduit(produit);

                ligne.setQuantite(rs.getInt("quantite"));

                lignes.add(ligne);
            }

            rs.close();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des lignes de commande: " + e.getMessage());
            e.printStackTrace();
        }

        return lignes;
    }

    public static boolean insert(CommandeInterne commande) {
        String query = "INSERT INTO commandes_internes (reference, date_commande, service_id, statut) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, commande.getReference());
            pstmt.setString(2, commande.getDateCommande().toString());
            pstmt.setInt(3, commande.getService().getId());
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

    public static boolean update(CommandeInterne commande) {
        String query = "UPDATE commandes_internes SET reference = ?, date_commande = ?, service_id = ?, statut = ? WHERE id = ?";

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, commande.getReference());
            pstmt.setString(2, commande.getDateCommande().toString());
            pstmt.setInt(3, commande.getService().getId());
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
        String deleteDetailsQuery = "DELETE FROM details_commandes_internes WHERE commande_id = ?";
        String deleteCommandeQuery = "DELETE FROM commandes_internes WHERE id = ?";

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

    public static boolean insertLigneCommande(LigneCommandeInterne ligne) {
        String query = "INSERT INTO details_commandes_internes (commande_id, produit_id, quantite) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, ligne.getCommande().getId());
            pstmt.setInt(2, ligne.getProduit().getId());
            pstmt.setInt(3, ligne.getQuantite());

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

    public static boolean updateLigneCommande(LigneCommandeInterne ligne) {
        String query = "UPDATE details_commandes_internes SET produit_id = ?, quantite = ? WHERE id = ?";

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, ligne.getProduit().getId());
            pstmt.setInt(2, ligne.getQuantite());
            pstmt.setInt(3, ligne.getId());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour de la ligne de commande: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteLigneCommande(int id) {
        String query = "DELETE FROM details_commandes_internes WHERE id = ?";

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

    public static boolean livrerCommande(CommandeInterne commande) {
        Connection conn = null;
        try {
            conn = DatabaseService.getConnection();
            conn.setAutoCommit(false);

            // Mettre à jour le statut de la commande
            String updateCommandeQuery = "UPDATE commandes_internes SET statut = ? WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(updateCommandeQuery)) {
                pstmt.setString(1, "Livrée");
                pstmt.setInt(2, commande.getId());
                pstmt.executeUpdate();
            }

            // Récupérer les lignes de commande
            List<LigneCommandeInterne> lignes = getLignesCommande(commande.getId());

            // Pour chaque ligne, mettre à jour le stock et créer un mouvement
            for (LigneCommandeInterne ligne : lignes) {
                // Mettre à jour le stock du produit
                Produit produit = ligne.getProduit();
                int nouvelleQuantite = produit.getQuantite() - ligne.getQuantite();

                if (nouvelleQuantite < 0) {
                    throw new SQLException("Stock insuffisant pour le produit " + produit.getDesignation());
                }

                produit.setQuantite(nouvelleQuantite);

                String updateProduitQuery = "UPDATE produits SET quantite = ? WHERE id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(updateProduitQuery)) {
                    pstmt.setInt(1, produit.getQuantite());
                    pstmt.setInt(2, produit.getId());
                    pstmt.executeUpdate();
                }

                // Créer un mouvement de sortie
                Mouvement mouvement = new Mouvement();
                mouvement.setProduit(produit);
                mouvement.setType("sortie");
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
            System.err.println("Erreur lors de la livraison de la commande: " + e.getMessage());
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
