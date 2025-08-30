package com.importservice.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;

public class CustomDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

    private static final List<DateTimeFormatter> FORMATTERS = Arrays.asList(
        DateTimeFormatter.ofPattern("M/d/yyyy h:mm:ss a"),     // 6/14/2023 1:20:22 PM
        DateTimeFormatter.ofPattern("MM/dd/yyyy h:mm:ss a"),   // 06/14/2023 1:20:22 PM
        DateTimeFormatter.ofPattern("M/d/yyyy HH:mm:ss"),      // 6/14/2023 13:20:22
        DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss"),    // 06/14/2023 13:20:22
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),    // 2023-06-14 13:20:22
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),  // 2023-06-14T13:20:22
        DateTimeFormatter.ISO_LOCAL_DATE_TIME                  // ISO format fallback
    );

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String dateString = p.getValueAsString();
        
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }
        
        dateString = dateString.trim();
        
        for (DateTimeFormatter formatter : FORMATTERS) {
            try {
                return LocalDateTime.parse(dateString, formatter);
            } catch (DateTimeParseException e) {
                // Continue to next formatter
            }
        }
        
        throw new IOException("Unable to parse date: " + dateString + 
            ". Supported formats: M/d/yyyy h:mm:ss a, MM/dd/yyyy h:mm:ss a, yyyy-MM-dd HH:mm:ss, ISO formats");
    }
}