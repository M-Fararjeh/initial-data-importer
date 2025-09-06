package com.importservice.controller.incoming;

import com.importservice.dto.ImportResponseDto;
import com.importservice.service.migration.incoming.IncomingCorrespondenceMigrationService;
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
@RequestMapping("/api/incoming-migration")
@Tag(name = "Incoming Correspondence Migration Controller", description = "Operations for migrating incoming correspondences")
@CrossOrigin(origins = "*")
public class IncomingCorrespondenceMigrationController {
    
    private static final Logger logger = LoggerFactory.getLogger(IncomingCorrespondenceMigrationController.class);
    
    @Autowired
    private IncomingCorrespondenceMigrationService migrationService;
    
    @PostMapping("/prepare-data")
    @Operation(summary = "Phase 1: Prepare Data", description = "Prepares incoming correspondences for migration")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Phase completed successfully"),
        @ApiResponse(responseCode = "400", description = "Phase failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ImportResponseDto> prepareData() {
        logger.info("Received request for Phase 1: Prepare Data");
        return executePhase(() -> migrationService.prepareData());
    }
    
    @PostMapping("/creation")
    @Operation(summary = "Phase 2: Creation", description = "Creates correspondences in destination system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Phase completed successfully"),
        @ApiResponse(responseCode = "400", description = "Phase failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ImportResponseDto> executeCreation() {
        logger.info("Received request for Phase 2: Creation");
        return executePhase(() -> migrationService.executeCreationPhase());
    }
    
    @PostMapping("/assignment")
    @Operation(summary = "Phase 3: Assignment", description = "Assigns correspondences to users/departments")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Phase completed successfully"),
        @ApiResponse(responseCode = "400", description = "Phase failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ImportResponseDto> executeAssignment() {
        logger.info("Received request for Phase 3: Assignment");
        return executePhase(() -> migrationService.executeAssignmentPhase());
    }
    
    @PostMapping("/business-log")
    @Operation(summary = "Phase 4: Business Log", description = "Processes business logic for correspondences")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Phase completed successfully"),
        @ApiResponse(responseCode = "400", description = "Phase failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ImportResponseDto> executeBusinessLog() {
        logger.info("Received request for Phase 4: Business Log");
        return executePhase(() -> migrationService.executeBusinessLogPhase());
    }
    
    @PostMapping("/comment")
    @Operation(summary = "Phase 5: Comment", description = "Processes comments for correspondences")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Phase completed successfully"),
        @ApiResponse(responseCode = "400", description = "Phase failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ImportResponseDto> executeComment() {
        logger.info("Received request for Phase 5: Comment");
        return executePhase(() -> migrationService.executeCommentPhase());
    }
    
    @PostMapping("/closing")
    @Operation(summary = "Phase 6: Closing", description = "Closes correspondences that need to be closed")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Phase completed successfully"),
        @ApiResponse(responseCode = "400", description = "Phase failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ImportResponseDto> executeClosing() {
        logger.info("Received request for Phase 6: Closing");
        return executePhase(() -> migrationService.executeClosingPhase());
    }
    
    @PostMapping("/retry-failed")
    @Operation(summary = "Retry Failed Migrations", description = "Retries failed migrations that haven't exceeded max retry count")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Retry completed successfully"),
        @ApiResponse(responseCode = "400", description = "Retry failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ImportResponseDto> retryFailed() {
        logger.info("Received request to retry failed migrations");
        return executePhase(() -> migrationService.retryFailedMigrations());
    }
    
    // Specific execution endpoints
    @PostMapping("/creation/execute-specific")
    @Operation(summary = "Execute Creation for Specific Correspondences", description = "Executes creation phase for specified correspondence GUIDs")
    public ResponseEntity<ImportResponseDto> executeCreationForSpecific(@RequestBody Map<String, List<String>> request) {
        List<String> correspondenceGuids = request.get("correspondenceGuids");
        logger.info("Received request to execute creation for {} specific correspondences", 
                   correspondenceGuids != null ? correspondenceGuids.size() : 0);
        return executePhase(() -> migrationService.executeCreationForSpecific(correspondenceGuids));
    }
    
