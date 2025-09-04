# Data Import Service

A comprehensive enterprise-grade Spring Boot application for importing and migrating correspondence data from legacy systems to modern document management platforms. The service provides both basic data import capabilities and a sophisticated multi-phase migration system for incoming and outgoing correspondences with full fault tolerance and recovery mechanisms.

## 🏗️ Architecture Overview

The application is built with a modern, scalable microservices architecture:

### Core Components
- **Spring Boot Backend**: RESTful APIs for data processing and migration control
- **Angular Frontend**: Real-time monitoring interface with live progress tracking
- **MySQL Database**: High-performance persistent storage with optimized indexing
- **Phase-based Migration**: Sequential processing with dependency management and fault tolerance
- **Keycloak Integration**: Enterprise authentication and authorization
- **Docker Containerization**: Production-ready containerized deployment
- **Kubernetes Support**: Helm charts for cloud-native deployment

### Migration Architecture
- **6-Phase Incoming Migration**: Complete workflow from data preparation to closing
- **7-Phase Outgoing Migration**: Enhanced workflow with approval and registration
- **Step-based Processing**: Granular fault tolerance with automatic recovery
- **Real-time Monitoring**: Live progress tracking with detailed error reporting
- **Selective Processing**: Execute phases for specific records or batches

## 📋 Features

### Basic Data Import
- ✅ Import external agency data from JSON files to destination REST API
- ✅ Import all basic entities (users, departments, roles, etc.) from source system
- ✅ Import correspondence data with related entities (attachments, comments, etc.)
- ✅ Comprehensive error handling and retry mechanisms with exponential backoff
- ✅ Real-time progress tracking and detailed logging

### Advanced Migration System
- ✅ **Multi-Phase Migration Process**: Sequential execution with dependency management
- ✅ **Real-time Monitoring**: Angular UI with live progress tracking and statistics
- ✅ **Fault Tolerance**: Step-based processing with automatic recovery from failures
- ✅ **Error Recovery**: Automatic retry with exponential backoff and manual retry options
- ✅ **Selective Processing**: Execute phases for specific records or correspondence types
- ✅ **Comprehensive Logging**: Detailed audit trail for troubleshooting and compliance
- ✅ **Performance Optimization**: Optimized database queries and connection pooling

### Enterprise Features
- ✅ **Authentication**: Keycloak integration with JWT token management
- ✅ **Security**: Role-based access control and secure API endpoints
- ✅ **Monitoring**: Prometheus metrics and health checks
- ✅ **Scalability**: Horizontal pod autoscaling and load balancing
- ✅ **High Availability**: Multi-replica deployment with pod disruption budgets
- ✅ **Backup & Recovery**: Automated database backups and disaster recovery

## 🚀 Quick Start

### Using Docker Compose (Recommended for Development)

```bash
# Clone the repository
git clone <repository-url>
cd data-import-service

# Quick start with defaults
docker-compose up -d

# Or set custom backend URL
export API_BASE_URL=http://your-backend-host:8080/data-import
docker-compose up -d

# Access the application
# Frontend: http://localhost:4200
# Backend API: http://localhost:8080/data-import
# Swagger UI: http://localhost:8080/data-import/swagger-ui.html
# MySQL: localhost:3306

# View logs
docker-compose logs -f

# Stop services
docker-compose down -v
```

### Production Deployment with Docker Compose

```bash
# Set up environment variables
cp .env.example .env
# Edit .env with your production values

# Start production environment
docker-compose -f docker-compose.prod.yml up -d

# Monitor deployment
docker-compose -f docker-compose.prod.yml ps
docker-compose -f docker-compose.prod.yml logs -f
```

### Kubernetes Deployment with Helm

```bash
# Development deployment
helm install data-import-dev ./helm-chart/data-import-service \
  -f ./helm-chart/data-import-service/values-dev.yaml \
  --namespace data-import-dev \
  --create-namespace

# Production deployment
helm install data-import-prod ./helm-chart/data-import-service \
  -f ./helm-chart/data-import-service/values-prod.yaml \
  --namespace data-import-prod \
  --create-namespace

# Check deployment status
kubectl get pods -n data-import-prod
helm status data-import-prod -n data-import-prod
```

### Local Development

```bash
# Start MySQL only
docker-compose up -d mysql

# Set environment variables
export API_BASE_URL=http://localhost:8080/data-import

# Build and run Spring Boot application
mvn clean compile
mvn spring-boot:run

# Start Angular UI (in separate terminal)
cd angular-migration-ui
npm install
npm start
```

## 🌐 Access Points

| Service | URL | Description |
|---------|-----|-------------|
| **Angular Frontend** | http://localhost:4200 | Migration monitoring dashboard |
| **Spring Boot API** | http://localhost:8080/data-import | REST API endpoints |
| **Swagger Documentation** | http://localhost:8080/data-import/swagger-ui.html | Interactive API documentation |
| **MySQL Database** | localhost:3306 | Database access |
| **Health Check** | http://localhost:8080/data-import/api/health | Service health status |

## 📊 Database Configuration

### Docker Environment
```yaml
Host: localhost:3306
Database: data_import_db
Username: import_user
Password: import_password123
Root Password: rootpassword123
```

### Production Environment
```yaml
Host: mysql-service:3306
Database: data_import_db
Username: ${MYSQL_USER}
Password: ${MYSQL_PASSWORD}
SSL: Enabled
Connection Pool: 20-30 connections
```

