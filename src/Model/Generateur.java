package Model;
import java.util.ArrayList;
import java.util.List;


public class Generateur {
    private String nom;
    private int capaciteMax;
    /*  on a pas utiliser un set car on gere ca dans le reseau electrique 
     quand on cree une connexion on verifie si la maison 
     est deja connectee a un generateur ou non (meme si le meme)'
    */
    private List<Maison> maisonsConnectees;

    public Generateur(String nom, int capaciteMax) {
        this.nom = nom;
        this.capaciteMax = capaciteMax;
        this.maisonsConnectees = new ArrayList<>();
    }

    public String getNom() {
        return nom;
    }

    public int getCapaciteMax() {
        return capaciteMax;
    }

    public void setCapaciteMax(int capaciteMax) {
        this.capaciteMax = capaciteMax;
    }

    public int getChargeActuelle() {
        int charge = 0;
        for (Maison maison : maisonsConnectees) {
            charge += maison.getConsommation();
        }
        return charge;
    }

    public double calculerTauxUtilisation() {
        if (capaciteMax == 0) {
            return 0.0;
        }
        return (double) getChargeActuelle() / capaciteMax;
    }

    public void ajouterMaison(Maison maison) {
        if (!maisonsConnectees.contains(maison)) {
            maisonsConnectees.add(maison);
            maison.setGenerateur(this);// lien bi-directionnel car on met a jour le generateur de la maison 
        }
    }

    public void retirerMaison(Maison maison) {
        maisonsConnectees.remove(maison);
        maison.setGenerateur(null);
    }


    public List<Maison> getMaisonsConnectees() {
        return maisonsConnectees;
    }

    @Override
    public String toString() {
        return nom + " (Capacité: " + capaciteMax + "kW, Charge: " + getChargeActuelle() + "kW)";
    }
}
