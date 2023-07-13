-- V3__create_product_table.sql
CREATE TABLE Product (
                         id SERIAL PRIMARY KEY,
                         name VARCHAR(255) NOT NULL,
                         description TEXT,
                         price DECIMAL(10, 2) NOT NULL,
                         available_quantity INT NOT NULL default 0
);
