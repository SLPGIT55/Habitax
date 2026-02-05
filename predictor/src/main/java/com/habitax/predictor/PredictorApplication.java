package com.habitax.predictor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackageClasses = HabitaxController.class) 
public class PredictorApplication {
    public static void main(String[] args) {
        SpringApplication.run(PredictorApplication.class, args);
    }
}