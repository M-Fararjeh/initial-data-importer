package com.importservice.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.importservice.dto.AgencyMappingDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AgencyMappingUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(AgencyMappingUtils.class);
    
    private static final Map<String, String> agencyMappings = new HashMap<>();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    static {
        loadAgencyMappings();
    }
    
    /**
     * Loads agency mappings from externalAgencies.json
     */
    private static void loadAgencyMappings() {
        logger.info("Loading agency mappings from agency mapping.json");
        
        try {
            ClassPathResource resource = new ClassPathResource("agency mapping.json");
            InputStream inputStream = resource.getInputStream();
            
            TypeReference<List<AgencyMappingDto>> typeReference = 
                new TypeReference<List<AgencyMappingDto>>() {};
            
            List<AgencyMappingDto> agencies = objectMapper.readValue(inputStream, typeReference);
            
            // Clear existing mappings and populate with new data
            agencyMappings.clear();
            
            for (AgencyMappingDto agency : agencies) {
                if (agency.getAgencyGuid() != null && agency.getAgencyCode() != null) {
                    // Format agency code to 3 digits with leading zeros
                    String formattedAgencyCode = String.format("%03d", agency.getAgencyCode());
                    
                    // Map agency GUID to formatted agency code
                    agencyMappings.put(agency.getAgencyGuid(), formattedAgencyCode);
                    
                    logger.debug("Loaded agency mapping: {} -> {}", agency.getAgencyGuid(), formattedAgencyCode);
                } else {
                    logger.warn("Skipping invalid agency mapping: AgencyGUID={}, AgencyCode={}", 
                              agency.getAgencyGuid(), agency.getAgencyCode());
                }
            }
            
            logger.info("Successfully loaded {} agency mappings", agencyMappings.size());
            
        } catch (IOException e) {
            logger.error("Failed to load agency mappings from agency mapping.json", e);
            // Add some default mappings as fallback
            agencyMappings.put("1a47e08f-b054-4eee-b426-d6c8fbb997a3", "001");
            logger.warn("Using fallback agency mappings due to file loading error");
        }
    }
    
    /**
     * Maps agency GUID to agency code
     * 
     * @param agencyGuid The agency GUID from correspondence
     * @return Agency code for destination system
     */
    public static String mapAgencyGuidToCode(String agencyGuid) {
        if (agencyGuid == null || agencyGuid.trim().isEmpty()) {
            logger.debug("Null or empty agencyGuid provided, returning default 'out1'");
            return "out1";
        }
        
        // Try direct lookup first
        String agencyCode = agencyMappings.get(agencyGuid.trim());
        
        if (agencyCode != null) {
            logger.debug("Found agency code '{}' for agencyGuid '{}'", agencyCode, agencyGuid);
            return agencyCode;
        }
        
        logger.debug("No agency code found for agencyGuid '{}', returning default '001'", agencyGuid);
        return "001"; // Default fallback
    }
    
    /**
     * Gets all agency mappings (for debugging/monitoring purposes)
     * 
     * @return A copy of all agency mappings
     */
    public static Map<String, String> getAllAgencyMappings() {
        return new HashMap<>(agencyMappings);
    }
    
    /**
     * Gets the total number of loaded agency mappings
     * 
     * @return The count of loaded mappings
     */
    public static int getMappingsCount() {
        return agencyMappings.size();
    }
    
    /**
     * Reloads agency mappings from file
     */
    public static void reloadMappings() {
        loadAgencyMappings();
    }
}