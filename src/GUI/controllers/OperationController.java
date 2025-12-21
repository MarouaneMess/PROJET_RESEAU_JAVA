package GUI.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import GUI.MainApp;
import Modele.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class OperationController {
    
    @FXML private Label labelCout;
    @FXML private ComboBox<String> ancienneMaison;
    @FXML private ComboBox<String> ancienGenerateur;
    @FXML private ComboBox<String> nouvelleMaison;
    @FXML private ComboBox<String> nouveauGenerateur;
    @FXML private TextArea affichageReseau;
    
    @FXML
    public void initialize() {
        chargerMaisonsEtGenerateurs();
    }
    
    private void chargerMaisonsEtGenerateurs() {
        // Charger les maisons
        for (Maison maison : MainApp.reseau.getMaisons()) {
            ancienneMaison.getItems().add(maison.getNom());
            nouvelleMaison.getItems().add(maison.getNom());
        }
        
        // Charger les générateurs
        for (Generateur gen : MainApp.reseau.getGenerateurs()) {
            ancienGenerateur.getItems().add(gen.getNom());
            nouveauGenerateur.getItems().add(gen.getNom());
        }
    }
    
    @FXML
    public void calculerCout() {
        try {
            double cout = MainApp.reseau.calculerCoutSilencieux();
            labelCout.setText("Coût: " + cout + " kW");
            labelCout.setStyle("-fx-font-size: 14px; -fx-text-fill: #27ae60; -fx-font-weight: bold;");
        } catch (Exception e) {
            afficherErreur("Erreur", "Impossible de calculer le coût: " + e.getMessage());
        }
    }
    
    @FXML
    public void modifierConnexion() {
        String ancMaison = ancienneMaison.getValue();
        String ancGen = ancienGenerateur.getValue();
        String nouvMaison = nouvelleMaison.getValue();
        String nouvGen = nouveauGenerateur.getValue();
        
        if (ancMaison == null || ancGen == null || nouvMaison == null || nouvGen == null) {
            afficherErreur("Erreur", "Veuillez remplir tous les champs.");
            return;
        }
        
        try {
            MainApp.reseau.modifierConnexion(ancMaison, ancGen, nouvMaison, nouvGen);
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setContentText("Connexion modifiée avec succès!");
            alert.showAndWait();
            
            // Réinitialiser les sélections
            ancienneMaison.setValue(null);
            ancienGenerateur.setValue(null);
            nouvelleMaison.setValue(null);
            nouveauGenerateur.setValue(null);
            
        } catch (IllegalArgumentException e) {
            afficherErreur("Erreur", e.getMessage());
        }
    }
    
    @FXML
    public void afficherReseau() {
        try {
            // Capturer la sortie de afficherReseau()
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos);
            PrintStream old = System.out;
            System.setOut(ps);
            
            MainApp.reseau.afficherReseau();
            
            System.out.flush();
            System.setOut(old);
            
            affichageReseau.setText(baos.toString());
            
        } catch (Exception e) {
            afficherErreur("Erreur", "Impossible d'afficher le réseau: " + e.getMessage());
        }
    }
    
    @FXML
    public void quitter() {
        try {
            Stage stage = (Stage) labelCout.getScene().getWindow();
            stage.close();
            System.exit(0);
        } catch (Exception e) {
            System.exit(0);
        }
    }
    
    private void afficherErreur(String titre, String contenu) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setContentText(contenu);
        alert.showAndWait();
    }
}
