package Algo;
import Model.*;
import java.io.IOException;
import java.io.PrintWriter;


public class Sauvegarde {
    public static boolean sauvegarderSolution(ReseauElectrique reseau, String nomFichier) {
        try (PrintWriter writer = new PrintWriter(nomFichier)) {
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
            return true;
        } catch (IOException e) {
            System.out.println("Erreur d'écriture: " + e.getMessage());
            return false;
        }
    }
}
