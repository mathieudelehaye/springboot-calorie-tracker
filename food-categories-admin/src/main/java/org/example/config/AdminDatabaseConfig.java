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
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import jakarta.persistence.EntityManagerFactory;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableTransactionManagement
@EnableRetry
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

    // Primary DataSource (for coaches authentication)
    @Primary
    @Bean(name = "primaryDataSource")
    public DataSource primaryDataSource() {
        try {
            DataSource dataSource = DataSourceBuilder.create()
                    .driverClassName("org.postgresql.Driver")
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
            return DataSourceBuilder.create()
                    .driverClassName("org.postgresql.Driver")
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
            DataSource dataSource = DataSourceBuilder.create()
                    .driverClassName("org.postgresql.Driver")
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
            return DataSourceBuilder.create()
                    .driverClassName("org.postgresql.Driver")
                    .url(foodCategoriesDbUrl)
                    .username(foodCategoriesDbUsername)
                    .password(foodCategoriesDbPassword)
                    .build();
        }
    }

    @Primary
    @Bean(name = "entityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("primaryDataSource") DataSource primaryDataSource,
            @Qualifier("foodCategoriesDataSource") DataSource foodCategoriesDataSource) {
        
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        properties.put("hibernate.hbm2ddl.auto", "none");
        properties.put("hibernate.show_sql", "true");
        properties.put("hibernate.temp.use_jdbc_metadata_defaults", "false");
        
        // Create a routing data source that will handle both databases
        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put("primary", primaryDataSource);
        targetDataSources.put("foodCategories", foodCategoriesDataSource);
        
        AbstractRoutingDataSource routingDataSource = new AbstractRoutingDataSource() {
            @Override
            protected Object determineCurrentLookupKey() {
                String currentPackage = "";
                StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
                for (StackTraceElement element : stackTrace) {
                    if (element.getClassName().startsWith("org.example")) {
                        currentPackage = element.getClassName();
                        break;
                    }
                }
                
                // Use Coach authentication from primary database
                if (currentPackage.contains("org.example.model") || currentPackage.contains("org.example.service.CoachUserDetailsService")) {
                    logger.debug("Routing to primary database for coach authentication - " + currentPackage);
                    return "primary";
                }
                
                // Use food categories from secondary database
                if (currentPackage.contains("org.example.foodcategories")) {
                    logger.debug("Routing to food categories database - " + currentPackage);
                    return "foodCategories";
                }
                
                // Default to primary for any other case
                logger.debug("Routing to primary database (default) - " + currentPackage);
                return "primary";
            }
        };
        routingDataSource.setTargetDataSources(targetDataSources);
        routingDataSource.setDefaultTargetDataSource(primaryDataSource);
        routingDataSource.afterPropertiesSet();
        
        return builder
                .dataSource(routingDataSource)
                .packages(
                    "org.example.model",           // For Coach entity
                    "org.example.foodcategories"   // For FoodCategory entity
                )
                .persistenceUnit("primary")
                .properties(properties)
                .build();
    }

    @Primary
    @Bean(name = "transactionManager")
    public PlatformTransactionManager transactionManager(
            @Qualifier("entityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
} 