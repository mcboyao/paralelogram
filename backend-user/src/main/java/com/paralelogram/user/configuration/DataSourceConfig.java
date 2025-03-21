package com.paralelogram.user.configuration;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    @Value("${PG_HOST}")
    private String pgHost;

    @Value("${PG_PORT}")
    private String pgPort;

    @Value("${PG_DATABASE}")
    private String pgDatabase;

    @Value("${PG_SCHEMA}")
    private String pgSchema;

    @Value("${PG_USERNAME}")
    private String pgUsername;

    @Value("${PG_PASSWORD}")
    private String pgPassword;

    @Value("${spring.datasource.hikari.maximum-pool-size}")
    private int maximumPoolSize;

    @Value("${spring.datasource.hikari.minimum-idle}")
    private int minimumIdle;

    @Bean
    public DataSource getDataSource() {
        HikariDataSource dataSource = DataSourceBuilder.create().type(HikariDataSource.class).build();
        dataSource.setJdbcUrl("jdbc:postgresql://" + pgHost + ":" + pgPort + "/" + pgDatabase + "?currentSchema=" + pgSchema);
        dataSource.setUsername(pgUsername);
        dataSource.setPassword(pgPassword);
        dataSource.setMaximumPoolSize(maximumPoolSize);
        dataSource.setMinimumIdle(minimumIdle);
        return dataSource;
    }

}
