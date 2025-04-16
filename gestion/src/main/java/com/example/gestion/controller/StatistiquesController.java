package com.example.gestion.controller;

import com.example.gestion.dao.CommandeExterneDAO;
import com.example.gestion.dao.CommandeInterneDAO;
import javafx.beans.property.SimpleIntegerProperty;
import com.example.gestion.dao.ProduitDAO;
import com.example.gestion.dao.StatistiquesDAO;
import com.example.gestion.model.Mouvement;
import com.example.gestion.model.Produit;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;

import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

public class StatistiquesController implements Initializable {
    // Onglet Mouvements
    @FXML
    private DatePicker dateDebutMouvements;

    @FXML
    private DatePicker dateFinMouvements;

    @FXML
    private ComboBox<Produit> produitComboBox;

    @FXML
    private TableView<Mouvement> mouvementsTable;

    @FXML
    private TableColumn<Mouvement, LocalDate> dateMouvementColumn;

    @FXML
    private TableColumn<Mouvement, String> produitMouvementColumn;

    @FXML
    private TableColumn<Mouvement, String> typeMouvementColumn;

    @FXML
    private TableColumn<Mouvement, Integer> quantiteMouvementColumn;

    @FXML
    private TableColumn<Mouvement, String> referenceCommandeColumn;

    @FXML
    private PieChart mouvementsPieChart;

    @FXML
    private BarChart<String, Number> mouvementsBarChart;

    // Onglet Commandes
    @FXML
    private DatePicker dateDebutCommandes;

    @FXML
    private DatePicker dateFinCommandes;

    @FXML
    private ComboBox<String> typeCommandeComboBox;

    @FXML
    private PieChart commandesPieChart;

    @FXML
    private BarChart<String, Number> commandesBarChart;

    @FXML
    private Label totalCommandesLabel;

    @FXML
    private Label commandesLivreesLabel;

    @FXML
    private Label commandesEnCoursLabel;

    // Onglet Inventaire
    @FXML
    private ComboBox<String> categorieComboBox;

    @FXML
    private ComboBox<String> typeProduitComboBox;

    @FXML
    private PieChart categoriePieChart;

    @FXML
    private BarChart<String, Number> stockBarChart;

    @FXML
    private Label totalProduitsLabel;

    @FXML
    private Label produitsEnAlerteLabel;

    @FXML
    private Label valeurStockLabel;

