package com.importservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;

@Configuration
@EnableTransactionManagement
public class TransactionConfig {

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory);
        
        // Set reasonable transaction timeout (5 minutes for migration operations)
        transactionManager.setDefaultTimeout(300);
        
        // Enable rollback on commit failure
        transactionManager.setRollbackOnCommitFailure(true);
        
        // Disable nested transactions to avoid complexity
        transactionManager.setNestedTransactionAllowed(false);
        
        // Ensure proper connection release
        transactionManager.setValidateExistingTransaction(true);
        
        // Disable global rollback on participation failure for better performance
        transactionManager.setGlobalRollbackOnParticipationFailure(false);
        
        return transactionManager;
    }
}