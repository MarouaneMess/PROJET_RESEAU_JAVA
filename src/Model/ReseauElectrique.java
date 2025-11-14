import java.util.HashMap;
import java.util.Map;

public class ReseauElectrique {

    //   Map des maisons du réseau, indexées par leur nom.
    //   Choix HashMap : Permet un accès en O(1) par nom de maison par contre O(n) pour une liste"pour les opeations rech, ...etc."
    private Map<String, Maison> maisons;
    private Map<String, Generateur> generateurs; // pareil pour les générateurs
    private int penalite; // val par défaut = 10
 
    public ReseauElectrique(int penalite) {
        this.maisons = new HashMap<>();// HashMap garantit des performances O(1) pour get/put/containsKey
        this.generateurs = new HashMap<>();
        this.penalite = penalite;
    }
    public ReseauElectrique() {
        // constructeur par défaut avec pénalité 10, on fait appel au constructeur principal    
        this(10); 
    }

    public void ajouterGenerateur(String nom, int capaciteMax) {
        String nomUpper = nom.toUpperCase();
        if (generateurs.containsKey(nomUpper)) {
            // Mise à jour de la capacité d'un générateur existant
            System.out.println("Mise à jour: Le générateur " + nomUpper + " existe déjà. Capacité mise à jour.");
            generateurs.get(nomUpper).setCapaciteMax(capaciteMax);
        } else {
            // Création d'un nouveau générateur (nom stocké en majuscules)
            generateurs.put(nomUpper, new Generateur(nomUpper, capaciteMax));
            System.out.println("Générateur " + nomUpper + " ajouté avec succès.");
        }
    }

    public void ajouterMaison(String nom, String typeConsommationStr) {
        try {
            // conbersion du string en enum
            TypeConsommation type = TypeConsommation.valueOf(typeConsommationStr.toUpperCase());
            String nomUpper = nom.toUpperCase();
            if (maisons.containsKey(nomUpper)) {
                System.out.println("Mise à jour: La maison " + nomUpper + " existe déjà. Consommation mise à jour.");
            } else {
                System.out.println("Maison " + nomUpper + " ajoutée avec succès.");
            }

            // put() écrase l'ancienne valeur si la clé existe
            maisons.put(nomUpper, new Maison(nomUpper, type));
            
        } catch (IllegalArgumentException e) {
            // si le type de consommation est invalide
            System.out.println("Erreur: Type de consommation invalide. Utilisez BASSE, NORMAL ou FORTE.");
        }
    }

    // ============================================================================
    // MÉTHODES DE GESTION DES CONNEXIONS
    // ============================================================================
    
    /**
     * Crée une connexion entre une maison et un générateur.
     * Gère automatiquement l'ordre des paramètres (M1 G1 ou G1 M1).
     */
    

    public void ajouterConnexion(String nom1, String nom2) {
        Maison maison = null;
        Generateur generateur = null;

        String k1 = nom1.toUpperCase();
        String k2 = nom2.toUpperCase();

        // les deux cas (G M ou M G)
        if (maisons.containsKey(k1) && generateurs.containsKey(k2)) {
            maison = maisons.get(k1);
            generateur = generateurs.get(k2);
        } else if (maisons.containsKey(k2) && generateurs.containsKey(k1)) {
            maison = maisons.get(k2);
            generateur = generateurs.get(k1);
        } else {
            System.out.println("Erreur: La maison ou le générateur n'existe pas.");
            return;
        }

        // Si la maison est déjà connectée à un autre générateur, on refuse la reconnexion automatique
        if (maison.getGenerateur() != null) {
            if (maison.getGenerateur() == generateur) {
                System.out.println("Info: La maison " + maison.getNom() + " est déjà connectée au générateur " + generateur.getNom() + ".");
                return;
            } else {
                System.out.println("Erreur: La maison " + maison.getNom() + " est déjà connectée à un autre générateur (" + maison.getGenerateur().getNom() + "). Supprimez d'abord la connexion existante.");
                return;
            }
        }

        // Création de la nouvelle connexion bidirectionnelle
        generateur.ajouterMaison(maison);          // Générateur → Maison

        System.out.println("Connexion créée entre " + maison.getNom() + " et " + generateur.getNom() + ".");
    }

    /**
     * Supprime une connexion existante entre une maison et un générateur.
     * Accepte l'ordre des paramètres indifféremment (M1 G1 ou G1 M1).
     */
    public void supprimerConnexion(String nom1, String nom2) {
        Maison maison = null;
        Generateur generateur = null;

        String k1 = nom1.toUpperCase();
        String k2 = nom2.toUpperCase();

        if (maisons.containsKey(k1) && generateurs.containsKey(k2)) {
            maison = maisons.get(k1);
            generateur = generateurs.get(k2);
        } else if (maisons.containsKey(k2) && generateurs.containsKey(k1)) {
            maison = maisons.get(k2);
            generateur = generateurs.get(k1);
        } else {
            System.out.println("Erreur: La maison ou le générateur n'existe pas.");
            return;
        }

        // Vérifier que la connexion existe réellement
        if (!generateur.getMaisonsConnectees().contains(maison)) {
            System.out.println("Erreur: La connexion entre " + maison.getNom() + " et " + generateur.getNom() + " n'existe pas.");
            return;
        }

        generateur.retirerMaison(maison);
        System.out.println("Connexion supprimée entre " + maison.getNom() + " et " + generateur.getNom() + ".");
    }