    private ObservableList<Mouvement> mouvementsList = FXCollections.observableArrayList();
    private NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.FRANCE);
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/yyyy");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            System.out.println("Initialisation du contrôleur de statistiques...");

            // Initialiser les dates
            LocalDate today = LocalDate.now();
            LocalDate firstDayOfMonth = LocalDate.of(today.getYear(), today.getMonth(), 1);
            LocalDate lastDayOfMonth = firstDayOfMonth.plusMonths(1).minusDays(1);

            dateDebutMouvements.setValue(firstDayOfMonth);
            dateFinMouvements.setValue(lastDayOfMonth);
            dateDebutCommandes.setValue(firstDayOfMonth);
            dateFinCommandes.setValue(lastDayOfMonth);

            // Configurer les colonnes de la TableView des mouvements
           //dateMouvementColumn.setCellValueFactory(cellData->new SimpleStringProperty(cellData.getValue().getDateMouvement()));
            produitMouvementColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getProduit().getDesignation()));
            typeMouvementColumn.setCellValueFactory(cellData->new SimpleStringProperty(cellData.getValue().getType()));
            //quantiteMouvementColumn.setCellValueFactory(cellData->new SimpleStringProperty(cellData.getValue().getQuantite()));
            referenceCommandeColumn.setCellValueFactory(cellData->new SimpleStringProperty(cellData.getValue().getReferenceCommande()));

            // Initialiser les ComboBox
            List<Produit> produits = ProduitDAO.getAll();
            produitComboBox.getItems().add(null); // Option pour "Tous les produits"
            produitComboBox.getItems().addAll(produits);

            produitComboBox.setConverter(new StringConverter<Produit>() {
                @Override
                public String toString(Produit produit) {
                    return produit == null ? "Tous les produits" : produit.getDesignation();
                }

                @Override
                public Produit fromString(String string) {
                    return null; // Non utilisé
                }
            });

            typeCommandeComboBox.getItems().addAll("Toutes", "Externes", "Internes");
            typeCommandeComboBox.getSelectionModel().selectFirst();

            categorieComboBox.getItems().add("Toutes");
            categorieComboBox.getItems().addAll("Bureautique", "Informatique", "Technologie", "Tirage",
                    "Nettoyage", "Entretient", "Jardin", "Meuble", "Divers");
            categorieComboBox.getSelectionModel().selectFirst();

            typeProduitComboBox.getItems().addAll("Tous", "consommable", "durable");
            typeProduitComboBox.getSelectionModel().selectFirst();

            // Charger les données initiales
            handleRechercherMouvements(null);
            handleRechercherCommandes(null);
            handleRechercherInventaire(null);

            System.out.println("Initialisation du contrôleur de statistiques terminée");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'initialisation: " + e.getMessage());
        }
    }

    @FXML
    private void handleRechercherMouvements(ActionEvent event) {
        try {
            System.out.println("Recherche des mouvements...");
            mouvementsList.clear();

            LocalDate dateDebut = dateDebutMouvements.getValue();
            LocalDate dateFin = dateFinMouvements.getValue();
            Produit produit = produitComboBox.getValue();

            List<Mouvement> mouvements = StatistiquesDAO.getMouvements(dateDebut, dateFin, produit != null ? produit.getId() : null);
            mouvementsList.addAll(mouvements);
            mouvementsTable.setItems(mouvementsList);

            // Mettre à jour les graphiques
            updateMouvementsCharts(mouvements);

            System.out.println("Mouvements chargés: " + mouvementsList.size());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la recherche des mouvements: " + e.getMessage());
        }
    }

    private void updateMouvementsCharts(List<Mouvement> mouvements) {
        // Graphique en camembert pour les types de mouvements
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        Map<String, Integer> mouvementsByType = StatistiquesDAO.getMouvementsByType(mouvements);

        for (Map.Entry<String, Integer> entry : mouvementsByType.entrySet()) {
            pieChartData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
        }

        mouvementsPieChart.setData(pieChartData);

        // Graphique en barres pour les quantités par produit
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Quantité");

        Map<String, Integer> mouvementsByProduit = StatistiquesDAO.getMouvementsByProduit(mouvements);

        for (Map.Entry<String, Integer> entry : mouvementsByProduit.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        mouvementsBarChart.getData().clear();
        mouvementsBarChart.getData().add(series);
    }

    @FXML
    private void handleRechercherCommandes(ActionEvent event) {
        try {
            System.out.println("Recherche des commandes...");

            LocalDate dateDebut = dateDebutCommandes.getValue();
            LocalDate dateFin = dateFinCommandes.getValue();
            String typeCommande = typeCommandeComboBox.getValue();

            // Statistiques générales
            Map<String, Integer> statsCommandes = StatistiquesDAO.getCommandesStats(dateDebut, dateFin, typeCommande);

            totalCommandesLabel.setText(String.valueOf(statsCommandes.getOrDefault("total", 0)));
            commandesLivreesLabel.setText(String.valueOf(statsCommandes.getOrDefault("livrees", 0)));
            commandesEnCoursLabel.setText(String.valueOf(statsCommandes.getOrDefault("en_cours", 0)));

            // Graphique en camembert pour les statuts de commandes
            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
            Map<String, Integer> commandesByStatut = StatistiquesDAO.getCommandesByStatut(dateDebut, dateFin, typeCommande);

            for (Map.Entry<String, Integer> entry : commandesByStatut.entrySet()) {
                pieChartData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
            }

            commandesPieChart.setData(pieChartData);

            // Graphique en barres pour les commandes par mois
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Nombre de commandes");

            Map<String, Integer> commandesByMonth = StatistiquesDAO.getCommandesByMonth(dateDebut, dateFin, typeCommande);

            for (Map.Entry<String, Integer> entry : commandesByMonth.entrySet()) {
                series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
            }

            commandesBarChart.getData().clear();
            commandesBarChart.getData().add(series);

            System.out.println("Statistiques des commandes chargées");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la recherche des commandes: " + e.getMessage());
        }
    }

    @FXML
    private void handleRechercherInventaire(ActionEvent event) {
        try {
            System.out.println("Recherche de l'inventaire...");

            String categorie = categorieComboBox.getValue().equals("Toutes") ? null : categorieComboBox.getValue();
            String type = typeProduitComboBox.getValue().equals("Tous") ? null : typeProduitComboBox.getValue();

            // Statistiques générales
            Map<String, Object> statsInventaire = StatistiquesDAO.getInventaireStats(categorie, type);

            totalProduitsLabel.setText(String.valueOf(statsInventaire.getOrDefault("total", 0)));
            produitsEnAlerteLabel.setText(String.valueOf(statsInventaire.getOrDefault("en_alerte", 0)));

            Double valeurStock = (Double) statsInventaire.getOrDefault("valeur", 0.0);
            valeurStockLabel.setText(currencyFormat.format(valeurStock));

            // Graphique en camembert pour les catégories
            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
            Map<String, Integer> produitsByCategorie = StatistiquesDAO.getProduitsByCategorie(type);

            for (Map.Entry<String, Integer> entry : produitsByCategorie.entrySet()) {
                pieChartData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
            }

            categoriePieChart.setData(pieChartData);

            // Graphique en barres pour les niveaux de stock
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Nombre de produits");

            Map<String, Integer> produitsByStockLevel = StatistiquesDAO.getProduitsByStockLevel(categorie, type);

            for (Map.Entry<String, Integer> entry : produitsByStockLevel.entrySet()) {
                series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
            }

            stockBarChart.getData().clear();
            stockBarChart.getData().add(series);

            System.out.println("Statistiques d'inventaire chargées");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la recherche de l'inventaire: " + e.getMessage());
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
