-- Add PayPal-related fields to users table
ALTER TABLE users
ADD COLUMN paypal_email VARCHAR(255) AFTER address,
ADD COLUMN paypal_payer_id VARCHAR(255) AFTER paypal_email;

-- Create index for faster PayPal payer lookups
CREATE INDEX idx_users_paypal_payer_id ON users(paypal_payer_id);
CREATE INDEX idx_users_paypal_email ON users(paypal_email);

-- Add PayPal payer ID to orders table
ALTER TABLE orders
ADD COLUMN paypal_payer_id VARCHAR(255) AFTER paypal_order_id;

-- Create index for faster PayPal payer lookups
CREATE INDEX idx_orders_paypal_payer_id ON orders(paypal_payer_id);