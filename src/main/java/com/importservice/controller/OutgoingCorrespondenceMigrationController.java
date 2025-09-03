package com.importservice.controller;

import com.importservice.dto.ImportResponseDto;
import com.importservice.service.OutgoingCorrespondenceMigrationService;
import com.importservice.entity.OutgoingCorrespondenceMigration;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/outgoing-migration")
@Tag(name = "Outgoing Correspondence Migration Controller", description = "Operations for migrating outgoing correspondences")
@CrossOrigin(origins = "*")
public class OutgoingCorrespondenceMigrationController {
    
    private static final Logger logger = LoggerFactory.getLogger(OutgoingCorrespondenceMigrationController.class);
    
    @Autowired
    private OutgoingCorrespondenceMigrationService migrationService;
    
    @PostMapping("/prepare-data")
    @Operation(summary = "Phase 1: Prepare Data", 
               description = "Prepares outgoing correspondences for migration")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Phase completed successfully"),
        @ApiResponse(responseCode = "400", description = "Phase failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ImportResponseDto> prepareData() {
        logger.info("Received request for Outgoing Phase 1: Prepare Data");
        
        try {
            ImportResponseDto response = migrationService.prepareData();
            return getResponseEntity(response);
        } catch (Exception e) {
            logger.error("Unexpected error in outgoing prepare data phase", e);
            return ResponseEntity.status(500).body(createErrorResponse("Unexpected error: " + e.getMessage()));
        }
    }
    
    @PostMapping("/creation")
    @Operation(summary = "Phase 2: Creation", 
               description = "Creates outgoing correspondences in destination system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Phase completed successfully"),
        @ApiResponse(responseCode = "400", description = "Phase failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ImportResponseDto> executeCreation() {
        logger.info("Received request for Outgoing Phase 2: Creation");
        
        try {
            ImportResponseDto response = migrationService.executeCreationPhase();
            return getResponseEntity(response);
        } catch (Exception e) {
            logger.error("Unexpected error in outgoing creation phase", e);
            return ResponseEntity.status(500).body(createErrorResponse("Unexpected error: " + e.getMessage()));
        }
    }
    
