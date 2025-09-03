package com.importservice.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.importservice.dto.DepartmentMappingDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.importservice.repository.UserRepository;
import com.importservice.repository.UserPositionRepository;
import com.importservice.repository.PositionRepository;
import com.importservice.repository.DepartmentRepository;
import com.importservice.entity.User;
import com.importservice.entity.UserPosition;
import com.importservice.entity.Position;
import com.importservice.entity.Department;

@Component
public class DepartmentUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(DepartmentUtils.class);
    
    private static final Map<String, String> departmentMappings = new HashMap<>();
    
    private final ObjectMapper objectMapper;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserPositionRepository userPositionRepository;
    
    @Autowired
    private PositionRepository positionRepository;
    
    @Autowired
    private DepartmentRepository departmentRepository;
    
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
    
    /**
     * Get department GUID by user email
     * 
     * @param userEmail The user email to look up
     * @return The department GUID, or null if not found
     */
    @Transactional(readOnly = true, timeout = 30)
    public String getDepartmentGuidByUserEmail(String userEmail) {
        if (userEmail == null || userEmail.trim().isEmpty()) {
            logger.debug("Null or empty userEmail provided");
            return null;
        }
        
        try {
            // Use native query for better performance
            String departmentGuid = userRepository.findDepartmentGuidByUserEmail(userEmail.trim());
            
            if (departmentGuid == null) {
                logger.debug("No department found for user email: {}", userEmail);
            } else {
                logger.debug("Found department GUID '{}' for user email: {}", departmentGuid, userEmail);
            }
            
            return departmentGuid;
            
        } catch (Exception e) {
            logger.error("Error getting department GUID by user email: {}", userEmail, e);
            return null;
        }
    }
    
    /**
     * Get department GUID by user GUID
     * 
     * @param userGuid The user GUID to look up
     * @return The department GUID, or null if not found
     */
    @Transactional(readOnly = true, timeout = 30)
    public String getDepartmentGuidByUserGuid(String userGuid) {
        if (userGuid == null || userGuid.trim().isEmpty()) {
            logger.debug("Null or empty userGuid provided");
            return null;
        }
        
        try {
            // Use native query for better performance
            String departmentGuid = userRepository.findDepartmentGuidByUserGuid(userGuid.trim());
            
            if (departmentGuid == null) {
                logger.debug("No department found for userGuid: {}", userGuid);
            } else {
                logger.debug("Found department GUID '{}' for userGuid: {}", departmentGuid, userGuid);
            }
            
            return departmentGuid;
            
        } catch (Exception e) {
            logger.error("Error getting department GUID by user GUID: {}", userGuid, e);
            return null;
        }
    }
}