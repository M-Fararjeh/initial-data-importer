package com.importservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.annotation.PreDestroy;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Configuration
@EnableJpaRepositories(basePackages = "com.importservice.repository")
@EnableTransactionManagement
public class JpaConfig {

    @PersistenceContext
    private EntityManager entityManager;

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