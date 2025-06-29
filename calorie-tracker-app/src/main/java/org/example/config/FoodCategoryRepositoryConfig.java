package org.example.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.PlatformTransactionManager;

import jakarta.persistence.EntityManagerFactory;

@Configuration
@EnableJpaRepositories(
    basePackages = {
        "org.example.repository.foodcategories"
    },
    entityManagerFactoryRef = "foodCategoriesEntityManagerFactory",
    transactionManagerRef = "foodCategoriesTransactionManager"
)
public class FoodCategoryRepositoryConfig {
} 