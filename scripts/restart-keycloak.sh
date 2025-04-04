#!/bin/bash

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[0;33m'
NC='\033[0m' # No Color

# Change to project root directory since this script is in scripts/
cd "$(dirname "$0")/.." || exit 1
PROJECT_ROOT=$(pwd)

echo -e "${YELLOW}Setting file permissions...${NC}"
chmod +x docker/keycloak/tools/docker-entrypoint.sh
chmod +x docker/postgres/init-scripts/*.sh 2>/dev/null || true

echo -e "${GREEN}Creating directories if they don't exist...${NC}"
mkdir -p docker/keycloak/import

echo -e "${YELLOW}Restarting PostgreSQL and Keycloak containers...${NC}"
docker-compose stop db keycloak
docker-compose rm -f db keycloak

# Create a PostgreSQL healthcheck script
cat > /tmp/wait-for-postgres.sh <<EOF
#!/bin/bash
set -e

until PGPASSWORD=ecommerce_pass psql -h localhost -p 5433 -U ecommerce_user -d ecommerce -c '\q'; do
  echo "PostgreSQL is unavailable - sleeping for 2 seconds"
  sleep 2
done

echo "PostgreSQL is up and running!"
EOF
chmod +x /tmp/wait-for-postgres.sh

echo -e "${YELLOW}Starting PostgreSQL container...${NC}"
docker-compose up -d db

echo -e "${YELLOW}Waiting for PostgreSQL to be healthy...${NC}"
/tmp/wait-for-postgres.sh

echo -e "${YELLOW}Starting Keycloak container...${NC}"
docker-compose up -d keycloak

echo -e "${GREEN}Waiting for services to start...${NC}"
sleep 5

echo -e "${YELLOW}Checking Keycloak logs...${NC}"
docker-compose logs keycloak

echo -e "${GREEN}Restart completed!${NC}"
echo "Keycloak admin console should be available at: http://localhost:8081"
echo "Username: admin"
echo "Password: admin"

# Clean up
rm /tmp/wait-for-postgres.sh
