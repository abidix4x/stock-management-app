package com.example.gestion.model;

import java.time.LocalDate;

public class Mouvement {
    private int id;
    private Produit produit;
    private String type; // entrée ou sortie
    private int quantite;
    private LocalDate dateMouvement;
    private String referenceCommande;

    public Mouvement() {
    }

    public Mouvement(int id, Produit produit, String type, int quantite, LocalDate dateMouvement, String referenceCommande) {
        this.id = id;
        this.produit = produit;
        this.type = type;
        this.quantite = quantite;
        this.dateMouvement = dateMouvement;
        this.referenceCommande = referenceCommande;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }

    public LocalDate getDateMouvement() {
        return dateMouvement;
    }

    public void setDateMouvement(LocalDate dateMouvement) {
        this.dateMouvement = dateMouvement;
    }

    public String getReferenceCommande() {
        return referenceCommande;
    }

    public void setReferenceCommande(String referenceCommande) {
        this.referenceCommande = referenceCommande;
    }
}
