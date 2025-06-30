package org.example.config;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EntityScan(basePackages = "org.example.foodcategories")
@EnableJpaRepositories(
    basePackages = "org.example.repository.foodcategories",
    entityManagerFactoryRef = "foodCategoriesEntityManagerFactory",
    transactionManagerRef = "foodCategoriesTransactionManager"
)
public class FoodCategoriesConfig {

    @Bean
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties foodCategoriesDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource foodCategoriesDataSource() {
        return foodCategoriesDataSourceProperties().initializeDataSourceBuilder().build();
    }

    @Bean(name = "foodCategoriesEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean foodCategoriesEntityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(foodCategoriesDataSource());
        em.setPackagesToScan("org.example.foodcategories");
        
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "none");
        properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        em.setJpaPropertyMap(properties);
        
        return em;
    }

    @Bean(name = "foodCategoriesTransactionManager")
    public PlatformTransactionManager foodCategoriesTransactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(foodCategoriesEntityManagerFactory().getObject());
        return transactionManager;
    }
} 