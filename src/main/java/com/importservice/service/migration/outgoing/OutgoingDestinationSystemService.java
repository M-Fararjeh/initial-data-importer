package com.importservice.service.migration.outgoing;

import com.importservice.dto.*;
import com.importservice.entity.Correspondence;
import com.importservice.entity.CorrespondenceAttachment;
import com.importservice.entity.CorrespondenceSendTo;
import com.importservice.service.KeycloakTokenService;
import com.importservice.util.AgencyMappingUtils;
import com.importservice.util.AttachmentUtils;
import com.importservice.util.CorrespondenceUtils;
import com.importservice.util.CorrespondenceSubjectGenerator;
import com.importservice.util.DepartmentUtils;
import com.importservice.util.HijriDateUtils;
import org.springframework.beans.factory.annotation.Value;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.*;

@Service
public class OutgoingDestinationSystemService {
    
    private static final Logger logger = LoggerFactory.getLogger(OutgoingDestinationSystemService.class);
    
    @Value("${destination.api.url}")
    private String destinationApiUrl;
    
    @Value("${destination.api.token}")
    private String authToken;
    
    @Value("${file.upload.use-sample:false}")
    private boolean useSampleFile;
    
    @Value("${file.upload.sample.filename:sample_document.pdf}")
    private String sampleFileName;
    
    @Value("${file.upload.sample.mimetype:application/pdf}")
    private String sampleMimeType;
    
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
    
    @Autowired
    private com.importservice.repository.CorrespondenceSendToRepository correspondenceSendToRepository;
    
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
            String url = getUploadEndpoint() + batchId + "/" + fileId;
            logApiCall("UPLOAD_FILE", url, "File: " + fileName + " (Base64 data length: " + 
                      (base64Data != null ? base64Data.length() : 0) + ")");
            
