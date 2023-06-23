-- V5__create_order_item_table.sql
CREATE TABLE OrderItem (
                           id BIGINT PRIMARY KEY,
                           order_id BIGINT NOT NULL,
                           product_id BIGINT NOT NULL,
                           quantity INT NOT NULL,
                           FOREIGN KEY (order_id) REFERENCES Orders (id),
                           FOREIGN KEY (product_id) REFERENCES Product (id)
);