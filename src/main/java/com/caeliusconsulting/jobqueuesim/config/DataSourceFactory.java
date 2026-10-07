package com.caeliusconsulting.jobqueuesim.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public final class DataSourceFactory {
    private DataSourceFactory() { }

    public static HikariDataSource create(AppConfig config) {
        HikariConfig pool = new HikariConfig();
        pool.setJdbcUrl(config.dbUrl());
        pool.setUsername(config.dbUser());
        pool.setPassword(config.dbPassword());
        pool.setMaximumPoolSize(config.dbPoolSize());
        pool.setMinimumIdle(1);
        pool.setConnectionTimeout(5000);
        pool.setPoolName("job-database");
        pool.addDataSourceProperty("connectTimeout", "5000");
        pool.addDataSourceProperty("socketTimeout", "5000");
        return new HikariDataSource(pool);
    }
}
