package com.importservice.controller;

import com.importservice.dto.ImportResponseDto;
import com.importservice.repository.*;
import com.importservice.service.DataImportService;
import com.importservice.entity.Correspondence;
import com.importservice.service.CorrespondenceRelatedImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@RequestMapping("/api/data-import")
@Tag(name = "Data Import Controller", description = "Operations for importing data from source system")
@CrossOrigin(origins = "*")
public class DataImportController {

    private static final Logger logger = LoggerFactory.getLogger(DataImportController.class);

    @Autowired
    private DataImportService dataImportService;

    @Autowired
    private CorrespondenceRelatedImportService correspondenceRelatedImportService;

    // Inject repositories for count endpoints
    @Autowired
    private ClassificationRepository classificationRepository;
    
    @Autowired
    private ContactRepository contactRepository;
    
    @Autowired
    private DecisionRepository decisionRepository;
    
    @Autowired
    private DepartmentRepository departmentRepository;
    
    @Autowired
    private FormRepository formRepository;
    
    @Autowired
    private FormTypeRepository formTypeRepository;
    
    @Autowired
    private ImportanceRepository importanceRepository;
    
    @Autowired
    private PositionRepository positionRepository;
    
    @Autowired
    private PosRoleRepository posRoleRepository;
    
    @Autowired
    private PriorityRepository priorityRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private SecrecyRepository secrecyRepository;
    
    @Autowired
    private UserPositionRepository userPositionRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CorrespondenceRepository correspondenceRepository;

    @Autowired
    private CorrespondenceAttachmentRepository correspondenceAttachmentRepository;
    
    @Autowired
    private CorrespondenceCommentRepository correspondenceCommentRepository;
    
    @Autowired
    private CorrespondenceTransactionRepository correspondenceTransactionRepository;

    // Basic entity imports
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

    @PostMapping("/decisions")
    @Operation(summary = "Import Decisions", description = "Import decision data from source API")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Import completed successfully"),
        @ApiResponse(responseCode = "400", description = "Import failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ImportResponseDto> importDecisions() {
        logger.info("Received request to import decisions");
        ImportResponseDto response = dataImportService.importDecisions();
        return getResponseEntity(response);
    }

    @PostMapping("/departments")
    @Operation(summary = "Import Departments", description = "Import department data from source API")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Import completed successfully"),
        @ApiResponse(responseCode = "400", description = "Import failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ImportResponseDto> importDepartments() {
        logger.info("Received request to import departments");
        ImportResponseDto response = dataImportService.importDepartments();
        return getResponseEntity(response);
    }

    @PostMapping("/forms")
    @Operation(summary = "Import Forms", description = "Import form data from source API")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Import completed successfully"),
        @ApiResponse(responseCode = "400", description = "Import failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ImportResponseDto> importForms() {
        logger.info("Received request to import forms");
        ImportResponseDto response = dataImportService.importForms();
        return getResponseEntity(response);
    }

    @PostMapping("/form-types")
    @Operation(summary = "Import Form Types", description = "Import form type data from source API")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Import completed successfully"),
        @ApiResponse(responseCode = "400", description = "Import failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ImportResponseDto> importFormTypes() {
        logger.info("Received request to import form types");
        ImportResponseDto response = dataImportService.importFormTypes();
        return getResponseEntity(response);
    }

    @PostMapping("/importance")
    @Operation(summary = "Import Importance", description = "Import importance data from source API")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Import completed successfully"),
        @ApiResponse(responseCode = "400", description = "Import failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ImportResponseDto> importImportance() {
        logger.info("Received request to import importance");
        ImportResponseDto response = dataImportService.importImportance();
        return getResponseEntity(response);
    }

    @PostMapping("/positions")
    @Operation(summary = "Import Positions", description = "Import position data from source API")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Import completed successfully"),
        @ApiResponse(responseCode = "400", description = "Import failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ImportResponseDto> importPositions() {
        logger.info("Received request to import positions");
        ImportResponseDto response = dataImportService.importPositions();
        return getResponseEntity(response);
    }

