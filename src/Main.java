import java.util.Scanner;

public class Main {

    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        ReseauElectrique reseau = new ReseauElectrique(10);

        // Phase de construction
        System.out.println("=== PHASE DE CONSTRUCTION ===");
        phaseConstruction(reseau);

        // Phase d'opération
        System.out.println("\n=== PHASE D'OPÉRATION ===");
        phaseOperation(reseau);

        scanner.close();
    }

    private static void phaseConstruction(ReseauElectrique reseau) {
        while (true) {
            afficherMenuConstruction();
            String choix = scanner.nextLine().trim();

            switch (choix) {
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
                        }
                    } else {
                        System.out.println("Erreur: Format invalide.");
                    }
                    break;
                case "2":
                    System.out.print("Nom et type de maison (ex: M1 (NORMAL/ BASSE/ FORTE)): ");
                    String[] inputMaison = scanner.nextLine().split("\\s+");
                    if (inputMaison.length == 2) {
                        reseau.ajouterMaison(inputMaison[0], inputMaison[1]);
                    } else {
                        System.out.println("Erreur: Format invalide.");
                    }
                    break;
                case "3":
                    reseau.afficherReseau();
                    System.out.print("Connexion (ex: M1 G1): ");
                    String[] inputCon = scanner.nextLine().split("\\s+");
                    if (inputCon.length == 2) {
                        reseau.ajouterConnexion(inputCon[0], inputCon[1]);
                    } else {
                        System.out.println("Erreur: Format invalide.");
                    }
                    break;
                case "4":
                    reseau.afficherReseau();
                    System.out.print("Supprimer connexion (ex: M1 G1): ");
                    String[] inputSup = scanner.nextLine().split("\\s+");
                    if (inputSup.length == 2) {
                        reseau.supprimerConnexion(inputSup[0], inputSup[1]);
                    } else {
                        System.out.println("Erreur: Format invalide.");
                    }
                    break;
                case "5":
                    if (reseau.verifierReseau()) {
                        return; // passer à la phase d'opération
                    }
                    break;
                
                default:
                    System.out.println("Choix invalide.");
            }
        }
    }

    private static void phaseOperation(ReseauElectrique reseau) {
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
                        reseau.modifierConnexion(ancienne[0], ancienne[1], nouvelle[0], nouvelle[1]);
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

    private static void afficherMenuConstruction() {
        System.out.println("\n--- MENU CONSTRUCTION ---");
        System.out.println("1. Ajouter un générateur");
        System.out.println("2. Ajouter une maison");
        System.out.println("3. Ajouter une connexion");
        System.out.println("4. Supprimer une connexion");
        System.out.println("5. Fin");
        System.out.print("Votre choix: ");
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