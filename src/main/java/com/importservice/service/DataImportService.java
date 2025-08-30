package com.importservice.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.importservice.dto.ApiResponseDto;
import com.importservice.dto.ImportResponseDto;
import com.importservice.entity.*;
import com.importservice.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class DataImportService {

    private static final Logger logger = LoggerFactory.getLogger(DataImportService.class);

    @Value("${source.api.base-url}")
    private String sourceApiBaseUrl;

    @Value("${source.api.key}")
    private String sourceApiKey;

    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private ObjectMapper objectMapper;

    // Basic entity repositories
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

    // Correspondence-related repositories
    @Autowired
    private CorrespondenceAttachmentRepository correspondenceAttachmentRepository;

    @Autowired
    private CorrespondenceCommentRepository correspondenceCommentRepository;

    @Autowired
    private CorrespondenceCopyToRepository correspondenceCopyToRepository;

    @Autowired
    private CorrespondenceCurrentDepartmentRepository correspondenceCurrentDepartmentRepository;

    @Autowired
    private CorrespondenceCurrentPositionRepository correspondenceCurrentPositionRepository;

    @Autowired
    private CorrespondenceCurrentUserRepository correspondenceCurrentUserRepository;

    @Autowired
    private CorrespondenceCustomFieldRepository correspondenceCustomFieldRepository;

    @Autowired
    private CorrespondenceLinkRepository correspondenceLinkRepository;

    @Autowired
    private CorrespondenceSendToRepository correspondenceSendToRepository;

    @Autowired
    private CorrespondenceTransactionRepository correspondenceTransactionRepository;


    // Basic entity import methods
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW, timeout = 300)
    public ImportResponseDto importClassifications() {
        logger.debug("Starting classifications import");
        return importGenericData("/Classifications", Classification.class, classificationRepository, "Classifications");
    }

    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW, timeout = 300)
    public ImportResponseDto importContacts() {
        logger.debug("Starting contacts import");
        return importGenericData("/Contacts", Contact.class, contactRepository, "Contacts");
    }

    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW, timeout = 300)
    public ImportResponseDto importDecisions() {
        logger.debug("Starting decisions import");
        return importGenericData("/Decisions", Decision.class, decisionRepository, "Decisions");
    }

    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW, timeout = 300)
    public ImportResponseDto importDepartments() {
        logger.debug("Starting departments import");
        return importGenericData("/Departments", Department.class, departmentRepository, "Departments");
    }

    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW, timeout = 300)
    public ImportResponseDto importForms() {
        logger.debug("Starting forms import");
        return importGenericData("/Forms", Form.class, formRepository, "Forms");
    }

    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW, timeout = 300)
    public ImportResponseDto importFormTypes() {
        logger.debug("Starting form types import");
        return importGenericData("/FormTypes", FormType.class, formTypeRepository, "FormTypes");
    }

    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW, timeout = 300)
    public ImportResponseDto importImportance() {
        logger.debug("Starting importance import");
        return importGenericData("/Importance", Importance.class, importanceRepository, "Importance");
    }

    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW, timeout = 300)
    public ImportResponseDto importPositions() {
        logger.debug("Starting positions import");
        return importGenericData("/Positions", Position.class, positionRepository, "Positions");
    }

    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW, timeout = 300)
    public ImportResponseDto importPosRoles() {
        logger.debug("Starting pos roles import");
        return importGenericData("/PosRole", PosRole.class, posRoleRepository, "PosRoles");
    }

    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW, timeout = 300)
    public ImportResponseDto importPriority() {
        logger.debug("Starting priority import");
        return importGenericData("/Priority", Priority.class, priorityRepository, "Priority");
    }

    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW, timeout = 300)
    public ImportResponseDto importRoles() {
        logger.debug("Starting roles import");
        return importGenericData("/Roles", Role.class, roleRepository, "Roles");
    }

    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW, timeout = 300)
    public ImportResponseDto importSecrecy() {
        logger.debug("Starting secrecy import");
        return importGenericData("/Secrecy", Secrecy.class, secrecyRepository, "Secrecy");
    }

    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW, timeout = 300)
    public ImportResponseDto importUserPositions() {
        logger.debug("Starting user positions import");
        return importGenericData("/UserPosition", UserPosition.class, userPositionRepository, "UserPositions");
    }

    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW, timeout = 300)
    public ImportResponseDto importUsers() {
        logger.debug("Starting users import");
        return importUsersData();
    }

    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW, timeout = 300)
    public ImportResponseDto importCorrespondences() {
        logger.debug("Starting correspondences import");
        return importCorrespondenceData();
    }

    private ImportResponseDto importUsersData() {
        List<String> errors = new ArrayList<>();
        int successfulImports = 0;
        int failedImports = 0;
        int totalRecords = 0;

        try {
            String url = sourceApiBaseUrl + "/Users";
            HttpHeaders headers = createHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            
            // Check if response body is null
            String responseBody = response.getBody();
            if (responseBody == null || responseBody.trim().isEmpty()) {
                logger.warn("Empty response body for Users");
                return new ImportResponseDto("SUCCESS", "No Users found", 
                    0, 0, 0, new ArrayList<>());
            }
            
            // Create specific TypeReference for Users
            TypeReference<ApiResponseDto<User>> typeRef = new TypeReference<ApiResponseDto<User>>() {};
            ApiResponseDto<User> apiResponse = objectMapper.readValue(responseBody, typeRef);

            if (apiResponse == null || !Boolean.TRUE.equals(apiResponse.getSuccess())) {
                String message = apiResponse != null ? apiResponse.getMessage() : "Unknown API error";
                return new ImportResponseDto("ERROR", "API returned failure: " + message, 
                    0, 0, 0, Arrays.asList("API returned failure: " + message));
            }

            List<User> users = apiResponse.getData();
            totalRecords = users != null ? users.size() : 0;
            logger.info("Found {} Users to import", totalRecords);

            if (users != null) {
                for (User user : users) {
                    try {
                        if (user == null) {
                            failedImports++;
                            errors.add("Null User object received");
                            continue;
                        }
                        userRepository.save(user);
                        successfulImports++;
                        logger.debug("Successfully saved user: {}", user.getGuid());
                    } catch (Exception e) {
                        failedImports++;
                        String userGuid = user != null ? user.getGuid() : "unknown";
                        String errorMsg = "Failed to save User " + userGuid + ": " + e.getMessage();
                        errors.add(errorMsg);
                        logger.error(errorMsg, e);
                    }
                }
            } else {
                logger.info("No Users data found in API response");
            }

            String status = failedImports == 0 ? "SUCCESS" : "PARTIAL_SUCCESS";
            String message = String.format("Users import completed. Success: %d, Failed: %d", 
                                         successfulImports, failedImports);

            return new ImportResponseDto(status, message, totalRecords, successfulImports, failedImports, errors);

        } catch (Exception e) {
            logger.error("Failed to import Users", e);
            return new ImportResponseDto("ERROR", "Failed to import Users: " + e.getMessage(), 
                0, 0, 0, Arrays.asList("Failed to import Users: " + e.getMessage()));
        }
    }

    // Correspondence-related import methods
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW, timeout = 300)
    public ImportResponseDto importCorrespondenceAttachments(String docGuid) {
        logger.debug("Starting correspondence attachments import for doc: {}", docGuid);
        return importCorrespondenceRelatedData("/CorrespondenceAttachments/docGuid/" + docGuid, 
                                             CorrespondenceAttachment.class, 
                                             correspondenceAttachmentRepository, 
                                             "CorrespondenceAttachments");
    }

    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW, timeout = 300)
    public ImportResponseDto importCorrespondenceComments(String docGuid) {
        logger.debug("Starting correspondence comments import for doc: {}", docGuid);
        return importCorrespondenceRelatedData("/CorrespondenceComments/docGuid/" + docGuid, 
                                             CorrespondenceComment.class, 
                                             correspondenceCommentRepository, 
                                             "CorrespondenceComments");
    }

    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW, timeout = 300)
    public ImportResponseDto importCorrespondenceCopyTos(String docGuid) {
        logger.debug("Starting correspondence copy tos import for doc: {}", docGuid);
        return importCorrespondenceRelatedData("/CorrespondenceCopyTo/docGUId/" + docGuid, 
                                             CorrespondenceCopyTo.class, 
                                             correspondenceCopyToRepository, 
                                             "CorrespondenceCopyTos");
    }

    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW, timeout = 300)
    public ImportResponseDto importCorrespondenceCurrentDepartments(String docGuid) {
        logger.debug("Starting correspondence current departments import for doc: {}", docGuid);
        
        List<String> errors = new ArrayList<>();
        int successfulImports = 0;
        int failedImports = 0;
        int totalRecords = 0;

        try {
            String url = sourceApiBaseUrl + "/CorrespondenceCurrentDepartments/docGuid/" + docGuid;
            HttpHeaders headers = createHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            
            // Check if response body is null
            String responseBody = response.getBody();
            if (responseBody == null || responseBody.trim().isEmpty()) {
                logger.warn("Empty response body for correspondence current departments, docGuid: {}", docGuid);
                return new ImportResponseDto("SUCCESS", "No correspondence current departments found for document", 
                    0, 0, 0, new ArrayList<>());
            }
            
            TypeReference<ApiResponseDto<CorrespondenceCurrentDepartment>> typeRef = 
                new TypeReference<ApiResponseDto<CorrespondenceCurrentDepartment>>() {};
            ApiResponseDto<CorrespondenceCurrentDepartment> apiResponse = objectMapper.readValue(responseBody, typeRef);

            if (apiResponse == null || !Boolean.TRUE.equals(apiResponse.getSuccess())) {
                String message = apiResponse != null ? apiResponse.getMessage() : "Unknown API error";
                return createErrorResponse("API returned failure: " + message);
            }

            List<CorrespondenceCurrentDepartment> departments = apiResponse.getData();
            if (departments == null || departments.isEmpty()) {
                logger.info("No correspondence current departments found for docGuid: {}", docGuid);
                return new ImportResponseDto("SUCCESS", "No correspondence current departments found for document", 
                    0, 0, 0, new ArrayList<>());
            }
            
            totalRecords = departments.size();
            logger.info("Found {} correspondence current departments for docGuid: {}", totalRecords, docGuid);

            for (CorrespondenceCurrentDepartment dept : departments) {
                try {
                    if (dept == null) {
                        failedImports++;
                        errors.add("Null department object received");
                        continue;
                    }
                    dept.setDocGuid(docGuid); // Set the doc guid
                    correspondenceCurrentDepartmentRepository.save(dept);
                    successfulImports++;
                    logger.debug("Successfully saved correspondence current department for docGuid: {}", docGuid);
                } catch (Exception e) {
                    failedImports++;
                    String errorMsg = "Failed to save correspondence current department: " + e.getMessage();
                    errors.add(errorMsg);
                    logger.error(errorMsg, e);
                }
            }

            String status = failedImports == 0 ? "SUCCESS" : "PARTIAL_SUCCESS";
            String message = String.format("CorrespondenceCurrentDepartments import completed. Success: %d, Failed: %d", 
                                         successfulImports, failedImports);

            return new ImportResponseDto(status, message, totalRecords, successfulImports, failedImports, errors);

        } catch (Exception e) {
            logger.error("Failed to import correspondence current departments", e);
            return createErrorResponse("Failed to import correspondence current departments: " + e.getMessage());
        }
    }

    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW, timeout = 300)
    public ImportResponseDto importCorrespondenceCurrentPositions(String docGuid) {
        logger.debug("Starting correspondence current positions import for doc: {}", docGuid);
        return importCorrespondenceRelatedData("/CorrespondenceCurrentPositions/docGuid/" + docGuid, 
                                             CorrespondenceCurrentPosition.class, 
                                             correspondenceCurrentPositionRepository, 
                                             "CorrespondenceCurrentPositions");
    }

    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW, timeout = 300)
    public ImportResponseDto importCorrespondenceCurrentUsers(String docGuid) {
        logger.debug("Starting correspondence current users import for doc: {}", docGuid);
        return importCorrespondenceRelatedData("/CorrespondenceCurrentUsers/docGuid/" + docGuid, 
                                             CorrespondenceCurrentUser.class, 
                                             correspondenceCurrentUserRepository, 
                                             "CorrespondenceCurrentUsers");
    }

    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW, timeout = 300)
    public ImportResponseDto importCorrespondenceCustomFields(String docGuid) {
        logger.debug("Starting correspondence custom fields import for doc: {}", docGuid);
        return importCorrespondenceRelatedData("/CorrespondenceCustomFields/docGuid/" + docGuid, 
                                             CorrespondenceCustomField.class, 
                                             correspondenceCustomFieldRepository, 
                                             "CorrespondenceCustomFields");
    }

    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW, timeout = 300)
    public ImportResponseDto importCorrespondenceLinks(String docGuid) {
        logger.debug("Starting correspondence links import for doc: {}", docGuid);
        return importCorrespondenceRelatedData("/CorrespondenceLinks/docGuid/" + docGuid, 
                                             CorrespondenceLink.class, 
                                             correspondenceLinkRepository, 
                                             "CorrespondenceLinks");
    }

    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW, timeout = 300)
    public ImportResponseDto importCorrespondenceSendTos(String docGuid) {
        logger.debug("Starting correspondence send tos import for doc: {}", docGuid);
        return importCorrespondenceRelatedData("/CorrespondenceSendTo/docGUId/" + docGuid, 
                                             CorrespondenceSendTo.class, 
                                             correspondenceSendToRepository, 
                                             "CorrespondenceSendTos");
    }

    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW, timeout = 300)
    public ImportResponseDto importCorrespondenceTransactions(String docGuid) {
        logger.debug("Starting correspondence transactions import for doc: {}", docGuid);
        return importCorrespondenceRelatedData("/CorrespondenceTransactions/docGuid/" + docGuid, 
                                             CorrespondenceTransaction.class, 
                                             correspondenceTransactionRepository, 
                                             "CorrespondenceTransactions");
    }

    public ImportResponseDto importAllCorrespondencesWithRelated() {
        logger.debug("Starting bulk import of all correspondences with related data");
        
        List<String> errors = new ArrayList<>();
        int totalRecords = 0;
        int successfulImports = 0;
        int failedImports = 0;
        
        try {
            // Get all correspondences from database
            List<Correspondence> correspondences = correspondenceRepository.findAll();
            Collections.reverse(correspondences);
            totalRecords = correspondences.size();
            
            logger.info("Found {} correspondences in database to process", totalRecords);
            
            if (correspondences.isEmpty()) {
                return new ImportResponseDto("SUCCESS", 
                    "No correspondences found in database. Import correspondences first.", 
                    0, 0, 0, new ArrayList<>());
            }
            
            for (Correspondence correspondence : correspondences) {
                String docGuid = correspondence.getGuid();
                logger.info("Processing correspondence: {} ({})", docGuid, correspondence.getSubject());
                
                try {
                    // Call the helper method that handles all related entities
                    ImportResponseDto result = importAllCorrespondenceRelated(docGuid);
                    
                    if ("ERROR".equals(result.getStatus()) || "PARTIAL_SUCCESS".equals(result.getStatus())) {
                        failedImports++;
                        if (result.getErrors() != null) {
                            errors.addAll(result.getErrors());
                        }
                        logger.warn("Failed to import related data for correspondence: {} - {}", docGuid, result.getMessage());
                    } else {
                        successfulImports++;
                        logger.debug("Successfully imported all related data for correspondence: {}", docGuid);
                    }
                    
                } catch (Exception e) {
                    failedImports++;
                    String errorMsg = "Error processing correspondence " + docGuid + ": " + e.getMessage();
                    errors.add(errorMsg);
                    logger.error(errorMsg, e);
                }
            }
            
            String status = failedImports == 0 ? "SUCCESS" : "PARTIAL_SUCCESS";
            String message = String.format(
                "Bulk import completed. Correspondences processed: %d (Success: %d, Failed: %d)", 
                totalRecords, successfulImports, failedImports
            );
            
            return new ImportResponseDto(status, message, totalRecords, successfulImports, failedImports, errors);
                
        } catch (Exception e) {
            logger.error("Failed to execute bulk correspondence import", e);
            return new ImportResponseDto("ERROR", "Failed to execute bulk correspondence import: " + e.getMessage(), 
                0, 0, 0, Arrays.asList("Failed to execute bulk correspondence import: " + e.getMessage()));
        }
    }

    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW, timeout = 300)
    public ImportResponseDto importAllCorrespondenceRelated(String docGuid) {
        logger.debug("Starting import of all correspondence-related data for doc: {}", docGuid);
        
        List<String> errors = new ArrayList<>();
        int totalRecords = 0;
        int successfulImports = 0;
        int failedImports = 0;
        
        try {
            // Import all correspondence-related entities
            ImportResponseDto attachmentsResult = importCorrespondenceAttachments(docGuid);
            ImportResponseDto commentsResult = importCorrespondenceComments(docGuid);
            ImportResponseDto copyTosResult = importCorrespondenceCopyTos(docGuid);
            ImportResponseDto currentDepartmentsResult = importCorrespondenceCurrentDepartments(docGuid);
            ImportResponseDto currentPositionsResult = importCorrespondenceCurrentPositions(docGuid);
            ImportResponseDto currentUsersResult = importCorrespondenceCurrentUsers(docGuid);
            ImportResponseDto customFieldsResult = importCorrespondenceCustomFields(docGuid);
            ImportResponseDto linksResult = importCorrespondenceLinks(docGuid);
            ImportResponseDto sendTosResult = importCorrespondenceSendTos(docGuid);
            ImportResponseDto transactionsResult = importCorrespondenceTransactions(docGuid);
            
            // Aggregate results
            List<ImportResponseDto> results = Arrays.asList(
                attachmentsResult, commentsResult, copyTosResult, currentDepartmentsResult,
                currentPositionsResult, currentUsersResult, customFieldsResult, 
                linksResult, sendTosResult, transactionsResult
            );
            
            for (ImportResponseDto result : results) {
                totalRecords += result.getTotalRecords();
                successfulImports += result.getSuccessfulImports();
                failedImports += result.getFailedImports();
                
                if (result.getErrors() != null) {
                    errors.addAll(result.getErrors());
                }
            }
            
            String status = failedImports == 0 ? "SUCCESS" : "PARTIAL_SUCCESS";
            String message = String.format(
                "All correspondence-related data import completed for doc %s. Total: %d, Success: %d, Failed: %d", 
                docGuid, totalRecords, successfulImports, failedImports
            );
            
            return new ImportResponseDto(status, message, totalRecords, successfulImports, failedImports, errors);
            
        } catch (Exception e) {
            logger.error("Failed to import all correspondence-related data for doc: {}", docGuid, e);
            return new ImportResponseDto("ERROR", 
                "Failed to import all correspondence-related data for doc " + docGuid + ": " + e.getMessage(), 
                0, 0, 0, Arrays.asList("Failed to import all correspondence-related data: " + e.getMessage()));
        }
    }

    // Helper method to process import results
    private boolean processResult(ImportResponseDto result, String entityType, String docGuid, List<String> errors) {
        if ("ERROR".equals(result.getStatus())) {
            errors.addAll(result.getErrors());
            logger.warn("Failed to import {} for correspondence {}: {}", entityType, docGuid, result.getMessage());
            return false;
        } else {
            logger.debug("Successfully imported {} {} for correspondence {}", 
                result.getSuccessfulImports(), entityType, docGuid);
            return true;
        }
    }
    
    // Helper method to update counters (simulating pass-by-reference)
    private void updateCounters(ImportResponseDto result, int total, int successful, int failed) {
        // Note: Java passes primitives by value, so we need to handle counter updates in the calling method
        // This method is kept for potential future use with wrapper objects
    }
    
    // Helper method to process related entities with individual error handling  
    private boolean processRelatedEntitiesForCorrespondence(String docGuid, String entityType, 
                                                           java.util.function.Supplier<ImportResponseDto> importFunction,
                                                           List<String> errors) {
        try {
            logger.debug("Processing {} for correspondence: {}", entityType, docGuid);
            ImportResponseDto result = importFunction.get();
            
            if ("ERROR".equals(result.getStatus())) {
                errors.addAll(result.getErrors());
                logger.warn("Failed to import {} for correspondence {}: {}", entityType, docGuid, result.getMessage());
                return false;
            } else {
                logger.debug("Successfully imported {} {} for correspondence {}", 
                    result.getSuccessfulImports(), entityType, docGuid);
                return true;
            }
        } catch (Exception e) {
            String errorMsg = "Exception importing " + entityType + " for correspondence " + docGuid + ": " + e.getMessage();
            errors.add(errorMsg);
            logger.error(errorMsg, e);
            return false;
        }
    }

    private <T, ID> ImportResponseDto importGenericData(String endpoint, Class<T> entityClass, 
                                                       JpaRepository<T, ID> repository, String entityName) {
        List<String> errors = new ArrayList<>();
        int successfulImports = 0;
        int failedImports = 0;
        int totalRecords = 0;

        try {
            String url = sourceApiBaseUrl + endpoint;
            HttpHeaders headers = createHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            
            // Check if response body is null
            String responseBody = response.getBody();
            if (responseBody == null || responseBody.trim().isEmpty()) {
                logger.warn("Empty response body for {}, endpoint: {}", entityName, endpoint);
                return new ImportResponseDto("SUCCESS", "No " + entityName + " found", 
                    0, 0, 0, new ArrayList<>());
            }
            
            // Parse response body manually to handle generic types properly
            logger.debug("Raw API response for {}: {}", entityName, responseBody);
            
            // First parse as generic ApiResponseDto
            ApiResponseDto<Object> genericResponse = objectMapper.readValue(responseBody, 
                new TypeReference<ApiResponseDto<Object>>() {});

            if (genericResponse == null || !Boolean.TRUE.equals(genericResponse.getSuccess())) {
                String message = genericResponse != null ? genericResponse.getMessage() : "Unknown API error";
                return createErrorResponse("API returned failure: " + message);
            }

            // Convert the data list to the specific entity type
            List<T> entities = new ArrayList<>();
            if (genericResponse.getData() != null) {
                for (Object item : genericResponse.getData()) {
                    try {
                        if (item == null) {
                            logger.warn("Null item found in {} data", entityName);
                            continue;
                        }
                        T entityData = objectMapper.convertValue(item, entityClass);
                        if (entityData != null) {
                            entities.add(entityData);
                        }
                    } catch (Exception e) {
                        logger.error("Failed to convert item to {}: {}", entityClass.getSimpleName(), e.getMessage());
                        failedImports++;
                        errors.add("Failed to parse " + entityName + " item: " + e.getMessage());
                    }
                }
            } else {
                logger.info("No data found for {}, endpoint: {}", entityName, endpoint);
                return new ImportResponseDto("SUCCESS", "No " + entityName + " found", 
                    0, 0, 0, new ArrayList<>());
            }

            totalRecords = entities.size();
            logger.info("Found {} {} to import", totalRecords, entityName);

            for (T entityData : entities) {
                try {
                    if (entityData == null) {
                        failedImports++;
                        errors.add("Null " + entityName + " object received");
                        continue;
                    }
                    repository.save(entityData);
                    successfulImports++;
                } catch (Exception e) {
                    failedImports++;
                    String errorMsg = "Failed to save " + entityName + ": " + e.getMessage();
                    errors.add(errorMsg);
                    logger.error(errorMsg, e);
                }
            }

            String status = failedImports == 0 ? "SUCCESS" : "PARTIAL_SUCCESS";
            String message = String.format("%s import completed. Success: %d, Failed: %d", 
                                         entityName, successfulImports, failedImports);

            return new ImportResponseDto(status, message, totalRecords, successfulImports, failedImports, errors);

        } catch (Exception e) {
            logger.error("Failed to import {}", entityName, e);
            return createErrorResponse("Failed to import " + entityName + ": " + e.getMessage());
        }
    }

    private <T, ID> ImportResponseDto importCorrespondenceRelatedData(String endpoint, Class<T> entityClass, 
                                                                     JpaRepository<T, ID> repository, String entityName) {
        List<String> errors = new ArrayList<>();
        int successfulImports = 0;
        int failedImports = 0;
        int totalRecords = 0;

        try {
            String url;
            if (endpoint.contains("/CorrespondenceAttachments/docGuid/")) {
                // Special case for CorrespondenceAttachments - use different base URL
                String docGuid = endpoint.substring(endpoint.lastIndexOf("/") + 1);
                url = "https://itba.tarasol.cloud/Tarasol4ExtractorApi/docGuid/" + docGuid;
            } else {
                url = sourceApiBaseUrl + endpoint;
            }
            HttpHeaders headers = createHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            
            // Check if response body is null
            String responseBody = response.getBody();
            if (responseBody == null || responseBody.trim().isEmpty()) {
                logger.warn("Empty response body for {}, endpoint: {}", entityName, endpoint);
                return new ImportResponseDto("SUCCESS", "No " + entityName + " found", 
                    0, 0, 0, new ArrayList<>());
            }
            
            // Parse response body manually to handle generic types properly
            logger.debug("Raw API response for {}: {}", entityName, responseBody);
            
            // First parse as generic ApiResponseDto
            ApiResponseDto<Object> genericResponse = objectMapper.readValue(responseBody, 
                new TypeReference<ApiResponseDto<Object>>() {});
            
            if (genericResponse == null || !Boolean.TRUE.equals(genericResponse.getSuccess())) {
                String message = genericResponse != null ? genericResponse.getMessage() : "Unknown API error";
                return createErrorResponse("API returned failure: " + message);
            }
            
            // Convert the data list to the specific entity type
            List<T> entities = new ArrayList<>();
            if (genericResponse.getData() != null) {
                for (Object item : genericResponse.getData()) {
                    try {
                        if (item == null) {
                            logger.warn("Null item found in {} data", entityName);
                            continue;
                        }
                        T entityData = objectMapper.convertValue(item, entityClass);
                        if (entityData != null) {
                            entities.add(entityData);
                        }
                    } catch (Exception e) {
                        logger.error("Failed to convert item to {}: {}", entityClass.getSimpleName(), e.getMessage());
                        failedImports++;
                        errors.add("Failed to parse " + entityName + " item: " + e.getMessage());
                    }
                }
            } else {
                logger.info("No data found for {}, endpoint: {}", entityName, endpoint);
                return new ImportResponseDto("SUCCESS", "No " + entityName + " found", 
                    0, 0, 0, new ArrayList<>());
            }

            totalRecords = entities.size();
            logger.info("Found {} {} to import", totalRecords, entityName);

            for (T entityData : entities) {
                try {
                    if (entityData == null) {
                        failedImports++;
                        errors.add("Null " + entityName + " object received");
                        continue;
                    }
                    
                    // Special handling for CorrespondenceAttachment with large file data
                    if (entityData instanceof CorrespondenceAttachment) {
                        CorrespondenceAttachment attachment = (CorrespondenceAttachment) entityData;
                        if (attachment.getFileData() != null && attachment.getFileData().length() > 50_000_000) { // 50MB
                            logger.warn("Skipping attachment {} due to large file size: {} bytes", 
                                      attachment.getGuid(), attachment.getFileData().length());
                            attachment.setFileData(null);
                            attachment.setFileDataErrorMessage("File too large for import (>50MB)");
                        }
                    }
                    
                    repository.save(entityData);
                    successfulImports++;
                    logger.debug("Successfully saved {}", entityName);
                } catch (Exception e) {
                    failedImports++;
                    String errorMsg = "Failed to save " + entityName + ": " + e.getMessage();
                    errors.add(errorMsg);
                    logger.error(errorMsg, e);
                }
            }

            String status = failedImports == 0 ? "SUCCESS" : "PARTIAL_SUCCESS";
            String message = String.format("%s import completed. Success: %d, Failed: %d", 
                                         entityName, successfulImports, failedImports);

            return new ImportResponseDto(status, message, totalRecords, successfulImports, failedImports, errors);

        } catch (Exception e) {
            logger.error("Failed to import {}", entityName, e);
            return createErrorResponse("Failed to import " + entityName + ": " + e.getMessage());
        }
    }

    private ImportResponseDto importCorrespondenceData() {
        List<String> errors = new ArrayList<>();
        int successfulImports = 0;
        int failedImports = 0;
        int totalRecords = 0;

        try {
            String url = sourceApiBaseUrl + "/Correspondences/All/PageIndex/1/PageSize/10000";
            HttpHeaders headers = createHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            
            // Check if response body is null
            String responseBody = response.getBody();
            if (responseBody == null || responseBody.trim().isEmpty()) {
                logger.warn("Empty response body for Correspondences");
                return new ImportResponseDto("SUCCESS", "No Correspondences found", 
                    0, 0, 0, new ArrayList<>());
            }
            
            // Parse the response manually to handle generic type properly
            logger.debug("Raw API response for Correspondences: {}", responseBody);
            
            // First parse as generic ApiResponseDto
            ApiResponseDto<Object> genericResponse = objectMapper.readValue(responseBody, 
                new TypeReference<ApiResponseDto<Object>>() {});
            
            if (genericResponse == null || !Boolean.TRUE.equals(genericResponse.getSuccess())) {
                String message = genericResponse != null ? genericResponse.getMessage() : "Unknown API error";
                return createErrorResponse("API returned failure: " + message);
            }
            
            // Convert the data list to the specific entity type
            List<Correspondence> correspondences = new ArrayList<>();
            if (genericResponse.getData() != null) {
                for (Object item : genericResponse.getData()) {
                    try {
                        if (item == null) {
                            logger.warn("Null item found in Correspondences data");
                            continue;
                        }
                        Correspondence correspondence = objectMapper.convertValue(item, Correspondence.class);
                        if (correspondence != null) {
                            correspondences.add(correspondence);
                        }
                    } catch (Exception e) {
                        logger.error("Failed to convert item to Correspondence: {}", e.getMessage());
                        failedImports++;
                        errors.add("Failed to parse Correspondence item: " + e.getMessage());
                    }
                }
            } else {
                logger.info("No Correspondences data found in API response");
                return new ImportResponseDto("SUCCESS", "No Correspondences found", 
                    0, 0, 0, new ArrayList<>());
            }

            totalRecords = correspondences.size();
            logger.info("Found {} correspondences to import", totalRecords);

            for (Correspondence correspondence : correspondences) {
                try {
                    if (correspondence == null || correspondence.getGuid() == null) {
                        failedImports++;
                        errors.add("Null correspondence or GUID received");
                        continue;
                    }
                    Optional<Correspondence> existing = correspondenceRepository.findById(correspondence.getGuid());
                    if (existing.isPresent()) {
                        // Update existing record
                        Correspondence existingCorr = existing.get();
                        updateCorrespondenceFields(existingCorr, correspondence);
                        correspondenceRepository.save(existingCorr);
                    } else {
                        // Insert new record
                        correspondenceRepository.save(correspondence);
                    }
                    successfulImports++;
                } catch (Exception e) {
                    failedImports++;
                    String corrGuid = correspondence != null ? correspondence.getGuid() : "unknown";
                    String errorMsg = "Failed to save correspondence " + corrGuid + ": " + e.getMessage();
                    errors.add(errorMsg);
                    logger.error(errorMsg, e);
                }
            }

            String status = failedImports == 0 ? "SUCCESS" : "PARTIAL_SUCCESS";
            String message = String.format("Correspondences import completed. Success: %d, Failed: %d", 
                                         successfulImports, failedImports);

            return new ImportResponseDto(status, message, totalRecords, successfulImports, failedImports, errors);

        } catch (Exception e) {
            logger.error("Failed to import correspondences", e);
            return createErrorResponse("Failed to import correspondences: " + e.getMessage());
        }
    }

    private void updateCorrespondenceFields(Correspondence existing, Correspondence newData) {
        existing.setSystemNo(newData.getSystemNo());
        existing.setSerialNumber(newData.getSerialNumber());
        existing.setSubject(newData.getSubject());
        existing.setReferenceNo(newData.getReferenceNo());
        existing.setExternalReferenceNumber(newData.getExternalReferenceNumber());
        existing.setNotes(newData.getNotes());
        existing.setImportanceId(newData.getImportanceId());
        existing.setPriorityId(newData.getPriorityId());
        existing.setSecrecyId(newData.getSecrecyId());
        existing.setIsDraft(newData.getIsDraft());
        existing.setIsDeleted(newData.getIsDeleted());
        existing.setIsBlocked(newData.getIsBlocked());
        existing.setIsArchive(newData.getIsArchive());
        existing.setIsFinal(newData.getIsFinal());
        existing.setIsMigrated(newData.getIsMigrated());
        // Add other fields as needed
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "*/*");
        headers.set("X-API-KEY", sourceApiKey);
        return headers;
    }

    private ImportResponseDto createErrorResponse(String errorMessage) {
        List<String> errors = new ArrayList<>();
        errors.add(errorMessage);
        return new ImportResponseDto("ERROR", errorMessage, 0, 0, 0, errors);
    }

}