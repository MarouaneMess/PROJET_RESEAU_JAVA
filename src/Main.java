import Modele.*;
import Menu.*;
import java.util.Scanner;
import java.io.IOException;

public class Main {

    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        if (args.length >= 2) {
            String cheminFichier = args[0];
            int penalite;
            try {
                penalite = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.out.println("Erreur: La pénalité doit être un entier valide.");
                return;
            }

            ReseauElectrique reseau;
            try {
                reseau = new ReseauElectrique(penalite);
            } catch (IllegalArgumentException e) {
                System.out.println("Erreur: " + e.getMessage());
                return;
            }

            try {
                reseau.chargerDepuisFichier(cheminFichier);
            } catch (IOException | IllegalArgumentException e) {
                System.out.println("Échec du chargement. Programme arrêté.");
                return;
            }

            if (!reseau.verifierReseau()) {
                System.out.println("Le réseau chargé n'est pas valide. Programme arrêté.");
                return;
            }

            System.out.println("\n=== MODE AUTOMATIQUE ===");
            MenuAutomatique.run(reseau, scanner);
        } else {
            ReseauElectrique reseau;
            try {
                reseau = new ReseauElectrique(10);
            } catch (IllegalArgumentException e) {
                System.out.println("Erreur: " + e.getMessage());
                return;
            }
            System.out.println("=== PHASE DE CONSTRUCTION ===");
            MenuConstruction.run(reseau, scanner);

            System.out.println("\n=== PHASE D'OPÉRATION ===");
            MenuOperation.run(reseau, scanner);
        }

        scanner.close();
    }
}