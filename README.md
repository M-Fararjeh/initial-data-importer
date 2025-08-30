# Data Import Service

A Spring Boot application for importing external agency data from JSON files to a destination REST API.

## Features

- REST API endpoint to trigger external agency import
- JSON file reader for source data
- HTTP client integration with destination API
- Field mapping between source and destination formats
- Comprehensive error handling and logging

## API Endpoints

### Import External Agencies
- **POST** `/api/import/external-agencies`
- Triggers the import process for external agencies
- Returns import status with success/failure counts

### Health Check
- **GET** `/api/health`
- Returns service health status

## Running the Application

```bash
# Build the project
mvn clean compile

# Run the application
mvn spring-boot:run
```

The application will start on port 8080 with context path `/data-import`.

## Example Usage

```bash
# Trigger external agencies import
curl -X POST http://localhost:8080/data-import/api/import/external-agencies

# Check service health
curl http://localhost:8080/data-import/api/health
```

## Configuration

Update `application.properties` to configure:
- Destination API URL
- Authentication token
- Timeout settings

## Data Mapping

Source JSON fields → Destination API fields:
- `id` → `externalagency:agencyId`
- `label_en` → `externalagency:nameEn`
- `label_ar` → `externalagency:nameAr`
- `category` → `externalagency:typee`
- `"saudiArabia"` → `externalagency:country` (constant)