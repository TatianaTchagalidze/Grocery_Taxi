-- V1__create_users_table.sql

-- Create the users table
CREATE TABLE users (
                       id INT PRIMARY KEY,
                       name VARCHAR(100) NOT NULL,
                       email VARCHAR(100) NOT NULL
);

-- Add an index on the email column
CREATE INDEX idx_users_email ON users (email);
