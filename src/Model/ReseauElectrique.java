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
        if (generateurs.containsKey(nom)) {
            // Mise à jour de la capacité d'un générateur existant
            System.out.println("Mise à jour: Le générateur " + nom + " existe déjà. Capacité mise à jour.");
            generateurs.get(nom).setCapaciteMax(capaciteMax);
        } else {
            // Création d'un nouveau générateur
            generateurs.put(nom, new Generateur(nom, capaciteMax));
            System.out.println("Générateur " + nom + " ajouté avec succès.");
        }
    }

    public void ajouterMaison(String nom, String typeConsommationStr) {
        try {
            // conbersion du string en enum
            TypeConsommation type = TypeConsommation.valueOf(typeConsommationStr.toUpperCase());
            
            if (maisons.containsKey(nom)) {
                System.out.println("Mise à jour: La maison " + nom + " existe déjà. Consommation mise à jour.");
            } else {
                System.out.println("Maison " + nom + " ajoutée avec succès.");
            }
            
            // put() écrase l'ancienne valeur si la clé existe
            maisons.put(nom, new Maison(nom, type));
            
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
        
        // on travaille sur les deux cas (G M ou M G)
        if (maisons.containsKey(nom1) && generateurs.containsKey(nom2)) {
            maison = maisons.get(nom1);
            generateur = generateurs.get(nom2);
        } else if (maisons.containsKey(nom2) && generateurs.containsKey(nom1)) {
            maison = maisons.get(nom2);
            generateur = generateurs.get(nom1);
        } else {
            System.out.println("Erreur: La maison ou le générateur n'existe pas.");
            return;
        }
        // Création de la nouvelle connexion bidirectionnelle
        //maison.setGenerateur(generateur);  Maison → Générateur (on peut l'enlever car fait dans generateur.ajouterMaison)
        generateur.ajouterMaison(maison);          // Générateur → Maison
        
        System.out.println("Connexion créée entre " + maison.getNom() + " et " + generateur.getNom() + ".");
    }

    /**
     * Modifie une connexion existante entre une maison et un générateur.
     * Vérifie que l'ancienne connexion existe réellement avant modification.
     */
    public void modifierConnexion(String nomMaisonOld, String nomGenOld, 
                                   String nomMaisonNew, String nomGenNew) {
        // Vérification de l'existence des éléments de l'ancienne connexion
        if (maisons.containsKey(nomMaisonOld) && generateurs.containsKey(nomGenOld)) {
            Maison maison = maisons.get(nomMaisonOld);
            Generateur generateurOld = generateurs.get(nomGenOld);

            // Vérification que la connexion existe réellement
            // Comparaison par référence (!=) car même instance (sans equals)
            if (maison.getGenerateur() != generateurOld) {
                System.out.println("Erreur: La connexion " + nomMaisonOld + "-" + nomGenOld + " n'existe pas.");
                return;
            }

            // Création de la nouvelle connexion via ajouterConnexion()
            generateurOld.retirerMaison(maison); // Retrait de l'ancienne connexion
            // vérifier que le nouveau générateur existe et que le nom de la maison n'a pas changé
            if (generateurs.containsKey(nomGenNew) && nomMaisonNew.equals(nomMaisonOld)) {
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
        //maisons.values() car on a juste besoin des maisons 
        for (Maison maison : maisons.values()) {
            if (maison.getGenerateur() == null) {
                if (problemes.length() > 0) {
                    problemes.append(", ");
                }
                problemes.append(maison.getNom());
            }
        }
        
        if (problemes.length() > 0) {
            System.out.println("Problème: Les maisons suivantes ne sont pas connectées: " + problemes.toString());
            return false;
        } else {
            System.out.println("Réseau valide: Toutes les maisons sont connectées.");
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
