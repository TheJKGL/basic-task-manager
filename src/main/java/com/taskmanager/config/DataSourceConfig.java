package com.taskmanager.config;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;

import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Configuration
@EnableJpaRepositories(basePackages = "com.taskmanager",
        transactionManagerRef = "transcationManager",
        entityManagerFactoryRef = "entityManager")
@EnableTransactionManagement
public class DataSourceConfig {

    @Bean(name = "entityManager")
    public LocalContainerEntityManagerFactoryBean entityManagerFactoryBean(EntityManagerFactoryBuilder builder) {

        return builder
                .dataSource(dataSource(primaryDataSource(), secondaryDataSource()))
                .packages("com.taskmanager.model")
                .build();
    }

    @Bean(name = "transcationManager")
    public JpaTransactionManager transactionManager(
            @Autowired @Qualifier("entityManager") LocalContainerEntityManagerFactoryBean entityManagerFactoryBean) {
        return new JpaTransactionManager(Objects.requireNonNull(entityManagerFactoryBean.getObject()));
    }

    @Primary
    @Bean
    @ConfigurationProperties("spring.datasource.primary")
    public DataSourceProperties primaryDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties("spring.datasource.secondary")
    public DataSourceProperties secondaryDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Primary
    @Bean
    public DataSource primaryDataSource() {
        return primaryDataSourceProperties()
                .initializeDataSourceBuilder()
                .build();
    }
    @Bean
    public DataSource secondaryDataSource() {
        return secondaryDataSourceProperties()
                .initializeDataSourceBuilder()
                .build();
    }

    @Bean
    public DataSource dataSource(@Qualifier("primaryDataSource") DataSource primaryDataSource,
                                 @Qualifier("secondaryDataSource") DataSource secondaryDataSource) {
        AbstractRoutingDataSource routingDataSource = new AbstractRoutingDataSource() {
            @Override
            protected Object determineCurrentLookupKey() {
                // Logic to determine whether to use main or reserve DataSource
                try {
                    primaryDataSource.getConnection().isValid(0);
                    System.out.println("Postgres connected");

                    executeFlywayMigration(primaryDataSource, "db/migration/postgres");
                    return "primary";  // If the main DB is available
                } catch (Exception e) {
                    System.out.println(e);
                    System.out.println("H2 connected");

                    executeFlywayMigration(secondaryDataSource, "db/migration/h2");
                    return "secondary";  // If the main DB fails, use reserve DB
                }
            }
        };

        Map<Object, Object> dataSources = new HashMap<>();
        dataSources.put("primary", primaryDataSource);
        dataSources.put("secondary", secondaryDataSource);
        routingDataSource.setTargetDataSources(dataSources);
        routingDataSource.setDefaultTargetDataSource(primaryDataSource);

        return routingDataSource;
    }

    private void executeFlywayMigration(DataSource dataSource, String location) {
        Flyway.configure()
                .dataSource(dataSource)
                .validateMigrationNaming(true)
                .baselineOnMigrate(true)
                .locations(location)
                .load()
                .migrate();
    }
}
