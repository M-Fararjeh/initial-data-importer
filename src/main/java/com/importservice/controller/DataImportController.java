package com.importservice.controller;

import com.importservice.dto.ImportResponseDto;
import com.importservice.service.DataImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/data-import")
@Tag(name = "Data Import Controller", description = "Operations for importing data from source system")
public class DataImportController {

    private static final Logger logger = LoggerFactory.getLogger(DataImportController.class);

    @Autowired
    private DataImportService dataImportService;

    @PostMapping("/classifications")
    @Operation(summary = "Import Classifications", description = "Import classification data from source API")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Import completed successfully"),
        @ApiResponse(responseCode = "400", description = "Import failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ImportResponseDto> importClassifications() {
        logger.info("Received request to import classifications");
        ImportResponseDto response = dataImportService.importClassifications();
        return getResponseEntity(response);
    }

    @PostMapping("/contacts")
    @Operation(summary = "Import Contacts", description = "Import contact data from source API")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Import completed successfully"),
        @ApiResponse(responseCode = "400", description = "Import failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ImportResponseDto> importContacts() {
        logger.info("Received request to import contacts");
        ImportResponseDto response = dataImportService.importContacts();
        return getResponseEntity(response);
    }

    @PostMapping("/correspondences")
    @Operation(summary = "Import Correspondences", description = "Import correspondence data from source API")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Import completed successfully"),
        @ApiResponse(responseCode = "400", description = "Import failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ImportResponseDto> importCorrespondences() {
        logger.info("Received request to import correspondences");
        ImportResponseDto response = dataImportService.importCorrespondences();
        return getResponseEntity(response);
    }

    @PostMapping("/all")
    @Operation(summary = "Import All Data", description = "Import all entity types from source API")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Import completed successfully"),
        @ApiResponse(responseCode = "400", description = "Import failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ImportResponseDto> importAllData() {
        logger.info("Received request to import all data");
        
        // Import basic entities first
        dataImportService.importClassifications();
        dataImportService.importContacts();
        
        // Then import correspondences
        ImportResponseDto response = dataImportService.importCorrespondences();
        
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