### Performance Optimizations
- **InnoDB Buffer Pool**: 2-6GB depending on environment
- **Connection Pooling**: HikariCP with optimized settings
- **Query Optimization**: Comprehensive indexing strategy
- **Transaction Management**: Optimized isolation levels and timeouts
- **Deadlock Prevention**: Aggressive timeout settings and retry mechanisms

## 🔧 API Endpoints

### Health and Status
```bash
# Service health check
GET /api/health
# Returns: service status, version, configuration info

# Keycloak token status
GET /api/health/keycloak-token
# Returns: token validity, expiry time, refresh status

# Force token refresh
POST /api/health/keycloak-token/refresh
# Returns: new token status
```

### Basic Data Import
```bash
# Import external agencies
POST /api/import/external-agencies

# Import all basic entities
POST /api/data-import/basic-entities

# Import specific entity types
POST /api/data-import/users
POST /api/data-import/departments
POST /api/data-import/correspondences
POST /api/data-import/classifications
POST /api/data-import/contacts
POST /api/data-import/decisions
POST /api/data-import/forms
POST /api/data-import/form-types
POST /api/data-import/importance
POST /api/data-import/positions
POST /api/data-import/pos-roles
POST /api/data-import/priority
POST /api/data-import/roles
POST /api/data-import/secrecy
POST /api/data-import/user-positions

# Import correspondence-related data
POST /api/data-import/correspondence-attachments/{docGuid}
POST /api/data-import/correspondence-comments/{docGuid}
POST /api/data-import/correspondence-copy-tos/{docGuid}
POST /api/data-import/correspondence-current-departments/{docGuid}
POST /api/data-import/correspondence-current-positions/{docGuid}
POST /api/data-import/correspondence-current-users/{docGuid}
POST /api/data-import/correspondence-custom-fields/{docGuid}
POST /api/data-import/correspondence-links/{docGuid}
POST /api/data-import/correspondence-send-tos/{docGuid}
POST /api/data-import/correspondence-transactions/{docGuid}

# Get entity counts
GET /api/data-import/{entity-type}/count
```

### Incoming Correspondence Migration System
```bash
# Phase execution
POST /api/incoming-migration/prepare-data
POST /api/incoming-migration/creation
POST /api/incoming-migration/assignment
POST /api/incoming-migration/business-log
POST /api/incoming-migration/comment
POST /api/incoming-migration/closing

# Statistics and monitoring
GET /api/incoming-migration/statistics
GET /api/incoming-migration/creation/details
GET /api/incoming-migration/creation/statistics
GET /api/incoming-migration/assignment/details?page=0&size=20&status=all&search=
GET /api/incoming-migration/business-log/details?page=0&size=20&status=all&search=
GET /api/incoming-migration/comment/details?page=0&size=20&status=all&commentType=all&search=
GET /api/incoming-migration/closing/details?page=0&size=20&status=all&needToClose=all&search=

# Selective execution
POST /api/incoming-migration/creation/execute-specific
POST /api/incoming-migration/assignment/execute-specific
POST /api/incoming-migration/business-log/execute-specific
POST /api/incoming-migration/comment/execute-specific
POST /api/incoming-migration/closing/execute-specific

# Retry failed migrations
POST /api/incoming-migration/retry-failed
```

### Outgoing Correspondence Migration System
```bash
# Phase execution
POST /api/outgoing-migration/prepare-data
POST /api/outgoing-migration/creation
POST /api/outgoing-migration/assignment
POST /api/outgoing-migration/approval
POST /api/outgoing-migration/business-log
POST /api/outgoing-migration/closing

# Statistics and monitoring
GET /api/outgoing-migration/statistics
GET /api/outgoing-migration/creation/details
GET /api/outgoing-migration/creation/statistics
GET /api/outgoing-migration/assignment/details?page=0&size=20&status=all&search=
GET /api/outgoing-migration/approval/details?page=0&size=20&status=all&step=all&search=
GET /api/outgoing-migration/business-log/details?page=0&size=20&status=all&search=
GET /api/outgoing-migration/closing/details?page=0&size=20&status=all&needToClose=all&search=

# Selective execution
POST /api/outgoing-migration/creation/execute-specific
POST /api/outgoing-migration/assignment/execute-specific
POST /api/outgoing-migration/approval/execute-specific
POST /api/outgoing-migration/business-log/execute-specific
POST /api/outgoing-migration/closing/execute-specific

# Retry failed migrations
POST /api/outgoing-migration/retry-failed
```

### User Management
```bash
# Import users to destination system
POST /api/user-import/users-to-destination
```

### Correspondence Related Data Import
```bash
# Import all correspondences with related data
POST /api/correspondence-import/all-correspondences-with-related

# Import related data for specific correspondence
POST /api/correspondence-import/correspondence/{correspondenceGuid}/related

# Get import statistics
GET /api/correspondence-import/statistics

# Get import statuses
GET /api/correspondence-import/status

# Reset import status
POST /api/correspondence-import/reset/{correspondenceGuid}

# Retry failed imports
POST /api/correspondence-import/retry-failed
```

## 🔄 Migration Process

### Incoming Correspondence Migration (6 Phases)

#### Phase 1: Prepare Data
**Purpose**: Select and prepare incoming correspondences for migration

**Process**:
- Filters correspondences where `correspondenceType = 2` (incoming)
- Excludes deleted, draft, and cancelled correspondences
- Creates migration tracking records in `incoming_correspondence_migrations` table
- Sets `isNeedToClose` based on business rules (isFinal, isArchive flags)

**API**: `POST /api/incoming-migration/prepare-data`

#### Phase 2: Creation
**Purpose**: Create correspondences in destination system with attachments

