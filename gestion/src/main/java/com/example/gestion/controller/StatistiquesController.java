package com.example.gestion.controller;

import com.example.gestion.dao.StatistiquesDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.File;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.ResourceBundle;

public class StatistiquesController implements Initializable {
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

    private NumberFormat currencyFormat;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/yyyy");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            System.out.println("Initialisation du contrôleur de statistiques...");

            // Configure currency format for TND (Tunisian Dinar)
            DecimalFormatSymbols symbols = new DecimalFormatSymbols();
            symbols.setDecimalSeparator('.');
            symbols.setGroupingSeparator(' ');
            DecimalFormat decimalFormat = new DecimalFormat("#,##0.000", symbols);
            decimalFormat.setGroupingUsed(true);
            currencyFormat = decimalFormat;

            // Initialiser les dates
            LocalDate today = LocalDate.now();
            LocalDate firstDayOfMonth = LocalDate.of(today.getYear(), today.getMonth(), 1);
            LocalDate lastDayOfMonth = firstDayOfMonth.plusMonths(1).minusDays(1);

            dateDebutCommandes.setValue(firstDayOfMonth);
            dateFinCommandes.setValue(lastDayOfMonth);

            // Initialiser les ComboBox
            typeCommandeComboBox.getItems().addAll("Toutes", "Externes", "Internes");
            typeCommandeComboBox.getSelectionModel().selectFirst();

            categorieComboBox.getItems().add("Toutes");
            categorieComboBox.getItems().addAll("Bureautique", "Informatique", "Technologie", "Tirage",
                    "Nettoyage", "Entretient", "Jardin", "Meuble", "Divers");
            categorieComboBox.getSelectionModel().selectFirst();

            typeProduitComboBox.getItems().addAll("Tous", "consommable", "durable");
            typeProduitComboBox.getSelectionModel().selectFirst();

            // Charger les données initiales
            handleRechercherCommandes(null);
            handleRechercherInventaire(null);

            System.out.println("Initialisation du contrôleur de statistiques terminée");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'initialisation: " + e.getMessage());
        }
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
    private void handleExportCommandesPDF(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));
        fileChooser.setInitialFileName("Statistiques_Commandes.pdf");
        File file = fileChooser.showSaveDialog(commandesPieChart.getScene().getWindow());

        if (file != null) {
            try (PDDocument document = new PDDocument()) {
                PDPage page = new PDPage();
                document.addPage(page);

                try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                    // Titre
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(50, 750);
                    contentStream.showText("Statistiques des Commandes");
                    contentStream.endText();

                    // Période
                    contentStream.setFont(PDType1Font.HELVETICA, 12);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(50, 720);
                    contentStream.showText("Période: " + dateDebutCommandes.getValue() + " au " + dateFinCommandes.getValue());
                    contentStream.newLineAtOffset(0, -20);
                    contentStream.showText("Type: " + typeCommandeComboBox.getValue());
                    contentStream.endText();

                    // Statistiques générales
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(50, 670);
                    contentStream.showText("Statistiques Générales");
                    contentStream.endText();

                    contentStream.setFont(PDType1Font.HELVETICA, 12);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(50, 650);
                    contentStream.showText("Total Commandes: " + totalCommandesLabel.getText());
                    contentStream.newLineAtOffset(0, -20);
                    contentStream.showText("Commandes Livrées: " + commandesLivreesLabel.getText());
                    contentStream.newLineAtOffset(0, -20);
                    contentStream.showText("Commandes En Cours: " + commandesEnCoursLabel.getText());
                    contentStream.endText();

                    // Commandes par Statut
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(50, 590);
                    contentStream.showText("Commandes par Statut");
                    contentStream.endText();

                    float y = 570;
                    contentStream.setFont(PDType1Font.HELVETICA, 12);
                    for (PieChart.Data data : commandesPieChart.getData()) {
                        contentStream.beginText();
                        contentStream.newLineAtOffset(50, y);
                        contentStream.showText(data.getName() + ": " + (int)data.getPieValue());
                        contentStream.endText();
                        y -= 20;
                    }

                    // Commandes par Mois
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(50, y - 20);
                    contentStream.showText("Commandes par Mois");
                    contentStream.endText();

                    y -= 40;
                    contentStream.setFont(PDType1Font.HELVETICA, 12);
                    for (XYChart.Series<String, Number> series : commandesBarChart.getData()) {
                        for (XYChart.Data<String, Number> data : series.getData()) {
                            contentStream.beginText();
                            contentStream.newLineAtOffset(50, y);
                            contentStream.showText(data.getXValue() + ": " + data.getYValue());
                            contentStream.endText();
                            y -= 20;
                        }
                    }
                }

                document.save(file);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Statistiques des commandes exportées en PDF avec succès");
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'exportation en PDF: " + e.getMessage());
            }
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
            valeurStockLabel.setText(currencyFormat.format(valeurStock) + " TND");

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

    @FXML
    private void handleExportInventairePDF(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));
        fileChooser.setInitialFileName("Statistiques_Inventaire.pdf");
        File file = fileChooser.showSaveDialog(categoriePieChart.getScene().getWindow());

        if (file != null) {
            try (PDDocument document = new PDDocument()) {
                PDPage page = new PDPage();
                document.addPage(page);

                try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                    // Titre
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(50, 750);
                    contentStream.showText("Statistiques de l'Inventaire");
                    contentStream.endText();

                    // Filtres
                    contentStream.setFont(PDType1Font.HELVETICA, 12);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(50, 720);
                    contentStream.showText("Catégorie: " + categorieComboBox.getValue());
                    contentStream.newLineAtOffset(0, -20);
                    contentStream.showText("Type: " + typeProduitComboBox.getValue());
                    contentStream.endText();

                    // Statistiques générales
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(50, 670);
                    contentStream.showText("Statistiques Générales");
                    contentStream.endText();

                    contentStream.setFont(PDType1Font.HELVETICA, 12);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(50, 650);
                    contentStream.showText("Total Produits: " + totalProduitsLabel.getText());
                    contentStream.newLineAtOffset(0, -20);
                    contentStream.showText("Produits en Alerte: " + produitsEnAlerteLabel.getText());
                    contentStream.newLineAtOffset(0, -20);
                    contentStream.showText("Valeur du Stock: " + valeurStockLabel.getText());
                    contentStream.endText();

                    // Répartition par Catégorie
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(50, 590);
                    contentStream.showText("Répartition par Catégorie");
                    contentStream.endText();

                    float y = 570;
                    contentStream.setFont(PDType1Font.HELVETICA, 12);
                    for (PieChart.Data data : categoriePieChart.getData()) {
                        contentStream.beginText();
                        contentStream.newLineAtOffset(50, y);
                        contentStream.showText(data.getName() + ": " + (int)data.getPieValue());
                        contentStream.endText();
                        y -= 20;
                    }

                    // Produits par Niveau de Stock
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(50, y - 20);
                    contentStream.showText("Produits par Niveau de Stock");
                    contentStream.endText();

                    y -= 40;
                    contentStream.setFont(PDType1Font.HELVETICA, 12);
                    for (XYChart.Series<String, Number> series : stockBarChart.getData()) {
                        for (XYChart.Data<String, Number> data : series.getData()) {
                            contentStream.beginText();
                            contentStream.newLineAtOffset(50, y);
                            contentStream.showText(data.getXValue() + ": " + data.getYValue());
                            contentStream.endText();
                            y -= 20;
                        }
                    }
                }

                document.save(file);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Statistiques de l'inventaire exportées en PDF avec succès");
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'exportation en PDF: " + e.getMessage());
            }
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