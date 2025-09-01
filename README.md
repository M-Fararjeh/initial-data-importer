# Data Import Service

A comprehensive Spring Boot application for importing and migrating correspondence data from legacy systems to modern document management platforms. The service provides both basic data import capabilities and a sophisticated multi-phase migration system for incoming correspondences.

## 🏗️ Architecture Overview

The application is built with a modular architecture consisting of:

- **Spring Boot Backend**: RESTful APIs for data processing and migration control
- **Angular Frontend**: User-friendly interface for monitoring migration progress
- **MySQL Database**: Persistent storage for imported data and migration tracking
- **Phase-based Migration**: Sequential processing ensures data integrity and proper dependencies

## 📋 Features

### Basic Data Import
- Import external agency data from JSON files to destination REST API
- Import all basic entities (users, departments, roles, etc.) from source system
- Import correspondence data with related entities (attachments, comments, etc.)
- Comprehensive error handling and retry mechanisms

### Advanced Migration System
- **6-Phase Migration Process**: Sequential execution with dependency management
- **Real-time Monitoring**: Angular UI with live progress tracking
- **Error Recovery**: Automatic retry with exponential backoff
- **Selective Processing**: Execute phases for specific records
- **Comprehensive Logging**: Detailed audit trail for troubleshooting

## 🚀 Quick Start

### Using Docker Compose (Recommended)

```bash
# Start MySQL and the application
docker-compose up -d

# View logs
docker-compose logs -f app

# Stop services
docker-compose down

# Stop and remove volumes (WARNING: This will delete all data)
docker-compose down -v
```

### Local Development

```bash
# Start MySQL only
docker-compose up -d mysql

# Build and run the Spring Boot application
mvn clean compile
mvn spring-boot:run

# Start Angular UI (in separate terminal)
cd angular-migration-ui
npm install
npm start
```

## 🌐 Access Points

- **Spring Boot API**: http://localhost:8080/data-import
- **Swagger Documentation**: http://localhost:8080/data-import/swagger-ui.html
- **Angular Migration UI**: http://localhost:4200
- **MySQL Database**: localhost:3306

## 📊 Database Configuration

### Docker Environment
- **Host**: localhost:3306
- **Database**: data_import_db
- **Username**: import_user
- **Password**: import_password123
- **Root Password**: rootpassword123

### Connection Details
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/data_import_db
spring.datasource.username=import_user
spring.datasource.password=import_password123
```

## 🔧 API Endpoints

### Health Check
```bash
GET /api/health
# Returns service health status and version
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
# ... and more
```

### Migration System
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
GET /api/incoming-migration/assignment/details

# Retry failed migrations
POST /api/incoming-migration/retry-failed
```

### User Management
```bash
# Import users to destination system
POST /api/user-import/users-to-destination
```

## 🔄 Migration Process

The migration system processes incoming correspondences through 6 sequential phases:

### Phase 1: Prepare Data
**Purpose**: Select and prepare incoming correspondences for migration

**Process**:
- Queries database for incoming correspondences (`CorrespondenceTypeId = 2`)
- Filters out deleted, draft, and cancelled correspondences
- Creates migration tracking records in `incoming_correspondence_migrations` table
- Determines if correspondence needs to be closed

**API**: `POST /api/incoming-migration/prepare-data`

### Phase 2: Creation
**Purpose**: Create correspondences in destination system with attachments

**Sub-steps**:
1. **Get Details**: Retrieve and validate correspondence data
2. **Get Attachments**: Find all attachments for the correspondence
3. **Upload Main Attachment**: Upload primary PDF attachment
4. **Create Correspondence**: Create the correspondence record
5. **Upload Other Attachments**: Process remaining attachments
6. **Create Physical Attachment**: Handle manual attachments
7. **Set Ready to Register**: Prepare for registration
8. **Register with Reference**: Register with required parameters
9. **Start Work**: Initiate workflow
10. **Set Owner**: Assign ownership

**API**: `POST /api/incoming-migration/creation`

### Phase 3: Assignment
**Purpose**: Assign correspondences to users and departments

**Process**:
- Processes assignment transactions (`action_id = 12`)
- Maps department GUIDs to department codes
- Creates assignments in destination system

**API**: `POST /api/incoming-migration/assignment`

### Phase 4: Business Log
**Purpose**: Process business logic and workflows

**Process**:
- Processes all non-assignment transactions (`action_id != 12`)
- Creates business log entries in destination system
- Maintains audit trail of correspondence actions

**API**: `POST /api/incoming-migration/business-log`

### Phase 5: Comment
**Purpose**: Process comments and annotations

