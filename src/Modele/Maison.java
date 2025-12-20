package Modele;
public class Maison {
    private String nom;
    private TypeConsommation typeConsommation;
    private int consommation;
    private Generateur generateur;

    public Maison(String nom, TypeConsommation typeConsommation) {
        if (nom == null || nom.isEmpty()) {
            throw new IllegalArgumentException("Le nom de la maison ne peut pas être vide.");
        }
        if (typeConsommation == null) {
            throw new IllegalArgumentException("Le type de consommation ne peut pas être null.");
        }
        this.nom = nom;
        this.typeConsommation = typeConsommation;
        this.consommation = typeConsommation.getValeur();
        this.generateur = null;
    }

    public String getNom() {
        return nom;
    }

    public int getConsommation() {
        return consommation;
    }

    public TypeConsommation getTypeConsommation() {
        return typeConsommation;
    }

    public Generateur getGenerateur() {
        return generateur;
    }

    public void setGenerateur(Generateur generateur) {
        this.generateur = generateur;
    }

    @Override
    public String toString() {
        return nom + " (" + typeConsommation.name() + ", " + consommation + "kW)";
    }
}
