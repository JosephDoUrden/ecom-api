#!/bin/bash
set -e

# Define default values and check if required environment variables are set
KC_DB_USERNAME=${KC_DB_USERNAME:-ecommerce_user}
KC_DB_PASSWORD=${KC_DB_PASSWORD:-ecommerce_pass}
KC_DB_HOST=${KC_DB_HOST:-db}
KC_DB_PORT=${KC_DB_PORT:-5432}
KC_DB_DATABASE=${KC_DB_DATABASE:-keycloak}
MAX_RETRY=${MAX_RETRY:-30}
RETRY_INTERVAL=${RETRY_INTERVAL:-2}

echo "Database connection configuration:"
echo " - Host: $KC_DB_HOST"
echo " - Port: $KC_DB_PORT"
echo " - Username: $KC_DB_USERNAME"
echo " - Database: $KC_DB_DATABASE"

# Check if we're running locally for testing
if [[ "$KC_DB_HOST" == "db" && ! -f /.dockerenv ]]; then
  echo "WARNING: It appears you are running this script outside of Docker."
  echo "The hostname 'db' will likely not resolve. Consider using:"
  echo "  KC_DB_HOST=localhost KC_DB_PORT=5433 $(basename "${0}")"
  echo "Make sure you're running this from the project root directory:"
  echo "  KC_DB_HOST=localhost KC_DB_PORT=5433 ./docker/keycloak/tools/docker-entrypoint.sh"
  echo "Continuing anyway with current settings..."
fi

# Check if required PostgreSQL tools are available
if ! command -v pg_isready >/dev/null || ! command -v psql >/dev/null; then
  echo "ERROR: PostgreSQL client tools (pg_isready, psql) are not installed or not in PATH."
  echo "Please install PostgreSQL client tools to use this script:"
  echo "  - On macOS: brew install postgresql"
  echo "  - On Ubuntu/Debian: apt-get install postgresql-client"
  echo "  - On RHEL/CentOS: yum install postgresql"
  exit 1
fi

# Function to wait for PostgreSQL to be ready
wait_for_postgres() {
  echo "Waiting for PostgreSQL server to be ready at $KC_DB_HOST:$KC_DB_PORT..."
  
  local count=0
  # Explicitly connect to the ecommerce database, NOT the username
  while [ $count -lt "$MAX_RETRY" ]; do
    count=$((count+1))
    echo "Attempt $count/$MAX_RETRY..."
    
    if PGPASSWORD=$KC_DB_PASSWORD pg_isready -h "$KC_DB_HOST" -p "$KC_DB_PORT" -U "$KC_DB_USERNAME" -d ecommerce; then
      # Verify we can actually connect to the ecommerce database
      if PGPASSWORD=$KC_DB_PASSWORD psql -h "$KC_DB_HOST" -p "$KC_DB_PORT" -U "$KC_DB_USERNAME" -d ecommerce -c '\conninfo' &>/dev/null; then
        echo "PostgreSQL server is up and accessible!"
        return 0
      else
        echo "PostgreSQL server responded to pg_isready but psql connection failed. Retrying..."
      fi
    else
      echo "PostgreSQL server is unavailable - sleeping for $RETRY_INTERVAL seconds..."
      sleep "$RETRY_INTERVAL"
    fi
  done
  
  echo "ERROR: Failed to connect to PostgreSQL after $MAX_RETRY attempts."
  return 1
}

