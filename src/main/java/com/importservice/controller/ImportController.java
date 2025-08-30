package com.importservice.controller;

import com.importservice.dto.ImportResponseDto;
import com.importservice.service.ExternalAgencyImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/import")
public class ImportController {

    private static final Logger logger = LoggerFactory.getLogger(ImportController.class);

    @Autowired
    private ExternalAgencyImportService importService;

    @PostMapping("/external-agencies")
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
}