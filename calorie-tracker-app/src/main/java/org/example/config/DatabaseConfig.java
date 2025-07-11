package org.example.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import jakarta.persistence.EntityManagerFactory;

@Configuration
@EnableTransactionManagement
public class DatabaseConfig {

    @Value("${spring.datasource.primary.url}")
    private String primaryDbUrl;
    
    @Value("${spring.datasource.primary.username}")
    private String primaryDbUsername;
    
    @Value("${spring.datasource.primary.password}")
    private String primaryDbPassword;
    
    @Value("${spring.datasource.foodcategories.url}")
    private String foodCategoriesDbUrl;
    
    @Value("${spring.datasource.foodcategories.username}")
    private String foodCategoriesDbUsername;
    
    @Value("${spring.datasource.foodcategories.password}")
    private String foodCategoriesDbPassword;

    // Primary DataSource (athletes, coaches, days, foods, meals - NO FOOD CATEGORIES)
    @Primary
    @Bean(name = "primaryDataSource")
    public DataSource primaryDataSource() {
        return DataSourceBuilder.create()
                .driverClassName("org.postgresql.Driver")
                .url(primaryDbUrl)
                .username(primaryDbUsername)
                .password(primaryDbPassword)
                .build();
    }

    // Primary EntityManagerFactory - NO FOOD CATEGORIES
    @Primary
    @Bean(name = "primaryEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean primaryEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("primaryDataSource") DataSource dataSource) {
        
        java.util.Map<String, Object> properties = new java.util.HashMap<>();
        properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        properties.put("hibernate.hbm2ddl.auto", "update");
        properties.put("hibernate.show_sql", "true");
        
        return builder
                .dataSource(dataSource)
                .packages("org.example.model")
                .persistenceUnit("primary")
                .properties(properties)
                .build();
    }

    // Primary TransactionManager
    @Primary
    @Bean(name = "primaryTransactionManager")
    public PlatformTransactionManager primaryTransactionManager(
            @Qualifier("primaryEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

    // Food Categories DataSource - SECOND DATABASE
    @Bean(name = "foodCategoriesDataSource")
    public DataSource foodCategoriesDataSource() {
        return DataSourceBuilder.create()
                .driverClassName("org.postgresql.Driver")
                .url(foodCategoriesDbUrl)
                .username(foodCategoriesDbUsername)
                .password(foodCategoriesDbPassword)
                .build();
    }

    // Food Categories EntityManagerFactory - ONLY FOOD CATEGORIES
    @Bean(name = "foodCategoriesEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean foodCategoriesEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("foodCategoriesDataSource") DataSource dataSource) {
        
        java.util.Map<String, Object> properties = new java.util.HashMap<>();
        properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        properties.put("hibernate.hbm2ddl.auto", "update");
        properties.put("hibernate.show_sql", "true");
        
        return builder
                .dataSource(dataSource)
                .packages("org.example.foodcategories")
                .persistenceUnit("foodCategories")
                .properties(properties)
                .build();
    }

    // Food Categories TransactionManager
    @Bean(name = "foodCategoriesTransactionManager")
    public PlatformTransactionManager foodCategoriesTransactionManager(
            @Qualifier("foodCategoriesEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
} 