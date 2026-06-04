package com.habitax.predictor.repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.habitax.predictor.model.Favorito;

public interface FavoritoRepository extends JpaRepository<Favorito, Long> {
    // Busca todos los favoritos de un usuario específico
    List<Favorito> findByUsuarioId(Long usuarioId);
}