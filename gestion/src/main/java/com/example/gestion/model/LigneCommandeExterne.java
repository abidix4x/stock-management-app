package com.example.gestion.model;

public class LigneCommandeExterne {
    private int id;
    private CommandeExterne commande;
    private Produit produit;
    private int quantite;
    private double prixUnitaire;

    public LigneCommandeExterne() {
    }

    public LigneCommandeExterne(int id, CommandeExterne commande, Produit produit, int quantite, double prixUnitaire) {
        this.id = id;
        this.commande = commande;
        this.produit = produit;
        this.quantite = quantite;
        this.prixUnitaire = prixUnitaire;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public CommandeExterne getCommande() {
        return commande;
    }

    public void setCommande(CommandeExterne commande) {
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

    public double getPrixUnitaire() {
        return prixUnitaire;
    }

    public void setPrixUnitaire(double prixUnitaire) {
        this.prixUnitaire = prixUnitaire;
    }

    public double getTotal() {
        return quantite * prixUnitaire;
    }
}
