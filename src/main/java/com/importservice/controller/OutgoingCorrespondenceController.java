package com.importservice.controller;

import com.importservice.dto.ImportResponseDto;
import com.importservice.service.OutgoingCorrespondenceImportService;
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
@RequestMapping("/api/outgoing-correspondence")
@Tag(name = "Outgoing Correspondence Controller", description = "Operations for importing outgoing correspondence data")
public class OutgoingCorrespondenceController {

    private static final Logger logger = LoggerFactory.getLogger(OutgoingCorrespondenceController.class);

    @Autowired
    private OutgoingCorrespondenceImportService outgoingCorrespondenceImportService;

    @PostMapping("/correspondences")
    @Operation(summary = "Import Outgoing Correspondences", description = "Import outgoing correspondence data from source API")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Import completed successfully"),
        @ApiResponse(responseCode = "400", description = "Import failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ImportResponseDto> importOutgoingCorrespondences() {
        logger.info("Received request to import outgoing correspondences");
        ImportResponseDto response = outgoingCorrespondenceImportService.importOutgoingCorrespondences();
        return getResponseEntity(response);
    }

    @PostMapping("/business-logs/{docGuid}")
    @Operation(summary = "Import Outgoing Correspondence Business Logs", description = "Import business log data for specific outgoing correspondence")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Import completed successfully"),
        @ApiResponse(responseCode = "400", description = "Import failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ImportResponseDto> importOutgoingCorrespondenceBusinessLogs(
            @Parameter(description = "Document GUID") @PathVariable String docGuid) {
        logger.info("Received request to import outgoing correspondence business logs for doc: {}", docGuid);
        ImportResponseDto response = outgoingCorrespondenceImportService.importOutgoingCorrespondenceBusinessLogs(docGuid);
        return getResponseEntity(response);
    }

    @PostMapping("/comments/{docGuid}")
    @Operation(summary = "Import Outgoing Correspondence Comments", description = "Import comment data for specific outgoing correspondence")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Import completed successfully"),
        @ApiResponse(responseCode = "400", description = "Import failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ImportResponseDto> importOutgoingCorrespondenceComments(
            @Parameter(description = "Document GUID") @PathVariable String docGuid) {
        logger.info("Received request to import outgoing correspondence comments for doc: {}", docGuid);
        ImportResponseDto response = outgoingCorrespondenceImportService.importOutgoingCorrespondenceComments(docGuid);
        return getResponseEntity(response);
    }

    @PostMapping("/closings/{docGuid}")
    @Operation(summary = "Import Outgoing Correspondence Closings", description = "Import closing data for specific outgoing correspondence")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Import completed successfully"),
        @ApiResponse(responseCode = "400", description = "Import failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ImportResponseDto> importOutgoingCorrespondenceClosings(
            @Parameter(description = "Document GUID") @PathVariable String docGuid) {
        logger.info("Received request to import outgoing correspondence closings for doc: {}", docGuid);
        ImportResponseDto response = outgoingCorrespondenceImportService.importOutgoingCorrespondenceClosings(docGuid);
        return getResponseEntity(response);
    }

    @PostMapping("/all-outgoing-related/{docGuid}")
    @Operation(summary = "Import All Outgoing Correspondence Related Data", description = "Import all outgoing correspondence-related data for specific document")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Import completed successfully"),
        @ApiResponse(responseCode = "400", description = "Import failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ImportResponseDto> importAllOutgoingCorrespondenceRelated(
            @Parameter(description = "Document GUID") @PathVariable String docGuid) {
        logger.info("Received request to import all outgoing correspondence-related data for doc: {}", docGuid);
        ImportResponseDto response = outgoingCorrespondenceImportService.importAllOutgoingCorrespondenceRelated(docGuid);
        return getResponseEntity(response);
    }

    @PostMapping("/all-outgoing-correspondences-with-related")
    @Operation(summary = "Import All Outgoing Correspondences with Related Data", 
               description = "Retrieves all outgoing correspondences from database and imports all related entities for each")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Import completed successfully"),
        @ApiResponse(responseCode = "400", description = "Import failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ImportResponseDto> importAllOutgoingCorrespondencesWithRelated() {
        logger.info("Received request to import all outgoing correspondences with related data");
        ImportResponseDto response = outgoingCorrespondenceImportService.importAllOutgoingCorrespondencesWithRelated();
        return getResponseEntity(response);
    }

    private ResponseEntity<ImportResponseDto> getResponseEntity(ImportResponseDto response) {
        if ("ERROR".equals(response.getStatus())) {
            return ResponseEntity.badRequest().body(response);
        } else {
            return ResponseEntity.ok(response);
        }
    }
}