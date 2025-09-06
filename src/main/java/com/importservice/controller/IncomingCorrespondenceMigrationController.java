package com.importservice.controller;

import com.importservice.dto.ImportResponseDto;
import com.importservice.service.IncomingCorrespondenceMigrationService;
import com.importservice.entity.IncomingCorrespondenceMigration;
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/incoming-migration")
@Tag(name = "Incoming Correspondence Migration Controller", description = "Operations for migrating incoming correspondences")
@CrossOrigin(origins = "*")
public class IncomingCorrespondenceMigrationController {
    
    private static final Logger logger = LoggerFactory.getLogger(IncomingCorrespondenceMigrationController.class);
    
    @Autowired
    private IncomingCorrespondenceMigrationService migrationService;
    
    @PostMapping("/prepare-data")
    @Operation(summary = "Phase 1: Prepare Data", 
               description = "Prepares incoming correspondences for migration")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Phase completed successfully"),
        @ApiResponse(responseCode = "400", description = "Phase failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ImportResponseDto> prepareData() {
        logger.info("Received request for Phase 1: Prepare Data");
        
        try {
            ImportResponseDto response = migrationService.prepareData();
            return getResponseEntity(response);
        } catch (Exception e) {
            logger.error("Unexpected error in prepare data phase", e);
            return ResponseEntity.status(500).body(createErrorResponse("Unexpected error: " + e.getMessage()));
        }
    }
    
