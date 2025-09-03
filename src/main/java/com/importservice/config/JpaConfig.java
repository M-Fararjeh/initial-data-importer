package com.importservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.annotation.PreDestroy;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Configuration
@EnableJpaRepositories(basePackages = "com.importservice.repository")
@EnableTransactionManagement
public class JpaConfig {

    @PersistenceContext
    private EntityManager entityManager;
    
    @Bean
    public JpaVendorAdapter jpaVendorAdapter() {
        HibernateJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
        adapter.setShowSql(false);
        adapter.setGenerateDdl(false);
        adapter.setDatabasePlatform("org.hibernate.dialect.MySQL8Dialect");
        return adapter;
    }

    /**
     * Cleanup method to ensure proper resource cleanup on shutdown
     */
    @PreDestroy
    public void cleanup() {
        if (entityManager != null && entityManager.isOpen()) {
            entityManager.close();
        }
    }
}