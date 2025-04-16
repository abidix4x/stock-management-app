package com.example.gestion.controller;

import com.example.gestion.dao.ServiceDAO;
import com.example.gestion.model.Service;
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

public class ServiceController implements Initializable {
    @FXML
    private TableView<Service> serviceTable;

    @FXML
    private TableColumn<Service, String> nomColumn;

    @FXML
    private TableColumn<Service, String> responsableColumn;

    @FXML
    private TableColumn<Service, String> telephoneColumn;

    @FXML
    private TableColumn<Service, String> emailColumn;

    @FXML
    private TextField nomField;

    @FXML
    private TextField responsableField;

    @FXML
    private TextField telephoneField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField searchField;

    private ObservableList<Service> serviceList = FXCollections.observableArrayList();
    private Service selectedService;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            System.out.println("Initialisation du contrôleur de services...");

            // Configurer les colonnes de la TableView
            nomColumn.setCellValueFactory(cellData->new SimpleStringProperty(cellData.getValue().getNom()));
            responsableColumn.setCellValueFactory(cellData->new SimpleStringProperty(cellData.getValue().getResponsable()));
            telephoneColumn.setCellValueFactory(cellData->new SimpleStringProperty(cellData.getValue().getTelephone()));
            emailColumn.setCellValueFactory(cellData->new SimpleStringProperty(cellData.getValue().getEmail()));

            // Sélectionner un service dans la table
            serviceTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    selectedService = newSelection;
                    nomField.setText(selectedService.getNom());
                    responsableField.setText(selectedService.getResponsable());
                    telephoneField.setText(selectedService.getTelephone());
                    emailField.setText(selectedService.getEmail());
                }
            });

            // Charger les services
            loadServices();

            // Configurer la recherche
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue.isEmpty()) {
                    loadServices();
                } else {
                    searchServices(newValue);
                }
            });

            System.out.println("Initialisation du contrôleur de services terminée");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'initialisation: " + e.getMessage());
        }
    }

    private void loadServices() {
        try {
            System.out.println("Chargement des services...");
            serviceList.clear();
            List<Service> services = ServiceDAO.getAll();

            // Afficher les services récupérés pour le débogage
            System.out.println("Nombre de services récupérés: " + services.size());
            for (Service s : services) {
                System.out.println("Service: ID=" + s.getId() +
                        ", Nom=" + s.getNom() +
                        ", Responsable=" + s.getResponsable());
            }

            serviceList.addAll(services);
            serviceTable.setItems(serviceList);

            // Forcer le rafraîchissement de la table
            serviceTable.refresh();

            System.out.println("Services chargés: " + serviceList.size());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement des services: " + e.getMessage());
        }
    }

    private void searchServices(String keyword) {
        try {
            serviceList.clear();
            List<Service> services = ServiceDAO.searchServices(keyword);
            serviceList.addAll(services);
            serviceTable.setItems(serviceList);
            serviceTable.refresh();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la recherche des services: " + e.getMessage());
        }
    }

    @FXML
    private void handleAjouter(ActionEvent event) {
        if (validateInputs()) {
            try {
                Service service = new Service();
                service.setNom(nomField.getText());
                service.setResponsable(responsableField.getText());
                service.setTelephone(telephoneField.getText());
                service.setEmail(emailField.getText());

                if (ServiceDAO.insert(service)) {
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Service ajouté avec succès");
                    clearFields();
                    loadServices();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ajout du service");
                }
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ajout du service: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleModifier(ActionEvent event) {
        if (selectedService == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner un service à modifier");
            return;
        }

        if (validateInputs()) {
            try {
                selectedService.setNom(nomField.getText());
                selectedService.setResponsable(responsableField.getText());
                selectedService.setTelephone(telephoneField.getText());
                selectedService.setEmail(emailField.getText());

                if (ServiceDAO.update(selectedService)) {
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Service modifié avec succès");
                    clearFields();
                    loadServices();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la modification du service");
                }
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la modification du service: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleSupprimer(ActionEvent event) {
        if (selectedService == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner un service à supprimer");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("Êtes-vous sûr de vouloir supprimer ce service ?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                if (ServiceDAO.delete(selectedService.getId())) {
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Service supprimé avec succès");
                    clearFields();
                    loadServices();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la suppression du service");
                }
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la suppression du service: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleNouveau(ActionEvent event) {
        clearFields();
        selectedService = null;
    }

    private boolean validateInputs() {
        if (nomField.getText().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Le nom du service est obligatoire");
            return false;
        }
        return true;
    }

    private void clearFields() {
        nomField.clear();
        responsableField.clear();
        telephoneField.clear();
        emailField.clear();
        selectedService = null;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
