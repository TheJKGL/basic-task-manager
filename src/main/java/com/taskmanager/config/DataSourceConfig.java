package com.taskmanager.config;

import com.taskmanager.exception.advice.GlobalExceptionHandler;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(DataSourceConfig.class);

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
                try {
                    primaryDataSource.getConnection().isValid(0);
                    logger.info("Connected to primary datasource Postgres");
                    return "primary";
                } catch (Exception e) {
                    logger.info("Connected to secondary datasource H2");
                    return "secondary";
                }
            }
        };

        try {
            primaryDataSource.getConnection().isValid(2);
            executeFlywayMigration(primaryDataSource, "db/migration/postgres");
            executeFlywayMigration(secondaryDataSource, "db/migration/h2");
        } catch (Exception e) {
            executeFlywayMigration(secondaryDataSource, "db/migration/h2");
        }

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
