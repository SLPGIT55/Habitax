package com.habitax.predictor;

import com.habitax.predictor.controller.StartupController;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class PredictorApplication {

    public static void main(String[] args) {
        // Mostrar dialogo ANTES de que Spring Boot inicialice
        StartupController.mostrar();

        SpringApplication.run(PredictorApplication.class, args);
    }

    // Dejamos el encriptador libre para que lo uses en tu HabitaxController.java
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}