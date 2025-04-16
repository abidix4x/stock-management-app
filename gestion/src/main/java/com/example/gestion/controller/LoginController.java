package com.example.gestion.controller;

import com.example.gestion.GestionApplication;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.animation.FadeTransition;
import javafx.util.Duration;
import javafx.scene.Parent;

import java.io.IOException;

public class LoginController {
    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        // Pour ce mini-projet, on utilise un login simple
        if (username.equals("admin") && password.equals("admin")) {
            try {
                System.out.println("Authentification réussie, chargement de l'interface principale...");

                // Charger la vue principale avec le chemin complet
                String mainViewPath = "/com/example/gestion/main-view.fxml";
                System.out.println("Tentative de chargement de: " + mainViewPath);

                FXMLLoader loader = new FXMLLoader(getClass().getResource(mainViewPath));
                Parent root = loader.load();

                // Ajouter une transition
                FadeTransition fadeIn = new FadeTransition(Duration.millis(1000), root);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);

                // Obtenir la fenêtre actuelle
                Stage stage = (Stage) usernameField.getScene().getWindow();
                Scene scene = new Scene(root, 1200, 800);

                // Ajouter une icône à la fenêtre
                try {
                    stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/app_icon.png")));
                } catch (Exception e) {
                    System.err.println("Impossible de charger l'icône de l'application: " + e.getMessage());
                }

                stage.setTitle("Gestion de Stock ISIMM - Tableau de bord");
                stage.setScene(scene);
                stage.setMaximized(true);

                // Démarrer la transition
                fadeIn.play();

                System.out.println("Interface principale chargée avec succès");
            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Erreur", "Erreur lors du chargement de l'interface principale: " + e.getMessage());
            }
        } else {
            showAlert("Erreur d'authentification", "Nom d'utilisateur ou mot de passe incorrect.");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