    @GetMapping("/creation/details")
    @Operation(summary = "Get Outgoing Creation Phase Details", 
               description = "Returns detailed information about outgoing creation phase migrations")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Details retrieved successfully")
    })
    @Transactional(readOnly = true, timeout = 60)
    public ResponseEntity<Map<String, Object>> getCreationDetails() {
        logger.info("Received request for outgoing creation phase details");
        
        try {
            Map<String, Object> migrations = migrationService.getCreationMigrationsWithDetails();
            return ResponseEntity.ok(migrations);
        } catch (Exception e) {
            logger.error("Error getting outgoing creation details", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("content", new ArrayList<>());
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    @GetMapping("/creation/statistics")
    @Operation(summary = "Get Outgoing Creation Phase Statistics", 
               description = "Returns statistics about outgoing creation phase status distribution")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully")
    })
    @Transactional(readOnly = true, timeout = 60)
    public ResponseEntity<Map<String, Object>> getCreationStatistics() {
        logger.info("Received request for outgoing creation phase statistics");
        
        try {
            Map<String, Object> statistics = migrationService.getCreationStatistics();
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            logger.error("Error getting outgoing creation statistics", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("total", 0);
            errorResponse.put("completed", 0);
            errorResponse.put("pending", 0);
            errorResponse.put("error", 0);
            errorResponse.put("stepStatistics", new ArrayList<>());
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    @PostMapping("/creation/execute-specific")
    @Operation(summary = "Execute Outgoing Creation for Specific Correspondences", 
               description = "Executes outgoing creation phase for specified correspondence GUIDs")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Execution completed successfully"),
        @ApiResponse(responseCode = "400", description = "Execution failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ImportResponseDto> executeCreationForSpecific(@RequestBody Map<String, List<String>> request) {
        List<String> correspondenceGuids = request.get("correspondenceGuids");
        logger.info("Received request to execute outgoing creation for {} specific correspondences", 
                   correspondenceGuids != null ? correspondenceGuids.size() : 0);
        
        try {
            ImportResponseDto response = migrationService.executeCreationForSpecific(correspondenceGuids);
            return getResponseEntity(response);
        } catch (Exception e) {
            logger.error("Unexpected error in execute outgoing creation for specific", e);
            return ResponseEntity.status(500).body(createErrorResponse("Unexpected error: " + e.getMessage()));
        }
    }
    
    @PostMapping("/assignment")
    @Operation(summary = "Phase 3: Assignment", 
               description = "Assigns outgoing correspondences to users/departments")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Phase completed successfully"),
        @ApiResponse(responseCode = "400", description = "Phase failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ImportResponseDto> executeAssignment() {
        logger.info("Received request for Outgoing Phase 3: Assignment");
        
        try {
            ImportResponseDto response = migrationService.executeAssignmentPhase();
            return getResponseEntity(response);
        } catch (Exception e) {
            logger.error("Unexpected error in outgoing assignment phase", e);
            return ResponseEntity.status(500).body(createErrorResponse("Unexpected error: " + e.getMessage()));
        }
    }
    
    @PostMapping("/assignment/execute-specific")
    @Operation(summary = "Execute Outgoing Assignment for Specific Transactions", 
               description = "Executes outgoing assignment phase for specified transaction GUIDs")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Execution completed successfully"),
        @ApiResponse(responseCode = "400", description = "Execution failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ImportResponseDto> executeAssignmentForSpecific(@RequestBody Map<String, List<String>> request) {
        List<String> transactionGuids = request.get("transactionGuids");
        logger.info("Received request to execute outgoing assignment for {} specific transactions", 
                   transactionGuids != null ? transactionGuids.size() : 0);
        
        try {
            ImportResponseDto response = migrationService.executeAssignmentForSpecific(transactionGuids);
            return getResponseEntity(response);
        } catch (Exception e) {
            logger.error("Unexpected error in execute outgoing assignment for specific", e);
            return ResponseEntity.status(500).body(createErrorResponse("Unexpected error: " + e.getMessage()));
        }
    }
    
    @PostMapping("/approval")
    @Operation(summary = "Phase 4: Approval", 
               description = "Approves outgoing correspondences and registers them")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Phase completed successfully"),
        @ApiResponse(responseCode = "400", description = "Phase failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ImportResponseDto> executeApproval() {
        logger.info("Received request for Outgoing Phase 4: Approval");
        
        try {
            ImportResponseDto response = migrationService.executeApprovalPhase();
            return getResponseEntity(response);
        } catch (Exception e) {
            logger.error("Unexpected error in outgoing approval phase", e);
            return ResponseEntity.status(500).body(createErrorResponse("Unexpected error: " + e.getMessage()));
        }
    }
    
    @PostMapping("/approval/execute-specific")
    @Operation(summary = "Execute Outgoing Approval for Specific Correspondences", 
               description = "Executes outgoing approval phase for specified correspondence GUIDs")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Execution completed successfully"),
        @ApiResponse(responseCode = "400", description = "Execution failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ImportResponseDto> executeApprovalForSpecific(@RequestBody Map<String, List<String>> request) {
        List<String> correspondenceGuids = request.get("correspondenceGuids");
        logger.info("Received request to execute outgoing approval for {} specific correspondences", 
                   correspondenceGuids != null ? correspondenceGuids.size() : 0);
        
        try {
            ImportResponseDto response = migrationService.executeApprovalForSpecific(correspondenceGuids);
            return getResponseEntity(response);
        } catch (Exception e) {
            logger.error("Unexpected error in execute outgoing approval for specific", e);
            return ResponseEntity.status(500).body(createErrorResponse("Unexpected error: " + e.getMessage()));
        }
    }
    
    @PostMapping("/business-log")
    @Operation(summary = "Phase 5: Business Log", 
               description = "Processes business logic for outgoing correspondences")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Phase completed successfully"),
        @ApiResponse(responseCode = "400", description = "Phase failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ImportResponseDto> executeBusinessLog() {
        logger.info("Received request for Outgoing Phase 5: Business Log");
        
        try {
            ImportResponseDto response = migrationService.executeBusinessLogPhase();
            return getResponseEntity(response);
        } catch (Exception e) {
            logger.error("Unexpected error in outgoing business log phase", e);
            return ResponseEntity.status(500).body(createErrorResponse("Unexpected error: " + e.getMessage()));
        }
    }
    
    @PostMapping("/business-log/execute-specific")
    @Operation(summary = "Execute Outgoing Business Log for Specific Transactions", 
               description = "Executes outgoing business log phase for specified transaction GUIDs")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Execution completed successfully"),
        @ApiResponse(responseCode = "400", description = "Execution failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ImportResponseDto> executeBusinessLogForSpecific(@RequestBody Map<String, List<String>> request) {
        List<String> transactionGuids = request.get("transactionGuids");
        logger.info("Received request to execute outgoing business log for {} specific transactions", 
                   transactionGuids != null ? transactionGuids.size() : 0);
        
        try {
            ImportResponseDto response = migrationService.executeBusinessLogForSpecific(transactionGuids);
            return getResponseEntity(response);
        } catch (Exception e) {
            logger.error("Unexpected error in execute outgoing business log for specific", e);
            return ResponseEntity.status(500).body(createErrorResponse("Unexpected error: " + e.getMessage()));
        }
    }
    
    @PostMapping("/comment")
    @Operation(summary = "Phase 6: Comment", 
               description = "Processes comments for outgoing correspondences")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Phase completed successfully"),
        @ApiResponse(responseCode = "400", description = "Phase failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ImportResponseDto> executeComment() {
        logger.info("Received request for Outgoing Phase 6: Comment");
        
        try {
            ImportResponseDto response = migrationService.executeCommentPhase();
            return getResponseEntity(response);
        } catch (Exception e) {
            logger.error("Unexpected error in outgoing comment phase", e);
            return ResponseEntity.status(500).body(createErrorResponse("Unexpected error: " + e.getMessage()));
        }
    }
    
    @PostMapping("/comment/execute-specific")
    @Operation(summary = "Execute Outgoing Comment for Specific Comments", 
               description = "Executes outgoing comment phase for specified comment GUIDs")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Execution completed successfully"),
        @ApiResponse(responseCode = "400", description = "Execution failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ImportResponseDto> executeCommentForSpecific(@RequestBody Map<String, List<String>> request) {
        List<String> commentGuids = request.get("commentGuids");
        logger.info("Received request to execute outgoing comment for {} specific comments", 
                   commentGuids != null ? commentGuids.size() : 0);
        
        try {
            ImportResponseDto response = migrationService.executeCommentForSpecific(commentGuids);
            return getResponseEntity(response);
        } catch (Exception e) {
            logger.error("Unexpected error in execute outgoing comment for specific", e);
            return ResponseEntity.status(500).body(createErrorResponse("Unexpected error: " + e.getMessage()));
        }
    }
    
    @PostMapping("/closing")
    @Operation(summary = "Phase 7: Closing", 
               description = "Closes outgoing correspondences that need archiving")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Phase completed successfully"),
        @ApiResponse(responseCode = "400", description = "Phase failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ImportResponseDto> executeClosing() {
        logger.info("Received request for Outgoing Phase 7: Closing");
        
        try {
            ImportResponseDto response = migrationService.executeClosingPhase();
            return getResponseEntity(response);
        } catch (Exception e) {
            logger.error("Unexpected error in outgoing closing phase", e);
            return ResponseEntity.status(500).body(createErrorResponse("Unexpected error: " + e.getMessage()));
        }
    }
    
    @PostMapping("/closing/execute-specific")
    @Operation(summary = "Execute Outgoing Closing for Specific Correspondences", 
               description = "Executes outgoing closing phase for specified correspondence GUIDs")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Execution completed successfully"),
        @ApiResponse(responseCode = "400", description = "Execution failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ImportResponseDto> executeClosingForSpecific(@RequestBody Map<String, List<String>> request) {
        List<String> correspondenceGuids = request.get("correspondenceGuids");
        logger.info("Received request to execute outgoing closing for {} specific correspondences", 
                   correspondenceGuids != null ? correspondenceGuids.size() : 0);
        
        try {
            ImportResponseDto response = migrationService.executeClosingForSpecific(correspondenceGuids);
            return getResponseEntity(response);
        } catch (Exception e) {
            logger.error("Unexpected error in execute outgoing closing for specific", e);
            return ResponseEntity.status(500).body(createErrorResponse("Unexpected error: " + e.getMessage()));
        }
    }
    
    @PostMapping("/retry-failed")
    @Operation(summary = "Retry Failed Outgoing Migrations", 
               description = "Retries failed outgoing migrations that haven't exceeded max retry count")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Retry completed successfully"),
        @ApiResponse(responseCode = "400", description = "Retry failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ImportResponseDto> retryFailed() {
        logger.info("Received request to retry failed outgoing migrations");
        
        try {
            ImportResponseDto response = migrationService.retryFailedMigrations();
            return getResponseEntity(response);
        } catch (Exception e) {
            logger.error("Unexpected error in retry failed outgoing migrations", e);
            return ResponseEntity.status(500).body(createErrorResponse("Unexpected error: " + e.getMessage()));
        }
    }
    
    @GetMapping("/statistics")
    @Operation(summary = "Get Outgoing Migration Statistics", 
               description = "Returns statistics about outgoing migration progress")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully")
    })
    @Transactional(readOnly = true, timeout = 60)
    public ResponseEntity<Map<String, Object>> getStatistics() {
        logger.info("Received request for outgoing migration statistics");
        
        try {
            Map<String, Object> statistics = migrationService.getMigrationStatistics();
            
            // Ensure all required fields are present with default values
            statistics.putIfAbsent("prepareData", 0L);
            statistics.putIfAbsent("creation", 0L);
            statistics.putIfAbsent("assignment", 0L);
            statistics.putIfAbsent("approval", 0L);
            statistics.putIfAbsent("businessLog", 0L);
            statistics.putIfAbsent("comment", 0L);
            statistics.putIfAbsent("closing", 0L);
            statistics.putIfAbsent("completed", 0L);
            statistics.putIfAbsent("failed", 0L);
            statistics.putIfAbsent("inProgress", 0L);
            
            logger.info("Returning outgoing migration statistics: {}", statistics);
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            logger.error("Error getting outgoing migration statistics", e);
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("error", "Failed to get outgoing statistics: " + e.getMessage());
            
            // Add default values even in error case
            errorMap.put("prepareData", 0L);
            errorMap.put("creation", 0L);
            errorMap.put("assignment", 0L);
            errorMap.put("approval", 0L);
            errorMap.put("businessLog", 0L);
            errorMap.put("comment", 0L);
            errorMap.put("closing", 0L);
            errorMap.put("completed", 0L);
            errorMap.put("failed", 0L);
            errorMap.put("inProgress", 0L);
            
            return ResponseEntity.status(500).body(errorMap);
        }
    }
    
    private ResponseEntity<ImportResponseDto> getResponseEntity(ImportResponseDto response) {
        if ("ERROR".equals(response.getStatus())) {
            return ResponseEntity.badRequest().body(response);
        } else {
            return ResponseEntity.ok(response);
        }
    }
    
    private ImportResponseDto createErrorResponse(String errorMessage) {
        ImportResponseDto errorResponse = new ImportResponseDto();
        errorResponse.setStatus("ERROR");
        errorResponse.setMessage(errorMessage);
        errorResponse.setTotalRecords(0);
        errorResponse.setSuccessfulImports(0);
        errorResponse.setFailedImports(0);
        errorResponse.setErrors(java.util.Arrays.asList(errorMessage));
        return errorResponse;
    }
}