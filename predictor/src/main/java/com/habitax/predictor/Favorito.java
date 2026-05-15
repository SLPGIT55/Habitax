package com.habitax.predictor;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Favorito {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long usuarioId;
    private String nombrePersonalizado;
    private String zona;
    private int metros;
    private double ultimoPrecio;
    private int habitaciones;
    private int banos;
    private String barrio;

    // Constructor vacío requerido por JPA
    public Favorito() {}

    // Constructor útil para crear favoritos rápido
    public Favorito(Long usuarioId, String nombrePersonalizado, String zona, int metros, double ultimoPrecio, int habitaciones, int banos, String barrio) {
        this.usuarioId = usuarioId;
        this.nombrePersonalizado = nombrePersonalizado;
        this.zona = zona;
        this.metros = metros;
        this.ultimoPrecio = ultimoPrecio;
        this.habitaciones = habitaciones;
        this.banos = banos;
        this.barrio = barrio;
    }

    // GETTERS Y SETTERS
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

    public String getNombrePersonalizado() { return nombrePersonalizado; }
    public void setNombrePersonalizado(String nombrePersonalizado) { this.nombrePersonalizado = nombrePersonalizado; }

    public String getZona() { return zona; }
    public void setZona(String zona) { this.zona = zona; }

    public int getMetros() { return metros; }
    public void setMetros(int metros) { this.metros = metros; }

    public double getUltimoPrecio() { return ultimoPrecio; }
    public void setUltimoPrecio(double ultimoPrecio) { this.ultimoPrecio = ultimoPrecio; }

    public int getHabitaciones() { return habitaciones; }
    public void setHabitaciones(int h) { this.habitaciones = h; }
    public int getBanos() { return banos; }
    public void setBanos(int b) { this.banos = b; }
    public String getBarrio() { return barrio; }
    public void setBarrio(String barrio) { this.barrio = barrio; }

}