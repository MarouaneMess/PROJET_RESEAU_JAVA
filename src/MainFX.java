import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.util.stream.Collectors;
import Model.*;
import Algo.*;
import java.io.File;
import java.util.*;

public class MainFX extends Application {

    // Classes de données pour l'interface
    public static class GeneratorData {
        private final String nom;
        private final int capacite;
        public GeneratorData(String nom, int capacite) { this.nom = nom; this.capacite = capacite; }
        public String getNom() { return nom; }
        public int getCapacite() { return capacite; }
    }

    public static class HouseData {
        private final String nom;
        private final String generateur;
        public HouseData(String nom, String generateur) { this.nom = nom; this.generateur = generateur; }
        public String getNom() { return nom; }
        public String getGenerateur() { return generateur; }
    }

    private ReseauElectrique reseau = new ReseauElectrique(10);
    private TableView<GeneratorData> genTable = new TableView<>();
    private TableView<HouseData> houseTable = new TableView<>();
    private ObservableList<GeneratorData> genData = FXCollections.observableArrayList();
    private ObservableList<HouseData> houseData = FXCollections.observableArrayList();
    private Canvas graphCanvas = new Canvas(450, 400);
    private TextArea output = new TextArea();
    private TextField kField = new TextField("500");

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("SmartGrid Manager");
        
        // Taille standard stable
        double initialWidth = 950;
        double initialHeight = 650;

        BorderPane mainLayout = new BorderPane();
        mainLayout.setStyle("-fx-background-color: #f4f4f4;");

