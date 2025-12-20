package Algo;
import Modele.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;


public class Sauvegarde {
    private static final String DOSSIER_AMELIOREES = "instancesAmeliorees";

    public static void sauvegarderSolution(ReseauElectrique reseau, String nomFichier) throws IOException, IllegalArgumentException {
        if (reseau == null) {
            throw new IllegalArgumentException("Le réseau ne peut pas être null.");
        }
        if (nomFichier == null || nomFichier.trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom du fichier ne peut pas être vide.");
        }
        
        try {
            Files.createDirectories(Paths.get(DOSSIER_AMELIOREES));
        } catch (IOException e) {
            throw new IOException("Erreur création du dossier " + DOSSIER_AMELIOREES + ": " + e.getMessage(), e);
        }
        
        String cheminFichier = DOSSIER_AMELIOREES + "/" + nomFichier;
        try (PrintWriter writer = new PrintWriter(cheminFichier)) {
            for (Generateur gen : reseau.getGenerateurs()) {
                writer.println("generateur(" + gen.getNom() + "," + gen.getCapaciteMax() + ").");
            }
            for (Maison maison : reseau.getMaisons()) {
                writer.println("maison(" + maison.getNom() + "," + maison.getTypeConsommation().name() + ").");
            }
            for (Generateur gen : reseau.getGenerateurs()) {
                for (Maison maison : gen.getMaisonsConnectees()) {
                    writer.println("connexion(" + gen.getNom() + "," + maison.getNom() + ").");
                }
            }
        } catch (IOException e) {
            throw new IOException("Erreur d'écriture du fichier: " + e.getMessage(), e);
        }
    }
}
