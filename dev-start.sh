#!/bin/bash

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[0;33m'
NC='\033[0m' # No Color

# Function to check if directory exists and create it if not
check_and_create_dir() {
  if [ ! -d "$1" ]; then
    echo -e "${YELLOW}Creating directory $1${NC}"
    mkdir -p "$1"
  fi
}

# Check and create necessary directories
check_and_create_dir "docker/postgres/init-scripts"
check_and_create_dir "docker/redis"
check_and_create_dir "docker/prometheus" 
check_and_create_dir "docker/grafana/provisioning/datasources"
check_and_create_dir "logs"

# Check if docker is running
if ! docker info > /dev/null 2>&1; then
  echo -e "${RED}Error: Docker is not running. Please start Docker and try again.${NC}"
  exit 1
fi

# Build the Spring Boot application
echo -e "${GREEN}Building the application...${NC}"
./mvnw clean package -DskipTests

if [ $? -ne 0 ]; then
  echo -e "${RED}Maven build failed. Exiting.${NC}"
  exit 1
fi

# Start the development environment
echo -e "${GREEN}Starting the development environment...${NC}"
docker-compose up -d

if [ $? -ne 0 ]; then
  echo -e "${RED}Failed to start Docker Compose services.${NC}"
  exit 1
fi

echo -e "${GREEN}Development environment is up and running!${NC}"
echo -e "${GREEN}Service endpoints:${NC}"
echo -e "  - API: http://localhost:8080"
echo -e "  - Swagger UI: http://localhost:8080/swagger-ui.html"
echo -e "  - Keycloak: http://localhost:8081"
echo -e "  - Adminer (DB): http://localhost:8082"
echo -e "  - Prometheus: http://localhost:9090"
echo -e "  - Grafana: http://localhost:3000"

echo -e "\n${YELLOW}Use the following commands:${NC}"
echo -e "  - ${GREEN}docker-compose logs -f${NC} to view logs"
echo -e "  - ${GREEN}docker-compose down${NC} to stop all services"
echo -e "  - ${GREEN}./dev-rebuild.sh${NC} to rebuild and restart the API"