**Process**:
- Migrates correspondence comments
- Preserves comment threading and user attribution
- Handles comment attachments

**API**: `POST /api/incoming-migration/comment`

### Phase 6: Closing
**Purpose**: Close correspondences that need archiving

**Process**:
- Identifies correspondences marked for closing (`isNeedToClose = true`)
- Executes closing procedures in destination system
- Updates final migration status

**API**: `POST /api/incoming-migration/closing`

## 🎯 Service Architecture

The application follows a modular service architecture:

### Core Services
- **`IncomingCorrespondenceMigrationService`**: Main orchestration service
- **`DataImportService`**: Basic entity import operations
- **`ExternalAgencyImportService`**: External agency data processing
- **`UserImportService`**: User creation and role assignment
- **`DestinationSystemService`**: API client for destination system

### Migration Phase Services
- **`PrepareDataService`**: Phase 1 implementation
- **`CreationPhaseService`**: Phase 2 implementation with 10 sub-steps
- **`AssignmentPhaseService`**: Phase 3 implementation
- **`BusinessLogPhaseService`**: Phase 4 implementation
- **`CommentPhaseService`**: Phase 5 implementation
- **`ClosingPhaseService`**: Phase 6 implementation
- **`MigrationPhaseService`**: Common phase utilities
- **`MigrationStatisticsService`**: Statistics and reporting

### Utility Classes
- **`AgencyMappingUtils`**: Maps agency GUIDs to codes
- **`DepartmentUtils`**: Maps department GUIDs to codes
- **`CorrespondenceUtils`**: Field mapping and data transformation
- **`AttachmentUtils`**: Attachment processing utilities
- **`HijriDateUtils`**: Gregorian to Hijri date conversion

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
# ... (see CorrespondenceUtils for complete mapping)
```

## 🖥️ Migration UI

The Angular-based migration interface provides:

### Dashboard Features
- **Real-time Statistics**: Live counts of completed, in-progress, and failed migrations
- **Phase Cards**: Visual representation of each migration phase with status indicators
- **Sequential Execution**: Phases must be executed in order with dependency checking
- **Error Handling**: Detailed error information and retry capabilities

### Detail Views
- **Creation Details**: Monitor correspondence creation progress with step-by-step tracking
- **Assignment Details**: View assignment transactions with user and department mapping
- **Business Log Details**: Track business logic processing and workflow events
- **Comment Details**: Monitor comment migration with type filtering
- **Closing Details**: Manage correspondence closing process

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
# Source API Configuration
source.api.base-url=https://itba.tarasol.cloud/Tarasol4ExtractorApi/Api/secure
source.api.key=test

# Destination API Configuration
destination.api.url=http://18.206.121.44/nuxeo/api/v1/custom-automation/
destination.api.token=<JWT_TOKEN>

# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/data_import_db
spring.datasource.username=import_user
spring.datasource.password=import_password123
```

#### Required JSON Files
- **`src/main/resources/externalAgencies.json`**: External agency data for import
- **`src/main/resources/agency mapping.json`**: Agency GUID to code mappings
- **`src/main/resources/departments.json`**: Department GUID to code mappings
- **`src/main/resources/users.json`**: User import data with roles and permissions

### Environment Profiles
- **Default**: Local development with MySQL
- **Docker**: Container environment with optimized settings
- **Production**: Production-ready configuration with enhanced security

## 🔍 Monitoring and Troubleshooting

### Logging Configuration
```properties
# Enable detailed logging
logging.level.com.importservice=DEBUG
destination.api.logging.enabled=true
destination.api.logging.include-headers=true
destination.api.logging.include-response=true
```

### Common Issues and Solutions

#### 1. Empty Statistics in UI
**Problem**: Angular UI shows zero statistics
**Solution**: Ensure Spring Boot service is running on port 8080 and check CORS configuration

#### 2. Authentication Failures
**Problem**: 401 Unauthorized errors
**Solution**: Verify JWT token is valid and not expired in `application.properties`

#### 3. File Upload Failures
**Problem**: Large attachments fail to upload
**Solution**: Check file size limits (200MB max) and MySQL `max_allowed_packet` setting

#### 4. Database Connection Issues
**Problem**: Cannot connect to MySQL
**Solution**: Verify Docker containers are running and database credentials are correct

### Performance Monitoring

#### Database Performance
```sql
-- Check connection pool status
SHOW PROCESSLIST;

-- Monitor slow queries
SHOW VARIABLES LIKE 'slow_query_log';

-- Check table sizes
SELECT table_name, 
       ROUND(((data_length + index_length) / 1024 / 1024), 2) AS 'Size (MB)'
FROM information_schema.tables 
WHERE table_schema = 'data_import_db'
ORDER BY (data_length + index_length) DESC;
```

