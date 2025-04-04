#!/bin/bash

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[0;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}🔧 Building the project using Maven...${NC}"
./mvnw clean package -DskipTests

if [ $? -ne 0 ]; then
  echo -e "${RED}❌ Maven build failed. Exiting.${NC}"
  exit 1
fi

echo -e "${GREEN}🐳 Building and starting services with Docker Compose...${NC}"

# Check if containers are already running
if docker ps | grep -q "ecom-api\|postgres-db"; then
  echo -e "${YELLOW}⚠️ Containers are already running. Stopping and removing them...${NC}"
  docker-compose down
fi

# Build and start the services
docker-compose up --build -d

if [ $? -eq 0 ]; then
  echo -e "${GREEN}✅ Docker Compose services started successfully!${NC}"
  echo -e "${GREEN}📋 Service status:${NC}"
  docker-compose ps
else
  echo -e "${RED}❌ Failed to start Docker Compose services.${NC}"
  exit 1
fi

echo -e "${GREEN}🔍 You can check the logs with: docker-compose logs -f${NC}"
echo -e "${GREEN}🌐 API is available at: http://localhost:8080${NC}"
echo -e "${GREEN}🛑 To stop the services run: docker-compose down${NC}"
