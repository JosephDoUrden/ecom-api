#!/bin/bash

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[0;33m'
NC='\033[0m' # No Color

# Change to project root directory since this script is in scripts/
cd "$(dirname "$0")/.." || exit 1
PROJECT_ROOT=$(pwd)

echo -e "${GREEN}Rebuilding API service...${NC}"

# Build the Spring Boot application
echo -e "${YELLOW}Building the application...${NC}"
./mvnw clean package -DskipTests

if [ $? -ne 0 ]; then
  echo -e "${RED}Maven build failed. Exiting.${NC}"
  exit 1
fi

# Check if we need to fix Keycloak database issues
if docker-compose logs postgres-db | grep -q "relation \"migration_model\" does not exist" || 
   docker-compose logs postgres-db | grep -q "relation \"public.databasechangelog\" does not exist"; then
  echo -e "${YELLOW}Detected Keycloak database migration issues. Attempting to fix...${NC}"
  
  # Stop Keycloak to prevent further errors
  echo -e "${YELLOW}Stopping Keycloak container...${NC}"
  docker-compose stop keycloak
  
  # Restart PostgreSQL to ensure a clean state
  echo -e "${YELLOW}Restarting PostgreSQL container...${NC}"
  docker-compose restart db
  
  # Wait for PostgreSQL to be ready
  echo -e "${YELLOW}Waiting for PostgreSQL to be ready...${NC}"
  sleep 10
  
  # Start Keycloak with additional parameters to initialize database schema
  echo -e "${YELLOW}Starting Keycloak with database initialization...${NC}"
  docker-compose up -d keycloak
  
  # Wait for Keycloak to initialize
  echo -e "${YELLOW}Waiting for Keycloak to initialize the database...${NC}"
  sleep 15
  
  echo -e "${GREEN}Keycloak database fix attempted. Continuing with API rebuild...${NC}"
fi

# Rebuild and restart only the API container
echo -e "${YELLOW}Rebuilding and restarting API container...${NC}"
docker-compose up -d --build --no-deps app

if [ $? -ne 0 ]; then
  echo -e "${RED}Failed to rebuild API container.${NC}"
  exit 1
fi

echo -e "${GREEN}API service has been successfully rebuilt and restarted.${NC}"
echo -e "${YELLOW}Use 'docker-compose logs -f app' to view logs.${NC}"

# Provide helpful commands for debugging
echo -e "${YELLOW}Helpful commands for debugging:${NC}"
echo -e "  ${GREEN}docker-compose logs -f${NC} - View all container logs"
echo -e "  ${GREEN}docker-compose logs -f postgres-db${NC} - View database logs"
echo -e "  ${GREEN}docker-compose logs -f keycloak${NC} - View Keycloak logs"
echo -e "  ${GREEN}docker-compose exec postgres-db psql -U ecommerce_user -d keycloak${NC} - Access Keycloak database"