    @PostMapping("/creation")
    @Operation(summary = "Phase 2: Creation", 
               description = "Creates correspondences in destination system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Phase completed successfully"),
        @ApiResponse(responseCode = "400", description = "Phase failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @Transactional(timeout = 900)
    public ResponseEntity<ImportResponseDto> executeCreation() {
        logger.info("Received request for Phase 2: Creation");
        
        try {
            ImportResponseDto response = migrationService.executeCreationPhase();
            return getResponseEntity(response);
        } catch (Exception e) {
            logger.error("Unexpected error in creation phase", e);
            return ResponseEntity.status(500).body(createErrorResponse("Unexpected error: " + e.getMessage()));
        }
    }
    
    @GetMapping("/creation/details")
    @Operation(summary = "Get Creation Phase Details", 
               description = "Returns detailed information about creation phase migrations")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Details retrieved successfully")
    })
    @Transactional(readOnly = true, timeout = 60)
    public ResponseEntity<Map<String, Object>> getCreationDetails() {
        logger.info("Received request for creation phase details");
        
        try {
            Map<String, Object> migrations = migrationService.getCreationMigrationsWithDetails();
            return ResponseEntity.ok(migrations);
        } catch (Exception e) {
            logger.error("Error getting creation details", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("content", new ArrayList<>());
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    @GetMapping("/creation/statistics")
    @Operation(summary = "Get Creation Phase Statistics", 
               description = "Returns statistics about creation phase status distribution")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully")
    })
    @Transactional(readOnly = true, timeout = 60)
    public ResponseEntity<Map<String, Object>> getCreationStatistics() {
        logger.info("Received request for creation phase statistics");
        
        try {
            Map<String, Object> statistics = migrationService.getCreationStatistics();
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            logger.error("Error getting creation statistics", e);
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
    @Operation(summary = "Execute Creation for Specific Correspondences", 
               description = "Executes creation phase for specified correspondence GUIDs")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Execution completed successfully"),
        @ApiResponse(responseCode = "400", description = "Execution failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @Transactional(timeout = 900)
    public ResponseEntity<ImportResponseDto> executeCreationForSpecific(@RequestBody Map<String, List<String>> request) {
        List<String> correspondenceGuids = request.get("correspondenceGuids");
        logger.info("Received request to execute creation for {} specific correspondences", 
                   correspondenceGuids != null ? correspondenceGuids.size() : 0);
        
        try {
            ImportResponseDto response = migrationService.executeCreationForSpecific(correspondenceGuids);
            return getResponseEntity(response);
        } catch (Exception e) {
            logger.error("Unexpected error in execute creation for specific", e);
            return ResponseEntity.status(500).body(createErrorResponse("Unexpected error: " + e.getMessage()));
        }
    }
    
    @PostMapping("/assignment")
    @Operation(summary = "Phase 3: Assignment", 
               description = "Assigns correspondences to users/departments")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Phase completed successfully"),
        @ApiResponse(responseCode = "400", description = "Phase failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ImportResponseDto> executeAssignment() {
        logger.info("Received request for Phase 3: Assignment");
        
        try {
            ImportResponseDto response = migrationService.executeAssignmentPhase();
            return getResponseEntity(response);
        } catch (Exception e) {
            logger.error("Unexpected error in assignment phase", e);
            return ResponseEntity.status(500).body(createErrorResponse("Unexpected error: " + e.getMessage()));
        }
    }
    
    @GetMapping("/assignment/details")
    @Operation(summary = "Get Assignment Phase Details", 
               description = "Returns detailed information about assignment phase migrations")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Details retrieved successfully")
    })
    @Transactional(readOnly = true, timeout = 60)
    public ResponseEntity<Map<String, Object>> getAssignmentDetails(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "all") String status,
            @RequestParam(defaultValue = "") String search) {
        logger.info("Received request for assignment phase details - page: {}, size: {}, status: {}, search: '{}'", 
                   page, size, status, search);
        
        try {
            Map<String, Object> assignments = migrationService.getAssignmentMigrations(page, size, status, search);
            return ResponseEntity.ok(assignments);
        } catch (Exception e) {
            logger.error("Error getting assignment details", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("content", new ArrayList<>());
            errorResponse.put("totalElements", 0L);
            errorResponse.put("totalPages", 0);
            errorResponse.put("currentPage", page);
            errorResponse.put("pageSize", size);
            errorResponse.put("hasNext", false);
            errorResponse.put("hasPrevious", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    @PostMapping("/assignment/execute-specific")
    @Operation(summary = "Execute Assignment for Specific Transactions", 
               description = "Executes assignment phase for specified transaction GUIDs")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Execution completed successfully"),
        @ApiResponse(responseCode = "400", description = "Execution failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ImportResponseDto> executeAssignmentForSpecific(@RequestBody Map<String, List<String>> request) {
        List<String> transactionGuids = request.get("transactionGuids");
        logger.info("Received request to execute assignment for {} specific transactions", 
                   transactionGuids != null ? transactionGuids.size() : 0);
        
        try {
            ImportResponseDto response = migrationService.executeAssignmentForSpecific(transactionGuids);
            return getResponseEntity(response);
        } catch (Exception e) {
            logger.error("Unexpected error in execute assignment for specific", e);
            return ResponseEntity.status(500).body(createErrorResponse("Unexpected error: " + e.getMessage()));
        }
    }
    
    @PostMapping("/business-log")
    @Operation(summary = "Phase 4: Business Log", 
               description = "Processes business logic for correspondences")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Phase completed successfully"),
        @ApiResponse(responseCode = "400", description = "Phase failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ImportResponseDto> executeBusinessLog() {
        logger.info("Received request for Phase 4: Business Log");
        
        try {
            ImportResponseDto response = migrationService.executeBusinessLogPhase();
            return getResponseEntity(response);
        } catch (Exception e) {
            logger.error("Unexpected error in business log phase", e);
            return ResponseEntity.status(500).body(createErrorResponse("Unexpected error: " + e.getMessage()));
        }
    }
    
    @GetMapping("/business-log/details")
    @Operation(summary = "Get Business Log Phase Details", 
               description = "Returns detailed information about business log phase migrations")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Details retrieved successfully")
    })
    @Transactional(readOnly = true, timeout = 60)
    public ResponseEntity<Map<String, Object>> getBusinessLogDetails(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "all") String status,
            @RequestParam(defaultValue = "") String search) {
        logger.info("Received request for business log phase details - page: {}, size: {}, status: {}, search: '{}'", 
                   page, size, status, search);
        
        try {
            Map<String, Object> businessLogs = migrationService.getBusinessLogMigrations(page, size, status, search);
            return ResponseEntity.ok(businessLogs);
        } catch (Exception e) {
            logger.error("Error getting business log details", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("content", new ArrayList<>());
            errorResponse.put("totalElements", 0L);
            errorResponse.put("totalPages", 0);
            errorResponse.put("currentPage", page);
            errorResponse.put("pageSize", size);
            errorResponse.put("hasNext", false);
            errorResponse.put("hasPrevious", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    @PostMapping("/business-log/execute-specific")
    @Operation(summary = "Execute Business Log for Specific Transactions", 
               description = "Executes business log phase for specified transaction GUIDs")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Execution completed successfully"),
        @ApiResponse(responseCode = "400", description = "Execution failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ImportResponseDto> executeBusinessLogForSpecific(@RequestBody Map<String, List<String>> request) {
        List<String> transactionGuids = request.get("transactionGuids");
        logger.info("Received request to execute business log for {} specific transactions", 
                   transactionGuids != null ? transactionGuids.size() : 0);
        
        try {
            ImportResponseDto response = migrationService.executeBusinessLogForSpecific(transactionGuids);
            return getResponseEntity(response);
        } catch (Exception e) {
            logger.error("Unexpected error in execute business log for specific", e);
            return ResponseEntity.status(500).body(createErrorResponse("Unexpected error: " + e.getMessage()));
        }
    }
    
    @PostMapping("/comment")
    @Operation(summary = "Phase 5: Comment", 
               description = "Processes comments for correspondences")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Phase completed successfully"),
        @ApiResponse(responseCode = "400", description = "Phase failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ImportResponseDto> executeComment() {
        logger.info("Received request for Phase 5: Comment");
        
        try {
            ImportResponseDto response = migrationService.executeCommentPhase();
            return getResponseEntity(response);
        } catch (Exception e) {
            logger.error("Unexpected error in comment phase", e);
            return ResponseEntity.status(500).body(createErrorResponse("Unexpected error: " + e.getMessage()));
        }
    }
    
    @PostMapping("/closing")
    @Operation(summary = "Phase 6: Closing", 
               description = "Closes correspondences that need to be closed")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Phase completed successfully"),
        @ApiResponse(responseCode = "400", description = "Phase failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ImportResponseDto> executeClosing() {
        logger.info("Received request for Phase 6: Closing");
        
        try {
            ImportResponseDto response = migrationService.executeClosingPhase();
            return getResponseEntity(response);
        } catch (Exception e) {
            logger.error("Unexpected error in closing phase", e);
            return ResponseEntity.status(500).body(createErrorResponse("Unexpected error: " + e.getMessage()));
        }
    }
    
    @GetMapping("/comment/details")
    @Operation(summary = "Get Comment Phase Details", 
               description = "Returns detailed information about comment phase migrations")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Details retrieved successfully")
    })
    @Transactional(readOnly = true, timeout = 60)
    public ResponseEntity<Map<String, Object>> getCommentDetails(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "all") String status,
            @RequestParam(defaultValue = "all") String commentType,
            @RequestParam(defaultValue = "") String search) {
        logger.info("Received request for comment phase details - page: {}, size: {}, status: {}, commentType: {}, search: '{}'", 
                   page, size, status, commentType, search);
        
        try {
            Map<String, Object> comments = migrationService.getCommentMigrations(page, size, status, commentType, search);
            return ResponseEntity.ok(comments);
        } catch (Exception e) {
            logger.error("Error getting comment details", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("content", new ArrayList<>());
            errorResponse.put("totalElements", 0L);
            errorResponse.put("totalPages", 0);
            errorResponse.put("currentPage", page);
            errorResponse.put("pageSize", size);
            errorResponse.put("hasNext", false);
            errorResponse.put("hasPrevious", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    @PostMapping("/comment/execute-specific")
    @Operation(summary = "Execute Comment for Specific Comments", 
               description = "Executes comment phase for specified comment GUIDs")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Execution completed successfully"),
        @ApiResponse(responseCode = "400", description = "Execution failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ImportResponseDto> executeCommentForSpecific(@RequestBody Map<String, List<String>> request) {
        List<String> commentGuids = request.get("commentGuids");
        logger.info("Received request to execute comment for {} specific comments", 
                   commentGuids != null ? commentGuids.size() : 0);
        
        try {
            ImportResponseDto response = migrationService.executeCommentForSpecific(commentGuids);
            return getResponseEntity(response);
        } catch (Exception e) {
            logger.error("Unexpected error in execute comment for specific", e);
            return ResponseEntity.status(500).body(createErrorResponse("Unexpected error: " + e.getMessage()));
        }
    }
    
    @GetMapping("/closing/details")
    @Operation(summary = "Get Closing Phase Details", 
               description = "Returns detailed information about closing phase migrations")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Details retrieved successfully")
    })
    @Transactional(readOnly = true, timeout = 60)
    public ResponseEntity<Map<String, Object>> getClosingDetails(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "all") String status,
            @RequestParam(defaultValue = "all") String needToClose,
            @RequestParam(defaultValue = "") String search) {
        logger.info("Received request for closing phase details - page: {}, size: {}, status: {}, needToClose: {}, search: '{}'", 
                   page, size, status, needToClose, search);
        
        try {
            Map<String, Object> closings = migrationService.getClosingMigrations(page, size, status, needToClose, search);
            return ResponseEntity.ok(closings);
        } catch (Exception e) {
            logger.error("Error getting closing details", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("content", new ArrayList<>());
            errorResponse.put("totalElements", 0L);
            errorResponse.put("totalPages", 0);
            errorResponse.put("currentPage", page);
            errorResponse.put("pageSize", size);
            errorResponse.put("hasNext", false);
            errorResponse.put("hasPrevious", false);
            errorResponse.put("needToCloseCount", 0L);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    @PostMapping("/closing/execute-specific")
    @Operation(summary = "Execute Closing for Specific Correspondences", 
               description = "Executes closing phase for specified correspondence GUIDs")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Execution completed successfully"),
        @ApiResponse(responseCode = "400", description = "Execution failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ImportResponseDto> executeClosingForSpecific(@RequestBody Map<String, List<String>> request) {
        List<String> correspondenceGuids = request.get("correspondenceGuids");
        logger.info("Received request to execute closing for {} specific correspondences", 
                   correspondenceGuids != null ? correspondenceGuids.size() : 0);
        
        try {
            ImportResponseDto response = migrationService.executeClosingForSpecific(correspondenceGuids);
            return getResponseEntity(response);
        } catch (Exception e) {
            logger.error("Unexpected error in execute closing for specific", e);
            return ResponseEntity.status(500).body(createErrorResponse("Unexpected error: " + e.getMessage()));
        }
    }
    
    @PostMapping("/retry-failed")
    @Operation(summary = "Retry Failed Migrations", 
               description = "Retries failed migrations that haven't exceeded max retry count")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Retry completed successfully"),
        @ApiResponse(responseCode = "400", description = "Retry failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ImportResponseDto> retryFailed() {
        logger.info("Received request to retry failed migrations");
        
        try {
            ImportResponseDto response = migrationService.retryFailedMigrations();
            return getResponseEntity(response);
        } catch (Exception e) {
            logger.error("Unexpected error in retry failed migrations", e);
            return ResponseEntity.status(500).body(createErrorResponse("Unexpected error: " + e.getMessage()));
        }
    }
    
    @GetMapping("/statistics")
    @Operation(summary = "Get Migration Statistics", 
               description = "Returns statistics about migration progress")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully")
    })
    @Transactional(readOnly = true, timeout = 60)
    public ResponseEntity<Map<String, Object>> getStatistics() {
        logger.info("Received request for migration statistics");
        
        try {
            Map<String, Object> statistics = migrationService.getMigrationStatistics();
            
            // Ensure all required fields are present with default values
            statistics.putIfAbsent("prepareData", 0L);
            statistics.putIfAbsent("creation", 0L);
            statistics.putIfAbsent("assignment", 0L);
            statistics.putIfAbsent("businessLog", 0L);
            statistics.putIfAbsent("comment", 0L);
            statistics.putIfAbsent("closing", 0L);
            statistics.putIfAbsent("completed", 0L);
            statistics.putIfAbsent("failed", 0L);
            statistics.putIfAbsent("inProgress", 0L);
            
            logger.info("Returning migration statistics: {}", statistics);
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            logger.error("Error getting migration statistics", e);
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("error", "Failed to get statistics: " + e.getMessage());
            
            // Add default values even in error case
            errorMap.put("prepareData", 0L);
            errorMap.put("creation", 0L);
            errorMap.put("assignment", 0L);
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