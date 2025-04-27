package com.example.gestion.controller;

import com.example.gestion.dao.ProduitDAO;
import com.example.gestion.model.Produit;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class ProduitController implements Initializable {
    @FXML private TableView<Produit> produitTable;
    @FXML private TableColumn<Produit, String> referenceColumn;
    @FXML private TableColumn<Produit, String> designationColumn;
    @FXML private TableColumn<Produit, String> typeColumn;
    @FXML private TableColumn<Produit, String> categorieColumn;
    @FXML private TableColumn<Produit, String> quantiteColumn;
    @FXML private TableColumn<Produit, String> stockMinimalColumn;
    @FXML private TableColumn<Produit, String> datePeremptionColumn;

    @FXML private TextField referenceField;
    @FXML private TextField designationField;
    @FXML private ComboBox<String> typeComboBox;
    @FXML private ComboBox<String> categorieComboBox;
    @FXML private TextField quantiteField;
    @FXML private TextField stockMinimalField;
    @FXML private DatePicker datePeremptionPicker;
    @FXML private CheckBox critiqueCheckBox;
    @FXML private TextField searchField;

    private ObservableList<Produit> produitList = FXCollections.observableArrayList();
    private Produit selectedProduit;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            System.out.println("Initialisation du contrôleur de produits...");

            referenceColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getReference()));
            designationColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDesignation()));
            typeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getType()));
            categorieColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCategorie()));
            quantiteColumn.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getQuantite())));
            stockMinimalColumn.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getStockMinimal())));
            datePeremptionColumn.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getDatePeremption())));

            typeComboBox.getItems().addAll("consommable", "durable");
            typeComboBox.getSelectionModel().selectFirst();

            ObservableList<String> categoriesConsommables = FXCollections.observableArrayList(
                    "Bureautique", "Informatique", "Technologie", "Tirage", "Nettoyage", "Entretient", "Jardin", "Divers"
            );

            ObservableList<String> categoriesDurables = FXCollections.observableArrayList(
                    "Meuble", "Informatique", "Technologie", "Divers"
            );

            categorieComboBox.setItems(categoriesConsommables);
            categorieComboBox.getSelectionModel().selectFirst();

            typeComboBox.setOnAction(event -> {
                String selectedType = typeComboBox.getValue();
                if (selectedType != null) {
                    if (selectedType.equals("consommable")) {
                        categorieComboBox.setItems(categoriesConsommables);
                    } else {
                        categorieComboBox.setItems(categoriesDurables);
                    }
                    categorieComboBox.getSelectionModel().selectFirst();
                }
            });

            produitTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    selectedProduit = newSelection;
                    referenceField.setText(selectedProduit.getReference());
                    designationField.setText(selectedProduit.getDesignation());
                    typeComboBox.setValue(selectedProduit.getType());
                    categorieComboBox.setValue(selectedProduit.getCategorie());
                    quantiteField.setText(String.valueOf(selectedProduit.getQuantite()));
                    stockMinimalField.setText(String.valueOf(selectedProduit.getStockMinimal()));
                    datePeremptionPicker.setValue(selectedProduit.getDatePeremption());
                    critiqueCheckBox.setSelected(selectedProduit.isCritique());
                }
            });

            loadProduits();

            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue.isEmpty()) {
                    loadProduits();
                } else {
                    searchProduits(newValue);
                }
            });

            System.out.println("Initialisation du contrôleur de produits terminée");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'initialisation: " + e.getMessage());
        }
    }

    private void loadProduits() {
        try {
            produitList.clear();
            List<Produit> produits = ProduitDAO.getAll();
            produitList.addAll(produits);
            produitTable.setItems(produitList);
            produitTable.refresh();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement des produits: " + e.getMessage());
        }
    }

    private void searchProduits(String keyword) {
        try {
            produitList.clear();
            List<Produit> produits = ProduitDAO.searchProduits(keyword);
            produitList.addAll(produits);
            produitTable.setItems(produitList);
            produitTable.refresh();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la recherche des produits: " + e.getMessage());
        }
    }

    @FXML
    private void handleAjouter(ActionEvent event) {
        if (validateInputs()) {
            try {
                Produit produit = new Produit();
                produit.setReference(referenceField.getText());
                produit.setDesignation(designationField.getText());
                produit.setType(typeComboBox.getValue());
                produit.setCategorie(categorieComboBox.getValue());
                produit.setQuantite(Integer.parseInt(quantiteField.getText()));
                produit.setStockMinimal(Integer.parseInt(stockMinimalField.getText()));
                produit.setDatePeremption(datePeremptionPicker.getValue());
                produit.setCritique(critiqueCheckBox.isSelected());

                if (ProduitDAO.insert(produit)) {
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Produit ajouté avec succès");
                    clearFields();
                    loadProduits();
                    produitTable.refresh();
                    for (Produit p : produitList) {
                        if (p.getReference().equals(produit.getReference())) {
                            produitTable.getSelectionModel().select(p);
                            break;
                        }
                    }
                } else {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ajout du produit");
                }
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ajout du produit: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleModifier(ActionEvent event) {
        if (selectedProduit == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner un produit à modifier");
            return;
        }

        if (validateInputs()) {
            try {
                selectedProduit.setReference(referenceField.getText());
                selectedProduit.setDesignation(designationField.getText());
                selectedProduit.setType(typeComboBox.getValue());
                selectedProduit.setCategorie(categorieComboBox.getValue());
                selectedProduit.setQuantite(Integer.parseInt(quantiteField.getText()));
                selectedProduit.setStockMinimal(Integer.parseInt(stockMinimalField.getText()));
                selectedProduit.setDatePeremption(datePeremptionPicker.getValue());
                selectedProduit.setCritique(critiqueCheckBox.isSelected());

                if (ProduitDAO.update(selectedProduit)) {
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Produit modifié avec succès");
                    clearFields();
                    loadProduits();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la modification du produit");
                }
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la modification du produit: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleSupprimer(ActionEvent event) {
        if (selectedProduit == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner un produit à supprimer");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("Êtes-vous sûr de vouloir supprimer ce produit ?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                if (ProduitDAO.delete(selectedProduit.getId())) {
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Produit supprimé avec succès");
                    clearFields();
                    loadProduits();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la suppression du produit");
                }
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la suppression du produit: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleNouveau(ActionEvent event) {
        clearFields();
        selectedProduit = null;
    }

    @FXML
    private void handleExportPDF(ActionEvent event) {
        if (produitList.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Aucun produit à exporter");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));
        fileChooser.setInitialFileName("Liste_Produits.pdf");
        File file = fileChooser.showSaveDialog(produitTable.getScene().getWindow());

        if (file != null) {
            try (PDDocument document = new PDDocument()) {
                PDPage page = new PDPage();
                document.addPage(page);
                float y = 700;
                int productIndex = 0;
                boolean isFirstPage = true;

                while (productIndex < produitList.size()) {
                    try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                        // Title (only on the first page)
                        if (isFirstPage) {
                            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
                            contentStream.beginText();
                            contentStream.newLineAtOffset(50, 750);
                            contentStream.showText("Liste des Produits");
                            contentStream.endText();
                            isFirstPage = false;
                        }

                        // Table Headers
                        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10);
                        contentStream.beginText();
                        contentStream.newLineAtOffset(50, 720);
                        contentStream.showText("Référence");
                        contentStream.newLineAtOffset(80, 0);
                        contentStream.showText("Désignation");
                        contentStream.newLineAtOffset(120, 0);
                        contentStream.showText("Type");
                        contentStream.newLineAtOffset(60, 0);
                        contentStream.showText("Catégorie");
                        contentStream.newLineAtOffset(60, 0);
                        contentStream.showText("Quantité");
                        contentStream.newLineAtOffset(50, 0);
                        contentStream.showText("Stock Min");
                        contentStream.newLineAtOffset(50, 0);
                        contentStream.showText("Date Péremption");
                        contentStream.newLineAtOffset(80, 0);
                        contentStream.showText("Critique");
                        contentStream.endText();

                        // Table Data
                        contentStream.setFont(PDType1Font.HELVETICA, 10);
                        for (; productIndex < produitList.size(); productIndex++) {
                            Produit produit = produitList.get(productIndex);
                            contentStream.beginText();
                            contentStream.newLineAtOffset(50, y);
                            contentStream.showText(produit.getReference());
                            contentStream.newLineAtOffset(80, 0);
                            contentStream.showText(produit.getDesignation());
                            contentStream.newLineAtOffset(120, 0);
                            contentStream.showText(produit.getType());
                            contentStream.newLineAtOffset(60, 0);
                            contentStream.showText(produit.getCategorie());
                            contentStream.newLineAtOffset(60, 0);
                            contentStream.showText(String.valueOf(produit.getQuantite()));
                            contentStream.newLineAtOffset(50, 0);
                            contentStream.showText(String.valueOf(produit.getStockMinimal()));
                            contentStream.newLineAtOffset(50, 0);
                            contentStream.showText(produit.getDatePeremption() != null ? produit.getDatePeremption().toString() : "N/A");
                            contentStream.newLineAtOffset(80, 0);
                            contentStream.showText(produit.isCritique() ? "Oui" : "Non");
                            contentStream.endText();
                            y -= 20;

                            if (y < 50 && productIndex + 1 < produitList.size()) {
                                y = 700;
                                page = new PDPage();
                                document.addPage(page);
                                break;
                            }
                        }
                    }
                }

                document.save(file);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Liste des produits exportée en PDF avec succès");
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'exportation en PDF: " + e.getMessage());
            }
        }
    }

    private boolean validateInputs() {
        if (referenceField.getText().isEmpty() || designationField.getText().isEmpty() ||
                typeComboBox.getValue() == null || categorieComboBox.getValue() == null ||
                quantiteField.getText().isEmpty() || stockMinimalField.getText().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Veuillez remplir tous les champs obligatoires");
            return false;
        }

        try {
            Integer.parseInt(quantiteField.getText());
            Integer.parseInt(stockMinimalField.getText());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Validation", "La quantité et le stock minimal doivent être des nombres");
            return false;
        }

        return true;
    }

    private void clearFields() {
        referenceField.clear();
        designationField.clear();
        typeComboBox.getSelectionModel().selectFirst();
        categorieComboBox.getSelectionModel().selectFirst();
        quantiteField.clear();
        stockMinimalField.clear();
        datePeremptionPicker.setValue(null);
        critiqueCheckBox.setSelected(false);
        selectedProduit = null;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}