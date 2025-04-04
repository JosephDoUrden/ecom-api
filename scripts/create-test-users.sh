#!/bin/bash

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[0;33m'
NC='\033[0m' # No Color

# Change to project root directory since this script is in scripts/
cd "$(dirname "$0")/.." || exit 1
PROJECT_ROOT=$(pwd)

echo -e "${GREEN}Creating sample users in Keycloak...${NC}"

# Wait for Keycloak to be ready
echo "Waiting for Keycloak to be ready..."
until curl -s http://localhost:8081/health/ready > /dev/null; do
  sleep 5
done

# Login to get admin token
echo "Logging in as admin..."
TOKEN=$(curl -s -X POST http://localhost:8081/realms/master/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=admin" \
  -d "password=admin" \
  -d "grant_type=password" \
  -d "client_id=admin-cli" | jq -r '.access_token')

if [ -z "$TOKEN" ] || [ "$TOKEN" == "null" ]; then
  echo -e "${RED}Failed to get admin token${NC}"
  exit 1
fi

# Create test users
echo "Creating test admin user..."
curl -s -X POST http://localhost:8081/admin/realms/ecommerce/users \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin_user",
    "email": "admin@example.com",
    "firstName": "Admin",
    "lastName": "User",
    "enabled": true,
    "credentials": [
      {
        "type": "password",
        "value": "password",
        "temporary": false
      }
    ],
    "realmRoles": ["admin"]
  }'

echo "Creating test customer user..."
curl -s -X POST http://localhost:8081/admin/realms/ecommerce/users \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "customer",
    "email": "customer@example.com",
    "firstName": "Regular",
    "lastName": "Customer",
    "enabled": true,
    "credentials": [
      {
        "type": "password",
        "value": "password",
        "temporary": false
      }
    ],
    "realmRoles": ["customer"]
  }'

echo "Creating test vendor user..."
curl -s -X POST http://localhost:8081/admin/realms/ecommerce/users \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "vendor",
    "email": "vendor@example.com",
    "firstName": "Test",
    "lastName": "Vendor",
    "enabled": true,
    "credentials": [
      {
        "type": "password",
        "value": "password",
        "temporary": false
      }
    ],
    "realmRoles": ["vendor"]
  }'

echo -e "${GREEN}Sample users created successfully!${NC}"
echo "You can now login with:"
echo "- admin_user@example.com / password (Admin)"
echo "- customer@example.com / password (Customer)"
echo "- vendor@example.com / password (Vendor)"
