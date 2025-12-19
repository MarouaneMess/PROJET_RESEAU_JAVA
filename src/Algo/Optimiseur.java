package Algo;
import Model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Optimiseur {

    // Paramètre de perturbation: plus agressif pour mieux explorer l'espace
    private static final double PERTURBATION_FRACTION = 0.33; // 33% des maisons

    /**
     * Optimise le réseau avec stratégie adaptative:
     * 1. Hill Climbing exhaustif par passe (tests exhaustifs)
     * 2. Perturbations AGRESSIVES entre passes (33% au lieu de 20%)
     * 3. Multi-passes pour explorer plusieurs bassins
     * 4. Détection de stagnation: augmente perturbation si pas d'amélioration
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
        List<ConfigurationMaison> meilleurConfig = sauvegarderConfiguration(maisons);
        
        // Nombre de passes : plus agressif avec gros k
        int nbPasses = Math.max(5, Math.min(15, k / 30)); // Min 5 passes, Max 15
        int ameliorationsTotal = 0;
        double coutPrecedent = coutInitial;

        System.out.println("Démarrage optimisation (" + nbPasses + " passes HC adaptative)...");

        for (int passe = 0; passe < nbPasses; passe++) {
            // Hill Climbing exhaustif jusqu'à convergence
            boolean ameliore = true;
            int iterationsPass = 0;
            int ameliorationsPass = 0;
            
            while (ameliore && iterationsPass < k / nbPasses) {
                ameliore = false;
                
                for (Maison m : maisons) {
                    Generateur ancienGen = m.getGenerateur();
                    Generateur meilleurGen = ancienGen;
                    double meilleurCout = reseau.calculerCoutSilencieux();

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

                    // Appliquer le meilleur changement
                    if (meilleurGen != ancienGen) {
                        if (ancienGen != null) ancienGen.retirerMaison(m);
                        meilleurGen.ajouterMaison(m);
                        ameliorationsTotal++;
                        ameliorationsPass++;
                        ameliore = true;
                    }
                }
                iterationsPass++;
            }

            double coutActuel = reseau.calculerCoutSilencieux();
            
            // Sauvegarder si meilleur
            if (coutActuel < meilleurCoutGlobal) {
                meilleurCoutGlobal = coutActuel;
                meilleurConfig = sauvegarderConfiguration(maisons);
                System.out.println("  Passe " + (passe + 1) + ": nouveau meilleur → " + String.format("%.3f", coutActuel));
            }

            // Perturbation ADAPTATIVE pour la prochaine passe
            if (passe < nbPasses - 1) {
                // Si stagnation (pas d'amélioration), perturbation PLUS agressif
                double fractionPerturbation = PERTURBATION_FRACTION;
                if (Math.abs(coutActuel - coutPrecedent) < 0.001) {
                    // Stagnation détectée: augmenter perturbation à 50%
                    fractionPerturbation = 0.50;
                }
                
                int nbPerturbations = Math.max(1, (int) Math.round(maisons.size() * fractionPerturbation));
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
            
            coutPrecedent = coutActuel;
        }

        // Restaurer la meilleure configuration
        restaurerConfiguration(meilleurConfig);
        
        System.out.println("Optimisation terminée: " + ameliorationsTotal + " améliorations.");
        System.out.println("Coût: " + String.format("%.3f", coutInitial) + " → " + String.format("%.3f", meilleurCoutGlobal) 
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