**Fault-Tolerant Sub-steps**:
1. **Get Details**: Retrieve and validate correspondence data from database
2. **Get Attachments**: Find all attachments and identify primary attachment
3. **Upload Main Attachment**: Create batch and upload primary PDF attachment (if exists)
4. **Create Correspondence**: Create the correspondence record in destination system
5. **Upload Other Attachments**: Process and upload remaining non-primary attachments
6. **Create Physical Attachment**: Handle manual attachments (if specified)
7. **Set Ready to Register**: Prepare correspondence for registration workflow
8. **Register with Reference**: Register correspondence with required parameters and context
9. **Start Work**: Initiate correspondence workflow in destination system
10. **Set Owner**: Assign ownership to the correspondence creator

**Fault Tolerance Features**:
- **Step Persistence**: Current step is saved to database after each successful operation
- **Resume Capability**: Failed correspondences can resume from their last successful step
- **Error Isolation**: Failure in one step doesn't affect other correspondences
- **Retry Logic**: Failed steps can be retried without reprocessing successful steps
- **Progress Tracking**: Real-time visibility into which step each correspondence is processing

**API**: `POST /api/incoming-migration/creation`

#### Phase 3: Assignment
**Purpose**: Assign correspondences to users and departments

**Process**:
- Processes assignment transactions (`action_id = 12`)
- Maps department GUIDs to department codes using `departments.json`
- Creates assignments in destination system with proper user and department mapping
- Supports both user-specific and department-level assignments

**API**: `POST /api/incoming-migration/assignment`

#### Phase 4: Business Log
**Purpose**: Process business logic and workflows

**Process**:
- Processes all non-assignment transactions (`action_id != 12`)
- Creates business log entries in destination system
- Maintains audit trail of correspondence actions and workflow events
- Maps action types and user information

**API**: `POST /api/incoming-migration/business-log`

#### Phase 5: Comment
**Purpose**: Process comments and annotations

**Process**:
- Migrates correspondence comments with full text and metadata
- Preserves comment threading and user attribution
- Handles comment attachments and file data
- Supports different comment types (User, System, Admin)

**API**: `POST /api/incoming-migration/comment`

#### Phase 6: Closing
**Purpose**: Close correspondences that need archiving

**Process**:
- Identifies correspondences marked for closing (`isNeedToClose = true`)
- Executes closing procedures in destination system
- Updates final migration status and completion timestamps
- Handles archival and retention policies

**API**: `POST /api/incoming-migration/closing`

### Outgoing Correspondence Migration (7 Phases)

The outgoing correspondence migration follows the same fault-tolerant approach but includes an additional approval phase and different API endpoints.

#### Phase 1: Prepare Data
**Purpose**: Select and prepare outgoing correspondences for migration

**Process**:
- Filters correspondences where `correspondenceType = 1` (outgoing)
- Excludes deleted, draft, and cancelled correspondences
- Creates migration tracking records in `outgoing_correspondence_migrations` table
- Sets `isNeedToClose` based on `isArchive` field from correspondence

**API**: `POST /api/outgoing-migration/prepare-data`

#### Phase 2: Creation
**Purpose**: Create outgoing correspondences in destination system

**Sub-steps**:
1. **Get Details**: Retrieve and validate outgoing correspondence data
2. **Upload Main Attachment**: Create batch and upload primary attachment (if exists)
3. **Create Correspondence**: Create outgoing correspondence record using `AC_UA_OutgoingCorrespondence_Create`
4. **Upload Other Attachments**: Process and upload remaining attachments
5. **Create Physical Attachment**: Handle manual attachments using `AC_UA_PhysicalAttachment_Add`

**Key Differences from Incoming**:
- Uses `AC_UA_OutgoingCorrespondence_Create` operation
- Maps `out_corr:signee` to "SECTOR"
- Sets `corr:fromAgency` to "ITBA"
- Maps `corr:toAgency` using agency mapping utilities
- Includes `out_corr:multiRecivers` array

**API**: `POST /api/outgoing-migration/creation`

#### Phase 3: Assignment
**Purpose**: Assign outgoing correspondences to users and departments

**Process**:
- Creates readonly assignments using `AC_UA_Assignment_Create`
- Sets `isReadOnly: true` in context for outgoing assignments
- Maps department GUIDs to department codes
- Includes `assign:completeDate` in updateProp

**API**: `POST /api/outgoing-migration/assignment`

#### Phase 4: Approval *(Unique to Outgoing)*
**Purpose**: Approve outgoing correspondences and register them for sending

**Sub-steps**:
1. **Approve Correspondence**: Uses `AC_UA_OutgoingCorrespondence_SendWithoutApproval`
2. **Register with Reference**: Uses `AC_UA_OutgoingCorrespondence_Register_WithReference`
3. **Send Correspondence**: Uses `AC_UA_OutgoingCorrespondence_Send`

**Step-based Processing**:
- Tracks current approval step in `approval_step` field
- Can resume from failed approval steps
- Maintains outgoing correspondence context throughout process

**API**: `POST /api/outgoing-migration/approval`

#### Phase 5: Business Log
**Purpose**: Process business logic and workflows for outgoing correspondences

**Process**:
- Similar to incoming business log but for outgoing correspondence transactions
- Uses `Document.CreateBusinessLog` with `documentTypes: "OutgoingCorrespondence"`
- Processes all non-assignment transactions

**API**: `POST /api/outgoing-migration/business-log`

#### Phase 6: Comment
**Purpose**: Process comments and annotations for outgoing correspondences

**Process**:
- Migrates correspondence comments for outgoing correspondences
- Uses same comment creation API as incoming
- Preserves comment threading and user attribution

