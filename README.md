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

## Construire le projet (sans outil de build)

Le projet ne contient pas de classe `main` par défaut. Vous pouvez compiler les classes du modèle ainsi:

```powershell
# Depuis la racine du projet
New-Item -ItemType Directory -Force bin | Out-Null
javac -d bin src/Model/*.java
```

Pour exécuter un exemple, créez une classe `Main.java` (voir ci-dessous), compilez-la et lancez-la avec le classpath pointant vers `bin`.

## Exemple minimal d’utilisation

Créez un fichier `src/Main.java` (sans package) avec le contenu suivant:

```java
public class Main {
		public static void main(String[] args) {
				ReseauElectrique reseau = new ReseauElectrique(10); // λ = 10

				reseau.ajouterGenerateur("G1", 60);
				reseau.ajouterGenerateur("G2", 50);

				reseau.ajouterMaison("M1", "BASSE");
				reseau.ajouterMaison("M2", "NORMAL");
				reseau.ajouterMaison("M3", "FORTE");

				reseau.ajouterConnexion("M1", "G1");
				reseau.ajouterConnexion("G2", "M2"); // ordre inversé accepté
				reseau.ajouterConnexion("M3", "G1");

				reseau.afficherReseau();
				reseau.verifierReseau();
				reseau.calculerCout();

				// Pour déplacer M3 de G1 vers G2, utiliser modifierConnexion:
				reseau.modifierConnexion("M3", "G1", "M3", "G2");
				reseau.afficherReseau();
				reseau.calculerCout();
		}
}
```

Compiler et exécuter sous Windows PowerShell:

```powershell
# Compilation des sources du modèle + Main
New-Item -ItemType Directory -Force bin | Out-Null
javac -d bin src/Model/*.java src/Main.java

# Exécution
java -cp bin Main
```

## Bonnes pratiques et garde-fous

- Évitez `capaciteMax = 0` pour un générateur (division par zéro dans le taux d’utilisation).
- Pour changer le générateur d’une maison, préférez `modifierConnexion` (qui retire l’ancienne connexion) plutôt que de ré-appeler `ajouterConnexion` directement, afin d’éviter de laisser la maison listée chez l’ancien générateur.
- Assurez-vous que les `hashCode/equals` ne sont pas surchargés sur `Maison` et `Generateur` si vous les utilisez comme clés dans des collections basées sur l’égalité (ici, les clés des maps sont des `String`, donc OK).

## Idées d’amélioration

- Valider les entrées (capacités > 0, noms uniques non vides).
- Empêcher une maison d’être rattachée à plusieurs générateurs simultanément côté structure (en forçant le retrait automatique de l’ancien lien lors d’une nouvelle connexion).
- Ajout d’une classe `Main` officielle et de tests unitaires.
- Passer à Maven/Gradle pour faciliter build et tests.
