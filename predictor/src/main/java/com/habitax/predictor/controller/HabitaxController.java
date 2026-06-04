package com.habitax.predictor.controller;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.habitax.predictor.dto.PrediccionDTO;
import com.habitax.predictor.dto.UsuarioSesionDTO;
import com.habitax.predictor.model.Favorito;
import com.habitax.predictor.model.Usuario;
import com.habitax.predictor.repository.FavoritoRepository;
import com.habitax.predictor.repository.PrediccionRepository;
import com.habitax.predictor.repository.UsuarioRepository;
import com.habitax.predictor.service.LoginRateLimiterService;
import com.habitax.predictor.service.PrecioService;

import jakarta.servlet.http.HttpSession;

@Controller
public class HabitaxController {

    @Autowired
    private PrediccionRepository prediccionRepo;

    @Autowired
    private UsuarioRepository usuarioRepo;

    @Autowired
    private FavoritoRepository favoritoRepo;

    @Autowired
    private PrecioService precioService; 

    @Autowired
    private LoginRateLimiterService rateLimiterService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    private static final int LIMITE_HISTORIAL = 3;

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
    public String loginPage() { 
        return "login"; 
    }

    @PostMapping("/login")
    public String procesarLogin(@RequestParam String email, 
                                @RequestParam String password, 
                                HttpSession session, 
                                Model model) {
        
        if (rateLimiterService.estaBloqueado(email)) {
            model.addAttribute("error", "Esta cuenta ha sido preliminarmente bloqueada por 15 minutos.");
            return "login";
        }

        Usuario user = usuarioRepo.findByEmail(email);

        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            rateLimiterService.limpiarIntentos(email);
            
            UsuarioSesionDTO dtoSesion = new UsuarioSesionDTO(user.getId(), user.getNombre(), user.getEmail());
            session.setAttribute("usuarioLogueado", dtoSesion);
            
            return "redirect:/predictor";
        } else {
            rateLimiterService.registrarIntentoFallido(email);
            int restantes = rateLimiterService.getIntentosRestantes(email);
            
            if (restantes <= 0) {
                model.addAttribute("error", "Has superado el límite de intentos. Cuenta bloqueada por 15 minutos.");
            } else {
                model.addAttribute("error", "Credenciales incorrectas. Te quedan " + restantes + " intentos.");
            }
            return "login";
        }
    }

    @GetMapping("/predictor")
    public String mostrarPredictor(Model model, HttpSession session) {
        UsuarioSesionDTO user = (UsuarioSesionDTO) session.getAttribute("usuarioLogueado");
        if (user == null) return "redirect:/";

        model.addAttribute("nombreUsuario", user.getNombre());
        model.addAttribute("provincias", PROVINCIAS);
        model.addAttribute("historial", obtenerHistorialDTO(user.getId()));

        // Aviso si no hay API Key configurada
        boolean tieneApiKey = Boolean.TRUE.equals(session.getAttribute("tieneApiKey"))
                || precioService.tieneApiKey();
        model.addAttribute("sinApiKey", !tieneApiKey);

        return "index";
    }

    @GetMapping("/perfil")
    public String verPerfil(HttpSession session, Model model) {
        UsuarioSesionDTO user = (UsuarioSesionDTO) session.getAttribute("usuarioLogueado");
        if (user == null) return "redirect:/";

        model.addAttribute("historial", obtenerHistorialDTO(user.getId()));
        model.addAttribute("favoritos", favoritoRepo.findByUsuarioId(user.getId()));
        model.addAttribute("nombreUsuario", user.getNombre());
        model.addAttribute("emailUsuario", user.getEmail());

        return "perfil";
    }

    @GetMapping("/api/zonas")
    @ResponseBody
    public List<String> obtenerZonas(@RequestParam String provincia, HttpSession session) {
        UsuarioSesionDTO user = (UsuarioSesionDTO) session.getAttribute("usuarioLogueado");
        if (user == null) return List.of();

        return precioService.obtenerSugerenciasZonas(provincia);
    }

    @PostMapping("/predecir")
    public String consultarIdealista(@RequestParam int metros,
                                     @RequestParam String provincia,
                                     @RequestParam String zona,
                                     @RequestParam int habitaciones,
                                     @RequestParam int banos,
                                     HttpSession session, Model model) {

        UsuarioSesionDTO user = (UsuarioSesionDTO) session.getAttribute("usuarioLogueado");
        if (user == null) return "redirect:/";

        long tiempoInicio = System.currentTimeMillis();

        double precioMedio = precioService.obtenerPrecioConCache(user.getId(), provincia, zona, metros, habitaciones, banos);
        
        double precioBarato = precioService.calcularPrecioBarato(precioMedio);
        double precioPremium = precioService.calcularPrecioPremium(precioMedio);

        long duracion = System.currentTimeMillis() - tiempoInicio;

        model.addAttribute("resultado", precioMedio);
        model.addAttribute("precioBarato", precioBarato);
        model.addAttribute("precioPremium", precioPremium);
        model.addAttribute("zonaSeleccionada", zona);
        model.addAttribute("provinciaSeleccionada", provincia); // Inyectamos provincia de control
        model.addAttribute("metrosIngresados", metros);
        model.addAttribute("nombreUsuario", user.getNombre());
        model.addAttribute("provincias", PROVINCIAS);
        model.addAttribute("habitacionesIngresadas", habitaciones);
        model.addAttribute("banosIngresados", banos);
        model.addAttribute("tiempoRespuesta", duracion); 
        model.addAttribute("historial", obtenerHistorialDTO(user.getId()));

        boolean tieneApiKey = Boolean.TRUE.equals(session.getAttribute("tieneApiKey"))
                || precioService.tieneApiKey();
        model.addAttribute("sinApiKey", !tieneApiKey);

        return "index";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    @GetMapping("/registro")
    public String mostrarRegistro() { 
        return "registro"; 
    }

    @PostMapping("/registro")
    public String registrarUsuario(@RequestParam String nombre, 
                                   @RequestParam String email, 
                                   @RequestParam String password, 
                                   Model model) {
        
        if (nombre == null || nombre.trim().length() < 2) {
            model.addAttribute("error", "El nombre debe tener al menos 2 caracteres.");
            return "registro";
        }
        
        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            model.addAttribute("error", "El formato del correo electrónico no es válido.");
            return "registro";
        }
        
        if (password == null || password.length() < 6) {
            model.addAttribute("error", "La contraseña es demasiado corta (mínimo 6 caracteres).");
            return "registro";
        }

        if (usuarioRepo.existsByEmail(email)) {
            model.addAttribute("error", "Ese correo electrónico ya está registrado en HABITAX.");
            return "registro";
        }

        String passwordHash = passwordEncoder.encode(password);
        usuarioRepo.save(new Usuario(nombre, email, passwordHash));
        
        model.addAttribute("mensajeExito", "¡Cuenta creada con éxito! Ya puedes iniciar sesión.");
        return "login";
    }

    // ÚNICO MÉTODO UNIFICADO PARA GUARDAR FAVORITOS
    @PostMapping("/favoritos/guardar")
    public String guardarFavorito(@RequestParam(required = false, defaultValue = "Madrid") String provincia,
                                  @RequestParam String zona, 
                                  @RequestParam int metros, 
                                  @RequestParam double precio,
                                  @RequestParam(required = false, defaultValue = "0") Integer habitaciones,
                                  @RequestParam(required = false, defaultValue = "0") Integer banos,
                                  @RequestParam(defaultValue = "") String barrio,
                                  HttpSession session, Model model) {
        
        UsuarioSesionDTO user = (UsuarioSesionDTO) session.getAttribute("usuarioLogueado");
        if (user == null) return "redirect:/";

        Favorito nuevoFav = new Favorito(user.getId(), "Propiedad en " + zona, provincia, zona, metros, precio, habitaciones, banos, barrio);
        favoritoRepo.save(nuevoFav);

        model.addAttribute("mensajeExito", "¡Propiedad añadida a tus favoritos!");
        model.addAttribute("resultado", precio);
        model.addAttribute("precioBarato", precioService.calcularPrecioBarato(precio));
        model.addAttribute("precioPremium", precioService.calcularPrecioPremium(precio));
        model.addAttribute("zonaSeleccionada", zona);
        model.addAttribute("metrosIngresados", metros);
        model.addAttribute("habitacionesIngresadas", habitaciones);
        model.addAttribute("banosIngresados", banos);
        model.addAttribute("provincias", PROVINCIAS);
        model.addAttribute("nombreUsuario", user.getNombre());
        model.addAttribute("historial", obtenerHistorialDTO(user.getId()));
        
        return "index"; 
    }

    @PostMapping("/recalcular")
    public String recalcularFavorito(@RequestParam Long idFavorito, HttpSession session) {
        UsuarioSesionDTO user = (UsuarioSesionDTO) session.getAttribute("usuarioLogueado");
        if (user == null) return "redirect:/";

        Favorito fav = favoritoRepo.findById(idFavorito).orElse(null);
        
        if (fav != null) {
            // Tu cálculo base original
            double precioBase = fav.getMetros() * 3250.0; 
            
            // Creamos un factor aleatorio para simular fluctuaciones reales de mercado en cada clic
            // Esto genera un porcentaje aleatorio entre -5% y +5%
            double variacionMercado = 0.95 + (Math.random() * 0.10); 
            
            // Aplicamos la variación al precio
            double precioActualizado = Math.round(precioBase * variacionMercado);
            
            // Guardamos el nuevo precio calculado para esta pulsación
            fav.setUltimoPrecio(precioActualizado);
            favoritoRepo.save(fav);
        }

        // Redirigimos para refrescar la pantalla con el nuevo valor cambiado
        return "redirect:/perfil"; 
    }

    private List<PrediccionDTO> obtenerHistorialDTO(Long usuarioId) {
        List<com.habitax.predictor.model.Prediccion> entidades = prediccionRepo.findByUsuarioIdOrderByFechaDesc(usuarioId, PageRequest.of(0, LIMITE_HISTORIAL));
        
        return entidades.stream()
                .map(p -> new PrediccionDTO(p.getZona(), p.getMetros(), p.getPrecio()))
                .collect(java.util.stream.Collectors.toList());
    }
}