package com.importservice.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
public class ThreadPoolConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(ThreadPoolConfig.class);
    
    @Value("${migration.creation.thread-pool-size:5}")
    private int corePoolSize;
    
    @Value("${migration.creation.max-pool-size:10}")
    private int maxPoolSize;
    
    @Value("${migration.creation.queue-capacity:50}")
    private int queueCapacity;
    
    @Value("${migration.creation.keep-alive-seconds:60}")
    private int keepAliveSeconds;
    
    @Value("${migration.creation.thread-name-prefix:creation-thread-}")
    private String threadNamePrefix;
    
    @Value("${migration.creation.await-termination-seconds:60}")
    private int awaitTerminationSeconds;
    
    @Bean(name = "creationTaskExecutor")
    public Executor creationTaskExecutor() {
        logger.info("Creating thread pool for creation phase with core size: {}, max size: {}, queue capacity: {}", 
                   corePoolSize, maxPoolSize, queueCapacity);
        
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setKeepAliveSeconds(keepAliveSeconds);
        executor.setThreadNamePrefix(threadNamePrefix);
        executor.setAwaitTerminationSeconds(awaitTerminationSeconds);
        
        // Rejection policy: CallerRunsPolicy ensures that if the thread pool is full,
        // the calling thread will execute the task, preventing task loss
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        // Wait for all tasks to complete before shutdown
        executor.setWaitForTasksToCompleteOnShutdown(true);
        
        executor.initialize();
        
        logger.info("Creation thread pool initialized successfully");
        return executor;
    }
}