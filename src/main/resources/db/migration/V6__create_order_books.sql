-- Drop the problematic table if it exists
DROP TABLE IF EXISTS order_books;

-- Create order_books with composite primary key
CREATE TABLE order_books (
    order_id CHAR(36) NOT NULL,
    book_id CHAR(36) NOT NULL,
    PRIMARY KEY (order_id, book_id),
    CONSTRAINT fk_order_books_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_order_books_book FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Create indexes for better performance
CREATE INDEX idx_order_books_order_id ON order_books(order_id);
CREATE INDEX idx_order_books_book_id ON order_books(book_id);