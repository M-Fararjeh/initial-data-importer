package com.importservice.controller;

import com.importservice.dto.ImportResponseDto;
import com.importservice.service.UserImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user-import")
@Tag(name = "User Import Controller", description = "Operations for importing users to destination system")
public class UserImportController {

    private static final Logger logger = LoggerFactory.getLogger(UserImportController.class);

    @Autowired
    private UserImportService userImportService;

    @PostMapping("/users-to-destination")
    @Operation(summary = "Import Users to Destination System", 
               description = "Creates users in destination system and assigns them to departments and roles")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Import completed successfully"),
        @ApiResponse(responseCode = "400", description = "Import failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ImportResponseDto> importUsersToDestination() {
        logger.info("Received request to import users to destination system");
        
        try {
            ImportResponseDto response = userImportService.importUsersToDestination();
            
            if ("ERROR".equals(response.getStatus())) {
                return ResponseEntity.badRequest().body(response);
            } else {
                return ResponseEntity.ok(response);
            }
            
        } catch (Exception e) {
            logger.error("Unexpected error during user import process", e);
            
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