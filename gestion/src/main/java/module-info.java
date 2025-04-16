module com.example.gestion {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    opens com.example.gestion.controller to javafx.fxml;


    opens com.example.gestion to javafx.fxml;
    exports com.example.gestion;
}