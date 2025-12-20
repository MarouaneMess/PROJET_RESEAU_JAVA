# Projet Réseau Électrique - Optimisation

Projet Java pour la gestion et l'optimisation d'un réseau électrique composé de générateurs et de maisons.

## Structure du projet

```
src/
├── Main.java                 # Point d'entrée: routeur vers les menus
├── Menu/
│   ├── MenuAutomatique.java  # Menu du mode automatique (optimisation+sauvegarde)
│   ├── MenuConstruction.java # Menu de construction (ajouts, connexions)
│   └── MenuOperation.java    # Menu d'opération (modifications, coûts, affichage)
├── Modele/
│   ├── Generateur.java       # Modèle du générateur électrique
│   ├── Maison.java           # Modèle de la maison consommatrice
│   ├── ReseauElectrique.java # Gestionnaire du réseau complet
│   └── TypeConsommation.java # Enum des types de consommation
└── Algo/
    ├── Optimiseur.java       # Algorithmes d'optimisation du réseau
    └── Sauvegarde.java       # Sauvegarde des solutions
```

## Aperçu du modèle

- **`Maison`**
  - Attributs: `nom`, `typeConsommation` (enum), `consommation` (kW), `generateur` (lien)
  - La consommation est déduite du type
  - Une maison peut être connectée à un seul générateur

- **`TypeConsommation`** (enum)
  - `BASSE=10 kW`, `NORMAL=20 kW`, `FORTE=40 kW`

- **`Generateur`**
  - Attributs: `nom`, `capaciteMax` (kW), `maisonsConnectees` (liste)
  - Métriques: `getChargeActuelle()` = somme des consommations, `calculerTauxUtilisation()` = Lg/Cg

- **`ReseauElectrique`**
  - Stocke `maisons` et `generateurs` dans des `LinkedHashMap` (ordre d'insertion préservé)
  - Paramètre `penalite` (λ) pour pondérer les surcharges (par défaut: 10)
  - Chargement depuis fichier avec validation syntaxique
  - Opérations: ajout/suppression/modification de connexions, calculs de coût, affichage

## API principale

### Construction du réseau

- **`ReseauElectrique(int penalite)`** throws `IllegalArgumentException`
  - Constructeur avec pénalité personnalisée (doit être > 0)

- **`boolean chargerDepuisFichier(String chemin)`** throws `IOException`, `IllegalArgumentException`
  - Charge un réseau depuis un fichier texte
  - Format: `Generateur(nom:capacite)` puis `Connexion(maison,type,generateur)`

- **`void ajouterGenerateur(String nom, int capaciteMax)`** throws `IllegalArgumentException`
  - Ajoute un générateur (capacité > 0)

- **`void ajouterMaison(String nom, String typeConsommationStr)`** throws `IllegalArgumentException`
  - Ajoute une maison avec type ∈ {`BASSE`, `NORMAL`, `FORTE`}

### Gestion des connexions

- **`void ajouterConnexion(String nom1, String nom2)`** throws `IllegalArgumentException`
  - Crée une connexion entre une maison et un générateur
  - Accepte l'ordre indifféremment (M1 G1 ou G1 M1)
  - Vérifie qu'aucune connexion n'existe déjà pour cette maison

- **`void supprimerConnexion(String nom1, String nom2)`** throws `IllegalArgumentException`
  - Supprime une connexion existante
  - Vérifie l'existence de la connexion avant suppression

- **`void modifierConnexion(String nomMaisonOld, String nomGenOld, String nomMaisonNew, String nomGenNew)`** throws `IllegalArgumentException`
  - Déplace une maison d'un générateur à un autre
  - Gère automatiquement la suppression et la création

### Calculs et vérifications

- **`boolean verifierReseau()`**
  - Vérifie que chaque maison est connectée à exactement un générateur

- **`double calculerDispersion()`**
  - Calcule la dispersion des taux d'utilisation

- **`double calculerSurcharge()`**
  - Calcule la surcharge normalisée totale

- **`double calculerCout()`**
  - Calcule le coût total = dispersion + λ × surcharge

- **`void afficherReseau()`**
  - Affiche l'état complet du réseau

## Gestion des exceptions

Le projet utilise exclusivement des **exceptions standard Java**:

- **`IllegalArgumentException`** - Paramètres invalides:
  - Valeurs null ou vides
  - Valeurs négatives ou nulles (penalite, capacité)
  - Éléments inexistants (maison/générateur introuvable)
  - Connexions impossibles (déjà connectée, inexistante)
  - Types de consommation invalides

- **`IOException`** - Opérations fichier:
  - Erreur de lecture lors du chargement
  - Erreur d'écriture lors de la sauvegarde

- **`NumberFormatException`** - Parsing:
  - Format numérique invalide dans les fichiers ou entrées utilisateur

Toutes les méthodes publiques valident leurs paramètres et lancent des exceptions appropriées.

## Formules et métriques

Pour chaque générateur g:
- **Lg**: charge actuelle (somme des consommations)
- **Cg**: capacité maximale
- **ug = Lg/Cg**: taux d'utilisation

**1) Dispersion des taux d'utilisation:**
```
Disp(S) = Σg |ug − ū|
```
où ū est la moyenne des taux d'utilisation.

**2) Surcharge totale (normalisée):**
```
Surcharge(S) = Σg max(0, ug − 1)
```

**3) Coût total:**
```
Cout(S) = Disp(S) + λ × Surcharge(S)
```

## Utilisation

### Mode automatique (avec fichier)
```bash
javac -d bin src/Main.java src/Modele/*.java src/Algo/*.java src/Menu/*.java
java -cp bin Main instance/instance1.txt 10
```
- Charge le réseau depuis le fichier
- Pénalité = 10
- Menu d'optimisation automatique disponible

### Mode interactif (construction manuelle)
```bash
java -cp bin Main
```
- Phase de construction: créer le réseau manuellement
- Phase d'opération: modifier et calculer les coûts

```

## Algorithmes d'optimisation

- **`Optimiseur.optimiserReseau(reseau, k)`** throws `IllegalArgumentException`
  - Optimise le réseau par k tentatives d'amélioration
  - Utilise une stratégie de recherche locale
  - Déplace les maisons pour minimiser le coût total

- **`Sauvegarde.sauvegarderSolution(reseau, nomFichier)`** throws `IOException`, `IllegalArgumentException`
  - Sauvegarde la configuration actuelle dans un fichier
  - Format réutilisable pour rechargement

## Choix de conception

- **LinkedHashMap** pour préserver l'ordre d'insertion des générateurs/maisons
- **Validation stricte** avec exceptions pour toutes les entrées
- **Normalisation** des noms en majuscules pour recherche insensible à la casse
- **Séparation claire** entre modèle (Modele/), algorithmes (Algo/), menus (Menu/) et routeur (Main)
- **Exceptions standard** Java uniquement (pas de classes d'exception personnalisées)
- **Accès O(1)** pour la recherche de maisons et générateurs par nom
