package Menu;

import Modele.*;
import java.util.Scanner;

public class MenuOperation {
    public static void run(ReseauElectrique reseau, Scanner scanner) {
        while (true) {
            afficherMenuOperation();
            String choix = scanner.nextLine().trim();
            switch (choix) {
                case "1":
                    reseau.calculerCout();
                    break;
                case "2":
                    System.out.print("Connexion à modifier (ex: M1 G1): ");
                    String[] ancienne = scanner.nextLine().split("\\s+");
                    System.out.print("Nouvelle connexion (ex: M1 G2): ");
                    String[] nouvelle = scanner.nextLine().split("\\s+");
                    if (ancienne.length == 2 && nouvelle.length == 2) {
                        try {
                            reseau.modifierConnexion(ancienne[0], ancienne[1], nouvelle[0], nouvelle[1]);
                        } catch (IllegalArgumentException e) {
                            System.out.println("Erreur: " + e.getMessage());
                        }
                    } else {
                        System.out.println("Erreur: Format invalide.");
                    }
                    break;
                case "3":
                    reseau.afficherReseau();
                    break;
                case "4":
                    System.out.println("Programme terminé.");
                    return;
                default:
                    System.out.println("Choix invalide.");
            }
        }
    }

    private static void afficherMenuOperation() {
        System.out.println("\n--- MENU OPÉRATION ---");
        System.out.println("1. Calculer le coût du réseau");
        System.out.println("2. Modifier une connexion");
        System.out.println("3. Afficher le réseau");
        System.out.println("4. Fin");
        System.out.print("Votre choix: ");
    }
}
