package com.importservice.service.migration.outgoing;

import com.importservice.dto.ImportResponseDto;
import com.importservice.entity.Correspondence;
import com.importservice.entity.OutgoingCorrespondenceMigration;
import com.importservice.repository.CorrespondenceRepository;
import com.importservice.repository.OutgoingCorrespondenceMigrationRepository;
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
 * Service for Outgoing Phase 1: Prepare Data
 * Selects and prepares outgoing correspondences for migration
 */
@Service
public class OutgoingPrepareDataService {
    
    private static final Logger logger = LoggerFactory.getLogger(OutgoingPrepareDataService.class);
    
    @Autowired
    private CorrespondenceRepository correspondenceRepository;
    
    @Autowired
    private OutgoingCorrespondenceMigrationRepository migrationRepository;
    
    @Autowired
    private MigrationPhaseService phaseService;
    
    /**
     * Phase 1: Prepare Data
     * Selects outgoing correspondences and creates migration tracking records
     */
    @Transactional(readOnly = false, timeout = 300)
    public ImportResponseDto prepareData() {
        logger.info("Starting Outgoing Phase 1: Prepare Data");
        
        List<String> errors = new ArrayList<>();
        int successfulImports = 0;
        int failedImports = 0;
        
        try {
            // Find outgoing correspondences (CorrespondenceTypeId = 1)
            // Filter out deleted, draft, and cancelled correspondences
            List<Correspondence> outgoingCorrespondences = correspondenceRepository
                .findByCorrespondenceTypeIdAndIsDeletedAndIsDraft(1, false, false);
            
            // Further filter out cancelled correspondences
            List<Correspondence> validCorrespondences = new ArrayList<>();
            for (Correspondence correspondence : outgoingCorrespondences) {
                if (correspondence.getIsCanceled() == null || correspondence.getIsCanceled() == 0) {
                    validCorrespondences.add(correspondence);
                }
            }
            
            logger.info("Found {} valid outgoing correspondences to prepare for migration", validCorrespondences.size());
            
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
                    OutgoingCorrespondenceMigration migration = new OutgoingCorrespondenceMigration(
                        correspondence.getGuid(), 
                        isNeedToClose
                    );
                    
                    migration.setPrepareDataStatus("COMPLETED");
                    migration.setCurrentPhase("CREATION");
                    migration.setNextPhase("ASSIGNMENT");
                    migration.setPhaseStatus("PENDING");
                    
                    migrationRepository.save(migration);
                    successfulImports++;
                    
                    logger.debug("Created outgoing migration record for correspondence: {} (needToClose: {})", 
                               correspondence.getGuid(), isNeedToClose);
                    
                } catch (Exception e) {
                    failedImports++;
                    String errorMsg = "Failed to prepare outgoing correspondence " + correspondence.getGuid() + ": " + e.getMessage();
                    errors.add(errorMsg);
                    logger.error(errorMsg, e);
                }
            }
            
            String status = phaseService.determineFinalStatus(successfulImports, failedImports);
            String message = String.format("Outgoing Phase 1 completed. Prepared: %d, Failed: %d", 
                                         successfulImports, failedImports);
            
            return phaseService.createResponse(status, message, validCorrespondences.size(), 
                                             successfulImports, failedImports, errors);
            
        } catch (Exception e) {
            logger.error("Error in Outgoing Phase 1: Prepare Data", e);
            return phaseService.createResponse("ERROR", "Outgoing Phase 1 failed: " + e.getMessage(), 
                                             0, 0, 0, Arrays.asList(e.getMessage()));
        }
    }
}