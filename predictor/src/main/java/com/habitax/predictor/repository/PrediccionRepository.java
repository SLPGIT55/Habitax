package com.habitax.predictor.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.habitax.predictor.model.Prediccion;

public interface PrediccionRepository extends JpaRepository<Prediccion, Long> {
    
    // Para el historial (Tarea 3)
    List<Prediccion> findByUsuarioIdOrderByFechaDesc(Long usuarioId, Pageable pageable);

    // Para la caché (Tarea 3)
    List<Prediccion> findByUsuarioIdAndZonaAndMetrosAndFechaAfter(
        Long usuarioId, String zona, int metros, LocalDateTime fecha);
}