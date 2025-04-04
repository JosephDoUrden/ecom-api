#!/bin/bash

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[0;33m'
NC='\033[0m' # No Color

# Change to project root directory since this script is in scripts/
cd "$(dirname "$0")/.." || exit 1
PROJECT_ROOT=$(pwd)

echo -e "${GREEN}Setting up Keycloak for E-Commerce Application${NC}"

# Check if required directories exist
echo -e "${YELLOW}Checking directories...${NC}"
mkdir -p docker/keycloak/import
mkdir -p docker/keycloak/tools
mkdir -p docker/postgres/init-scripts
mkdir -p src/main/java/com/ecom/api/controller

# Copy or create configuration files
echo -e "${YELLOW}Setting up Keycloak configuration files...${NC}"

# Create entrypoint script if it doesn't exist
if [ ! -f docker/keycloak/tools/docker-entrypoint.sh ]; then
  echo -e "${YELLOW}Creating docker-entrypoint.sh for Keycloak${NC}"
  cat > docker/keycloak/tools/docker-entrypoint.sh << 'EOF'
#!/bin/sh
set -e

# Function to wait for PostgreSQL to be ready
wait_for_postgres() {
  echo "Waiting for PostgreSQL..."
  until PGPASSWORD=$KC_DB_PASSWORD psql -h db -U $KC_DB_USERNAME -d keycloak -c '\q'; do
    echo "PostgreSQL is unavailable - waiting..."
    sleep 2
  done
  echo "PostgreSQL is up - executing command"
}

# Wait for the database to be ready before starting Keycloak
wait_for_postgres

# Execute the original entrypoint with the provided arguments
echo "Starting Keycloak with command: $@"
exec /opt/keycloak/bin/kc.sh "$@"
EOF
  chmod +x docker/keycloak/tools/docker-entrypoint.sh
fi

# Create basic realm file if it doesn't exist
if [ ! -f docker/keycloak/import/ecommerce-realm.json ]; then
  echo -e "${YELLOW}Creating default realm configuration${NC}"
  cat > docker/keycloak/import/ecommerce-realm.json << 'EOF'
{
  "id": "ecommerce",
  "realm": "ecommerce",
  "displayName": "E-Commerce Application",
  "enabled": true,
  "sslRequired": "external",
  "registrationAllowed": true,
  "resetPasswordAllowed": true,
  "editUsernameAllowed": false,
  "bruteForceProtected": true,
  "failureFactor": 3,
  "roles": {
    "realm": [
      {
        "name": "admin",
        "description": "Administrator role"
      },
      {
        "name": "customer",
        "description": "Regular customer role"
      },
      {
        "name": "vendor",
        "description": "Vendor/seller role"
      }
    ]
  },
  "clients": [
    {
      "clientId": "ecommerce-api",
      "name": "E-Commerce API",
      "description": "E-Commerce API Client",
      "enabled": true,
      "clientAuthenticatorType": "client-secret",
      "secret": "ecommerce-api-secret",
      "redirectUris": [
        "http://localhost:8080/*",
        "http://localhost:3000/*"
      ],
      "webOrigins": [
        "http://localhost:8080",
        "http://localhost:3000"
      ],
      "bearerOnly": false,
      "consentRequired": false,
      "standardFlowEnabled": true,
      "implicitFlowEnabled": false,
      "directAccessGrantsEnabled": true,
      "serviceAccountsEnabled": true,
      "publicClient": false
    }
  ],
  "defaultRoles": ["customer"]
}
EOF
fi

# Create database initialization script
if [ ! -f docker/postgres/init-scripts/00-create-keycloak-db.sh ]; then
  echo -e "${YELLOW}Creating database initialization script${NC}"
  cat > docker/postgres/init-scripts/00-create-keycloak-db.sh << 'EOF'
#!/bin/bash

set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE DATABASE keycloak;
    GRANT ALL PRIVILEGES ON DATABASE keycloak TO $POSTGRES_USER;
EOSQL

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "keycloak" <<-EOSQL
    CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
EOSQL
EOF
  chmod +x docker/postgres/init-scripts/00-create-keycloak-db.sh
fi

echo -e "${GREEN}Configuration files created successfully!${NC}"

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
  echo -e "${RED}Error: Docker is not running. Please start Docker and try again.${NC}"
  exit 1
fi

# Restart Keycloak container only
echo -e "${YELLOW}Restarting Keycloak container...${NC}"
docker-compose up -d --force-recreate keycloak

if [ $? -ne 0 ]; then
  echo -e "${RED}Failed to start Keycloak container. Please check logs with 'docker-compose logs keycloak'${NC}"
  exit 1
fi

# Verify Keycloak is running
echo -e "${YELLOW}Waiting for Keycloak to start...${NC}"
attempt=0
max_attempts=30
until curl -s -f http://localhost:8081 > /dev/null || [ $attempt -eq $max_attempts ]; do
  attempt=$((attempt+1))
  echo "Attempt $attempt/$max_attempts: Waiting for Keycloak to be available..."
  sleep 5
done

if [ $attempt -eq $max_attempts ]; then
  echo -e "${RED}Keycloak did not become available after multiple attempts. Please check logs.${NC}"
  exit 1
fi

echo -e "${GREEN}Keycloak is now running!${NC}"
echo -e "Admin console: http://localhost:8081/admin/"
echo -e "Username: admin"
echo -e "Password: admin"
echo -e "Realm: ecommerce"
echo -e ""
echo -e "You can create test users with: ./scripts/create-test-users.sh"
echo -e ""
echo -e "${YELLOW}Next steps:${NC}"
echo -e "1. Configure your Spring Boot application to use Keycloak for authentication"
echo -e "2. Update the BACKLOG.md to mark 'OAuth2 Authorization Server konfigürasyonu' as completed"
