package com.example.gestion.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CommandeExterne {
    private int id;
    private String reference;
    private LocalDate dateCommande;
    private Fournisseur fournisseur;
    private String statut;
    private List<LigneCommandeExterne> lignes;

    public CommandeExterne() {
        this.lignes = new ArrayList<>();
    }

    public CommandeExterne(int id, String reference, LocalDate dateCommande, Fournisseur fournisseur, String statut) {
        this.id = id;
        this.reference = reference;
        this.dateCommande = dateCommande;
        this.fournisseur = fournisseur;
        this.statut = statut;
        this.lignes = new ArrayList<>();
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

    public LocalDate getDateCommande() {
        return dateCommande;
    }

    public void setDateCommande(LocalDate dateCommande) {
        this.dateCommande = dateCommande;
    }

    public Fournisseur getFournisseur() {
        return fournisseur;
    }

    public void setFournisseur(Fournisseur fournisseur) {
        this.fournisseur = fournisseur;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public List<LigneCommandeExterne> getLignes() {
        return lignes;
    }

    public void setLignes(List<LigneCommandeExterne> lignes) {
        this.lignes = lignes;
    }

    public void ajouterLigne(LigneCommandeExterne ligne) {
        this.lignes.add(ligne);
    }

    public double getTotal() {
        return lignes.stream()
                .mapToDouble(ligne -> ligne.getQuantite() * ligne.getPrixUnitaire())
                .sum();
    }
}
