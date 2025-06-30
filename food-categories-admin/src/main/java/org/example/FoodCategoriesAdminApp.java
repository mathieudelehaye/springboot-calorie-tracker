package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(
    scanBasePackages = {
        "org.example.config",
        "org.example.controller",
        "org.example.service",
        "org.example.repository"
    }
)
public class FoodCategoriesAdminApp {
    public static void main(String[] args) {
        SpringApplication.run(FoodCategoriesAdminApp.class, args);
    }
} 