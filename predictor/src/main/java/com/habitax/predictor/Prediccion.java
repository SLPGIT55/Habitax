package com.habitax.predictor;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Prediccion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String zona;
    private int metros;
    private double precio;
    private LocalDateTime fecha = LocalDateTime.now();

    // Constructor vacío (obligatorio para JPA)
    public Prediccion() {}

    public Prediccion(String zona, int metros, double precio) {
        this.zona = zona;
        this.metros = metros;
        this.precio = precio;
    }

    // Getters para que Thymeleaf pueda leer los datos
    public String getZona() { return zona; }
    public int getMetros() { return metros; }
    public double getPrecio() { return precio; }
    public LocalDateTime getFecha() { return fecha; }
}
