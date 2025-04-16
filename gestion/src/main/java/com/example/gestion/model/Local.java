package com.example.gestion.model;

public class Local {
    private int id;
    private String code;
    private String designation;
    private String type;

    public Local() {
    }

    public Local(int id, String code, String designation, String type) {
        this.id = id;
        this.code = code;
        this.designation = designation;
        this.type = type;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
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

    @Override
    public String toString() {
        return code + " - " + designation;
    }
}