    @PostMapping("/pos-roles")
    @Operation(summary = "Import Position Roles", description = "Import position role data from source API")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Import completed successfully"),
        @ApiResponse(responseCode = "400", description = "Import failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ImportResponseDto> importPosRoles() {
        logger.info("Received request to import pos roles");
        ImportResponseDto response = dataImportService.importPosRoles();
        return getResponseEntity(response);
    }

    @PostMapping("/priority")
    @Operation(summary = "Import Priority", description = "Import priority data from source API")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Import completed successfully"),
        @ApiResponse(responseCode = "400", description = "Import failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ImportResponseDto> importPriority() {
        logger.info("Received request to import priority");
        ImportResponseDto response = dataImportService.importPriority();
        return getResponseEntity(response);
    }

    @PostMapping("/roles")
    @Operation(summary = "Import Roles", description = "Import role data from source API")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Import completed successfully"),
        @ApiResponse(responseCode = "400", description = "Import failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ImportResponseDto> importRoles() {
        logger.info("Received request to import roles");
        ImportResponseDto response = dataImportService.importRoles();
        return getResponseEntity(response);
    }

    @PostMapping("/secrecy")
    @Operation(summary = "Import Secrecy", description = "Import secrecy data from source API")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Import completed successfully"),
        @ApiResponse(responseCode = "400", description = "Import failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ImportResponseDto> importSecrecy() {
        logger.info("Received request to import secrecy");
        ImportResponseDto response = dataImportService.importSecrecy();
        return getResponseEntity(response);
    }

    @PostMapping("/user-positions")
    @Operation(summary = "Import User Positions", description = "Import user position data from source API")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Import completed successfully"),
        @ApiResponse(responseCode = "400", description = "Import failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ImportResponseDto> importUserPositions() {
        logger.info("Received request to import user positions");
        ImportResponseDto response = dataImportService.importUserPositions();
        return getResponseEntity(response);
    }

