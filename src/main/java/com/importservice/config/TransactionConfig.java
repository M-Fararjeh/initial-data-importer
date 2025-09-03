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
        
        // Set shorter transaction timeout to prevent long-running locks
        transactionManager.setDefaultTimeout(60);
        
        // Enable rollback on commit failure
        transactionManager.setRollbackOnCommitFailure(true);
        
        // Enable nested transactions for better isolation
        transactionManager.setNestedTransactionAllowed(true);
        
        // Ensure proper connection release
        transactionManager.setValidateExistingTransaction(true);
        
        // Enable global rollback for consistency
        transactionManager.setGlobalRollbackOnParticipationFailure(true);
        
        // Fail fast on transaction issues
        transactionManager.setFailEarlyOnGlobalRollbackOnly(true);
        
        return transactionManager;
    }
}