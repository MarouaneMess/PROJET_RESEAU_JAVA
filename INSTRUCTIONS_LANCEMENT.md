# Instructions de lancement

## Prérequis
- JDK installé et disponible dans le `PATH` (javac / java).
- **JavaFX SDK** installé (pour l'interface graphique).
  - Télécharger depuis : https://openjfx.io/
  - Extraire dans un dossier (ex: `C:\javafx\javafx-sdk-25.0.1\`)
- Dossier `bin` présent (créé automatiquement par les commandes ci-dessous si absent).

**Note** : Remplacez `C:\javafx\javafx-sdk-25.0.1\lib` par le chemin vers votre installation JavaFX.

### Interface graphique (recommandé)
```bat
javac --module-path C:\javafx\javafx-sdk-25.0.1\lib --add-modules javafx.controls,javafx.fxml -d bin -sourcepath src src\Main.java src\Algo\*.java src\Menu\*.java src\Modele\*.java src\GUI\*.java src\GUI\controllers\*.java

java --module-path C:\javafx\javafx-sdk-25.0.1\lib --add-modules javafx.controls,javafx.fxml -cp bin GUI.MainApp 
```
- Lance l'interface graphique JavaFX
- Page d'accueil avec choix du mode (construction manuelle ou chargement depuis fichier)
- Navigation intuitive entre les phases : construction, opération, visualisation
- Toutes les fonctionnalités disponibles via l'interface graphique

### Mode console

#### Mode interactif (construction manuelle)
```bat
javac -d bin src/Modele/*.java src/Algo/*.java src/Menu/*.java src/Main.java
java -cp bin Main
```
- Construis le réseau via les menus (ajout générateurs/maisons, connexions), puis calcule les coûts.

#### Mode automatique (chargement fichier + pénalité)
```bat
javac -d bin src/Modele/*.java src/Algo/*.java src/Menu/*.java src/Main.java
java -cp bin Main instance/instance1.txt 10
```
- Charge le réseau depuis le fichier `instance/instance1.txt`.
- Pénalité `λ = 10` (doit être > 0).
- Affiche le menu d'optimisation automatique (choix 1 pour optimiser, 2 pour sauvegarder).
- Les solutions sauvegardées sont stockées dans le dossier `instancesAmeliorees/`.

### Autres instances disponibles
- `instance/instance1.txt`
- `instance/instance2.txt`
- `instance/instance3.txt`
- `instance/instance4.txt`
- `instance/instance5.txt`
- `instance/instance6.txt`
- `instance/instance7.txt`
- `instance/instance_tres_grande1.txt`

## Notes importantes

### Configuration du chemin JavaFX
Si votre installation JavaFX est dans un autre emplacement, remplacez `C:\javafx\javafx-sdk-25.0.1\lib` dans les commandes ci-dessus par votre chemin. Par exemple :
- `C:\Program Files\JavaFX\javafx-sdk-21.0.1\lib`
- `D:\javafx\lib`
- etc.

### Gestion des erreurs
- Toutes les erreurs d'entrée (noms vides, valeurs négatives, types inconnus) lèvent `IllegalArgumentException`.
- Les erreurs de fichier (lecture/écriture) lèvent `IOException`.
- Les valeurs numériques invalides lèvent `NumberFormatException`.

### Choix du mode
- **Interface graphique** : Expérience utilisateur moderne et intuitive, recommandée pour la plupart des utilisateurs.
- **Mode console** : Utile pour l'automatisation, les scripts, ou si JavaFX n'est pas disponible.
