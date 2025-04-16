package com.example.gestion.controller;

import com.example.gestion.dao.CommandeExterneDAO;
import com.example.gestion.dao.CommandeInterneDAO;
import com.example.gestion.dao.ProduitDAO;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {
    @FXML
    private Label totalProduitsLabel;

    @FXML
    private Label commandesEnCoursLabel;

    @FXML
    private Label produitsEnAlerteLabel;

    @FXML
    private Label produitsCritiquesLabel;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            System.out.println("Initialisation du tableau de bord...");

            // Charger les données pour le tableau de bord
            int totalProduits = ProduitDAO.getAll().size();
            int produitsEnAlerte = ProduitDAO.getProduitsEnAlerte().size();
            int produitsCritiques = ProduitDAO.getProduitsCritiques().size();
            int cmdex= CommandeExterneDAO.getAll().size();
            int cmdin= CommandeInterneDAO.getAll().size();
            int commandes=cmdex+cmdin;

            // Mettre à jour les labels
            totalProduitsLabel.setText(String.valueOf(totalProduits));
            produitsEnAlerteLabel.setText(String.valueOf(produitsEnAlerte));
            produitsCritiquesLabel.setText(String.valueOf(produitsCritiques));
            commandesEnCoursLabel.setText(String.valueOf(commandes)); // À implémenter plus tard

            System.out.println("Tableau de bord initialisé avec succès");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur lors de l'initialisation du tableau de bord: " + e.getMessage());
        }
    }
}
