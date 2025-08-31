package com.importservice.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.importservice.dto.DepartmentMappingDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    
    private static Map<String, String> departmentMapping = new HashMap<>();
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @PostConstruct
    public void loadDepartmentMappings() {
        logger.info("Loading department mappings from departments.json");
        
        try {
            ClassPathResource resource = new ClassPathResource("departments.json");
            InputStream inputStream = resource.getInputStream();
            
            TypeReference<List<DepartmentMappingDto>> typeReference = 
                new TypeReference<List<DepartmentMappingDto>>() {};
            List<DepartmentMappingDto> departments = objectMapper.readValue(inputStream, typeReference);
            
            // Clear existing mappings
            departmentMapping.clear();
            
            // Load mappings into static map
            for (DepartmentMappingDto dept : departments) {
                if (dept.getOldDepartmentGuid() != null && dept.getDepartmentCode() != null) {
                    departmentMapping.put(dept.getOldDepartmentGuid(), dept.getDepartmentCode());
                    logger.debug("Loaded mapping: {} -> {}", dept.getOldDepartmentGuid(), dept.getDepartmentCode());
                }
            }
            
            logger.info("Successfully loaded {} department mappings", departmentMapping.size());
            
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
            logger.warn("Null or empty oldDepartmentGuid provided");
            return null;
        }
        
        String departmentCode = departmentMapping.get(oldDepartmentGuid);
        
        if (departmentCode == null) {
            logger.warn("No department code found for oldDepartmentGuid: {}", oldDepartmentGuid);
        } else {
            logger.debug("Found department code '{}' for oldDepartmentGuid: {}", departmentCode, oldDepartmentGuid);
        }
        
        return departmentCode;
    }
    
    /**
     * Get all department mappings
     * 
     * @return A copy of the department mappings map
     */
    public static Map<String, String> getAllDepartmentMappings() {
        return new HashMap<>(departmentMapping);
    }
    
    /**
     * Check if a department GUID exists in the mappings
     * 
     * @param oldDepartmentGuid The old department GUID to check
     * @return true if the GUID exists in mappings, false otherwise
     */
    public static boolean hasDepartmentMapping(String oldDepartmentGuid) {
        return oldDepartmentGuid != null && departmentMapping.containsKey(oldDepartmentGuid);
    }
    
    /**
     * Get the total number of department mappings loaded
     * 
     * @return The number of department mappings
     */
    public static int getMappingCount() {
        return departmentMapping.size();
    }
}