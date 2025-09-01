package com.importservice.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

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
        
        String result;
        switch (priorityId) {
            case 1:
                result = "N";  // Normal
                break;
            case 2:
                result = "H";  // Important
                break;
            case 3:
                result = "C";  // High
                break;
            default:
                result = "N"; // Default to Normal
                break;
        }
        
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
        
        String result;
        switch (secrecyId) {
            case 1:
                result = "Normal";
                break;
            case 2:
                result = "Top_Secret";
                break;
            case 3:
                result = "Secret";
                break;
            default:
                result = "Normal"; // Default to Normal
                break;
        }
        
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
        
        String result;
        String lowerActionGuid = actionGuid.toLowerCase().trim();
        
        if ("5ed900bc-a5f1-41cd-8f4f-0f05b7ef67c2".equals(lowerActionGuid)) {
            result = "ForInformation";
        } else if ("11f24280-d287-42ee-a72a-1c91274cfa4a".equals(lowerActionGuid)) {
            result = "ForAdvice";
        } else if ("1225fe27-5841-48e6-a47b-4cc9d9770fa6".equals(lowerActionGuid)) {
            result = "Toproceed";
        } else if ("a93aa474-7a3a-4d9a-8c15-6a3bcd706b51".equals(lowerActionGuid)) {
            result = "ToTakeNeededAction";
        } else if ("5379d40a-2726-4372-ab80-9564586e0458".equals(lowerActionGuid)) {
            result = "FYI";
        } else if ("2fb05e63-896a-4c9b-a99f-bca4deccc6ac".equals(lowerActionGuid)) {
            result = "ForSaving";
        } else {
            result = "ForAdvice"; // Default fallback
        }
        
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
        
        String result;
        String lowerCategoryGuid = categoryGuid.toLowerCase().trim();
        
        if ("01b1a89b-dff0-4040-878e-02c3fd4d7925".equals(lowerCategoryGuid)) {
            result = "AwardDecision";
        } else if ("06841b0a-f569-40c5-91d5-276c7f8c532b".equals(lowerCategoryGuid)) {
            result = "AccessPermit";
        } else if ("00a91759-734c-4be5-8a11-96e69dfae5a0".equals(lowerCategoryGuid)) {
            result = "WorkAssignment";
        } else if ("29f2cf7c-3a43-44cb-9ac7-b5570c760c60".equals(lowerCategoryGuid)) {
            result = "Promotion";
        } else if ("0bfa3e9c-682b-41c4-a275-ba395b52b0f7".equals(lowerCategoryGuid)) {
            result = "Circular";
        } else if ("26878084-7736-4935-88cb-d4312c2324f9".equals(lowerCategoryGuid)) {
            result = "Private";
        } else if ("87b623f9-eeeb-4829-8f8e-dc54d1fb242e".equals(lowerCategoryGuid)) {
            result = "PurchaseContract";
        } else if ("0d00ee38-b289-42a2-a3a9-e0d8421ac1c6".equals(lowerCategoryGuid)) {
            result = "General";
        } else if ("6dcc58c0-ebca-46b0-afb1-e96a4f1ebb7c".equals(lowerCategoryGuid)) {
            result = "PeriodicReport";
        } else {
            result = "General"; // Default fallback
        }
        
        logger.debug("Mapped categoryGuid '{}' to category '{}'", categoryGuid, result);
        return result;
    }
    
    /**
     * Determines the primary attachment from a list of attachments
     * 
     * @param attachments List of attachment data
     * @return Primary attachment or null if none found
     */
    public static Object determinePrimaryAttachment(List<Object> attachments) {
        if (attachments == null || attachments.isEmpty()) {
            logger.debug("No attachments provided");
            return null;
        }
        
        // Note: This method signature uses Object since SourceAttachmentDto.AttachmentData 
        // is not defined in the current codebase. Adjust the type as needed.
        logger.debug("Determining primary attachment from {} attachments", attachments.size());
        
        // Implementation would need to be adjusted based on actual attachment DTO structure
        return attachments.stream()
                .filter(attachment -> attachment != null)
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Maps file type to standardized attachment file type
     * 
     * @param fileType The file type from source system
     * @return Standardized file type for destination system
     */
    public static String mapAttachmentFileType(String fileType) {
        if (fileType == null) {
            logger.debug("Null fileType provided, returning 'UNKNOWN'");
            return "UNKNOWN";
        }
        
        String result;
        switch (fileType) {
            case "Attachment":
                result = "ATTACHMENT";
                break;
            case "TemplateOfficeFile":
                result = "OFFICE_TEMPLATE";
                break;
            case "TemplatePdfFile":
                result = "PDF_TEMPLATE";
                break;
            default:
                result = "OTHER";
                break;
        }
        
        logger.debug("Mapped fileType '{}' to '{}'", fileType, result);
        return result;
    }
    
    /**
     * Extracts file extension from filename
     * 
     * @param fileName The filename to extract extension from
     * @return File extension in lowercase, or empty string if none found
     */
    public static String extractFileExtension(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            logger.debug("Null or empty fileName provided");
            return "";
        }
        
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            String extension = fileName.substring(lastDotIndex + 1).toLowerCase();
            logger.debug("Extracted extension '{}' from fileName '{}'", extension, fileName);
            return extension;
        }
        
        logger.debug("No extension found in fileName '{}'", fileName);
        return "";
    }
    
    /**
     * Cleans filename by removing GUID prefix if present
     * 
     * @param fileName The filename to clean
     * @return Cleaned filename without GUID prefix
     */
    public static String cleanFileName(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            logger.debug("Null or empty fileName provided, returning default");
            return "unnamed_file";
        }
        
        // Remove GUID prefix if present (pattern: guid_filename)
        String cleaned = fileName;
        if (fileName.contains("_") && fileName.length() > 36) {
            String[] parts = fileName.split("_", 2);
            if (parts.length > 1 && parts[0].length() == 36) {
                cleaned = parts[1];
                logger.debug("Removed GUID prefix from fileName '{}', result: '{}'", fileName, cleaned);
            }
        }
        
        return cleaned;
    }
    
    /**
     * Maps attachment classification based on file type and name
     * 
     * @param fileType The file type from source system
     * @param fileName The filename to analyze
     * @return Classification for the attachment
     */
    public static String mapAttachmentClassification(String fileType, String fileName) {
        if (fileName == null) {
            logger.debug("Null fileName provided, returning default 'Document'");
            return "Document";
        }
        
        String extension = extractFileExtension(fileName).toLowerCase();
        String result;
        
        if ("pdf".equals(extension)) {
            result = "Document";
        } else if ("jpg".equals(extension) || "jpeg".equals(extension) || "png".equals(extension) || 
                   "gif".equals(extension) || "bmp".equals(extension)) {
            result = "Image";
        } else if ("doc".equals(extension) || "docx".equals(extension) || 
                   "xls".equals(extension) || "xlsx".equals(extension) || 
                   "ppt".equals(extension) || "pptx".equals(extension) || 
                   "txt".equals(extension)) {
            result = "Document";
        } else if ("zip".equals(extension) || "rar".equals(extension) || "7z".equals(extension)) {
            result = "Archive";
        } else {
            result = "Document";
        }
        
        logger.debug("Mapped fileName '{}' with extension '{}' to classification '{}'", fileName, extension, result);
        return result;
    }
    
    /**
     * Maps attachment category based on primary status and file type
     * 
     * @param isPrimary Whether this is the primary attachment
     * @param fileType The file type from source system
     * @return Category for the attachment
     */
    public static String mapAttachmentCategory(boolean isPrimary, String fileType) {
        if (isPrimary) {
            logger.debug("Primary attachment detected, returning 'MAIN'");
            return "MAIN";
        }
        
        String result;
        switch (fileType) {
            case "Attachment":
                result = "ATTACHMENT";
                break;
            case "TemplateOfficeFile":
                result = "TEMPLATE";
                break;
            case "TemplatePdfFile":
                result = "TEMPLATE";
                break;
            default:
                result = "ATTACHMENT";
                break;
        }
        
        logger.debug("Mapped non-primary fileType '{}' to category '{}'", fileType, result);
        return result;
    }
    
    /**
     * Maps attachment type based on filename extension
     * 
     * @param fileName The filename to analyze
     * @return Attachment type based on file extension
     */
    public static String mapAttachmentType(String fileName) {
        if (fileName == null) {
            logger.debug("Null fileName provided, returning 'OTHER'");
            return "OTHER";
        }
        
        String extension = extractFileExtension(fileName).toLowerCase();
        String result;
        
        if ("pdf".equals(extension)) {
            result = "PDF";
        } else if ("jpg".equals(extension) || "jpeg".equals(extension) || "png".equals(extension) || 
                   "gif".equals(extension) || "bmp".equals(extension)) {
            result = "IMAGE";
        } else if ("doc".equals(extension) || "docx".equals(extension)) {
            result = "WORD";
        } else if ("xls".equals(extension) || "xlsx".equals(extension)) {
            result = "EXCEL";
        } else if ("ppt".equals(extension) || "pptx".equals(extension)) {
            result = "POWERPOINT";
        } else if ("txt".equals(extension)) {
            result = "TEXT";
        } else if ("zip".equals(extension) || "rar".equals(extension) || "7z".equals(extension)) {
            result = "ARCHIVE";
        } else {
            result = "OTHER";
        }
        
        logger.debug("Mapped fileName '{}' with extension '{}' to type '{}'", fileName, extension, result);
        return result;
    }
    
    /**
     * Gets MIME type based on filename extension
     * 
     * @param fileName The filename to analyze
     * @return MIME type for the file
     */
    public static String getMimeType(String fileName) {
        if (fileName == null) {
            logger.debug("Null fileName provided, returning default MIME type");
            return "application/octet-stream";
        }
        
        String extension = extractFileExtension(fileName).toLowerCase();
        String result;
        
        if ("pdf".equals(extension)) {
            result = "application/pdf";
        } else if ("jpg".equals(extension) || "jpeg".equals(extension)) {
            result = "image/jpeg";
        } else if ("png".equals(extension)) {
            result = "image/png";
        } else if ("gif".equals(extension)) {
            result = "image/gif";
        } else if ("bmp".equals(extension)) {
            result = "image/bmp";
        } else if ("doc".equals(extension)) {
            result = "application/msword";
        } else if ("docx".equals(extension)) {
            result = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        } else if ("xls".equals(extension)) {
            result = "application/vnd.ms-excel";
        } else if ("xlsx".equals(extension)) {
            result = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        } else if ("ppt".equals(extension)) {
            result = "application/vnd.ms-powerpoint";
        } else if ("pptx".equals(extension)) {
            result = "application/vnd.openxmlformats-officedocument.presentationml.presentation";
        } else if ("txt".equals(extension)) {
            result = "text/plain";
        } else if ("zip".equals(extension)) {
            result = "application/zip";
        } else if ("rar".equals(extension)) {
            result = "application/x-rar-compressed";
        } else if ("7z".equals(extension)) {
            result = "application/x-7z-compressed";
        } else {
            result = "application/octet-stream";
        }
        
        logger.debug("Mapped fileName '{}' with extension '{}' to MIME type '{}'", fileName, extension, result);
        return result;
    }
    
    /**
     * Removes HTML tags from text content
     * 
     * @param text The text that may contain HTML tags
     * @return Clean text without HTML tags
     */
    public static String cleanHtmlTags(String text) {
        if (text == null || text.trim().isEmpty()) {
            logger.debug("Null or empty text provided for HTML cleaning");
            return "";
        }
        
        // Remove HTML tags using regex
        String cleaned = text.replaceAll("<[^>]*>", "");
        
        // Remove extra whitespace and normalize
        cleaned = cleaned.replaceAll("\\s+", " ").trim();
        
        logger.debug("Cleaned HTML tags from text. Original length: {}, Cleaned length: {}", 
                   text.length(), cleaned.length());
        
        return cleaned;
    }
}