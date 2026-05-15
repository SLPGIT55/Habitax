package com.habitax.predictor;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FavoritoRepository extends JpaRepository<Favorito, Long> {
    // Busca todos los favoritos de un usuario específico
    List<Favorito> findByUsuarioId(Long usuarioId);
}