package Model;
public class Maison {
    private String nom;
    private TypeConsommation typeConsommation;
    private int consommation;
    private Generateur generateur;

    public Maison(String nom, TypeConsommation typeConsommation) {
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