**API**: `POST /api/outgoing-migration/comment`

#### Phase 7: Closing
**Purpose**: Close outgoing correspondences that need archiving

**Process**:
- Uses `AC_UA_OutgoingCorrespondence_Close` operation
- Includes `corr:closeDate` in updateProp
- Only processes correspondences where `isNeedToClose = true`

**API**: `POST /api/outgoing-migration/closing`

## 🎯 Service Architecture

### Core Services
- **`IncomingCorrespondenceMigrationService`**: Main orchestration for incoming migration
- **`OutgoingCorrespondenceMigrationService`**: Main orchestration for outgoing migration
- **`DataImportService`**: Basic entity import operations
- **`ExternalAgencyImportService`**: External agency data processing
- **`UserImportService`**: User creation and role assignment
- **`DestinationSystemService`**: API client for destination system (incoming)
- **`OutgoingDestinationSystemService`**: API client for destination system (outgoing)
- **`KeycloakTokenService`**: JWT token management and refresh
- **`CorrespondenceRelatedImportService`**: Correspondence-related data import with status tracking

### Migration Phase Services

#### Incoming Migration Services
- **`PrepareDataService`**: Phase 1 implementation
- **`CreationPhaseService`**: Phase 2 implementation with 10 sub-steps
- **`AssignmentPhaseService`**: Phase 3 implementation
- **`BusinessLogPhaseService`**: Phase 4 implementation
- **`CommentPhaseService`**: Phase 5 implementation
- **`ClosingPhaseService`**: Phase 6 implementation

#### Outgoing Migration Services
- **`OutgoingPrepareDataService`**: Outgoing Phase 1 implementation
- **`OutgoingCreationPhaseService`**: Outgoing Phase 2 implementation with 5 sub-steps
- **`OutgoingAssignmentPhaseService`**: Outgoing Phase 3 implementation
- **`OutgoingApprovalPhaseService`**: Outgoing Phase 4 implementation with 3 sub-steps
- **`OutgoingBusinessLogPhaseService`**: Outgoing Phase 5 implementation
- **`OutgoingClosingPhaseService`**: Outgoing Phase 7 implementation

#### Common Services
- **`MigrationPhaseService`**: Common phase utilities and status management
- **`MigrationStatisticsService`**: Statistics and reporting for both migration types

### Utility Classes
- **`AgencyMappingUtils`**: Maps agency GUIDs to codes using `agency mapping.json`
- **`DepartmentUtils`**: Maps department GUIDs to codes using `departments.json`
- **`CorrespondenceUtils`**: Field mapping and data transformation utilities
- **`AttachmentUtils`**: Attachment processing and validation utilities
- **`HijriDateUtils`**: Gregorian to Hijri date conversion
- **`CorrespondenceSubjectGenerator`**: Random subject generation for testing

## 📁 Data Mapping

### Field Mappings

#### External Agencies
```
Source JSON → Destination API
id → externalagency:agencyId (formatted to 3 digits)
label_en → externalagency:nameEn
label_ar → externalagency:nameAr
category → externalagency:typee
"saudiArabia" → externalagency:country (constant)
```

#### Correspondence Priority
```
1 → N (Normal)
2 → H (Important)  
3 → C (High)
```

#### Secrecy Levels
```
1 → Normal
2 → Top_Secret
3 → Secret
```

#### Categories
```
Classification GUID → Category Type
01b1a89b-dff0-4040-878e-02c3fd4d7925 → AwardDecision
06841b0a-f569-40c5-91d5-276c7f8c532b → AccessPermit
00a91759-734c-4be5-8a11-96e69dfae5a0 → WorkAssignment
29f2cf7c-3a43-44cb-9ac7-b5570c760c60 → Promotion
0bfa3e9c-682b-41c4-a275-ba395b52b0f7 → Circular
26878084-7736-4935-88cb-d4312c2324f9 → Private
87b623f9-eeeb-4829-8f8e-dc54d1fb242e → PurchaseContract
0d00ee38-b289-42a2-a3a9-e0d8421ac1c6 → General
6dcc58c0-ebca-46b0-afb1-e96a4f1ebb7c → PeriodicReport
```

#### Actions
```
Action GUID → Action Type
5ed900bc-a5f1-41cd-8f4f-0f05b7ef67c2 → ForInformation
11f24280-d287-42ee-a72a-1c91274cfa4a → ForAdvice
1225fe27-5841-48e6-a47b-4cc9d9770fa6 → Toproceed
a93aa474-7a3a-4d9a-8c15-6a3bcd706b51 → ToTakeNeededAction
5379d40a-2726-4372-ab80-9564586e0458 → FYI
2fb05e63-896a-4c9b-a99f-bca4deccc6ac → ForSaving
```

## 🖥️ Migration UI

The Angular-based migration interface provides comprehensive monitoring and control:

### Dashboard Features
- **Real-time Statistics**: Live counts of completed, in-progress, and failed migrations
- **Phase Cards**: Visual representation of each migration phase with status indicators
- **Sequential Execution**: Phases must be executed in order with dependency checking
- **Error Handling**: Detailed error information and retry capabilities
- **Dual Migration Support**: Separate dashboards for incoming and outgoing correspondences
- **Progress Tracking**: Step-by-step progress monitoring with percentage completion
- **Search and Filtering**: Advanced filtering by status, step, user, and text search

### Detail Views

#### Incoming Correspondence Details
- **Creation Details**: Monitor correspondence creation progress with 10-step tracking
- **Assignment Details**: View assignment transactions with user and department mapping
- **Business Log Details**: Track business logic processing and workflow events
- **Comment Details**: Monitor comment migration with type filtering and attachment handling
- **Closing Details**: Manage correspondence closing process with need-to-close filtering

