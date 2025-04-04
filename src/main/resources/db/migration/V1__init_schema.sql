-- Initial schema setup

-- Create extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create audit trigger function
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
   NEW.updated_at = now();
   RETURN NEW;
END;
$$ language 'plpgsql';

-- Create schema check function
CREATE OR REPLACE FUNCTION check_if_table_exists(schema_name TEXT, table_name TEXT)
RETURNS BOOLEAN AS $$
DECLARE
    exists_val BOOLEAN;
BEGIN
    SELECT EXISTS (
        SELECT FROM information_schema.tables 
        WHERE table_schema = schema_name
        AND table_name = table_name
    ) INTO exists_val;
    
    RETURN exists_val;
END;
$$ LANGUAGE plpgsql;

-- Note: Actual entity tables will be created in subsequent migrations
-- during the implementation of specific features
