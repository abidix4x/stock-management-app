package com.example.gestion.dao;

import com.example.gestion.DatabaseService;
import com.example.gestion.model.Mouvement;
import com.example.gestion.model.Produit;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class StatistiquesDAO {

    public static List<Mouvement> getMouvements(LocalDate dateDebut, LocalDate dateFin, Integer produitId) {
        List<Mouvement> mouvements = new ArrayList<>();
        StringBuilder queryBuilder = new StringBuilder("SELECT * FROM mouvements WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (dateDebut != null) {
            queryBuilder.append(" AND date_mouvement >= ?");
            params.add(dateDebut.toString());
        }

        if (dateFin != null) {
            queryBuilder.append(" AND date_mouvement <= ?");
            params.add(dateFin.toString());
        }

        if (produitId != null) {
            queryBuilder.append(" AND produit_id = ?");
            params.add(produitId);
        }

        queryBuilder.append(" ORDER BY date_mouvement DESC");

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(queryBuilder.toString())) {

            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Mouvement mouvement = new Mouvement();
                mouvement.setId(rs.getInt("id"));

                int prodId = rs.getInt("produit_id");
                Produit produit = ProduitDAO.getById(prodId);
                mouvement.setProduit(produit);

                mouvement.setType(rs.getString("type"));
                mouvement.setQuantite(rs.getInt("quantite"));

                String dateStr = rs.getString("date_mouvement");
                if (dateStr != null && !dateStr.isEmpty()) {
                    mouvement.setDateMouvement(LocalDate.parse(dateStr));
                }

                mouvement.setReferenceCommande(rs.getString("reference_commande"));

                mouvements.add(mouvement);
            }

            rs.close();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des mouvements: " + e.getMessage());
            e.printStackTrace();
        }

        return mouvements;
    }

    public static Map<String, Integer> getMouvementsByType(List<Mouvement> mouvements) {
        Map<String, Integer> result = new HashMap<>();

        for (Mouvement mouvement : mouvements) {
            String type = mouvement.getType();
            int quantite = mouvement.getQuantite();

            result.put(type, result.getOrDefault(type, 0) + quantite);
        }

        return result;
    }

    public static Map<String, Integer> getMouvementsByProduit(List<Mouvement> mouvements) {
        Map<String, Integer> result = new HashMap<>();

        for (Mouvement mouvement : mouvements) {
            String produit = mouvement.getProduit().getDesignation();
            int quantite = mouvement.getQuantite();

            result.put(produit, result.getOrDefault(produit, 0) + quantite);
        }

        // Limiter à 10 produits pour la lisibilité
        if (result.size() > 10) {
            List<Map.Entry<String, Integer>> entries = new ArrayList<>(result.entrySet());
            entries.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

            Map<String, Integer> top10 = new LinkedHashMap<>();
            for (int i = 0; i < Math.min(10, entries.size()); i++) {
                Map.Entry<String, Integer> entry = entries.get(i);
                top10.put(entry.getKey(), entry.getValue());
            }

            return top10;
        }

        return result;
    }

    public static Map<String, Integer> getCommandesStats(LocalDate dateDebut, LocalDate dateFin, String typeCommande) {
        Map<String, Integer> result = new HashMap<>();

        try (Connection conn = DatabaseService.getConnection()) {
            // Total des commandes
            StringBuilder totalQueryBuilder = new StringBuilder();
            List<Object> totalParams = new ArrayList<>();

            if ("Externes".equals(typeCommande)) {
                totalQueryBuilder.append("SELECT COUNT(*) as total FROM commandes_externes WHERE 1=1");
            } else if ("Internes".equals(typeCommande)) {
                totalQueryBuilder.append("SELECT COUNT(*) as total FROM commandes_internes WHERE 1=1");
            } else {
                totalQueryBuilder.append("SELECT (SELECT COUNT(*) FROM commandes_externes WHERE 1=1");

                if (dateDebut != null) {
                    totalQueryBuilder.append(" AND date_commande >= ?");
                    totalParams.add(dateDebut.toString());
                }

                if (dateFin != null) {
                    totalQueryBuilder.append(" AND date_commande <= ?");
                    totalParams.add(dateFin.toString());
                }

                totalQueryBuilder.append(") + (SELECT COUNT(*) FROM commandes_internes WHERE 1=1");

                if (dateDebut != null) {
                    totalQueryBuilder.append(" AND date_commande >= ?");
                    totalParams.add(dateDebut.toString());
                }

                if (dateFin != null) {
                    totalQueryBuilder.append(" AND date_commande <= ?");
                    totalParams.add(dateFin.toString());
                }

                totalQueryBuilder.append(") as total");
            }

            if (!"Toutes".equals(typeCommande)) {
                if (dateDebut != null) {
                    totalQueryBuilder.append(" AND date_commande >= ?");
                    totalParams.add(dateDebut.toString());
                }

                if (dateFin != null) {
                    totalQueryBuilder.append(" AND date_commande <= ?");
                    totalParams.add(dateFin.toString());
                }
            }

            try (PreparedStatement pstmt = conn.prepareStatement(totalQueryBuilder.toString())) {
                for (int i = 0; i < totalParams.size(); i++) {
                    pstmt.setObject(i + 1, totalParams.get(i));
                }

                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    result.put("total", rs.getInt("total"));
                }

                rs.close();
            }

            // Commandes livrées
            StringBuilder livreesQueryBuilder = new StringBuilder();
            List<Object> livreesParams = new ArrayList<>();

            if ("Externes".equals(typeCommande)) {
                livreesQueryBuilder.append("SELECT COUNT(*) as total FROM commandes_externes WHERE statut = 'Réceptionnée'");
            } else if ("Internes".equals(typeCommande)) {
                livreesQueryBuilder.append("SELECT COUNT(*) as total FROM commandes_internes WHERE statut = 'Livrée'");
            } else {
                livreesQueryBuilder.append("SELECT (SELECT COUNT(*) FROM commandes_externes WHERE statut = 'Réceptionnée'");

                if (dateDebut != null) {
                    livreesQueryBuilder.append(" AND date_commande >= ?");
                    livreesParams.add(dateDebut.toString());
                }

                if (dateFin != null) {
                    livreesQueryBuilder.append(" AND date_commande <= ?");
                    livreesParams.add(dateFin.toString());
                }

                livreesQueryBuilder.append(") + (SELECT COUNT(*) FROM commandes_internes WHERE statut = 'Livrée'");

                if (dateDebut != null) {
                    livreesQueryBuilder.append(" AND date_commande >= ?");
                    livreesParams.add(dateDebut.toString());
                }

                if (dateFin != null) {
                    livreesQueryBuilder.append(" AND date_commande <= ?");
                    livreesParams.add(dateFin.toString());
                }

                livreesQueryBuilder.append(") as total");
            }

            if (!"Toutes".equals(typeCommande)) {
                if (dateDebut != null) {
                    livreesQueryBuilder.append(" AND date_commande >= ?");
                    livreesParams.add(dateDebut.toString());
                }

                if (dateFin != null) {
                    livreesQueryBuilder.append(" AND date_commande <= ?");
                    livreesParams.add(dateFin.toString());
                }
            }

            try (PreparedStatement pstmt = conn.prepareStatement(livreesQueryBuilder.toString())) {
                for (int i = 0; i < livreesParams.size(); i++) {
                    pstmt.setObject(i + 1, livreesParams.get(i));
                }

                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    result.put("livrees", rs.getInt("total"));
                }

                rs.close();
            }

            // Commandes en cours
            StringBuilder enCoursQueryBuilder = new StringBuilder();
            List<Object> enCoursParams = new ArrayList<>();

            if ("Externes".equals(typeCommande)) {
                enCoursQueryBuilder.append("SELECT COUNT(*) as total FROM commandes_externes WHERE statut = 'En cours'");
            } else if ("Internes".equals(typeCommande)) {
                enCoursQueryBuilder.append("SELECT COUNT(*) as total FROM commandes_internes WHERE statut = 'En cours'");
            } else {
                enCoursQueryBuilder.append("SELECT (SELECT COUNT(*) FROM commandes_externes WHERE statut = 'En cours'");

                if (dateDebut != null) {
                    enCoursQueryBuilder.append(" AND date_commande >= ?");
                    enCoursParams.add(dateDebut.toString());
                }

                if (dateFin != null) {
                    enCoursQueryBuilder.append(" AND date_commande <= ?");
                    enCoursParams.add(dateFin.toString());
                }

                enCoursQueryBuilder.append(") + (SELECT COUNT(*) FROM commandes_internes WHERE statut = 'En cours'");

                if (dateDebut != null) {
                    enCoursQueryBuilder.append(" AND date_commande >= ?");
                    enCoursParams.add(dateDebut.toString());
                }

                if (dateFin != null) {
                    enCoursQueryBuilder.append(" AND date_commande <= ?");
                    enCoursParams.add(dateFin.toString());
                }

                enCoursQueryBuilder.append(") as total");
            }

            if (!"Toutes".equals(typeCommande)) {
                if (dateDebut != null) {
                    enCoursQueryBuilder.append(" AND date_commande >= ?");
                    enCoursParams.add(dateDebut.toString());
                }

                if (dateFin != null) {
                    enCoursQueryBuilder.append(" AND date_commande <= ?");
                    enCoursParams.add(dateFin.toString());
                }
            }

            try (PreparedStatement pstmt = conn.prepareStatement(enCoursQueryBuilder.toString())) {
                for (int i = 0; i < enCoursParams.size(); i++) {
                    pstmt.setObject(i + 1, enCoursParams.get(i));
                }

                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    result.put("en_cours", rs.getInt("total"));
                }

                rs.close();
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des statistiques de commandes: " + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    public static Map<String, Integer> getCommandesByStatut(LocalDate dateDebut, LocalDate dateFin, String typeCommande) {
        Map<String, Integer> result = new LinkedHashMap<>();

        try (Connection conn = DatabaseService.getConnection()) {
            if ("Externes".equals(typeCommande) || "Toutes".equals(typeCommande)) {
                StringBuilder queryBuilder = new StringBuilder("SELECT statut, COUNT(*) as total FROM commandes_externes WHERE 1=1");
                List<Object> params = new ArrayList<>();

                if (dateDebut != null) {
                    queryBuilder.append(" AND date_commande >= ?");
                    params.add(dateDebut.toString());
                }

                if (dateFin != null) {
                    queryBuilder.append(" AND date_commande <= ?");
                    params.add(dateFin.toString());
                }

                queryBuilder.append(" GROUP BY statut");

                try (PreparedStatement pstmt = conn.prepareStatement(queryBuilder.toString())) {
                    for (int i = 0; i < params.size(); i++) {
                        pstmt.setObject(i + 1, params.get(i));
                    }

                    ResultSet rs = pstmt.executeQuery();
                    while (rs.next()) {
                        String statut = rs.getString("statut") + " (Ext)";
                        int total = rs.getInt("total");
                        result.put(statut, total);
                    }

                    rs.close();
                }
            }

            if ("Internes".equals(typeCommande) || "Toutes".equals(typeCommande)) {
                StringBuilder queryBuilder = new StringBuilder("SELECT statut, COUNT(*) as total FROM commandes_internes WHERE 1=1");
                List<Object> params = new ArrayList<>();

                if (dateDebut != null) {
                    queryBuilder.append(" AND date_commande >= ?");
                    params.add(dateDebut.toString());
                }

                if (dateFin != null) {
                    queryBuilder.append(" AND date_commande <= ?");
                    params.add(dateFin.toString());
                }

                queryBuilder.append(" GROUP BY statut");

                try (PreparedStatement pstmt = conn.prepareStatement(queryBuilder.toString())) {
                    for (int i = 0; i < params.size(); i++) {
                        pstmt.setObject(i + 1, params.get(i));
                    }

                    ResultSet rs = pstmt.executeQuery();
                    while (rs.next()) {
                        String statut = rs.getString("statut") + " (Int)";
                        int total = rs.getInt("total");
                        result.put(statut, total);
                    }

                    rs.close();
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des commandes par statut: " + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    public static Map<String, Integer> getCommandesByMonth(LocalDate dateDebut, LocalDate dateFin, String typeCommande) {
        Map<String, Integer> result = new LinkedHashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yyyy");

        try (Connection conn = DatabaseService.getConnection()) {
            if ("Externes".equals(typeCommande) || "Toutes".equals(typeCommande)) {
                StringBuilder queryBuilder = new StringBuilder("SELECT strftime('%m/%Y', date_commande) as month, COUNT(*) as total FROM commandes_externes WHERE 1=1");
                List<Object> params = new ArrayList<>();

                if (dateDebut != null) {
                    queryBuilder.append(" AND date_commande >= ?");
                    params.add(dateDebut.toString());
                }

                if (dateFin != null) {
                    queryBuilder.append(" AND date_commande <= ?");
                    params.add(dateFin.toString());
                }

                queryBuilder.append(" GROUP BY month ORDER BY date_commande");

                try (PreparedStatement pstmt = conn.prepareStatement(queryBuilder.toString())) {
                    for (int i = 0; i < params.size(); i++) {
                        pstmt.setObject(i + 1, params.get(i));
                    }

                    ResultSet rs = pstmt.executeQuery();
                    while (rs.next()) {
                        String month = rs.getString("month");
                        int total = rs.getInt("total");
                        result.put(month, result.getOrDefault(month, 0) + total);
                    }

                    rs.close();
                }
            }

            if ("Internes".equals(typeCommande) || "Toutes".equals(typeCommande)) {
                StringBuilder queryBuilder = new StringBuilder("SELECT strftime('%m/%Y', date_commande) as month, COUNT(*) as total FROM commandes_internes WHERE 1=1");
                List<Object> params = new ArrayList<>();

                if (dateDebut != null) {
                    queryBuilder.append(" AND date_commande >= ?");
                    params.add(dateDebut.toString());
                }

                if (dateFin != null) {
                    queryBuilder.append(" AND date_commande <= ?");
                    params.add(dateFin.toString());
                }

                queryBuilder.append(" GROUP BY month ORDER BY date_commande");

                try (PreparedStatement pstmt = conn.prepareStatement(queryBuilder.toString())) {
                    for (int i = 0; i < params.size(); i++) {
                        pstmt.setObject(i + 1, params.get(i));
                    }

                    ResultSet rs = pstmt.executeQuery();
                    while (rs.next()) {
                        String month = rs.getString("month");
                        int total = rs.getInt("total");
                        result.put(month, result.getOrDefault(month, 0) + total);
                    }

                    rs.close();
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des commandes par mois: " + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    public static Map<String, Object> getInventaireStats(String categorie, String type) {
        Map<String, Object> result = new HashMap<>();

        try (Connection conn = DatabaseService.getConnection()) {
            // Total des produits
            StringBuilder totalQueryBuilder = new StringBuilder("SELECT COUNT(*) as total FROM produits WHERE 1=1");
            List<Object> totalParams = new ArrayList<>();

            if (categorie != null) {
                totalQueryBuilder.append(" AND categorie = ?");
                totalParams.add(categorie);
            }

            if (type != null) {
                totalQueryBuilder.append(" AND type = ?");
                totalParams.add(type);
            }

            try (PreparedStatement pstmt = conn.prepareStatement(totalQueryBuilder.toString())) {
                for (int i = 0; i < totalParams.size(); i++) {
                    pstmt.setObject(i + 1, totalParams.get(i));
                }

                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    result.put("total", rs.getInt("total"));
                }

                rs.close();
            }

            // Produits en alerte
            StringBuilder alerteQueryBuilder = new StringBuilder("SELECT COUNT(*) as total FROM produits WHERE quantite <= stock_minimal");
            List<Object> alerteParams = new ArrayList<>();

            if (categorie != null) {
                alerteQueryBuilder.append(" AND categorie = ?");
                alerteParams.add(categorie);
            }

            if (type != null) {
                alerteQueryBuilder.append(" AND type = ?");
                alerteParams.add(type);
            }

            try (PreparedStatement pstmt = conn.prepareStatement(alerteQueryBuilder.toString())) {
                for (int i = 0; i < alerteParams.size(); i++) {
                    pstmt.setObject(i + 1, alerteParams.get(i));
                }

                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    result.put("en_alerte", rs.getInt("total"));
                }

                rs.close();
            }

            // Valeur du stock (estimation basée sur les dernières commandes externes)
            StringBuilder valeurQueryBuilder = new StringBuilder(
                    "SELECT SUM(p.quantite * COALESCE((" +
                            "  SELECT AVG(d.prix_unitaire) " +
                            "  FROM details_commandes_externes d " +
                            "  JOIN commandes_externes c ON d.commande_id = c.id " +
                            "  WHERE d.produit_id = p.id " +
                            "  ORDER BY c.date_commande DESC " +
                            "  LIMIT 1" +
                            "), 0)) as valeur " +
                            "FROM produits p WHERE 1=1");

            List<Object> valeurParams = new ArrayList<>();

            if (categorie != null) {
                valeurQueryBuilder.append(" AND p.categorie = ?");
                valeurParams.add(categorie);
            }

            if (type != null) {
                valeurQueryBuilder.append(" AND p.type = ?");
                valeurParams.add(type);
            }

            try (PreparedStatement pstmt = conn.prepareStatement(valeurQueryBuilder.toString())) {
                for (int i = 0; i < valeurParams.size(); i++) {
                    pstmt.setObject(i + 1, valeurParams.get(i));
                }

                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    result.put("valeur", rs.getDouble("valeur"));
                }

                rs.close();
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des statistiques d'inventaire: " + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    public static Map<String, Integer> getProduitsByCategorie(String type) {
        Map<String, Integer> result = new LinkedHashMap<>();

        StringBuilder queryBuilder = new StringBuilder("SELECT categorie, COUNT(*) as total FROM produits WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (type != null) {
            queryBuilder.append(" AND type = ?");
            params.add(type);
        }

        queryBuilder.append(" GROUP BY categorie ORDER BY total DESC");

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(queryBuilder.toString())) {

            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String categorie = rs.getString("categorie");
                int total = rs.getInt("total");
                result.put(categorie, total);
            }

            rs.close();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des produits par catégorie: " + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    public static Map<String, Integer> getProduitsByStockLevel(String categorie, String type) {
        Map<String, Integer> result = new LinkedHashMap<>();
        result.put("Rupture", 0);
        result.put("Critique", 0);
        result.put("Normal", 0);
        result.put("Abondant", 0);

        StringBuilder queryBuilder = new StringBuilder(
                "SELECT " +
                        "SUM(CASE WHEN quantite = 0 THEN 1 ELSE 0 END) as rupture, " +
                        "SUM(CASE WHEN quantite > 0 AND quantite <= stock_minimal THEN 1 ELSE 0 END) as critique, " +
                        "SUM(CASE WHEN quantite > stock_minimal AND quantite <= stock_minimal * 2 THEN 1 ELSE 0 END) as normal, " +
                        "SUM(CASE WHEN quantite > stock_minimal * 2 THEN 1 ELSE 0 END) as abondant " +
                        "FROM produits WHERE 1=1");

        List<Object> params = new ArrayList<>();

        if (categorie != null) {
            queryBuilder.append(" AND categorie = ?");
            params.add(categorie);
        }

        if (type != null) {
            queryBuilder.append(" AND type = ?");
            params.add(type);
        }

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(queryBuilder.toString())) {

            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                result.put("Rupture", rs.getInt("rupture"));
                result.put("Critique", rs.getInt("critique"));
                result.put("Normal", rs.getInt("normal"));
                result.put("Abondant", rs.getInt("abondant"));
            }

            rs.close();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des produits par niveau de stock: " + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }
}