#### Outgoing Correspondence Details
- **Creation Details**: Monitor outgoing correspondence creation with 5-step process
- **Assignment Details**: View readonly assignment creation for outgoing correspondences
- **Approval Details**: Track 3-step approval process (approve, register, send)
- **Business Log Details**: Monitor outgoing business logic processing
- **Comment Details**: Track outgoing comment migration
- **Closing Details**: Manage outgoing correspondence closing process

### Advanced Features
- **Pagination**: Server-side pagination for large datasets
- **Bulk Operations**: Select and execute operations on multiple records
- **Real-time Updates**: Live statistics updates without page refresh
- **Error Recovery**: Individual and bulk retry mechanisms
- **Export Capabilities**: Export migration reports and statistics

### Running the Migration UI

```bash
cd angular-migration-ui
npm install
npm start
# Access at: http://localhost:4200
```

## 🔧 Configuration

### Required Configuration Files

#### Application Properties
```properties
# Environment Configuration
api.base.url=${API_BASE_URL:http://localhost:8080/data-import}

# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/data_import_db?useSSL=false&serverTimezone=UTC&useUnicode=true&characterEncoding=utf8
spring.datasource.username=import_user
spring.datasource.password=import_password123
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# Keycloak Configuration
keycloak.auth.enabled=true
keycloak.auth.server-url=http://18.206.121.44/auth
keycloak.auth.realm=itba
keycloak.auth.client-id=cspfrontend
keycloak.auth.username=cts_admin
keycloak.auth.password=your_actual_password

# Source API Configuration
source.api.base-url=https://itba.tarasol.cloud/Tarasol4ExtractorApi/Api/secure
source.api.key=test
source.api.timeout=600000

# Destination API Configuration
destination.api.base-url=http://18.206.121.44
destination.api.url=http://18.206.121.44/nuxeo/api/v1/custom-automation/AC_UA_ExternalAgency_Create
destination.api.token=fallback_token_if_keycloak_fails

# File Upload Configuration
file.upload.use-sample=true
file.upload.sample.filename=sample_document.pdf

# Correspondence Configuration
correspondence.random-subject.enabled=true
correspondence.random-subject.prefix=AUTO-

# Admin User Configuration
admin.user.username=cts_admin
```

#### Required JSON Files
- **`src/main/resources/externalAgencies.json`**: External agency data for import
- **`src/main/resources/agency mapping.json`**: Agency GUID to code mappings (100+ agencies)
- **`src/main/resources/departments.json`**: Department GUID to code mappings (25+ departments)
- **`src/main/resources/users.json`**: User import data with roles and permissions
- **`src/main/resources/test.pdf`**: Sample PDF file for testing file uploads

### Environment Profiles
- **Default**: Local development with MySQL
- **Docker**: Container environment with optimized settings
- **Production**: Production-ready configuration with enhanced security
- **Kubernetes**: Cloud-native configuration with service discovery

### Docker Environment Variables

Configure the system using environment variables:

```yaml
services:
  app:
    environment:
      # Database Configuration
      - SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/data_import_db
      - SPRING_DATASOURCE_USERNAME=import_user
      - SPRING_DATASOURCE_PASSWORD=import_password123
      
      # JVM Configuration
      - JAVA_OPTS=-Xmx2g -Xms1g -XX:+UseG1GC
      
      # Logging Configuration
      - LOGGING_LEVEL_ROOT=INFO
      - LOGGING_LEVEL_COM_IMPORTSERVICE=INFO
      
      # Keycloak Configuration
      - KEYCLOAK_AUTH_ENABLED=true
      - KEYCLOAK_AUTH_PASSWORD=your_password
      
      # API Configuration
      - DESTINATION_API_TOKEN=your_token
      - SOURCE_API_TIMEOUT=600000
      
      # Feature Flags
      - CORRESPONDENCE_RANDOM_SUBJECT_ENABLED=true
      - FILE_UPLOAD_USE_SAMPLE=true
  
  frontend:
    environment:
      - API_BASE_URL=http://app:8080/data-import
```

### Kubernetes Configuration

The Helm chart supports extensive configuration:

```yaml
# Production values example
backend:
  replicaCount: 5
  resources:
    limits:
      memory: 6Gi
      cpu: 3000m
    requests:
      memory: 3Gi
      cpu: 1500m
  
  autoscaling:
    enabled: true
    minReplicas: 5
    maxReplicas: 20
  
  env:
    JAVA_OPTS: "-Xmx4g -Xms2g -XX:+UseG1GC"
    SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE: "30"

mysql:
  primary:
    persistence:
      size: 200Gi
      storageClass: "fast-ssd"
    resources:
      limits:
        memory: 8Gi
        cpu: 4000m
```

## 🔍 Monitoring and Troubleshooting

### Logging Configuration
```properties
# Enable detailed logging
logging.level.com.importservice=DEBUG
destination.api.logging.enabled=true
destination.api.logging.include-headers=true
destination.api.logging.include-response=true

# File logging
logging.file.name=/app/logs/application.log
logging.file.max-size=100MB
logging.pattern.file=%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n
```

### Health Checks

#### Application Health
```bash
# Basic health check
curl http://localhost:8080/data-import/api/health

# Keycloak token status
curl http://localhost:8080/data-import/api/health/keycloak-token

# Force token refresh
curl -X POST http://localhost:8080/data-import/api/health/keycloak-token/refresh
```

