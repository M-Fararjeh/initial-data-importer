package com.importservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.TransactionManagementConfigurer;

import javax.persistence.EntityManagerFactory;

@Configuration
@EnableTransactionManagement
public class TransactionConfig implements TransactionManagementConfigurer {

    @Bean
    @Override
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory);
        
        // Set reasonable transaction timeout
        transactionManager.setDefaultTimeout(300); // 5 minutes for migration operations
        
        // Enable rollback on commit failure
        transactionManager.setRollbackOnCommitFailure(true);
        
        // Set nested transaction allowed
        transactionManager.setNestedTransactionAllowed(true);
        
        return transactionManager;
    }
}