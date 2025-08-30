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
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class DataImportService {

    private static final Logger logger = LoggerFactory.getLogger(DataImportService.class);

    @Value("${source.api.base-url}")
    private String sourceApiBaseUrl;

    @Value("${source.api.key}")
    private String sourceApiKey;

    @Autowired
    private RestTemplate restTemplate;

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

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Basic entity import methods
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    public ImportResponseDto importClassifications() {
        logger.info("Starting classifications import");
        return importGenericData("/Classifications", Classification.class, classificationRepository, "Classifications");
    }

    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    public ImportResponseDto importContacts() {
        logger.info("Starting contacts import");
        return importGenericData("/Contacts", Contact.class, contactRepository, "Contacts");
    }

    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    public ImportResponseDto importDecisions() {
        logger.info("Starting decisions import");
        return importGenericData("/Decisions", Decision.class, decisionRepository, "Decisions");
    }

    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    public ImportResponseDto importDepartments() {
        logger.info("Starting departments import");
        return importGenericData("/Departments", Department.class, departmentRepository, "Departments");
    }

    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    public ImportResponseDto importForms() {
        logger.info("Starting forms import");
        return importGenericData("/Forms", Form.class, formRepository, "Forms");
    }

    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    public ImportResponseDto importFormTypes() {
        logger.info("Starting form types import");
        return importGenericData("/FormTypes", FormType.class, formTypeRepository, "FormTypes");
    }

    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    public ImportResponseDto importImportance() {
        logger.info("Starting importance import");
        return importGenericData("/Importance", Importance.class, importanceRepository, "Importance");
    }

    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    public ImportResponseDto importPositions() {
        logger.info("Starting positions import");
        return importGenericData("/Positions", Position.class, positionRepository, "Positions");
    }

    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    public ImportResponseDto importPosRoles() {
        logger.info("Starting pos roles import");
        return importGenericData("/PosRole", PosRole.class, posRoleRepository, "PosRoles");
    }

    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    public ImportResponseDto importPriority() {
        logger.info("Starting priority import");
        return importGenericData("/Priority", Priority.class, priorityRepository, "Priority");
    }

    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    public ImportResponseDto importRoles() {
        logger.info("Starting roles import");
        return importGenericData("/Roles", Role.class, roleRepository, "Roles");
    }

    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    public ImportResponseDto importSecrecy() {
        logger.info("Starting secrecy import");
        return importGenericData("/Secrecy", Secrecy.class, secrecyRepository, "Secrecy");
    }

    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    public ImportResponseDto importUserPositions() {
        logger.info("Starting user positions import");
        return importGenericData("/UserPosition", UserPosition.class, userPositionRepository, "UserPositions");
    }

    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    public ImportResponseDto importUsers() {
        logger.info("Starting users import");
        return importGenericData("/Users", User.class, userRepository, "Users");
    }

    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    public ImportResponseDto importCorrespondences() {
        logger.info("Starting correspondences import");
        return importCorrespondenceData();
    }

    // Correspondence-related import methods
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    public ImportResponseDto importCorrespondenceAttachments(String docGuid) {
        logger.info("Starting correspondence attachments import for doc: {}", docGuid);
        return importCorrespondenceRelatedData("/CorrespondenceAttachments/docGuid/" + docGuid, 
                                             CorrespondenceAttachment.class, 
                                             correspondenceAttachmentRepository, 
                                             "CorrespondenceAttachments");
    }

    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    public ImportResponseDto importCorrespondenceComments(String docGuid) {
        logger.info("Starting correspondence comments import for doc: {}", docGuid);
        return importCorrespondenceRelatedData("/CorrespondenceComments/docGuid/" + docGuid, 
                                             CorrespondenceComment.class, 
                                             correspondenceCommentRepository, 
                                             "CorrespondenceComments");
    }

    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    public ImportResponseDto importCorrespondenceCopyTos(String docGuid) {
        logger.info("Starting correspondence copy tos import for doc: {}", docGuid);
        return importCorrespondenceRelatedData("/CorrespondenceCopyTo/docGUId/" + docGuid, 
                                             CorrespondenceCopyTo.class, 
                                             correspondenceCopyToRepository, 
                                             "CorrespondenceCopyTos");
    }

    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    public ImportResponseDto importCorrespondenceCurrentDepartments(String docGuid) {
        logger.info("Starting correspondence current departments import for doc: {}", docGuid);
        
        List<String> errors = new ArrayList<>();
        int successfulImports = 0;
        int failedImports = 0;
        int totalRecords = 0;

        try {
            String url = sourceApiBaseUrl + "/CorrespondenceCurrentDepartments/docGuid/" + docGuid;
            HttpHeaders headers = createHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            
            TypeReference<ApiResponseDto<CorrespondenceCurrentDepartment>> typeRef = 
                new TypeReference<ApiResponseDto<CorrespondenceCurrentDepartment>>() {};
            ApiResponseDto<CorrespondenceCurrentDepartment> apiResponse = objectMapper.readValue(response.getBody(), typeRef);

            if (!apiResponse.getSuccess()) {
                return createErrorResponse("API returned failure: " + apiResponse.getMessage());
            }

            List<CorrespondenceCurrentDepartment> departments = apiResponse.getData();
            totalRecords = departments.size();

            for (CorrespondenceCurrentDepartment dept : departments) {
                try {
                    dept.setDocGuid(docGuid); // Set the doc guid
                    correspondenceCurrentDepartmentRepository.save(dept);
                    successfulImports++;
                } catch (Exception e) {
                    failedImports++;
                    errors.add("Failed to save correspondence current department: " + e.getMessage());
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
    public ImportResponseDto importCorrespondenceCurrentPositions(String docGuid) {
        logger.info("Starting correspondence current positions import for doc: {}", docGuid);
        return importCorrespondenceRelatedData("/CorrespondenceCurrentPositions/docGuid/" + docGuid, 
                                             CorrespondenceCurrentPosition.class, 
                                             correspondenceCurrentPositionRepository, 
                                             "CorrespondenceCurrentPositions");
    }

    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    public ImportResponseDto importCorrespondenceCurrentUsers(String docGuid) {
        logger.info("Starting correspondence current users import for doc: {}", docGuid);
        return importCorrespondenceRelatedData("/CorrespondenceCurrentUsers/docGuid/" + docGuid, 
                                             CorrespondenceCurrentUser.class, 
                                             correspondenceCurrentUserRepository, 
                                             "CorrespondenceCurrentUsers");
    }

    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    public ImportResponseDto importCorrespondenceCustomFields(String docGuid) {
        logger.info("Starting correspondence custom fields import for doc: {}", docGuid);
        return importCorrespondenceRelatedData("/CorrespondenceCustomFields/docGuid/" + docGuid, 
                                             CorrespondenceCustomField.class, 
                                             correspondenceCustomFieldRepository, 
                                             "CorrespondenceCustomFields");
    }

    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    public ImportResponseDto importCorrespondenceLinks(String docGuid) {
        logger.info("Starting correspondence links import for doc: {}", docGuid);
        return importCorrespondenceRelatedData("/CorrespondenceLinks/docGuid/" + docGuid, 
                                             CorrespondenceLink.class, 
                                             correspondenceLinkRepository, 
                                             "CorrespondenceLinks");
    }

    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    public ImportResponseDto importCorrespondenceSendTos(String docGuid) {
        logger.info("Starting correspondence send tos import for doc: {}", docGuid);
        return importCorrespondenceRelatedData("/CorrespondenceSendTo/docGUId/" + docGuid, 
                                             CorrespondenceSendTo.class, 
                                             correspondenceSendToRepository, 
                                             "CorrespondenceSendTos");
    }

    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    public ImportResponseDto importCorrespondenceTransactions(String docGuid) {
        logger.info("Starting correspondence transactions import for doc: {}", docGuid);
        return importCorrespondenceRelatedData("/CorrespondenceTransactions/docGuid/" + docGuid, 
                                             CorrespondenceTransaction.class, 
                                             correspondenceTransactionRepository, 
                                             "CorrespondenceTransactions");
    }

    public ImportResponseDto importAllCorrespondencesWithRelated() {
        logger.info("Starting bulk import of all correspondences with related data");
        
        List<String> errors = new ArrayList<>();
        int totalCorrespondences = 0;
        int successfulCorrespondences = 0;
        int failedCorrespondences = 0;
        int totalRelatedEntities = 0;
        int successfulRelatedEntities = 0;
        int failedRelatedEntities = 0;
        
        try {
            // Get all correspondences from database
            List<Correspondence> correspondences = correspondenceRepository.findAll();
            totalCorrespondences = correspondences.size();
            
            logger.info("Found {} correspondences in database to process", totalCorrespondences);
            
            if (correspondences.isEmpty()) {
                return new ImportResponseDto("SUCCESS", 
                    "No correspondences found in database. Import correspondences first.", 
                    0, 0, 0, new ArrayList<>());
            }
            
            for (Correspondence correspondence : correspondences) {
                String docGuid = correspondence.getGuid();
                logger.info("Processing correspondence: {} ({})", docGuid, correspondence.getSubject());
                
                try {
                    // Import all related entities for this correspondence
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
                    
                    boolean correspondenceSuccess = true;
                    for (ImportResponseDto result : results) {
                        totalRelatedEntities += result.getTotalRecords();
                        successfulRelatedEntities += result.getSuccessfulImports();
                        failedRelatedEntities += result.getFailedImports();
                        
                        if ("ERROR".equals(result.getStatus())) {
                            correspondenceSuccess = false;
                            errors.addAll(result.getErrors());
                        }
                    }
                    
                    if (correspondenceSuccess) {
                        successfulCorrespondences++;
                        logger.info("Successfully processed all related data for correspondence: {}", docGuid);
                    } else {
                        failedCorrespondences++;
                        errors.add("Failed to process some related data for correspondence: " + docGuid);
                    }
                    
                } catch (Exception e) {
                    failedCorrespondences++;
                    String errorMsg = "Error processing correspondence " + docGuid + ": " + e.getMessage();
                    errors.add(errorMsg);
                    logger.error(errorMsg, e);
                }
            }
            
            String status = failedCorrespondences == 0 ? "SUCCESS" : "PARTIAL_SUCCESS";
            String message = String.format(
                "Bulk import completed. Correspondences processed: %d (Success: %d, Failed: %d). " +
                "Related entities: %d (Success: %d, Failed: %d)", 
                totalCorrespondences, successfulCorrespondences, failedCorrespondences,
                totalRelatedEntities, successfulRelatedEntities, failedRelatedEntities
            );
            
            return new ImportResponseDto(status, message, 
                totalCorrespondences + totalRelatedEntities, 
                successfulCorrespondences + successfulRelatedEntities, 
                failedCorrespondences + failedRelatedEntities, 
                errors);
                
        } catch (Exception e) {
            logger.error("Failed to execute bulk correspondence import", e);
            return new ImportResponseDto("ERROR", "Failed to execute bulk correspondence import: " + e.getMessage(), 
                0, 0, 0, Arrays.asList("Failed to execute bulk correspondence import: " + e.getMessage()));
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
            
            TypeReference<ApiResponseDto<T>> typeRef = new TypeReference<ApiResponseDto<T>>() {};
            ApiResponseDto<T> apiResponse = objectMapper.readValue(response.getBody(), typeRef);

            if (!apiResponse.getSuccess()) {
                return createErrorResponse("API returned failure: " + apiResponse.getMessage());
            }

            List<T> entities = apiResponse.getData();
            totalRecords = entities.size();
            logger.info("Found {} {} to import", totalRecords, entityName);

            for (T entityData : entities) {
                try {
                    repository.save(entityData);
                    successfulImports++;
                } catch (Exception e) {
                    failedImports++;
                    errors.add("Failed to save " + entityName + ": " + e.getMessage());
                    logger.error("Error saving {}: {}", entityName, e.getMessage());
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
            String url = sourceApiBaseUrl + endpoint;
            HttpHeaders headers = createHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            
            TypeReference<ApiResponseDto<T>> typeRef = new TypeReference<ApiResponseDto<T>>() {};
            ApiResponseDto<T> apiResponse = objectMapper.readValue(response.getBody(), typeRef);

            if (!apiResponse.getSuccess()) {
                return createErrorResponse("API returned failure: " + apiResponse.getMessage());
            }

            List<T> entities = apiResponse.getData();
            totalRecords = entities.size();
            logger.info("Found {} {} to import", totalRecords, entityName);

            for (T entityData : entities) {
                try {
                    repository.save(entityData);
                    successfulImports++;
                } catch (Exception e) {
                    failedImports++;
                    errors.add("Failed to save " + entityName + ": " + e.getMessage());
                    logger.error("Error saving {}: {}", entityName, e.getMessage());
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
            
            TypeReference<ApiResponseDto<Correspondence>> typeRef = new TypeReference<ApiResponseDto<Correspondence>>() {};
            ApiResponseDto<Correspondence> apiResponse = objectMapper.readValue(response.getBody(), typeRef);

            if (!apiResponse.getSuccess()) {
                return createErrorResponse("API returned failure: " + apiResponse.getMessage());
            }

            List<Correspondence> correspondences = apiResponse.getData();
            totalRecords = correspondences.size();
            logger.info("Found {} correspondences to import", totalRecords);

            for (Correspondence correspondence : correspondences) {
                try {
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
                    errors.add("Failed to save correspondence " + correspondence.getGuid() + ": " + e.getMessage());
                    logger.error("Error saving correspondence: {}", e.getMessage());
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