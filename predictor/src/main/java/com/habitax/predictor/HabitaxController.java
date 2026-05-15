package com.habitax.predictor;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.HttpSession;

@Controller
public class HabitaxController {

    @Autowired
    private PrediccionRepository prediccionRepo;

    @Autowired
    private UsuarioRepository usuarioRepo;

    // BCrypt encoder para cifrar/verificar contrasenas
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // Numero maximo de busquedas recientes a mostrar en el historial
    private static final int LIMITE_HISTORIAL = 3;

    // API Key cargada desde variable de entorno
    @Value("${rapidapi.key}")
    private String apiKey;

    @Value("${rapidapi.host:idealista7.p.rapidapi.com}")
    private String apiHost;

    private final List<String> PROVINCIAS = Arrays.asList(
            "Alava", "Albacete", "Alicante", "Almeria", "Asturias", "Avila", "Badajoz", "Baleares",
            "Barcelona", "Burgos", "Caceres", "Cadiz", "Cantabria", "Castellon", "Ciudad Real",
            "Cordoba", "A Coruna", "Cuenca", "Girona", "Granada", "Guadalajara", "Gipuzkoa",
            "Huelva", "Huesca", "Jaen", "Leon", "Lleida", "Lugo", "Madrid", "Malaga", "Murcia",
            "Navarra", "Ourense", "Palencia", "Las Palmas", "Pontevedra", "La Rioja", "Salamanca",
            "Segovia", "Sevilla", "Soria", "Tarragona", "Santa Cruz de Tenerife", "Teruel",
            "Toledo", "Valencia", "Valladolid", "Vizcaya", "Zamora", "Zaragoza"
    );

    @GetMapping("/")
    public String loginPage() { return "login"; }