            if (useSampleFile) {
                logger.info("Using sample file for upload instead of real file data");
                byte[] sampleFileData = readSampleFileFromResources();
                if (sampleFileData == null) {
                    logger.error("Failed to read sample file from resources");
                    return false;
                }
                return uploadFileToBatch(batchId, fileId, sampleFileData, sampleFileName, url);
            } else {
                if (base64Data == null || base64Data.trim().isEmpty() || "testbase64".equals(base64Data)) {
                    logger.info("No valid file data provided, using sample file from resources");
                    byte[] sampleFileData = readSampleFileFromResources();
                    if (sampleFileData == null) {
                        logger.error("Failed to read sample file from resources");
                        return false;
                    }
                    return uploadFileToBatch(batchId, fileId, sampleFileData, sampleFileName, "");
                } else {
                    byte[] fileData = Base64.getDecoder().decode(base64Data);
                    return uploadFileToBatch(batchId, fileId, fileData, fileName, "");
                }
            }
        } catch (IllegalArgumentException e) {
            logger.error("Invalid base64 data for file: {}", fileName, e);
            byte[] sampleFileData = readSampleFileFromResources();
            if (sampleFileData == null) {
                logger.error("Failed to read sample file from resources as fallback");
                return false;
            }
            return uploadFileToBatch(batchId, fileId, sampleFileData, sampleFileName, "");
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
    private boolean uploadFileToBatch(String batchId, String fileId, byte[] fileData, String fileName, String unusedParam) {
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
     * Creates outgoing correspondence in destination system
     */
    public String createOutgoingCorrespondence(String guid, String asUser, Correspondence correspondence, String batchId) {
        try {
            String url = getAutomationEndpoint();
            
            OutgoingCorrespondenceCreateRequest request = new OutgoingCorrespondenceCreateRequest();
            
            // Set params
            request.setOperationName("AC_UA_OutgoingCorrespondence_Create");
            request.setAsUser(adminUsername);
            request.setDocCreator(asUser);
            request.setGuid(guid);
            request.setDocDate(correspondence.getCorrespondenceCreationDate() != null ? 
                             correspondence.getCorrespondenceCreationDate().toString() + "Z" : 
                             LocalDateTime.now().toString() + "Z");
            request.setTenantID("ITBA");
            
            // Build outCorrespondence context
            Map<String, Object> outCorrespondence = buildOutgoingCorrespondenceContext(correspondence, batchId);
            request.setOutCorrespondence(outCorrespondence);
            
            logApiCall("CREATE_OUTGOING_CORRESPONDENCE", url, request);
            
            HttpHeaders headers = createHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<OutgoingCorrespondenceCreateRequest> entity = new HttpEntity<>(request, headers);
            
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
                            logger.info("Successfully created outgoing correspondence: {} with document ID: {}", guid, documentId);
                            return documentId;
                        } else {
                            logger.warn("Document ID not found in response for outgoing correspondence: {}", guid);
                            return guid;
                        }
                    } else {
                        logger.warn("Empty response body for outgoing correspondence creation: {}", guid);
                        return guid;
                    }
                } catch (Exception e) {
                    logger.error("Error parsing outgoing correspondence creation response for: {}", guid, e);
                    return guid;
                }
            } else {
                logger.error("Failed to create outgoing correspondence - Status: {}", response.getStatusCode());
                return null;
            }
            
        } catch (Exception e) {
            logger.error("Error creating outgoing correspondence: {}", guid, e);
            return null;
        }
    }
    
    /**
     * Builds outgoing correspondence context for API calls
     */
    private Map<String, Object> buildOutgoingCorrespondenceContext(Correspondence correspondence, String batchId) {
        Map<String, Object> outCorrespondence = new HashMap<>();
        
        // Use subject generator if enabled
        String finalSubject = correspondence.getSubject();
        if (subjectGenerator.isRandomSubjectEnabled()) {
            String category = CorrespondenceUtils.mapCategory(correspondence.getClassificationGuid());
            finalSubject = subjectGenerator.generateSubjectWithCategory(category);
            logger.info("Generated random subject for outgoing correspondence {}: {}", correspondence.getGuid(), finalSubject);
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
        
        outCorrespondence.put("corr:gDocumentDate", gDocumentDate);
        outCorrespondence.put("corr:hDocumentDate", hDocumentDate);
        //outCorrespondence.put("out_corr:signee", "SECTOR");
        outCorrespondence.put("corr:action", CorrespondenceUtils.mapAction(correspondence.getLastDecisionGuid()));
        outCorrespondence.put("corr:subject", finalSubject != null ? finalSubject : "");
        outCorrespondence.put("corr:remarks", correspondence.getNotes() != null ? CorrespondenceUtils.cleanHtmlTags(correspondence.getNotes()) : "");
        outCorrespondence.put("corr:referenceNumber", correspondence.getReferenceNo() != null ? correspondence.getReferenceNo() : "");
        outCorrespondence.put("corr:category", CorrespondenceUtils.mapCategory(correspondence.getClassificationGuid()));
        outCorrespondence.put("corr:secrecyLevel", CorrespondenceUtils.mapSecrecyLevel(correspondence.getSecrecyId()));
        outCorrespondence.put("corr:priority", CorrespondenceUtils.mapPriority(correspondence.getPriorityId()));
        outCorrespondence.put("corr:gDueDate", gDueDate);
        outCorrespondence.put("corr:hDueDate", hDueDate);
        outCorrespondence.put("corr:requireReply", CorrespondenceUtils.mapRequireReply(correspondence.getNeedReplyStatus()));
        
        // Department mapping
        String fromDepartment = DepartmentUtils.getDepartmentCodeByOldGuid(correspondence.getCreationDepartmentGuid());
        if (fromDepartment == null) {
            fromDepartment = "COF"; // Default department
        }
        
        outCorrespondence.put("corr:from", fromDepartment);
        outCorrespondence.put("corr:to", "");
        outCorrespondence.put("corr:fromAgency", "ITBA");
        
        // Get toAgency from correspondence_send_tos table
        String toAgency = getToAgencyFromSendTos(correspondence.getGuid());
        outCorrespondence.put("corr:toAgency", toAgency);
        
        outCorrespondence.put("out_corr:multiRecivers", Arrays.asList());
        
        // Add file content if batch exists
        if (batchId != null) {
            Map<String, Object> fileContent = new HashMap<>();
            fileContent.put("upload-batch", batchId);
            fileContent.put("upload-fileId", "0");
            outCorrespondence.put("file:content", fileContent);
        }
        
        return outCorrespondence;
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
            
            logApiCall("CREATE_ATTACHMENT", url, request);
            
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
                logger.info("Successfully created attachment: {}", attachment.getName());
            } else {
                logger.error("Failed to create attachment {} - Status: {}", attachment.getName(), response.getStatusCode());
            }
            
            return success;
            
        } catch (Exception e) {
            logger.error("Error creating attachment: {}", attachment.getName(), e);
            return false;
        }
    }
    
    /**
     * Creates physical attachment for outgoing correspondence
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
            
            logApiCall("CREATE_PHYSICAL_ATTACHMENT", url, request);
            
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
                logger.info("Successfully created physical attachment for outgoing correspondence: {}", correspondenceGuid);
            } else {
                logger.error("Failed to create physical attachment for outgoing correspondence {} - Status: {}", 
                           correspondenceGuid, response.getStatusCode());
            }
            
            return success;
            
        } catch (Exception e) {
            logger.error("Error creating physical attachment for outgoing correspondence: {}", correspondenceGuid, e);
            return false;
        }
    }
    
    /**
     * Creates assignment for outgoing correspondence
     */
    public boolean createOutgoingAssignment(String transactionGuid, String asUser, String documentId, 
                                          LocalDateTime actionDate, String toUserName, String departmentCode, 
                                          String decisionGuid) {
        try {
            String url = getAutomationEndpoint();
            
            AssignmentCreateRequest request = new AssignmentCreateRequest();
            
            // Set params
            request.setOperationName("AC_UA_Assignment_Create");
            request.setAsUser("cts_admin");
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
            if (StringUtils.isNotEmpty(toUserName)){
                assignment.put("assign:assignee", Arrays.asList(departmentCode != null ? departmentCode : "COF"));
            }else{
                assignment.put("assign:assignee", Arrays.asList());
            }
            assignment.put("assign:department", Arrays.asList(departmentCode != null ? departmentCode : "COF"));
            assignment.put("assign:dueDate", actionDate != null ? 
                         actionDate.toString() + "Z" : 
                         LocalDateTime.now().toString() + "Z");
            assignment.put("assign:action", CorrespondenceUtils.mapAction(decisionGuid));
            assignment.put("assign:private", false);
            assignment.put("assign:canReAssign", false);
            
            request.setAssignment(assignment);
            request.getContext().put("tenantId", "ITBA");
            request.getContext().put("isReadOnly", "true");

            logApiCall("CREATE_OUTGOING_ASSIGNMENT", url, request);
            
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
                logger.info("Successfully created outgoing assignment: {}", transactionGuid);
            } else {
                logger.error("Failed to create outgoing assignment {} - Status: {}", transactionGuid, response.getStatusCode());
            }
            
            return success;
            
        } catch (Exception e) {
            logger.error("Error creating outgoing assignment: {}", transactionGuid, e);
            return false;
        }
    }
    
    /**
     * Approves outgoing correspondence without approval workflow
     */
    public boolean approveOutgoingCorrespondence(String documentId, String asUser) {
        try {
            String url = getAutomationEndpoint();
            
            OutgoingApprovalRequest request = new OutgoingApprovalRequest();
            
            // Set params
            request.setOperationName("AC_UA_OutgoingCorrespondence_SendWithoutApproval");
            request.setAsUser(adminUsername);
            request.setDocID(documentId);
            request.setDocCreator(asUser);
            request.setTenantID("ITBA");
            
            logApiCall("APPROVE_OUTGOING_CORRESPONDENCE", url, request);
            
            HttpHeaders headers = createHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<OutgoingApprovalRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String.class
            );
            
            boolean success = response.getStatusCode().is2xxSuccessful();
            if (success) {
                logger.info("Successfully approved outgoing correspondence: {}", documentId);
            } else {
                logger.error("Failed to approve outgoing correspondence {} - Status: {}", documentId, response.getStatusCode());
            }
            
            return success;
            
        } catch (Exception e) {
            logger.error("Error approving outgoing correspondence: {}", documentId, e);
            return false;
        }
    }
    
    /**
     * Registers outgoing correspondence with reference
     */
    public boolean registerOutgoingWithReference(String documentId, String asUser, Map<String, Object> outCorrespondenceContext) {
        try {
            String url = getAutomationEndpoint();
            
            OutgoingRegisterWithReferenceRequest request = new OutgoingRegisterWithReferenceRequest();
            
            // Set params
            request.setOperationName("AC_UA_OutgoingCorrespondence_Register_WithReference");
            request.setAsUser(adminUsername);
            request.setDocID(documentId);
            request.setDocCreator(asUser);
            request.setTenantID("ITBA");
            
            // Set context
            request.setOutCorrespondence(outCorrespondenceContext);
            
            logApiCall("REGISTER_OUTGOING_WITH_REFERENCE", url, request);
            
            HttpHeaders headers = createHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<OutgoingRegisterWithReferenceRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String.class
            );
            
            boolean success = response.getStatusCode().is2xxSuccessful();
            if (success) {
                logger.info("Successfully registered outgoing correspondence with reference: {}", documentId);
            } else {
                logger.error("Failed to register outgoing correspondence with reference {} - Status: {}", documentId, response.getStatusCode());
            }
            
            return success;
            
        } catch (Exception e) {
            logger.error("Error registering outgoing correspondence with reference: {}", documentId, e);
            return false;
        }
    }
    
    /**
     * Sends outgoing correspondence
     */
    public boolean sendOutgoingCorrespondence(String documentId, String asUser) {
        try {
            String url = getAutomationEndpoint();
            
            OutgoingSendRequest request = new OutgoingSendRequest();
            
            // Set params
            request.setOperationName("AC_UA_OutgoingCorrespondence_Send");
            request.setAsUser(adminUsername);
            request.setDocID(documentId);
            request.setDocCreator(asUser);
            request.setTenantID("ITBA");
            
            logApiCall("SEND_OUTGOING_CORRESPONDENCE", url, request);
            
            HttpHeaders headers = createHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<OutgoingSendRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String.class
            );
            
            boolean success = response.getStatusCode().is2xxSuccessful();
            if (success) {
                logger.info("Successfully sent outgoing correspondence: {}", documentId);
            } else {
                logger.error("Failed to send outgoing correspondence {} - Status: {}", documentId, response.getStatusCode());
            }
            
            return success;
            
        } catch (Exception e) {
            logger.error("Error sending outgoing correspondence: {}", documentId, e);
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
            request.setEventName(eventName != null ? eventName : "outgoing_register");
            request.setEventDate(actionDate != null ? 
                               actionDate.toString() + "Z" : 
                               LocalDateTime.now().toString() + "Z");
            request.setEventTypes("userEvent");
            request.setEventComment(CorrespondenceUtils.cleanHtmlTags(eventComment));
            request.setDocumentTypes("OutgoingCorrespondence");
            request.setExtendedInfo(null);
            request.setCurrentLifeCycle("draft");
            request.setPerson(fromUserName != null ? fromUserName : "itba-emp1");
            
            logApiCall("CREATE_OUTGOING_BUSINESS_LOG", url, request);
            
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
                logger.info("Successfully created outgoing business log: {}", transactionGuid);
            } else {
                logger.error("Failed to create outgoing business log {} - Status: {}", transactionGuid, response.getStatusCode());
            }
            
            return success;
            
        } catch (Exception e) {
            logger.error("Error creating outgoing business log: {}", transactionGuid, e);
            return false;
        }
    }
    
    /**
     * Creates comment in destination system
     */
    public boolean createComment(String commentGuid, String documentId, LocalDateTime commentCreationDate,
                               String commentText, String creationUserGuid) {
        try {
            String url = getAutomationEndpoint();
            
            CommentCreateRequest request = new CommentCreateRequest();
            
            // Set params according to API specification
            request.setOperationName("Document.CreateCustomComment");
            request.setDocID(documentId);
            request.setDocDate(commentCreationDate != null ? 
                             commentCreationDate.toString() + "Z" : 
                             LocalDateTime.now().toString() + "Z");
            request.setGuid(commentGuid);
            request.setAuthor(creationUserGuid != null ? creationUserGuid : "itba-emp1");
            request.setDate(commentCreationDate != null ? 
                          commentCreationDate.toString() + "Z" : 
                          LocalDateTime.now().toString() + "Z");
            request.setText(CorrespondenceUtils.cleanHtmlTags(commentText));
            
            logApiCall("CREATE_OUTGOING_COMMENT", url, request);
            
            HttpHeaders headers = createHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<CommentCreateRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String.class
            );
            
            boolean success = response.getStatusCode().is2xxSuccessful();
            if (success) {
                logger.info("Successfully created outgoing comment: {}", commentGuid);
            } else {
                logger.error("Failed to create outgoing comment {} - Status: {}", commentGuid, response.getStatusCode());
            }
            
            return success;
            
        } catch (Exception e) {
            logger.error("Error creating outgoing comment: {}", commentGuid, e);
            return false;
        }
    }
    
    /**
     * Closes outgoing correspondence in destination system
     */
    public boolean closeOutgoingCorrespondence(String correspondenceGuid, String documentId, 
                                             String asUser, LocalDateTime closeDate) {
        try {
            String url = getAutomationEndpoint();
            
            OutgoingClosingRequest request = new OutgoingClosingRequest();
            
            // Set params according to API specification
            request.setOperationName("AC_UA_OutgoingCorrespondence_Close");
            request.setAsUser(asUser != null ? asUser : "itba-emp1");
            request.setDocID(documentId);
            request.setDocCreator(asUser);
            
            // Set update properties
            Map<String, Object> updateProp = new HashMap<>();
            updateProp.put("corr:closeDate", closeDate != null ? 
                         closeDate.toString() + "Z" : 
                         LocalDateTime.now().toString() + "Z");
            request.setUpdateProp(updateProp);
            request.setTenantID("ITBA");
            
            logApiCall("CLOSE_OUTGOING_CORRESPONDENCE", url, request);
            
            HttpHeaders headers = createHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<OutgoingClosingRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String.class
            );
            
            boolean success = response.getStatusCode().is2xxSuccessful();
            if (success) {
                logger.info("Successfully closed outgoing correspondence: {}", correspondenceGuid);
            } else {
                logger.error("Failed to close outgoing correspondence {} - Status: {}", correspondenceGuid, response.getStatusCode());
            }
            
            return success;
            
        } catch (Exception e) {
            logger.error("Error closing outgoing correspondence: {}", correspondenceGuid, e);
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
            System.out.println("=== OUTGOING DESTINATION API CALL: " + operation + " ===");
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
            logger.debug("Using dynamic Keycloak token for outgoing API request");
        } else {
            headers.set("Authorization", "Bearer " + authToken);
            logger.debug("Using static token from configuration for outgoing");
        }
        
        headers.set("Connection", "keep-alive");
        return headers;
    }
    
    /**
     * Gets the toAgency from correspondence_send_tos table
     * 
     * @param correspondenceGuid The correspondence GUID to look up
     * @return Agency code for the destination agency
     */
    private String getToAgencyFromSendTos(String correspondenceGuid) {
        try {
            logger.debug("Looking up toAgency for correspondence: {}", correspondenceGuid);
            
            // Get send_tos records for this correspondence
            List<CorrespondenceSendTo> sendTos =
                correspondenceSendToRepository.findByDocGuid(correspondenceGuid);
            
            if (sendTos == null || sendTos.isEmpty()) {
                logger.debug("No send_tos found for correspondence: {}, using default agency", correspondenceGuid);
                return "001"; // Default agency code
            }
            
            // Get the first send_to record (you might want to add business logic here for multiple recipients)
            com.importservice.entity.CorrespondenceSendTo firstSendTo = sendTos.get(0);
            String sendToGuid = firstSendTo.getSendToGuid();
            
            if (sendToGuid == null || sendToGuid.trim().isEmpty()) {
                logger.debug("Empty sendToGuid for correspondence: {}, using default agency", correspondenceGuid);
                return "001"; // Default agency code
            }
            
            // Map the send_to_guid to agency code using AgencyMappingUtils
            String agencyCode = AgencyMappingUtils.mapAgencyGuidToCode(sendToGuid);
            
            if (agencyCode == null) {
                logger.debug("No agency mapping found for sendToGuid: {}, using default agency", sendToGuid);
                return "001"; // Default agency code
            }
            
            logger.info("Found toAgency '{}' for correspondence '{}' from sendToGuid '{}'", 
                       agencyCode, correspondenceGuid, sendToGuid);
            return agencyCode;
            
        } catch (Exception e) {
            logger.error("Error getting toAgency from send_tos for correspondence: {}", correspondenceGuid, e);
            return "001"; // Default fallback
        }
    }
}