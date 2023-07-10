-- V5__create_order_item_table.sql
CREATE TABLE OrderItem (
                           id SERIAL PRIMARY KEY,
                           order_id INT NOT NULL,
                           product_id INT NOT NULL,
                           quantity INT NOT NULL,
                           FOREIGN KEY (order_id) REFERENCES Orders (id),
                           FOREIGN KEY (product_id) REFERENCES Product (id)
);