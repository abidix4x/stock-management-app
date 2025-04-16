package com.example.gestion;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.scene.image.Image;

import java.io.IOException;
import java.net.URL;

public class GestionApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        try {
            // Initialiser la base de données
            DatabaseService.initializeDatabase();

            // Charger la vue de login avec le chemin complet
            String loginViewPath = "/com/example/gestion/login-view.fxml";
            System.out.println("Tentative de chargement de: " + loginViewPath);

            // Vérifier si la ressource existe
            URL resource = getClass().getResource(loginViewPath);
            if (resource == null) {
                throw new IOException("Ressource non trouvée: " + loginViewPath);
            }
            System.out.println("Ressource trouvée à: " + resource);

            FXMLLoader fxmlLoader = new FXMLLoader(resource);
            Scene scene = new Scene(fxmlLoader.load(), 800, 600);

            // Ajouter une icône à la fenêtre
            try {
                stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/app_icon.png")));
            } catch (Exception e) {
                System.err.println("Impossible de charger l'icône de l'application: " + e.getMessage());
            }

            stage.setTitle("Gestion de Stock ISIMM");
            stage.setScene(scene);
            stage.show();

            System.out.println("Application démarrée avec succès");
        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Erreur", "Erreur lors du chargement de l'interface: " + e.getMessage());
        }
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        try {
            // Vérifier explicitement que le driver SQLite est disponible
            Class.forName("org.sqlite.JDBC");
            System.out.println("SQLite JDBC driver chargé avec succès");

            // Lancer l'application
            launch();
        } catch (ClassNotFoundException e) {
            System.err.println("Erreur: Le driver SQLite JDBC est manquant. Assurez-vous qu'il est ajouté au classpath.");
            e.printStackTrace();

            // Afficher une alerte même si l'application n'est pas encore lancée
            Platform.startup(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur de driver");
                alert.setHeaderText(null);
                alert.setContentText("Le driver SQLite JDBC est manquant. Assurez-vous qu'il est ajouté au classpath.");
                alert.showAndWait();
                Platform.exit();
            });
        }
    }
}
