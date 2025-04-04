#!/bin/bash

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[0;33m'
NC='\033[0m' # No Color

# Change to project root directory since this script is in scripts/
cd "$(dirname "$0")/.." || exit 1
PROJECT_ROOT=$(pwd)

echo -e "${YELLOW}Attempting to fix database issues...${NC}"

# Stop containers
echo -e "${YELLOW}Stopping containers...${NC}"
docker-compose stop

# Remove PostgreSQL volume
echo -e "${YELLOW}Removing PostgreSQL volume to start fresh...${NC}"
docker volume rm $(docker volume ls -q | grep -E 'ecom.*postgres-data') 2>/dev/null || true

# Start database container only
echo -e "${GREEN}Starting PostgreSQL container...${NC}"
docker-compose up -d db

# Wait for PostgreSQL to be ready
echo -e "${YELLOW}Waiting for PostgreSQL to be ready...${NC}"
sleep 10

# Create the ecommerce database
echo -e "${YELLOW}Creating ecommerce database...${NC}"
docker-compose exec db psql -U ecommerce_user -c "CREATE DATABASE ecommerce WITH OWNER=ecommerce_user ENCODING='UTF8' LC_COLLATE='en_US.utf8' LC_CTYPE='en_US.utf8';" postgres

# Create the keycloak database
echo -e "${YELLOW}Creating keycloak database...${NC}"
docker-compose exec db psql -U ecommerce_user -c "CREATE DATABASE keycloak WITH OWNER=ecommerce_user ENCODING='UTF8' LC_COLLATE='en_US.utf8' LC_CTYPE='en_US.utf8';" postgres

# Create extensions in the ecommerce database
echo -e "${YELLOW}Setting up ecommerce database...${NC}"
docker-compose exec db psql -U ecommerce_user -c "CREATE EXTENSION IF NOT EXISTS \"uuid-ossp\"; CREATE EXTENSION IF NOT EXISTS \"pg_trgm\";" ecommerce

# Create extensions in the keycloak database
echo -e "${YELLOW}Setting up keycloak database...${NC}"
docker-compose exec db psql -U ecommerce_user -c "CREATE EXTENSION IF NOT EXISTS \"uuid-ossp\";" keycloak

# Verify databases
echo -e "${YELLOW}Verifying databases...${NC}"
docker-compose exec db psql -U ecommerce_user -c "\l" postgres

# Start all services
echo -e "${GREEN}Starting all services...${NC}"
docker-compose up -d

echo -e "${GREEN}Database fix completed!${NC}"
echo -e "${YELLOW}If you continue to experience issues, you may need to manually review the database configuration.${NC}"
