package com.example.gestion.controller;

import com.example.gestion.dao.FournisseurDAO;
import com.example.gestion.model.Fournisseur;
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

public class FournisseurController implements Initializable {
    @FXML
    private TableView<Fournisseur> fournisseurTable;

    @FXML
    private TableColumn<Fournisseur, String> nomColumn;

    @FXML
    private TableColumn<Fournisseur, String> adresseColumn;

    @FXML
    private TableColumn<Fournisseur, String> telephoneColumn;

    @FXML
    private TableColumn<Fournisseur, String> emailColumn;

    @FXML
    private TextField nomField;

    @FXML
    private TextField adresseField;

    @FXML
    private TextField telephoneField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField searchField;

    private ObservableList<Fournisseur> fournisseurList = FXCollections.observableArrayList();
    private Fournisseur selectedFournisseur;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            System.out.println("Initialisation du contrôleur de fournisseurs...");

            // Configurer les colonnes de la TableView
            nomColumn.setCellValueFactory(cellData->new SimpleStringProperty(cellData.getValue().getNom()));
            adresseColumn.setCellValueFactory(cellData->new SimpleStringProperty(cellData.getValue().getAdresse()));
            telephoneColumn.setCellValueFactory(cellData->new SimpleStringProperty(cellData.getValue().getTelephone()));
            emailColumn.setCellValueFactory(cellData->new SimpleStringProperty(cellData.getValue().getEmail()));

            // Sélectionner un fournisseur dans la table
            fournisseurTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    selectedFournisseur = newSelection;
                    nomField.setText(selectedFournisseur.getNom());
                    adresseField.setText(selectedFournisseur.getAdresse());
                    telephoneField.setText(selectedFournisseur.getTelephone());
                    emailField.setText(selectedFournisseur.getEmail());
                }
            });

            // Charger les fournisseurs
            loadFournisseurs();

            // Configurer la recherche
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue.isEmpty()) {
                    loadFournisseurs();
                } else {
                    searchFournisseurs(newValue);
                }
            });

            System.out.println("Initialisation du contrôleur de fournisseurs terminée");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'initialisation: " + e.getMessage());
        }
    }

    private void loadFournisseurs() {
        try {
            System.out.println("Chargement des fournisseurs...");
            fournisseurList.clear();
            List<Fournisseur> fournisseurs = FournisseurDAO.getAll();

            // Afficher les fournisseurs récupérés pour le débogage
            System.out.println("Nombre de fournisseurs récupérés: " + fournisseurs.size());
            for (Fournisseur f : fournisseurs) {
                System.out.println("Fournisseur: ID=" + f.getId() +
                        ", Nom=" + f.getNom() +
                        ", Adresse=" + f.getAdresse());
            }

            fournisseurList.addAll(fournisseurs);
            fournisseurTable.setItems(fournisseurList);

            // Forcer le rafraîchissement de la table
            fournisseurTable.refresh();

            System.out.println("Fournisseurs chargés: " + fournisseurList.size());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement des fournisseurs: " + e.getMessage());
        }
    }

    private void searchFournisseurs(String keyword) {
        try {
            fournisseurList.clear();
            List<Fournisseur> fournisseurs = FournisseurDAO.searchFournisseurs(keyword);
            fournisseurList.addAll(fournisseurs);
            fournisseurTable.setItems(fournisseurList);
            fournisseurTable.refresh();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la recherche des fournisseurs: " + e.getMessage());
        }
    }

    @FXML
    private void handleAjouter(ActionEvent event) {
        if (validateInputs()) {
            try {
                Fournisseur fournisseur = new Fournisseur();
                fournisseur.setNom(nomField.getText());
                fournisseur.setAdresse(adresseField.getText());
                fournisseur.setTelephone(telephoneField.getText());
                fournisseur.setEmail(emailField.getText());

                if (FournisseurDAO.insert(fournisseur)) {
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Fournisseur ajouté avec succès");
                    clearFields();
                    loadFournisseurs();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ajout du fournisseur");
                }
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ajout du fournisseur: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleModifier(ActionEvent event) {
        if (selectedFournisseur == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner un fournisseur à modifier");
            return;
        }

        if (validateInputs()) {
            try {
                selectedFournisseur.setNom(nomField.getText());
                selectedFournisseur.setAdresse(adresseField.getText());
                selectedFournisseur.setTelephone(telephoneField.getText());
                selectedFournisseur.setEmail(emailField.getText());

                if (FournisseurDAO.update(selectedFournisseur)) {
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Fournisseur modifié avec succès");
                    clearFields();
                    loadFournisseurs();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la modification du fournisseur");
                }
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la modification du fournisseur: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleSupprimer(ActionEvent event) {
        if (selectedFournisseur == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner un fournisseur à supprimer");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("Êtes-vous sûr de vouloir supprimer ce fournisseur ?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                if (FournisseurDAO.delete(selectedFournisseur.getId())) {
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Fournisseur supprimé avec succès");
                    clearFields();
                    loadFournisseurs();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la suppression du fournisseur");
                }
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la suppression du fournisseur: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleNouveau(ActionEvent event) {
        clearFields();
        selectedFournisseur = null;
    }

    private boolean validateInputs() {
        if (nomField.getText().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Le nom du fournisseur est obligatoire");
            return false;
        }
        return true;
    }

    private void clearFields() {
        nomField.clear();
        adresseField.clear();
        telephoneField.clear();
        emailField.clear();
        selectedFournisseur = null;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
