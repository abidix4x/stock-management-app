package com.example.gestion.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    @FXML
    private BorderPane mainPane;

    @FXML
    private Label titleLabel;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Afficher un message de bienvenue par défaut
        titleLabel.setText("Tableau de bord");

        // Charger le tableau de bord par défaut
        handleDashboard(null);
    }

    @FXML
    private void handleDashboard(ActionEvent event) {
        try {
            System.out.println("Chargement du tableau de bord...");
            String dashboardPath = "/com/example/gestion/dashboard-view.fxml";
            System.out.println("Tentative de chargement de: " + dashboardPath);

            URL resource = getClass().getResource(dashboardPath);
            if (resource == null) {
                throw new IOException("Ressource non trouvée: " + dashboardPath);
            }

            FXMLLoader loader = new FXMLLoader(resource);
            Parent view = loader.load();
            mainPane.setCenter(view);
            titleLabel.setText("Tableau de bord");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement du tableau de bord: " + e.getMessage());
        }
    }

    @FXML
    private void handleProduits(ActionEvent event) {
        try {
            System.out.println("Chargement de la gestion des produits...");
            String produitsPath = "/com/example/gestion/produits-view.fxml";
            System.out.println("Tentative de chargement de: " + produitsPath);

            URL resource = getClass().getResource(produitsPath);
            if (resource == null) {
                throw new IOException("Ressource non trouvée: " + produitsPath);
            }

            FXMLLoader loader = new FXMLLoader(resource);
            Parent view = loader.load();
            mainPane.setCenter(view);
            titleLabel.setText("Gestion des Produits");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement de la gestion des produits: " + e.getMessage());
        }
    }

    @FXML
    private void handleLocaux(ActionEvent event) {
        try {
            System.out.println("Chargement de la gestion des locaux...");
            String locauxPath = "/com/example/gestion/locaux-view.fxml";
            System.out.println("Tentative de chargement de: " + locauxPath);

            URL resource = getClass().getResource(locauxPath);
            if (resource == null) {
                throw new IOException("Ressource non trouvée: " + locauxPath);
            }

            FXMLLoader loader = new FXMLLoader(resource);
            Parent view = loader.load();
            mainPane.setCenter(view);
            titleLabel.setText("Gestion des Locaux");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement de la gestion des locaux: " + e.getMessage());
        }
    }

    @FXML
    private void handleFournisseurs(ActionEvent event) {
        try {
            System.out.println("Chargement de la gestion des fournisseurs...");
            String fournisseursPath = "/com/example/gestion/fournisseurs-view.fxml";
            System.out.println("Tentative de chargement de: " + fournisseursPath);

            URL resource = getClass().getResource(fournisseursPath);
            if (resource == null) {
                throw new IOException("Ressource non trouvée: " + fournisseursPath);
            }

            FXMLLoader loader = new FXMLLoader(resource);
            Parent view = loader.load();
            mainPane.setCenter(view);
            titleLabel.setText("Gestion des Fournisseurs");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement de la gestion des fournisseurs: " + e.getMessage());
        }
    }

    @FXML
    private void handleServices(ActionEvent event) {
        try {
            System.out.println("Chargement de la gestion des services...");
            String servicesPath = "/com/example/gestion/services-view.fxml";
            System.out.println("Tentative de chargement de: " + servicesPath);

            URL resource = getClass().getResource(servicesPath);
            if (resource == null) {
                throw new IOException("Ressource non trouvée: " + servicesPath);
            }

            FXMLLoader loader = new FXMLLoader(resource);
            Parent view = loader.load();
            mainPane.setCenter(view);
            titleLabel.setText("Gestion des Services");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement de la gestion des services: " + e.getMessage());
        }
    }

    @FXML
    private void handleCommandesExternes(ActionEvent event) {
        try {
            System.out.println("Chargement de la gestion des commandes externes...");
            String commandesExternesPath = "/com/example/gestion/commandes-externes-view.fxml";
            System.out.println("Tentative de chargement de: " + commandesExternesPath);

            URL resource = getClass().getResource(commandesExternesPath);
            if (resource == null) {
                throw new IOException("Ressource non trouvée: " + commandesExternesPath);
            }

            FXMLLoader loader = new FXMLLoader(resource);
            Parent view = loader.load();
            mainPane.setCenter(view);
            titleLabel.setText("Gestion des Commandes Externes");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement de la gestion des commandes externes: " + e.getMessage());
        }
    }

    @FXML
    private void handleCommandesInternes(ActionEvent event) {
        try {
            System.out.println("Chargement de la gestion des commandes internes...");
            String commandesInternesPath = "/com/example/gestion/commandes-internes-view.fxml";
            System.out.println("Tentative de chargement de: " + commandesInternesPath);

            URL resource = getClass().getResource(commandesInternesPath);
            if (resource == null) {
                throw new IOException("Ressource non trouvée: " + commandesInternesPath);
            }

            FXMLLoader loader = new FXMLLoader(resource);
            Parent view = loader.load();
            mainPane.setCenter(view);
            titleLabel.setText("Gestion des Commandes Internes");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement de la gestion des commandes internes: " + e.getMessage());
        }
    }

    @FXML
    private void handleInventaire(ActionEvent event) {
        try {
            System.out.println("Chargement de la gestion de l'inventaire...");
            String inventairePath = "/com/example/gestion/inventaire-view.fxml";
            System.out.println("Tentative de chargement de: " + inventairePath);

            URL resource = getClass().getResource(inventairePath);
            if (resource == null) {
                throw new IOException("Ressource non trouvée: " + inventairePath);
            }

            FXMLLoader loader = new FXMLLoader(resource);
            Parent view = loader.load();
            mainPane.setCenter(view);
            titleLabel.setText("Gestion de l'Inventaire");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement de la gestion de l'inventaire: " + e.getMessage());
        }
    }

    @FXML
    private void handleStatistiques(ActionEvent event) {
        try {
            System.out.println("Chargement des statistiques...");
            String statistiquesPath = "/com/example/gestion/statistiques-view.fxml";
            System.out.println("Tentative de chargement de: " + statistiquesPath);

            URL resource = getClass().getResource(statistiquesPath);
            if (resource == null) {
                throw new IOException("Ressource non trouvée: " + statistiquesPath);
            }

            FXMLLoader loader = new FXMLLoader(resource);
            Parent view = loader.load();
            mainPane.setCenter(view);
            titleLabel.setText("Statistiques");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement des statistiques: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
