# Algorithme d'Optimisation du Réseau Électrique

## Vue d'ensemble

L'algorithme d'optimisation implémenté dans `Algo.Optimiseur` utilise une **stratégie hybride avancée** combinant plusieurs techniques métaheuristiques pour trouver une configuration optimale du réseau électrique minimisant le coût total.

## Objectif

Minimiser la fonction de coût :
```
Cout(S) = Disp(S) + λ × Surcharge(S)
```

Où :
- **Disp(S)** = Dispersion des taux d'utilisation des générateurs
- **Surcharge(S)** = Pénalité pour les générateurs surchargés
- **λ** = Coefficient de pénalisation (paramètre du système)

---

## Architecture de l'Algorithme

L'algorithme se décompose en **3 phases principales** exécutées sur plusieurs redémarrages :

### Phase 1 : Hill Climbing Exhaustif
**Objectif** : Converger rapidement vers un optimum local

**Fonctionnement** :
1. Pour chaque maison du réseau :
   - Tester **tous** les générateurs disponibles
   - Calculer le coût pour chaque connexion possible
   - Garder le générateur donnant le **meilleur coût**
2. Répéter tant qu'une amélioration est trouvée
3. S'arrêter à la convergence (aucun changement n'améliore le coût)

**Avantages** :
- ✅ Déterministe et puissant
- ✅ Explore exhaustivement le voisinage local
- ✅ Converge rapidement (quelques itérations suffisent)

**Limites** :
- ⚠️ Peut rester bloqué dans un minimum local

---

### Phase 2 : Simulated Annealing
**Objectif** : Échapper aux minima locaux en acceptant temporairement des solutions pires

**Fonctionnement** :
1. Choisir aléatoirement une maison et un générateur
2. Calculer le changement de coût (Δ = coût_nouveau - coût_actuel)
3. **Critère d'acceptation** :
   - Si Δ < 0 (amélioration) → **accepter**
   - Si Δ ≥ 0 (dégradation) → accepter avec probabilité `exp(-Δ / T)`
4. Réduire la température : `T ← T × 0.95` (refroidissement)

**Paramètres** :
- Température initiale : `T₀ = coût_actuel × 0.5`
- Coefficient de refroidissement : `α = 0.95`
- Nombre d'itérations : `k / (2 × nb_redémarrages)`

**Avantages** :
- ✅ Peut sortir des minima locaux
- ✅ Balance exploration (haute T) et exploitation (basse T)

**Limites** :
- ⚠️ Résultats légèrement aléatoires

---

### Phase 3 : Perturbations et Redémarrages
**Objectif** : Explorer différentes régions de l'espace des solutions

**Fonctionnement** :
1. Après chaque cycle (HC + SA), sauvegarder la meilleure solution trouvée
2. Avant le redémarrage suivant :
   - Perturber **1/3 des maisons** en les connectant aléatoirement
   - Cela force l'exploration d'autres zones
3. Exécuter un nouveau cycle complet
4. À la fin, **restaurer la meilleure solution globale**

**Avantages** :
- ✅ Évite de rester coincé dans le même minimum local
- ✅ Augmente les chances de trouver l'optimum global

---

## Paramètres de l'Algorithme

### Paramètre d'entrée : `k` (nombre de tentatives)

Le paramètre `k` contrôle l'intensité de la recherche :

| Valeur de k | Nb Redémarrages | Itérations HC | Itérations SA | Temps |
|-------------|-----------------|---------------|---------------|--------|
| k = 10      | 3               | ~1-2          | ~1-2          | Rapide |
| k = 100     | 3               | ~15-20        | ~15-20        | Moyen  |
| k = 1000    | 10              | ~50           | ~50           | Long   |

**Formule** :
```
nb_redémarrages = max(3, k / 100)
iter_par_redemarrage = k / nb_redémarrages
iter_HC = iter_par_redemarrage / 2
iter_SA = iter_par_redemarrage / 2
```

**Recommandations** :
- **k = 10-50** : Tests rapides, exploration limitée
- **k = 100-500** : Bon compromis qualité/temps
- **k = 1000+** : Optimisation intensive, meilleurs résultats

### Paramètres du recuit simulé (Simulated Annealing)
- **TEMP_INIT_FACTOR** (= 0.50) : T₀ = coût × 0.5
   - Choisi empiriquement : on part d'une température proportionnelle au coût pour accepter quelques dégradations au début puis se stabiliser.
