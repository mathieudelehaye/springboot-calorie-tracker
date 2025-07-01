package org.example.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.retry.annotation.EnableRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import jakarta.persistence.EntityManagerFactory;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableTransactionManagement
@EnableRetry
@EnableJpaRepositories(
    basePackages = {"org.example.repository"},
    excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = "org.example.repository.foodcategories.*"),
    entityManagerFactoryRef = "primaryEntityManagerFactory",
    transactionManagerRef = "primaryTransactionManager"
)
public class AdminDatabaseConfig {
    private static final Logger logger = LoggerFactory.getLogger(AdminDatabaseConfig.class);

    @Value("${spring.datasource.primary.url}")
    private String primaryDbUrl;
    
    @Value("${spring.datasource.primary.username}")
    private String primaryDbUsername;
    
    @Value("${spring.datasource.primary.password}")
    private String primaryDbPassword;

    @Value("${spring.datasource.url}")
    private String foodCategoriesDbUrl;
    
    @Value("${spring.datasource.username}")
    private String foodCategoriesDbUsername;
    
    @Value("${spring.datasource.password}")
    private String foodCategoriesDbPassword;

    // Primary DataSource (for coaches authentication and main app entities)
    @Primary
    @Bean(name = "primaryDataSource")
    public DataSource primaryDataSource() {
        try {
            String driverClassName = primaryDbUrl.startsWith("jdbc:h2:") ? "org.h2.Driver" : "org.postgresql.Driver";
            DataSource dataSource = DataSourceBuilder.create()
                    .driverClassName(driverClassName)
                    .url(primaryDbUrl)
                    .username(primaryDbUsername)
                    .password(primaryDbPassword)
                    .build();
            
            // Test the connection
            dataSource.getConnection().close();
            logger.info("Successfully configured primary database connection");
            return dataSource;
        } catch (Exception e) {
            logger.error("Failed to initialize primary database connection: {}", e.getMessage(), e);
            // Still return the datasource - let the application handle connection issues at runtime
            String driverClassName = primaryDbUrl.startsWith("jdbc:h2:") ? "org.h2.Driver" : "org.postgresql.Driver";
            return DataSourceBuilder.create()
                    .driverClassName(driverClassName)
                    .url(primaryDbUrl)
                    .username(primaryDbUsername)
                    .password(primaryDbPassword)
                    .build();
        }
    }

    // Food Categories DataSource
    @Bean(name = "foodCategoriesDataSource")
    public DataSource foodCategoriesDataSource() {
        try {
            String driverClassName = foodCategoriesDbUrl.startsWith("jdbc:h2:") ? "org.h2.Driver" : "org.postgresql.Driver";
            DataSource dataSource = DataSourceBuilder.create()
                    .driverClassName(driverClassName)
                    .url(foodCategoriesDbUrl)
                    .username(foodCategoriesDbUsername)
                    .password(foodCategoriesDbPassword)
                    .build();
            
            // Test the connection
            dataSource.getConnection().close();
            logger.info("Successfully configured food categories database connection");
            return dataSource;
        } catch (Exception e) {
            logger.error("Failed to initialize food categories database connection: {}", e.getMessage(), e);
            // Still return the datasource - let the application handle connection issues at runtime
            String driverClassName = foodCategoriesDbUrl.startsWith("jdbc:h2:") ? "org.h2.Driver" : "org.postgresql.Driver";
            return DataSourceBuilder.create()
                    .driverClassName(driverClassName)
                    .url(foodCategoriesDbUrl)
                    .username(foodCategoriesDbUsername)
                    .password(foodCategoriesDbPassword)
                    .build();
        }
    }

    // Primary Entity Manager Factory (for Coach, Athletes, Days, Meals, Foods)
    @Primary
    @Bean(name = "primaryEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean primaryEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("primaryDataSource") DataSource primaryDataSource) {
        
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.dialect", primaryDbUrl.startsWith("jdbc:h2:") ? 
            "org.hibernate.dialect.H2Dialect" : "org.hibernate.dialect.PostgreSQLDialect");
        properties.put("hibernate.hbm2ddl.auto", "create-drop");
        properties.put("hibernate.show_sql", "true");
        properties.put("hibernate.temp.use_jdbc_metadata_defaults", "false");
        
        return builder
                .dataSource(primaryDataSource)
                .packages("org.example.model") // All main entities including Coach
                .persistenceUnit("primary")
                .properties(properties)
                .build();
    }

    // Food Categories Entity Manager Factory 
    @Bean(name = "foodCategoriesEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean foodCategoriesEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("foodCategoriesDataSource") DataSource foodCategoriesDataSource) {
        
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.dialect", foodCategoriesDbUrl.startsWith("jdbc:h2:") ? 
            "org.hibernate.dialect.H2Dialect" : "org.hibernate.dialect.PostgreSQLDialect");
        properties.put("hibernate.hbm2ddl.auto", "create-drop");
        properties.put("hibernate.show_sql", "true");
        properties.put("hibernate.temp.use_jdbc_metadata_defaults", "false");
        
        return builder
                .dataSource(foodCategoriesDataSource)
                .packages("org.example.foodcategories") // FoodCategory entities
                .persistenceUnit("foodcategories")
                .properties(properties)
                .build();
    }

    // Primary Transaction Manager
    @Primary
    @Bean(name = "primaryTransactionManager")
    public PlatformTransactionManager primaryTransactionManager(
            @Qualifier("primaryEntityManagerFactory") EntityManagerFactory primaryEntityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager(primaryEntityManagerFactory);
        logger.info("Configured primary transaction manager for authentication");
        return transactionManager;
    }

    // Food Categories Transaction Manager
    @Bean(name = "foodCategoriesTransactionManager")
    public PlatformTransactionManager foodCategoriesTransactionManager(
            @Qualifier("foodCategoriesEntityManagerFactory") EntityManagerFactory foodCategoriesEntityManagerFactory) {
        return new JpaTransactionManager(foodCategoriesEntityManagerFactory);
    }
} 