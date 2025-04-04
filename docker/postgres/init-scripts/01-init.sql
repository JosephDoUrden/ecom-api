-- Make sure the database exists - create it if it doesn't
SELECT 'CREATE DATABASE ecommerce' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'ecommerce')\gexec

-- Grant appropriate permissions to ecommerce_user
GRANT ALL PRIVILEGES ON DATABASE ecommerce TO ecommerce_user;

-- Drop keycloak database if exists to ensure clean state
DROP DATABASE IF EXISTS keycloak;

-- Create keycloak database explicitly
CREATE DATABASE keycloak;

-- Give appropriate permissions to ecommerce_user
GRANT ALL PRIVILEGES ON DATABASE keycloak TO ecommerce_user;

-- Create extensions for Keycloak database
\c keycloak;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Set ownership for the keycloak schema
ALTER SCHEMA public OWNER TO ecommerce_user;

-- Create extensions for ecommerce database
\c ecommerce;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- Create audit trigger function
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
   NEW.updated_at = now();
   RETURN NEW;
END;
$$ language 'plpgsql';
