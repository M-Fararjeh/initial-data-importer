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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
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

    @Autowired
    private ClassificationRepository classificationRepository;

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private CorrespondenceRepository correspondenceRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

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
    public ImportResponseDto importCorrespondences() {
        logger.info("Starting correspondences import");
        return importCorrespondenceData();
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

    private ImportResponseDto createErrorResponse(String message) {
        return new ImportResponseDto("ERROR", message, 0, 0, 0, List.of(message));
    }
}