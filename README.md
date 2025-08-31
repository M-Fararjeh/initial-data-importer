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

### Using Docker Compose (Recommended)

```bash
# Start MySQL and the application
docker-compose up -d

# View logs
docker-compose logs -f

# Stop services
docker-compose down

# Stop and remove volumes (WARNING: This will delete all data)
docker-compose down -v
```

### Local Development

```bash
# Build the project
mvn clean compile

# Run the application
mvn spring-boot:run
```

The application will start on port 8080 with context path `/data-import`.

### Database Access

When using Docker Compose:
- **MySQL Host**: localhost:3306
- **Database**: data_import_db
- **Username**: import_user
- **Password**: import_password123
- **Root Password**: rootpassword123

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

## Migration Process

The Data Import Service includes a comprehensive migration system for transferring correspondence data from legacy systems to the new platform. The migration process is divided into sequential phases to ensure data integrity and proper error handling.

### Migration Architecture

The migration system consists of:
- **Spring Boot Backend**: Handles data processing and API integration
- **Angular Frontend**: Provides a user-friendly interface for monitoring and controlling migration phases
- **Phase-based Processing**: Sequential execution ensures dependencies are met
- **Error Recovery**: Automatic retry mechanisms for failed operations

### Migration Phases

#### Phase 1: Prepare Data
**Purpose**: Select and prepare incoming correspondences for migration

**Process**:
1. Queries database for incoming correspondences (`CorrespondenceTypeId = 2`)
2. Filters out deleted, draft, and cancelled correspondences
3. Creates migration tracking records in `incoming_correspondence_migrations` table
4. Sets initial phase status and determines if correspondence needs to be closed

**API Endpoint**: `POST /api/incoming-migration/prepare-data`

**Success Criteria**: Migration records created for all valid incoming correspondences

#### Phase 2: Creation
**Purpose**: Create correspondences in destination system with attachments

**Process**:
1. **Get Details**: Retrieves correspondence data and validates it
2. **Get Attachments**: Finds all attachments for the correspondence
3. **Upload Main Attachment**: 
   - Identifies primary attachment (FileType="Attachment", contains "pdf", Caption="مرفق")
   - Creates upload batch in destination system
   - Uploads primary attachment file data
4. **Create Correspondence**: 
   - Maps source fields to destination format
   - Converts dates to Hijri calendar
   - Maps priority, secrecy, and category codes
   - Creates correspondence record in destination system
5. **Upload Other Attachments**: Processes remaining attachments individually

**API Endpoint**: `POST /api/incoming-migration/creation`

**Field Mappings**:
- Priority: `1=N (Normal), 2=H (Important), 3=C (High)`
- Secrecy: `1=Normal, 2=Top_Secret, 3=Secret`
- Category: Maps classification GUID to category type
- Agency: Maps coming from GUID to agency code
- Department: Maps department GUID to department code

#### Phase 3: Assignment
**Purpose**: Assign correspondences to users and departments

**Process**:
1. Determines target users based on department mappings
2. Assigns correspondences to appropriate personnel
3. Sets up workflow routing

**API Endpoint**: `POST /api/incoming-migration/assignment`

**Status**: Implementation in progress

#### Phase 4: Business Log
**Purpose**: Process business logic and workflows

**Process**:
1. Applies business rules to correspondences
2. Sets up automated workflows
3. Configures approval chains

**API Endpoint**: `POST /api/incoming-migration/business-log`

**Status**: Implementation in progress

#### Phase 5: Comment
**Purpose**: Process comments and annotations

**Process**:
1. Migrates correspondence comments
2. Preserves comment threading
3. Maintains user attribution

**API Endpoint**: `POST /api/incoming-migration/comment`

**Status**: Implementation in progress

#### Phase 6: Closing
**Purpose**: Close correspondences that need to be archived

**Process**:
1. Identifies correspondences marked for closing (`isNeedToClose = true`)
2. Executes closing procedures in destination system
3. Updates final status

**API Endpoint**: `POST /api/incoming-migration/closing`

**Status**: Implementation in progress

### Migration UI

The Angular-based migration UI provides:

#### Dashboard Features
- **Real-time Statistics**: Shows completed, in-progress, and failed migration counts
- **Phase Cards**: Visual representation of each migration phase with status indicators
- **Sequential Execution**: Phases must be executed in order
- **Error Handling**: Displays detailed error information for failed operations
- **Retry Mechanism**: Allows retrying failed migrations

#### Running the Migration UI

```bash
# Navigate to Angular UI directory
cd angular-migration-ui

# Install dependencies
npm install

# Start development server
npm start
```

Access the UI at: `http://localhost:4200`

#### Using the Migration Interface

1. **View Statistics**: Dashboard automatically loads current migration statistics
2. **Execute Phases**: Click "Execute Phase" buttons in sequential order
3. **Monitor Progress**: Watch real-time updates as phases complete
4. **Handle Errors**: Use "Retry Failed" button to reprocess failed migrations
5. **Refresh Data**: Use "Refresh" button to update statistics

### Migration Data Flow

```
Source System → Data Import Service → Destination System
     ↓                    ↓                    ↓
JSON/API Data    →  Phase Processing  →  REST API Calls
Attachments      →  File Upload       →  Document Storage
Metadata         →  Field Mapping     →  Structured Data
```

### Error Handling and Recovery

#### Automatic Retry
- Failed operations are automatically retried up to 3 times
- Exponential backoff prevents system overload
- Detailed error logging for troubleshooting

#### Manual Recovery
- Use "Retry Failed" button in UI to reprocess failed migrations
- Individual phase re-execution capability
- Detailed error messages for debugging

#### Monitoring
- Real-time statistics tracking
- Phase-level progress monitoring
- Error aggregation and reporting

### Database Schema

#### Migration Tracking Table
```sql
incoming_correspondence_migrations
├── correspondence_guid (Primary identifier)
├── current_phase (Current processing phase)
├── phase_status (PENDING/COMPLETED/ERROR)
├── creation_step (Detailed step within creation phase)
├── created_document_id (ID in destination system)
├── batch_id (File upload batch identifier)
├── retry_count (Number of retry attempts)
└── error fields (Detailed error information per phase)
```

### Configuration Files

#### Required JSON Files
- `src/main/resources/externalAgencies.json`: Agency mappings
- `src/main/resources/departments.json`: Department code mappings
- `src/main/resources/users.json`: User import data

#### Application Properties
```properties
# Source API Configuration
source.api.base-url=https://itba.tarasol.cloud/Tarasol4ExtractorApi/Api/secure
source.api.key=test

# Destination API Configuration
destination.api.url=http://18.206.121.44/nuxeo/api/v1/custom-automation/
destination.api.token=<JWT_TOKEN>
```

### Troubleshooting

#### Common Issues
1. **Empty Statistics**: Ensure Spring Boot service is running on port 8080
2. **CORS Errors**: Backend includes CORS headers for Angular development
3. **Authentication Failures**: Verify JWT token is valid and not expired
4. **File Upload Failures**: Check file size limits (200MB max)

#### Logs
- Spring Boot logs: Check console output for detailed error information
- Angular logs: Open browser developer tools (F12) for frontend issues
- Database logs: Monitor MySQL logs for database-related issues

### Performance Considerations

- **Batch Processing**: Large datasets are processed in manageable chunks
- **Connection Pooling**: Optimized database connections for concurrent operations
- **File Size Limits**: Attachments over 200MB are skipped to prevent memory issues
- **Transaction Management**: Each phase uses separate transactions for isolation