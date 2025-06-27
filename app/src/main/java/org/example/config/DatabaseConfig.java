package org.example.config;

import org.springframework.beans.factory.annotation.Qualifier;
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

    // Primary DataSource (Main App - athletes, coaches, days, foods, meals)
    @Primary
    @Bean(name = "primaryDataSource")
    public DataSource primaryDataSource() {
        return DataSourceBuilder.create()
                .driverClassName("org.postgresql.Driver")
                .url("jdbc:postgresql://ep-cold-violet-a98cycpm-pooler.gwc.azure.neon.tech/neondb?sslmode=require&channel_binding=require")
                .username("neondb_owner")
                .password("npg_8EdaAKTp9qoP")
                .build();
    }

    // Primary EntityManagerFactory
    @Primary
    @Bean(name = "primaryEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean primaryEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("primaryDataSource") DataSource dataSource) {
        
        java.util.Map<String, Object> properties = new java.util.HashMap<>();
        properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        properties.put("hibernate.hbm2ddl.auto", "none");
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

    // Food Categories DataSource
    @Bean(name = "foodCategoriesDataSource")
    public DataSource foodCategoriesDataSource() {
        return DataSourceBuilder.create()
                .driverClassName("org.postgresql.Driver")
                .url("jdbc:postgresql://ep-empty-math-a9w5gft6-pooler.gwc.azure.neon.tech/neondb?sslmode=require&channel_binding=require")
                .username("neondb_owner")
                .password("npg_8EdaAKTp9qoP")
                .build();
    }

    // Food Categories EntityManagerFactory
    @Bean(name = "foodCategoriesEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean foodCategoriesEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("foodCategoriesDataSource") DataSource dataSource) {
        
        java.util.Map<String, Object> properties = new java.util.HashMap<>();
        properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        properties.put("hibernate.hbm2ddl.auto", "none");
        properties.put("hibernate.show_sql", "true");
        
        return builder
                .dataSource(dataSource)
                .packages("org.example.model")
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