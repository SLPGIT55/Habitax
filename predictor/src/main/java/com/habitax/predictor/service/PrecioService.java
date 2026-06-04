package com.habitax.predictor.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.habitax.predictor.model.Prediccion;
import com.habitax.predictor.repository.PrediccionRepository;

@Service
public class PrecioService {

    private static final Logger log = LoggerFactory.getLogger(PrecioService.class);

    @Autowired
    private PrediccionRepository prediccionRepo;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${rapidapi.key}")
    private String apiKey;

    @Value("${rapidapi.host:idealista7.p.rapidapi.com}")
    private String apiHost;

    private static final double PRECIO_FALLBACK_M2 = 3500.0;
    private static final double FACTOR_OPORTUNIDAD = 0.85;
    private static final double FACTOR_PREMIUM = 1.25;

    public double calcularPrecioBarato(double precioMedio) {
        return precioMedio * FACTOR_OPORTUNIDAD;
    }

    public double calcularPrecioPremium(double precioMedio) {
        return precioMedio * FACTOR_PREMIUM;
    }

    public List<String> obtenerSugerenciasZonas(String provincia) {
        String busqueda = Normalizer.normalize(provincia, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

        try {
            String parametroCodificado = URLEncoder.encode(busqueda, StandardCharsets.UTF_8);
            String url = "https://" + apiHost + "/getsuggestions?prefix=" + parametroCodificado + "&location=es&propertyType=homes&operation=sale";

            HttpHeaders headers = new HttpHeaders();
            headers.set("x-rapidapi-key", apiKey);
            headers.set("x-rapidapi-host", apiHost);

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

            Map<String, Object> body = response.getBody();
            List<Map<String, Object>> lista = (List<Map<String, Object>>) body.get("locations");

            if (lista == null || lista.isEmpty()) {
                return List.of("No se encontraron zonas");
            }

            return lista.stream()
                    .map(loc -> loc.get("name").toString())
                    .distinct()
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error al obtener zonas para la provincia de {}: {}", provincia, e.getMessage());
            return List.of("Error al cargar zonas");
        }
    }

    public double obtenerPrecioConCache(Long usuarioId, String provincia, String zona, int metros, int habitaciones, int banos) {
        LocalDateTime haceUnaHora = LocalDateTime.now().minusHours(1);
        List<Prediccion> busquedasRecientes = prediccionRepo.findByUsuarioIdAndZonaAndMetrosAndFechaAfter(usuarioId, zona, metros, haceUnaHora);

        if (!busquedasRecientes.isEmpty()) {
            log.info("[CACHÉ HIT] Recuperando datos históricos para la zona: {} y m²: {}", zona, metros);
            return busquedasRecientes.get(0).getPrecio();
        }

        log.info("[CACHÉ MISS] Solicitando estimación a la API externa para: {}", zona);
        return ejecutarConsultaApi(usuarioId, provincia, zona, metros, habitaciones, banos);
    }

    public double recalcularPrecioDirecto(Long usuarioId, String provincia, String zona, int metros, int habitaciones, int banos) {
        log.info("[FORZAR REFRESCO] Solicitando estimación DIRECTA a la API externa para: {}", zona);
        return ejecutarConsultaApi(usuarioId, provincia, zona, metros, habitaciones, banos);
    }

    /**
     * Método privado unificado que sanea parámetros y conecta de forma segura con RapidAPI
     */
    private double ejecutarConsultaApi(Long usuarioId, String provincia, String zona, int metros, int habitaciones, int banos) {
        double resultadoFinal = 0;
        try {
            // Evitamos errores de formato eliminando comas raras y codificando la URL limpiamente
            String busquedaLimpia = (provincia + " " + zona).replace(",", "");
            String parametroCodificado = URLEncoder.encode(busquedaLimpia, StandardCharsets.UTF_8);

            String url = "https://" + apiHost + "/listhomes?locationName=" + parametroCodificado +
                    "&operation=sale&location=es&locale=es&numPage=1&maxItems=30";

            if (habitaciones > 0) url += "&rooms=" + habitaciones;
            if (banos > 0) url += "&bathrooms=" + banos;

            HttpHeaders headers = new HttpHeaders();
            headers.set("x-rapidapi-key", apiKey);
            headers.set("x-rapidapi-host", apiHost);

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            List<Map<String, Object>> casas = (List<Map<String, Object>>) response.getBody().get("elementList");

            if (casas != null && !casas.isEmpty()) {
                double sumaPreciosPorMetro = 0;
                int total = 0;
                for (Map<String, Object> casa : casas) {
                    if (casa.get("price") != null && casa.get("size") != null) {
                        sumaPreciosPorMetro += Double.parseDouble(casa.get("price").toString()) / Double.parseDouble(casa.get("size").toString());
                        total++;
                    }
                }
                resultadoFinal = (sumaPreciosPorMetro / total) * metros;
            } else {
                log.warn("[API] La respuesta no contiene elementos válidos. Utilizando fallback.");
                resultadoFinal = metros * PRECIO_FALLBACK_M2;
            }
        } catch (Exception e) {
            log.error("Error crítico al mapear respuesta de la API externa: {}", e.getMessage());
            resultadoFinal = metros * PRECIO_FALLBACK_M2;
        }

        // Almacenamos el resultado en la tabla H2 de histórico de consultas
        Prediccion p = new Prediccion(usuarioId, zona, metros, habitaciones, banos, resultadoFinal);
        p.setFecha(LocalDateTime.now());
        prediccionRepo.save(p);

        return resultadoFinal;
    }
}