package GUI;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import Modele.ReseauElectrique;
import java.io.File;

public class MainApp extends Application {
    
    public static ReseauElectrique reseau;
    
    @Override
    public void start(Stage primaryStage) {
        try {
            // Initialiser le réseau par défaut
            reseau = new ReseauElectrique(10);
            
            // Charger la scène principale
            String fxmlPath = "src/GUI/fxml/accueil.fxml";
            if (!new File(fxmlPath).exists()) {
                fxmlPath = "GUI/fxml/accueil.fxml";
            }
            FXMLLoader loader = new FXMLLoader(new File(fxmlPath).toURI().toURL());
            Parent root = loader.load();
            
            Scene scene = new Scene(root, 600, 500);
            primaryStage.setTitle("Réseau Électrique - Optimisation");
            primaryStage.setScene(scene);
            primaryStage.show();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
