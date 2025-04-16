package com.example.gestion.model;

import java.time.LocalDate;

public class Produit {
    private int id;
    private String reference;
    private String designation;
    private String type; // consommable ou durable
    private String categorie;
    private int quantite;
    private int stockMinimal;
    private LocalDate datePeremption;
    private boolean critique;

    public Produit() {
    }

    public Produit(int id, String reference, String designation, String type, String categorie,
                   int quantite, int stockMinimal, LocalDate datePeremption, boolean critique) {
        this.id = id;
        this.reference = reference;
        this.designation = designation;
        this.type = type;
        this.categorie = categorie;
        this.quantite = quantite;
        this.stockMinimal = stockMinimal;
        this.datePeremption = datePeremption;
        this.critique = critique;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCategorie() {
        return categorie;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }

    public int getStockMinimal() {
        return stockMinimal;
    }

    public void setStockMinimal(int stockMinimal) {
        this.stockMinimal = stockMinimal;
    }

    public LocalDate getDatePeremption() {
        return datePeremption;
    }

    public void setDatePeremption(LocalDate datePeremption) {
        this.datePeremption = datePeremption;
    }

    public boolean isCritique() {
        return critique;
    }

    public void setCritique(boolean critique) {
        this.critique = critique;
    }

    @Override
    public String toString() {
        return designation;
    }
}
