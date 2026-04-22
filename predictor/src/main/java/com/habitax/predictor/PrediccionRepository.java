package com.habitax.predictor;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrediccionRepository extends JpaRepository<Prediccion, Long> {

    // Devuelve las predicciones de un usuario ordenadas por fecha descendente.
    // Se usa Pageable para limitar el numero de resultados desde el controlador.
    List<Prediccion> findByUsuarioIdOrderByFechaDesc(Long usuarioId, Pageable pageable);
}