# Function to check if keycloak database exists and create it if not
ensure_keycloak_db() {
  echo "Checking if $KC_DB_DATABASE database exists..."
  
  # First check if we can connect to postgres database (default system database)
  local count=0
  while [ $count -lt "$MAX_RETRY" ]; do
    count=$((count+1))
    echo "Attempt $count/$MAX_RETRY to connect to postgres database..."
    
    if PGPASSWORD=$KC_DB_PASSWORD psql -h "$KC_DB_HOST" -p "$KC_DB_PORT" -U "$KC_DB_USERNAME" -d postgres -c '\q' 2>/dev/null; then
      echo "Successfully connected to PostgreSQL default database."
      break
    else
      echo "PostgreSQL default database not yet accessible - waiting ($count/$MAX_RETRY)..."
      if [ $count -eq "$MAX_RETRY" ]; then
        echo "ERROR: Could not connect to the PostgreSQL default database."
        return 1
      fi
      sleep "$RETRY_INTERVAL"
    fi
  done
  
  # Check if keycloak database exists
  echo "Checking if $KC_DB_DATABASE database exists by listing all databases..."
  if PGPASSWORD=$KC_DB_PASSWORD psql -h "$KC_DB_HOST" -p "$KC_DB_PORT" -U "$KC_DB_USERNAME" -d postgres -c "SELECT datname FROM pg_database WHERE datname = '$KC_DB_DATABASE';" | grep -q "$KC_DB_DATABASE"; then
    echo "$KC_DB_DATABASE database already exists."
  else
    echo "$KC_DB_DATABASE database does not exist. Creating it now..."
    
    # Create keycloak database with explicit encoding and template
    if PGPASSWORD=$KC_DB_PASSWORD psql -h "$KC_DB_HOST" -p "$KC_DB_PORT" -U "$KC_DB_USERNAME" -d postgres -c "CREATE DATABASE \"$KC_DB_DATABASE\" WITH ENCODING 'UTF8' TEMPLATE template0;"; then
      echo "Successfully created $KC_DB_DATABASE database!"
      
      # Create extensions
      echo "Setting up extensions in $KC_DB_DATABASE database..."
      if PGPASSWORD=$KC_DB_PASSWORD psql -h "$KC_DB_HOST" -p "$KC_DB_PORT" -U "$KC_DB_USERNAME" -d "$KC_DB_DATABASE" -c "CREATE EXTENSION IF NOT EXISTS \"uuid-ossp\";"; then
        echo "Successfully created uuid-ossp extension."
      else
        echo "WARNING: Failed to create uuid-ossp extension. Keycloak may still work without it."
      fi
      
      if PGPASSWORD=$KC_DB_PASSWORD psql -h "$KC_DB_HOST" -p "$KC_DB_PORT" -U "$KC_DB_USERNAME" -d "$KC_DB_DATABASE" -c "ALTER SCHEMA public OWNER TO \"$KC_DB_USERNAME\";"; then
        echo "Successfully set schema ownership."
      else
        echo "WARNING: Failed to set schema ownership. This may cause permission issues."
      fi
      
      # Grant all privileges explicitly
      if PGPASSWORD=$KC_DB_PASSWORD psql -h "$KC_DB_HOST" -p "$KC_DB_PORT" -U "$KC_DB_USERNAME" -d postgres -c "GRANT ALL PRIVILEGES ON DATABASE \"$KC_DB_DATABASE\" TO \"$KC_DB_USERNAME\";"; then
        echo "Successfully granted all privileges on $KC_DB_DATABASE to $KC_DB_USERNAME."
      else
        echo "WARNING: Failed to grant privileges. This may cause permission issues."
      fi
    else
      echo "ERROR: Failed to create $KC_DB_DATABASE database!"
      return 1
    fi
  fi
  
  # Verify keycloak database is fully accessible
  count=0
  while [ $count -lt "$MAX_RETRY" ]; do
    count=$((count+1))
    echo "Attempt $count/$MAX_RETRY to verify $KC_DB_DATABASE database accessibility..."
    if PGPASSWORD=$KC_DB_PASSWORD psql -h "$KC_DB_HOST" -p "$KC_DB_PORT" -U "$KC_DB_USERNAME" -d "$KC_DB_DATABASE" -c '\conninfo' 2>/dev/null; then
      echo "$KC_DB_DATABASE database is ready and accessible!"
      return 0
    else
      echo "$KC_DB_DATABASE database is not yet accessible - waiting ($count/$MAX_RETRY)..."
      if [ $count -eq "$MAX_RETRY" ]; then
        echo "ERROR: Could not connect to the $KC_DB_DATABASE database."
        return 1
      fi
      sleep "$RETRY_INTERVAL"
    fi
  done
}

# Main script execution
echo "Starting Keycloak container setup..."

# First wait for PostgreSQL server to be up
if wait_for_postgres; then
  # Then ensure the keycloak database exists
  if ensure_keycloak_db; then
    # Execute the original entrypoint with the provided arguments
    if [ -f /opt/keycloak/bin/kc.sh ]; then
      echo "Starting Keycloak with command: $*"
      exec /opt/keycloak/bin/kc.sh "$@"
    else
      echo "WARNING: /opt/keycloak/bin/kc.sh not found. This script should be run inside a Keycloak container."
      echo "Script completed successfully, but could not start Keycloak."
      exit 0
    fi
  else
    echo "ERROR: Failed to ensure the Keycloak database exists."
    exit 1
  fi
else
  echo "ERROR: Failed to connect to PostgreSQL server."
  exit 1
fi
