package com.habitax.predictor.dto;

import java.io.Serializable;

public class PrediccionDTO implements Serializable {

    private String zona;
    private int metros;
    private double precio;

    // Constructor vacío
    public PrediccionDTO() {
    }

    // Constructor lleno para mapear los datos rápidamente
    public PrediccionDTO(String zona, int metros, double precio) {
        this.zona = zona;
        this.metros = metros;
        this.precio = precio;
    }

    // Getters y Setters necesarios para que Thymeleaf pueda leer los campos
    public String getZona() {
        return zona;
    }

    public void setZona(String zona) {
        this.zona = zona;
    }

    public int getMetros() {
        return metros;
    }

    public void setMetros(int metros) {
        this.metros = metros;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }
}