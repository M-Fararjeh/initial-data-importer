package com.importservice.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.importservice.dto.DestinationRequestDto;
import com.importservice.dto.ExternalAgencyDto;
import com.importservice.dto.ExternalAgencyInfoDto;
import com.importservice.dto.ImportResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExternalAgencyImportService {

    private static final Logger logger = LoggerFactory.getLogger(ExternalAgencyImportService.class);

    @Value("${destination.api.url}")
    private String destinationApiUrl;

    @Value("${destination.api.token}")
    private String authToken;

    @Autowired
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public ExternalAgencyImportService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
    }

    public ImportResponseDto importExternalAgencies() {
        logger.info("Starting external agencies import process");
        
        List<String> errors = new ArrayList<>();
        int successfulImports = 0;
        int failedImports = 0;
        
        try {
            List<ExternalAgencyDto> agencies = readExternalAgenciesFromJson();
            logger.info("Found {} agencies to import", agencies.size());
            
            for (ExternalAgencyDto agency : agencies) {
                try {
                    boolean success = importSingleAgency(agency);
                    if (success) {
                        successfulImports++;
                        logger.info("Successfully imported agency: {}", agency.getLabelEn());
                    } else {
                        failedImports++;
                        errors.add("Failed to import agency: " + agency.getLabelEn());
                    }
                } catch (Exception e) {
                    failedImports++;
                    String errorMsg = "Error importing agency " + agency.getLabelEn() + ": " + e.getMessage();
                    errors.add(errorMsg);
                    logger.error(errorMsg, e);
                }
            }
            
            String status = failedImports == 0 ? "SUCCESS" : "PARTIAL_SUCCESS";
            String message = String.format("Import completed. Success: %d, Failed: %d", 
                                         successfulImports, failedImports);
            
            return new ImportResponseDto(status, message, agencies.size(), 
                                       successfulImports, failedImports, errors);
                                       
        } catch (IOException e) {
            logger.error("Failed to read external agencies JSON file", e);
            return new ImportResponseDto("ERROR", "Failed to read source data", 0, 0, 0, 
                Collections.singletonList("Failed to read JSON file: " + e.getMessage()));
        }
    }

    private List<ExternalAgencyDto> readExternalAgenciesFromJson() throws IOException {
        ClassPathResource resource = new ClassPathResource("externalAgencies.json");
        InputStream inputStream = resource.getInputStream();
        
        TypeReference<List<ExternalAgencyDto>> typeReference = new TypeReference<List<ExternalAgencyDto>>() {};
        return objectMapper.readValue(inputStream, typeReference);
    }

    private boolean importSingleAgency(ExternalAgencyDto agency) {
        try {
            DestinationRequestDto request = mapToDestinationRequest(agency);
            
            HttpHeaders headers = createHttpHeaders();
            HttpEntity<DestinationRequestDto> httpEntity = new HttpEntity<DestinationRequestDto>(request, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                destinationApiUrl, 
                HttpMethod.POST, 
                httpEntity, 
                String.class
            );
            
            return response.getStatusCode().is2xxSuccessful();
            
        } catch (Exception e) {
            logger.error("Failed to import agency: " + agency.getLabelEn(), e);
            return false;
        }
    }

    private DestinationRequestDto mapToDestinationRequest(ExternalAgencyDto agency) {
        DestinationRequestDto request = new DestinationRequestDto();
        
        ExternalAgencyInfoDto agencyInfo = new ExternalAgencyInfoDto(
            String.valueOf(agency.getId()),      // Convert id to string
            agency.getLabelEn(),                 // nameEn from label_en
            agency.getLabelAr(),                 // nameAr from label_ar
            "saudiArabia",                       // constant country value
            agency.getCategory()                 // type from category
        );
        
        request.setExternalAgencyInfo(agencyInfo);
        
        return request;
    }

    private HttpHeaders createHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        headers.set("Accept-Language", "en-US,en;q=0.9,ar;q=0.8");
        headers.set("Authorization", "Bearer " + authToken);
        headers.set("Connection", "keep-alive");
        headers.set("Content-Type", "application/json");
        return headers;
    }
}