# Instructions de lancement

## Prérequis
- JDK installé et disponible dans le `PATH` (javac / java).
- Dossier `bin` présent (créé automatiquement par les commandes ci-dessous si absent).

## Compilation (Windows)
```bat
javac -d bin src/Modele/*.java src/Algo/*.java src/Menu/*.java src/Main.java
```

## Exécution

### Mode interactif (construction manuelle)
```bash
java -cp bin Main
```
- Construis le réseau via les menus (ajout générateurs/maisons, connexions), puis calcule les coûts.

### Mode automatique (chargement fichier + pénalité)
```bash
java -cp bin Main instance/instance1.txt 10
```
- Charge le réseau depuis le fichier `instance/instance1.txt`.
- Pénalité `λ = 10` (doit être > 0).
- Affiche le menu d’optimisation automatique (choix 1 pour optimiser, 2 pour sauvegarder).- Les solutions sauvegardées sont stockées dans le dossier `instancesAmeliorees/`.
### Autres instances disponibles
- `instance/instance1.txt`
- `instance/instance2.txt`
- `instance/instance3.txt`
- `instance/instance4.txt`
- `instance/instance5.txt`
- `instance/instance6.txt`
- `instance/instance7.txt`
- `instance/instance_tres_grande1.txt`

## Notes
- Toutes les erreurs d’entrée (noms vides, valeurs négatives, types inconnus) lèvent `IllegalArgumentException`.
- Les erreurs de fichier (lecture/écriture) lèvent `IOException`.
- Les valeurs numériques invalides lèvent `NumberFormatException`.
