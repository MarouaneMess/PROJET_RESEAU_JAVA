package GUI.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import GUI.MainApp;
import Modele.*;
import java.io.File;

public class ConstructionController {
    
    @FXML private TextField nomGenerateur;
    @FXML private TextField capaciteGenerateur;
    @FXML private ListView<String> listeGenerateurs;
    
    @FXML private TextField nomMaison;
    @FXML private ComboBox<TypeConsommation> typeMaison;
    @FXML private ListView<String> listeMaisons;
    
    @FXML private ComboBox<String> maison1;
    @FXML private ComboBox<String> generateur1;
    @FXML private ListView<String> listeConnexions;
    
    @FXML private ComboBox<String> maisonSuppr;
    @FXML private ComboBox<String> generateurSuppr;
    
    @FXML
    public void initialize() {
        // Initialiser le ComboBox pour les types de consommation
        typeMaison.getItems().addAll(TypeConsommation.values());
        typeMaison.setValue(TypeConsommation.NORMAL);
        
        // Listener pour mettre à jour les ComboBox
        listeGenerateurs.setStyle("-fx-control-inner-background: #f5f5f5;");
        listeMaisons.setStyle("-fx-control-inner-background: #f5f5f5;");
    }
    
    @FXML
    public void ajouterGenerateur() {
        String nom = nomGenerateur.getText().trim();
        String capaciteStr = capaciteGenerateur.getText().trim();
        
        if (nom.isEmpty() || capaciteStr.isEmpty()) {
            afficherErreur("Erreur", "Veuillez remplir tous les champs.");
            return;
        }
        
        try {
            int capacite = Integer.parseInt(capaciteStr);
            MainApp.reseau.ajouterGenerateur(nom, capacite);
            
            listeGenerateurs.getItems().add(nom + " (Capacité: " + capacite + " kW)");
            generateur1.getItems().add(nom);
            
            nomGenerateur.clear();
            capaciteGenerateur.clear();
            
        } catch (NumberFormatException e) {
            afficherErreur("Erreur", "La capacité doit être un entier.");
        } catch (IllegalArgumentException e) {
            afficherErreur("Erreur", e.getMessage());
        }
    }
    
    @FXML
    public void ajouterMaison() {
        String nom = nomMaison.getText().trim();
        TypeConsommation type = typeMaison.getValue();
        
        if (nom.isEmpty() || type == null) {
            afficherErreur("Erreur", "Veuillez remplir tous les champs.");
            return;
        }
        
        try {
            MainApp.reseau.ajouterMaison(nom, type.name());
            
            listeMaisons.getItems().add(nom + " (" + type.name() + ", " + type.getValeur() + " kW)");
            maison1.getItems().add(nom);
            
            nomMaison.clear();
            
        } catch (IllegalArgumentException e) {
            afficherErreur("Erreur", e.getMessage());
        }
    }
    
    @FXML
    public void connecterMaison() {
        String maison = maison1.getValue();
        String generateur = generateur1.getValue();
        
        if (maison == null || generateur == null) {
            afficherErreur("Erreur", "Veuillez sélectionner une maison et un générateur.");
            return;
        }
        
        try {
            MainApp.reseau.ajouterConnexion(maison, generateur);
            listeConnexions.getItems().add(maison + " → " + generateur);
            
            // Mettre à jour les ComboBox de suppression
            if (!maisonSuppr.getItems().contains(maison)) {
                maisonSuppr.getItems().add(maison);
            }
            if (!generateurSuppr.getItems().contains(generateur)) {
                generateurSuppr.getItems().add(generateur);
            }
            
        } catch (IllegalArgumentException e) {
            afficherErreur("Erreur", e.getMessage());
        }
    }
    
    @FXML
    public void supprimerConnexion() {
        String maison = maisonSuppr.getValue();
        String generateur = generateurSuppr.getValue();
        
        if (maison == null || generateur == null) {
            afficherErreur("Erreur", "Veuillez sélectionner une maison et un générateur.");
            return;
        }
        
        try {
            MainApp.reseau.supprimerConnexion(maison, generateur);
            
            // Mettre à jour la liste des connexions
            listeConnexions.getItems().removeIf(connexion -> 
                connexion.equals(maison + " → " + generateur));
            
            maisonSuppr.setValue(null);
            generateurSuppr.setValue(null);
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setContentText("Connexion supprimée avec succès!");
            alert.showAndWait();
            
        } catch (IllegalArgumentException e) {
            afficherErreur("Erreur", e.getMessage());
        }
    }
    
    @FXML
    public void visualiserReseau() {
        if (MainApp.reseau.getMaisons().isEmpty() || MainApp.reseau.getGenerateurs().isEmpty()) {
            afficherErreur("Erreur", "Le réseau doit avoir au moins une maison et un générateur.");
            return;
        }
        
        if (!MainApp.reseau.verifierReseau()) {
            afficherErreur("Erreur", "Le réseau n'est pas valide. Toutes les maisons doivent être connectées.");
            return;
        }
        
        try {
            String[] cheminsPossibles = {
                "src/GUI/fxml/visualisation.fxml",
                "GUI/fxml/visualisation.fxml"
            };
            
            String fxmlPath = null;
            for (String chemin : cheminsPossibles) {
                if (new File(chemin).exists()) {
                    fxmlPath = chemin;
                    break;
                }
            }
            
            if (fxmlPath == null) {
                afficherErreur("Erreur", "Fichier visualisation.fxml introuvable.");
                return;
            }
            
            FXMLLoader loader = new FXMLLoader(new File(fxmlPath).toURI().toURL());
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("Visualisation du Réseau");
            stage.setScene(new Scene(root, 900, 750));
            stage.show();
            
        } catch (Exception e) {
            afficherErreur("Erreur", "Impossible d'ouvrir la visualisation: " + e.getMessage());
        }
    }

    @FXML
    public void ouvrirOperation() {
        try {
            String[] cheminsPossibles = {
                "src/GUI/fxml/operation.fxml",
                "GUI/fxml/operation.fxml"
            };
            
            String fxmlPath = null;
            for (String chemin : cheminsPossibles) {
                if (new File(chemin).exists()) {
                    fxmlPath = chemin;
                    break;
                }
            }
            
            if (fxmlPath == null) {
                afficherErreur("Erreur", "Fichier operation.fxml introuvable.");
                return;
            }
            
            FXMLLoader loader = new FXMLLoader(new File(fxmlPath).toURI().toURL());
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("Opération du Réseau");
            stage.setScene(new Scene(root, 900, 750));
            stage.setResizable(true);  // Permet d'agrandir
            stage.setMinWidth(800);
            stage.setMinHeight(600);
            stage.show();
            
        } catch (Exception e) {
            afficherErreur("Erreur", "Impossible d'ouvrir l'interface opération: " + e.getMessage());
        }
    }
    
    private void afficherErreur(String titre, String contenu) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setContentText(contenu);
        alert.showAndWait();
    }
}
