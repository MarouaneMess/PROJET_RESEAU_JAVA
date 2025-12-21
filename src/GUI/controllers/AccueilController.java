package GUI.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.Region;
import GUI.MainApp;
import Modele.ReseauElectrique;
import java.io.File;
import java.util.Optional;

public class AccueilController {
    
    @FXML
    private Button btnConstruction;
    
    public void ouvrirConstruction() {
        try {
            String[] cheminsPossibles = {
                "src/GUI/fxml/construction.fxml",
                "GUI/fxml/construction.fxml"
            };
            String fxmlPath = null;
            for (String chemin : cheminsPossibles) {
                if (new File(chemin).exists()) {
                    fxmlPath = chemin;
                    break;
                }
            }
            
            if (fxmlPath == null) {
                afficherErreur("Erreur", "Fichier construction.fxml introuvable. Chemins cherchés:\n" + 
                    "- src/GUI/fxml/construction.fxml\n" + 
                    "- GUI/fxml/construction.fxml", "");
                return;
            }
            
            FXMLLoader loader = new FXMLLoader(new File(fxmlPath).toURI().toURL());
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("Construction du Réseau");
            stage.setScene(new Scene(root, 750, 750));
            stage.show();
            
        } catch (Exception e) {
             Alert alert = new Alert(Alert.AlertType.ERROR);
            // agrandir la fenêtre d'alerte si le message est long
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            alert.setTitle("Erreur");
            alert.setContentText("Impossible d'ouvrir la fenêtre de construction: " + e.getMessage());
            alert.showAndWait();
        }
    }
    
    public void chargerFichier() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Ouvrir un fichier réseau");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Fichiers texte", "*.txt"),
            new FileChooser.ExtensionFilter("Tous les fichiers", "*.*")
        );
        
        File file = fileChooser.showOpenDialog(new Stage());
        if (file != null) {
            // Stage stage = new Stage();
            TextInputDialog dialog = new TextInputDialog("10");
            dialog.setTitle("Pénalité");
            dialog.setHeaderText("Entrez la pénalité pour le calcul du coût :");
            dialog.setContentText("Pénalité (entier > 0) :");
            
            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()) {
                try {
                    int penalite = Integer.parseInt(result.get());
                    MainApp.reseau = new ReseauElectrique(penalite);
                    MainApp.reseau.chargerDepuisFichier(file.getAbsolutePath());
                    
                    if (!MainApp.reseau.verifierReseau()) {
                        afficherErreur("Erreur", "Le réseau chargé n'est pas valide.", "");
                        return;
                    }
                    
                            ouvrirVisualisation();
                    afficherInfo("Succès", "Réseau chargé avec succès !");
                    
                } catch (NumberFormatException e) {
                    afficherErreur("Erreur", "La pénalité doit être un entier.", "");
                } catch (Exception e) {
                    afficherErreur("Erreur", "Impossible de charger le fichier.", e.getMessage());
                }
            }
        }
    }
    
    private void ouvrirVisualisation() {
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
                afficherErreur("Erreur", "Fichier visualisation.fxml introuvable.", "");
                return;
            }
            
            FXMLLoader loader = new FXMLLoader(new File(fxmlPath).toURI().toURL());
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("Visualisation du Réseau");
            stage.setScene(new Scene(root, 900, 750));
            stage.show();
            
        } catch (Exception e) {
            afficherErreur("Erreur", "Impossible d'ouvrir la visualisation.", e.getMessage());
        }
    }
    
    private void afficherErreur(String titre, String header, String contenu) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText(header);
        alert.setContentText(contenu);
        alert.showAndWait();
    }
    
    private void afficherInfo(String titre, String contenu) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setContentText(contenu);
        alert.showAndWait();
    }
    
           public void ouvrirOptimisation() {
            try {
                String[] cheminsPossibles = {
                    "src/GUI/fxml/optimisation.fxml",
                    "GUI/fxml/optimisation.fxml"
                };
            
                String fxmlPath = null;
                for (String chemin : cheminsPossibles) {
                    if (new File(chemin).exists()) {
                        fxmlPath = chemin;
                        break;
                    }
                }
            
                if (fxmlPath == null) {
                        afficherErreur("Erreur", "Fichier optimisation.fxml introuvable.", "");
                    return;
                }
            
                FXMLLoader loader = new FXMLLoader(new File(fxmlPath).toURI().toURL());
                Parent root = loader.load();
            
                Stage stage = new Stage();
                    stage.setTitle("Optimisation du Réseau");
                    stage.setScene(new Scene(root, 850, 600));
                stage.show();
            
            } catch (Exception e) {
                    afficherErreur("Erreur", "Impossible d'ouvrir l'interface optimisation: " + e.getMessage(), "");
            }
        }
}
