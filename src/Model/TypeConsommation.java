package Model;
public enum TypeConsommation {
    BASSE(10),
    NORMAL(20),
    FORTE(40);

    private final int valeur;

    private TypeConsommation(int valeur) {
        this.valeur = valeur;
    }

    public int getValeur() {
        return valeur;
    }
}

