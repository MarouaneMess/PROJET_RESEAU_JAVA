package Menu;

import Modele.*;
import Algo.*;
import java.io.IOException;
import java.util.Scanner;

public class MenuAutomatique {
    public static void run(ReseauElectrique reseau, Scanner scanner) {
        while (true) {
            afficherMenuAutomatique();
            String choix = scanner.nextLine().trim();
            switch (choix) {
                case "1":
                    System.out.print("Nombre de tentatives d'optimisation (k): ");
                    try {
                        int k = Integer.parseInt(scanner.nextLine().trim());
                        if (k <= 0) {
                            System.out.println("Erreur: k doit être positif.");
                            break;
                        }
                        System.out.println("\n--- Résolution automatique en cours... ---");
                        double coutInitial = reseau.calculerCoutSilencieux();
                        Optimiseur.optimiserReseau(reseau, k);
                        double coutFinal = reseau.calculerCoutSilencieux();
                        double amelioration = coutInitial - coutFinal;
                        System.out.println("\n=== RÉSULTAT ===");
                        System.out.println("Coût initial: " + String.format("%.9f", coutInitial));
                        System.out.println("Coût final: " + String.format("%.9f", coutFinal));
                        System.out.println("Amélioration: " + amelioration + " (" + String.format("%.2f", amelioration / coutInitial * 100) + "%)");
                    } catch (NumberFormatException e) {
                        System.out.println("Erreur: k doit être un entier valide.");
                    } catch (IllegalArgumentException e) {
                        System.out.println("Erreur: " + e.getMessage());
                    }
                    break;
                case "2":
                    System.out.print("Nom du fichier de sauvegarde: ");
                    String nomFichier = scanner.nextLine().trim();
                    try {
                        Sauvegarde.sauvegarderSolution(reseau, nomFichier);
                        System.out.println("Solution sauvegardée avec succès dans " + nomFichier);
                    } catch (IOException e) {
                        System.out.println("Erreur lors de la sauvegarde: " + e.getMessage());
                    } catch (IllegalArgumentException e) {
                        System.out.println("Erreur: " + e.getMessage());
                    }
                    break;
                case "3":
                    System.out.println("Programme terminé.");
                    return;
                default:
                    System.out.println("Choix invalide.");
            }
        }
    }

    private static void afficherMenuAutomatique() {
        System.out.println("\n--- MENU AUTOMATIQUE ---");
        System.out.println("1. Résolution automatique");
        System.out.println("2. Sauvegarder la solution actuelle");
        System.out.println("3. Fin");
        System.out.print("Votre choix: ");
    }
}
