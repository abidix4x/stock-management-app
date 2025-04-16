package com.example.gestion.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CommandeInterne {
    private int id;
    private String reference;
    private LocalDate dateCommande;
    private Service service;
    private String statut;
    private List<LigneCommandeInterne> lignes;

    public CommandeInterne() {
        this.lignes = new ArrayList<>();
    }

    public CommandeInterne(int id, String reference, LocalDate dateCommande, Service service, String statut) {
        this.id = id;
        this.reference = reference;
        this.dateCommande = dateCommande;
        this.service = service;
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

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public List<LigneCommandeInterne> getLignes() {
        return lignes;
    }

    public void setLignes(List<LigneCommandeInterne> lignes) {
        this.lignes = lignes;
    }

    public void ajouterLigne(LigneCommandeInterne ligne) {
        this.lignes.add(ligne);
    }
}
