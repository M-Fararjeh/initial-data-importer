package com.importservice.service;

import com.importservice.dto.*;
import com.importservice.entity.Correspondence;
import com.importservice.entity.CorrespondenceAttachment;
import com.importservice.util.AttachmentUtils;
import com.importservice.util.CorrespondenceUtils;
import com.importservice.util.CorrespondenceSubjectGenerator;
import com.importservice.util.DepartmentUtils;
import com.importservice.util.HijriDateUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class InternalDestinationSystemService {
    
    private static final Logger logger = LoggerFactory.getLogger(InternalDestinationSystemService.class);
    
    @Value("${destination.api.url}")
    private String destinationApiUrl;
    
    @Value("${destination.api.token}")
    private String authToken;
    
    @Value("${file.upload.use-sample:false}")
    private boolean useSampleFile;
    
    @Value("${file.upload.sample.filename:sample_document.pdf}")
    private String sampleFileName;
    
    @Value("${destination.api.logging.enabled:false}")
    private boolean loggingEnabled;
    
    @Value("${admin.user.username:cts_admin}")
    private String adminUsername;
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private CorrespondenceSubjectGenerator subjectGenerator;
    
    @Autowired
    private KeycloakTokenService keycloakTokenService;
    
    /**
     * Gets the base URL from the destination API URL
     */
    private String getBaseUrl() {
        if (destinationApiUrl == null) {
            return "http://18.206.121.44";
        }
        try {
            String[] parts = destinationApiUrl.split("/");
            return parts[0] + "//" + parts[2];
        } catch (Exception e) {
            logger.warn("Failed to extract base URL from destination.api.url: {}, using default", destinationApiUrl);
            return "http://18.206.121.44";
        }
    }
    
    /**
     * Gets the automation endpoint URL
     */
    private String getAutomationEndpoint() {
        return getBaseUrl() + "/nuxeo/api/v1/custom-automation/AC_Admin_RunOperation";
    }
    
    /**
     * Gets the upload endpoint URL
     */
    private String getUploadEndpoint() {
        return getBaseUrl() + "/nuxeo/api/v1/upload/";
    }
    
    /**
     * Creates a batch for file upload
     */
    public String createBatch() {
        try {
            String url = getUploadEndpoint();
            logApiCall("CREATE_BATCH", url, "{}");
            
            HttpHeaders headers = createHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<String> entity = new HttpEntity<>("{}", headers);
            
            ResponseEntity<BatchCreateResponse> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                BatchCreateResponse.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String batchId = response.getBody().getBatchId();
                logger.info("Created batch with ID: {}", batchId);
                return batchId;
            } else {
                logger.error("Failed to create batch - Status: {}", response.getStatusCode());
                return null;
            }
            
        } catch (Exception e) {
            logger.error("Error creating batch", e);
            return null;
        }
    }
    
    /**
     * Uploads base64 file to batch
     */
    public boolean uploadBase64FileToBatch(String batchId, String fileId, String base64Data, String fileName) {
        try {
            if (useSampleFile) {
                logger.info("Using sample file for upload instead of real file data");
                byte[] sampleFileData = readSampleFileFromResources();
                if (sampleFileData == null) {
                    logger.error("Failed to read sample file from resources");
                    return false;
                }
                return uploadFileToBatch(batchId, fileId, sampleFileData, sampleFileName);
            } else {
                if (base64Data == null || base64Data.trim().isEmpty() || "testbase64".equals(base64Data)) {
                    logger.info("No valid file data provided, using sample file from resources");
                    byte[] sampleFileData = readSampleFileFromResources();
                    if (sampleFileData == null) {
                        logger.error("Failed to read sample file from resources");
                        return false;
                    }
                    return uploadFileToBatch(batchId, fileId, sampleFileData, sampleFileName);
                } else {
                    byte[] fileData = Base64.getDecoder().decode(base64Data);
                    return uploadFileToBatch(batchId, fileId, fileData, fileName);
                }
            }
        } catch (IllegalArgumentException e) {
            logger.error("Invalid base64 data for file: {}", fileName, e);
            byte[] sampleFileData = readSampleFileFromResources();
            if (sampleFileData == null) {
                logger.error("Failed to read sample file from resources as fallback");
                return false;
            }
            return uploadFileToBatch(batchId, fileId, sampleFileData, sampleFileName);
        }
    }
    
    /**
     * Reads the test.pdf file from resources and converts it to byte array
     */
    private byte[] readSampleFileFromResources() {
        try {
            ClassPathResource resource = new ClassPathResource("test.pdf");
            if (!resource.exists()) {
                logger.error("test.pdf file not found in resources folder");
                return null;
            }
            
            try (InputStream inputStream = resource.getInputStream()) {
                java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
                int nRead;
                byte[] data = new byte[16384];
                while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, nRead);
                }
                byte[] fileData = buffer.toByteArray();
                logger.info("Successfully read test.pdf from resources: {} bytes", fileData.length);
                return fileData;
            }
        } catch (IOException e) {
            logger.error("Error reading test.pdf from resources", e);
            return null;
        }
    }
    
    /**
     * Uploads file data to batch
     */
    private boolean uploadFileToBatch(String batchId, String fileId, byte[] fileData, String fileName) {
        try {
            String uploadUrl = getUploadEndpoint() + batchId + "/" + fileId;
            
            ByteArrayResource resource = new ByteArrayResource(fileData) {
                @Override
                public String getFilename() {
                    return fileName;
                }
            };
            
            HttpHeaders headers = createHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            
            HttpEntity<ByteArrayResource> entity = new HttpEntity<>(resource, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                uploadUrl,
                HttpMethod.POST,
                entity,
                String.class
            );
            
            boolean success = response.getStatusCode().is2xxSuccessful();
            if (success) {
                logger.debug("Successfully uploaded file {} to batch {}", fileName, batchId);
            } else {
                logger.error("Failed to upload file {} to batch {} - Status: {}", fileName, batchId, response.getStatusCode());
            }
            
            return success;
            
        } catch (Exception e) {
            logger.error("Error uploading file {} to batch {}", fileName, batchId, e);
            return false;
        }
    }
    
    /**
     * Creates internal correspondence in destination system
     */
    public String createInternalCorrespondence(String guid, String asUser, Correspondence correspondence, String batchId) {
        try {
            String url = getAutomationEndpoint();
            
            InternalCorrespondenceCreateRequest request = new InternalCorrespondenceCreateRequest();
            
            // Set params
            request.setOperationName("AC_UA_InternalCorrespondence_Create");
            request.setAsUser(adminUsername);
            request.setDocCreator(asUser);
            request.setGuid(guid);
            request.setDocDate(correspondence.getCorrespondenceCreationDate() != null ? 
                             correspondence.getCorrespondenceCreationDate().toString() + "Z" : 
                             LocalDateTime.now().toString() + "Z");
            request.setTenantID("ITBA");
            
            // Build interCorrespondence context
            Map<String, Object> interCorrespondence = buildInternalCorrespondenceContext(correspondence, batchId);
            request.setInterCorrespondence(interCorrespondence);
            
            logApiCall("CREATE_INTERNAL_CORRESPONDENCE", url, request);
            
            HttpHeaders headers = createHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<InternalCorrespondenceCreateRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                try {
                    String responseBody = response.getBody();
                    if (responseBody != null && !responseBody.trim().isEmpty()) {
                        CorrespondenceCreateResponse createResponse = objectMapper.readValue(responseBody, CorrespondenceCreateResponse.class);
                        String documentId = createResponse.getUid();
                        
                        if (documentId != null && !documentId.trim().isEmpty()) {
                            logger.info("Successfully created internal correspondence: {} with document ID: {}", guid, documentId);
                            return documentId;
                        } else {
                            logger.warn("Document ID not found in response for internal correspondence: {}", guid);
                            return guid;
                        }
                    } else {
                        logger.warn("Empty response body for internal correspondence creation: {}", guid);
                        return guid;
                    }
                } catch (Exception e) {
                    logger.error("Error parsing internal correspondence creation response for: {}", guid, e);
                    return guid;
                }
            } else {
                logger.error("Failed to create internal correspondence - Status: {}", response.getStatusCode());
                return null;
            }
            
        } catch (Exception e) {
            logger.error("Error creating internal correspondence: {}", guid, e);
            return null;
        }
    }
    
    /**
     * Builds internal correspondence context for API calls
     */
    private Map<String, Object> buildInternalCorrespondenceContext(Correspondence correspondence, String batchId) {
        Map<String, Object> interCorrespondence = new HashMap<>();
        
        // Use subject generator if enabled
        String finalSubject = correspondence.getSubject();
        if (subjectGenerator.isRandomSubjectEnabled()) {
            String category = CorrespondenceUtils.mapCategory(correspondence.getClassificationGuid());
            finalSubject = subjectGenerator.generateSubjectWithCategory(category);
            logger.info("Generated random subject for internal correspondence {}: {}", correspondence.getGuid(), finalSubject);
        }
        
        // Document dates
        String gDocumentDate = correspondence.getCorrespondenceCreationDate() != null ?
                correspondence.getCorrespondenceCreationDate().toString() + "Z" :
                LocalDateTime.now().toString() + "Z";
        String hDocumentDate = correspondence.getCorrespondenceCreationDate() != null ?
                HijriDateUtils.convertToHijri(correspondence.getCorrespondenceCreationDate()) :
                HijriDateUtils.getCurrentHijriDate();
        
        // Due dates
        String gDueDate = correspondence.getDueDate() != null ? 
                        correspondence.getDueDate().toString() + "Z" : 
                        LocalDateTime.now().plusDays(30).toString() + "Z";
        String hDueDate = correspondence.getDueDate() != null ? 
                        HijriDateUtils.convertToHijri(correspondence.getDueDate()) : 
                        HijriDateUtils.convertToHijri(LocalDateTime.now().plusDays(30));
        
        // Department mapping
        String fromDepartment = DepartmentUtils.getDepartmentCodeByOldGuid(correspondence.getCreationDepartmentGuid());
        if (fromDepartment == null) {
            fromDepartment = "COF"; // Default department
        }
        
        String toDepartment = DepartmentUtils.getDepartmentCodeByOldGuid(correspondence.getFromDepartmentGuid());
        if (toDepartment == null) {
            toDepartment = "CTS"; // Default to department
        }
        
        interCorrespondence.put("corr:action", CorrespondenceUtils.mapAction(correspondence.getLastDecisionGuid()));
        interCorrespondence.put("corr:subject", finalSubject != null ? finalSubject : "");
        interCorrespondence.put("corr:remarks", correspondence.getNotes() != null ? CorrespondenceUtils.cleanHtmlTags(correspondence.getNotes()) : "");
        interCorrespondence.put("corr:referenceNumber", correspondence.getReferenceNo() != null ? correspondence.getReferenceNo() : "");
        interCorrespondence.put("corr:category", CorrespondenceUtils.mapCategory(correspondence.getClassificationGuid()));
        interCorrespondence.put("corr:secrecyLevel", CorrespondenceUtils.mapSecrecyLevel(correspondence.getSecrecyId()));
        interCorrespondence.put("corr:priority", CorrespondenceUtils.mapPriority(correspondence.getPriorityId()));
        interCorrespondence.put("corr:gDueDate", gDueDate);
        interCorrespondence.put("corr:hDueDate", hDueDate);
        interCorrespondence.put("corr:requireReply", CorrespondenceUtils.mapRequireReply(correspondence.getNeedReplyStatus()));
        interCorrespondence.put("corr:from", fromDepartment);
        interCorrespondence.put("corr:to", toDepartment);
        interCorrespondence.put("corr:gDocumentDate", gDocumentDate);
        interCorrespondence.put("corr:hDocumentDate", hDocumentDate);
        interCorrespondence.put("corr:fromAgency", "ITBA");
        interCorrespondence.put("corr:toAgency", "ITBA");
        
        // Add file content if batch exists
        if (batchId != null) {
            Map<String, Object> fileContent = new HashMap<>();
            fileContent.put("upload-batch", batchId);
            fileContent.put("upload-fileId", "0");
            interCorrespondence.put("file:content", fileContent);
        }
        
        return interCorrespondence;
    }
    
    /**
     * Creates attachment in destination system
     */
    public boolean createAttachment(CorrespondenceAttachment attachment, String batchId, String correspondenceDocumentId) {
        try {
            String url = getAutomationEndpoint();
            
            AttachmentCreateRequest request = new AttachmentCreateRequest();
            
            // Set params
            request.setOperationName("AC_UA_Correspondence_Attach_Create");
            request.setDocDate(attachment.getFileCreationDate() != null ? 
                             attachment.getFileCreationDate().toString() + "Z" : 
                             LocalDateTime.now().toString() + "Z");
            request.setDocID(correspondenceDocumentId);
            request.setGuid(attachment.getGuid());
            request.setAsUser(adminUsername);
            request.setDocCreator(attachment.getCreationUserName() != null ? attachment.getCreationUserName() : "itba-emp1");

            // Build attachment context
            Map<String, Object> attachmentData = new HashMap<>();
            attachmentData.put("title", CorrespondenceUtils.cleanFileName(attachment.getName()));
            attachmentData.put("barcode", AttachmentUtils.generateAttachmentBarcode(attachment.getGuid()));
            attachmentData.put("corr_attach:attachmentId", attachment.getGuid());
            attachmentData.put("corr_attach:classification", 
                             CorrespondenceUtils.mapAttachmentClassification(
                                 attachment.getFileType(), attachment.getName()));
            attachmentData.put("corr_attach:category", 
                             CorrespondenceUtils.mapAttachmentCategory(
                                 attachment.getIsPrimary() != null && attachment.getIsPrimary(), 
                                 attachment.getFileType()));
            attachmentData.put("corr_attach:type", 
                             CorrespondenceUtils.mapAttachmentType(attachment.getName()));
            attachmentData.put("corr_attach:remarks", 
                             attachment.getCaption() != null ? attachment.getCaption() : "");
            attachmentData.put("corr_attach:isObject", false);
            attachmentData.put("corr_attach:count", 1);
            
            // Add file content
            Map<String, Object> fileContent = new HashMap<>();
            fileContent.put("upload-batch", batchId);
            fileContent.put("upload-fileId", "0");
            fileContent.put("mime-type", CorrespondenceUtils.getMimeType(attachment.getName()));
            attachmentData.put("file:content", fileContent);
            
            request.setAttachment(attachmentData);
            request.getContext().put("tenantId", "ITBA");
            
            logApiCall("CREATE_INTERNAL_ATTACHMENT", url, request);
            
            HttpHeaders headers = createHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<AttachmentCreateRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String.class
            );
            
            boolean success = response.getStatusCode().is2xxSuccessful();
            if (success) {
                logger.info("Successfully created internal attachment: {}", attachment.getName());
            } else {
                logger.error("Failed to create internal attachment {} - Status: {}", attachment.getName(), response.getStatusCode());
            }
            
            return success;
            
        } catch (Exception e) {
            logger.error("Error creating internal attachment: {}", attachment.getName(), e);
            return false;
        }
    }
    
    /**
     * Creates physical attachment for internal correspondence
     */
    public boolean createPhysicalAttachment(String correspondenceGuid, String asUser, String physicalAttachments) {
        try {
            String url = getAutomationEndpoint();
            
            PhysicalAttachmentRequest request = new PhysicalAttachmentRequest();
            
            // Set params
            request.setOperationName("AC_UA_PhysicalAttachment_Add");
            request.setDocID(correspondenceGuid);
            request.setAsUser(adminUsername);
            request.setDocCreator(asUser);
            
            // Set context
            request.setPhysicalAttachments(physicalAttachments != null ? physicalAttachments : "");
            request.getContext().put("tenantId", "ITBA");
            
            logApiCall("CREATE_INTERNAL_PHYSICAL_ATTACHMENT", url, request);
            
            HttpHeaders headers = createHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<PhysicalAttachmentRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String.class
            );
            
            boolean success = response.getStatusCode().is2xxSuccessful();
            if (success) {
                logger.info("Successfully created physical attachment for internal correspondence: {}", correspondenceGuid);
            } else {
                logger.error("Failed to create physical attachment for internal correspondence {} - Status: {}", 
                           correspondenceGuid, response.getStatusCode());
            }
            
            return success;
            
        } catch (Exception e) {
            logger.error("Error creating physical attachment for internal correspondence: {}", correspondenceGuid, e);
            return false;
        }
    }
    
    /**
     * Creates assignment for internal correspondence
     */
    public boolean createInternalAssignment(String transactionGuid, String asUser, String documentId, 
                                          LocalDateTime actionDate, String toUserName, String departmentCode, 
                                          String decisionGuid) {
        try {
            String url = getAutomationEndpoint();
            
            AssignmentCreateRequest request = new AssignmentCreateRequest();
            
            // Set params
            request.setOperationName("AC_UA_Assignment_Create");
            request.setAsUser(adminUsername);
            request.setDocID(documentId);
            request.setDocDate(actionDate != null ?
                             actionDate.toString() + "Z" :
                             LocalDateTime.now().toString() + "Z");
            request.setGuid(transactionGuid);
            request.setDocCreator(asUser);
            request.setUpdateProp(actionDate != null ?
                    actionDate.toString() + "Z" :
                    LocalDateTime.now().toString() + "Z"
            );
            
            // Build assignment context
            Map<String, Object> assignment = new HashMap<>();
            assignment.put("title", "assignment-" + transactionGuid);
            if (org.apache.commons.lang3.StringUtils.isNotEmpty(toUserName)){
                assignment.put("assign:assignee", java.util.Arrays.asList(toUserName));
            } else {
                assignment.put("assign:assignee", java.util.Arrays.asList(departmentCode != null ? departmentCode : "COF"));
            }
            assignment.put("assign:department", java.util.Arrays.asList(departmentCode != null ? departmentCode : "COF"));
            assignment.put("assign:dueDate", actionDate != null ? 
                         actionDate.toString() + "Z" : 
                         LocalDateTime.now().toString() + "Z");
            assignment.put("assign:action", CorrespondenceUtils.mapAction(decisionGuid));
            assignment.put("assign:private", false);
            assignment.put("assign:canReAssign", false);
            
            request.setAssignment(assignment);
            request.getContext().put("tenantId", "ITBA");
            request.getContext().put("isReadOnly", "true");

            logApiCall("CREATE_INTERNAL_ASSIGNMENT", url, request);
            
            HttpHeaders headers = createHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<AssignmentCreateRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String.class
            );
            
            boolean success = response.getStatusCode().is2xxSuccessful();
            if (success) {
                logger.info("Successfully created internal assignment: {}", transactionGuid);
            } else {
                logger.error("Failed to create internal assignment {} - Status: {}", transactionGuid, response.getStatusCode());
            }
            
            return success;
            
        } catch (Exception e) {
            logger.error("Error creating internal assignment: {}", transactionGuid, e);
            return false;
        }
    }
    
    /**
     * Approves internal correspondence without approval workflow
     */
    public boolean approveInternalCorrespondence(String documentId, String asUser) {
        try {
            String url = getAutomationEndpoint();
            
            InternalApprovalRequest request = new InternalApprovalRequest();
            
            // Set params
            request.setOperationName("AC_UA_OutgoingCorrespondence_SendWithoutApproval");
            request.setAsUser(adminUsername);
            request.setDocID(documentId);
            request.setDocCreator(asUser);
            request.setTenantID("ITBA");
            
            logApiCall("APPROVE_INTERNAL_CORRESPONDENCE", url, request);
            
            HttpHeaders headers = createHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<InternalApprovalRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String.class
            );
            
            boolean success = response.getStatusCode().is2xxSuccessful();
            if (success) {
                logger.info("Successfully approved internal correspondence: {}", documentId);
            } else {
                logger.error("Failed to approve internal correspondence {} - Status: {}", documentId, response.getStatusCode());
            }
            
            return success;
            
        } catch (Exception e) {
            logger.error("Error approving internal correspondence: {}", documentId, e);
            return false;
        }
    }
    
    /**
     * Registers internal correspondence with reference
     */
    public boolean registerInternalWithReference(String documentId, String asUser, Map<String, Object> interCorrespondenceContext) {
        try {
            String url = getAutomationEndpoint();
            
            InternalRegisterWithReferenceRequest request = new InternalRegisterWithReferenceRequest();
            
            // Set params
            request.setOperationName("AC_UA_InternalCorrespondence_Register_WithReference");
            request.setAsUser(adminUsername);
            request.setDocID(documentId);
            request.setDocCreator(asUser);
            request.setTenantID("ITBA");
            
            // Set context
            request.setInterCorrespondence(interCorrespondenceContext);
            
            logApiCall("REGISTER_INTERNAL_WITH_REFERENCE", url, request);
            
            HttpHeaders headers = createHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<InternalRegisterWithReferenceRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String.class
            );
            
            boolean success = response.getStatusCode().is2xxSuccessful();
            if (success) {
                logger.info("Successfully registered internal correspondence with reference: {}", documentId);
            } else {
                logger.error("Failed to register internal correspondence with reference {} - Status: {}", documentId, response.getStatusCode());
            }
            
            return success;
            
        } catch (Exception e) {
            logger.error("Error registering internal correspondence with reference: {}", documentId, e);
            return false;
        }
    }
    
    /**
     * Sends internal correspondence
     */
    public boolean sendInternalCorrespondence(String documentId, String asUser) {
        try {
            String url = getAutomationEndpoint();
            
            InternalSendRequest request = new InternalSendRequest();
            
            // Set params
            request.setOperationName("AC_UA_InternalCorrespondence_Sent");
            request.setAsUser(adminUsername);
            request.setDocID(documentId);
            request.setDocCreator(asUser);
            request.setTenantID("ITBA");
            
            logApiCall("SEND_INTERNAL_CORRESPONDENCE", url, request);
            
            HttpHeaders headers = createHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<InternalSendRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String.class
            );
            
            boolean success = response.getStatusCode().is2xxSuccessful();
            if (success) {
                logger.info("Successfully sent internal correspondence: {}", documentId);
            } else {
                logger.error("Failed to send internal correspondence {} - Status: {}", documentId, response.getStatusCode());
            }
            
            return success;
            
        } catch (Exception e) {
            logger.error("Error sending internal correspondence: {}", documentId, e);
            return false;
        }
    }
    
    /**
     * Sets owner for internal correspondence
     */
    public boolean setInternalCorrespondenceOwner(String documentId, String asUser) {
        try {
            String url = getAutomationEndpoint();
            
            SetOwnerRequest request = new SetOwnerRequest();
            
            // Set params
            request.setOperationName("AC_UA_Correspondence_SetOwner");
            request.setAsUser(adminUsername);
            request.setDocCreator(asUser);
            request.setDocID(documentId);
            request.getContext().put("tenantId", "ITBA");
            request.getContext().put("docCreator", asUser);
            
            logApiCall("SET_INTERNAL_OWNER", url, request);
            
            HttpHeaders headers = createHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<SetOwnerRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String.class
            );
            
            boolean success = response.getStatusCode().is2xxSuccessful();
            if (success) {
                logger.info("Successfully set owner for internal correspondence: {}", documentId);
            } else {
                logger.error("Failed to set owner for internal correspondence {} - Status: {}", 
                           documentId, response.getStatusCode());
            }
            
            return success;
            
        } catch (Exception e) {
            logger.error("Error setting owner for internal correspondence: {}", documentId, e);
            return false;
        }
    }
    
    /**
     * Creates business log in destination system
     */
    public boolean createBusinessLog(String transactionGuid, String documentId, LocalDateTime actionDate,
                                   String eventName, String eventComment, String fromUserName) {
        try {
            String url = getAutomationEndpoint();
            
            BusinessLogCreateRequest request = new BusinessLogCreateRequest();
            
            // Set params according to API specification
            request.setOperationName("Document.CreateBusinessLog");
            request.setDocID(documentId);
            request.setDocDate(actionDate != null ? 
                             actionDate.toString() + "Z" : 
                             LocalDateTime.now().toString() + "Z");
            request.setGuid(transactionGuid);
            request.setEventCategory("document");
            request.setEventName(eventName != null ? eventName : "internal_register");
            request.setEventDate(actionDate != null ? 
                               actionDate.toString() + "Z" : 
                               LocalDateTime.now().toString() + "Z");
            request.setEventTypes("userEvent");
            request.setEventComment(CorrespondenceUtils.cleanHtmlTags(eventComment));
            request.setDocumentTypes("InternalCorrespondence");
            request.setExtendedInfo(null);
            request.setCurrentLifeCycle("draft");
            request.setPerson(adminUsername); // Use administrator as creator for business log
            
            logApiCall("CREATE_INTERNAL_BUSINESS_LOG", url, request);
            
            HttpHeaders headers = createHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<BusinessLogCreateRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String.class
            );
            
            boolean success = response.getStatusCode().is2xxSuccessful();
            if (success) {
                logger.info("Successfully created internal business log: {}", transactionGuid);
            } else {
                logger.error("Failed to create internal business log {} - Status: {}", transactionGuid, response.getStatusCode());
            }
            
            return success;
            
        } catch (Exception e) {
            logger.error("Error creating internal business log: {}", transactionGuid, e);
            return false;
        }
    }
    
    /**
     * Closes internal correspondence in destination system
     */
    public boolean closeInternalCorrespondence(String correspondenceGuid, String documentId, 
                                             String asUser, LocalDateTime closeDate) {
        try {
            String url = getAutomationEndpoint();
            
            InternalClosingRequest request = new InternalClosingRequest();
            
            // Set params according to API specification
            request.setOperationName("AC_UA_InternalCorrespondence_Close");
            request.setAsUser(adminUsername);
            request.setDocID(documentId);
            request.setDocCreator(asUser);
            
            // Set update properties
            Map<String, Object> updateProp = new HashMap<>();
            updateProp.put("corr:closeDate", closeDate != null ? 
                         closeDate.toString() + "Z" : 
                         LocalDateTime.now().toString() + "Z");
            request.setUpdateProp(updateProp);
            request.setTenantID("ITBA");
            
            logApiCall("CLOSE_INTERNAL_CORRESPONDENCE", url, request);
            
            HttpHeaders headers = createHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<InternalClosingRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String.class
            );
            
            boolean success = response.getStatusCode().is2xxSuccessful();
            if (success) {
                logger.info("Successfully closed internal correspondence: {}", correspondenceGuid);
            } else {
                logger.error("Failed to close internal correspondence {} - Status: {}", correspondenceGuid, response.getStatusCode());
            }
            
            return success;
            
        } catch (Exception e) {
            logger.error("Error closing internal correspondence: {}", correspondenceGuid, e);
            return false;
        }
    }
    
    /**
     * Logs request details if logging is enabled
     */
    private void logApiCall(String operation, String url, Object requestBody) {
        if (url.contains("/nuxeo/api/v1/upload/")){
            return;
        }
        if (loggingEnabled) {
            System.out.println("=== INTERNAL DESTINATION API CALL: " + operation + " ===");
            System.out.println("URL: " + url);
            if (requestBody != null) {
                System.out.println("Request Body: " + requestBody.toString());
            }
            System.out.println("=== END API CALL ===");
        }
    }
    
    /**
     * Creates HTTP headers for API requests
     */
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        headers.set("Accept-Language", "en-US,en;q=0.9,ar;q=0.8");
        
        // Use dynamic token from Keycloak service if available
        String token = keycloakTokenService.getCurrentToken();
        if (token != null) {
            headers.set("Authorization", "Bearer " + token);
            logger.debug("Using dynamic Keycloak token for internal API request");
        } else {
            headers.set("Authorization", "Bearer " + authToken);
            logger.debug("Using static token from configuration for internal");
        }
        
        headers.set("Connection", "keep-alive");
        return headers;
    }
}