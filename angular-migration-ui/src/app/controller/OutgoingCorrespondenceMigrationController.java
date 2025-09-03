@@ .. @@
     }
     
+    @GetMapping("/assignment/details")
+    @Operation(summary = "Get Outgoing Assignment Phase Details", 
+               description = "Returns detailed information about outgoing assignment phase migrations")
+    @ApiResponses(value = {
+        @ApiResponse(responseCode = "200", description = "Details retrieved successfully")
+    })
+    @Transactional(readOnly = true, timeout = 60)
+    public ResponseEntity<Map<String, Object>> getAssignmentDetails(
+            @RequestParam(defaultValue = "0") int page,
+            @RequestParam(defaultValue = "20") int size,
+            @RequestParam(defaultValue = "all") String status,
+            @RequestParam(defaultValue = "") String search) {
+        logger.info("Received request for outgoing assignment phase details - page: {}, size: {}, status: {}, search: '{}'", 
+                   page, size, status, search);
+        
+        try {
+            Map<String, Object> assignments = migrationService.getAssignmentMigrations(page, size, status, search);
+            return ResponseEntity.ok(assignments);
+        } catch (Exception e) {
+            logger.error("Error getting outgoing assignment details", e);
+            Map<String, Object> errorResponse = new HashMap<>();
+            errorResponse.put("content", new ArrayList<>());
+            errorResponse.put("totalElements", 0L);
+            errorResponse.put("totalPages", 0);
+            errorResponse.put("currentPage", page);
+            errorResponse.put("pageSize", size);
+            errorResponse.put("hasNext", false);
+            errorResponse.put("hasPrevious", false);
+            errorResponse.put("error", e.getMessage());
+            return ResponseEntity.status(500).body(errorResponse);
+        }
+    }
+    
+    @GetMapping("/approval/details")
+    @Operation(summary = "Get Outgoing Approval Phase Details", 
+               description = "Returns detailed information about outgoing approval phase migrations")
+    @ApiResponses(value = {
+        @ApiResponse(responseCode = "200", description = "Details retrieved successfully")
+    })
+    @Transactional(readOnly = true, timeout = 60)
+    public ResponseEntity<Map<String, Object>> getApprovalDetails(
+            @RequestParam(defaultValue = "0") int page,
+            @RequestParam(defaultValue = "20") int size,
+            @RequestParam(defaultValue = "all") String status,
+            @RequestParam(defaultValue = "all") String step,
+            @RequestParam(defaultValue = "") String search) {
+        logger.info("Received request for outgoing approval phase details - page: {}, size: {}, status: {}, step: {}, search: '{}'", 
+                   page, size, status, step, search);
+        
+        try {
+            // For now return empty data - implement when approval service methods are ready
+            Map<String, Object> approvals = new HashMap<>();
+            approvals.put("content", new ArrayList<>());
+            approvals.put("totalElements", 0L);
+            approvals.put("totalPages", 0);
+            approvals.put("currentPage", page);
+            approvals.put("pageSize", size);
+            approvals.put("hasNext", false);
+            approvals.put("hasPrevious", false);
+            return ResponseEntity.ok(approvals);
+        } catch (Exception e) {
+            logger.error("Error getting outgoing approval details", e);
+            Map<String, Object> errorResponse = new HashMap<>();
+            errorResponse.put("content", new ArrayList<>());
+            errorResponse.put("totalElements", 0L);
+            errorResponse.put("totalPages", 0);
+            errorResponse.put("currentPage", page);
+            errorResponse.put("pageSize", size);
+            errorResponse.put("hasNext", false);
+            errorResponse.put("hasPrevious", false);
+            errorResponse.put("error", e.getMessage());
+            return ResponseEntity.status(500).body(errorResponse);
+        }
+    }
+    
+    @GetMapping("/business-log/details")
+    @Operation(summary = "Get Outgoing Business Log Phase Details", 
+               description = "Returns detailed information about outgoing business log phase migrations")
+    @ApiResponses(value = {
+        @ApiResponse(responseCode = "200", description = "Details retrieved successfully")
+    })
+    @Transactional(readOnly = true, timeout = 60)
+    public ResponseEntity<Map<String, Object>> getBusinessLogDetails(
+            @RequestParam(defaultValue = "0") int page,
+            @RequestParam(defaultValue = "20") int size,
+            @RequestParam(defaultValue = "all") String status,
+            @RequestParam(defaultValue = "") String search) {
+        logger.info("Received request for outgoing business log phase details - page: {}, size: {}, status: {}, search: '{}'", 
+                   page, size, status, search);
+        
+        try {
+            Map<String, Object> businessLogs = migrationService.getBusinessLogMigrations(page, size, status, search);
+            return ResponseEntity.ok(businessLogs);
+        } catch (Exception e) {
+            logger.error("Error getting outgoing business log details", e);
+            Map<String, Object> errorResponse = new HashMap<>();
+            errorResponse.put("content", new ArrayList<>());
+            errorResponse.put("totalElements", 0L);
+            errorResponse.put("totalPages", 0);
+            errorResponse.put("currentPage", page);
+            errorResponse.put("pageSize", size);
+            errorResponse.put("hasNext", false);
+            errorResponse.put("hasPrevious", false);
+            errorResponse.put("error", e.getMessage());
+            return ResponseEntity.status(500).body(errorResponse);
+        }
+    }
+    
+    @GetMapping("/comment/details")
+    @Operation(summary = "Get Outgoing Comment Phase Details", 
+               description = "Returns detailed information about outgoing comment phase migrations")
+    @ApiResponses(value = {
+        @ApiResponse(responseCode = "200", description = "Details retrieved successfully")
+    })
+    @Transactional(readOnly = true, timeout = 60)
+    public ResponseEntity<Map<String, Object>> getCommentDetails(
+            @RequestParam(defaultValue = "0") int page,
+            @RequestParam(defaultValue = "20") int size,
+            @RequestParam(defaultValue = "all") String status,
+            @RequestParam(defaultValue = "all") String commentType,
+            @RequestParam(defaultValue = "") String search) {
+        logger.info("Received request for outgoing comment phase details - page: {}, size: {}, status: {}, commentType: {}, search: '{}'", 
+                   page, size, status, commentType, search);
+        
+        try {
+            Map<String, Object> comments = migrationService.getCommentMigrations(page, size, status, commentType, search);
+            return ResponseEntity.ok(comments);
+        } catch (Exception e) {
+            logger.error("Error getting outgoing comment details", e);
+            Map<String, Object> errorResponse = new HashMap<>();
+            errorResponse.put("content", new ArrayList<>());
+            errorResponse.put("totalElements", 0L);
+            errorResponse.put("totalPages", 0);
+            errorResponse.put("currentPage", page);
+            errorResponse.put("pageSize", size);
+            errorResponse.put("hasNext", false);
+            errorResponse.put("hasPrevious", false);
+            errorResponse.put("error", e.getMessage());
+            return ResponseEntity.status(500).body(errorResponse);
+        }
+    }
+    
+    @GetMapping("/closing/details")
+    @Operation(summary = "Get Outgoing Closing Phase Details", 
+               description = "Returns detailed information about outgoing closing phase migrations")
+    @ApiResponses(value = {
+        @ApiResponse(responseCode = "200", description = "Details retrieved successfully")
+    })
+    @Transactional(readOnly = true, timeout = 60)
+    public ResponseEntity<Map<String, Object>> getClosingDetails(
+            @RequestParam(defaultValue = "0") int page,
+            @RequestParam(defaultValue = "20") int size,
+            @RequestParam(defaultValue = "all") String status,
+            @RequestParam(defaultValue = "all") String needToClose,
+            @RequestParam(defaultValue = "") String search) {
+        logger.info("Received request for outgoing closing phase details - page: {}, size: {}, status: {}, needToClose: {}, search: '{}'", 
+                   page, size, status, needToClose, search);
+        
+        try {
+            Map<String, Object> closings = migrationService.getClosingMigrations(page, size, status, needToClose, search);
+            return ResponseEntity.ok(closings);
+        } catch (Exception e) {
+            logger.error("Error getting outgoing closing details", e);
+            Map<String, Object> errorResponse = new HashMap<>();
+            errorResponse.put("content", new ArrayList<>());
+            errorResponse.put("totalElements", 0L);
+            errorResponse.put("totalPages", 0);
+            errorResponse.put("currentPage", page);
+            errorResponse.put("pageSize", size);
+            errorResponse.put("hasNext", false);
+            errorResponse.put("hasPrevious", false);
+            errorResponse.put("needToCloseCount", 0L);
+            errorResponse.put("error", e.getMessage());
+            return ResponseEntity.status(500).body(errorResponse);
+        }
+    }
+    
     private ResponseEntity<ImportResponseDto> getResponseEntity(ImportResponseDto response) {