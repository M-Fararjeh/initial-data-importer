package com.importservice.controller.incoming;

import com.importservice.dto.ImportResponseDto;
import com.importservice.service.migration.incoming.IncomingCorrespondenceMigrationService;
import com.importservice.service.migration.incoming.CreationPhaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/api/incoming-migration")
@Tag(name = "Incoming Correspondence Migration Controller", description = "Operations for migrating incoming correspondences")
@CrossOrigin(origins = "*")
public class IncomingCorrespondenceMigrationController {
    
    private static final Logger logger = LoggerFactory.getLogger(IncomingCorrespondenceMigrationController.class);
    
    @Autowired
    private IncomingCorrespondenceMigrationService migrationService;
    
    @Autowired
    private CreationPhaseService creationPhaseService;
    
    // Multithreading configuration
    @Value("${migration.creation.thread-pool.core-size:5}")
    private int corePoolSize;
    
    @Value("${migration.creation.thread-pool.max-size:10}")
    private int maxPoolSize;
    
    @Value("${migration.creation.thread-pool.queue-capacity:50}")
    private int queueCapacity;
    
    @Value("${migration.creation.thread-pool.keep-alive-seconds:60}")
    private int keepAliveSeconds;
    
    @Value("${migration.creation.thread-pool.thread-name-prefix:CreationPhase-}")
    private String threadNamePrefix;
    
    @Value("${migration.creation.concurrent-correspondences:5}")
    private int concurrentCorrespondences;
    
    @Value("${migration.creation.processing-delay-ms:200}")
    private long processingDelayMs;
    
    private ThreadPoolTaskExecutor taskExecutor;
    
    @PostConstruct
    public void initializeThreadPool() {
        taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(corePoolSize);
        taskExecutor.setMaxPoolSize(maxPoolSize);
        taskExecutor.setQueueCapacity(queueCapacity);
        taskExecutor.setKeepAliveSeconds(keepAliveSeconds);
        taskExecutor.setThreadNamePrefix(threadNamePrefix);
        taskExecutor.setWaitForTasksToCompleteOnShutdown(true);
        taskExecutor.setAwaitTerminationSeconds(60);
        taskExecutor.initialize();
        
        logger.info("Initialized creation phase thread pool - Core: {}, Max: {}, Queue: {}, Concurrent: {}", 
                   corePoolSize, maxPoolSize, queueCapacity, concurrentCorrespondences);
    }
    
    @PreDestroy
    public void shutdownThreadPool() {
        if (taskExecutor != null) {
            logger.info("Shutting down creation phase thread pool");
            taskExecutor.shutdown();
        }
    }
    
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
        
        try {
            ImportResponseDto response = executeCreationWithMultithreading();
            return getResponseEntity(response);
        } catch (Exception e) {
            logger.error("Unexpected error in creation phase", e);
            return ResponseEntity.status(500).body(createErrorResponse("Unexpected error: " + e.getMessage()));
        }
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
        
        try {
            ImportResponseDto response = executeCreationForSpecificWithMultithreading(correspondenceGuids);
            return getResponseEntity(response);
        } catch (Exception e) {
            logger.error("Unexpected error in execute creation for specific", e);
            return ResponseEntity.status(500).body(createErrorResponse("Unexpected error: " + e.getMessage()));
        }
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
    
    /**
     * Executes creation phase with multithreading support
     */
    private ImportResponseDto executeCreationWithMultithreading() {
        logger.info("Starting Phase 2: Creation with {} concurrent threads", concurrentCorrespondences);
        
        try {
            // Get all correspondences that need creation
            List<com.importservice.entity.IncomingCorrespondenceMigration> migrations = 
                migrationService.getCreationMigrations().stream()
                .filter(m -> "CREATION".equals(m.getCurrentPhase()))
                .collect(java.util.stream.Collectors.toList());
            
            if (migrations.isEmpty()) {
                return createErrorResponse("No correspondences found in CREATION phase");
            }
            
            List<String> correspondenceGuids = migrations.stream()
                .map(com.importservice.entity.IncomingCorrespondenceMigration::getCorrespondenceGuid)
                .collect(java.util.stream.Collectors.toList());
            
            return executeCreationForSpecificWithMultithreading(correspondenceGuids);
            
        } catch (Exception e) {
            logger.error("Error in multithreaded creation phase", e);
            return createErrorResponse("Multithreaded creation failed: " + e.getMessage());
        }
    }
    
