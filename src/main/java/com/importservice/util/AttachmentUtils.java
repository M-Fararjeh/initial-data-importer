package com.importservice.util;

import com.importservice.entity.CorrespondenceAttachment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class AttachmentUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(AttachmentUtils.class);
    
    /**
     * Finds the primary attachment from a list of attachments
     * Primary attachment criteria:
     * 1. FileType = "Attachment"
     * 2. Name contains "pdf"
     * 3. Caption = "مرفق"
     * 
     * @param attachments List of correspondence attachments
     * @return Primary attachment or null if not found
     */
    public static CorrespondenceAttachment findPrimaryAttachment(List<CorrespondenceAttachment> attachments) {
        if (attachments == null || attachments.isEmpty()) {
            logger.debug("No attachments provided");
            return null;
        }
        
        logger.debug("Searching for primary attachment among {} attachments", attachments.size());
        
        Optional<CorrespondenceAttachment> primary = attachments.stream()
            .filter(attachment -> attachment != null)
            .filter(attachment -> "Attachment".equals(attachment.getFileType()))
            .filter(attachment -> attachment.getName() != null && 
                                attachment.getName().toLowerCase().contains("pdf"))
            .filter(attachment -> "مرفق".equals(attachment.getCaption()))
            .findFirst();
        
        if (primary.isPresent()) {
            logger.debug("Found primary attachment: {}", primary.get().getName());
            return primary.get();
        }
        
        // Fallback: look for any attachment with FileType = "Attachment"
        Optional<CorrespondenceAttachment> fallback = attachments.stream()
            .filter(attachment -> attachment != null)
            .filter(attachment -> "Attachment".equals(attachment.getFileType()))
            .findFirst();
        
        if (fallback.isPresent()) {
            logger.debug("Found fallback primary attachment: {}", fallback.get().getName());
            return fallback.get();
        }
        
        logger.debug("No primary attachment found");
        return null;
    }
    
    /**
     * Filters out the primary attachment from the list
     * 
     * @param attachments List of all attachments
     * @param primaryAttachment The primary attachment to exclude
     * @return List of non-primary attachments
     */
    public static List<CorrespondenceAttachment> getNonPrimaryAttachments(
            List<CorrespondenceAttachment> attachments, 
            CorrespondenceAttachment primaryAttachment) {
        
        if (attachments == null || attachments.isEmpty()) {
            logger.debug("No attachments provided");
            return new ArrayList<>();
        }
        
        if (primaryAttachment == null) {
            logger.debug("No primary attachment specified, returning all attachments");
            return new ArrayList<>(attachments);
        }
        
        List<CorrespondenceAttachment> nonPrimary = attachments.stream()
            .filter(attachment -> attachment != null)
            .filter(attachment -> !attachment.getGuid().equals(primaryAttachment.getGuid()))
            .collect(Collectors.toList());
        
        logger.debug("Found {} non-primary attachments", nonPrimary.size());
        return nonPrimary;
    }
    
    /**
     * Generates a barcode for an attachment
     * 
     * @param attachmentGuid The attachment GUID
     * @return Generated barcode
     */
    public static String generateAttachmentBarcode(String attachmentGuid) {
        if (attachmentGuid == null || attachmentGuid.trim().isEmpty()) {
            logger.debug("Null or empty attachmentGuid provided, generating random barcode");
            return UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        }
        
        // Use the first 12 characters of the GUID (removing hyphens)
        String barcode = attachmentGuid.replace("-", "").substring(0, 12);
        logger.debug("Generated barcode '{}' for attachmentGuid '{}'", barcode, attachmentGuid);
        return barcode;
    }
    
    /**
     * Validates if attachment has required data for upload
     * 
     * @param attachment The attachment to validate
     * @return true if attachment is valid for upload
     */
    public static boolean isValidForUpload(CorrespondenceAttachment attachment) {
        if (attachment == null) {
            logger.debug("Null attachment provided");
            return false;
        }
        
        if (attachment.getFileData() == null || attachment.getFileData().trim().isEmpty()) {
            logger.debug("Attachment {} has no file data", attachment.getGuid());
            return false;
        }
        
        if (attachment.getName() == null || attachment.getName().trim().isEmpty()) {
            logger.debug("Attachment {} has no name", attachment.getGuid());
            return false;
        }
        
        logger.debug("Attachment {} is valid for upload", attachment.getGuid());
        return true;
    }
    
    /**
     * Gets the clean file name for upload
     * 
     * @param originalName The original file name
     * @param isPrimary Whether this is the primary attachment
     * @return Clean file name suitable for upload
     */
    public static String getFileNameForUpload(String originalName, boolean isPrimary) {
        if (originalName == null || originalName.trim().isEmpty()) {
            return isPrimary ? "primary_document.pdf" : "attachment.pdf";
        }
        
        String cleanName = CorrespondenceUtils.cleanFileName(originalName);
        logger.debug("Cleaned file name from '{}' to '{}'", originalName, cleanName);
        return cleanName;
    }
    
    /**
     * Determines if file data should be uploaded based on size and configuration
     * 
     * @param fileData The base64 file data
     * @param fileName The file name
     * @param isPrimary Whether this is the primary attachment
     * @return The file data to upload or null if should be skipped
     */
    public static String getFileDataForUpload(String fileData, String fileName, boolean isPrimary) {
        if (fileData == null || fileData.trim().isEmpty()) {
            logger.debug("No file data provided for {}", fileName);
            return null;
        }
        
        // Check file size (base64 encoded size is roughly 4/3 of original)
        long estimatedSize = (fileData.length() * 3) / 4;
        long maxSize = 200_000_000; // 200MB
        
        if (estimatedSize > maxSize) {
            logger.warn("File {} is too large ({} bytes), skipping upload", fileName, estimatedSize);
            return null;
        }
        
        logger.debug("File {} is valid for upload ({} bytes)", fileName, estimatedSize);
        return fileData;
    }
}