#### Database Health
```sql
-- Check connection pool status
SHOW PROCESSLIST;

-- Monitor slow queries
SELECT * FROM mysql.slow_log ORDER BY start_time DESC LIMIT 10;

-- Check table sizes
SELECT 
    table_name, 
    ROUND(((data_length + index_length) / 1024 / 1024), 2) AS 'Size (MB)',
    table_rows
FROM information_schema.tables 
WHERE table_schema = 'data_import_db'
ORDER BY (data_length + index_length) DESC;

-- Check migration progress
SELECT 
    current_phase, 
    COUNT(*) as count,
    AVG(retry_count) as avg_retries
FROM incoming_correspondence_migrations 
GROUP BY current_phase;
```

### Common Issues and Solutions

#### 1. Keycloak Token Issues
**Problem**: Authentication failures or expired tokens
**Solution**: 
- Verify Keycloak credentials in configuration
- Check token status: `GET /api/health/keycloak-token`
- Force token refresh: `POST /api/health/keycloak-token/refresh`
- Ensure Keycloak server is accessible at configured URL

#### 2. Creation Process Stuck on Specific Step
**Problem**: Correspondence creation appears stuck on a particular step
**Solution**: 
- Check the `creation_step` field in migration tables
- Review logs for the specific step that's failing
- Use the retry mechanism to resume from the failed step
- Verify destination system connectivity for the failing step

#### 3. Database Connection Issues
**Problem**: Connection timeouts or deadlocks
**Solution**: 
- Check connection pool settings in HikariCP configuration
- Monitor active connections: `SHOW PROCESSLIST`
- Verify MySQL timeout settings
- Check for long-running transactions

#### 4. File Upload Failures
**Problem**: Large attachments fail to upload
**Solution**: 
- Check file size limits (configurable, default 200MB)
- Verify MySQL `max_allowed_packet` setting
- Monitor disk space on MySQL data directory
- Check network timeouts for large file transfers

#### 5. Memory Issues
**Problem**: OutOfMemoryError or high memory usage
**Solution**: 
- Adjust JVM heap size: `-Xmx4g -Xms2g`
- Monitor garbage collection: `-XX:+PrintGCDetails`
- Check for memory leaks in file processing
- Optimize batch processing sizes

### Performance Monitoring

#### Application Metrics
```bash
# JVM metrics (if actuator is enabled)
curl http://localhost:8080/data-import/actuator/metrics

# Custom migration metrics
curl http://localhost:8080/data-import/api/incoming-migration/statistics
curl http://localhost:8080/data-import/api/outgoing-migration/statistics
```

#### Database Performance
```sql
-- Check slow queries
SELECT 
    sql_text,
    exec_count,
    avg_timer_wait/1000000000 as avg_time_seconds
FROM performance_schema.events_statements_summary_by_digest 
ORDER BY avg_timer_wait DESC 
LIMIT 10;

-- Monitor connection usage
SELECT 
    PROCESSLIST_USER,
    PROCESSLIST_HOST,
    COUNT(*) as connections
FROM performance_schema.processlist 
GROUP BY PROCESSLIST_USER, PROCESSLIST_HOST;

-- Check table lock waits
SELECT 
    object_schema,
    object_name,
    count_star,
    sum_timer_wait/1000000000 as total_wait_seconds
FROM performance_schema.table_lock_waits_summary_by_table 
ORDER BY sum_timer_wait DESC;
```

## 🧪 Testing

### API Testing Examples

```bash
# Health checks
curl http://localhost:8080/data-import/api/health
curl http://localhost:8080/data-import/api/health/keycloak-token

# Import operations
curl -X POST http://localhost:8080/data-import/api/import/external-agencies
curl -X POST http://localhost:8080/data-import/api/data-import/basic-entities

# Migration phases
curl -X POST http://localhost:8080/data-import/api/incoming-migration/prepare-data
curl -X POST http://localhost:8080/data-import/api/incoming-migration/creation

# Statistics
curl http://localhost:8080/data-import/api/incoming-migration/statistics
curl http://localhost:8080/data-import/api/outgoing-migration/statistics

# Specific execution
curl -X POST http://localhost:8080/data-import/api/incoming-migration/creation/execute-specific \
  -H "Content-Type: application/json" \
  -d '{"correspondenceGuids": ["guid1", "guid2"]}'
```

### Database Testing
```sql
-- Check migration progress
SELECT 
    current_phase, 
    COUNT(*) as count,
    SUM(CASE WHEN phase_status = 'COMPLETED' THEN 1 ELSE 0 END) as completed,
    SUM(CASE WHEN phase_status = 'ERROR' THEN 1 ELSE 0 END) as failed
FROM incoming_correspondence_migrations 
GROUP BY current_phase;

-- Check error rates by phase
SELECT 
    current_phase,
    creation_status,
    COUNT(*) as count
FROM incoming_correspondence_migrations 
GROUP BY current_phase, creation_status;

-- Monitor assignment processing
SELECT 
    migrate_status, 
    COUNT(*) as count,
    AVG(retry_count) as avg_retries
FROM correspondence_transactions 
WHERE action_id = 12 
GROUP BY migrate_status;
```

### Load Testing

```bash
# Use Apache Bench for basic load testing
ab -n 100 -c 10 http://localhost:8080/data-import/api/health

# Use curl for concurrent requests
for i in {1..10}; do
  curl -X POST http://localhost:8080/data-import/api/incoming-migration/statistics &
done
wait
```

## 🔐 Security Considerations

### Authentication & Authorization
- **JWT Token-based Authentication**: Keycloak integration with automatic token refresh
- **API Key Authentication**: Secure source system access
- **Role-based Access Control**: Department-based user assignments in destination system
- **Secure Credential Storage**: Environment variables and Kubernetes secrets