    /**
     * Executes creation for specific correspondences with multithreading support
     */
    private ImportResponseDto executeCreationForSpecificWithMultithreading(List<String> correspondenceGuids) {
        if (correspondenceGuids == null || correspondenceGuids.isEmpty()) {
            return createErrorResponse("No correspondence GUIDs provided");
        }
        
        logger.info("Starting multithreaded creation for {} correspondences with {} concurrent threads", 
                   correspondenceGuids.size(), concurrentCorrespondences);
        
        AtomicInteger successfulImports = new AtomicInteger(0);
        AtomicInteger failedImports = new AtomicInteger(0);
        List<String> errors = Collections.synchronizedList(new ArrayList<>());
        
        try {
            // Split correspondences into batches for concurrent processing
            List<List<String>> batches = createBatches(correspondenceGuids, concurrentCorrespondences);
            
            for (int batchIndex = 0; batchIndex < batches.size(); batchIndex++) {
                List<String> batch = batches.get(batchIndex);
                logger.info("Processing batch {} of {} ({} correspondences)", 
                           batchIndex + 1, batches.size(), batch.size());
                
                // Process batch concurrently
                List<CompletableFuture<Boolean>> futures = new ArrayList<>();
                
                for (String correspondenceGuid : batch) {
                    CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> {
                        try {
                            logger.debug("Processing correspondence in thread: {}", correspondenceGuid);
                            return creationPhaseService.processCorrespondenceCreationInNewTransaction(correspondenceGuid);
                        } catch (Exception e) {
                            logger.error("Error processing correspondence {}: {}", correspondenceGuid, e.getMessage());
                            errors.add("Error processing correspondence " + correspondenceGuid + ": " + e.getMessage());
                            return false;
                        }
                    }, taskExecutor);
                    
                    futures.add(future);
                }
                
                // Wait for all futures in this batch to complete
                for (int i = 0; i < futures.size(); i++) {
                    try {
                        Boolean result = futures.get(i).get();
                        String correspondenceGuid = batch.get(i);
                        
                        if (result) {
                            successfulImports.incrementAndGet();
                            logger.info("✅ Successfully completed creation for correspondence: {}", correspondenceGuid);
                        } else {
                            failedImports.incrementAndGet();
                            logger.warn("❌ Failed to complete creation for correspondence: {}", correspondenceGuid);
                        }
                    } catch (Exception e) {
                        failedImports.incrementAndGet();
                        String correspondenceGuid = batch.get(i);
                        String errorMsg = "Future execution failed for correspondence " + correspondenceGuid + ": " + e.getMessage();
                        errors.add(errorMsg);
                        logger.error(errorMsg, e);
                    }
                }
                
                // Add delay between batches to reduce system load
                if (batchIndex < batches.size() - 1) {
                    Thread.sleep(processingDelayMs);
                }
                
                // Log progress
                logger.info("Batch {} completed - Total processed: {}, Success: {}, Failed: {}", 
                           batchIndex + 1, successfulImports.get() + failedImports.get(), 
                           successfulImports.get(), failedImports.get());
            }
            
            // Create final response
            String status = determineFinalStatus(successfulImports.get(), failedImports.get());
            String message = String.format("Multithreaded creation completed. Created: %d, Failed: %d (Processed %d batches with %d concurrent threads)", 
                                         successfulImports.get(), failedImports.get(), batches.size(), concurrentCorrespondences);
            
            ImportResponseDto response = new ImportResponseDto();
            response.setStatus(status);
            response.setMessage(message);
            response.setTotalRecords(correspondenceGuids.size());
            response.setSuccessfulImports(successfulImports.get());
            response.setFailedImports(failedImports.get());
            response.setErrors(errors);
            
            logger.info("Multithreaded creation completed - Total: {}, Success: {}, Failed: {}", 
                       correspondenceGuids.size(), successfulImports.get(), failedImports.get());
            
            return response;
            
        } catch (Exception e) {
            logger.error("Error in multithreaded creation execution", e);
            return createErrorResponse("Multithreaded creation failed: " + e.getMessage());
        }
    }
    
    /**
     * Creates batches of correspondence GUIDs for concurrent processing
     */
    private List<List<String>> createBatches(List<String> correspondenceGuids, int batchSize) {
        List<List<String>> batches = new ArrayList<>();
        
        for (int i = 0; i < correspondenceGuids.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, correspondenceGuids.size());
            List<String> batch = correspondenceGuids.subList(i, endIndex);
            batches.add(new ArrayList<>(batch)); // Create new list to avoid sublist issues
        }
        
        logger.debug("Created {} batches from {} correspondences (batch size: {})", 
                    batches.size(), correspondenceGuids.size(), batchSize);
        
        return batches;
    }
    
    /**
     * Determines final status based on success/failure counts
     */
    private String determineFinalStatus(int successfulImports, int failedImports) {
        if (failedImports == 0) {
            return "SUCCESS";
        } else if (successfulImports > 0) {
            return "PARTIAL_SUCCESS";
        } else {
            return "ERROR";
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