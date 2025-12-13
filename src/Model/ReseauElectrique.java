package Model;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
public class ReseauElectrique {

    //   Map des maisons du réseau, indexées par leur nom.
    //   LinkedHashMap pour préserver l'ordre d'insertion (aligné sur le fichier chargé).
    private Map<String, Maison> maisons;
    private Map<String, Generateur> generateurs; // pareil pour les générateurs
    private int penalite; // val par défaut = 10
 
    public ReseauElectrique(int penalite) {
        this.maisons = new LinkedHashMap<>();
        this.generateurs = new LinkedHashMap<>();
        this.penalite = penalite;
    }
    public ReseauElectrique() {
        // constructeur par défaut avec pénalité 10, on fait appel au constructeur principal    
        this(10); 
    }

    public Collection<Maison> getMaisons() {
        return maisons.values();
    }
    public Collection<Generateur> getGenerateurs() {
        return generateurs.values();
    }

    /**
     * Recharge complètement le réseau à partir d'un fichier texte.
     */
    public boolean chargerDepuisFichier(String cheminFichier) {
        Map<String, Generateur> nouveauxGenerateurs = new LinkedHashMap<>();
        Map<String, Maison> nouvellesMaisons = new LinkedHashMap<>();
        int phase = 0; // 0: generateurs, 1: maisons, 2: connexions

        try {
            int ligneNo = 0;
            for (String rawLine : Files.readAllLines(Path.of(cheminFichier))) {
                ligneNo++;
                String line = rawLine.trim();
                if (line.isEmpty()) continue;

                // Retirer le point final
                if (line.endsWith(".")) {
                    line = line.substring(0, line.length() - 1).trim();
                }

                // Générateurs: generateur(nom,capacite)
                if (line.toLowerCase().startsWith("generateur(") && line.endsWith(")")) {
                    if (phase > 0) {
                        System.out.println("Erreur ligne " + ligneNo + " : générateurs avant maisons/connexions.");
                        return false;
                    }
                    String contenu = line.substring(11, line.length() - 1); // enlever "generateur(" et ")"
                    String[] parts = contenu.split(",");
                    if (parts.length != 2) {
                        System.out.println("Erreur ligne " + ligneNo + " : format générateur invalide.");
                        return false;
                    }
                    String nom = parts[0].trim().toUpperCase();
                    int capacite = Integer.parseInt(parts[1].trim());
                    if (nouveauxGenerateurs.containsKey(nom)) {
                        System.out.println("Erreur ligne " + ligneNo + " : générateur en double.");
                        return false;
                    }
                    nouveauxGenerateurs.put(nom, new Generateur(nom, capacite));
                }
                // Maisons: maison(nom,TYPE)
                else if (line.toLowerCase().startsWith("maison(") && line.endsWith(")")) {
                    if (nouveauxGenerateurs.isEmpty()) {
                        System.out.println("Erreur ligne " + ligneNo + " : définir générateurs d'abord.");
                        return false;
                    }
                    if (phase == 0) phase = 1;
                    else if (phase > 1) {
                        System.out.println("Erreur ligne " + ligneNo + " : maisons avant connexions.");
                        return false;
                    }
                    String contenu = line.substring(7, line.length() - 1); // enlever "maison(" et ")"
                    String[] parts = contenu.split(",");
                    if (parts.length != 2) {
                        System.out.println("Erreur ligne " + ligneNo + " : format maison invalide.");
                        return false;
                    }
                    String nom = parts[0].trim().toUpperCase();
                    String type = parts[1].trim().toUpperCase();
                    if (nouvellesMaisons.containsKey(nom)) {
                        System.out.println("Erreur ligne " + ligneNo + " : maison en double.");
                        return false;
                    }
                    nouvellesMaisons.put(nom, new Maison(nom, TypeConsommation.valueOf(type)));
                }
                // Connexions: connexion(a,b)
                else if (line.toLowerCase().startsWith("connexion(") && line.endsWith(")")) {
                    if (nouvellesMaisons.isEmpty()) {
                        System.out.println("Erreur ligne " + ligneNo + " : définir maisons d'abord.");
                        return false;
                    }
                    phase = 2;
                    String contenu = line.substring(10, line.length() - 1); // enlever "connexion(" et ")"
                    String[] parts = contenu.split(",");
                    if (parts.length != 2) {
                        System.out.println("Erreur ligne " + ligneNo + " : format connexion invalide.");
                        return false;
                    }
                    String n1 = parts[0].trim().toUpperCase();
                    String n2 = parts[1].trim().toUpperCase();

                    Generateur gen = nouveauxGenerateurs.get(n1);
                    Maison maison = nouvellesMaisons.get(n2);
                    if (gen == null || maison == null) {
                        gen = nouveauxGenerateurs.get(n2);
                        maison = nouvellesMaisons.get(n1);
                    }
                    if (gen == null || maison == null) {
                        System.out.println("Erreur ligne " + ligneNo + " : élément inconnu.");
                        return false;
                    }
                    if (maison.getGenerateur() != null) {
                        System.out.println("Erreur ligne " + ligneNo + " : maison déjà connectée.");
                        return false;
                    }
                    gen.ajouterMaison(maison);
                } else {
                    System.out.println("Erreur ligne " + ligneNo + " : format inconnu.");
                    return false;
                }
            }
        } catch (IOException e) {
            System.out.println("Erreur: fichier illisible (" + e.getMessage() + ").");
            return false;
        } catch (Exception e) {
            System.out.println("Erreur de parsing: " + e.getMessage());
            return false;
        }

        this.generateurs = nouveauxGenerateurs;
        this.maisons = nouvellesMaisons;
        System.out.println("Réseau chargé: " + generateurs.size() + " générateur(s), " + maisons.size() + " maison(s).");
        return true;
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
        System.out.println("Coût total (lambda=" + penalite + "): " + cout);
        System.out.println("======================\n");
        
        return cout;
    }

    /**
     * Version du calcul du coût (sans affichage),
     * utilisée par l'algorithme d'optimisation.
     */
    public double calculerCoutSilencieux() {
        double dispersion = calculerDispersion();
        double surcharge = calculerSurcharge();
        return dispersion + penalite * surcharge;
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
