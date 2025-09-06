// Phase 2: Creation
    public ImportResponseDto executeCreationPhase() {
        logger.info("Starting Phase 2: Creation with individual transactions per correspondence");
        
        List<String> errors = new ArrayList<>();
        int successfulImports = 0;
        int failedImports = 0;
        
        try {
            // Get all correspondences that need creation processing
            List<IncomingCorrespondenceMigration> migrations = phaseService.getMigrationsForPhase("CREATION");
            logger.info("Found {} correspondences in CREATION phase", migrations.size());
            
            if (migrations.isEmpty()) {
                return phaseService.createResponse("SUCCESS", "No correspondences found in CREATION phase", 
                                                 0, 0, 0, new ArrayList<>());
            }
            
            // Process each correspondence in its own separate transaction
            for (int i = 0; i < migrations.size(); i++) {
                IncomingCorrespondenceMigration migration = migrations.get(i);
                String correspondenceGuid = migration.getCorrespondenceGuid();
                
                try {
                    logger.info("Processing correspondence: {} ({}/{})", 
                               correspondenceGuid, i + 1, migrations.size());
                    
                    // Process in completely separate transaction with immediate commit
                    boolean success = creationPhaseService.processCorrespondenceCreationInNewTransaction(correspondenceGuid);
                    
                    if (success) {
                        successfulImports++;
                        logger.info("✅ Successfully completed creation for correspondence: {}", correspondenceGuid);
                    } else {
                        failedImports++;
                        logger.warn("❌ Failed to complete creation for correspondence: {}", correspondenceGuid);
                    }
                    
                    // Add delay between correspondences to reduce system load and lock contention
                    if (i < migrations.size() - 1) {
                        try {
                            Thread.sleep(300); // 300ms delay between correspondences
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            logger.warn("Thread interrupted during processing delay");
                            break;
                        }
                    }
                    
                    // Log progress every 10 correspondences
                    if ((i + 1) % 10 == 0) {
                        logger.info("Progress: {}/{} correspondences processed (Success: {}, Failed: {})", 
                                   i + 1, migrations.size(), successfulImports, failedImports);
                    }
                    
                } catch (Exception e) {
                    failedImports++;
                    String errorMsg = "Error processing correspondence " + correspondenceGuid + ": " + e.getMessage();
                    errors.add(errorMsg);
                    logger.error("❌ " + errorMsg, e);
                    
                    // Continue processing other correspondences even if one fails
                    continue;
                }
            }
            
            String status = phaseService.determineFinalStatus(successfulImports, failedImports);
            String message = String.format("Phase 2 completed. Created: %d, Failed: %d (Each correspondence processed in separate transaction)", 
                                         successfulImports, failedImports);
            
            logger.info("Phase 2 Creation completed - Total: {}, Success: {}, Failed: {}", 
                       migrations.size(), successfulImports, failedImports);
            
            return phaseService.createResponse(status, message, migrations.size(), 
                                             successfulImports, failedImports, errors);
            
        } catch (Exception e) {
            logger.error("Error in Phase 2: Creation orchestration", e);
            return phaseService.createResponse("ERROR", "Phase 2 failed: " + e.getMessage(), 
                                             0, 0, 0, Arrays.asList(e.getMessage()));
        }
    }