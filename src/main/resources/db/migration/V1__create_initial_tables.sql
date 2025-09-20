-- Create table roles
CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_name VARCHAR(255) UNIQUE NOT NULL
);

-- Create table users
CREATE TABLE users (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    image LONGTEXT,
    email VARCHAR(255),
    password VARCHAR(255),
    mobile VARCHAR(50),
    address LONGTEXT,
    created_at DATETIME(6),
    last_updated DATETIME(6),
    role_id BIGINT,
    FOREIGN KEY (role_id) REFERENCES roles(id)
);

-- Create table categories
CREATE TABLE categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255)
);

-- Create table authors
CREATE TABLE authors (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    name VARCHAR(255),
    description LONGTEXT
);

-- Create table books
CREATE TABLE books (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    title VARCHAR(255),
    image LONGTEXT,
    description LONGTEXT,
    published_date DATE,
    page INT,
    price DOUBLE,
    created_at DATETIME(6),
    last_updated DATETIME(6),
    author_id CHAR(36),
    category_id BIGINT,
    FOREIGN KEY (author_id) REFERENCES authors(id),
    FOREIGN KEY (category_id) REFERENCES categories(id)
);