    /**
     * Modifie une connexion existante entre une maison et un générateur.
     * Vérifie que l'ancienne connexion existe réellement avant modification.
     */
    public void modifierConnexion(String nomMaisonOld, String nomGenOld, 
                                   String nomMaisonNew, String nomGenNew) {
        // Normaliser clés
        String oldM = nomMaisonOld.toUpperCase();
        String oldG = nomGenOld.toUpperCase();
        String newM = nomMaisonNew.toUpperCase();
        String newG = nomGenNew.toUpperCase();

        // Vérification de l'existence des éléments de l'ancienne connexion
        if (maisons.containsKey(oldM) && generateurs.containsKey(oldG)) {
            Maison maison = maisons.get(oldM);
            Generateur generateurOld = generateurs.get(oldG);

            // Vérification que la connexion existe réellement
            if (maison.getGenerateur() != generateurOld) {
                System.out.println("Erreur: La connexion " + nomMaisonOld + "-" + nomGenOld + " n'existe pas.");
                return;
            }

            // Retrait de l'ancienne connexion
            generateurOld.retirerMaison(maison);
            // vérifier que le nouveau générateur existe et que le nom de la maison n'a pas changé
            if (generateurs.containsKey(newG) && newM.equals(oldM)) {
                ajouterConnexion(nomMaisonNew, nomGenNew);
            } else {
                System.out.println("Erreur: Le générateur " + nomGenNew + " n'existe pas ou le nom de la maison a changé.");
            }
        } else {
            System.out.println("Erreur: L'ancienne connexion n'existe pas.");
        }
    }

    public boolean verifierReseau() {
        StringBuilder problemes = new StringBuilder();

        for (Maison maison : maisons.values()) {
            int compte = 0;
            for (Generateur gen : generateurs.values()) {
                if (gen.getMaisonsConnectees().contains(maison)) {
                    compte++;
                }
            }

            if (compte == 0) {
                if (problemes.length() > 0) problemes.append(", ");
                problemes.append(maison.getNom() + " (pas de connexion)");
            } else if (compte > 1) {
                if (problemes.length() > 0) problemes.append(", ");
                problemes.append(maison.getNom() + " (" + compte + " connexions)");
            }
        }

        if (problemes.length() > 0) {
            System.out.println("Problème: Les maisons suivantes posent problème: " + problemes.toString());
            return false;
        } else {
            System.out.println("Réseau valide: Chaque maison est connectée à un unique générateur.");
            return true;
        }
    }

    // ============================================================================

    // MÉTHODES DE CALCUL DU COÛT

    /**
        * Calcule la dispersion des taux d'utilisation des générateurs.
        * Disp(S) = Σ |u_g - ū| pour tous les générateurs g
    */
    public double calculerDispersion() {
        if (generateurs.isEmpty()) return 0;

        // calcul de la moyenne des taux d'utilisation
        double sommeTaux = 0;
        for (Generateur gen : generateurs.values()) {
            sommeTaux += gen.calculerTauxUtilisation();
        }
        double moyenne = sommeTaux / generateurs.size();
        
        // calcul de la somme des écarts absoluus
        double dispersion = 0;
        for (Generateur gen : generateurs.values()) {
            dispersion += Math.abs(gen.calculerTauxUtilisation() - moyenne);
        }
        
        return dispersion;
    }

    /**
        * Calcule la pénalité totale due aux surcharges des générateurs.
        * Formule : Surcharge(S) = Σ max(0, (L_g - C_g)/C_g) pour tous les générateurs g
            et c'est pareil que max(0, ug - 1) avec ug = Lg/Cg 
     */
    
    public double calculerSurcharge() {
        double surcharge = 0;
        
        for (Generateur gen : generateurs.values()) {
            double ug = gen.calculerTauxUtilisation();    
            surcharge += Math.max(0, (ug - 1));
        }
        
        return surcharge;
    }

    /**
     * Calcule le coût total du réseau électrique actuel.
     * Formule : Cout(S) = Disp(S) + λ × Surcharge(S)
     */
    public double calculerCout() {
        double dispersion = calculerDispersion();
        double surcharge = calculerSurcharge();
        double cout = dispersion + penalite * surcharge;
        
        System.out.println("\n=== Calcul du coût ===");
        System.out.println ("Dispersion (Disp(S)): " + dispersion); 
        System.out.println("Surcharge (Surcharge(S)): " + surcharge); 
        System.out.println("Coût total (λ=" + penalite + "): " + cout);
        System.out.println("======================\n");
        
        return cout;
    }

    // ============================================================================
    // MÉTHODES D'AFFICHAGE
    // ============================================================================
    
    public void afficherReseau() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("RÉSEAU ÉLECTRIQUE");
        System.out.println("=".repeat(60));
        
        // Affichage par générateurs
        System.out.println("\n--- GÉNÉRATEURS ---");
        for (Generateur gen : generateurs.values()) {
            System.out.println("  " + gen);
            
            if (!gen.getMaisonsConnectees().isEmpty()) {
                System.out.println("    Maisons connectées:");
                for (Maison maison : gen.getMaisonsConnectees()) {
                    System.out.println("      - " + maison);
                }
            } else {
                System.out.println("    Aucune maison connectée");
            }
        }
        
        // Affichage par maisons
        System.out.println("\n--- MAISONS ---");
        for (Maison maison : maisons.values()) {
            Generateur gen = maison.getGenerateur();
            String genNom = (gen != null) ? gen.getNom() : "Non connectée";
            System.out.println("  " + maison + " → Générateur: " + genNom);
        }
        
        System.out.println("\n" + "=".repeat(60) + "\n");
    }
}