        // Barre de titre
        HBox topBar = new HBox();
        topBar.setPadding(new Insets(10));
        topBar.setStyle("-fx-background-color: #2c3e50;");
        Label title = new Label("SOCIÉTÉ ÉLECTRIQUE - DASHBOARD");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("System", FontWeight.BOLD, 14));
        topBar.getChildren().add(title);
        mainLayout.setTop(topBar);

        setupTables();
        VBox left = new VBox(10, new Label("DONNÉES"), genTable, houseTable);
        left.setPadding(new Insets(10));
        left.setPrefWidth(300);

        StackPane graphBox = new StackPane(graphCanvas);
        graphBox.setStyle("-fx-background-color: white; -fx-border-color: #ccc;");
        VBox center = new VBox(10, new Label("VUE DU RÉSEAU"), graphBox);
        center.setPadding(new Insets(10));
        
        mainLayout.setCenter(new HBox(left, center));

        VBox menu = new VBox(8);
        menu.setPadding(new Insets(10));
        menu.setPrefWidth(180);
        menu.setStyle("-fx-background-color: #eee; -fx-border-color: #ccc;");

        Button btnLoad = createBtn("Charger", "#34495e", "white");
        btnLoad.setOnAction(e -> chargerNetwork());
        Button btnSave = createBtn("Sauvegarder", "#2980b9", "white");
        btnSave.setOnAction(e -> sauvegarderSolution());
        
        Button btnAddG = createBtn("Ajouter Gen", "white", "#333");
        btnAddG.setOnAction(e -> dialogAjouterGenerateur());
        Button btnAddM = createBtn("Ajouter Maison", "white", "#333");
        btnAddM.setOnAction(e -> dialogAjouterMaison());
        
        Button btnConn = createBtn("Connecter", "white", "#333");
        btnConn.setOnAction(e -> dialogAjouterConnexion());
        
        // NOUVEAUTÉ : Bouton pour supprimer une connexion
        Button btnDelConn = createBtn("Déconnecter", "white", "#333");
        btnDelConn.setOnAction(e -> supprimerConnexionSelectionnee());
        
        kField.setPromptText("k itérations");
        Button btnOpti = createBtn("OPTIMISER", "#27ae60", "white");
        btnOpti.setOnAction(e -> lancerOptimisation());
        
        Button btnDel = createBtn("Supprimer", "#e74c3c", "white");
        btnDel.setOnAction(e -> supprimerElementSelectionne());

        menu.getChildren().addAll(
            new Label("ACTIONS"), btnLoad, btnSave, new Separator(), 
            btnAddG, btnAddM, new Separator(),
            btnConn, btnDelConn, new Separator(), 
            new Label("PARAMÈTRES (k)"), kField, btnOpti, btnDel
        );
        mainLayout.setRight(menu);

        output.setPrefHeight(100);
        mainLayout.setBottom(output);

        Scene scene = new Scene(mainLayout, initialWidth, initialHeight);
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.show();

        actualiserTout();
    }

    private void dessinerGraphe() {
        GraphicsContext gc = graphCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, graphCanvas.getWidth(), graphCanvas.getHeight());
        double cx = graphCanvas.getWidth() / 2;
        double cy = graphCanvas.getHeight() / 2;

        List<Generateur> gens = new ArrayList<>(reseau.getGenerateurs());
        List<Maison> houses = new ArrayList<>(reseau.getMaisons());
        Map<String, double[]> gPos = new HashMap<>();

        for (int i = 0; i < gens.size(); i++) {
            Generateur g = gens.get(i);
            double a = 2 * Math.PI * i / Math.max(1, gens.size());
            double x = cx + 80 * Math.cos(a);
            double y = cy + 80 * Math.sin(a);
            gPos.put(g.getNom(), new double[]{x, y});
            gc.setFill(Color.web("#2980b9"));
            gc.fillOval(x-15, y-15, 30, 30);
            gc.setFill(Color.BLACK);
            gc.setFont(Font.font("System", FontWeight.BOLD, 10));
            gc.fillText(g.getNom() + " (" + g.getChargeActuelle() + "/" + g.getCapaciteMax() + ")", x-25, y-20);
            gc.setFill(Color.WHITE);
            gc.fillText("G", x-5, y+5);
        }

        gc.setStroke(Color.web("#2ecc71", 0.7));
        gc.setLineWidth(2);
        for (int i = 0; i < houses.size(); i++) {
            Maison m = houses.get(i);
            double a = 2 * Math.PI * i / Math.max(1, houses.size());
            double hx = cx + 160 * Math.cos(a);
            double hy = cy + 160 * Math.sin(a);
            if (m.getGenerateur() != null) {
                double[] p = gPos.get(m.getGenerateur().getNom());
                if (p != null) gc.strokeLine(hx, hy, p[0], p[1]);
            }
        }

        for (int i = 0; i < houses.size(); i++) {
            Maison m = houses.get(i);
            double a = 2 * Math.PI * i / Math.max(1, houses.size());
            double hx = cx + 160 * Math.cos(a);
            double hy = cy + 160 * Math.sin(a);
            gc.setFill(Color.web("#34495e"));
            gc.fillRect(hx-6, hy-6, 12, 12);
            gc.setFill(Color.BLACK);
            gc.setFont(Font.font("System", FontWeight.NORMAL, 9));
            gc.fillText(m.getNom() + " (" + m.getConsommation() + ")", hx-12, hy+18);
        }
    }

    private void actualiserTout() {
        genData.clear();
        for (Generateur g : reseau.getGenerateurs()) genData.add(new GeneratorData(g.getNom(), g.getCapaciteMax()));
        houseData.clear();
        for (Maison m : reseau.getMaisons()) {
            String g = (m.getGenerateur() != null) ? m.getGenerateur().getNom() : "LIBRE";
            houseData.add(new HouseData(m.getNom(), g));
        }
        dessinerGraphe();
    }

    private Button createBtn(String t, String b, String f) {
        Button btn = new Button(t);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle("-fx-background-color: " + b + "; -fx-text-fill: " + f + "; -fx-cursor: hand; -fx-border-color: #ccc;");
        return btn;
    }

    private void chargerNetwork() {
        FileChooser fc = new FileChooser();
        File f = fc.showOpenDialog(null);
        if (f != null && reseau.chargerDepuisFichier(f.getAbsolutePath())) actualiserTout();
    }

    private void sauvegarderSolution() {
        FileChooser fc = new FileChooser();
        File f = fc.showSaveDialog(null);
        if (f != null) {
            Sauvegarde.sauvegarderSolution(reseau, f.getAbsolutePath());
            output.appendText("[OK] Enregistré.\n");
        }
    }

    private void dialogAjouterGenerateur() {
        TextInputDialog d = new TextInputDialog("G1,100");
        d.showAndWait().ifPresent(v -> {
            try {
                String[] p = v.split(",");
                reseau.ajouterGenerateur(p[0].trim(), Integer.parseInt(p[1].trim()));
                actualiserTout();
            } catch (Exception e) {}
        });
    }

    private void dialogAjouterMaison() {
        TextInputDialog d = new TextInputDialog("M1,NORMAL");
        d.showAndWait().ifPresent(v -> {
            try {
                String[] p = v.split(",");
                reseau.ajouterMaison(p[0].trim(), p[1].trim().toUpperCase());
                actualiserTout();
            } catch (Exception e) {}
        });
    }

    private void dialogAjouterConnexion() {
        if(reseau.getMaisons().isEmpty() || reseau.getGenerateurs().isEmpty()) return;
        List<String> mList = reseau.getMaisons().stream().map(Maison::getNom).collect(Collectors.toList());
        ChoiceDialog<String> d = new ChoiceDialog<>(mList.get(0), mList);
        d.showAndWait().ifPresent(m -> {
            List<String> gList = reseau.getGenerateurs().stream().map(Generateur::getNom).collect(Collectors.toList());
            ChoiceDialog<String> d2 = new ChoiceDialog<>(gList.get(0), gList);
            d2.showAndWait().ifPresent(g -> {
                reseau.ajouterConnexion(m, g);
                actualiserTout();
            });
        });
    }

    // LOGIQUE DE DÉCONNEXION
    private void supprimerConnexionSelectionnee() {
        HouseData selected = houseTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            // On cherche la maison dans le modèle
            for (Maison m : reseau.getMaisons()) {
                if (m.getNom().equals(selected.getNom())) {
                    if (m.getGenerateur() != null) {
                        m.getGenerateur().retirerMaison(m);
                        m.setGenerateur(null);
                        output.appendText("Déconnexion : " + m.getNom() + "\n");
                        actualiserTout();
                    }
                    break;
                }
            }
        }
    }

    private void supprimerElementSelectionne() {
        HouseData h = houseTable.getSelectionModel().getSelectedItem();
        GeneratorData g = genTable.getSelectionModel().getSelectedItem();
        if (h != null) reseau.supprimerMaison(h.getNom());
        else if (g != null) reseau.supprimerGenerateur(g.getNom());
        actualiserTout();
    }

    private void lancerOptimisation() {
        if (!reseau.verifierReseau()) {
            output.appendText("Reliez toutes les maisons d'abord.\n");
            return;
        }
        int k;
        try {
            k = Integer.parseInt(kField.getText().trim());
        } catch (Exception e) { k = 500; }

        double coutInitial = reseau.calculerCoutSilencieux();
        Optimiseur.optimiserReseau(reseau, k);
        double coutFinal = reseau.calculerCoutSilencieux();
        
        output.appendText("\nOptimisation (k=" + k + ") terminée.");
        output.appendText("\nCoût Final : " + String.format("%.4f", coutFinal));
        output.appendText("\n-----------------------------\n");
        actualiserTout();
    }

    private void setupTables() {
        TableColumn<GeneratorData, String> c1 = new TableColumn<>("Gen");
        c1.setCellValueFactory(new PropertyValueFactory<>("nom"));
        genTable.getColumns().clear();
        genTable.getColumns().add(c1);
        TableColumn<HouseData, String> h1 = new TableColumn<>("Maison");
        h1.setCellValueFactory(new PropertyValueFactory<>("nom"));
        houseTable.getColumns().clear();
        houseTable.getColumns().add(h1);
        genTable.setItems(genData);
        houseTable.setItems(houseData);
    }

    public static void main(String[] args) { launch(args); }
}