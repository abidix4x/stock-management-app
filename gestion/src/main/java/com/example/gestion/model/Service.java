package com.example.gestion.model;

public class Service {
    private int id;
    private String nom;
    private String responsable;
    private String telephone;
    private String email;

    public Service() {
    }

    public Service(int id, String nom, String responsable, String telephone, String email) {
        this.id = id;
        this.nom = nom;
        this.responsable = responsable;
        this.telephone = telephone;
        this.email = email;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getResponsable() {
        return responsable;
    }

    public void setResponsable(String responsable) {
        this.responsable = responsable;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return nom;
    }
}
