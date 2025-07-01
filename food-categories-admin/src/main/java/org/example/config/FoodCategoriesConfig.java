package org.example.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(
    basePackages = "org.example.repository.foodcategories",
    entityManagerFactoryRef = "foodCategoriesEntityManagerFactory",
    transactionManagerRef = "foodCategoriesTransactionManager"
)
public class FoodCategoriesConfig {
    // This configuration ensures that repositories in the foodcategories package
    // use the food categories database
} 