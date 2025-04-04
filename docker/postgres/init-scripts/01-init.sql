-- Create database if not exists
CREATE DATABASE keycloak WITH OWNER = ecommerce_user;

-- Create extensions for Keycloak database
\c keycloak;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

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
