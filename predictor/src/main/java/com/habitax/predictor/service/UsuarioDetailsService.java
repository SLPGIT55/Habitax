package com.habitax.predictor.service;

import java.util.Collections;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.habitax.predictor.model.Usuario;
import com.habitax.predictor.repository.UsuarioRepository;

@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepo;

    public UsuarioDetailsService(UsuarioRepository usuarioRepo) {
        this.usuarioRepo = usuarioRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Buscamos el usuario en vuestra base de datos H2 por email
        Usuario usuario = usuarioRepo.findByEmail(email);
        if (usuario == null) {
            throw new UsernameNotFoundException("Usuario no encontrado con el email: " + email);
        }

        // Le devolvemos a Spring Security el usuario con su contraseña encriptada para que valide el login solo
        return new User(
                usuario.getEmail(),
                usuario.getPassword(),
                Collections.emptyList() // Sin roles complejos para no liar el proyecto
        );
    }
}