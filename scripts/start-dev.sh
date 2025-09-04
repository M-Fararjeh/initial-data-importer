#!/bin/bash

# Development startup script
echo "Starting Data Import Service in Development Mode..."

# Set default environment variables if not already set
export API_BASE_URL=${API_BASE_URL:-http://localhost:8080/data-import}
export MYSQL_ROOT_PASSWORD=${MYSQL_ROOT_PASSWORD:-rootpassword123}
export MYSQL_USER=${MYSQL_USER:-import_user}
export MYSQL_PASSWORD=${MYSQL_PASSWORD:-import_password123}
export MYSQL_DATABASE=${MYSQL_DATABASE:-data_import_db}

echo "Environment Configuration:"
echo "API_BASE_URL: $API_BASE_URL"
echo "MYSQL_DATABASE: $MYSQL_DATABASE"
echo "MYSQL_USER: $MYSQL_USER"

# Start development environment
docker-compose -f docker-compose.dev.yml up -d

echo "Development environment started!"
echo "Frontend: http://localhost:4200"
echo "Backend API: http://localhost:8080/data-import"
echo "Swagger UI: http://localhost:8080/data-import/swagger-ui.html"
echo "MySQL: localhost:3306"

# Show logs
echo "Showing logs (Ctrl+C to exit)..."
docker-compose -f docker-compose.dev.yml logs -f