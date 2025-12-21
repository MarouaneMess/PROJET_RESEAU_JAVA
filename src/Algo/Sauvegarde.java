package Algo;
import Modele.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


public class Sauvegarde {
    private static final String DOSSIER_AMELIOREES = "instancesAmeliorees";

    public static void sauvegarderSolution(ReseauElectrique reseau, String nomFichier) throws IOException, IllegalArgumentException {
        sauvegarderReseau(reseau, nomFichier);
    }

    public static void sauvegarderReseau(ReseauElectrique reseau, String nomFichier) throws IOException, IllegalArgumentException {
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
        
        Path cheminFichier = Paths.get(DOSSIER_AMELIOREES, nomFichier);
        List<String> lignes = new ArrayList<>();
        
        for (Generateur gen : reseau.getGenerateurs()) {
            lignes.add("generateur(" + gen.getNom() + "," + gen.getCapaciteMax() + ").");
        }
        for (Maison maison : reseau.getMaisons()) {
            lignes.add("maison(" + maison.getNom() + "," + maison.getTypeConsommation().name() + ").");
        }
        for (Generateur gen : reseau.getGenerateurs()) {
            for (Maison maison : gen.getMaisonsConnectees()) {
                lignes.add("connexion(" + gen.getNom() + "," + maison.getNom() + ").");
            }
        }
        
        try {
            Files.write(cheminFichier, lignes);
        } catch (IOException e) {
            throw new IOException("Erreur d'écriture du fichier: " + e.getMessage(), e);
        }
    }
}
