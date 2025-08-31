package com.importservice.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.importservice.dto.DepartmentMappingDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DepartmentUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(DepartmentUtils.class);
    
    private static final Map<String, String> departmentMappings = new HashMap<>();
    
    private final ObjectMapper objectMapper;
    
    public DepartmentUtils(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    @PostConstruct
    public void loadDepartmentMappings() {
        logger.info("Loading department mappings from departments.json");
        
        try {
            ClassPathResource resource = new ClassPathResource("departments.json");
            InputStream inputStream = resource.getInputStream();
            
            TypeReference<List<DepartmentMappingDto>> typeReference = 
                new TypeReference<List<DepartmentMappingDto>>() {};
            
            List<DepartmentMappingDto> mappings = objectMapper.readValue(inputStream, typeReference);
            
            // Clear existing mappings and populate with new data
            departmentMappings.clear();
            
            for (DepartmentMappingDto mapping : mappings) {
                if (mapping.getOldDepartmentGuid() != null && mapping.getDepartmentCode() != null) {
                    departmentMappings.put(mapping.getOldDepartmentGuid(), mapping.getDepartmentCode());
                    logger.debug("Loaded mapping: {} -> {}", 
                               mapping.getOldDepartmentGuid(), mapping.getDepartmentCode());
                } else {
                    logger.warn("Skipping invalid mapping: {}", mapping);
                }
            }
            
            logger.info("Successfully loaded {} department mappings", departmentMappings.size());
            
        } catch (IOException e) {
            logger.error("Failed to load department mappings from departments.json", e);
            throw new RuntimeException("Failed to initialize department mappings", e);
        }
    }
    
    /**
     * Get department code by old department GUID
     * 
     * @param oldDepartmentGuid The old department GUID to look up
     * @return The corresponding department code, or null if not found
     */
    public static String getDepartmentCodeByOldGuid(String oldDepartmentGuid) {
        if (oldDepartmentGuid == null || oldDepartmentGuid.trim().isEmpty()) {
            logger.debug("Null or empty oldDepartmentGuid provided");
            return null;
        }
        
        String departmentCode = departmentMappings.get(oldDepartmentGuid.trim());
        
        if (departmentCode == null) {
            logger.debug("No department code found for oldDepartmentGuid: {}", oldDepartmentGuid);
        } else {
            logger.debug("Found department code '{}' for oldDepartmentGuid: {}", 
                       departmentCode, oldDepartmentGuid);
        }
        
        return departmentCode;
    }
    
    /**
     * Check if a department mapping exists for the given old department GUID
     * 
     * @param oldDepartmentGuid The old department GUID to check
     * @return true if mapping exists, false otherwise
     */
    public static boolean hasDepartmentMapping(String oldDepartmentGuid) {
        return oldDepartmentGuid != null && 
               departmentMappings.containsKey(oldDepartmentGuid.trim());
    }
    
    /**
     * Get all department mappings (for debugging/monitoring purposes)
     * 
     * @return A copy of all department mappings
     */
    public static Map<String, String> getAllDepartmentMappings() {
        return new HashMap<>(departmentMappings);
    }
    
    /**
     * Get the total number of loaded department mappings
     * 
     * @return The count of loaded mappings
     */
    public static int getMappingsCount() {
        return departmentMappings.size();
    }
}