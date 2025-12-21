# Projet Réseau Électrique - Optimisation

Projet Java pour la gestion et l'optimisation d'un réseau électrique composé de générateurs et de maisons.

## Structure du projet

```
src/
├── Main.java                 # Point d'entrée console: routeur vers les menus
├── Menu/
│   ├── MenuAutomatique.java  # Menu du mode automatique (optimisation+sauvegarde)
│   ├── MenuConstruction.java # Menu de construction (ajouts, connexions)
│   └── MenuOperation.java    # Menu d'opération (modifications, coûts, affichage)
├── Modele/
│   ├── Generateur.java       # Modèle du générateur électrique
│   ├── Maison.java           # Modèle de la maison consommatrice
│   ├── ReseauElectrique.java # Gestionnaire du réseau complet
│   └── TypeConsommation.java # Enum des types de consommation
├── Algo/
│   │── Algo.md
│   ├── Optimiseur.java       # Algorithmes d'optimisation du réseau
│   └── Sauvegarde.java       # Sauvegarde des solutions
└── GUI/
    ├── MainApp.java          # Point d'entrée de l'interface graphique (JavaFX)
    ├── controllers/
    │   ├── AccueilController.java      # Contrôleur de la page d'accueil
    │   ├── ConstructionController.java # Contrôleur de la phase de construction
    │   ├── OperationController.java    # Contrôleur de la phase d'opération
    │   └── VisualisationController.java # Contrôleur de la visualisation du réseau
    └── fxml/
        ├── accueil.fxml       # Interface de la page d'accueil
        ├── construction.fxml  # Interface de construction du réseau
        ├── operation.fxml     # Interface d'opération et modification
        └── visualisation.fxml # Interface de visualisation du réseau
        
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

Le projet propose **deux modes d'utilisation** : une interface graphique (JavaFX) et une interface console.

### Interface graphique (recommandé)

L'application dispose d'une interface graphique moderne développée avec JavaFX offrant :
- **Page d'accueil** : sélection du mode (construction manuelle ou chargement depuis fichier)
- **Phase de construction** : ajout interactif de générateurs et maisons, création de connexions
- **Phase d'opération** : modification des connexions, calcul des métriques, optimisation
- **Visualisation** : affichage graphique du réseau avec ses connexions

#### Compilation et lancement de l'interface graphique
```bash
# Compilation (incluant JavaFX)
javac -d bin --module-path <chemin-javafx>/lib --add-modules javafx.controls,javafx.fxml src/GUI/*.java src/GUI/controllers/*.java src/Modele/*.java src/Algo/*.java

# Lancement
java -cp bin --module-path <chemin-javafx>/lib --add-modules javafx.controls,javafx.fxml GUI.MainApp
```

**Note** : Remplacez `<chemin-javafx>` par le chemin vers votre installation JavaFX (ou utilisez un gestionnaire de dépendances comme Maven/Gradle).

### Mode console

#### Mode automatique (avec fichier)
```bash
javac -d bin src/Main.java src/Modele/*.java src/Algo/*.java src/Menu/*.java
java -cp bin Main instance/instance1.txt 10
```
- Charge le réseau depuis le fichier
- Pénalité = 10
- Menu d'optimisation automatique disponible

#### Mode interactif (construction manuelle)
```bash
java -cp bin Main
```
- Phase de construction: créer le réseau manuellement
- Phase d'opération: modifier et calculer les coûts

## Interface graphique

L'application dispose d'une **interface graphique JavaFX** offrant une expérience utilisateur intuitive pour gérer le réseau électrique.

### Fonctionnalités de l'interface graphique

- **Page d'accueil** (`AccueilController`)
  - Choix entre construction manuelle ou chargement depuis fichier
  - Configuration de la pénalité (λ)
  - Navigation vers les différentes phases

- **Phase de construction** (`ConstructionController`)
  - Ajout interactif de générateurs (nom et capacité)
  - Ajout de maisons avec sélection du type de consommation
  - Création de connexions entre maisons et générateurs
  - Validation en temps réel des entrées

- **Phase d'opération** (`OperationController`)
  - Modification des connexions existantes
  - Calcul et affichage des métriques (dispersion, surcharge, coût)
  - Optimisation du réseau avec paramétrage du nombre d'itérations
  - Sauvegarde de la configuration actuelle

- **Visualisation** (`VisualisationController`)
  - Affichage graphique du réseau
  - Représentation visuelle des connexions
  - Informations détaillées sur chaque générateur et maison

### Architecture de l'interface graphique

L'interface suit une architecture **MVC (Model-View-Controller)** :
- **Modèle** : classes dans `Modele/` (partagées avec le mode console)
- **Vue** : fichiers FXML dans `GUI/fxml/`
- **Contrôleur** : classes dans `GUI/controllers/`

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
- **Séparation claire** entre modèle (Modele/), algorithmes (Algo/), menus (Menu/), interface graphique (GUI/) et routeur (Main)
- **Architecture MVC** pour l'interface graphique : séparation entre vues (FXML), contrôleurs et modèle
- **Double interface** : console et graphique partagent le même modèle métier
- **Exceptions standard** Java uniquement (pas de classes d'exception personnalisées)
- **Accès O(1)** pour la recherche de maisons et générateurs par nom
