package com.importservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

@Configuration
@EnableJpaRepositories(basePackages = "com.importservice.repository")
@EnableTransactionManagement
public class JpaConfig {
    
    @Bean
    public JpaVendorAdapter jpaVendorAdapter() {
        HibernateJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
        adapter.setShowSql(false);
        adapter.setGenerateDdl(false);
        adapter.setDatabasePlatform("org.hibernate.dialect.MySQL8Dialect");
        return adapter;
    }
    
    @Bean
    public org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean entityManagerFactory(
            javax.sql.DataSource dataSource, JpaVendorAdapter jpaVendorAdapter) {
        org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean factory = 
            new org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(dataSource);
        factory.setJpaVendorAdapter(jpaVendorAdapter);
        factory.setPackagesToScan("com.importservice.entity");
        
        // Configure JPA properties for better concurrency
        java.util.Properties jpaProperties = new java.util.Properties();
        jpaProperties.put("hibernate.hbm2ddl.auto", "update");
        jpaProperties.put("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");
        jpaProperties.put("hibernate.show_sql", "false");
        jpaProperties.put("hibernate.format_sql", "false");
        
        // Disable batch processing completely
        jpaProperties.put("hibernate.jdbc.batch_size", "1");
        jpaProperties.put("hibernate.order_inserts", "false");
        jpaProperties.put("hibernate.order_updates", "false");
        jpaProperties.put("hibernate.jdbc.batch_versioned_data", "false");
        
        // Connection and transaction settings
        jpaProperties.put("hibernate.connection.autocommit", "false");
        jpaProperties.put("hibernate.connection.isolation", "2");
        jpaProperties.put("hibernate.connection.release_mode", "after_transaction");
        
        // Lock timeout settings
        jpaProperties.put("hibernate.dialect.lock_timeout", "10");
        jpaProperties.put("javax.persistence.lock.timeout", "10000");
        
        // Disable caching to prevent stale data issues
        jpaProperties.put("hibernate.cache.use_second_level_cache", "false");
        jpaProperties.put("hibernate.cache.use_query_cache", "false");
        
        factory.setJpaProperties(jpaProperties);
        return factory;
    }
}