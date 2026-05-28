package com.roomate.app.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Map;

/**
 * Second datasource for the personal budget database.
 * This database is shared with n8n — both the backend and n8n read/write to it.
 * Uses ddl-auto: update so the schema is auto-created if missing.
 */
@Configuration
@EnableJpaRepositories(
        basePackages = "com.roomate.app.repository.budget",
        entityManagerFactoryRef = "budgetEntityManagerFactory",
        transactionManagerRef = "budgetTransactionManager"
)
public class BudgetDataSourceConfig {

    @Bean
    @ConfigurationProperties("budget.datasource")
    public DataSourceProperties budgetDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource budgetDataSource() {
        return budgetDataSourceProperties()
                .initializeDataSourceBuilder()
                .build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean budgetEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("budgetDataSource") DataSource dataSource) {
        return builder
                .dataSource(dataSource)
                .packages("com.roomate.app.entities.budget")
                .persistenceUnit("budget")
                .properties(Map.of(
                        "hibernate.hbm2ddl.auto", "update",
                        "hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect",
                        "hibernate.globally_quoted_identifiers", "true",
                        "hibernate.format_sql", "true"
                ))
                .build();
    }

    @Bean
    public PlatformTransactionManager budgetTransactionManager(
            @Qualifier("budgetEntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}
