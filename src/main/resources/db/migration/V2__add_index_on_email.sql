-- V2__add_index_on_email.sql

-- Create an index on the email column
CREATE INDEX idx_email ON users (email);
