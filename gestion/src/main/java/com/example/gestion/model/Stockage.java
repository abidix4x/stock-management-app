package com.example.gestion.model;

public class Stockage {
    private int id;
    private Produit produit;
    private Local local;
    private int quantite;

    public Stockage() {
    }

    public Stockage(int id, Produit produit, Local local, int quantite) {
        this.id = id;
        this.produit = produit;
        this.local = local;
        this.quantite = quantite;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Produit getProduit() {
        return produit;
    }

    public void setProduit(Produit produit) {
        this.produit = produit;
    }

    public Local getLocal() {
        return local;
    }

    public void setLocal(Local local) {
        this.local = local;
    }

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }
}