    @PostMapping("/assignment/execute-specific")
    @Operation(summary = "Execute Assignment for Specific Transactions", description = "Executes assignment phase for specified transaction GUIDs")
    public ResponseEntity<ImportResponseDto> executeAssignmentForSpecific(@RequestBody Map<String, List<String>> request) {
        List<String> transactionGuids = request.get("transactionGuids");
        logger.info("Received request to execute assignment for {} specific transactions", 
                   transactionGuids != null ? transactionGuids.size() : 0);
        return executePhase(() -> migrationService.executeAssignmentForSpecific(transactionGuids));
    }
    
    @PostMapping("/business-log/execute-specific")
    @Operation(summary = "Execute Business Log for Specific Transactions", description = "Executes business log phase for specified transaction GUIDs")
    public ResponseEntity<ImportResponseDto> executeBusinessLogForSpecific(@RequestBody Map<String, List<String>> request) {
        List<String> transactionGuids = request.get("transactionGuids");
        logger.info("Received request to execute business log for {} specific transactions", 
                   transactionGuids != null ? transactionGuids.size() : 0);
        return executePhase(() -> migrationService.executeBusinessLogForSpecific(transactionGuids));
    }
    
    @PostMapping("/comment/execute-specific")
    @Operation(summary = "Execute Comment for Specific Comments", description = "Executes comment phase for specified comment GUIDs")
    public ResponseEntity<ImportResponseDto> executeCommentForSpecific(@RequestBody Map<String, List<String>> request) {
        List<String> commentGuids = request.get("commentGuids");
        logger.info("Received request to execute comment for {} specific comments", 
                   commentGuids != null ? commentGuids.size() : 0);
        return executePhase(() -> migrationService.executeCommentForSpecific(commentGuids));
    }
    
    @PostMapping("/closing/execute-specific")
    @Operation(summary = "Execute Closing for Specific Correspondences", description = "Executes closing phase for specified correspondence GUIDs")
    public ResponseEntity<ImportResponseDto> executeClosingForSpecific(@RequestBody Map<String, List<String>> request) {
        List<String> correspondenceGuids = request.get("correspondenceGuids");
        logger.info("Received request to execute closing for {} specific correspondences", 
                   correspondenceGuids != null ? correspondenceGuids.size() : 0);
        return executePhase(() -> migrationService.executeClosingForSpecific(correspondenceGuids));
    }
    
    // Detail endpoints
    @GetMapping("/creation/details")
    @Operation(summary = "Get Creation Phase Details", description = "Returns detailed information about creation phase migrations")
    @Transactional(readOnly = true, timeout = 60)
    public ResponseEntity<Map<String, Object>> getCreationDetails() {
        logger.info("Received request for creation phase details");
        return getDetails(() -> migrationService.getCreationMigrationsWithDetails());
    }
    
    @GetMapping("/creation/statistics")
    @Operation(summary = "Get Creation Phase Statistics", description = "Returns statistics about creation phase status distribution")
    @Transactional(readOnly = true, timeout = 60)
    public ResponseEntity<Map<String, Object>> getCreationStatistics() {
        logger.info("Received request for creation phase statistics");
        return getDetails(() -> migrationService.getCreationStatistics());
    }
    