- **COOLING_FACTOR** (= 0.95) : T ← T × 0.95
   - Testé entre 0.90 et 0.99 ; 0.95 offre un bon équilibre exploration/exploitation sur les instances 1–7.

### Paramètre de perturbation entre redémarrages
- **PERTURBATION_FRACTION** (= 1/3 des maisons)
   - Perturber ~33% évite de casser totalement une bonne solution tout en explorant d'autres vallées de solutions. Testé entre 20% et 50% ; 33% s'est montré le plus stable.

---

## Structure des Données

### Configuration Sauvegardée
```java
class ConfigurationMaison {
    Maison maison;
    Generateur generateur;
}
```

Chaque fois qu'une amélioration est trouvée, la configuration complète est sauvegardée pour pouvoir la restaurer à la fin.

---

## Complexité

### Complexité temporelle
- **Hill Climbing** : O(nb_iterations × nb_maisons × nb_générateurs)
- **Simulated Annealing** : O(nb_iterations)
- **Total** : O(k × nb_maisons × nb_générateurs)

### Complexité spatiale
- O(nb_maisons) pour sauvegarder la meilleure configuration

---

## Exemple d'Utilisation

### Compilation
```bash
javac -d ./bin ./src/Model/*.java ./src/Algo/*.java ./src/Main.java
```

### Exécution

**Mode manuel** (partie 1) :
```bash
java -cp ./bin Main
```

**Mode automatique** (partie 2) :
```bash
java -cp ./bin Main instance/instance1.txt 10
```
- Premier argument : chemin du fichier réseau
- Deuxième argument : pénalité λ

**Dans le menu automatique** :
```
1. Résolution automatique
   → Saisir k (ex: 1000)
2. Sauvegarder la solution actuelle
   → Saisir nom de fichier (ex: solution.txt)
3. Fin
```

---

## Résultats Attendus

### Instance 1 (6 générateurs, 9 maisons)
```
Coût initial : ~34.88
Coût optimal trouvé : 0.698 - 1.10
Amélioration : 96-98%
```

### Performances

| k   | Temps moyen | Qualité solution |
|-----|-------------|------------------|
| 10  | < 1s        | Bonne            |
| 100 | 1-3s        | Très bonne       |
| 500 | 5-10s       | Excellente       |

---

## Limites et Améliorations Futures

### Limites actuelles
1. **Variabilité** : Résultats légèrement différents à chaque exécution (dû au SA)
2. **Pas de garantie d'optimalité** : Algorithme approximatif (métaheuristique)
3. **Pas de parallélisation** : Exécution séquentielle

### Améliorations possibles
1. **Tabu Search** : Mémoriser les solutions récentes pour éviter les cycles
2. **Algorithme génétique** : Population de solutions évoluant par croisement
3. **Parallélisation** : Exécuter plusieurs redémarrages en parallèle
4. **Adaptation dynamique** : Ajuster automatiquement les paramètres selon le réseau

---

## Comparaison avec l'Algorithme Naïf

Une version naïve (`optimiserNaif`) est conservée pour comparaison :

| Aspect          | Algorithme Naïf      | Algorithme Hybride       |
|-----------------|----------------------|--------------------------|
| Stratégie       | Aléatoire pur        | HC + SA + Redémarrages   |
| Exploration     | Faible               | Intensive                |
| Qualité         | ~70-80%              | ~96-98%                  |
| Déterminisme    | Très aléatoire       | Semi-déterministe        |
| Temps           | Rapide               | Moyen                    |

---

## Références Théoriques

### Hill Climbing
- Algorithme glouton local
- Converge vers optimum local
- Pas de garantie d'optimalité globale

### Simulated Annealing
- Inspiré du recuit simulé en métallurgie
- Probabilité d'acceptation : `P(accept) = exp(-ΔE / kT)`
- Température décroissante pour convergence

### Multi-start
- Exécutions multiples avec configurations initiales différentes
- Augmente la probabilité de trouver l'optimum global
- Efficace pour espaces de recherche multimodaux

---

## Auteurs et Licence

Projet réalisé dans le cadre du cours de Réseaux Électriques.
Algorithme d'optimisation : Stratégie hybride (Hill Climbing + Simulated Annealing + Multi-start).
