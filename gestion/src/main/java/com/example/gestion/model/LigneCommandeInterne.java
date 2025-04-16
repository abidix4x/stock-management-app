package com.example.gestion.model;

public class LigneCommandeInterne {
    private int id;
    private CommandeInterne commande;
    private Produit produit;
    private int quantite;

    public LigneCommandeInterne() {
    }

    public LigneCommandeInterne(int id, CommandeInterne commande, Produit produit, int quantite) {
        this.id = id;
        this.commande = commande;
        this.produit = produit;
        this.quantite = quantite;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public CommandeInterne getCommande() {
        return commande;
    }

    public void setCommande(CommandeInterne commande) {
        this.commande = commande;
    }

    public Produit getProduit() {
        return produit;
    }

    public void setProduit(Produit produit) {
        this.produit = produit;
    }

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }
}
