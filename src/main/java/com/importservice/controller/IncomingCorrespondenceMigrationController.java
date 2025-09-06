package com.importservice.controller;

import com.importservice.dto.ImportResponseDto;
import com.importservice.service.IncomingCorrespondenceMigrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/incoming-correspondence-migration")
@Tag(name = "Incoming Correspondence Migration Controller", description = "Operations for migrating incoming correspondence data with multithreading support")
public class IncomingCorrespondenceMigrationController {

    private static final Logger logger = LoggerFactory.getLogger(IncomingCorrespondenceMigrationController.class);

    @Autowired
    private IncomingCorrespondenceMigrationService migrationService;

    @PostMapping("/execute-creation")
    @Operation(summary = "Execute Creation Migration", description = "Execute creation migration for all incoming correspondences using multithreading")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Migration completed successfully"),
        @ApiResponse(responseCode = "400", description = "Migration failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ImportResponseDto> executeCreation() {
        logger.info("Received request to execute creation migration for all incoming correspondences");
        ImportResponseDto response = migrationService.executeCreation();
        return getResponseEntity(response);
    }

    @PostMapping("/execute-creation/{correspondenceGuid}")
    @Operation(summary = "Execute Creation Migration for Specific Correspondence", 
               description = "Execute creation migration for a specific incoming correspondence using multithreading")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Migration completed successfully"),
        @ApiResponse(responseCode = "400", description = "Migration failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ImportResponseDto> executeCreationForSpecific(
            @Parameter(description = "Correspondence GUID") @PathVariable String correspondenceGuid) {
        logger.info("Received request to execute creation migration for correspondence: {}", correspondenceGuid);
        ImportResponseDto response = migrationService.executeCreationForSpecific(correspondenceGuid);
        return getResponseEntity(response);
    }

    @PostMapping("/execute-assignment")
    @Operation(summary = "Execute Assignment Migration", description = "Execute assignment migration for all incoming correspondences using multithreading")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Migration completed successfully"),
        @ApiResponse(responseCode = "400", description = "Migration failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ImportResponseDto> executeAssignment() {
        logger.info("Received request to execute assignment migration for all incoming correspondences");
        ImportResponseDto response = migrationService.executeAssignment();
        return getResponseEntity(response);
    }

    @PostMapping("/execute-business-log")
    @Operation(summary = "Execute Business Log Migration", description = "Execute business log migration for all incoming correspondences using multithreading")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Migration completed successfully"),
        @ApiResponse(responseCode = "400", description = "Migration failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ImportResponseDto> executeBusinessLog() {
        logger.info("Received request to execute business log migration for all incoming correspondences");
        ImportResponseDto response = migrationService.executeBusinessLog();
        return getResponseEntity(response);
    }

    @PostMapping("/execute-comment")
    @Operation(summary = "Execute Comment Migration", description = "Execute comment migration for all incoming correspondences using multithreading")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Migration completed successfully"),
        @ApiResponse(responseCode = "400", description = "Migration failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ImportResponseDto> executeComment() {
        logger.info("Received request to execute comment migration for all incoming correspondences");
        ImportResponseDto response = migrationService.executeComment();
        return getResponseEntity(response);
    }

    @PostMapping("/execute-closing")
    @Operation(summary = "Execute Closing Migration", description = "Execute closing migration for all incoming correspondences using multithreading")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Migration completed successfully"),
        @ApiResponse(responseCode = "400", description = "Migration failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ImportResponseDto> executeClosing() {
        logger.info("Received request to execute closing migration for all incoming correspondences");
        ImportResponseDto response = migrationService.executeClosing();
        return getResponseEntity(response);
    }

    @GetMapping("/status")
    @Operation(summary = "Get Migration Status", description = "Get the current status of migration operations")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Status retrieved successfully")
    })
    public ResponseEntity<ImportResponseDto> getMigrationStatus() {
        logger.info("Received request to get migration status");
        ImportResponseDto response = migrationService.getMigrationStatus();
        return ResponseEntity.ok(response);
    }

    private ResponseEntity<ImportResponseDto> getResponseEntity(ImportResponseDto response) {
        if ("ERROR".equals(response.getStatus())) {
            return ResponseEntity.badRequest().body(response);
        } else {
            return ResponseEntity.ok(response);
        }
    }
}