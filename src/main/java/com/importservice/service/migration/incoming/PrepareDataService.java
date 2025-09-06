package com.importservice.service.migration.incoming;

import java.util.Arrays;
import com.importservice.dto.ImportResponseDto;
import com.importservice.entity.Correspondence;
import com.importservice.entity.IncomingCorrespondenceMigration;
import com.importservice.repository.CorrespondenceRepository;
import com.importservice.repository.IncomingCorrespondenceMigrationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for Phase 1: Prepare Data
 * Selects and prepares incoming correspondences for migration
 */
@Service
public class PrepareDataService {
    
    private static final Logger logger = LoggerFactory.getLogger(PrepareDataService.class);
    
    @Autowired
    private CorrespondenceRepository correspondenceRepository;
    
    @Autowired
    private IncomingCorrespondenceMigrationRepository migrationRepository;
    
    @Autowired
    private MigrationPhaseService phaseService;
    
    /**
     * Phase 1: Prepare Data
     * Selects incoming correspondences and creates migration tracking records
     */
    @Transactional(readOnly = false, timeout = 300)
    public ImportResponseDto prepareData() {
        logger.info("Starting Phase 1: Prepare Data");
        
        List<String> errors = new ArrayList<>();
        int successfulImports = 0;
        int failedImports = 0;
        
        try {
            // Find incoming correspondences (CorrespondenceTypeId = 2)
            // Filter out deleted, draft, and cancelled correspondences
            List<Correspondence> incomingCorrespondences = correspondenceRepository
                .findByCorrespondenceTypeIdAndIsDeletedAndIsDraft(2, false, false);
            
            // Further filter out cancelled correspondences
            List<Correspondence> validCorrespondences = new ArrayList<>();
            for (Correspondence correspondence : incomingCorrespondences) {
                if (correspondence.getIsCanceled() == null || correspondence.getIsCanceled() == 0) {
                    validCorrespondences.add(correspondence);
                }
            }
            
            logger.info("Found {} valid incoming correspondences to prepare for migration", validCorrespondences.size());
            
            for (Correspondence correspondence : validCorrespondences) {
                try {
                    // Check if migration record already exists
                    if (migrationRepository.findByCorrespondenceGuid(correspondence.getGuid()).isPresent()) {
                        logger.debug("Migration record already exists for correspondence: {}", correspondence.getGuid());
                        successfulImports++;
                        continue;
                    }
                    
                    // Determine if correspondence needs to be closed
                    boolean isNeedToClose = determineIfNeedToClose(correspondence);
                    
                    // Create migration tracking record
                    IncomingCorrespondenceMigration migration = new IncomingCorrespondenceMigration(
                        correspondence.getGuid(), 
                        isNeedToClose
                    );
                    
                    migration.setPrepareDataStatus("COMPLETED");
                    migration.setCurrentPhase("CREATION");
                    migration.setNextPhase("ASSIGNMENT");
                    migration.setPhaseStatus("PENDING");
                    
                    migrationRepository.save(migration);
                    successfulImports++;
                    
                    logger.debug("Created migration record for correspondence: {} (needToClose: {})", 
                               correspondence.getGuid(), isNeedToClose);
                    
                } catch (Exception e) {
                    failedImports++;
                    String errorMsg = "Failed to prepare correspondence " + correspondence.getGuid() + ": " + e.getMessage();
                    errors.add(errorMsg);
                    logger.error(errorMsg, e);
                }
            }
            
            String status = phaseService.determineFinalStatus(successfulImports, failedImports);
            String message = String.format("Phase 1 completed. Prepared: %d, Failed: %d", 
                                         successfulImports, failedImports);
            
            return phaseService.createResponse(status, message, validCorrespondences.size(), 
                                             successfulImports, failedImports, errors);
            
        } catch (Exception e) {
            logger.error("Error in Phase 1: Prepare Data", e);
            return phaseService.createResponse("ERROR", "Phase 1 failed: " + e.getMessage(), 
                                             0, 0, 0, Arrays.asList(e.getMessage()));
        }
    }
    
    /**
     * Determines if a correspondence needs to be closed based on business rules
     */
    private boolean determineIfNeedToClose(Correspondence correspondence) {
        // Business logic to determine if correspondence should be closed
        // This is a placeholder - implement actual business rules
        
        if (correspondence.getIsFinal() != null && correspondence.getIsFinal()) {
            return true;
        }
        
        if (correspondence.getIsArchive() != null && correspondence.getIsArchive()) {
            return true;
        }
        
        // Add more business rules as needed
        return false;
    }
}