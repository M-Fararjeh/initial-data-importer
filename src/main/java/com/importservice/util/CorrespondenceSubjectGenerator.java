package com.importservice.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Utility class for generating random correspondence subjects
 */
@Component
public class CorrespondenceSubjectGenerator {
    
    private static final Logger logger = LoggerFactory.getLogger(CorrespondenceSubjectGenerator.class);
    
    @Value("${correspondence.random-subject.enabled:false}")
    private boolean randomSubjectEnabled;
    
    @Value("${correspondence.random-subject.prefix:AUTO-}")
    private String subjectPrefix;
    
    @Value("${correspondence.random-subject.templates:Important Document,Official Communication,Business Matter}")
    private String subjectTemplatesString;
    
    private final Random random = new Random();
    
    /**
     * Generates a random subject for correspondence if enabled
     * 
     * @param originalSubject The original subject from source system
     * @return Generated subject if enabled, otherwise original subject
     */
    public String generateSubject(String originalSubject) {
        if (!randomSubjectEnabled) {
            logger.debug("Random subject generation is disabled, returning original subject");
            return originalSubject != null ? originalSubject : "";
        }
        
        String generatedSubject = generateRandomSubject();
        logger.info("Generated random subject (replacing original '{}'): {}", 
                   originalSubject != null ? originalSubject : "null", generatedSubject);
        return generatedSubject;
    }
    
    /**
     * Generates a completely random subject
     * 
     * @return Random subject string
     */
    public String generateRandomSubject() {
        try {
            List<String> templates = getSubjectTemplates();
            String template = templates.get(random.nextInt(templates.size()));
            
            // Add timestamp and unique identifier
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmm"));
            String uniqueId = UUID.randomUUID().toString().substring(0, 8);
            
            String subject = String.format("%s%s - %s - %s", 
                                         subjectPrefix, 
                                         template, 
                                         timestamp, 
                                         uniqueId);
            
            logger.debug("Generated random subject: {}", subject);
            return subject;
            
        } catch (Exception e) {
            logger.error("Error generating random subject", e);
            return subjectPrefix + "Generated Subject - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmm"));
        }
    }
    
    /**
     * Gets the list of subject templates from configuration
     * 
     * @return List of subject templates
     */
    private List<String> getSubjectTemplates() {
        if (subjectTemplatesString == null || subjectTemplatesString.trim().isEmpty()) {
            return Arrays.asList("Important Document", "Official Communication", "Business Matter");
        }
        
        return Arrays.asList(subjectTemplatesString.split(","));
    }
    
    /**
     * Checks if random subject generation is enabled
     * 
     * @return true if enabled, false otherwise
     */
    public boolean isRandomSubjectEnabled() {
        return randomSubjectEnabled;
    }
    
    /**
     * Gets the configured subject prefix
     * 
     * @return Subject prefix
     */
    public String getSubjectPrefix() {
        return subjectPrefix;
    }
    
    /**
     * Generates a subject with specific category
     * 
     * @param category The category to include in subject
     * @return Generated subject with category
     */
    public String generateSubjectWithCategory(String category) {
        if (!randomSubjectEnabled) {
            return "";
        }
        
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmm"));
            String uniqueId = UUID.randomUUID().toString().substring(0, 6);
            
            String subject = String.format("%s%s - %s - %s", 
                                         subjectPrefix, 
                                         category != null ? category : "General", 
                                         timestamp, 
                                         uniqueId);
            
            logger.debug("Generated subject with category {}: {}", category, subject);
            return subject;
            
        } catch (Exception e) {
            logger.error("Error generating subject with category: {}", category, e);
            return subjectPrefix + "Generated Subject - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmm"));
        }
    }
    
    /**
     * Generates a subject based on correspondence type
     * 
     * @param correspondenceType The type of correspondence
     * @param priority The priority level
     * @return Generated subject based on type and priority
     */
    public String generateSubjectByType(String correspondenceType, String priority) {
        if (!randomSubjectEnabled) {
            return "";
        }
        
        try {
            String priorityPrefix = "";
            if ("H".equals(priority)) {
                priorityPrefix = "[HIGH] ";
            } else if ("C".equals(priority)) {
                priorityPrefix = "[CRITICAL] ";
            }
            
            String typePrefix = correspondenceType != null ? correspondenceType + " - " : "";
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmm"));
            String uniqueId = UUID.randomUUID().toString().substring(0, 6);
            
            String subject = String.format("%s%s%s%s - %s", 
                                         priorityPrefix,
                                         subjectPrefix, 
                                         typePrefix,
                                         timestamp, 
                                         uniqueId);
            
            logger.debug("Generated subject by type {}: {}", correspondenceType, subject);
            return subject;
            
        } catch (Exception e) {
            logger.error("Error generating subject by type: {}", correspondenceType, e);
            return subjectPrefix + "Generated Subject - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmm"));
        }
    }
}