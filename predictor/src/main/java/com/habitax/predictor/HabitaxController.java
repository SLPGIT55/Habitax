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

    @PostMapping("/login")
    public String login(@RequestParam String email, @RequestParam String password, HttpSession session, Model model) {
        Usuario user = usuarioRepo.findByEmail(email);
        if (user != null && user.getPassword().equals(password)) {
            session.setAttribute("usuarioLogueado", user);
            return "redirect:/predictor";
        }
        model.addAttribute("error", "Credenciales incorrectas");
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    @GetMapping("/predictor")
    public String predictor(HttpSession session, Model model) {
        Usuario user = (Usuario) session.getAttribute("usuarioLogueado");
        if (user == null) return "redirect:/";
        
        model.addAttribute("nombreUsuario", user.getNombre());
        model.addAttribute("historial", prediccionRepo.findAll());
        return "index";
    }

    @PostMapping("/predecir")
    public String consultarIdealista(@RequestParam int metros, 
                                @RequestParam String zona, 
                                @RequestParam int habitaciones,
                                @RequestParam int banos,
                                HttpSession session, Model model) {
        
        Usuario user = (Usuario) session.getAttribute("usuarioLogueado");
        if (user == null) return "redirect:/";

        // Limpiamos la zona de espacios en blanco
        String zonaLimpia = zona.trim();
        String locationId = "";
        if (zonaLimpia.equalsIgnoreCase("Madrid")) locationId = "0-EU-ES-28-07-001-079";
        else if (zonaLimpia.equalsIgnoreCase("Barcelona")) locationId = "0-EU-ES-08-13-001-019";

        // Construimos la URL asegurando que los parámetros numéricos van como texto
        String url = "https://idealista7.p.rapidapi.com/listhomes?operation=sale&propertyType=homes&locationName=" + zonaLimpia + 
                    "&locationId=" + locationId + "&rooms=" + String.valueOf(habitaciones) + 
                    "&bathrooms=" + String.valueOf(banos) + "&numPage=1&maxItems=40&location=es&locale=es";
        
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
                // Si no hay casas exactas, el cálculo local es: metros * 3900 (precio medio Madrid/Barna)
                resultadoFinal = metros * 3900.0;
            }
        } catch (Exception e) {
            // Si la API falla por cuota (Error 429), usamos un cálculo local digno
            resultadoFinal = metros * 3750.0;
            System.out.println("ERROR DE RED O CUOTA: " + e.getMessage());
        }

        // Guardar y mostrar (Esto SIEMPRE funcionará aunque la API falle)
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