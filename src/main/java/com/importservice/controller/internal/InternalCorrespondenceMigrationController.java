package com.importservice.controller.internal;

import com.importservice.dto.ImportResponseDto;
import com.importservice.service.migration.internal.InternalCorrespondenceMigrationService;
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
@RequestMapping("/api/internal-migration")
@Tag(name = "Internal Correspondence Migration Controller", description = "Operations for migrating internal correspondences")
@CrossOrigin(origins = "*")
public class InternalCorrespondenceMigrationController {
    
    private static final Logger logger = LoggerFactory.getLogger(InternalCorrespondenceMigrationController.class);
    
    @Autowired
    private InternalCorrespondenceMigrationService migrationService;
    
    @PostMapping("/prepare-data")
    @Operation(summary = "Phase 1: Prepare Data", 
               description = "Prepares internal correspondences for migration")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Phase completed successfully"),
        @ApiResponse(responseCode = "400", description = "Phase failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ImportResponseDto> prepareData() {
        logger.info("Received request for Internal Phase 1: Prepare Data");
        
        try {
            ImportResponseDto response = migrationService.prepareData();
            return getResponseEntity(response);
        } catch (Exception e) {
            logger.error("Unexpected error in internal prepare data phase", e);
            return ResponseEntity.status(500).body(createErrorResponse("Unexpected error: " + e.getMessage()));
        }
    }
    
    @PostMapping("/creation")
    @Operation(summary = "Phase 2: Creation", 
               description = "Creates internal correspondences in destination system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Phase completed successfully"),
        @ApiResponse(responseCode = "400", description = "Phase failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ImportResponseDto> executeCreation() {
        logger.info("Received request for Internal Phase 2: Creation");
        
        try {
            ImportResponseDto response = migrationService.executeCreationPhase();
            return getResponseEntity(response);
        } catch (Exception e) {
            logger.error("Unexpected error in internal creation phase", e);
            return ResponseEntity.status(500).body(createErrorResponse("Unexpected error: " + e.getMessage()));
        }
    }
    
