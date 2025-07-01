package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@SpringBootApplication(
    scanBasePackages = {
        "org.example.config",
        "org.example.controller",
        "org.example.service",
        "org.example.repository"
    }
)
@EnableJpaRepositories(
    basePackages = {"org.example.repository", "org.example.repository.foodcategories"}
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