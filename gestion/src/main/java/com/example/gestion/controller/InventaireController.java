package com.example.gestion.controller;

import com.example.gestion.dao.LocalDAO;
import com.example.gestion.dao.ProduitDAO;
import com.example.gestion.dao.StockageDAO;
import com.example.gestion.model.Local;
import com.example.gestion.model.Produit;
import com.example.gestion.model.Stockage;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class InventaireController implements Initializable {
    @FXML
    private TableView<Stockage> stockageTable;

    @FXML
    private TableColumn<Stockage, String> produitColumn;

    @FXML
    private TableColumn<Stockage, String> localColumn;

    @FXML
    private TableColumn<Stockage, Integer> quantiteColumn;

    @FXML
    private TableColumn<Stockage, String> stockMinimalColumn;

    @FXML
    private TableColumn<Stockage, String> statutColumn;

    @FXML
    private ComboBox<Local> localComboBox;

    @FXML
    private ComboBox<Produit> produitComboBox;

    @FXML
    private ComboBox<Local> localStockageComboBox;

    @FXML
    private TextField quantiteField;

    @FXML
    private TextField searchField;

    private ObservableList<Stockage> stockageList = FXCollections.observableArrayList();
    private Stockage selectedStockage;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            System.out.println("Initialisation du contrôleur d'inventaire...");

            // Configurer les colonnes de la TableView
            produitColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getProduit().getDesignation()));

            localColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getLocal().getDesignation()));

            quantiteColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getQuantite()).asObject());

            stockMinimalColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(String.valueOf(cellData.getValue().getProduit().getStockMinimal())));

            statutColumn.setCellValueFactory(cellData -> {
                Stockage stockage = cellData.getValue();
                int quantite = stockage.getQuantite();
                int stockMinimal = stockage.getProduit().getStockMinimal();

                if (quantite <= 0) {
                    return new SimpleStringProperty("Rupture de stock");
                } else if (quantite <= stockMinimal) {
                    return new SimpleStringProperty("Stock critique");
                } else {
                    return new SimpleStringProperty("Stock normal");
                }
            });

            // Charger les locaux pour le filtre
            List<Local> locaux = LocalDAO.getAll();
            localComboBox.getItems().add(null); // Option pour "Tous les locaux"
            localComboBox.getItems().addAll(locaux);

            // Configurer l'affichage des locaux dans le ComboBox
            localComboBox.setConverter(new StringConverter<Local>() {
                @Override
                public String toString(Local local) {
                    return local == null ? "Tous les locaux" : local.getDesignation();
                }

                @Override
                public Local fromString(String string) {
                    return null; // Non utilisé
                }
            });

            // Charger les produits et locaux pour l'ajout/modification
            List<Produit> produits = ProduitDAO.getAll();
            produitComboBox.getItems().addAll(produits);

            produitComboBox.setConverter(new StringConverter<Produit>() {
                @Override
                public String toString(Produit produit) {
                    return produit == null ? "" : produit.getDesignation();
                }

                @Override
                public Produit fromString(String string) {
                    return null; // Non utilisé
                }
            });

            localStockageComboBox.getItems().addAll(locaux);

            localStockageComboBox.setConverter(new StringConverter<Local>() {
                @Override
                public String toString(Local local) {
                    return local == null ? "" : local.getDesignation();
                }

                @Override
                public Local fromString(String string) {
                    return null; // Non utilisé
                }
            });

            // Sélectionner un stockage dans la table
            stockageTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    selectedStockage = newSelection;
                    produitComboBox.setValue(selectedStockage.getProduit());
                    localStockageComboBox.setValue(selectedStockage.getLocal());
                    quantiteField.setText(String.valueOf(selectedStockage.getQuantite()));
                }
            });

            // Filtrer par local
            localComboBox.setOnAction(event -> loadStockage());

            // Charger les données initiales
            loadStockage();

            System.out.println("Initialisation du contrôleur d'inventaire terminée");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'initialisation: " + e.getMessage());
        }
    }

    private void loadStockage() {
        try {
            System.out.println("Chargement de l'inventaire...");
            stockageList.clear();

            Local selectedLocal = localComboBox.getValue();
            String searchText = searchField.getText();

            List<Stockage> stockages;
            if (selectedLocal == null && (searchText == null || searchText.isEmpty())) {
                stockages = StockageDAO.getAll();
            } else if (selectedLocal != null && (searchText == null || searchText.isEmpty())) {
                stockages = StockageDAO.getByLocal(selectedLocal.getId());
            } else if (selectedLocal == null && searchText != null && !searchText.isEmpty()) {
                stockages = StockageDAO.searchByProduit(searchText);
            } else {
                stockages = StockageDAO.searchByLocalAndProduit(selectedLocal.getId(), searchText);
            }

            stockageList.addAll(stockages);
            stockageTable.setItems(stockageList);

            // Forcer le rafraîchissement de la table
            stockageTable.refresh();

            System.out.println("Inventaire chargé: " + stockageList.size() + " entrées");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement de l'inventaire: " + e.getMessage());
        }
    }

    @FXML
    private void handleRechercher(ActionEvent event) {
        loadStockage();
    }

    @FXML
    private void handleAjouter(ActionEvent event) {
        if (validateInputs()) {
            try {
                Stockage stockage = new Stockage();
                stockage.setProduit(produitComboBox.getValue());
                stockage.setLocal(localStockageComboBox.getValue());
                stockage.setQuantite(Integer.parseInt(quantiteField.getText()));

                if (StockageDAO.insert(stockage)) {
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Stockage ajouté avec succès");
                    clearFields();
                    loadStockage();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ajout du stockage");
                }
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ajout du stockage: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleModifier(ActionEvent event) {
        if (selectedStockage == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner un stockage à modifier");
            return;
        }

        if (validateInputs()) {
            try {
                selectedStockage.setProduit(produitComboBox.getValue());
                selectedStockage.setLocal(localStockageComboBox.getValue());
                selectedStockage.setQuantite(Integer.parseInt(quantiteField.getText()));

                if (StockageDAO.update(selectedStockage)) {
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Stockage modifié avec succès");
                    clearFields();
                    loadStockage();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la modification du stockage");
                }
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la modification du stockage: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleSupprimer(ActionEvent event) {
        if (selectedStockage == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner un stockage à supprimer");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("Êtes-vous sûr de vouloir supprimer ce stockage ?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                if (StockageDAO.delete(selectedStockage.getId())) {
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Stockage supprimé avec succès");
                    clearFields();
                    loadStockage();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la suppression du stockage");
                }
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la suppression du stockage: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleNouveau(ActionEvent event) {
        clearFields();
        selectedStockage = null;
    }

    @FXML
    private void handleExportPDF(ActionEvent event) {
        if (stockageList.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Aucun stockage à exporter");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));
        fileChooser.setInitialFileName("Inventaire_Produits.pdf");
        File file = fileChooser.showSaveDialog(stockageTable.getScene().getWindow());

        if (file != null) {
            try (PDDocument document = new PDDocument()) {
                PDPage page = new PDPage();
                document.addPage(page);
                float y = 700;
                int stockIndex = 0;
                boolean isFirstPage = true;

                while (stockIndex < stockageList.size()) {
                    try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                        // Title (only on the first page)
                        if (isFirstPage) {
                            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
                            contentStream.beginText();
                            contentStream.newLineAtOffset(50, 750);
                            contentStream.showText("Inventaire des Produits par Local");
                            contentStream.endText();
                            isFirstPage = false;
                        }

                        // Table Headers
                        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10);
                        contentStream.beginText();
                        contentStream.newLineAtOffset(50, 720);
                        contentStream.showText("Produit");
                        contentStream.newLineAtOffset(120, 0);
                        contentStream.showText("Local");
                        contentStream.newLineAtOffset(120, 0);
                        contentStream.showText("Quantité");
                        contentStream.newLineAtOffset(80, 0);
                        contentStream.showText("Stock Minimal");
                        contentStream.newLineAtOffset(80, 0);
                        contentStream.showText("Statut");
                        contentStream.endText();

                        // Table Data
                        contentStream.setFont(PDType1Font.HELVETICA, 10);
                        for (; stockIndex < stockageList.size(); stockIndex++) {
                            Stockage stockage = stockageList.get(stockIndex);
                            contentStream.beginText();
                            contentStream.newLineAtOffset(50, y);
                            contentStream.showText(stockage.getProduit().getDesignation());
                            contentStream.newLineAtOffset(120, 0);
                            contentStream.showText(stockage.getLocal().getDesignation());
                            contentStream.newLineAtOffset(120, 0);
                            contentStream.showText(String.valueOf(stockage.getQuantite()));
                            contentStream.newLineAtOffset(80, 0);
                            contentStream.showText(String.valueOf(stockage.getProduit().getStockMinimal()));
                            contentStream.newLineAtOffset(80, 0);
                            String statut;
                            int quantite = stockage.getQuantite();
                            int stockMinimal = stockage.getProduit().getStockMinimal();
                            if (quantite <= 0) {
                                statut = "Rupture de stock";
                            } else if (quantite <= stockMinimal) {
                                statut = "Stock critique";
                            } else {
                                statut = "Stock normal";
                            }
                            contentStream.showText(statut);
                            contentStream.endText();
                            y -= 20;

                            if (y < 50 && stockIndex + 1 < stockageList.size()) {
                                y = 700;
                                page = new PDPage();
                                document.addPage(page);
                                break;
                            }
                        }
                    }
                }

                document.save(file);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Inventaire exporté en PDF avec succès");
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'exportation en PDF: " + e.getMessage());
            }
        }
    }

    private boolean validateInputs() {
        if (produitComboBox.getValue() == null || localStockageComboBox.getValue() == null ||
                quantiteField.getText().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Veuillez remplir tous les champs obligatoires");
            return false;
        }

        try {
            Integer.parseInt(quantiteField.getText());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Validation", "La quantité doit être un nombre");
            return false;
        }

        return true;
    }

    private void clearFields() {
        produitComboBox.setValue(null);
        localStockageComboBox.setValue(null);
        quantiteField.clear();
        selectedStockage = null;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}