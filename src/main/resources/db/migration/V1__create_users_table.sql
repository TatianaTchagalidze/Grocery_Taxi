-- V1__create_users_table.sql
DROP TABLE IF EXISTS users CASCADE;

-- Create the users table
CREATE TABLE users (
                       id SERIAL PRIMARY KEY,
                       email VARCHAR(255) NOT NULL,
                       first_name VARCHAR(255) NOT NULL,
                       last_name VARCHAR(255) NOT NULL,
                       role VARCHAR(255) NOT NULL,
                       password VARCHAR(255) NOT NULL
);