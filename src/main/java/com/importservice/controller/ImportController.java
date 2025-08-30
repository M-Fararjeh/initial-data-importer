package com.importservice.controller;

import com.importservice.dto.ImportResponseDto;
import com.importservice.service.ExternalAgencyImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/import")
@Tag(name = "Import Controller", description = "Operations for importing external data")
public class ImportController {

    private static final Logger logger = LoggerFactory.getLogger(ImportController.class);

    @Autowired
    private ExternalAgencyImportService importService;

    @PostMapping("/external-agencies")
    @Operation(summary = "Import External Agencies", 
               description = "Imports external agency data from JSON file to destination API")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Import completed successfully"),
        @ApiResponse(responseCode = "400", description = "Import failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ImportResponseDto> importExternalAgencies() {
        logger.info("Received request to import external agencies");
        
        try {
            ImportResponseDto response = importService.importExternalAgencies();
            
            if ("ERROR".equals(response.getStatus())) {
                return ResponseEntity.badRequest().body(response);
            } else if ("PARTIAL_SUCCESS".equals(response.getStatus())) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.ok(response);
            }
            
        } catch (Exception e) {
            logger.error("Unexpected error during import process", e);
            
            ImportResponseDto errorResponse = new ImportResponseDto();
            errorResponse.setStatus("ERROR");
            errorResponse.setMessage("Unexpected error: " + e.getMessage());
            errorResponse.setTotalRecords(0);
            errorResponse.setSuccessfulImports(0);
            errorResponse.setFailedImports(0);
            
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @GetMapping("/status")
    @Operation(summary = "Get Import Service Status", 
               description = "Returns the current status and configuration of the import service")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Status retrieved successfully")
    })
    public ResponseEntity<Map<String, Object>> getImportStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("service", "data-import-service");
        status.put("status", "READY");
        status.put("version", "1.0.0");
        
        // Add configuration info (without exposing sensitive data)
        Map<String, Object> config = new HashMap<>();
        config.put("hasDestinationUrl", destinationApiUrl != null && !destinationApiUrl.isEmpty());
        config.put("hasAuthToken", authToken != null && !authToken.isEmpty());
        status.put("configuration", config);
        
        return ResponseEntity.ok(status);
    }

    @Autowired
    @Value("${destination.api.url}")
    private String destinationApiUrl;

    @Autowired
    @Value("${destination.api.token}")
    private String authToken;
}