    @GetMapping("/creation/details")
    @Operation(summary = "Get Internal Creation Phase Details", 
               description = "Returns detailed information about internal creation phase migrations")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Details retrieved successfully")
    })
    @Transactional(readOnly = true, timeout = 60)
    public ResponseEntity<Map<String, Object>> getCreationDetails() {
        logger.info("Received request for internal creation phase details");
        
        try {
            Map<String, Object> migrations = migrationService.getCreationMigrationsWithDetails();
            return ResponseEntity.ok(migrations);
        } catch (Exception e) {
            logger.error("Error getting internal creation details", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("content", new ArrayList<>());
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    @GetMapping("/creation/statistics")
    @Operation(summary = "Get Internal Creation Phase Statistics", 
               description = "Returns statistics about internal creation phase status distribution")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully")
    })
    @Transactional(readOnly = true, timeout = 60)
    public ResponseEntity<Map<String, Object>> getCreationStatistics() {
        logger.info("Received request for internal creation phase statistics");
        
        try {
            Map<String, Object> statistics = migrationService.getCreationStatistics();
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            logger.error("Error getting internal creation statistics", e);
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
    @Operation(summary = "Execute Internal Creation for Specific Correspondences", 
               description = "Executes internal creation phase for specified correspondence GUIDs")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Execution completed successfully"),
        @ApiResponse(responseCode = "400", description = "Execution failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ImportResponseDto> executeCreationForSpecific(@RequestBody Map<String, List<String>> request) {
        List<String> correspondenceGuids = request.get("correspondenceGuids");
        logger.info("Received request to execute internal creation for {} specific correspondences", 
                   correspondenceGuids != null ? correspondenceGuids.size() : 0);
        
        try {
            ImportResponseDto response = migrationService.executeCreationForSpecific(correspondenceGuids);
            return getResponseEntity(response);
        } catch (Exception e) {
            logger.error("Unexpected error in execute internal creation for specific", e);
            return ResponseEntity.status(500).body(createErrorResponse("Unexpected error: " + e.getMessage()));
        }
    }
    
    @PostMapping("/assignment")
    @Operation(summary = "Phase 3: Assignment", 
               description = "Assigns internal correspondences to users/departments")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Phase completed successfully"),
        @ApiResponse(responseCode = "400", description = "Phase failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ImportResponseDto> executeAssignment() {
        logger.info("Received request for Internal Phase 3: Assignment");
        
        try {
            ImportResponseDto response = migrationService.executeAssignmentPhase();
            return getResponseEntity(response);
        } catch (Exception e) {
            logger.error("Unexpected error in internal assignment phase", e);
            return ResponseEntity.status(500).body(createErrorResponse("Unexpected error: " + e.getMessage()));
        }
    }
    
    @PostMapping("/assignment/execute-specific")
    @Operation(summary = "Execute Internal Assignment for Specific Transactions", 
               description = "Executes internal assignment phase for specified transaction GUIDs")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Execution completed successfully"),
        @ApiResponse(responseCode = "400", description = "Execution failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ImportResponseDto> executeAssignmentForSpecific(@RequestBody Map<String, List<String>> request) {
        List<String> transactionGuids = request.get("transactionGuids");
        logger.info("Received request to execute internal assignment for {} specific transactions", 
                   transactionGuids != null ? transactionGuids.size() : 0);
        
        try {
            ImportResponseDto response = migrationService.executeAssignmentForSpecific(transactionGuids);
            return getResponseEntity(response);
        } catch (Exception e) {
            logger.error("Unexpected error in execute internal assignment for specific", e);
            return ResponseEntity.status(500).body(createErrorResponse("Unexpected error: " + e.getMessage()));
        }
    }
    
    @PostMapping("/approval")
    @Operation(summary = "Phase 4: Approval", 
               description = "Approves internal correspondences and registers them")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Phase completed successfully"),
        @ApiResponse(responseCode = "400", description = "Phase failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ImportResponseDto> executeApproval() {
        logger.info("Received request for Internal Phase 4: Approval");
        
        try {
            ImportResponseDto response = migrationService.executeApprovalPhase();
            return getResponseEntity(response);
        } catch (Exception e) {
            logger.error("Unexpected error in internal approval phase", e);
            return ResponseEntity.status(500).body(createErrorResponse("Unexpected error: " + e.getMessage()));
        }
    }
    
    @GetMapping("/approval/details")
    @Operation(summary = "Get Internal Approval Phase Details", 
               description = "Returns detailed information about internal approval phase migrations")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Details retrieved successfully")
    })
    @Transactional(readOnly = true, timeout = 60)
    public ResponseEntity<Map<String, Object>> getApprovalDetails(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "all") String status,
            @RequestParam(defaultValue = "all") String step,
            @RequestParam(defaultValue = "") String search) {
        logger.info("Received request for internal approval phase details - page: {}, size: {}, status: {}, step: {}, search: '{}'", 
                   page, size, status, step, search);
        
        try {
            Map<String, Object> approvals = migrationService.getApprovalMigrations(page, size, status, step, search);
            return ResponseEntity.ok(approvals);
        } catch (Exception e) {
            logger.error("Error getting internal approval details", e);
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
    
    @GetMapping("/assignment/details")
    @Operation(summary = "Get Internal Assignment Phase Details", 
               description = "Returns detailed information about internal assignment phase migrations")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Details retrieved successfully")
    })
    @Transactional(readOnly = true, timeout = 60)
    public ResponseEntity<Map<String, Object>> getAssignmentDetails(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "all") String status,
            @RequestParam(defaultValue = "") String search) {
        logger.info("Received request for internal assignment phase details - page: {}, size: {}, status: {}, search: '{}'", 
                   page, size, status, search);
        
        try {
            Map<String, Object> assignments = migrationService.getAssignmentMigrations(page, size, status, search);
            return ResponseEntity.ok(assignments);
        } catch (Exception e) {
            logger.error("Error getting internal assignment details", e);
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
    
    @PostMapping("/approval/execute-specific")
    @Operation(summary = "Execute Internal Approval for Specific Correspondences", 
               description = "Executes internal approval phase for specified correspondence GUIDs")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Execution completed successfully"),
        @ApiResponse(responseCode = "400", description = "Execution failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ImportResponseDto> executeApprovalForSpecific(@RequestBody Map<String, List<String>> request) {
        List<String> correspondenceGuids = request.get("correspondenceGuids");
        logger.info("Received request to execute internal approval for {} specific correspondences", 
                   correspondenceGuids != null ? correspondenceGuids.size() : 0);
        
        try {
            ImportResponseDto response = migrationService.executeApprovalForSpecific(correspondenceGuids);
            return getResponseEntity(response);
        } catch (Exception e) {
            logger.error("Unexpected error in execute internal approval for specific", e);
            return ResponseEntity.status(500).body(createErrorResponse("Unexpected error: " + e.getMessage()));
        }
    }
    
    @PostMapping("/business-log")
    @Operation(summary = "Phase 5: Business Log", 
               description = "Processes business logic for internal correspondences")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Phase completed successfully"),
        @ApiResponse(responseCode = "400", description = "Phase failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ImportResponseDto> executeBusinessLog() {
        logger.info("Received request for Internal Phase 5: Business Log");
        
        try {
            ImportResponseDto response = migrationService.executeBusinessLogPhase();
            return getResponseEntity(response);
        } catch (Exception e) {
            logger.error("Unexpected error in internal business log phase", e);
            return ResponseEntity.status(500).body(createErrorResponse("Unexpected error: " + e.getMessage()));
        }
    }
    
    @PostMapping("/business-log/execute-specific")
    @Operation(summary = "Execute Internal Business Log for Specific Transactions", 
               description = "Executes internal business log phase for specified transaction GUIDs")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Execution completed successfully"),
        @ApiResponse(responseCode = "400", description = "Execution failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ImportResponseDto> executeBusinessLogForSpecific(@RequestBody Map<String, List<String>> request) {
        List<String> transactionGuids = request.get("transactionGuids");
        logger.info("Received request to execute internal business log for {} specific transactions", 
                   transactionGuids != null ? transactionGuids.size() : 0);
        
        try {
            ImportResponseDto response = migrationService.executeBusinessLogForSpecific(transactionGuids);
            return getResponseEntity(response);
        } catch (Exception e) {
            logger.error("Unexpected error in execute internal business log for specific", e);
            return ResponseEntity.status(500).body(createErrorResponse("Unexpected error: " + e.getMessage()));
        }
    }
    
    @PostMapping("/closing")
    @Operation(summary = "Phase 6: Closing", 
               description = "Closes internal correspondences that need archiving")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Phase completed successfully"),
        @ApiResponse(responseCode = "400", description = "Phase failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ImportResponseDto> executeClosing() {
        logger.info("Received request for Internal Phase 6: Closing");
        
        try {
            ImportResponseDto response = migrationService.executeClosingPhase();
            return getResponseEntity(response);
        } catch (Exception e) {
            logger.error("Unexpected error in internal closing phase", e);
            return ResponseEntity.status(500).body(createErrorResponse("Unexpected error: " + e.getMessage()));
        }
    }
    
    @PostMapping("/closing/execute-specific")
    @Operation(summary = "Execute Internal Closing for Specific Correspondences",
               description = "Executes internal closing phase for specified correspondence GUIDs")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Execution completed successfully"),
        @ApiResponse(responseCode = "400", description = "Execution failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ImportResponseDto> executeClosingForSpecific(@RequestBody Map<String, List<String>> request) {
        List<String> correspondenceGuids = request.get("correspondenceGuids");
        logger.info("Received request to execute internal closing for {} specific correspondences", 
                   correspondenceGuids != null ? correspondenceGuids.size() : 0);
        
        try {
            ImportResponseDto response = migrationService.executeClosingForSpecific(correspondenceGuids);
            return getResponseEntity(response);
        } catch (Exception e) {
            logger.error("Unexpected error in execute internal closing for specific", e);
            return ResponseEntity.status(500).body(createErrorResponse("Unexpected error: " + e.getMessage()));
        }
    }
    
    @PostMapping("/retry-failed")
    @Operation(summary = "Retry Failed Internal Migrations", 
               description = "Retries failed internal migrations that haven't exceeded max retry count")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Retry completed successfully"),
        @ApiResponse(responseCode = "400", description = "Retry failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ImportResponseDto> retryFailed() {
        logger.info("Received request to retry failed internal migrations");
        
        try {
            ImportResponseDto response = migrationService.retryFailedMigrations();
            return getResponseEntity(response);
        } catch (Exception e) {
            logger.error("Unexpected error in retry failed internal migrations", e);
            return ResponseEntity.status(500).body(createErrorResponse("Unexpected error: " + e.getMessage()));
        }
    }
    
    @GetMapping("/statistics")
    @Operation(summary = "Get Internal Migration Statistics", 
               description = "Returns statistics about internal migration progress")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully")
    })
    @Transactional(readOnly = true, timeout = 60)
    public ResponseEntity<Map<String, Object>> getStatistics() {
        logger.info("Received request for internal migration statistics");
        
        try {
            Map<String, Object> statistics = migrationService.getMigrationStatistics();
            
            // Ensure all required fields are present with default values
            statistics.putIfAbsent("prepareData", 0L);
            statistics.putIfAbsent("creation", 0L);
            statistics.putIfAbsent("assignment", 0L);
            statistics.putIfAbsent("approval", 0L);
            statistics.putIfAbsent("businessLog", 0L);
            statistics.putIfAbsent("closing", 0L);
            statistics.putIfAbsent("completed", 0L);
            statistics.putIfAbsent("failed", 0L);
            statistics.putIfAbsent("inProgress", 0L);
            
            logger.info("Returning internal migration statistics: {}", statistics);
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            logger.error("Error getting internal migration statistics", e);
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("error", "Failed to get internal statistics: " + e.getMessage());
            
            // Add default values even in error case
            errorMap.put("prepareData", 0L);
            errorMap.put("creation", 0L);
            errorMap.put("assignment", 0L);
            errorMap.put("approval", 0L);
            errorMap.put("businessLog", 0L);
            errorMap.put("closing", 0L);
            errorMap.put("completed", 0L);
            errorMap.put("failed", 0L);
            errorMap.put("inProgress", 0L);
            
            return ResponseEntity.status(500).body(errorMap);
        }
    }
    
    @GetMapping("/business-log/details")
    @Operation(summary = "Get Internal Business Log Phase Details", 
               description = "Returns detailed information about internal business log phase migrations")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Details retrieved successfully")
    })
    @Transactional(readOnly = true, timeout = 60)
    public ResponseEntity<Map<String, Object>> getBusinessLogDetails(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "all") String status,
            @RequestParam(defaultValue = "") String search) {
        logger.info("Received request for internal business log phase details - page: {}, size: {}, status: {}, search: '{}'", 
                   page, size, status, search);
        
        try {
            Map<String, Object> businessLogs = migrationService.getBusinessLogMigrations(page, size, status, search);
            return ResponseEntity.ok(businessLogs);
        } catch (Exception e) {
            logger.error("Error getting internal business log details", e);
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
    
    @GetMapping("/closing/details")
    @Operation(summary = "Get Internal Closing Phase Details", 
               description = "Returns detailed information about internal closing phase migrations")
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
        logger.info("Received request for internal closing phase details - page: {}, size: {}, status: {}, needToClose: {}, search: '{}'", 
                   page, size, status, needToClose, search);
        
        try {
            Map<String, Object> closings = migrationService.getClosingMigrations(page, size, status, needToClose, search);
            return ResponseEntity.ok(closings);
        } catch (Exception e) {
            logger.error("Error getting internal closing details", e);
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