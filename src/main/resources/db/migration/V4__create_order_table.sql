-- V4__create_order_table.sql
CREATE TABLE Orders (
                        id BIGINT PRIMARY KEY,
                        user_id BIGINT NOT NULL,
                        order_state VARCHAR(20) NOT NULL,
                        total_amount DECIMAL(10, 2) NOT NULL,
                        editable BOOLEAN default true,
                        FOREIGN KEY (user_id) REFERENCES users (id)

);