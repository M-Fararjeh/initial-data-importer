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
     * Approves internal correspondence
     */
    public boolean approveInternalCorrespondence(String documentId, String asUser) {
        try {
            String url = getAutomationEndpoint();
            
            InternalApprovalRequest request = new InternalApprovalRequest();
            
            // Set params
            request.setOperationName("AC_UA_InternalCorrespondence_Approve");
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
            request.setOperationName("AC_UA_InternalCorrespondence_Send");
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
            request.setTenantID("ITBA");
            
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
                logger.error("Failed to set owner for internal correspondence {} - Status: {}", documentId, response.getStatusCode());
            }
            
            return success;
            
        } catch (Exception e) {
            logger.error("Error setting owner for internal correspondence: {}", documentId, e);
            return false;
        }
    }
    
    /**
     * Creates business log in destination system for internal correspondence
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
            request.setPerson(adminUsername); // Use cts_admin as creator for internal correspondence
            
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