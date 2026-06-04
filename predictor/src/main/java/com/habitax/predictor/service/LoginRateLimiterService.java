package com.habitax.predictor.service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

@Service
public class LoginRateLimiterService {

    private final int MAX_INTENTOS = 5;
    private final long TIEMPO_BLOQUEO_MS = TimeUnit.MINUTES.toMillis(15); // 15 minutos

    // Estructuras en memoria para almacenar los intentos y el momento del bloqueo
    private final ConcurrentHashMap<String, Integer> intentosRegistro = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> tiempoBloqueoRegistro = new ConcurrentHashMap<>();

    // Verifica si un correo está bloqueado actualmente
    public boolean estaBloqueado(String email) {
        if (!tiempoBloqueoRegistro.containsKey(email)) {
            return false;
        }

        long momentoBloqueo = tiempoBloqueoRegistro.get(email);
        if (System.currentTimeMillis() - momentoBloqueo > TIEMPO_BLOQUEO_MS) {
            // El tiempo de penalización ya ha pasado, lo desbloqueamos
            tiempoBloqueoRegistro.remove(email);
            intentosRegistro.remove(email);
            return false;
        }
        return true;
    }

    // Registra un intento fallido
    public void registrarIntentoFallido(String email) {
        int intentos = intentosRegistro.getOrDefault(email, 0) + 1;
        intentosRegistro.put(email, intentos);

        if (intentos >= MAX_INTENTOS) {
            tiempoBloqueoRegistro.put(email, System.currentTimeMillis());
        }
    }

    // Si el login es correcto, limpiamos sus intentos acumulados
    public void limpiarIntentos(String email) {
        intentosRegistro.remove(email);
        tiempoBloqueoRegistro.remove(email);
    }
    
    // Devuelve los intentos restantes orientativos
    public int getIntentosRestantes(String email) {
        return MAX_INTENTOS - intentosRegistro.getOrDefault(email, 0);
    }
}
