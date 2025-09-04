package com.importservice.service.migration.internal;

import com.importservice.dto.ImportResponseDto;
import com.importservice.entity.Correspondence;
import com.importservice.entity.InternalCorrespondenceMigration;
import com.importservice.repository.CorrespondenceRepository;
import com.importservice.repository.InternalCorrespondenceMigrationRepository;
import com.importservice.service.migration.MigrationPhaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Service for Internal Phase 1: Prepare Data
 * Selects and prepares internal correspondences for migration
 */
@Service
public class InternalPrepareDataService {
    
    private static final Logger logger = LoggerFactory.getLogger(InternalPrepareDataService.class);
    
    @Autowired
    private CorrespondenceRepository correspondenceRepository;
    
    @Autowired
    private InternalCorrespondenceMigrationRepository migrationRepository;
    
    @Autowired
    private MigrationPhaseService phaseService;
    
    /**
     * Phase 1: Prepare Data
     * Selects internal correspondences and creates migration tracking records
     */
    @Transactional(readOnly = false, timeout = 300)
    public ImportResponseDto prepareData() {
        logger.info("Starting Internal Phase 1: Prepare Data");
        
        List<String> errors = new ArrayList<>();
        int successfulImports = 0;
        int failedImports = 0;
        
        try {
            // Find internal correspondences (CorrespondenceTypeId = 3)
            // Filter out deleted, draft, and cancelled correspondences
            List<Correspondence> internalCorrespondences = correspondenceRepository
                .findByCorrespondenceTypeIdAndIsDeletedAndIsDraft(3, false, false);
            
            // Further filter out cancelled correspondences
            List<Correspondence> validCorrespondences = new ArrayList<>();
            for (Correspondence correspondence : internalCorrespondences) {
                if (correspondence.getIsCanceled() == null || correspondence.getIsCanceled() == 0) {
                    validCorrespondences.add(correspondence);
                }
            }
            
            logger.info("Found {} valid internal correspondences to prepare for migration", validCorrespondences.size());
            
            for (Correspondence correspondence : validCorrespondences) {
                try {
                    // Check if migration record already exists
                    if (migrationRepository.findByCorrespondenceGuid(correspondence.getGuid()).isPresent()) {
                        logger.debug("Migration record already exists for correspondence: {}", correspondence.getGuid());
                        successfulImports++;
                        continue;
                    }
                    
                    // Determine if correspondence needs to be closed (from isArchive field)
                    boolean isNeedToClose = correspondence.getIsArchive() != null && correspondence.getIsArchive();
                    
                    // Create migration tracking record
                    InternalCorrespondenceMigration migration = new InternalCorrespondenceMigration(
                        correspondence.getGuid(), 
                        isNeedToClose
                    );
                    
                    migration.setPrepareDataStatus("COMPLETED");
                    migration.setCurrentPhase("CREATION");
                    migration.setNextPhase("ASSIGNMENT");
                    migration.setPhaseStatus("PENDING");
                    
                    migrationRepository.save(migration);
                    successfulImports++;
                    
                    logger.debug("Created internal migration record for correspondence: {} (needToClose: {})", 
                               correspondence.getGuid(), isNeedToClose);
                    
                } catch (Exception e) {
                    failedImports++;
                    String errorMsg = "Failed to prepare internal correspondence " + correspondence.getGuid() + ": " + e.getMessage();
                    errors.add(errorMsg);
                    logger.error(errorMsg, e);
                }
            }
            
            String status = phaseService.determineFinalStatus(successfulImports, failedImports);
            String message = String.format("Internal Phase 1 completed. Prepared: %d, Failed: %d", 
                                         successfulImports, failedImports);
            
            return phaseService.createResponse(status, message, validCorrespondences.size(), 
                                             successfulImports, failedImports, errors);
            
        } catch (Exception e) {
            logger.error("Error in Internal Phase 1: Prepare Data", e);
            return phaseService.createResponse("ERROR", "Internal Phase 1 failed: " + e.getMessage(), 
                                             0, 0, 0, Arrays.asList(e.getMessage()));
        }
    }
}