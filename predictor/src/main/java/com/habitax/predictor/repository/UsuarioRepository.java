package com.habitax.predictor.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.habitax.predictor.model.Usuario;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Usuario findByEmail(String email);
    
    // CORRECCIÓN AUDITORÍA 2.5: Método para comprobar si el email ya está registrado
    boolean existsByEmail(String email);
}