package Algo;
import Model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Optimiseur {

    // Paramètres du recuit simulé 
    private static final double TEMP_INIT_FACTOR = 0.50;    // T0 = coût * 0.50
    private static final double COOLING_FACTOR   = 0.95;    // T <- T * 0.95

    // Paramètre de perturbation entre redémarrages (fraction de maisons perturbées)
    private static final double PERTURBATION_FRACTION = 1.0 / 3.0; // ~33% des maisons

    /**
     * Optimise le réseau avec stratégie hybride avancée:
     * 1. Hill Climbing exhaustif (teste tous les générateurs pour chaque maison)
     * 2. Perturbations aléatoires pour échapper aux minima locaux
     * 3. Redémarrages multiples pour explorer l'espace des solutions
     * 4. Simulated Annealing pour accepter temporairement des solutions pires
     */
    public static void optimiserReseau(ReseauElectrique reseau, int k) {
        if (reseau == null) return;
        List<Maison> maisons = new ArrayList<>(reseau.getMaisons());
        List<Generateur> generateurs = new ArrayList<>(reseau.getGenerateurs());
        if (maisons.isEmpty() || generateurs.isEmpty()) {
            System.out.println("Réseau vide, impossible d'optimiser.");
            return;
        }

        Random random = new Random();
        double coutInitial = reseau.calculerCoutSilencieux();
        double meilleurCoutGlobal = coutInitial;
        
        // Sauvegarder la meilleure configuration
        List<ConfigurationMaison> meilleurConfig = sauvegarderConfiguration(maisons);
        
        int nbRedemarrages = Math.max(3, k / 100); // Au moins 3 redémarrages
        int iterationsParRedemarrage = k / nbRedemarrages;
        int ameliorationsTotal = 0;

        System.out.println("Démarrage optimisation hybride (" + nbRedemarrages + " redémarrages)...");

        for (int redemarrage = 0; redemarrage < nbRedemarrages; redemarrage++) {
            // Phase 1: Hill Climbing exhaustif
            boolean ameliore = true;
            int iterHC = 0;
            
            while (ameliore && iterHC < iterationsParRedemarrage / 2) {
                ameliore = false;
                
                for (Maison m : maisons) {
                    Generateur ancienGen = m.getGenerateur();
                    double coutActuel = reseau.calculerCoutSilencieux();
                    Generateur meilleurGen = ancienGen;
                    double meilleurCout = coutActuel;

                    // Tester tous les générateurs
                    for (Generateur g : generateurs) {
                        if (g == ancienGen) continue;

                        if (ancienGen != null) ancienGen.retirerMaison(m);
                        g.ajouterMaison(m);
                        double nouveauCout = reseau.calculerCoutSilencieux();

                        if (nouveauCout < meilleurCout) {
                            meilleurCout = nouveauCout;
                            meilleurGen = g;
                        }

                        g.retirerMaison(m);
                        if (ancienGen != null) ancienGen.ajouterMaison(m);
                    }

                    if (meilleurGen != ancienGen) {
                        if (ancienGen != null) ancienGen.retirerMaison(m);
                        meilleurGen.ajouterMaison(m);
                        ameliorationsTotal++;
                        ameliore = true;
                    }
                }
                iterHC++;
            }

            double coutApresHC = reseau.calculerCoutSilencieux();

            // Phase 2: Simulated Annealing pour échapper au minimum local
            double temperature = coutApresHC * TEMP_INIT_FACTOR; // Température initiale
            double refroidissement = COOLING_FACTOR;
            int iterSA = iterationsParRedemarrage / 2;

            for (int i = 0; i < iterSA; i++) {
                Maison m = maisons.get(random.nextInt(maisons.size()));
                Generateur g = generateurs.get(random.nextInt(generateurs.size()));
                Generateur ancien = m.getGenerateur();
                
                if (ancien == g) continue;

                double coutActuel = reseau.calculerCoutSilencieux();
                
                if (ancien != null) ancien.retirerMaison(m);
                g.ajouterMaison(m);
                double coutNouveau = reseau.calculerCoutSilencieux();

                double delta = coutNouveau - coutActuel;
                
                // Accepter si amélioration OU avec probabilité selon température
                if (delta < 0 || random.nextDouble() < Math.exp(-delta / temperature)) {
                    ameliorationsTotal++;
                } else {
                    // Rejeter le changement
                    g.retirerMaison(m);
                    if (ancien != null) ancien.ajouterMaison(m);
                }
                
                temperature *= refroidissement;
            }

            double coutFinal = reseau.calculerCoutSilencieux();
            
            // Garder la meilleure solution globale
            if (coutFinal < meilleurCoutGlobal) {
                meilleurCoutGlobal = coutFinal;
                meilleurConfig = sauvegarderConfiguration(maisons);
                System.out.println("  Redémarrage " + (redemarrage + 1) + ": nouveau meilleur → " + String.format("%.2f", coutFinal));
            }

            // Phase 3: Perturbation pour le prochain redémarrage (sauf dernier)
            if (redemarrage < nbRedemarrages - 1) {
                int nbPerturbations = Math.max(1, (int) Math.round(maisons.size() * PERTURBATION_FRACTION)); // Perturber ~33% des maisons
                for (int p = 0; p < nbPerturbations; p++) {
                    Maison m = maisons.get(random.nextInt(maisons.size()));
                    Generateur g = generateurs.get(random.nextInt(generateurs.size()));
                    Generateur ancien = m.getGenerateur();
                    
                    if (ancien != g) {
                        if (ancien != null) ancien.retirerMaison(m);
                        g.ajouterMaison(m);
                    }
                }
            }
        }

        // Restaurer la meilleure configuration trouvée
        restaurerConfiguration(meilleurConfig);
        
        System.out.println("Optimisation terminée: " + ameliorationsTotal + " changements testés.");
        System.out.println("Coût: " + String.format("%.2f", coutInitial) + " → " + String.format("%.2f", meilleurCoutGlobal) 
                         + " (amélioration: " + String.format("%.1f%%", (coutInitial - meilleurCoutGlobal) / coutInitial * 100) + ")");
    }

    // Classe interne pour sauvegarder la configuration
    private static class ConfigurationMaison {
        Maison maison;
        Generateur generateur;
        ConfigurationMaison(Maison m, Generateur g) {
            this.maison = m;
            this.generateur = g;
        }
    }

    private static List<ConfigurationMaison> sauvegarderConfiguration(List<Maison> maisons) {
        List<ConfigurationMaison> config = new ArrayList<>();
        for (Maison m : maisons) {
            config.add(new ConfigurationMaison(m, m.getGenerateur()));
        }
        return config;
    }

    private static void restaurerConfiguration(List<ConfigurationMaison> config) {
        for (ConfigurationMaison c : config) {
            Generateur ancien = c.maison.getGenerateur();
            if (ancien != c.generateur) {
                if (ancien != null) ancien.retirerMaison(c.maison);
                if (c.generateur != null) c.generateur.ajouterMaison(c.maison);
            }
        }
    }

    /**
     * Algorithme naïf original (aléatoire pur).
     * Gardé pour comparaison.
     */
    public static void optimiserNaif(ReseauElectrique reseau, int k) {
        if (reseau == null) return;
        List<Maison> maisons = new ArrayList<>(reseau.getMaisons());
        List<Generateur> generateurs = new ArrayList<>(reseau.getGenerateurs());
        if (maisons.isEmpty() || generateurs.isEmpty()) return;

        Random random = new Random();
        int ameliorations = 0;

        for (int i = 0; i < k; i++) {
            Maison m = maisons.get(random.nextInt(maisons.size()));
            Generateur g = generateurs.get(random.nextInt(generateurs.size()));
            Generateur ancien = m.getGenerateur();
            if (ancien == g) continue;

            double coutActuel = reseau.calculerCoutSilencieux();
            if (ancien != null) ancien.retirerMaison(m);
            g.ajouterMaison(m);
            double coutNouveau = reseau.calculerCoutSilencieux();

            if (coutNouveau < coutActuel) {
                ameliorations++;
            } else {
                g.retirerMaison(m);
                if (ancien != null) ancien.ajouterMaison(m);
            }
        }

        System.out.println("Optimisation Naïve: " + ameliorations + " améliorations sur " + k + " tentatives.");
    }
}
