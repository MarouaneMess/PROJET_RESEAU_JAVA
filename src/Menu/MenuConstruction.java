package Menu;

import Modele.*;
import java.io.IOException;
import java.util.Scanner;

public class MenuConstruction {
    public static void run(ReseauElectrique reseau, Scanner scanner) {
        while (true) {
            afficherMenuConstruction();
            String choix = scanner.nextLine().trim();
            switch (choix) {
                case "0":
                    System.out.print("Chemin du fichier réseau: ");
                    String chemin = scanner.nextLine().trim();
                    try {
                        reseau.chargerDepuisFichier(chemin);
                        reseau.afficherReseau();
                    } catch (IOException | IllegalArgumentException e) {
                        System.out.println("Erreur lors du chargement: " + e.getMessage());
                    }
                    break;
                case "1":
                    System.out.print("Nom et capacité du générateur (ex: G1 60): ");
                    String[] inputGen = scanner.nextLine().split("\\s+");
                    if (inputGen.length == 2) {
                        try {
                            String nom = inputGen[0];
                            int capacite = Integer.parseInt(inputGen[1]);
                            if (capacite <= 0) {
                                System.out.println("Erreur: La capacité doit être un entier positif.");
                                break;
                            }
                            reseau.ajouterGenerateur(nom, capacite);
                        } catch (NumberFormatException e) {
                            System.out.println("Erreur: Capacité invalide.");
                        } catch (IllegalArgumentException e) {
                            System.out.println("Erreur: " + e.getMessage());
                        }
                    } else {
                        System.out.println("Erreur: Format invalide.");
                    }
                    break;
                case "2":
                    System.out.print("Nom et type de maison (ex: M1 (NORMAL/ BASSE/ FORTE)): ");
                    String[] inputMaison = scanner.nextLine().split("\\s+");
                    if (inputMaison.length == 2) {
                        try {
                            reseau.ajouterMaison(inputMaison[0], inputMaison[1]);
                        } catch (IllegalArgumentException e) {
                            System.out.println("Erreur: " + e.getMessage());
                        }
                    } else {
                        System.out.println("Erreur: Format invalide.");
                    }
                    break;
                case "3":
                    reseau.afficherReseau();
                    System.out.print("Connexion (ex: M1 G1): ");
                    String[] inputCon = scanner.nextLine().split("\\s+");
                    if (inputCon.length == 2) {
                        try {
                            reseau.ajouterConnexion(inputCon[0], inputCon[1]);
                        } catch (IllegalArgumentException e) {
                            System.out.println("Erreur: " + e.getMessage());
                        }
                    } else {
                        System.out.println("Erreur: Format invalide.");
                    }
                    break;
                case "4":
                    reseau.afficherReseau();
                    System.out.print("Supprimer connexion (ex: M1 G1): ");
                    String[] inputSup = scanner.nextLine().split("\\s+");
                    if (inputSup.length == 2) {
                        try {
                            reseau.supprimerConnexion(inputSup[0], inputSup[1]);
                        } catch (IllegalArgumentException e) {
                            System.out.println("Erreur: " + e.getMessage());
                        }
                    } else {
                        System.out.println("Erreur: Format invalide.");
                    }
                    break;
                case "5":
                    if (reseau.verifierReseau()) {
                        return;
                    }
                    break;
                default:
                    System.out.println("Choix invalide.");
            }
        }
    }

    private static void afficherMenuConstruction() {
        System.out.println("\n--- MENU CONSTRUCTION ---");
        System.out.println("0. Charger un réseau depuis un fichier");
        System.out.println("1. Ajouter un générateur");
        System.out.println("2. Ajouter une maison");
        System.out.println("3. Ajouter une connexion");
        System.out.println("4. Supprimer une connexion");
        System.out.println("5. Fin");
        System.out.print("Votre choix: ");
    }
}
