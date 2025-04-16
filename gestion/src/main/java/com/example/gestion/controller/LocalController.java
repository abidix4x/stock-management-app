package com.example.gestion.controller;

import com.example.gestion.dao.LocalDAO;
import com.example.gestion.model.Local;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class LocalController implements Initializable {
    @FXML
    private TableView<Local> localTable;

    @FXML
    private TableColumn<Local, String> codeColumn;

    @FXML
    private TableColumn<Local, String> designationColumn;

    @FXML
    private TableColumn<Local, String> typeColumn;

    @FXML
    private TextField codeField;

    @FXML
    private TextField designationField;

    @FXML
    private ComboBox<String> typeComboBox;

    @FXML
    private TextField searchField;

    private ObservableList<Local> localList = FXCollections.observableArrayList();
    private Local selectedLocal;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            System.out.println("Initialisation du contrôleur de locaux...");

            // Configurer les colonnes de la TableView
            codeColumn.setCellValueFactory(cellData-> new SimpleStringProperty(cellData.getValue().getCode()));
            designationColumn.setCellValueFactory(cellData-> new SimpleStringProperty(cellData.getValue().getDesignation()));
            typeColumn.setCellValueFactory(cellData->new SimpleStringProperty(cellData.getValue().getType() == null ? "" : cellData.getValue().getType().toString() ) );

            // Initialiser le ComboBox des types
            typeComboBox.getItems().addAll("Magasin", "Bureau", "Atelier", "Laboratoire", "Salle", "Autre");
            typeComboBox.getSelectionModel().selectFirst();

            // Sélectionner un local dans la table
            localTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    selectedLocal = newSelection;
                    codeField.setText(selectedLocal.getCode());
                    designationField.setText(selectedLocal.getDesignation());
                    typeComboBox.setValue(selectedLocal.getType());
                }
            });

            // Charger les locaux
            loadLocaux();

            // Configurer la recherche
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue.isEmpty()) {
                    loadLocaux();
                } else {
                    searchLocaux(newValue);
                }
            });

            System.out.println("Initialisation du contrôleur de locaux terminée");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'initialisation: " + e.getMessage());
        }
    }

    private void loadLocaux() {
        try {
            System.out.println("Chargement des locaux...");
            localList.clear();
            List<Local> locaux = LocalDAO.getAll();

            // Afficher les locaux récupérés pour le débogage
            System.out.println("Nombre de locaux récupérés: " + locaux.size());
            for (Local l : locaux) {
                System.out.println("Local: ID=" + l.getId() +
                        ", Code=" + l.getCode() +
                        ", Designation=" + l.getDesignation() +
                        ", Type=" + l.getType());
            }

            localList.addAll(locaux);
            localTable.setItems(localList);

            // Forcer le rafraîchissement de la table
            localTable.refresh();

            System.out.println("Locaux chargés: " + localList.size());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement des locaux: " + e.getMessage());
        }
    }

    private void searchLocaux(String keyword) {
        try {
            localList.clear();
            List<Local> locaux = LocalDAO.searchLocaux(keyword);
            localList.addAll(locaux);
            localTable.setItems(localList);
            localTable.refresh();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la recherche des locaux: " + e.getMessage());
        }
    }

    @FXML
    private void handleAjouter(ActionEvent event) {
        if (validateInputs()) {
            try {
                Local local = new Local();
                local.setCode(codeField.getText());
                local.setDesignation(designationField.getText());
                local.setType(typeComboBox.getValue());

                if (LocalDAO.insert(local)) {
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Local ajouté avec succès");
                    clearFields();
                    loadLocaux();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ajout du local");
                }
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ajout du local: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleModifier(ActionEvent event) {
        if (selectedLocal == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner un local à modifier");
            return;
        }

        if (validateInputs()) {
            try {
                selectedLocal.setCode(codeField.getText());
                selectedLocal.setDesignation(designationField.getText());
                selectedLocal.setType(typeComboBox.getValue());

                if (LocalDAO.update(selectedLocal)) {
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Local modifié avec succès");
                    clearFields();
                    loadLocaux();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la modification du local");
                }
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la modification du local: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleSupprimer(ActionEvent event) {
        if (selectedLocal == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner un local à supprimer");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("Êtes-vous sûr de vouloir supprimer ce local ?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                if (LocalDAO.delete(selectedLocal.getId())) {
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Local supprimé avec succès");
                    clearFields();
                    loadLocaux();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la suppression du local");
                }
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la suppression du local: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleNouveau(ActionEvent event) {
        clearFields();
        selectedLocal = null;
    }

    private boolean validateInputs() {
        if (codeField.getText().isEmpty() || designationField.getText().isEmpty() ||
                typeComboBox.getValue() == null) {

            showAlert(Alert.AlertType.WARNING, "Validation", "Veuillez remplir tous les champs obligatoires");
            return false;
        }
        return true;
    }

    private void clearFields() {
        codeField.clear();
        designationField.clear();
        typeComboBox.getSelectionModel().selectFirst();
        selectedLocal = null;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