### Data Protection
- **Sensitive Data Masking**: Automatic masking in logs and error messages
- **Secure File Upload Handling**: Validation and size limits for attachments
- **Transaction Isolation**: Proper isolation levels for data integrity
- **Audit Trail**: Comprehensive logging of all operations and changes

### Network Security
- **HTTPS/TLS**: SSL termination at ingress level
- **Network Policies**: Kubernetes network segmentation
- **CORS Configuration**: Controlled cross-origin access
- **Rate Limiting**: API rate limiting and DDoS protection

### Security Best Practices
- **Non-root Containers**: All containers run as non-root users
- **Read-only Filesystems**: Where possible, containers use read-only root filesystems
- **Security Contexts**: Proper security contexts and capabilities dropping
- **Secret Management**: Kubernetes secrets for sensitive configuration

## 📈 Performance Optimization

### Database Optimizations
- **Connection Pooling**: HikariCP with optimized settings (8-30 connections)
- **Batch Processing**: Configurable batch sizes for large datasets
- **Optimized Queries**: Native SQL queries with proper indexing
- **Index Strategy**: Comprehensive indexing on frequently queried columns
- **Transaction Management**: Optimized isolation levels and timeout settings

### Application Optimizations
- **Step-based Processing**: Minimizes transaction scope and lock duration
- **Fault-tolerant Creation**: Automatic recovery from partial failures
- **Memory Management**: Optimized JVM settings with G1GC
- **Connection Management**: Proper connection lifecycle management

### File Processing
- **Streaming**: Memory-efficient processing for large file uploads
- **Base64 Optimization**: Efficient encoding/decoding with size validation
- **File Size Limits**: Configurable limits (default 200MB) with fallback mechanisms
- **Sample File Support**: Testing mode with sample files

### API Performance
- **Request/Response Logging**: Configurable logging for debugging
- **Connection Timeout Configuration**: Optimized timeouts for different environments
- **Retry Mechanisms**: Exponential backoff with configurable max attempts
- **Bulk Processing**: Batch operations for improved throughput

## 🔄 Data Flow

```
Source System → Data Import Service → Destination System
     ↓                    ↓                    ↓
JSON/API Data    →  Phase Processing  →  REST API Calls
Attachments      →  File Upload       →  Document Storage
Metadata         →  Field Mapping     →  Structured Data
Relationships    →  Dependency Mgmt   →  Linked Records

Incoming Flow: 6 Phases → IncomingCorrespondence APIs
Outgoing Flow: 7 Phases → OutgoingCorrespondence APIs (includes Approval)

Phase Dependencies:
Prepare Data → Creation → Assignment → Business Log → Comment → Closing
                    ↓
            (Outgoing: + Approval Phase)
```

## 📝 Development Guidelines

### Adding New Migration Phases
1. Create new service in appropriate package (`migration` or `migration.outgoing`)
2. Implement phase-specific logic with proper error handling
3. Add repository methods for data access and pagination
4. Update main migration service to delegate to new service
5. Add corresponding Angular component for UI monitoring
6. **Implement Step-Based Processing**: Follow the creation phase pattern for fault tolerance
7. **Add Comprehensive Testing**: Unit tests, integration tests, and error scenario tests

### Code Organization Principles
- **Single Responsibility**: Each service handles one specific concern
- **Dependency Injection**: Use Spring's IoC container for service dependencies
- **Transaction Management**: Proper transaction boundaries for data consistency
- **Error Handling**: Comprehensive exception handling with meaningful messages
- **Logging**: Structured logging with appropriate levels (DEBUG, INFO, WARN, ERROR)
- **Fault Tolerance**: Design services to handle partial failures gracefully
- **Step Tracking**: Implement step-based processing for complex operations
- **Recovery Mechanisms**: Enable automatic recovery from failures

### Testing Strategy
- **Unit Tests**: Individual service method testing with mocking
- **Integration Tests**: API endpoint testing with test database
- **Database Tests**: Repository operation testing with test data
- **End-to-end Tests**: Complete migration workflow testing
- **Performance Tests**: Load testing with large datasets
- **Failure Recovery Tests**: Verify system handles failures correctly

### API Design Principles
- **RESTful Design**: Consistent REST API patterns
- **Pagination**: Server-side pagination for large datasets
- **Filtering**: Comprehensive filtering and search capabilities
- **Error Responses**: Standardized error response format
- **Documentation**: Comprehensive Swagger/OpenAPI documentation

## 🛠️ Deployment Options

### 1. Docker Compose (Development & Small Production)

```bash
# Development
docker-compose up -d

# Production
docker-compose -f docker-compose.prod.yml up -d
```

**Pros**: Simple setup, good for development and small deployments
**Cons**: Limited scalability, manual management

### 2. Kubernetes with Helm (Recommended for Production)

```bash
# Install with Helm
helm install data-import ./helm-chart/data-import-service \
  -f values-prod.yaml \
  --namespace production \
  --create-namespace
```

**Pros**: Auto-scaling, high availability, service discovery, rolling updates
**Cons**: More complex setup, requires Kubernetes knowledge

### 3. Manual Deployment

```bash
# Build application
mvn clean package

# Run with external MySQL
java -jar target/data-import-service-1.0.0.jar \
  --spring.datasource.url=jdbc:mysql://your-mysql:3306/data_import_db
```

**Pros**: Full control, custom configuration
**Cons**: Manual management, no auto-scaling

## 🔧 Maintenance