    @GetMapping("/assignment/details")
    @Operation(summary = "Get Assignment Phase Details", description = "Returns detailed information about assignment phase migrations")
    @Transactional(readOnly = true, timeout = 60)
    public ResponseEntity<Map<String, Object>> getAssignmentDetails(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "all") String status,
            @RequestParam(defaultValue = "") String search) {
        logger.info("Received request for assignment phase details - page: {}, size: {}, status: {}, search: '{}'", 
                   page, size, status, search);
        return getDetails(() -> migrationService.getAssignmentMigrations(page, size, status, search));
    }
    
    @GetMapping("/business-log/details")
    @Operation(summary = "Get Business Log Phase Details", description = "Returns detailed information about business log phase migrations")
    @Transactional(readOnly = true, timeout = 60)
    public ResponseEntity<Map<String, Object>> getBusinessLogDetails(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "all") String status,
            @RequestParam(defaultValue = "") String search) {
        logger.info("Received request for business log phase details - page: {}, size: {}, status: {}, search: '{}'", 
                   page, size, status, search);
        return getDetails(() -> migrationService.getBusinessLogMigrations(page, size, status, search));
    }
    
    @GetMapping("/comment/details")
    @Operation(summary = "Get Comment Phase Details", description = "Returns detailed information about comment phase migrations")
    @Transactional(readOnly = true, timeout = 60)
    public ResponseEntity<Map<String, Object>> getCommentDetails(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "all") String status,
            @RequestParam(defaultValue = "all") String commentType,
            @RequestParam(defaultValue = "") String search) {
        logger.info("Received request for comment phase details - page: {}, size: {}, status: {}, commentType: {}, search: '{}'", 
                   page, size, status, commentType, search);
        return getDetails(() -> migrationService.getCommentMigrations(page, size, status, commentType, search));
    }
    
    @GetMapping("/closing/details")
    @Operation(summary = "Get Closing Phase Details", description = "Returns detailed information about closing phase migrations")
    @Transactional(readOnly = true, timeout = 60)
    public ResponseEntity<Map<String, Object>> getClosingDetails(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "all") String status,
            @RequestParam(defaultValue = "all") String needToClose,
            @RequestParam(defaultValue = "") String search) {
        logger.info("Received request for closing phase details - page: {}, size: {}, status: {}, needToClose: {}, search: '{}'", 
                   page, size, status, needToClose, search);
        return getDetails(() -> migrationService.getClosingMigrations(page, size, status, needToClose, search));
    }
    
    @GetMapping("/statistics")
    @Operation(summary = "Get Migration Statistics", description = "Returns statistics about migration progress")
    @Transactional(readOnly = true, timeout = 60)
    public ResponseEntity<Map<String, Object>> getStatistics() {
        logger.info("Received request for migration statistics");
        
        try {
            Map<String, Object> statistics = migrationService.getMigrationStatistics();
            
            // Ensure all required fields are present with default values
            String[] requiredFields = {"prepareData", "creation", "assignment", "businessLog", "comment", "closing", "completed", "failed", "inProgress"};
            for (String field : requiredFields) {
                statistics.putIfAbsent(field, 0L);
            }
            
            logger.info("Returning migration statistics: {}", statistics);
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            logger.error("Error getting migration statistics", e);
            return ResponseEntity.status(500).body(createErrorStatistics(e.getMessage()));
        }
    }
    
    // Helper methods to reduce code duplication
    private ResponseEntity<ImportResponseDto> executePhase(PhaseExecutor executor) {
        try {
            ImportResponseDto response = executor.execute();
            return getResponseEntity(response);
        } catch (Exception e) {
            logger.error("Unexpected error in phase execution", e);
            return ResponseEntity.status(500).body(createErrorResponse("Unexpected error: " + e.getMessage()));
        }
    }
    
    private ResponseEntity<Map<String, Object>> getDetails(DetailsProvider provider) {
        try {
            Map<String, Object> details = provider.getDetails();
            return ResponseEntity.ok(details);
        } catch (Exception e) {
            logger.error("Error getting details", e);
            return ResponseEntity.status(500).body(createErrorDetails(e.getMessage()));
        }
    }
    
    private ResponseEntity<ImportResponseDto> getResponseEntity(ImportResponseDto response) {
        return "ERROR".equals(response.getStatus()) ? 
            ResponseEntity.badRequest().body(response) : 
            ResponseEntity.ok(response);
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
    
    private Map<String, Object> createErrorDetails(String errorMessage) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("content", new ArrayList<>());
        errorResponse.put("totalElements", 0L);
        errorResponse.put("totalPages", 0);
        errorResponse.put("currentPage", 0);
        errorResponse.put("pageSize", 20);
        errorResponse.put("hasNext", false);
        errorResponse.put("hasPrevious", false);
        errorResponse.put("error", errorMessage);
        return errorResponse;
    }
    
    private Map<String, Object> createErrorStatistics(String errorMessage) {
        Map<String, Object> errorMap = new HashMap<>();
        errorMap.put("error", "Failed to get statistics: " + errorMessage);
        
        // Add default values
        String[] fields = {"prepareData", "creation", "assignment", "businessLog", "comment", "closing", "completed", "failed", "inProgress"};
        for (String field : fields) {
            errorMap.put(field, 0L);
        }
        
        return errorMap;
    }
    
    // Functional interfaces for cleaner code
    @FunctionalInterface
    private interface PhaseExecutor {
        ImportResponseDto execute() throws Exception;
    }
    
    @FunctionalInterface
    private interface DetailsProvider {
        Map<String, Object> getDetails() throws Exception;
    }
}