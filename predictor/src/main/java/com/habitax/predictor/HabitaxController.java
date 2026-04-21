package com.habitax.predictor;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
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

    private final String API_KEY = "f6d6ea3df2msh6e78596b39fbc81p1aa67ejsnb83fc12344e5";
    private final String API_HOST = "idealista7.p.rapidapi.com";

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
        // Comparamos la contrasena escrita contra el hash BCrypt almacenado
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            session.setAttribute("usuarioLogueado", user);
            return "redirect:/predictor";
        }
        model.addAttribute("error", "Email o contrasena incorrectos");
        return "login";
    }

    @GetMapping("/predictor")
    public String predictor(HttpSession session, Model model) {
        Usuario user = (Usuario) session.getAttribute("usuarioLogueado");
        if (user == null) return "redirect:/";
        model.addAttribute("provincias", PROVINCIAS);
        model.addAttribute("nombreUsuario", user.getNombre());
        model.addAttribute("historial", prediccionRepo.findAll());
        return "index";
    }

    @GetMapping("/api/zonas")
    @ResponseBody
    public List<String> obtenerZonas(@RequestParam String provincia) {
        String busqueda = Normalizer.normalize(provincia, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

        String url = "https://idealista7.p.rapidapi.com/getsuggestions" +
                "?prefix=" + busqueda +
                "&location=es" +
                "&propertyType=homes" +
                "&operation=sale";

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-rapidapi-key", API_KEY);
        headers.set("x-rapidapi-host", API_HOST);

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
            System.err.println("Error: " + e.getMessage());
            return List.of("Error al cargar zonas");
        }
    }

    @PostMapping("/predecir")
    public String consultarIdealista(@RequestParam int metros,
                                     @RequestParam String zona,
                                     @RequestParam int habitaciones,
                                     @RequestParam int banos,
                                     HttpSession session, Model model) {

        Usuario user = (Usuario) session.getAttribute("usuarioLogueado");
        if (user == null) return "redirect:/";

        String url = "https://idealista7.p.rapidapi.com/listhomes?locationName=" + zona +
                "&operation=sale&location=es&locale=es&numPage=1&maxItems=30";

        if (habitaciones > 0) url += "&rooms=" + habitaciones;
        if (banos > 0) url += "&bathrooms=" + banos;

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-rapidapi-key", API_KEY);
        headers.set("x-rapidapi-host", API_HOST);

        double resultadoFinal = 0;
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
            resultadoFinal = metros * 3500.0;
        }

        prediccionRepo.save(new Prediccion(zona, metros, habitaciones, banos, resultadoFinal));

        model.addAttribute("resultado", resultadoFinal);
        model.addAttribute("zonaSeleccionada", zona);
        model.addAttribute("metrosIngresados", metros);
        model.addAttribute("nombreUsuario", user.getNombre());
        model.addAttribute("provincias", PROVINCIAS);
        model.addAttribute("historial", prediccionRepo.findAll());
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
        // Ciframos la contrasena con BCrypt antes de guardarla en BD
        String passwordHash = passwordEncoder.encode(password);
        usuarioRepo.save(new Usuario(nombre, email, passwordHash));
        return "login";
    }
}