# Réseau Électrique – Projet Java

Modélisation simple d’un réseau électrique en Java avec des maisons, des générateurs et des métriques de coût pour évaluer une configuration.

## Aperçu du modèle

- `Maison`
	- Attributs: `nom`, `typeConsommation` (enum), `consommation` (kW), `generateur` (lien)
	- La consommation est déduite du type.

- `TypeConsommation` (enum)
	- `BASSE=10 kW`, `NORMAL=20 kW`, `FORTE=40 kW`.

- `Generateur`
	- Attributs: `nom`, `capaciteMax` (kW), `maisonsConnectees` (liste)
	- Métriques: `getChargeActuelle()` = somme des consommations des maisons connectées, `calculerTauxUtilisation()` = Lg/Cg.

- `ReseauElectrique`
	- Stocke `maisons` et `generateurs` dans des `HashMap` pour des accès en O(1) moyen.
	- Paramètre `penalite` (λ) pour pondérer les surcharges.
	- Opérations: ajout de générateurs/maisons, connexions, vérification du réseau, calculs de dispersion/surcharge/coût, affichage du réseau.

## API principale (résumé)

- `void ajouterGenerateur(String nom, int capaciteMax)`
- `void ajouterMaison(String nom, String typeConsommationStr)`
	- `typeConsommationStr` ∈ {`BASSE`, `NORMAL`, `FORTE`} (insensible à la casse).
- `void ajouterConnexion(String nom1, String nom2)`
	- Accepte l’ordre indifféremment (Maison, Générateur) ou (Générateur, Maison).
	- Remarque: cette méthode crée le lien bidirectionnel en ajoutant la maison au générateur (et met à jour la maison). Pour déplacer une maison d’un générateur A vers B, utilisez plutôt `modifierConnexion` (voir ci-dessous) afin de nettoyer l’ancienne connexion.
 - `void ajouterConnexion(String nom1, String nom2)`
	- Accepte l’ordre indifféremment (Maison, Générateur) ou (Générateur, Maison).
	- Remarque: cette méthode crée le lien bidirectionnel en ajoutant la maison au générateur (et met à jour la maison). Pour déplacer une maison d’un générateur A vers B, utilisez plutôt `modifierConnexion` (voir ci-dessous) afin de nettoyer l’ancienne connexion.

- `void supprimerConnexion(String nom1, String nom2)`
	- Supprime une connexion existante entre une maison et un générateur.
	- Accepte l'ordre indifféremment (`M1 G1` ou `G1 M1`) et est insensible à la casse (les clés sont normalisées en majuscules en interne).
	- Vérifie que la maison et le générateur existent; imprime un message d'erreur si l'un des deux est absent.
	- Vérifie que la connexion existe réellement avant suppression; imprime une erreur si aucune connexion n'est trouvée.
	- Retire la maison de la liste `maisonsConnectees` du générateur et met à jour le lien côté `Maison`.
	- Exemple d'utilisation dans `Main.java`: `reseau.supprimerConnexion("M1", "G1");`
- `void modifierConnexion(String nomMaisonOld, String nomGenOld, String nomMaisonNew, String nomGenNew)`
	- Vérifie que la connexion (maison, ancien générateur) existe, la retire puis crée la nouvelle connexion.
- `boolean verifierReseau()`
	- Vérifie que chaque maison est connectée à un générateur (exactement un côté Maison → Générateur).
- `double calculerDispersion()`
- `double calculerSurcharge()`
- `double calculerCout()`
- `void afficherReseau()`

## Formules et métriques

Soit, pour chaque générateur g:
- Lg: charge actuelle (somme des consommations des maisons connectées à g)
- Cg: capacité maximale du générateur g
- ug = Lg/Cg: taux d’utilisation

1) Dispersion des taux d’utilisation

	 Disp(S) = Σg |ug − ū|, où ū est la moyenne des `ug` sur tous les générateurs.

2) Surcharge totale (normalisée)

	 Surcharge(S) = Σg max(0, ug − 1)

3) Coût total

	 Cout(S) = Disp(S) + λ × Surcharge(S)

Ces formules sont implémentées dans `ReseauElectrique` via `calculerDispersion`, `calculerSurcharge` et `calculerCout`.

## Choix de conception: Map vs HashMap

- On déclare les champs en type `Map<K,V>` mais on instancie en `HashMap<>`.
- `HashMap` offre des opérations `get/put/containsKey` en O(1) moyen, suffisant ici car l’ordre d’itération n’est pas requis.
- On peut remplacer plus tard par `LinkedHashMap` (ordre d’insertion) ou `TreeMap` (clés triées) sans changer les signatures publiques.


```powershell
# Compilation des sources du modèle + Main
javac -d bin src/Model/*.java src/Main.java

# Exécution
java -cp bin Main
```

## Idées d’amélioration

- Valider les entrées ( noms uniques non vides).
- Empêcher une maison d’être rattachée à plusieurs générateurs simultanément côté structure (en forçant le retrait automatique de l’ancien lien lors d’une nouvelle connexion).
