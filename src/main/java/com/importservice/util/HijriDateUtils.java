package com.importservice.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class HijriDateUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(HijriDateUtils.class);
    
    /**
     * Converts Gregorian date to Hijri date string
     * This is a simplified conversion - in production, use a proper Hijri calendar library
     * 
     * @param gregorianDate The Gregorian date to convert
     * @return Hijri date string in format YYYY-MM-DD
     */
    public static String convertToHijri(LocalDateTime gregorianDate) {
        if (gregorianDate == null) {
            logger.debug("Null gregorianDate provided, returning current Hijri date");
            return getCurrentHijriDate();
        }
        
        try {
            // Simplified conversion: subtract approximately 578-580 years
            // This is an approximation - use proper Hijri calendar library in production
            int gregorianYear = gregorianDate.getYear();
            int hijriYear = gregorianYear - 579; // Approximate conversion
            
            int month = gregorianDate.getMonthValue();
            int day = gregorianDate.getDayOfMonth();
            
            // Adjust for Hijri calendar differences (simplified)
            if (month > 6) {
                hijriYear++;
            }
            
            String hijriDate = String.format("%04d-%02d-%02d", hijriYear, month, day);
            logger.debug("Converted Gregorian date {} to Hijri date {}", gregorianDate, hijriDate);
            return hijriDate;
            
        } catch (Exception e) {
            logger.error("Error converting Gregorian date to Hijri: {}", gregorianDate, e);
            return getCurrentHijriDate();
        }
    }
    
    /**
     * Gets the current Hijri date as a string
     * 
     * @return Current Hijri date string
     */
    public static String getCurrentHijriDate() {
        return convertToHijri(LocalDateTime.now());
    }
    
    /**
     * Formats a LocalDateTime to ISO string format for API requests
     * 
     * @param dateTime The date time to format
     * @return ISO formatted date string
     */
    public static String formatToIsoString(LocalDateTime dateTime) {
        if (dateTime == null) {
            logger.debug("Null dateTime provided, returning current time");
            dateTime = LocalDateTime.now();
        }
        
        String isoString = dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "Z";
        logger.debug("Formatted dateTime {} to ISO string {}", dateTime, isoString);
        return isoString;
    }
    
    /**
     * Adds years to a date
     * 
     * @param dateTime The base date time
     * @param years Number of years to add
     * @return New date time with added years
     */
    public static LocalDateTime addYears(LocalDateTime dateTime, int years) {
        if (dateTime == null) {
            logger.debug("Null dateTime provided, using current time");
            dateTime = LocalDateTime.now();
        }
        
        LocalDateTime result = dateTime.plusYears(years);
        logger.debug("Added {} years to {} = {}", years, dateTime, result);
        return result;
    }
}