#### Application Metrics
- Monitor memory usage during large file processing
- Track API response times for destination system calls
- Monitor retry counts and failure patterns

## 🧪 Testing

### API Testing Examples

```bash
# Test health endpoint
curl http://localhost:8080/data-import/api/health

# Execute migration phases
curl -X POST http://localhost:8080/data-import/api/incoming-migration/prepare-data
curl -X POST http://localhost:8080/data-import/api/incoming-migration/creation

# Get statistics
curl http://localhost:8080/data-import/api/incoming-migration/statistics

# Import external agencies
curl -X POST http://localhost:8080/data-import/api/import/external-agencies
```

### Database Testing
```sql
-- Check migration progress
SELECT current_phase, COUNT(*) as count 
FROM incoming_correspondence_migrations 
GROUP BY current_phase;

-- Check error rates
SELECT migrate_status, COUNT(*) as count 
FROM correspondence_transactions 
WHERE action_id = 12 
GROUP BY migrate_status;
```

## 🔐 Security Considerations

### Authentication
- JWT token-based authentication for destination API
- API key authentication for source system
- Secure credential storage in application properties

### Data Protection
- Sensitive data masking in logs
- Secure file upload handling
- Transaction isolation for data integrity

### Access Control
- Role-based access in destination system
- Department-based user assignments
- Security clearance level mapping (Normal, Secret, Top Secret)

## 📈 Performance Optimization

### Database Optimizations
- Connection pooling with HikariCP
- Batch processing for large datasets
- Optimized queries with native SQL for pagination
- Proper indexing on frequently queried columns

### File Processing
- Streaming for large file uploads
- Base64 encoding/decoding optimization
- File size validation and limits
- Memory-efficient attachment processing

### API Performance
- Request/response logging toggle
- Connection timeout configuration
- Retry mechanisms with exponential backoff
- Bulk processing capabilities

## 🔄 Data Flow

```
Source System → Data Import Service → Destination System
     ↓                    ↓                    ↓
JSON/API Data    →  Phase Processing  →  REST API Calls
Attachments      →  File Upload       →  Document Storage
Metadata         →  Field Mapping     →  Structured Data
Relationships    →  Dependency Mgmt   →  Linked Records
```

## 📝 Development Guidelines

### Adding New Migration Phases
1. Create new service in `com.importservice.service.migration` package
2. Implement phase-specific logic with proper error handling
3. Add repository methods for data access and pagination
4. Update `IncomingCorrespondenceMigrationService` to delegate to new service
5. Add corresponding Angular component for UI monitoring

### Code Organization Principles
- **Single Responsibility**: Each service handles one specific concern
- **Dependency Injection**: Use Spring's IoC container for service dependencies
- **Transaction Management**: Proper transaction boundaries for data consistency
- **Error Handling**: Comprehensive exception handling with meaningful messages
- **Logging**: Structured logging with appropriate levels

### Testing Strategy
- Unit tests for individual service methods
- Integration tests for API endpoints
- Database tests for repository operations
- End-to-end tests for complete migration workflows

## 🛠️ Maintenance

### Regular Tasks
- Monitor disk space for MySQL data directory
- Review and rotate application logs
- Update JWT tokens before expiration
- Backup migration tracking data
- Monitor system performance metrics

### Scaling Considerations
- Increase MySQL connection pool size for high load
- Implement horizontal scaling for processing services
- Consider message queues for asynchronous processing
- Monitor memory usage during large file operations

## 📚 Additional Resources

### Documentation
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Angular Documentation](https://angular.io/docs)
- [MySQL 8.0 Reference](https://dev.mysql.com/doc/refman/8.0/en/)

### Configuration References
- `docker-compose.yml`: Container orchestration
- `pom.xml`: Maven dependencies and build configuration
- `application.properties`: Application configuration
- `angular.json`: Angular build configuration

### Troubleshooting Logs
- Spring Boot: Check console output for detailed error information
- Angular: Open browser developer tools (F12) for frontend issues
- MySQL: Monitor `/var/log/mysql/` for database-related issues
- Docker: Use `docker-compose logs <service-name>` for container logs

---

## 🤝 Contributing

When contributing to this project:

1. Follow the established service architecture patterns
2. Maintain comprehensive error handling and logging
3. Update documentation for any new features or changes
4. Test thoroughly with both small and large datasets
5. Consider performance implications of changes

## 📞 Support

For technical support or questions:
- Review the troubleshooting section above
- Check application logs for detailed error messages
- Verify configuration settings match your environment
- Test with smaller datasets to isolate issues

---

*Last updated: January 2025*