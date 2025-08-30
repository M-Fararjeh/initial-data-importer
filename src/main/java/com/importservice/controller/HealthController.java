package com.importservice.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
@Api(value = "Health Controller", description = "Health check operations")
public class HealthController {

    @GetMapping
    @ApiOperation(value = "Health Check", notes = "Returns the health status of the service")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Service is healthy")
    })
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "data-import-service");
        response.put("version", "1.0.0");
        return ResponseEntity.ok(response);
    }
}