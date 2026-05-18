-- 1. Create independent tables first
CREATE TABLE users (
                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       name VARCHAR(255) NOT NULL,
                       email VARCHAR(255) NOT NULL UNIQUE,
                       password TEXT NOT NULL
);

CREATE TABLE categories (
                            id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                            name VARCHAR(255) NOT NULL
);

-- 2. Create dependent tables with inline foreign keys
CREATE TABLE addresses (
                           id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                           street VARCHAR(255) NOT NULL,
                           city VARCHAR(255) NOT NULL,
                           state VARCHAR(255) NOT NULL,
                           zip VARCHAR(255) NOT NULL,
                           user_id UUID NOT NULL REFERENCES users(id) ON DELETE NO ACTION
);

CREATE TABLE products(
                         id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                         name VARCHAR(255) NOT NULL,
                         price NUMERIC(10, 2) NOT NULL DEFAULT 0.00 CHECK (price > 0.00),
                         description TEXT NOT NULL,
                         category_id UUID REFERENCES categories(id) ON DELETE NO ACTION
);

CREATE TABLE profiles (
                          id UUID PRIMARY KEY REFERENCES users(id) ON DELETE NO ACTION,
                          bio TEXT,
                          phone_number VARCHAR(15),
                          date_of_birth DATE,
    -- PostgreSQL doesn't have an UNSIGNED type, so we use a CHECK constraint
                          loyalty_points INTEGER DEFAULT 0 CHECK (loyalty_points >= 0)
);

CREATE TABLE wishlist (
                          product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
                          user_id UUID NOT NULL REFERENCES users(id) ON DELETE NO ACTION,
                          PRIMARY KEY (product_id, user_id)
);

-- 3. Create indexes for foreign keys
-- (PostgreSQL does not automatically index foreign keys like MySQL does)
CREATE INDEX idx_addresses_user_id ON addresses (user_id);
CREATE INDEX idx_products_category_id ON products (category_id);
CREATE INDEX idx_wishlist_user_id ON wishlist (user_id);