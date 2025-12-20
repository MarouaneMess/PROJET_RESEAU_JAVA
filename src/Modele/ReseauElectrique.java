package Modele;
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
 
    public ReseauElectrique(int penalite) throws IllegalArgumentException {
        if (penalite <= 0) {
            throw new IllegalArgumentException("La pénalité doit être positive (>0).");
        }
        this.maisons = new LinkedHashMap<>();
        this.generateurs = new LinkedHashMap<>();
        this.penalite = penalite;
    }
    public ReseauElectrique() throws IllegalArgumentException {
        // constructeur par défaut avec pénalité 10, on fait appel au constructeur principal    
        this(10);
    }

    public Collection<Maison> getMaisons() {
        return maisons.values();
    }
    public Collection<Generateur> getGenerateurs() {
        return generateurs.values();
    }

    public boolean chargerDepuisFichier(String cheminFichier) throws IOException, IllegalArgumentException {
        if (cheminFichier == null || cheminFichier.trim().isEmpty()) {
            throw new IllegalArgumentException("Le chemin du fichier ne peut pas être vide.");
        }
        Map<String, Generateur> nouveauxGenerateurs = new LinkedHashMap<>();
        Map<String, Maison> nouvellesMaisons = new LinkedHashMap<>();
        int phase = 0;

        try {
            int ligneNo = 0;
            for (String rawLine : Files.readAllLines(Path.of(cheminFichier))) {
                ligneNo++;
                String line = rawLine.trim();
                if (line.isEmpty()) continue;

                if (line.endsWith(".")) {
                    line = line.substring(0, line.length() - 1).trim();
                }

                if (line.toLowerCase().startsWith("generateur(") && line.endsWith(")")) {
                    if (phase > 0) {
                        throw new IllegalArgumentException("Erreur ligne " + ligneNo + " : générateurs avant maisons/connexions.");
                    }
                    String contenu = line.substring(11, line.length() - 1);
                    String[] parts = contenu.split(",");
                    if (parts.length != 2) {
                        throw new IllegalArgumentException("Erreur ligne " + ligneNo + " : format générateur invalide.");
                    }
                    String nom = parts[0].trim().toUpperCase();
                    int capacite = Integer.parseInt(parts[1].trim());
                    if (nouveauxGenerateurs.containsKey(nom)) {
                        throw new IllegalArgumentException("Erreur ligne " + ligneNo + " : générateur en double.");
                    }
                    nouveauxGenerateurs.put(nom, new Generateur(nom, capacite));
                }
                else if (line.toLowerCase().startsWith("maison(") && line.endsWith(")")) {
                    if (nouveauxGenerateurs.isEmpty()) {
                        throw new IllegalArgumentException("Erreur ligne " + ligneNo + " : définir générateurs d'abord.");
                    }
                    if (phase == 0) phase = 1;
                    else if (phase > 1) {
                        throw new IllegalArgumentException("Erreur ligne " + ligneNo + " : maisons avant connexions.");
                    }
                    String contenu = line.substring(7, line.length() - 1);
                    String[] parts = contenu.split(",");
                    if (parts.length != 2) {
                        throw new IllegalArgumentException("Erreur ligne " + ligneNo + " : format maison invalide.");
                    }
                    String nom = parts[0].trim().toUpperCase();
                    String type = parts[1].trim().toUpperCase();
                    if (nouvellesMaisons.containsKey(nom)) {
                        throw new IllegalArgumentException("Erreur ligne " + ligneNo + " : maison en double.");
                    }
                    nouvellesMaisons.put(nom, new Maison(nom, TypeConsommation.valueOf(type)));
                }
                else if (line.toLowerCase().startsWith("connexion(") && line.endsWith(")")) {
                    if (nouvellesMaisons.isEmpty()) {
                        throw new IllegalArgumentException("Erreur ligne " + ligneNo + " : définir maisons d'abord.");
                    }
                    phase = 2;
                    String contenu = line.substring(10, line.length() - 1);
                    String[] parts = contenu.split(",");
                    if (parts.length != 2) {
                        throw new IllegalArgumentException("Erreur ligne " + ligneNo + " : format connexion invalide.");
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
                        throw new IllegalArgumentException("Erreur ligne " + ligneNo + " : élément inconnu.");
                    }
                    if (maison.getGenerateur() != null) {
                        throw new IllegalArgumentException("Erreur ligne " + ligneNo + " : maison déjà connectée.");
                    }
                    gen.ajouterMaison(maison);
                } else {
                    throw new IllegalArgumentException("Erreur ligne " + ligneNo + " : format inconnu.");
                }
            }
        } catch (IOException e) {
            throw new IOException("Erreur: fichier illisible (" + e.getMessage() + ").", e);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Erreur de parsing numérique: " + e.getMessage(), e);
        }

        this.generateurs = nouveauxGenerateurs;
        this.maisons = nouvellesMaisons;
        System.out.println("Réseau chargé: " + generateurs.size() + " générateur(s), " + maisons.size() + " maison(s).");
        return true;
    }

    public void ajouterGenerateur(String nom, int capaciteMax) {
        if (nom == null || nom.isEmpty()) {
            throw new IllegalArgumentException("Le nom du générateur ne peut pas être vide.");
        }
        if (capaciteMax <= 0) {
            throw new IllegalArgumentException("La capacité doit être positive.");
        }
        String nomUpper = nom.toUpperCase();
        if (generateurs.containsKey(nomUpper)) {
            System.out.println("Mise à jour: Le générateur " + nomUpper + " existe déjà. Capacité mise à jour.");
            generateurs.get(nomUpper).setCapaciteMax(capaciteMax);
        } else {
            generateurs.put(nomUpper, new Generateur(nomUpper, capaciteMax));
            System.out.println("Générateur " + nomUpper + " ajouté avec succès.");
        }
    }

    public void ajouterMaison(String nom, String typeConsommationStr) throws IllegalArgumentException {
        TypeConsommation type = TypeConsommation.valueOf(typeConsommationStr.toUpperCase());
        String nomUpper = nom.toUpperCase();
        if (maisons.containsKey(nomUpper)) {
            System.out.println("Mise à jour: La maison " + nomUpper + " existe déjà. Consommation mise à jour.");
        } else {
            System.out.println("Maison " + nomUpper + " ajoutée avec succès.");
        }

        maisons.put(nomUpper, new Maison(nomUpper, type));
    }

    // ============================================================================
    // MÉTHODES DE GESTION DES CONNEXIONS
    // ============================================================================
    
    /**
     * Crée une connexion entre une maison et un générateur.
     * Gère automatiquement l'ordre des paramètres (M1 G1 ou G1 M1).
     */
    

    public void ajouterConnexion(String nom1, String nom2) throws IllegalArgumentException {
        if (nom1 == null || nom1.trim().isEmpty() || nom2 == null || nom2.trim().isEmpty()) {
            throw new IllegalArgumentException("Les noms ne peuvent pas être vides.");
        }
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
            throw new IllegalArgumentException("La maison ou le générateur n'existe pas.");
        }

        if (maison.getGenerateur() != null) {
            if (maison.getGenerateur() == generateur) {
                throw new IllegalArgumentException("La maison " + maison.getNom() + " est déjà connectée au générateur " + generateur.getNom() + ".");
            } else {
                throw new IllegalArgumentException("La maison " + maison.getNom() + " est déjà connectée à " + maison.getGenerateur().getNom() + ".");
            }
        }

        generateur.ajouterMaison(maison);
        System.out.println("Connexion créée entre " + maison.getNom() + " et " + generateur.getNom() + ".");
    }

    /**
     * Supprime une connexion existante entre une maison et un générateur.
     * Accepte l'ordre des paramètres indifféremment (M1 G1 ou G1 M1).
     */
    public void supprimerConnexion(String nom1, String nom2) throws IllegalArgumentException {
        if (nom1 == null || nom1.trim().isEmpty() || nom2 == null || nom2.trim().isEmpty()) {
            throw new IllegalArgumentException("Les noms ne peuvent pas être vides.");
        }
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
            throw new IllegalArgumentException("La maison ou le générateur n'existe pas.");
        }

        if (!generateur.getMaisonsConnectees().contains(maison)) {
            throw new IllegalArgumentException("La connexion entre " + maison.getNom() + " et " + generateur.getNom() + " n'existe pas.");
        }

        generateur.retirerMaison(maison);
        System.out.println("Connexion supprimée entre " + maison.getNom() + " et " + generateur.getNom() + ".");
    }

    public void modifierConnexion(String nomMaisonOld, String nomGenOld, 
                                   String nomMaisonNew, String nomGenNew) throws IllegalArgumentException {
        if (nomMaisonOld == null || nomGenOld == null || nomMaisonNew == null || nomGenNew == null) {
            throw new IllegalArgumentException("Les noms ne peuvent pas être null.");
        }
        String oldM = nomMaisonOld.toUpperCase();
        String oldG = nomGenOld.toUpperCase();
        String newM = nomMaisonNew.toUpperCase();
        String newG = nomGenNew.toUpperCase();

        if (!maisons.containsKey(oldM) || !generateurs.containsKey(oldG)) {
            throw new IllegalArgumentException("L'ancienne connexion n'existe pas.");
        }
        
        Maison maison = maisons.get(oldM);
        Generateur generateurOld = generateurs.get(oldG);

        if (maison.getGenerateur() != generateurOld) {
            throw new IllegalArgumentException("La connexion " + nomMaisonOld + "-" + nomGenOld + " n'existe pas.");
        }

        generateurOld.retirerMaison(maison);
        
        if (!generateurs.containsKey(newG) || !newM.equals(oldM)) {
            generateurOld.ajouterMaison(maison);
            throw new IllegalArgumentException("Le générateur " + nomGenNew + " n'existe pas ou le nom de la maison a changé.");
        }

        ajouterConnexion(nomMaisonNew, nomGenNew);
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
