#!/bin/bash

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[0;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}Rebuilding API service...${NC}"

# Build the Spring Boot application
echo -e "${YELLOW}Building the application...${NC}"
./mvnw clean package -DskipTests

if [ $? -ne 0 ]; then
  echo -e "${RED}Maven build failed. Exiting.${NC}"
  exit 1
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