    @PostMapping("/users")
    @Operation(summary = "Import Users", description = "Import user data from source API")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Import completed successfully"),
        @ApiResponse(responseCode = "400", description = "Import failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ImportResponseDto> importUsers() {
        logger.info("Received request to import users");
        ImportResponseDto response = dataImportService.importUsers();
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

    // Correspondence-related entity imports
    @PostMapping("/correspondence-attachments/{docGuid}")
    @Operation(summary = "Import Correspondence Attachments", description = "Import correspondence attachment data for specific document")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Import completed successfully"),
        @ApiResponse(responseCode = "400", description = "Import failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ImportResponseDto> importCorrespondenceAttachments(
            @Parameter(description = "Document GUID") @PathVariable String docGuid) {
        logger.info("Received request to import correspondence attachments for doc: {}", docGuid);
        ImportResponseDto response = dataImportService.importCorrespondenceAttachments(docGuid);
        return getResponseEntity(response);
    }

    @PostMapping("/correspondence-comments/{docGuid}")
    @Operation(summary = "Import Correspondence Comments", description = "Import correspondence comment data for specific document")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Import completed successfully"),
        @ApiResponse(responseCode = "400", description = "Import failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ImportResponseDto> importCorrespondenceComments(
            @Parameter(description = "Document GUID") @PathVariable String docGuid) {
        logger.info("Received request to import correspondence comments for doc: {}", docGuid);
        ImportResponseDto response = dataImportService.importCorrespondenceComments(docGuid);
        return getResponseEntity(response);
    }

    @PostMapping("/correspondence-copy-tos/{docGuid}")
    @Operation(summary = "Import Correspondence Copy Tos", description = "Import correspondence copy to data for specific document")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Import completed successfully"),
        @ApiResponse(responseCode = "400", description = "Import failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ImportResponseDto> importCorrespondenceCopyTos(
            @Parameter(description = "Document GUID") @PathVariable String docGuid) {
        logger.info("Received request to import correspondence copy tos for doc: {}", docGuid);
        ImportResponseDto response = dataImportService.importCorrespondenceCopyTos(docGuid);
        return getResponseEntity(response);
    }

    @PostMapping("/correspondence-current-departments/{docGuid}")
    @Operation(summary = "Import Correspondence Current Departments", description = "Import correspondence current department data for specific document")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Import completed successfully"),
        @ApiResponse(responseCode = "400", description = "Import failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ImportResponseDto> importCorrespondenceCurrentDepartments(
            @Parameter(description = "Document GUID") @PathVariable String docGuid) {
        logger.info("Received request to import correspondence current departments for doc: {}", docGuid);
        ImportResponseDto response = dataImportService.importCorrespondenceCurrentDepartments(docGuid);
        return getResponseEntity(response);
    }

    @PostMapping("/correspondence-current-positions/{docGuid}")
    @Operation(summary = "Import Correspondence Current Positions", description = "Import correspondence current position data for specific document")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Import completed successfully"),
        @ApiResponse(responseCode = "400", description = "Import failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ImportResponseDto> importCorrespondenceCurrentPositions(
            @Parameter(description = "Document GUID") @PathVariable String docGuid) {
        logger.info("Received request to import correspondence current positions for doc: {}", docGuid);
        ImportResponseDto response = dataImportService.importCorrespondenceCurrentPositions(docGuid);
        return getResponseEntity(response);
    }

    @PostMapping("/correspondence-current-users/{docGuid}")
    @Operation(summary = "Import Correspondence Current Users", description = "Import correspondence current user data for specific document")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Import completed successfully"),
        @ApiResponse(responseCode = "400", description = "Import failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ImportResponseDto> importCorrespondenceCurrentUsers(
            @Parameter(description = "Document GUID") @PathVariable String docGuid) {
        logger.info("Received request to import correspondence current users for doc: {}", docGuid);
        ImportResponseDto response = dataImportService.importCorrespondenceCurrentUsers(docGuid);
        return getResponseEntity(response);
    }

    @PostMapping("/correspondence-custom-fields/{docGuid}")
    @Operation(summary = "Import Correspondence Custom Fields", description = "Import correspondence custom field data for specific document")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Import completed successfully"),
        @ApiResponse(responseCode = "400", description = "Import failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ImportResponseDto> importCorrespondenceCustomFields(
            @Parameter(description = "Document GUID") @PathVariable String docGuid) {
        logger.info("Received request to import correspondence custom fields for doc: {}", docGuid);
        ImportResponseDto response = dataImportService.importCorrespondenceCustomFields(docGuid);
        return getResponseEntity(response);
    }

    @PostMapping("/correspondence-links/{docGuid}")
    @Operation(summary = "Import Correspondence Links", description = "Import correspondence link data for specific document")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Import completed successfully"),
        @ApiResponse(responseCode = "400", description = "Import failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ImportResponseDto> importCorrespondenceLinks(
            @Parameter(description = "Document GUID") @PathVariable String docGuid) {
        logger.info("Received request to import correspondence links for doc: {}", docGuid);
        ImportResponseDto response = dataImportService.importCorrespondenceLinks(docGuid);
        return getResponseEntity(response);
    }

    @PostMapping("/correspondence-send-tos/{docGuid}")
    @Operation(summary = "Import Correspondence Send Tos", description = "Import correspondence send to data for specific document")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Import completed successfully"),
        @ApiResponse(responseCode = "400", description = "Import failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ImportResponseDto> importCorrespondenceSendTos(
            @Parameter(description = "Document GUID") @PathVariable String docGuid) {
        logger.info("Received request to import correspondence send tos for doc: {}", docGuid);
        ImportResponseDto response = dataImportService.importCorrespondenceSendTos(docGuid);
        return getResponseEntity(response);
    }

    @PostMapping("/correspondence-transactions/{docGuid}")
    @Operation(summary = "Import Correspondence Transactions", description = "Import correspondence transaction data for specific document")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Import completed successfully"),
        @ApiResponse(responseCode = "400", description = "Import failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ImportResponseDto> importCorrespondenceTransactions(
            @Parameter(description = "Document GUID") @PathVariable String docGuid) {
        logger.info("Received request to import correspondence transactions for doc: {}", docGuid);
        ImportResponseDto response = dataImportService.importCorrespondenceTransactions(docGuid);
        return getResponseEntity(response);
    }

    @PostMapping("/basic-entities")
    @Operation(summary = "Import All Basic Entities", description = "Import all basic entity types from source API")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Import completed successfully"),
        @ApiResponse(responseCode = "400", description = "Import failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ImportResponseDto> importBasicEntities() {
        logger.info("Received request to import all basic entities");
        
        // Import all basic entities in logical order
        dataImportService.importClassifications();
        dataImportService.importContacts();
        dataImportService.importDecisions();
        dataImportService.importDepartments();
        dataImportService.importForms();
        dataImportService.importFormTypes();
        dataImportService.importImportance();
        dataImportService.importPositions();
        dataImportService.importPosRoles();
        dataImportService.importPriority();
        dataImportService.importRoles();
        dataImportService.importSecrecy();
        dataImportService.importUserPositions();
        dataImportService.importUsers();
        
        // Finally import correspondences
        ImportResponseDto response = dataImportService.importCorrespondences();
        
        return getResponseEntity(response);
    }

    @PostMapping("/all-correspondence-related/{docGuid}")
    @Operation(summary = "Import All Correspondence Related Data", description = "Import all correspondence-related data for specific document")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Import completed successfully"),
        @ApiResponse(responseCode = "400", description = "Import failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ImportResponseDto> importAllCorrespondenceRelated(
            @Parameter(description = "Document GUID") @PathVariable String docGuid) {
        logger.info("Received request to import all correspondence-related data for doc: {}", docGuid);
        
        // Import all correspondence-related entities
        dataImportService.importCorrespondenceAttachments(docGuid);
        dataImportService.importCorrespondenceComments(docGuid);
        dataImportService.importCorrespondenceCopyTos(docGuid);
        dataImportService.importCorrespondenceCurrentDepartments(docGuid);
        dataImportService.importCorrespondenceCurrentPositions(docGuid);
        dataImportService.importCorrespondenceCurrentUsers(docGuid);
        dataImportService.importCorrespondenceCustomFields(docGuid);
        dataImportService.importCorrespondenceLinks(docGuid);
        dataImportService.importCorrespondenceSendTos(docGuid);
        ImportResponseDto response = dataImportService.importCorrespondenceTransactions(docGuid);
        
        return getResponseEntity(response);
    }

    @PostMapping("/all-correspondences-with-related")
    @Operation(summary = "Import All Correspondences with Related Data", 
               description = "Import all correspondences with related data using DataImportService")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Import completed successfully"),
        @ApiResponse(responseCode = "400", description = "Import failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ImportResponseDto> importAllCorrespondencesWithRelated() {
        logger.info("Received request to import all correspondences with related data");
        
        try {
            ImportResponseDto response = dataImportService.importAllCorrespondencesWithRelated();
            return getResponseEntity(response);
        } catch (Exception e) {
            logger.error("Unexpected error during correspondence related import", e);
            return ResponseEntity.status(500).body(createErrorResponse("Unexpected error: " + e.getMessage()));
        }
    }
    
    @PostMapping("/correspondence-related/{correspondenceGuid}")
    @Operation(summary = "Import Related Data for Specific Correspondence", 
               description = "Import all related data for a specific correspondence")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Import completed successfully"),
        @ApiResponse(responseCode = "400", description = "Import failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> importCorrespondenceRelated(
            @Parameter(description = "Correspondence GUID") @PathVariable String correspondenceGuid) {
        logger.info("Received request to import related data for correspondence: {}", correspondenceGuid);
        
        try {
            ImportResponseDto result = dataImportService.importAllCorrespondenceRelated(correspondenceGuid);
            boolean success = "SUCCESS".equals(result.getStatus()) || "PARTIAL_SUCCESS".equals(result.getStatus());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("correspondenceGuid", correspondenceGuid);
            response.put("message", result.getMessage());
            response.put("details", result);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Unexpected error during correspondence related import for: {}", correspondenceGuid, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("correspondenceGuid", correspondenceGuid);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    @GetMapping("/correspondence-import-statistics")
    @Operation(summary = "Get Correspondence Import Statistics", 
               description = "Returns basic statistics about correspondence import")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully")
    })
    @Transactional(readOnly = true, timeout = 60)
    public ResponseEntity<Map<String, Object>> getCorrespondenceImportStatistics() {
        logger.info("Received request for correspondence import statistics");
        
        try {
            // Redirect to the proper correspondence import statistics endpoint
            return ResponseEntity.status(302)
                .header("Location", "/api/correspondence-import/statistics")
                .build();
        } catch (Exception e) {
            logger.error("Error getting correspondence import statistics", e);
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("error", "Failed to get statistics: " + e.getMessage());
            errorMap.put("pending", 0L);
            errorMap.put("inProgress", 0L);
            errorMap.put("completed", 0L);
            errorMap.put("failed", 0L);
            errorMap.put("total", 0L);
            return ResponseEntity.status(500).body(errorMap);
        }
    }
    
    @GetMapping("/correspondence-import-status")
    @Operation(summary = "Get All Correspondence Import Statuses", 
               description = "Returns basic import status information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Statuses retrieved successfully")
    })
    @Transactional(readOnly = true, timeout = 60)
    public ResponseEntity<List<Object>> getAllCorrespondenceImportStatuses() {
        logger.info("Received request for all correspondence import statuses");
        
        try {
            // Redirect to the proper correspondence import status endpoint
            return ResponseEntity.status(302)
                .header("Location", "/api/correspondence-import/status")
                .build();
        } catch (Exception e) {
            logger.error("Error getting all import statuses", e);
            return ResponseEntity.status(500).body(new ArrayList<>());
        }
    }

    // Count endpoints for UI
    @GetMapping("/classifications/count")
    @Operation(summary = "Get Classifications Count", description = "Get total count of classifications in database")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Count retrieved successfully")
    })
    @Transactional(readOnly = true, timeout = 30)
    public ResponseEntity<Map<String, Long>> getClassificationsCount() {
        logger.info("Received request to get classifications count");
        Map<String, Long> response = new HashMap<>();
        response.put("count", classificationRepository.count());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/contacts/count")
    @Operation(summary = "Get Contacts Count", description = "Get total count of contacts in database")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Count retrieved successfully")
    })
    @Transactional(readOnly = true, timeout = 30)
    public ResponseEntity<Map<String, Long>> getContactsCount() {
        logger.info("Received request to get contacts count");
        Map<String, Long> response = new HashMap<>();
        response.put("count", contactRepository.count());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/decisions/count")
    @Operation(summary = "Get Decisions Count", description = "Get total count of decisions in database")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Count retrieved successfully")
    })
    @Transactional(readOnly = true, timeout = 30)
    public ResponseEntity<Map<String, Long>> getDecisionsCount() {
        logger.info("Received request to get decisions count");
        Map<String, Long> response = new HashMap<>();
        response.put("count", decisionRepository.count());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/departments/count")
    @Operation(summary = "Get Departments Count", description = "Get total count of departments in database")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Count retrieved successfully")
    })
    @Transactional(readOnly = true, timeout = 30)
    public ResponseEntity<Map<String, Long>> getDepartmentsCount() {
        logger.info("Received request to get departments count");
        Map<String, Long> response = new HashMap<>();
        response.put("count", departmentRepository.count());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/forms/count")
    @Operation(summary = "Get Forms Count", description = "Get total count of forms in database")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Count retrieved successfully")
    })
    @Transactional(readOnly = true, timeout = 30)
    public ResponseEntity<Map<String, Long>> getFormsCount() {
        logger.info("Received request to get forms count");
        Map<String, Long> response = new HashMap<>();
        response.put("count", formRepository.count());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/form-types/count")
    @Operation(summary = "Get Form Types Count", description = "Get total count of form types in database")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Count retrieved successfully")
    })
    @Transactional(readOnly = true, timeout = 30)
    public ResponseEntity<Map<String, Long>> getFormTypesCount() {
        logger.info("Received request to get form types count");
        Map<String, Long> response = new HashMap<>();
        response.put("count", formTypeRepository.count());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/importance/count")
    @Operation(summary = "Get Importance Count", description = "Get total count of importance levels in database")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Count retrieved successfully")
    })
    @Transactional(readOnly = true, timeout = 30)
    public ResponseEntity<Map<String, Long>> getImportanceCount() {
        logger.info("Received request to get importance count");
        Map<String, Long> response = new HashMap<>();
        response.put("count", importanceRepository.count());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/positions/count")
    @Operation(summary = "Get Positions Count", description = "Get total count of positions in database")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Count retrieved successfully")
    })
    @Transactional(readOnly = true, timeout = 30)
    public ResponseEntity<Map<String, Long>> getPositionsCount() {
        logger.info("Received request to get positions count");
        Map<String, Long> response = new HashMap<>();
        response.put("count", positionRepository.count());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/pos-roles/count")
    @Operation(summary = "Get Position Roles Count", description = "Get total count of position roles in database")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Count retrieved successfully")
    })
    @Transactional(readOnly = true, timeout = 30)
    public ResponseEntity<Map<String, Long>> getPosRolesCount() {
        logger.info("Received request to get pos roles count");
        Map<String, Long> response = new HashMap<>();
        response.put("count", posRoleRepository.count());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/priority/count")
    @Operation(summary = "Get Priority Count", description = "Get total count of priority levels in database")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Count retrieved successfully")
    })
    @Transactional(readOnly = true, timeout = 30)
    public ResponseEntity<Map<String, Long>> getPriorityCount() {
        logger.info("Received request to get priority count");
        Map<String, Long> response = new HashMap<>();
        response.put("count", priorityRepository.count());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/roles/count")
    @Operation(summary = "Get Roles Count", description = "Get total count of roles in database")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Count retrieved successfully")
    })
    @Transactional(readOnly = true, timeout = 30)
    public ResponseEntity<Map<String, Long>> getRolesCount() {
        logger.info("Received request to get roles count");
        Map<String, Long> response = new HashMap<>();
        response.put("count", roleRepository.count());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/secrecy/count")
    @Operation(summary = "Get Secrecy Count", description = "Get total count of secrecy levels in database")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Count retrieved successfully")
    })
    @Transactional(readOnly = true, timeout = 30)
    public ResponseEntity<Map<String, Long>> getSecrecyCount() {
        logger.info("Received request to get secrecy count");
        Map<String, Long> response = new HashMap<>();
        response.put("count", secrecyRepository.count());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user-positions/count")
    @Operation(summary = "Get User Positions Count", description = "Get total count of user positions in database")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Count retrieved successfully")
    })
    @Transactional(readOnly = true, timeout = 30)
    public ResponseEntity<Map<String, Long>> getUserPositionsCount() {
        logger.info("Received request to get user positions count");
        Map<String, Long> response = new HashMap<>();
        response.put("count", userPositionRepository.count());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/count")
    @Operation(summary = "Get Users Count", description = "Get total count of users in database")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Count retrieved successfully")
    })
    @Transactional(readOnly = true, timeout = 30)
    public ResponseEntity<Map<String, Long>> getUsersCount() {
        logger.info("Received request to get users count");
        Map<String, Long> response = new HashMap<>();
        response.put("count", userRepository.count());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/correspondences/count")
    @Operation(summary = "Get Correspondences Count", description = "Get total count of correspondences in database")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Count retrieved successfully")
    })
    @Transactional(readOnly = true, timeout = 30)
    public ResponseEntity<Map<String, Long>> getCorrespondencesCount() {
        logger.info("Received request to get correspondences count");
        Map<String, Long> response = new HashMap<>();
        response.put("count", correspondenceRepository.count());
        return ResponseEntity.ok(response);
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