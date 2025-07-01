package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication(
    scanBasePackages = {
        "org.example.config",
        "org.example.controller",
        "org.example.service",
        "org.example.repository",
        "org.example.foodcategories"
    }
)
@EntityScan(
    basePackageClasses = {
        org.example.model.Coach.class,  // Only Coach entity from primary database
        org.example.foodcategories.FoodCategory.class  // Food categories database entities
    }
)
public class FoodCategoriesAdminApp {
    public static void main(String[] args) {
        SpringApplication.run(FoodCategoriesAdminApp.class, args);
    }
} 