package com.example.gestion.controller;

import com.example.gestion.dao.CommandeInterneDAO;
import com.example.gestion.dao.ProduitDAO;
import com.example.gestion.dao.ServiceDAO;
import com.example.gestion.model.CommandeInterne;
import com.example.gestion.model.LigneCommandeInterne;
import com.example.gestion.model.Produit;
import com.example.gestion.model.Service;
import javafx.beans.property.ReadOnlyObjectWrapper;
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
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class CommandeInterneController implements Initializable {
    @FXML
    private TableView<CommandeInterne> commandeTable;

    @FXML
    private TableColumn<CommandeInterne, String> referenceColumn;

    @FXML
    private TableColumn<CommandeInterne, LocalDate> dateCommandeColumn;

    @FXML
    private TableColumn<CommandeInterne, String> serviceColumn;

    @FXML
    private TableColumn<CommandeInterne, String> statutColumn;

    @FXML
    private TableView<LigneCommandeInterne> ligneCommandeTable;

    @FXML
    private TableColumn<LigneCommandeInterne, String> produitColumn;

    @FXML
    private TableColumn<LigneCommandeInterne, Integer> quantiteColumn;

    @FXML
    private TableColumn<LigneCommandeInterne, String> stockDisponibleColumn;

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> statutFilterComboBox;

    @FXML
    private ComboBox<Service> serviceFilterComboBox;

    @FXML
    private TextField referenceField;

    @FXML
    private DatePicker dateCommandePicker;

    @FXML
    private ComboBox<Service> serviceComboBox;

    @FXML
    private ComboBox<String> statutComboBox;

    @FXML
    private ComboBox<Produit> produitComboBox;

    @FXML
    private TextField quantiteField;

    private ObservableList<CommandeInterne> commandeList = FXCollections.observableArrayList();
    private ObservableList<LigneCommandeInterne> ligneCommandeList = FXCollections.observableArrayList();
    private CommandeInterne selectedCommande;
    private LigneCommandeInterne selectedLigneCommande;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            System.out.println("Initialisation du contrôleur de commandes internes...");

            // Configurer les colonnes de la TableView des commandes
            referenceColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getReference()));
            dateCommandeColumn.setCellValueFactory(cellData ->
                    new ReadOnlyObjectWrapper<>(cellData.getValue().getDateCommande()));
            serviceColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getService().getNom()));
            statutColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatut()));

            // Configurer les colonnes de la TableView des lignes de commande
            produitColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getProduit().getDesignation()));
            quantiteColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getQuantite()));
            stockDisponibleColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(String.valueOf(cellData.getValue().getProduit().getQuantite())));

            // Initialiser les ComboBox de filtres
            statutFilterComboBox.getItems().addAll("Tous", "En cours", "Validée", "Livrée", "Annulée");
            statutFilterComboBox.getSelectionModel().selectFirst();

            serviceFilterComboBox.getItems().add(null); // Option pour "Tous les services"
            List<Service> services = ServiceDAO.getAll();
            serviceFilterComboBox.getItems().addAll(services);

            serviceFilterComboBox.setConverter(new StringConverter<Service>() {
                @Override
                public String toString(Service service) {
                    return service == null ? "Tous les services" : service.getNom();
                }

                @Override
                public Service fromString(String string) {
                    return null; // Non utilisé
                }
            });

            // Initialiser les ComboBox pour l'ajout/modification
            statutComboBox.getItems().addAll("En cours", "Validée", "Livrée", "Annulée");
            statutComboBox.getSelectionModel().selectFirst();

            serviceComboBox.getItems().addAll(services);
            serviceComboBox.setConverter(new StringConverter<Service>() {
                @Override
                public String toString(Service service) {
                    return service == null ? "" : service.getNom();
                }

                @Override
                public Service fromString(String string) {
                    return null; // Non utilisé
                }
            });

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

            // Sélectionner une commande dans la table
            commandeTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    selectedCommande = newSelection;
                    referenceField.setText(selectedCommande.getReference());
                    dateCommandePicker.setValue(selectedCommande.getDateCommande());
                    serviceComboBox.setValue(selectedCommande.getService());
                    statutComboBox.setValue(selectedCommande.getStatut());

                    // Charger les lignes de commande
                    loadLignesCommande(selectedCommande);
                }
            });

            // Sélectionner une ligne de commande dans la table
            ligneCommandeTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    selectedLigneCommande = newSelection;
                    produitComboBox.setValue(selectedLigneCommande.getProduit());
                    quantiteField.setText(String.valueOf(selectedLigneCommande.getQuantite()));
                }
            });

            // Initialiser la date à aujourd'hui
            dateCommandePicker.setValue(LocalDate.now());

            // Charger les commandes
            loadCommandes();

            System.out.println("Initialisation du contrôleur de commandes internes terminée");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'initialisation: " + e.getMessage());
        }
    }

    private void loadCommandes() {
        try {
            System.out.println("Chargement des commandes internes...");
            commandeList.clear();

            String reference = searchField.getText();
            String statut = statutFilterComboBox.getValue().equals("Tous") ? null : statutFilterComboBox.getValue();
            Service service = serviceFilterComboBox.getValue();

            List<CommandeInterne> commandes = CommandeInterneDAO.searchCommandes(reference, statut, service != null ? service.getId() : null);

            commandeList.addAll(commandes);
            commandeTable.setItems(commandeList);

            // Forcer le rafraîchissement de la table
            commandeTable.refresh();

            System.out.println("Commandes internes chargées: " + commandeList.size());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement des commandes: " + e.getMessage());
        }
    }

    private void loadLignesCommande(CommandeInterne commande) {
        try {
            System.out.println("Chargement des lignes de commande...");
            ligneCommandeList.clear();

            if (commande != null && commande.getId() > 0) {
                List<LigneCommandeInterne> lignes = CommandeInterneDAO.getLignesCommande(commande.getId());
                commande.setLignes(lignes);
                ligneCommandeList.addAll(lignes);
            }

            ligneCommandeTable.setItems(ligneCommandeList);

            // Forcer le rafraîchissement de la table
            ligneCommandeTable.refresh();

            System.out.println("Lignes de commande chargées: " + ligneCommandeList.size());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement des lignes de commande: " + e.getMessage());
        }
    }

    @FXML
    private void handleRechercher(ActionEvent event) {
        loadCommandes();
    }

    @FXML
    private void handleNouveau(ActionEvent event) {
        clearFields();
        selectedCommande = new CommandeInterne();
        selectedCommande.setDateCommande(LocalDate.now());
        selectedCommande.setStatut("En cours");

        dateCommandePicker.setValue(LocalDate.now());
        statutComboBox.setValue("En cours");

        ligneCommandeList.clear();
        ligneCommandeTable.setItems(ligneCommandeList);
    }

    @FXML
    private void handleEnregistrer(ActionEvent event) {
        if (validateCommandeInputs()) {
            try {
                if (selectedCommande == null) {
                    selectedCommande = new CommandeInterne();
                }

                selectedCommande.setReference(referenceField.getText());
                selectedCommande.setDateCommande(dateCommandePicker.getValue());
                selectedCommande.setService(serviceComboBox.getValue());
                selectedCommande.setStatut(statutComboBox.getValue());

                boolean success;
                if (selectedCommande.getId() > 0) {
                    success = CommandeInterneDAO.update(selectedCommande);
                } else {
                    success = CommandeInterneDAO.insert(selectedCommande);
                }

                if (success) {
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Commande enregistrée avec succès");
                    loadCommandes();

                    // Sélectionner la commande dans la table
                    for (CommandeInterne c : commandeList) {
                        if (c.getReference().equals(selectedCommande.getReference())) {
                            commandeTable.getSelectionModel().select(c);
                            break;
                        }
                    }
                } else {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'enregistrement de la commande");
                }
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'enregistrement de la commande: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleSupprimer(ActionEvent event) {
        if (selectedCommande == null || selectedCommande.getId() <= 0) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner une commande à supprimer");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("Êtes-vous sûr de vouloir supprimer cette commande ?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                if (CommandeInterneDAO.delete(selectedCommande.getId())) {
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Commande supprimée avec succès");
                    clearFields();
                    loadCommandes();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la suppression de la commande");
                }
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la suppression de la commande: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleAjouterLigne(ActionEvent event) {
        if (selectedCommande == null || selectedCommande.getId() <= 0) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez d'abord enregistrer la commande");
            return;
        }

        if (validateLigneInputs()) {
            try {
                LigneCommandeInterne ligne = new LigneCommandeInterne();
                ligne.setCommande(selectedCommande);
                ligne.setProduit(produitComboBox.getValue());
                ligne.setQuantite(Integer.parseInt(quantiteField.getText()));

                if (CommandeInterneDAO.insertLigneCommande(ligne)) {
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Ligne de commande ajoutée avec succès");
                    clearLigneFields();
                    loadLignesCommande(selectedCommande);
                } else {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ajout de la ligne de commande");
                }
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ajout de la ligne de commande: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleSupprimerLigne(ActionEvent event) {
        if (selectedLigneCommande == null || selectedLigneCommande.getId() <= 0) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner une ligne de commande à supprimer");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("Êtes-vous sûr de vouloir supprimer cette ligne de commande ?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                if (CommandeInterneDAO.deleteLigneCommande(selectedLigneCommande.getId())) {
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Ligne de commande supprimée avec succès");
                    clearLigneFields();
                    loadLignesCommande(selectedCommande);
                } else {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la suppression de la ligne de commande");
                }
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la suppression de la ligne de commande: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleValiderCommande(ActionEvent event) {
        if (selectedCommande == null || selectedCommande.getId() <= 0) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner une commande à valider");
            return;
        }

        if (selectedCommande.getLignes().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Attention", "La commande ne contient aucune ligne");
            return;
        }

        if (!"En cours".equals(selectedCommande.getStatut())) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Seules les commandes en cours peuvent être validées");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("Êtes-vous sûr de vouloir valider cette commande ?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                selectedCommande.setStatut("Validée");

                if (CommandeInterneDAO.update(selectedCommande)) {
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Commande validée avec succès");
                    statutComboBox.setValue("Validée");
                    loadCommandes();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la validation de la commande");
                }
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la validation de la commande: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleLivrer(ActionEvent event) {
        if (selectedCommande == null || selectedCommande.getId() <= 0) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner une commande à livrer");
            return;
        }

        if (!"Validée".equals(selectedCommande.getStatut())) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Seules les commandes validées peuvent être livrées");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("Êtes-vous sûr de vouloir livrer cette commande ? Cette action mettra à jour le stock.");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                if (CommandeInterneDAO.livrerCommande(selectedCommande)) {
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Commande livrée avec succès");
                    selectedCommande.setStatut("Livrée");
                    statutComboBox.setValue("Livrée");
                    loadCommandes();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la livraison de la commande");
                }
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la livraison de la commande: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleExportPDF(ActionEvent event) {
        if (selectedCommande == null || selectedCommande.getId() <= 0) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner une commande à exporter");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));
        fileChooser.setInitialFileName("CommandeInterne_" + selectedCommande.getReference() + ".pdf");
        File file = fileChooser.showSaveDialog(commandeTable.getScene().getWindow());

        if (file != null) {
            try (PDDocument document = new PDDocument()) {
                PDPage page = new PDPage();
                document.addPage(page);

                try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(50, 750);
                    contentStream.showText("Commande Interne");
                    contentStream.endText();

                    contentStream.setFont(PDType1Font.HELVETICA, 12);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(50, 720);
                    contentStream.showText("Référence: " + selectedCommande.getReference());
                    contentStream.newLineAtOffset(0, -20);
                    contentStream.showText("Date: " + selectedCommande.getDateCommande().toString());
                    contentStream.newLineAtOffset(0, -20);
                    contentStream.showText("Service: " + selectedCommande.getService().getNom());
                    contentStream.newLineAtOffset(0, -20);
                    contentStream.showText("Statut: " + selectedCommande.getStatut());
                    contentStream.endText();

                    // Tableau des lignes de commande
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(50, 620);
                    contentStream.showText("Produit");
                    contentStream.newLineAtOffset(200, 0);
                    contentStream.showText("Quantité");
                    contentStream.newLineAtOffset(100, 0);
                    contentStream.showText("Stock Disponible");
                    contentStream.endText();

                    contentStream.setFont(PDType1Font.HELVETICA, 12);
                    float y = 600;
                    for (LigneCommandeInterne ligne : selectedCommande.getLignes()) {
                        contentStream.beginText();
                        contentStream.newLineAtOffset(50, y);
                        contentStream.showText(ligne.getProduit().getDesignation());
                        contentStream.newLineAtOffset(200, 0);
                        contentStream.showText(String.valueOf(ligne.getQuantite()));
                        contentStream.newLineAtOffset(100, 0);
                        contentStream.showText(String.valueOf(ligne.getProduit().getQuantite()));
                        contentStream.endText();
                        y -= 20;
                    }
                }

                document.save(file);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Commande exportée en PDF avec succès");
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'exportation en PDF: " + e.getMessage());
            }
        }
    }

    private boolean validateCommandeInputs() {
        if (referenceField.getText().isEmpty() || dateCommandePicker.getValue() == null ||
                serviceComboBox.getValue() == null || statutComboBox.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Veuillez remplir tous les champs obligatoires");
            return false;
        }
        return true;
    }

    private boolean validateLigneInputs() {
        if (produitComboBox.getValue() == null || quantiteField.getText().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Veuillez remplir tous les champs obligatoires");
            return false;
        }

        try {
            int quantite = Integer.parseInt(quantiteField.getText());
            if (quantite <= 0) {
                showAlert(Alert.AlertType.WARNING, "Validation", "La quantité doit être supérieure à 0");
                return false;
            }

            // Vérifier si le stock est suffisant
            Produit produit = produitComboBox.getValue();
            if (quantite > produit.getQuantite()) {
                showAlert(Alert.AlertType.WARNING, "Validation", "Stock insuffisant. Disponible: " + produit.getQuantite());
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Validation", "La quantité doit être un nombre");
            return false;
        }

        return true;
    }

    private void clearFields() {
        referenceField.clear();
        dateCommandePicker.setValue(LocalDate.now());
        serviceComboBox.setValue(null);
        statutComboBox.setValue("En cours");
        clearLigneFields();
        selectedCommande = null;
    }

    private void clearLigneFields() {
        produitComboBox.setValue(null);
        quantiteField.clear();
        selectedLigneCommande = null;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}