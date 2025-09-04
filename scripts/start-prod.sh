#!/bin/bash

# Production startup script
echo "Starting Data Import Service in Production Mode..."

# Check if .env file exists
if [ ! -f .env ]; then
    echo "Warning: .env file not found. Creating from example..."
    cp .env.example .env
    echo "Please edit .env file with your production values before running again."
    exit 1
fi

# Load environment variables from .env file
set -a
source .env
set +a

echo "Environment Configuration:"
echo "API_BASE_URL: $API_BASE_URL"
echo "MYSQL_DATABASE: $MYSQL_DATABASE"
echo "MYSQL_USER: $MYSQL_USER"

# Validate required environment variables
if [ -z "$MYSQL_ROOT_PASSWORD" ] || [ -z "$KEYCLOAK_PASSWORD" ] || [ -z "$DESTINATION_TOKEN" ]; then
    echo "Error: Required environment variables are not set in .env file:"
    echo "- MYSQL_ROOT_PASSWORD"
    echo "- KEYCLOAK_PASSWORD" 
    echo "- DESTINATION_TOKEN"
    exit 1
fi

# Start production environment
docker-compose -f docker-compose.prod.yml up -d

echo "Production environment started!"
echo "Frontend: http://localhost"
echo "Backend API: $API_BASE_URL"
echo "MySQL: localhost:3306"

# Show status
docker-compose -f docker-compose.prod.yml ps