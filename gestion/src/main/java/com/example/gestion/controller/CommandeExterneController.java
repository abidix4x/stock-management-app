
package com.example.gestion.controller;

import com.example.gestion.dao.CommandeExterneDAO;
import com.example.gestion.dao.FournisseurDAO;
import com.example.gestion.dao.ProduitDAO;
import com.example.gestion.model.CommandeExterne;
import com.example.gestion.model.Fournisseur;
import com.example.gestion.model.LigneCommandeExterne;
import com.example.gestion.model.Produit;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
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

import java.io.File;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

                public class CommandeExterneController implements Initializable {
                    @FXML
                    private TableView<CommandeExterne> commandeTable;

                    @FXML
                    private TableColumn<CommandeExterne, String> referenceColumn;

                    @FXML
                    private TableColumn<CommandeExterne, LocalDate> dateCommandeColumn;

                    @FXML
                    private TableColumn<CommandeExterne, String> fournisseurColumn;

                    @FXML
                    private TableColumn<CommandeExterne, String> statutColumn;

                    @FXML
                    private TableColumn<CommandeExterne, String> totalColumn;

                    @FXML
                    private TableView<LigneCommandeExterne> ligneCommandeTable;

                    @FXML
                    private TableColumn<LigneCommandeExterne, String> produitColumn;

                    @FXML
                    private TableColumn<LigneCommandeExterne, Integer> quantiteColumn;

                    @FXML
                    private TableColumn<LigneCommandeExterne, Double> prixUnitaireColumn;

                    @FXML
                    private TableColumn<LigneCommandeExterne, String> sousTotal;

                    @FXML
                    private TextField searchField;

                    @FXML
                    private ComboBox<String> statutFilterComboBox;

                    @FXML
                    private ComboBox<Fournisseur> fournisseurFilterComboBox;

                    @FXML
                    private TextField referenceField;

                    @FXML
                    private DatePicker dateCommandePicker;

                    @FXML
                    private ComboBox<Fournisseur> fournisseurComboBox;

                    @FXML
                    private ComboBox<String> statutComboBox;

                    @FXML
                    private ComboBox<Produit> produitComboBox;

                    @FXML
                    private TextField quantiteField;

                    @FXML
                    private TextField prixUnitaireField;

                    @FXML
                    private Label totalLabel;

                    private ObservableList<CommandeExterne> commandeList = FXCollections.observableArrayList();
                    private ObservableList<LigneCommandeExterne> ligneCommandeList = FXCollections.observableArrayList();
                    private CommandeExterne selectedCommande;
                    private LigneCommandeExterne selectedLigneCommande;
                    private NumberFormat currencyFormat;

                    @Override
                    public void initialize(URL url, ResourceBundle resourceBundle) {
                        try {
                            System.out.println("Initialisation du contrôleur de commandes externes...");

                            // Configure currency format for TND (Tunisian Dinar)
                            DecimalFormatSymbols symbols = new DecimalFormatSymbols();
                            symbols.setDecimalSeparator('.');
                            symbols.setGroupingSeparator(' ');
                            DecimalFormat decimalFormat = new DecimalFormat("#,##0.000", symbols);
                            decimalFormat.setGroupingUsed(true);
                            currencyFormat = decimalFormat;

                            // Configurer les colonnes de la TableView des commandes
                            referenceColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getReference()));
                            dateCommandeColumn.setCellValueFactory(cellData ->
                                    new ReadOnlyObjectWrapper<>(cellData.getValue().getDateCommande()));
                            fournisseurColumn.setCellValueFactory(cellData ->
                                    new SimpleStringProperty(cellData.getValue().getFournisseur().getNom()));
                            statutColumn.setCellValueFactory(cellData ->
                                    new SimpleStringProperty(cellData.getValue().getStatut()));
                            totalColumn.setCellValueFactory(cellData ->
                                    new SimpleStringProperty(currencyFormat.format(cellData.getValue().getTotal()) + " TND"));

                            // Configurer les colonnes de la TableView des lignes de commande
                            produitColumn.setCellValueFactory(cellData ->
                                    new SimpleStringProperty(cellData.getValue().getProduit().getDesignation()));
                            quantiteColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getQuantite()));
                            prixUnitaireColumn.setCellValueFactory(cellData ->
                                    new ReadOnlyObjectWrapper<>(cellData.getValue().getPrixUnitaire()));

                            sousTotal.setCellValueFactory(cellData -> {
                                double total = cellData.getValue().getQuantite() * cellData.getValue().getPrixUnitaire();
                                return new SimpleStringProperty(currencyFormat.format(total) + " TND");
                            });

                            // Initialiser les ComboBox de filtres
                            statutFilterComboBox.getItems().addAll("Tous", "En cours", "Validée", "Réceptionnée", "Annulée");
                            statutFilterComboBox.getSelectionModel().selectFirst();

                            fournisseurFilterComboBox.getItems().add(null); // Option pour "Tous les fournisseurs"
                            List<Fournisseur> fournisseurs = FournisseurDAO.getAll();
                            fournisseurFilterComboBox.getItems().addAll(fournisseurs);

                            fournisseurFilterComboBox.setConverter(new StringConverter<Fournisseur>() {
                                @Override
                                public String toString(Fournisseur fournisseur) {
                                    return fournisseur == null ? "Tous les fournisseurs" : fournisseur.getNom();
                                }

                                @Override
                                public Fournisseur fromString(String string) {
                                    return null; // Non utilisé
                                }
                            });

                            // Initialiser les ComboBox pour l'ajout/modification
                            statutComboBox.getItems().addAll("En cours", "Validée", "Réceptionnée", "Annulée");
                            statutComboBox.getSelectionModel().selectFirst();

                            fournisseurComboBox.getItems().addAll(fournisseurs);
                            fournisseurComboBox.setConverter(new StringConverter<Fournisseur>() {
                                @Override
                                public String toString(Fournisseur fournisseur) {
                                    return fournisseur == null ? "" : fournisseur.getNom();
                                }

                                @Override
                                public Fournisseur fromString(String string) {
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
                                    fournisseurComboBox.setValue(selectedCommande.getFournisseur());
                                    statutComboBox.setValue(selectedCommande.getStatut());

                                    // Charger les lignes de commande
                                    loadLignesCommande(selectedCommande);

                                    // Mettre à jour le total
                                    updateTotal();
                                }
                            });

                            // Sélectionner une ligne de commande dans la table
                            ligneCommandeTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                                if (newSelection != null) {
                                    selectedLigneCommande = newSelection;
                                    produitComboBox.setValue(selectedLigneCommande.getProduit());
                                    quantiteField.setText(String.valueOf(selectedLigneCommande.getQuantite()));
                                    prixUnitaireField.setText(String.valueOf(selectedLigneCommande.getPrixUnitaire()));
                                }
                            });

                            // Initialiser la date à aujourd'hui
                            dateCommandePicker.setValue(LocalDate.now());

                            // Charger les commandes
                            loadCommandes();

                            System.out.println("Initialisation du contrôleur de commandes externes terminée");
                        } catch (Exception e) {
                            e.printStackTrace();
                            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'initialisation: " + e.getMessage());
                        }
                    }

                    private void loadCommandes() {
                        try {
                            System.out.println("Chargement des commandes externes...");
                            commandeList.clear();

                            String reference = searchField.getText();
                            String statut = statutFilterComboBox.getValue().equals("Tous") ? null : statutFilterComboBox.getValue();
                            Fournisseur fournisseur = fournisseurFilterComboBox.getValue();

                            List<CommandeExterne> commandes = CommandeExterneDAO.searchCommandes(reference, statut, fournisseur != null ? fournisseur.getId() : null);

                            commandeList.addAll(commandes);
                            commandeTable.setItems(commandeList);

                            // Forcer le rafraîchissement de la table
                            commandeTable.refresh();

                            System.out.println("Commandes externes chargées: " + commandeList.size());
                        } catch (Exception e) {
                            e.printStackTrace();
                            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement des commandes: " + e.getMessage());
                        }
                    }

                    private void loadLignesCommande(CommandeExterne commande) {
                        try {
                            System.out.println("Chargement des lignes de commande...");
                            ligneCommandeList.clear();

                            if (commande != null && commande.getId() > 0) {
                                List<LigneCommandeExterne> lignes = CommandeExterneDAO.getLignesCommande(commande.getId());
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

                    private void updateTotal() {
                        if (selectedCommande != null) {
                            totalLabel.setText(currencyFormat.format(selectedCommande.getTotal()) + " TND");
                        } else {
                            totalLabel.setText(currencyFormat.format(0) + " TND");
                        }
                    }

                    @FXML
                    private void handleRechercher(ActionEvent event) {
                        loadCommandes();
                    }

                    @FXML
                    private void handleNouveau(ActionEvent event) {
                        clearFields();
                        selectedCommande = new CommandeExterne();
                        selectedCommande.setDateCommande(LocalDate.now());
                        selectedCommande.setStatut("En cours");

                        dateCommandePicker.setValue(LocalDate.now());
                        statutComboBox.setValue("En cours");

                        ligneCommandeList.clear();
                        ligneCommandeTable.setItems(ligneCommandeList);

                        updateTotal();
                    }

                    @FXML
                    private void handleEnregistrer(ActionEvent event) {
                        if (validateCommandeInputs()) {
                            try {
                                if (selectedCommande == null) {
                                    selectedCommande = new CommandeExterne();
                                }

                                selectedCommande.setReference(referenceField.getText());
                                selectedCommande.setDateCommande(dateCommandePicker.getValue());
                                selectedCommande.setFournisseur(fournisseurComboBox.getValue());
                                selectedCommande.setStatut(statutComboBox.getValue());

                                boolean success;
                                if (selectedCommande.getId() > 0) {
                                    success = CommandeExterneDAO.update(selectedCommande);
                                } else {
                                    success = CommandeExterneDAO.insert(selectedCommande);
                                }

                                if (success) {
                                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Commande enregistrée avec succès");
                                    loadCommandes();

                                    // Sélectionner la commande dans la table
                                    for (CommandeExterne c : commandeList) {
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
                                if (CommandeExterneDAO.delete(selectedCommande.getId())) {
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
                                LigneCommandeExterne ligne = new LigneCommandeExterne();
                                ligne.setCommande(selectedCommande);
                                ligne.setProduit(produitComboBox.getValue());
                                ligne.setQuantite(Integer.parseInt(quantiteField.getText()));
                                ligne.setPrixUnitaire(Double.parseDouble(prixUnitaireField.getText()));

                                if (CommandeExterneDAO.insertLigneCommande(ligne)) {
                                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Ligne de commande ajoutée avec succès");
                                    clearLigneFields();
                                    loadLignesCommande(selectedCommande);
                                    updateTotal();
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
                                if (CommandeExterneDAO.deleteLigneCommande(selectedLigneCommande.getId())) {
                                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Ligne de commande supprimée avec succès");
                                    clearLigneFields();
                                    loadLignesCommande(selectedCommande);
                                    updateTotal();
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

                                if (CommandeExterneDAO.update(selectedCommande)) {
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
                    private void handleReceptionner(ActionEvent event) {
                        if (selectedCommande == null || selectedCommande.getId() <= 0) {
                            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner une commande à réceptionner");
                            return;
                        }

                        if (!"Validée".equals(selectedCommande.getStatut())) {
                            showAlert(Alert.AlertType.WARNING, "Attention", "Seules les commandes validées peuvent être réceptionnées");
                            return;
                        }

                        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                        confirmAlert.setTitle("Confirmation");
                        confirmAlert.setHeaderText(null);
                        confirmAlert.setContentText("Êtes-vous sûr de vouloir réceptionner cette commande ? Cette action mettra à jour le stock.");

                        Optional<ButtonType> result = confirmAlert.showAndWait();
                        if (result.isPresent() && result.get() == ButtonType.OK) {
                            try {
                                if (CommandeExterneDAO.receptionnerCommande(selectedCommande)) {
                                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Commande réceptionnée avec succès");
                                    selectedCommande.setStatut("Réceptionnée");
                                    statutComboBox.setValue("Réceptionnée");
                                    loadCommandes();
                                } else {
                                    showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la réception de la commande");
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la réception de la commande: " + e.getMessage());
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
                        fileChooser.setInitialFileName("Commande_" + selectedCommande.getReference() + ".pdf");
                        File file = fileChooser.showSaveDialog(commandeTable.getScene().getWindow());

                        if (file != null) {
                            try (PDDocument document = new PDDocument()) {
                                PDPage page = new PDPage();
                                document.addPage(page);
                                float y = 600;
                                int lineIndex = 0;
                                boolean isFirstPage = true;

                                while (lineIndex < selectedCommande.getLignes().size()) {
                                    try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                                        // Title and Commande Details (only on the first page)
                                        if (isFirstPage) {
                                            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
                                            contentStream.beginText();
                                            contentStream.newLineAtOffset(50, 750);
                                            contentStream.showText("Commande Externe");
                                            contentStream.endText();

                                            contentStream.setFont(PDType1Font.HELVETICA, 12);
                                            contentStream.beginText();
                                            contentStream.newLineAtOffset(50, 720);
                                            contentStream.showText("Référence: " + selectedCommande.getReference());
                                            contentStream.newLineAtOffset(0, -20);
                                            contentStream.showText("Date: " + selectedCommande.getDateCommande().toString());
                                            contentStream.newLineAtOffset(0, -20);
                                            contentStream.showText("Fournisseur: " + selectedCommande.getFournisseur().getNom());
                                            contentStream.newLineAtOffset(0, -20);
                                            contentStream.showText("Statut: " + selectedCommande.getStatut());
                                            contentStream.endText();

                                            isFirstPage = false;
                                        }

                                        // Table Headers
                                        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                                        contentStream.beginText();
                                        contentStream.newLineAtOffset(50, 620);
                                        contentStream.showText("Produit");
                                        contentStream.newLineAtOffset(200, 0);
                                        contentStream.showText("Quantité");
                                        contentStream.newLineAtOffset(100, 0);
                                        contentStream.showText("Prix Unitaire");
                                        contentStream.newLineAtOffset(100, 0);
                                        contentStream.showText("Sous-total");
                                        contentStream.endText();

                                        // Table Data
                                        contentStream.setFont(PDType1Font.HELVETICA, 12);
                                        for (; lineIndex < selectedCommande.getLignes().size(); lineIndex++) {
                                            LigneCommandeExterne ligne = selectedCommande.getLignes().get(lineIndex);
                                            contentStream.beginText();
                                            contentStream.newLineAtOffset(50, y);
                                            contentStream.showText(ligne.getProduit().getDesignation());
                                            contentStream.newLineAtOffset(200, 0);
                                            contentStream.showText(String.valueOf(ligne.getQuantite()));
                                            contentStream.newLineAtOffset(100, 0);
                                            contentStream.showText(currencyFormat.format(ligne.getPrixUnitaire()) + " TND");
                                            contentStream.newLineAtOffset(100, 0);
                                            contentStream.showText(currencyFormat.format(ligne.getQuantite() * ligne.getPrixUnitaire()) + " TND");
                                            contentStream.endText();
                                            y -= 20;

                                            if (y < 50 && lineIndex + 1 < selectedCommande.getLignes().size()) {
                                                y = 600;
                                                page = new PDPage();
                                                document.addPage(page);
                                                break;
                                            }
                                        }

                                        // Total (on the last page)
                                        if (lineIndex == selectedCommande.getLignes().size()) {
                                            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                                            contentStream.beginText();
                                            contentStream.newLineAtOffset(50, y - 20);
                                            contentStream.showText("Total: " + currencyFormat.format(selectedCommande.getTotal()) + " TND");
                                            contentStream.endText();
                                        }
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
                                fournisseurComboBox.getValue() == null || statutComboBox.getValue() == null) {
                            showAlert(Alert.AlertType.WARNING, "Validation", "Veuillez remplir tous les champs obligatoires");
                            return false;
                        }
                        return true;
                    }

                    private boolean validateLigneInputs() {
                        if (produitComboBox.getValue() == null || quantiteField.getText().isEmpty() ||
                                prixUnitaireField.getText().isEmpty()) {
                            showAlert(Alert.AlertType.WARNING, "Validation", "Veuillez remplir tous les champs obligatoires");
                            return false;
                        }

                        try {
                            int quantite = Integer.parseInt(quantiteField.getText());
                            if (quantite <= 0) {
                                showAlert(Alert.AlertType.WARNING, "Validation", "La quantité doit être supérieure à 0");
                                return false;
                            }

                            double prixUnitaire = Double.parseDouble(prixUnitaireField.getText());
                            if (prixUnitaire <= 0) {
                                showAlert(Alert.AlertType.WARNING, "Validation", "Le prix unitaire doit être supérieur à 0");
                                return false;
                            }
                        } catch (NumberFormatException e) {
                            showAlert(Alert.AlertType.WARNING, "Validation", "La quantité et le prix unitaire doivent être des nombres");
                            return false;
                        }

                        return true;
                    }

                    private void clearFields() {
                        referenceField.clear();
                        dateCommandePicker.setValue(LocalDate.now());
                        fournisseurComboBox.setValue(null);
                        statutComboBox.setValue("En cours");
                        clearLigneFields();
                        selectedCommande = null;
                    }

                    private void clearLigneFields() {
                        produitComboBox.setValue(null);
                        quantiteField.clear();
                        prixUnitaireField.clear();
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