    @PostMapping("/login")
    public String login(@RequestParam String email, @RequestParam String password, HttpSession session, Model model) {
        Usuario user = usuarioRepo.findByEmail(email);
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            session.setAttribute("usuarioLogueado", user);
            return "redirect:/predictor";
        }
        model.addAttribute("error", "Email o contrasena incorrectos");
        return "login";
    }

   @GetMapping("/predictor")
    public String mostrarPredictor(Model model, HttpSession session) {
        Usuario user = (Usuario) session.getAttribute("usuarioLogueado");
        if (user == null) return "redirect:/";

        model.addAttribute("nombreUsuario", user.getNombre());
        model.addAttribute("provincias", PROVINCIAS); // Asegúrate de que se llame así

        // LLAMADA CORREGIDA AL REPOSITORIO
        model.addAttribute("historial", 
            prediccionRepo.findByUsuarioIdOrderByFechaDesc(user.getId(), PageRequest.of(0, 3)));

        return "index";
    }

  

   @GetMapping("/perfil")
    public String verPerfil(HttpSession session, Model model) {
        Usuario user = (Usuario) session.getAttribute("usuarioLogueado");
        if (user == null) return "redirect:/";

        model.addAttribute("historial", prediccionRepo.findByUsuarioIdOrderByFechaDesc(user.getId(), PageRequest.of(0, 3)));
        
        // CARGA REAL DE FAVORITOS
        model.addAttribute("favoritos", favoritoRepo.findByUsuarioId(user.getId()));
        
        model.addAttribute("nombreUsuario", user.getNombre());
        model.addAttribute("emailUsuario", user.getEmail());

        return "perfil";
    }
    /**
     * Protegido por sesion: solo responde si el usuario esta autenticado.
     * Evita que terceros consuman nuestra cuota de RapidAPI desde fuera.
     */
    @GetMapping("/api/zonas")
    @ResponseBody
    public List<String> obtenerZonas(@RequestParam String provincia, HttpSession session) {
        // Proteccion: solo usuarios logueados pueden usar este endpoint
        Usuario user = (Usuario) session.getAttribute("usuarioLogueado");
        if (user == null) {
            return List.of();
        }

        String busqueda = Normalizer.normalize(provincia, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

        String url = "https://" + apiHost + "/getsuggestions" +
                "?prefix=" + busqueda +
                "&location=es" +
                "&propertyType=homes" +
                "&operation=sale";

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-rapidapi-key", apiKey);
        headers.set("x-rapidapi-host", apiHost);

        try {
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
            System.err.println("Error al obtener zonas: " + e.getMessage());
            return List.of("Error al cargar zonas");
        }
    }

    @PostMapping("/predecir")
    public String consultarIdealista(@RequestParam int metros,
                                    @RequestParam String provincia,
                                    @RequestParam String zona,
                                    @RequestParam int habitaciones,
                                    @RequestParam int banos,
                                    HttpSession session, Model model) {

        Usuario user = (Usuario) session.getAttribute("usuarioLogueado");
        if (user == null) return "redirect:/";

        // --- TAREA 3: LÓGICA DE CACHÉ (1 HORA) ---
        java.time.LocalDateTime haceUnaHora = java.time.LocalDateTime.now().minusHours(1);

        // Verificamos si existe una búsqueda idéntica reciente
        List<Prediccion> busquedasRecientes = prediccionRepo
                .findByUsuarioIdAndZonaAndMetrosAndFechaAfter(user.getId(), zona, metros, haceUnaHora);

        double resultadoFinal = 0;

        if (!busquedasRecientes.isEmpty()) {
            // ESCENARIO A: Recuperamos datos de la base de datos (Caché)
            resultadoFinal = busquedasRecientes.get(0).getPrecio();
            System.out.println(">>> Info: Recuperando datos de caché para evitar llamada a API redundante");
        } else {
            // ESCENARIO B: No hay caché. Llamada a la API de Idealista
            String busquedaPrecisa = provincia + " " + zona; // TAREA 2.1: Precisión

            String url = "https://" + apiHost + "/listhomes?locationName=" + busquedaPrecisa +
                    "&operation=sale&location=es&locale=es&numPage=1&maxItems=30";

            if (habitaciones > 0) url += "&rooms=" + habitaciones;
            if (banos > 0) url += "&bathrooms=" + banos;

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set("x-rapidapi-key", apiKey);
            headers.set("x-rapidapi-host", apiHost);

            try {
                HttpEntity<String> entity = new HttpEntity<>(headers);
                ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
                List<Map<String, Object>> casas = (List<Map<String, Object>>) response.getBody().get("elementList");

                if (casas != null && !casas.isEmpty()) {
                    double sumaM2 = 0;
                    int total = 0;
                    for (Map<String, Object> casa : casas) {
                        if (casa.get("price") != null && casa.get("size") != null) {
                            sumaM2 += (Double.parseDouble(casa.get("price").toString()) / Double.parseDouble(casa.get("size").toString()));
                            total++;
                        }
                    }
                    resultadoFinal = (sumaM2 / total) * metros;
                }
            } catch (Exception e) {
                System.err.println("Error al consultar Idealista: " + e.getMessage());
                resultadoFinal = metros * 3500.0; // Fallback
            }

            // Guardamos la nueva predicción (servirá de caché para la siguiente hora)
            prediccionRepo.save(new Prediccion(user.getId(), zona, metros, habitaciones, banos, resultadoFinal));
        }

        // --- TAREA 2.2: LÓGICA DE ALGORITMO (3 PRECIOS) ---
        double precioMedio = resultadoFinal;
        double precioBarato = resultadoFinal * 0.85;
        double precioPremium = resultadoFinal * 1.25;

        // --- TAREA 3.2: HISTORIAL LIMITADO (TOP 3) ---
        List<Prediccion> historialTresUltimas = prediccionRepo
                .findByUsuarioIdOrderByFechaDesc(user.getId(), PageRequest.of(0, 3));

        // ENVÍO DE DATOS A LA VISTA
        model.addAttribute("resultado", precioMedio);
        model.addAttribute("precioBarato", precioBarato);
        model.addAttribute("precioPremium", precioPremium);

        model.addAttribute("zonaSeleccionada", zona);
        model.addAttribute("metrosIngresados", metros);
        model.addAttribute("nombreUsuario", user.getNombre());
        model.addAttribute("provincias", PROVINCIAS);
        model.addAttribute("historial", historialTresUltimas);
        model.addAttribute("habitacionesIngresadas", habitaciones);
        model.addAttribute("banosIngresados", banos);

        return "index";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    @GetMapping("/registro")
    public String mostrarRegistro() { return "registro"; }

    @PostMapping("/registro")
    public String registrarUsuario(@RequestParam String nombre, @RequestParam String email, @RequestParam String password, Model model) {
        String passwordHash = passwordEncoder.encode(password);
        usuarioRepo.save(new Usuario(nombre, email, passwordHash));
        return "login";
    }

    @PostMapping("/favoritos/guardar")
    public String guardarFavorito(@RequestParam String zona, 
                                @RequestParam int metros, 
                                @RequestParam double precio,
                                @RequestParam(required = false) Integer habitaciones,
                                @RequestParam(required = false) Integer banos,
                                @RequestParam(defaultValue = "") String barrio,
                                HttpSession session, Model model) {
        Usuario user = (Usuario) session.getAttribute("usuarioLogueado");
        if (user == null) return "redirect:/";
        if (habitaciones == null) habitaciones = 0;
        if (banos == null) banos = 0;
        System.out.println(">>> GUARDAR FAVORITO - habitaciones: " + habitaciones + ", banos: " + banos + ", zona: " + zona); // Check si guarda bien los datos


        // Guardamos el favorito en la BD
        Favorito nuevoFav = new Favorito(
                user.getId(), "Propiedad en " + zona, zona, metros, precio, habitaciones, banos, barrio
        );
        favoritoRepo.save(nuevoFav);

        // Cargamos los datos para volver a mostrar el index con el mensaje de éxito
        model.addAttribute("mensajeExito", "¡Propiedad añadida a tus favoritos!");
        model.addAttribute("resultado", precio);
        model.addAttribute("zonaSeleccionada", zona);
        model.addAttribute("metrosIngresados", metros);
        model.addAttribute("habitacionesIngresadas", habitaciones);
        model.addAttribute("banosIngresados", banos);
        model.addAttribute("provincias", PROVINCIAS);
        model.addAttribute("nombreUsuario", user.getNombre());
        model.addAttribute("historial", 
            prediccionRepo.findByUsuarioIdOrderByFechaDesc(user.getId(), org.springframework.data.domain.PageRequest.of(0, 3)));
        
        // Volvemos a calcular los segmentos para la vista
        model.addAttribute("resultado", precio);
        model.addAttribute("precioBarato", precio * 0.85);
        model.addAttribute("precioPremium", precio * 1.25);
        
        // Resto de atributos necesarios para index.html
        model.addAttribute("provincias", PROVINCIAS);
        model.addAttribute("nombreUsuario", user.getNombre());
        model.addAttribute("zonaSeleccionada", zona);
        model.addAttribute("metrosIngresados", metros);
        model.addAttribute("mensajeExito", "¡Propiedad añadida a favoritos!");
            return "index"; 
    }
    @PostMapping("/recalcular")
    public String recalcularFavorito(@RequestParam Long idFavorito, HttpSession session) {
        Usuario user = (Usuario) session.getAttribute("usuarioLogueado");
        if (user == null) return "redirect:/";

        // 1. Buscamos el favorito guardado
        Favorito fav = favoritoRepo.findById(idFavorito).orElse(null);
        
        if (fav != null) {
            // 2. Ejecutamos el recálculo (Aquí puedes llamar a tu lógica de la API)
            // Por ahora, simulamos la actualización del valor de mercado:
            double precioActualizado = fav.getMetros() * 3250.0; 
            
            // 3. Guardamos el nuevo precio
            fav.setUltimoPrecio(precioActualizado);
            favoritoRepo.save(fav);
        }

        // 4. Redirigimos al perfil para ver el cambio reflejado
        return "redirect:/perfil"; 
    }

    
    
        @Autowired
        private FavoritoRepository favoritoRepo;

    }