package com.importservice.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
public class DatabaseConfig {

    @Value("${spring.datasource.url}")
    private String jdbcUrl;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;

    @Bean
    @Primary
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        
        // Basic connection settings
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName(driverClassName);
        
        // Optimized pool settings for better performance
        config.setMaximumPoolSize(15);  // Increased for better concurrency
        config.setMinimumIdle(5);
        config.setConnectionTimeout(30000); // 30 seconds
        config.setIdleTimeout(600000); // 10 minutes
        config.setMaxLifetime(1800000); // 30 minutes
        config.setLeakDetectionThreshold(180000); // 3 minutes
        
        // Connection validation
        config.setValidationTimeout(5000);
        config.setConnectionTestQuery("SELECT 1");
        
        // Auto-commit and transaction settings
        config.setAutoCommit(false); // Disable auto-commit for proper transaction management
        
        // Pool name for monitoring - use unique name to avoid conflicts
        config.setPoolName("DataImportHikariCP-" + System.currentTimeMillis());
        
        // Disable MBean registration to avoid conflicts
        config.setRegisterMbeans(false);
        
        // Connection initialization
        config.setInitializationFailTimeout(10000);
        
        // Additional MySQL-specific settings for better performance
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("useLocalSessionState", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        config.addDataSourceProperty("cacheServerConfiguration", "true");
        config.addDataSourceProperty("elideSetAutoCommits", "true");
        config.addDataSourceProperty("maintainTimeStats", "false");
        config.addDataSourceProperty("useLocalTransactionState", "true");
        config.addDataSourceProperty("readOnlyPropagatesToServer", "true");
        config.addDataSourceProperty("enableQueryTimeouts", "true");
        
        return new HikariDataSource(config);
    }
}