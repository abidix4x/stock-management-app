package com.example.gestion;

import java.sql.*;

public class DatabaseService {
    private static final String DB_URL = "jdbc:sqlite:gestion_stock.db";
    private static Connection connection;

    public static void initializeDatabase() {
        try {
            // Charger explicitement le driver
            Class.forName("org.sqlite.JDBC");

            // Créer la connexion
            connection = DriverManager.getConnection(DB_URL);

            // Créer les tables si elles n'existent pas
            createTables();

            System.out.println("Base de données initialisée avec succès");
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Erreur lors de l'initialisation de la base de données: " + e.getMessage());
            e.printStackTrace();
            showErrorAlert("Erreur de base de données",
                    "Erreur lors de l'initialisation de la base de données: " + e.getMessage());
        }
    }

    private static void showErrorAlert(String title, String message) {
        javafx.application.Platform.runLater(() -> {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    private static void createTables() throws SQLException {
        Statement stmt = connection.createStatement();

        // Table des produits
        stmt.execute("CREATE TABLE IF NOT EXISTS produits (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "reference TEXT NOT NULL," +
                "designation TEXT NOT NULL," +
                "type TEXT NOT NULL," + // consommable ou durable
                "categorie TEXT NOT NULL," +
                "quantite INTEGER NOT NULL," +
                "stock_minimal INTEGER NOT NULL," +
                "date_peremption TEXT," +
                "critique BOOLEAN DEFAULT 0" +
                ")");

        System.out.println("Table 'produits' vérifiée/créée avec succès");

        // Vérifier si la table produits contient des données
        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM produits");
        if (rs.next()) {
            int count = rs.getInt("count");
            System.out.println("Nombre de produits dans la base de données: " + count);
        }
        rs.close();

        // Table des locaux
        stmt.execute("CREATE TABLE IF NOT EXISTS locaux (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "code TEXT NOT NULL," +
                "designation TEXT NOT NULL," +
                "type TEXT NOT NULL" +
                ")");

        // Table des fournisseurs
        stmt.execute("CREATE TABLE IF NOT EXISTS fournisseurs (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "nom TEXT NOT NULL," +
                "adresse TEXT," +
                "telephone TEXT," +
                "email TEXT" +
                ")");

        // Table des services (consommateurs)
        stmt.execute("CREATE TABLE IF NOT EXISTS services (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "nom TEXT NOT NULL," +
                "responsable TEXT," +
                "telephone TEXT," +
                "email TEXT" +
                ")");

        // Table des commandes externes (achats)
        stmt.execute("CREATE TABLE IF NOT EXISTS commandes_externes (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "reference TEXT NOT NULL," +
                "date_commande TEXT NOT NULL," +
                "fournisseur_id INTEGER NOT NULL," +
                "statut TEXT NOT NULL," +
                "FOREIGN KEY (fournisseur_id) REFERENCES fournisseurs(id)" +
                ")");

        // Table des détails de commandes externes
        stmt.execute("CREATE TABLE IF NOT EXISTS details_commandes_externes (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "commande_id INTEGER NOT NULL," +
                "produit_id INTEGER NOT NULL," +
                "quantite INTEGER NOT NULL," +
                "prix_unitaire REAL NOT NULL," +
                "FOREIGN KEY (commande_id) REFERENCES commandes_externes(id)," +
                "FOREIGN KEY (produit_id) REFERENCES produits(id)" +
                ")");

        // Table des commandes internes (consommation)
        stmt.execute("CREATE TABLE IF NOT EXISTS commandes_internes (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "reference TEXT NOT NULL," +
                "date_commande TEXT NOT NULL," +
                "service_id INTEGER NOT NULL," +
                "statut TEXT NOT NULL," +
                "FOREIGN KEY (service_id) REFERENCES services(id)" +
                ")");

        // Table des détails de commandes internes
        stmt.execute("CREATE TABLE IF NOT EXISTS details_commandes_internes (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "commande_id INTEGER NOT NULL," +
                "produit_id INTEGER NOT NULL," +
                "quantite INTEGER NOT NULL," +
                "FOREIGN KEY (commande_id) REFERENCES commandes_internes(id)," +
                "FOREIGN KEY (produit_id) REFERENCES produits(id)" +
                ")");

        // Table de stockage (relation entre produits et locaux)
        stmt.execute("CREATE TABLE IF NOT EXISTS stockage (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "produit_id INTEGER NOT NULL," +
                "local_id INTEGER NOT NULL," +
                "quantite INTEGER NOT NULL," +
                "FOREIGN KEY (produit_id) REFERENCES produits(id)," +
                "FOREIGN KEY (local_id) REFERENCES locaux(id)" +
                ")");

        // Table des mouvements de stock
        stmt.execute("CREATE TABLE IF NOT EXISTS mouvements (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "produit_id INTEGER NOT NULL," +
                "type TEXT NOT NULL," + // entrée ou sortie
                "quantite INTEGER NOT NULL," +
                "date_mouvement TEXT NOT NULL," +
                "reference_commande TEXT," +
                "FOREIGN KEY (produit_id) REFERENCES produits(id)" +
                ")");

        stmt.close();
    }

    public static Connection getConnection() throws SQLException {
        // Créer une nouvelle connexion à chaque fois pour éviter les problèmes de connexion fermée
        return DriverManager.getConnection(DB_URL);
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la fermeture de la connexion: " + e.getMessage());
        }
    }
}
