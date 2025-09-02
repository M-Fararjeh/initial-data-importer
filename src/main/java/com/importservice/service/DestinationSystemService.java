package com.importservice.service;

import com.importservice.dto.AttachmentCreateRequest;
import com.importservice.dto.BusinessLogCreateRequest;
import com.importservice.dto.CommentCreateRequest;
import com.importservice.dto.BatchCreateResponse;
import com.importservice.dto.CorrespondenceCreateResponse;
import com.importservice.dto.IncomingCorrespondenceCreateRequest;
import com.importservice.dto.PhysicalAttachmentRequest;
import com.importservice.dto.ReadyToRegisterRequest;
import com.importservice.dto.RegisterWithReferenceRequest;
import com.importservice.dto.StartWorkRequest;
import com.importservice.dto.SetOwnerRequest;
import com.importservice.dto.AssignmentCreateRequest;
import com.importservice.dto.ClosingRequest;
import com.importservice.entity.CorrespondenceAttachment;
import com.importservice.entity.CorrespondenceComment;
import com.importservice.entity.Correspondence;
import com.importservice.util.AttachmentUtils;
import com.importservice.util.CorrespondenceUtils;
import com.importservice.util.CorrespondenceSubjectGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.importservice.util.HijriDateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class DestinationSystemService {
    
    private static final Logger logger = LoggerFactory.getLogger(DestinationSystemService.class);
    
    @Value("${destination.api.url}")
    private String destinationApiUrl;
    
    @Value("${destination.api.token}")
    private String authToken;
    
    @Value("${file.upload.use-sample:false}")
    private boolean useSampleFile;
    
    @Value("${file.upload.sample.base64:testbase64}")
    private String sampleBase64;
    
    @Value("${file.upload.sample.filename:sample_document.pdf}")
    private String sampleFileName;
    
    @Value("${file.upload.sample.mimetype:application/pdf}")
    private String sampleMimeType;
    
    @Value("${destination.api.logging.enabled:false}")
    private boolean loggingEnabled;
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private CorrespondenceSubjectGenerator subjectGenerator;
    
    /**
     * Logs request details if logging is enabled
     */
    private void logApiCall(String operation, String url, Object requestBody) {
        if (url.contains("/nuxeo/api/v1/upload/")){
            return;
        }
        if (loggingEnabled) {
            System.out.println("=== DESTINATION API CALL: " + operation + " ===");
            System.out.println("URL: " + url);
            if (requestBody != null) {
                System.out.println("Request Body: " + requestBody.toString());
            }
            System.out.println("=== END API CALL ===");
        }
    }
    
    /**
     * Gets the base URL from the destination API URL
     */
    private String getBaseUrl() {
        if (destinationApiUrl == null) {
            return "http://18.206.121.44";
        }
        // Extract base URL from destination.api.url
        // Example: http://18.206.121.44/nuxeo/api/v1/custom-automation/AC_UA_ExternalAgency_Create
        // Should return: http://18.206.121.44
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
                byte[] sampleFileData = Base64.getDecoder().decode(sampleBase64);
                return uploadFileToBatch(batchId, fileId, sampleFileData, sampleFileName, url);
            } else {
                byte[] fileData = Base64.getDecoder().decode(base64Data);
                return uploadFileToBatch(batchId, fileId, fileData, fileName, url);
            }
        } catch (IllegalArgumentException e) {
            logger.error("Invalid base64 data for file: {}", fileName, e);
            return false;
        }
    }
    
    /**
     * Uploads file data to batch
     */
    private boolean uploadFileToBatch(String batchId, String fileId, byte[] fileData, String fileName, String url) {
        try {
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
                url,
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
     * Creates incoming correspondence in destination system
     */
    public String createIncomingCorrespondence(String guid, String asUser, String docDate,
                                             String subject, String externalRef, String notes,
                                             String referenceNo, String category, String secrecyLevel,
                                             String priority, String gDueDate, String hDueDate,
                                             Boolean requireReply, String fromAgency,
                                             String gDocumentDate, String hDocumentDate,
                                             String gDate, String hDate, String toDepartment,
                                             String batchId, String action) {
        try {
            String url = getAutomationEndpoint();
            
            IncomingCorrespondenceCreateRequest request = new IncomingCorrespondenceCreateRequest();
            
            // Set params
            request.setOperationName("AC_UA_IncomingCorrespondence_Create_Application");
            request.setAsUser(asUser != null ? asUser : "itba-emp1");
            request.setGuid(guid);
            request.setDocDate(docDate);
            
            // Build incCorrespondence context
            Map<String, Object> incCorrespondence = new HashMap<>();
            
            // Use subject generator if subject is empty and random generation is enabled
            String finalSubject = subject;
            if (subjectGenerator.isRandomSubjectEnabled()) {
                finalSubject = subjectGenerator.generateSubjectWithCategory(category);
                logger.info("Generated random subject for correspondence {}: {}", guid, finalSubject);
            }
            
            incCorrespondence.put("corr:subject", finalSubject != null ? finalSubject : "");
            incCorrespondence.put("corr:externalCorrespondenceNumber", externalRef != null ? externalRef : "");
            incCorrespondence.put("corr:remarks", notes != null ? notes : "");
            incCorrespondence.put("corr:referenceNumber", referenceNo != null ? referenceNo : "");
            incCorrespondence.put("corr:category", category);
            incCorrespondence.put("corr:secrecyLevel", secrecyLevel);
            incCorrespondence.put("corr:priority", priority);
            incCorrespondence.put("corr:gDueDate", gDueDate);
            incCorrespondence.put("corr:hDueDate", hDueDate);
            incCorrespondence.put("corr:requireReply", requireReply);
            incCorrespondence.put("corr:from", "");
            incCorrespondence.put("corr:fromAgency", fromAgency);
            incCorrespondence.put("corr:gDocumentDate", gDocumentDate);
            incCorrespondence.put("corr:hDocumentDate", hDocumentDate);
            incCorrespondence.put("corr:gDate", gDate);
            incCorrespondence.put("corr:hDate", hDate);
            incCorrespondence.put("corr:delivery", "unknown");
            incCorrespondence.put("corr:to", toDepartment);
            incCorrespondence.put("corr:toAgency", "ITBA");
            incCorrespondence.put("corr:action", action);
            
            // Add file content if batch exists
            if (batchId != null) {
                Map<String, Object> fileContent = new HashMap<>();
                fileContent.put("upload-batch", batchId);
                fileContent.put("upload-fileId", "0");
                incCorrespondence.put("file:content", fileContent);
            }
            
            request.setIncCorrespondence(incCorrespondence);
            
            logApiCall("CREATE_INCOMING_CORRESPONDENCE", url, request);
            
            HttpHeaders headers = createHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<IncomingCorrespondenceCreateRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                // Parse response to extract document ID
                try {
                    String responseBody = response.getBody();
                    if (responseBody != null && !responseBody.trim().isEmpty()) {
                        CorrespondenceCreateResponse createResponse = objectMapper.readValue(responseBody, CorrespondenceCreateResponse.class);
                        String documentId = createResponse.getUid();
                        
                        if (documentId != null && !documentId.trim().isEmpty()) {
                            logger.info("Successfully created incoming correspondence: {} with document ID: {}", guid, documentId);
                            return documentId;
                        } else {
                            logger.warn("Document ID not found in response for correspondence: {}", guid);
                            return guid; // Fallback to original GUID
                        }
                    } else {
                        logger.warn("Empty response body for correspondence creation: {}", guid);
                        return guid; // Fallback to original GUID
                    }
                } catch (Exception e) {
                    logger.error("Error parsing correspondence creation response for: {}", guid, e);
                    return guid; // Fallback to original GUID
                }
            } else {
                logger.error("Failed to create incoming correspondence - Status: {}", response.getStatusCode());
                return null;
            }
            
        } catch (Exception e) {
            logger.error("Error creating incoming correspondence: {}", guid, e);
            return null;
        }
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
            //request.setAsUser(attachment.getCreationUserName() != null ? attachment.getCreationUserName() : "itba-emp1");
            request.setDocID(correspondenceDocumentId); // Use the correspondence document ID from destination system
            request.setGuid(attachment.getGuid()); // Use attachment GUID
            request.setAsUser("cts_admin");
            request.setDocCreator(attachment.getCreationUserName() != null ? attachment.getCreationUserName() : "itba-emp1");

            // Build attachment context
            Map<String, Object> attachmentData = new HashMap<>();
            attachmentData.put("title", CorrespondenceUtils.cleanFileName(attachment.getName()));
            attachmentData.put("barcode", AttachmentUtils.generateAttachmentBarcode(attachment.getGuid()));
            attachmentData.put("corr_attach:attachmentId", attachment.getGuid());
/*            attachmentData.put("corr_attach:classification",
                             CorrespondenceUtils.mapAttachmentClassification(
                                 attachment.getFileType(), attachment.getName()));
            attachmentData.put("corr_attach:category", 
                             CorrespondenceUtils.mapAttachmentCategory(
                                 attachment.getIsPrimary() != null && attachment.getIsPrimary(), 
                                 attachment.getFileType()));
            attachmentData.put("corr_attach:type", 
                             CorrespondenceUtils.mapAttachmentType(attachment.getName()));*/
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
            request.getContext().put("tenantId", "ITBA");
            request.setAttachment(attachmentData);
            
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
     * Creates physical attachment for correspondence
     */
    public boolean createPhysicalAttachment(String correspondenceGuid, String asUser, String physicalAttachments) {
        try {
            String url = getAutomationEndpoint();
            
            PhysicalAttachmentRequest request = new PhysicalAttachmentRequest();
            
            // Set params
            request.setOperationName("AC_UA_PhysicalAttachment_Add");
            request.setDocID(correspondenceGuid);
            request.setAsUser(asUser != null ? asUser : "itba-emp1");
            
            // Set context
            request.setPhysicalAttachments(physicalAttachments != null ? physicalAttachments : "");
            
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
                logger.info("Successfully created physical attachment for correspondence: {}", correspondenceGuid);
            } else {
                logger.error("Failed to create physical attachment for correspondence {} - Status: {}", 
                           correspondenceGuid, response.getStatusCode());
            }
            
            return success;
            
        } catch (Exception e) {
            logger.error("Error creating physical attachment for correspondence: {}", correspondenceGuid, e);
            return false;
        }
    }
    
    /**
     * Sets incoming correspondence ready to register
     */
    public boolean setIncomingReadyToRegister(String correspondenceGuid, String asUser) {
        try {
            String url = getAutomationEndpoint();
            
            ReadyToRegisterRequest request = new ReadyToRegisterRequest();
            
            // Set params
            request.setOperationName("AC_UA_Correspondence_ReadyToRegister");
            request.setAsUser(asUser != null ? asUser : "itba-emp1");
            request.setDocID(correspondenceGuid);
            
            logApiCall("SET_READY_TO_REGISTER", url, request);
            
            HttpHeaders headers = createHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<ReadyToRegisterRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String.class
            );
            
            boolean success = response.getStatusCode().is2xxSuccessful();
            if (success) {
                logger.info("Successfully set ready to register for correspondence: {}", correspondenceGuid);
            } else {
                logger.error("Failed to set ready to register for correspondence {} - Status: {}", 
                           correspondenceGuid, response.getStatusCode());
            }
            
            return success;
            
        } catch (Exception e) {
            logger.error("Error setting ready to register for correspondence: {}", correspondenceGuid, e);
            return false;
        }
    }
    
    /**
     * Registers correspondence with reference
     */
    public boolean registerWithReference(String correspondenceGuid, String asUser, Map<String, Object> incCorrespondenceContext, 
                                       String action, String toDepartment) {
        try {
            String url = getAutomationEndpoint();
            
            RegisterWithReferenceRequest request = new RegisterWithReferenceRequest();
            
            // Set params
            request.setOperationName("AC_UA_IncomingCorrespondence_Register_WithReference");
            request.setAsUser(asUser != null ? asUser : "itba-emp1");
            request.setDocID(correspondenceGuid);
            
            // Set context - reuse the same context from correspondence creation and add required parameters
            Map<String, Object> updatedContext = new HashMap<>(incCorrespondenceContext);
            updatedContext.put("corr:action", action != null ? action : "ForAdvice");
            updatedContext.put("corr:to", toDepartment != null ? toDepartment : "COF");
            updatedContext.put("corr:from", "");
            
            request.setIncCorrespondence(updatedContext);
            
            logApiCall("REGISTER_WITH_REFERENCE", url, request);
            
            HttpHeaders headers = createHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<RegisterWithReferenceRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String.class
            );
            
            boolean success = response.getStatusCode().is2xxSuccessful();
            if (success) {
                logger.info("Successfully registered with reference for correspondence: {}", correspondenceGuid);
            } else {
                logger.error("Failed to register with reference for correspondence {} - Status: {}", 
                           correspondenceGuid, response.getStatusCode());
            }
            
            return success;
            
        } catch (Exception e) {
            logger.error("Error registering with reference for correspondence: {}", correspondenceGuid, e);
            return false;
        }
    }
    
    /**
     * Starts work for incoming correspondence (send)
     */
    public boolean startIncomingCorrespondenceWork(String correspondenceGuid, String asUser) {
        try {
            String url = getAutomationEndpoint();
            
            StartWorkRequest request = new StartWorkRequest();
            
            // Set params
            request.setOperationName("AC_UA_IncomingCorrespondence_Send");
            request.setAsUser(asUser != null ? asUser : "itba-emp1");
            request.setDocID(correspondenceGuid);
            
            logApiCall("START_WORK", url, request);
            
            HttpHeaders headers = createHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<StartWorkRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String.class
            );
            
            boolean success = response.getStatusCode().is2xxSuccessful();
            if (success) {
                logger.info("Successfully started work for correspondence: {}", correspondenceGuid);
            } else {
                logger.error("Failed to start work for correspondence {} - Status: {}", 
                           correspondenceGuid, response.getStatusCode());
            }
            
            return success;
            
        } catch (Exception e) {
            logger.error("Error starting work for correspondence: {}", correspondenceGuid, e);
            return false;
        }
    }
    
    /**
     * Sets owner for correspondence (claim by owner)
     */
    public boolean setCorrespondenceOwner(String correspondenceGuid, String asUser) {
        try {
            String url = getAutomationEndpoint();
            
            SetOwnerRequest request = new SetOwnerRequest();
            
            // Set params
            request.setOperationName("AC_UA_Correspondence_SetOwner");
            request.setAsUser(asUser != null ? asUser : "itba-emp1");
            request.setDocID(correspondenceGuid);
            
            logApiCall("SET_OWNER", url, request);
            
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
                logger.info("Successfully set owner for correspondence: {}", correspondenceGuid);
            } else {
                logger.error("Failed to set owner for correspondence {} - Status: {}", 
                           correspondenceGuid, response.getStatusCode());
            }
            
            return success;
            
        } catch (Exception e) {
            logger.error("Error setting owner for correspondence: {}", correspondenceGuid, e);
            return false;
        }
    }
    
    /**
     * Creates assignment in destination system
     */
    public boolean createAssignment(String transactionGuid, String asUser, String documentId, 
                                  LocalDateTime actionDate, String toUserName, String departmentCode, 
                                  String decisionGuid) {
        try {
            String url = getAutomationEndpoint();
            
            AssignmentCreateRequest request = new AssignmentCreateRequest();
            
            // Set params
            request.setOperationName("AC_UA_Assignment_Create");
            request.setDocID(documentId);
            request.setDocDate(actionDate != null ?
                             HijriDateUtils.addYears(actionDate,5).toString() + "Z" :
                             HijriDateUtils.addYears(LocalDateTime.now(),5).toString() + "Z");
            request.setGuid(transactionGuid);
            request.setAsUser("cts_admin");
            request.setDocCreator(asUser);
            
            // Build assignment context
            Map<String, Object> assignment = new HashMap<>();
            assignment.put("title", "assignment-" + transactionGuid);
            assignment.put("assign:assignee", Arrays.asList(toUserName != null ? toUserName : "itba-emp1"));
            assignment.put("assign:department", Arrays.asList(departmentCode != null ? departmentCode : "CEO"));
            assignment.put("assign:dueDate", actionDate != null ? 
                         actionDate.toString() + "Z" : 
                         LocalDateTime.now().toString() + "Z");
            assignment.put("assign:action", CorrespondenceUtils.mapAction(decisionGuid));
            assignment.put("assign:private", false);
            assignment.put("assign:canReAssign", false);
            //assignment.put("isReadOnly", true);
            //assignment.put("tenantId", "ITBA");
            
            request.setAssignment(assignment);
            request.getContext().put("tenantId", "ITBA");
            request.getContext().put("isReadOnly", "true");

            logApiCall("CREATE_ASSIGNMENT", url, request);
            
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
                logger.info("Successfully created assignment: {}", transactionGuid);
            } else {
                logger.error("Failed to create assignment {} - Status: {}", transactionGuid, response.getStatusCode());
            }
            
            return success;
            
        } catch (Exception e) {
            logger.error("Error creating assignment: {}", transactionGuid, e);
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
            request.setEventName(eventName != null ? eventName : "incoming_register");
            request.setEventDate(actionDate != null ? 
                               actionDate.toString() + "Z" : 
                               LocalDateTime.now().toString() + "Z");
            request.setEventTypes("userEvent");
            request.setEventComment(CorrespondenceUtils.cleanHtmlTags(eventComment));
            request.setDocumentTypes("IncomingCorrespondence");
            request.setExtendedInfo(null);
            request.setCurrentLifeCycle("draft");
            request.setPerson(fromUserName != null ? fromUserName : "itba-emp1");
            
            logApiCall("CREATE_BUSINESS_LOG", url, request);
            
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
                logger.info("Successfully created business log: {}", transactionGuid);
            } else {
                logger.error("Failed to create business log {} - Status: {}", transactionGuid, response.getStatusCode());
            }
            
            return success;
            
        } catch (Exception e) {
            logger.error("Error creating business log: {}", transactionGuid, e);
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
            
            logApiCall("CREATE_COMMENT", url, request);
            
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
                logger.info("Successfully created comment: {}", commentGuid);
            } else {
                logger.error("Failed to create comment {} - Status: {}", commentGuid, response.getStatusCode());
            }
            
            return success;
            
        } catch (Exception e) {
            logger.error("Error creating comment: {}", commentGuid, e);
            return false;
        }
    }
    
    /**
     * Closes correspondence in destination system
     */
    public boolean closeCorrespondence(String correspondenceGuid, String documentId, 
                                     String asUser, LocalDateTime closeDate) {
        try {
            String url = getAutomationEndpoint();
            
            ClosingRequest request = new ClosingRequest();
            
            // Set params according to API specification
            request.setCloseDate(closeDate != null ? 
                               closeDate.toString() + "Z" : 
                               LocalDateTime.now().toString() + "Z");
            request.setOperationName("AC_UA_IncomingCorrespondence_Close");
            request.setAsUser(asUser != null ? asUser : "itba-emp1");
            request.setDocID(documentId);
            
            logApiCall("CLOSE_CORRESPONDENCE", url, request);
            
            HttpHeaders headers = createHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<ClosingRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String.class
            );
            
            boolean success = response.getStatusCode().is2xxSuccessful();
            if (success) {
                logger.info("Successfully closed correspondence: {}", correspondenceGuid);
            } else {
                logger.error("Failed to close correspondence {} - Status: {}", correspondenceGuid, response.getStatusCode());
            }
            
            return success;
            
        } catch (Exception e) {
            logger.error("Error closing correspondence: {}", correspondenceGuid, e);
            return false;
        }
    }
    
    /**
     * Creates HTTP headers for API requests
     */
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        headers.set("Accept-Language", "en-US,en;q=0.9,ar;q=0.8");
        headers.set("Authorization", "Bearer " + authToken);
        headers.set("Connection", "keep-alive");
        return headers;
    }
}