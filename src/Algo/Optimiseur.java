package Algo;
import Model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Optimiseur {

    // Paramètre de perturbation entre passes (fraction de maisons perturbées)
    private static final double PERTURBATION_FRACTION = 0.20; // 20% des maisons

    /**
     * Optimise le réseau avec stratégie simplifiée et efficace:
     * 1. Hill Climbing exhaustif (teste tous les générateurs pour chaque maison)
     * 2. Multi-passes avec perturbations légères pour échapper aux minima locaux
     * 
     * Plus simple que SA, presque aussi efficace, déterministe sur chaque passe.
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
        
        // Nombre de passes : minimum 3, maximum 10, basé sur k
        int nbPasses = Math.max(3, Math.min(10, k / 50));
        int ameliorationsTotal = 0;

        System.out.println("Démarrage optimisation (" + nbPasses + " passes HC)...");

        for (int passe = 0; passe < nbPasses; passe++) {
            // Hill Climbing exhaustif jusqu'à convergence
            boolean ameliore = true;
            int iterationsPass = 0;
            
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
                System.out.println("  Passe " + (passe + 1) + ": nouveau meilleur → " + String.format("%.2f", coutActuel));
            }

            // Perturbation légère pour la prochaine passe (sauf dernière)
            if (passe < nbPasses - 1) {
                int nbPerturbations = Math.max(1, (int) (maisons.size() * PERTURBATION_FRACTION));
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

        // Restaurer la meilleure configuration
        restaurerConfiguration(meilleurConfig);
        
        System.out.println("Optimisation terminée: " + ameliorationsTotal + " améliorations.");
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
