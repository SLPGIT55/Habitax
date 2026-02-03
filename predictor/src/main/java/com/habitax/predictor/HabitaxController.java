package com.habitax.predictor;

import java.util.List;
import java.util.Map;

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
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.HttpSession;

@Controller
public class HabitaxController {

    @Autowired
    private PrediccionRepository prediccionRepo;

    @Autowired
    private UsuarioRepository usuarioRepo;

    @GetMapping("/")
    public String loginPage() {
        return "login";
    }

    // --- ¡ESTO FALTABA! El método para procesar el login ---
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
        
        model.addAttribute("nombreUsuario", user.getNombre());
        model.addAttribute("historial", prediccionRepo.findAll());
        return "index";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    @PostMapping("/predecir")
    public String consultarIdealista(@RequestParam int metros, 
                                   @RequestParam String zona, 
                                   @RequestParam int habitaciones,
                                   @RequestParam int banos,
                                   HttpSession session, Model model) {
        
        Usuario user = (Usuario) session.getAttribute("usuarioLogueado");
        if (user == null) return "redirect:/";

        String zonaLimpia = zona.trim();
        String locationId = "";
        
        if (zonaLimpia.equalsIgnoreCase("Madrid")) locationId = "0-EU-ES-28-07-001-079";
        else if (zonaLimpia.equalsIgnoreCase("Barcelona")) locationId = "0-EU-ES-08-13-001-019";

        // URL CORREGIDA: Construida en una sola línea para evitar errores de concatenación
        // URL DEFINITIVA: Corregida según el playground de RapidAPI
        String url = "https://idealista7.p.rapidapi.com/listhomes?order=relevance&operation=sale&locationId=" + locationId + 
                    "&locationName=" + zonaLimpia + "&numPage=1&maxItems=40&location=es&locale=es";

        // Si el usuario puso habitaciones o baños, los añadimos a la URL
        if (habitaciones > 0) url += "&rooms=" + habitaciones;
        if (banos > 0) url += "&bathrooms=" + banos;
                
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-rapidapi-key", "f6d6ea3df2msh6e78596b39fbc81p1aa67ejsnb83fc12344e5");
        headers.set("x-rapidapi-host", "idealista7.p.rapidapi.com");

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
            } else {
                resultadoFinal = metros * 3950.0;
            }
        } catch (Exception e) {
            System.out.println("DEBUG - URL USADA: " + url); // Esto te ayudará a ver qué se envía
            System.out.println("ERROR DE RED O CUOTA: " + e.getMessage());
            resultadoFinal = metros * 3700.0; 
        }

        prediccionRepo.save(new Prediccion(zonaLimpia, metros, habitaciones, banos, resultadoFinal));

        model.addAttribute("resultado", resultadoFinal);
        model.addAttribute("zonaSeleccionada", zonaLimpia);
        model.addAttribute("metrosIngresados", metros);
        model.addAttribute("habitaciones", habitaciones);
        model.addAttribute("banos", banos);
        model.addAttribute("nombreUsuario", user.getNombre());
        model.addAttribute("historial", prediccionRepo.findAll());
        
        return "index";
    }

    @GetMapping("/registro")
    public String mostrarRegistro() { return "registro"; }

    @PostMapping("/registro")
    public String registrarUsuario(@RequestParam String nombre, @RequestParam String email, @RequestParam String password, Model model) {
        usuarioRepo.save(new Usuario(nombre, email, password));
        model.addAttribute("mensajeExito", "¡Cuenta creada! Ya puedes entrar.");
        return "login";
    }
}