-- Create carts table (one cart per user)
CREATE TABLE IF NOT EXISTS carts (
    id CHAR(36) PRIMARY KEY,
    user_id CHAR(36) NOT NULL UNIQUE,
    created_at DATETIME NOT NULL,
    last_updated DATETIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create cart_books junction table (many-to-many between carts and books)
CREATE TABLE IF NOT EXISTS cart_books (
    cart_id CHAR(36) NOT NULL,
    book_id CHAR(36) NOT NULL,
    PRIMARY KEY (cart_id, book_id),
    FOREIGN KEY (cart_id) REFERENCES carts(id) ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE
);

-- Create indexes for better query performance
CREATE INDEX idx_carts_user_id ON carts(user_id);
CREATE INDEX idx_cart_books_cart_id ON cart_books(cart_id);
CREATE INDEX idx_cart_books_book_id ON cart_books(book_id);