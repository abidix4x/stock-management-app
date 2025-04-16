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
