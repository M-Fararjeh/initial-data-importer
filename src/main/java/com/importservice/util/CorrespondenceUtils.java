package com.importservice.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for mapping correspondence data during migration
 */
public class CorrespondenceUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(CorrespondenceUtils.class);
    
    /**
     * Maps priority ID to priority code
     * 
     * @param priorityId The priority ID from source system
     * @return Priority code for destination system (N=Normal, H=Important, C=High)
     */
    public static String mapPriority(Integer priorityId) {
        if (priorityId == null) {
            logger.debug("Null priorityId provided, returning default 'N'");
            return "N";
        }
        
        String result = switch (priorityId) {
            case 1 -> "N";  // Normal
            case 2 -> "H";  // Important
            case 3 -> "C";  // High
            default -> "N"; // Default to Normal
        };
        
        logger.debug("Mapped priorityId {} to priority code '{}'", priorityId, result);
        return result;
    }
    
    /**
     * Maps secrecy ID to secrecy level
     * 
     * @param secrecyId The secrecy ID from source system
     * @return Secrecy level for destination system
     */
    public static String mapSecrecyLevel(Integer secrecyId) {
        if (secrecyId == null) {
            logger.debug("Null secrecyId provided, returning default 'Normal'");
            return "Normal";
        }
        
        String result = switch (secrecyId) {
            case 1 -> "Normal";
            case 2 -> "Top_Secret";
            case 3 -> "Secret";
            default -> "Normal"; // Default to Normal
        };
        
        logger.debug("Mapped secrecyId {} to secrecy level '{}'", secrecyId, result);
        return result;
    }
    
    /**
     * Maps need reply status to boolean
     * 
     * @param needReplyStatus The need reply status from source system
     * @return Boolean indicating if reply is required
     */
    public static Boolean mapRequireReply(String needReplyStatus) {
        if (needReplyStatus == null || needReplyStatus.trim().isEmpty()) {
            logger.debug("Null or empty needReplyStatus provided, returning false");
            return false;
        }
        
        boolean result = "TRUE".equalsIgnoreCase(needReplyStatus.trim());
        logger.debug("Mapped needReplyStatus '{}' to requireReply {}", needReplyStatus, result);
        return result;
    }
    
    /**
     * Maps action GUID to action type
     * 
     * @param actionGuid The action GUID from source system
     * @return Action type for destination system
     */
    public static String mapAction(String actionGuid) {
        if (actionGuid == null || actionGuid.trim().isEmpty()) {
            logger.debug("Null or empty actionGuid provided, returning default 'ForAdvice'");
            return "ForAdvice"; // Default action
        }
        
        String result = switch (actionGuid.toLowerCase().trim()) {
            case "5ed900bc-a5f1-41cd-8f4f-0f05b7ef67c2" -> "ForInformation";
            case "11f24280-d287-42ee-a72a-1c91274cfa4a" -> "ForAdvice";
            case "1225fe27-5841-48e6-a47b-4cc9d9770fa6" -> "Toproceed";
            case "a93aa474-7a3a-4d9a-8c15-6a3bcd706b51" -> "ToTakeNeededAction";
            case "5379d40a-2726-4372-ab80-9564586e0458" -> "FYI";
            case "2fb05e63-896a-4c9b-a99f-bca4deccc6ac" -> "ForSaving";
            default -> "ForAdvice"; // Default fallback
        };
        
        logger.debug("Mapped actionGuid '{}' to action '{}'", actionGuid, result);
        return result;
    }
    
    /**
     * Maps category GUID to category type
     * 
     * @param categoryGuid The category GUID from source system
     * @return Category type for destination system
     */
    public static String mapCategory(String categoryGuid) {
        if (categoryGuid == null || categoryGuid.trim().isEmpty()) {
            logger.debug("Null or empty categoryGuid provided, returning default 'General'");
            return "General"; // Default category
        }
        
        String result = switch (categoryGuid.toLowerCase().trim()) {
            case "01b1a89b-dff0-4040-878e-02c3fd4d7925" -> "AwardDecision";
            case "06841b0a-f569-40c5-91d5-276c7f8c532b" -> "AccessPermit";
            case "00a91759-734c-4be5-8a11-96e69dfae5a0" -> "WorkAssignment";
            case "29f2cf7c-3a43-44cb-9ac7-b5570c760c60" -> "Promotion";
            case "0bfa3e9c-682b-41c4-a275-ba395b52b0f7" -> "Circular";
            case "26878084-7736-4935-88cb-d4312c2324f9" -> "Private";
            case "87b623f9-eeeb-4829-8f8e-dc54d1fb242e" -> "PurchaseContract";
            case "0d00ee38-b289-42a2-a3a9-e0d8421ac1c6" -> "General";
            case "6dcc58c0-ebca-46b0-afb1-e96a4f1ebb7c" -> "PeriodicReport";
            default -> "General"; // Default fallback
        };
        
        logger.debug("Mapped categoryGuid '{}' to category '{}'", categoryGuid, result);
        return result;
    }
}