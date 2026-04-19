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

    private final String API_KEY = "f6d6ea3df2msh6e78596b39fbc81p1aa67ejsnb83fc12344e5";
    private final String API_HOST = "idealista7.p.rapidapi.com";

    private final List<String> PROVINCIAS = Arrays.asList(
        "Álava", "Albacete", "Alicante", "Almería", "Asturias", "Ávila", "Badajoz", "Baleares", 
        "Barcelona", "Burgos", "Cáceres", "Cádiz", "Cantabria", "Castellón", "Ciudad Real", 
        "Córdoba", "A Coruña", "Cuenca", "Girona", "Granada", "Guadalajara", "Gipuzkoa", 
        "Huelva", "Huesca", "Jaén", "León", "Lleida", "Lugo", "Madrid", "Málaga", "Murcia", 
        "Navarra", "Ourense", "Palencia", "Las Palmas", "Pontevedra", "La Rioja", "Salamanca", 
        "Segovia", "Sevilla", "Soria", "Tarragona", "Santa Cruz de Tenerife", "Teruel", 
        "Toledo", "Valencia", "Valladolid", "Vizcaya", "Zamora", "Zaragoza"
    );

    @GetMapping("/")
    public String loginPage() { return "login"; }

    @PostMapping("/login")
    public String login(@RequestParam String email, @RequestParam String password, HttpSession session, Model model) {
        Usuario user = usuarioRepo.findByEmail(email);
        if (user != null && user.getPassword().equals(password)) {
            session.setAttribute("usuarioLogueado", user);
            return "redirect:/predictor";
        }
        model.addAttribute("error", "Email o contraseña incorrectos");
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

    /**
     * TAREA: Desplegables anidados REALES.
     * Según tu captura image_51fe8c.png, el endpoint es /getsuggestions
     */
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
            
            // CAMBIO CLAVE: Tu API usa la palabra "locations", no "suggestions"
            List<Map<String, Object>> lista = (List<Map<String, Object>>) body.get("locations");

            if (lista == null || lista.isEmpty()) {
                return List.of("No se encontraron zonas");
            }

            // Extraemos los nombres (Alicante Golf, Torrevieja, etc.)
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

        // Según tu captura image_5202e1.png, el endpoint es /listhomes
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
        usuarioRepo.save(new Usuario(nombre, email, password));
        return "login";
    }
}