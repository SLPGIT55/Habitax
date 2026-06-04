package com.habitax.predictor.dto;

import java.io.Serializable;

public class UsuarioSesionDTO implements Serializable {
    
    private Long id;
    private String nombre;
    private String email;

    // Constructor vacío (necesario por buenas prácticas)
    public UsuarioSesionDTO() {
    }

    // Constructor lleno para pasarle los datos rápidamente en el Login
    public UsuarioSesionDTO(Long id, String nombre, String email) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
    }

    // Getters y Setters para que Thymeleaf y el controlador puedan leer los datos
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
