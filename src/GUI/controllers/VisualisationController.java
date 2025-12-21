package GUI.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import GUI.MainApp;
import Modele.*;
import Algo.Optimiseur;
import java.io.File;
import java.util.Optional;

public class VisualisationController {
    @FXML private BorderPane rootPane;
    
    @FXML private Label coutLabel;
    @FXML private Label dispersionnLabel;
    @FXML private Label surchargeLabel;
    @FXML private ListView<String> listeGenerateurs;
    @FXML private ListView<String> listeMaisons;
    @FXML private TextArea detailsOptimisation;
    
    @FXML
    public void initialize() {
        rafraichirDonnees();
    }
    
    public void rafraichirDonnees() {
        listeGenerateurs.getItems().clear();
        listeMaisons.getItems().clear();
        
        // Afficher les générateurs
        for (Generateur g : MainApp.reseau.getGenerateurs()) {
            double taux = g.calculerTauxUtilisation();
            String status = taux > 1.0 ? " ⚠️ SURCHARGE" : (taux > 0.8 ? " ⚡" : "");
            String item = String.format("%s: %d/%d kW (%.1f%%)%s", 
                g.getNom(), g.getChargeActuelle(), g.getCapaciteMax(), taux * 100, status);
            listeGenerateurs.getItems().add(item);
        }
        
        // Afficher les maisons
        for (Maison m : MainApp.reseau.getMaisons()) {
            String gen = m.getGenerateur() != null ? m.getGenerateur().getNom() : "Non connectée";
            String item = String.format("%s (%s, %d kW) → %s", 
                m.getNom(), m.getTypeConsommation().name(), m.getConsommation(), gen);
            listeMaisons.getItems().add(item);
        }
        
        // Afficher les métriques
        double cout = MainApp.reseau.calculerCoutSilencieux();
        double dispersion = MainApp.reseau.calculerDispersion();
        double surcharge = MainApp.reseau.calculerSurcharge();
        
        coutLabel.setText(String.format("Coût total : %.3f", cout));
        dispersionnLabel.setText(String.format("Dispersion : %.3f", dispersion));
        surchargeLabel.setText(String.format("Surcharge : %.3f", surcharge));
    }
    
    @FXML
    public void lancerOptimisation() {
        TextInputDialog dialog = new TextInputDialog("100000");
        dialog.setTitle("Lancer l'optimisation");
        dialog.setHeaderText("Veuillez entrer le nombre d'itérations k");
        dialog.setContentText("k :");
        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) {
            return;
        }
        try {
            int k = Integer.parseInt(result.get().trim());
            if (k <= 0) {
                showError("Le nombre d'itérations doit être positif.");
                return;
            }

            double coutInitial = MainApp.reseau.calculerCoutSilencieux();
            detailsOptimisation.clear();
            detailsOptimisation.appendText("▶ Lancement optimisation k=" + k + "\n");
            detailsOptimisation.appendText(String.format("Coût initial : %.3f\n", coutInitial));
            detailsOptimisation.appendText("─────────────────────────\n");

            Optimiseur.optimiserReseau(MainApp.reseau, k);

            double coutFinal = MainApp.reseau.calculerCoutSilencieux();
            double diff = coutInitial - coutFinal;
            double pct = (coutInitial != 0) ? (diff / coutInitial) * 100 : 0;

            detailsOptimisation.appendText("✓ Optimisation terminée\n");
            detailsOptimisation.appendText(String.format("Coût final : %.3f\n", coutFinal));
            detailsOptimisation.appendText(String.format("Amélioration : %.3f (-%.1f%%)\n", diff, pct));

            rafraichirDonnees();
            showInfo("Optimisation terminée", String.format("Amélioration : %.1f%%", pct));

        } catch (NumberFormatException e) {
            showError("Le nombre d'itérations doit être un entier.");
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        } catch (Exception e) {
            showError("Erreur pendant l'optimisation: " + e.getMessage());
        }
    }

    @FXML
    public void sauvegarderReseau() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sauvegarder le réseau optimisé");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Fichiers texte", "*.txt"),
            new FileChooser.ExtensionFilter("Tous les fichiers", "*.*")
        );

        File file = fileChooser.showSaveDialog(new Stage());
        if (file != null) {
            try {
                java.nio.file.Path cheminFichier = file.toPath();
                java.util.List<String> lignes = new java.util.ArrayList<>();

                for (Generateur gen : MainApp.reseau.getGenerateurs()) {
                    lignes.add("generateur(" + gen.getNom() + "," + gen.getCapaciteMax() + ").");
                }
                for (Maison maison : MainApp.reseau.getMaisons()) {
                    lignes.add("maison(" + maison.getNom() + "," + maison.getTypeConsommation().name() + ").");
                }
                for (Generateur gen : MainApp.reseau.getGenerateurs()) {
                    for (Maison maison : gen.getMaisonsConnectees()) {
                        lignes.add("connexion(" + gen.getNom() + "," + maison.getNom() + ").");
                    }
                }

                java.nio.file.Files.write(cheminFichier, lignes);
                showInfo("Succès", "Réseau sauvegardé dans :\n" + file.getAbsolutePath());
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
                alert.setTitle("Erreur");
                alert.setContentText("Impossible de sauvegarder : " + e.getMessage());
                alert.showAndWait();
            }
        }
    }

    @FXML
    public void fermer() {
        Stage stage = (Stage) rootPane.getScene().getWindow();
        stage.close();
        System.exit(0);
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
