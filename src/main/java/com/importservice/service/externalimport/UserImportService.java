package com.importservice.service.externalimport;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.importservice.dto.*;
import com.importservice.service.KeycloakTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class UserImportService {

    private static final Logger logger = LoggerFactory.getLogger(UserImportService.class);

    @Value("${destination.api.url}")
    private String destinationApiBaseUrl;

    @Value("${destination.api.token}")
    private String authToken;

    @Value("${destination.api.logging.enabled:false}")
    private boolean loggingEnabled;

    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private KeycloakTokenService keycloakTokenService;
    
    private final ObjectMapper objectMapper;

    public UserImportService() {
        this.objectMapper = new ObjectMapper();
    }

    public ImportResponseDto importUsersToDestination() {
        logger.info("Starting user import to destination system");
        
        List<String> errors = new ArrayList<>();
        int successfulImports = 0;
        int failedImports = 0;
        
        try {
            List<UserImportDto> users = readUsersFromJson();
            logger.info("Found {} users to import", users.size());
            
            for (UserImportDto user : users) {
                try {
                    // Step 1: Create user
                    boolean userCreated = createUser(user);
                    if (!userCreated) {
                        failedImports++;
                        errors.add("Failed to create user: " + user.getEmail());
                        continue;
                    }
                    
                    // Step 2: Assign user to department and roles
                    boolean userAssigned = assignUserToDepartmentAndRoles(user);
                    if (!userAssigned) {
                        failedImports++;
                        errors.add("Failed to assign user to department/roles: " + user.getEmail());
                        continue;
                    }
                    
                    successfulImports++;
                    logger.info("Successfully imported user: {}", user.getEmail());
                    
                } catch (Exception e) {
                    failedImports++;
                    String errorMsg = "Error importing user " + user.getEmail() + ": " + e.getMessage();
                    errors.add(errorMsg);
                    logger.error(errorMsg, e);
                }
            }
            
            String status = failedImports == 0 ? "SUCCESS" : "PARTIAL_SUCCESS";
            String message = String.format("User import completed. Success: %d, Failed: %d", 
                                         successfulImports, failedImports);
            
            return new ImportResponseDto(status, message, users.size(), 
                                       successfulImports, failedImports, errors);
                                       
        } catch (IOException e) {
            logger.error("Failed to read users JSON file", e);
            return new ImportResponseDto("ERROR", "Failed to read source data", 0, 0, 0, 
                Collections.singletonList("Failed to read JSON file: " + e.getMessage()));
        }
    }

    private List<UserImportDto> readUsersFromJson() throws IOException {
        ClassPathResource resource = new ClassPathResource("users.json");
        InputStream inputStream = resource.getInputStream();
        
        TypeReference<List<UserImportDto>> typeReference = new TypeReference<List<UserImportDto>>() {};
        return objectMapper.readValue(inputStream, typeReference);
    }

    private boolean createUser(UserImportDto user) {
        try {
            // Extract base URL and construct the create user endpoint
            String baseUrl = destinationApiBaseUrl.substring(0, destinationApiBaseUrl.lastIndexOf("/"));
            String url = baseUrl + "/AC_UA_AddDomainUser";
            
            CreateUserRequestDto request = mapToCreateUserRequest(user);
            
            if (loggingEnabled) {
                System.out.println("=== CREATE USER ===");
                System.out.println("URL: " + url);
                System.out.println("Request Body: " + request.toString());
                System.out.println("=== END CREATE USER ===");
            }
            
            HttpHeaders headers = createHttpHeaders();
            HttpEntity<CreateUserRequestDto> httpEntity = new HttpEntity<>(request, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.POST, httpEntity, String.class
            );
            
            boolean success = response.getStatusCode().is2xxSuccessful();
            if (success) {
                logger.debug("Successfully created user: {}", user.getEmail());
            } else {
                logger.warn("Failed to create user: {} - Status: {}", user.getEmail(), response.getStatusCode());
            }
            
            return success;
            
        } catch (Exception e) {
            logger.error("Failed to create user: " + user.getEmail(), e);
            return false;
        }
    }

    private boolean assignUserToDepartmentAndRoles(UserImportDto user) {
        try {
            // Extract base URL and construct the assign user endpoint
            String baseUrl = destinationApiBaseUrl.substring(0, destinationApiBaseUrl.lastIndexOf("/"));
            String url = baseUrl + "/AC_UA_User_AddToDepartment";
            
            AssignUserRequestDto request = mapToAssignUserRequest(user);
            
            if (loggingEnabled) {
                System.out.println("=== ASSIGN USER ===");
                System.out.println("URL: " + url);
                System.out.println("Request Body: " + request.toString());
                System.out.println("=== END ASSIGN USER ===");
            }
            
            HttpHeaders headers = createHttpHeaders();
            HttpEntity<AssignUserRequestDto> httpEntity = new HttpEntity<>(request, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.POST, httpEntity, String.class
            );
            
            boolean success = response.getStatusCode().is2xxSuccessful();
            if (success) {
                logger.debug("Successfully assigned user to department: {}", user.getEmail());
            } else {
                logger.warn("Failed to assign user to department: {} - Status: {}", user.getEmail(), response.getStatusCode());
            }
            
            return success;
            
        } catch (Exception e) {
            logger.error("Failed to assign user to department: " + user.getEmail(), e);
            return false;
        }
    }

    private CreateUserRequestDto mapToCreateUserRequest(UserImportDto user) {
        CreateUserRequestDto request = new CreateUserRequestDto();
        
        // Extract username from email
        String username = user.getUsernameFromEmail();
        request.setUsername(username);
        
        // Set phone number
        //TODO change to original
        request.setPhoneNumber(user.getMobileNumber());
        //request.setPhoneNumber("5456545");

        // Set email
        request.setEmail(user.getEmail());
        
        // Parse Arabic name
        String[] arabicNames = parseFullName(user.getFullArabicName());
        request.setFirstNameAr(arabicNames[0]);
        request.setLastNameAr(arabicNames[1]);
        
        // Parse English name
        String[] englishNames = parseFullName(user.getFullEnglishName());
        request.setFirstNameEn(englishNames[0]);
        request.setLastNameEn(englishNames[1]);
        
        // Set job grades
        request.setJobGradeEn(user.getEnglishPositionName());
        request.setJobGradeAr(user.getArabicPositionName());
        
        // Set constants
        request.setKeycloak("disable");
        request.setTenantID("ITBA");
        
        return request;
    }

    private AssignUserRequestDto mapToAssignUserRequest(UserImportDto user) {
        AssignUserRequestDto request = new AssignUserRequestDto();
        
        // Extract username from email
        String username = user.getUsernameFromEmail();
        request.setUsername(username);
        
        // Set basic role based on manager status
        String basicRole = user.isManagerUser() ? "Manager" : "Employee";
        request.setBasicRole(basicRole);
        
        // Set department code
        request.setDepartment(user.getDepartmentCode());
        
        // Set roles based on manager status and security clearance
        List<String> roles = buildRolesList(user);
        request.setRoles(roles);
        
        // Set list of roles based on department and manager status
        List<String> listOfRoles = buildListOfRoles(user);
        request.setListOfRoles(listOfRoles);
        
        // Set tenant ID
        request.setTenantID("ITBA");
        
        return request;
    }

    private String[] parseFullName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return new String[]{"", ""};
        }
        
        String[] parts = fullName.trim().split("\\s+");
        
        if (parts.length == 1) {
            return new String[]{parts[0], ""};
        } else if (parts.length == 2) {
            return new String[]{parts[0], parts[1]};
        } else {
            // Take first two words as first name, rest as last name
            String firstName = parts[0] + " " + parts[1];
            String lastName = String.join(" ", Arrays.copyOfRange(parts, 2, parts.length));
            return new String[]{firstName, lastName};
        }
    }

    private List<String> buildRolesList(UserImportDto user) {
        List<String> roles = new ArrayList<>();
        
        String prefix = user.isManagerUser() ? "Manager" : "Employee";
        
        // Always add normal role
        roles.add(prefix + "_Normal");
        
        // Always add non-transferable role
        roles.add(prefix + "_NonTransferable");
        
        // Add secret role if user has secret clearance
        if (user.hasSecret()) {
            roles.add(prefix + "_Secret");
        }
        
        // Add top secret role if user has top secret clearance
        if (user.hasTopSecret()) {
            roles.add(prefix + "_Top_Secret");
        }
        
        return roles;
    }

    private List<String> buildListOfRoles(UserImportDto user) {
        List<String> listOfRoles = new ArrayList<>();
        
        if (user.isCeoDepartment()) {
            // CEO department gets all roles
            listOfRoles.addAll(getAllCeoRoles());
        } else if (user.isManagerUser()) {
            // Manager in non-CEO department
            listOfRoles.addAll(getManagerRoles(user));
        } else {
            // Regular employee in non-CEO department
            listOfRoles.addAll(getEmployeeRoles(user));
        }
        
        return listOfRoles;
    }

    private List<String> getAllCeoRoles() {
        return Arrays.asList(
            "incoming_assignments_receiver",
            "incoming_assignments_receiver_Secret",
            "incoming_assignments_receiver_Top_Secret",
            "incoming_assignments_receiver_NonTransferable",
            "incoming_handler_Secret",
            "incoming_handler",
            "incoming_handler_Top_Secret",
            "incoming_handler_NonTransferable",
            "incoming_owner",
            "incoming_owner_Secret",
            "incoming_owner_Top_Secret",
            "incoming_owner_NonTransferable",
            "incoming_registrar",
            "incoming_registrar_Secret",
            "incoming_registrar_Top_Secret",
            "incoming_registrar_NonTransferable",
            "incoming_sender",
            "incoming_sender_Secret",
            "incoming_sender_Top_Secret",
            "incoming_sender_NonTransferable",
            "outgoing_approver",
            "outgoing_approver_Secret",
            "outgoing_approver_Top_Secret",
            "outgoing_approver_NonTransferable",
            "outgoing_assignments_receiver",
            "outgoing_assignments_receiver_Secret",
            "outgoing_assignments_receiver_Top_Secret",
            "outgoing_assignments_receiver_NonTransferable",
            "outgoing_registrar",
            "outgoing_registrar_Secret",
            "outgoing_registrar_Top_Secret",
            "outgoing_registrar_NonTransferable",
            "outgoing_sender",
            "outgoing_sender_Secret",
            "outgoing_sender_Top_Secret",
            "outgoing_sender_NonTransferable",
            "internal_assignments_receiver",
            "internal_assignments_receiver_Secret",
            "internal_assignments_receiver_Top_Secret",
            "internal_assignments_receiver_NonTransferable",
            "internal_owner",
            "internal_owner_Secret",
            "internal_owner_Top_Secret",
            "internal_owner_NonTransferable",
            "internal_registrar",
            "internal_registrar_Secret",
            "internal_registrar_Top_Secret",
            "internal_registrar_NonTransferable",
            "internal_sender",
            "internal_sender_Secret",
            "internal_sender_Top_Secret",
            "internal_sender_NonTransferable"
        );
    }

    private List<String> getManagerRoles(UserImportDto user) {
        List<String> roles = new ArrayList<>(Arrays.asList(
            "incoming_assignments_receiver",
            "incoming_assignments_receiver_Secret",
            "incoming_assignments_receiver_Top_Secret",
            "incoming_assignments_receiver_NonTransferable",
            "outgoing_approver",
            "outgoing_approver_Secret",
            "outgoing_approver_Top_Secret",
            "outgoing_approver_NonTransferable",
            "outgoing_assignments_receiver",
            "outgoing_assignments_receiver_Secret",
            "outgoing_assignments_receiver_Top_Secret",
            "outgoing_assignments_receiver_NonTransferable",
            "internal_assignments_receiver",
            "internal_assignments_receiver_Secret",
            "internal_assignments_receiver_Top_Secret",
            "internal_assignments_receiver_NonTransferable",
            "internal_owner",
            "internal_owner_Secret",
            "internal_owner_Top_Secret",
            "internal_owner_NonTransferable",
            "internal_registrar",
            "internal_registrar_Secret",
            "internal_registrar_Top_Secret",
            "internal_registrar_NonTransferable",
            "internal_sender",
            "internal_sender_Secret",
            "internal_sender_Top_Secret",
            "internal_sender_NonTransferable"
        ));
        
        return roles;
    }

    private List<String> getEmployeeRoles(UserImportDto user) {
        List<String> roles = new ArrayList<>();
        
        // Base roles for all employees
        roles.add("internal_registrar");
        roles.add("internal_registrar_NonTransferable");
        roles.add("internal_sender");
        roles.add("internal_sender_NonTransferable");
        
        // Add secret roles if user has secret clearance
        if (user.hasSecret()) {
            roles.add("internal_registrar_Secret");
            roles.add("internal_sender_Secret");
        }
        
        // Add top secret roles if user has top secret clearance
        if (user.hasTopSecret()) {
            roles.add("internal_registrar_Top_Secret");
            roles.add("internal_sender_Top_Secret");
        }
        
        return roles;
    }

    private HttpHeaders createHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        headers.set("Accept-Language", "en-US,en;q=0.9,ar;q=0.8");
        
        // Use dynamic token from Keycloak service if available
        String token = keycloakTokenService.getCurrentToken();
        if (token != null) {
            headers.set("Authorization", "Bearer " + token);
            logger.debug("Using dynamic Keycloak token for user import");
        } else {
            headers.set("Authorization", "Bearer " + authToken);
            logger.debug("Using static token from configuration for user import");
        }
        
        headers.set("Connection", "keep-alive");
        headers.set("Content-Type", "application/json");
        return headers;
    }
}