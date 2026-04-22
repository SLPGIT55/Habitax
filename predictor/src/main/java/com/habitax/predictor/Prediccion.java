package com.habitax.predictor;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Prediccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Asocia la prediccion al usuario que la realizo.
    // Coincide con la columna usuario_id de la tabla (NULLABLE).
    @Column(name = "usuario_id")
    private Long usuarioId;

    private String zona;
    private int metros;
    private int habitaciones;
    private int banos;
    private double precio;
    private LocalDateTime fecha;

    // Constructor vacio
    public Prediccion() {
        this.fecha = LocalDateTime.now();
    }

    // Constructor completo con usuarioId
    public Prediccion(Long usuarioId, String zona, int metros, int habitaciones, int banos, double precio) {
        this.usuarioId = usuarioId;
        this.zona = zona;
        this.metros = metros;
        this.habitaciones = habitaciones;
        this.banos = banos;
        this.precio = precio;
        this.fecha = LocalDateTime.now();
    }

    // Getters
    public Long getId() { return id; }
    public Long getUsuarioId() { return usuarioId; }
    public String getZona() { return zona; }
    public int getMetros() { return metros; }
    public int getHabitaciones() { return habitaciones; }
    public int getBanos() { return banos; }
    public double getPrecio() { return precio; }
    public LocalDateTime getFecha() { return fecha; }

    // Setters
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
    public void setZona(String zona) { this.zona = zona; }
    public void setMetros(int metros) { this.metros = metros; }
    public void setHabitaciones(int habitaciones) { this.habitaciones = habitaciones; }
    public void setBanos(int banos) { this.banos = banos; }
    public void setPrecio(double precio) { this.precio = precio; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
}