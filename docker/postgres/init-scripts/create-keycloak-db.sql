-- Drop keycloak database if exists
DROP DATABASE IF EXISTS keycloak;

-- Create keycloak database
CREATE DATABASE keycloak;

-- Give appropriate permissions
GRANT ALL PRIVILEGES ON DATABASE keycloak TO ecommerce_user;

-- Connect to keycloak database
\c keycloak;

-- Create extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Set ownership for the keycloak schema
ALTER SCHEMA public OWNER TO ecommerce_user;
