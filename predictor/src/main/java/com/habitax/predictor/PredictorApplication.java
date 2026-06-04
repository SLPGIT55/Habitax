package com.habitax.predictor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PredictorApplication {

    public static void main(String[] args) {
        // Mostrar dialogo ANTES de que Spring Boot inicialice
        StartupDialog.mostrar();

        SpringApplication.run(PredictorApplication.class, args);
    }
}