package com.importservice.controller;

import com.importservice.dto.ImportResponseDto;
import com.importservice.service.DataImportService;
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
@RequestMapping("/api/data-import")
@Tag(name = "Data Import Controller", description = "Operations for importing data from source system")
public class DataImportController {

    private static final Logger logger = LoggerFactory.getLogger(DataImportController.class);

    @Autowired
    private DataImportService dataImportService;

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
               description = "Retrieves all correspondences from database and imports all related entities for each")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Import completed successfully"),
        @ApiResponse(responseCode = "400", description = "Import failed with errors"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ImportResponseDto> importAllCorrespondencesWithRelated() {
        logger.info("Received request to import all correspondences with related data");
        ImportResponseDto response = dataImportService.importAllCorrespondencesWithRelated();
        return getResponseEntity(response);
    }
    
    // Entity count endpoints
    @GetMapping("/classifications/count")
    @Operation(summary = "Get Classifications Count", description = "Returns the count of classifications in database")
    public ResponseEntity<Map<String, Object>> getClassificationsCount() {
        long count = classificationRepository.count();
        Map<String, Object> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/contacts/count")
    @Operation(summary = "Get Contacts Count", description = "Returns the count of contacts in database")
    public ResponseEntity<Map<String, Object>> getContactsCount() {
        long count = contactRepository.count();
        Map<String, Object> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/decisions/count")
    @Operation(summary = "Get Decisions Count", description = "Returns the count of decisions in database")
    public ResponseEntity<Map<String, Object>> getDecisionsCount() {
        long count = decisionRepository.count();
        Map<String, Object> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/departments/count")
    @Operation(summary = "Get Departments Count", description = "Returns the count of departments in database")
    public ResponseEntity<Map<String, Object>> getDepartmentsCount() {
        long count = departmentRepository.count();
        Map<String, Object> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/forms/count")
    @Operation(summary = "Get Forms Count", description = "Returns the count of forms in database")
    public ResponseEntity<Map<String, Object>> getFormsCount() {
        long count = formRepository.count();
        Map<String, Object> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/form-types/count")
    @Operation(summary = "Get Form Types Count", description = "Returns the count of form types in database")
    public ResponseEntity<Map<String, Object>> getFormTypesCount() {
        long count = formTypeRepository.count();
        Map<String, Object> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/importance/count")
    @Operation(summary = "Get Importance Count", description = "Returns the count of importance levels in database")
    public ResponseEntity<Map<String, Object>> getImportanceCount() {
        long count = importanceRepository.count();
        Map<String, Object> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/positions/count")
    @Operation(summary = "Get Positions Count", description = "Returns the count of positions in database")
    public ResponseEntity<Map<String, Object>> getPositionsCount() {
        long count = positionRepository.count();
        Map<String, Object> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/pos-roles/count")
    @Operation(summary = "Get Position Roles Count", description = "Returns the count of position roles in database")
    public ResponseEntity<Map<String, Object>> getPosRolesCount() {
        long count = posRoleRepository.count();
        Map<String, Object> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/priority/count")
    @Operation(summary = "Get Priority Count", description = "Returns the count of priority levels in database")
    public ResponseEntity<Map<String, Object>> getPriorityCount() {
        long count = priorityRepository.count();
        Map<String, Object> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/roles/count")
    @Operation(summary = "Get Roles Count", description = "Returns the count of roles in database")
    public ResponseEntity<Map<String, Object>> getRolesCount() {
        long count = roleRepository.count();
        Map<String, Object> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/secrecy/count")
    @Operation(summary = "Get Secrecy Count", description = "Returns the count of secrecy levels in database")
    public ResponseEntity<Map<String, Object>> getSecrecyCount() {
        long count = secrecyRepository.count();
        Map<String, Object> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/user-positions/count")
    @Operation(summary = "Get User Positions Count", description = "Returns the count of user positions in database")
    public ResponseEntity<Map<String, Object>> getUserPositionsCount() {
        long count = userPositionRepository.count();
        Map<String, Object> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/users/count")
    @Operation(summary = "Get Users Count", description = "Returns the count of users in database")
    public ResponseEntity<Map<String, Object>> getUsersCount() {
        long count = userRepository.count();
        Map<String, Object> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/correspondences/count")
    @Operation(summary = "Get Correspondences Count", description = "Returns the count of correspondences in database")
    public ResponseEntity<Map<String, Object>> getCorrespondencesCount() {
        long count = correspondenceRepository.count();
        Map<String, Object> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }
    
    private ResponseEntity<ImportResponseDto> getResponseEntity(ImportResponseDto response) {
        if ("ERROR".equals(response.getStatus())) {
            return ResponseEntity.badRequest().body(response);
        } else {
            return ResponseEntity.ok(response);
        }
    }
    
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
}