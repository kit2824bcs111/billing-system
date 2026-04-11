CREATE DATABASE IF NOT EXISTS cafe_billing;
USE cafe_billing;

CREATE TABLE IF NOT EXISTS menu_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    stock INT DEFAULT 100
);

CREATE TABLE IF NOT EXISTS bills (
    id INT AUTO_INCREMENT PRIMARY KEY,
    customer_name VARCHAR(100),
    total DECIMAL(10,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS bill_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    bill_id INT,
    item_name VARCHAR(100),
    quantity INT,
    unit_price DECIMAL(10,2),
    FOREIGN KEY (bill_id) REFERENCES bills(id)
);

INSERT INTO menu_items (name, price, stock) VALUES
('Masala Chai',   40.00, 100),
('Cold Coffee',  120.00, 80),
('Veg Sandwich',  80.00, 50),
('Paneer Roll',  130.00, 40),
('Gulab Jamun',   60.00, 60);