### Regular Tasks
- **Monitor Disk Space**: MySQL data directory and log files
- **Review Application Logs**: Check for errors and performance issues
- **Update JWT Tokens**: Monitor token expiration and refresh
- **Backup Migration Data**: Regular backups of migration tracking tables
- **Performance Monitoring**: Monitor system metrics and database performance
- **Security Updates**: Keep dependencies and base images updated

### Database Maintenance
```sql
-- Optimize tables
OPTIMIZE TABLE correspondences, correspondence_transactions, incoming_correspondence_migrations;

-- Analyze table statistics
ANALYZE TABLE correspondences, correspondence_transactions;

-- Check index usage
SELECT 
    table_schema,
    table_name,
    index_name,
    cardinality
FROM information_schema.statistics 
WHERE table_schema = 'data_import_db'
ORDER BY cardinality DESC;

-- Clean up old logs
DELETE FROM mysql.slow_log WHERE start_time < DATE_SUB(NOW(), INTERVAL 7 DAY);
```

### Scaling Considerations
- **Horizontal Scaling**: Use Kubernetes HPA for automatic scaling
- **Database Scaling**: Consider read replicas for read-heavy workloads
- **Connection Pool Tuning**: Adjust pool sizes based on load patterns
- **Memory Optimization**: Monitor and tune JVM heap sizes
- **Batch Size Optimization**: Tune batch processing for optimal performance

### Backup and Recovery
```bash
# Database backup
kubectl exec -it mysql-pod -- mysqldump -u root -p data_import_db > backup.sql

# Application logs backup
kubectl logs deployment/data-import-backend > app-logs-$(date +%Y%m%d).log

# Configuration backup
kubectl get configmap,secret -o yaml > config-backup-$(date +%Y%m%d).yaml
```

## 📚 Additional Resources

### Documentation
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Angular Documentation](https://angular.io/docs)
- [MySQL 8.0 Reference](https://dev.mysql.com/doc/refman/8.0/en/)
- [Kubernetes Documentation](https://kubernetes.io/docs/)
- [Helm Documentation](https://helm.sh/docs/)

### Configuration References
- `docker-compose.yml`: Development container orchestration
- `docker-compose.prod.yml`: Production container orchestration
- `helm-chart/`: Kubernetes deployment manifests
- `pom.xml`: Maven dependencies and build configuration
- `application.properties`: Application configuration
- `angular.json`: Angular build configuration

### Troubleshooting Resources
- **Application Logs**: Check `/app/logs/application.log` in containers
- **MySQL Logs**: Check `/var/log/mysql/` for database issues
- **Angular Logs**: Open browser developer tools (F12) for frontend issues
- **Docker Logs**: Use `docker-compose logs <service-name>` for container logs
- **Kubernetes Logs**: Use `kubectl logs <pod-name>` for pod logs

### Scripts and Utilities
- `scripts/build-images.sh`: Build Docker images for Kubernetes
- `scripts/deploy-k8s.sh`: Deploy to Kubernetes with Helm
- `scripts/k8s-logs.sh`: View Kubernetes logs
- `scripts/k8s-status.sh`: Check Kubernetes deployment status
- `scripts/start-dev.sh`: Start development environment
- `scripts/start-prod.sh`: Start production environment

## 🤝 Contributing

When contributing to this project:

1. **Follow Architecture Patterns**: Maintain the established service architecture
2. **Comprehensive Error Handling**: Include proper error handling and logging
3. **Update Documentation**: Update README and API documentation for changes
4. **Test Thoroughly**: Test with both small and large datasets
5. **Performance Considerations**: Consider performance implications of changes
6. **Security Review**: Ensure security best practices are followed
7. **Backward Compatibility**: Maintain API backward compatibility where possible

### Development Workflow
1. Fork the repository
2. Create a feature branch
3. Implement changes with tests
4. Update documentation
5. Submit pull request with detailed description
6. Code review and testing
7. Merge and deploy

## 📞 Support

### Getting Help
- **Documentation**: Check this README and inline code documentation
- **Logs**: Review application and database logs for error details
- **Health Checks**: Use health endpoints to verify system status
- **Configuration**: Verify all configuration settings match your environment
- **Testing**: Test with smaller datasets to isolate issues

### Reporting Issues
When reporting issues, please include:
- Environment details (Docker, Kubernetes, local)
- Error messages and stack traces
- Steps to reproduce the issue
- Configuration settings (without sensitive data)
- Log excerpts showing the problem

### Performance Issues
For performance problems:
- Monitor database query performance
- Check connection pool utilization
- Review JVM memory usage and garbage collection
- Analyze network latency to external APIs
- Consider scaling resources or optimizing queries

---

## 📊 System Requirements

### Minimum Requirements
- **CPU**: 2 cores
- **Memory**: 4GB RAM
- **Storage**: 20GB available space
- **Network**: Stable internet connection for API access

### Recommended Production Requirements
- **CPU**: 8+ cores
- **Memory**: 16GB+ RAM
- **Storage**: 200GB+ SSD storage
- **Network**: High-bandwidth connection with low latency
- **Database**: Dedicated MySQL server with 8GB+ RAM

### Supported Platforms
- **Operating Systems**: Linux (Ubuntu 18.04+, CentOS 7+, RHEL 7+)
- **Container Platforms**: Docker 20.10+, Kubernetes 1.19+
- **Java**: OpenJDK 8 or Oracle JDK 8
- **Database**: MySQL 8.0.43+
- **Web Browsers**: Chrome 90+, Firefox 88+, Safari 14+, Edge 90+

---

*Last updated: January 2025*
*Version: 1.0.0*
*Maintained by: Data Import Team*