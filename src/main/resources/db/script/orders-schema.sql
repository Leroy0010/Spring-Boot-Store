CREATE TABLE orders(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL REFERENCES users(id),
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total_price NUMERIC(10, 2) NOT NULL DEFAULT 0.00
);

CREATE TABLE order_items(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL REFERENCES orders(id),
    product_id UUID NOT NULL REFERENCES products(id),
    unit_price NUMERIC(10, 2) NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    total_price NUMERIC(10, 2) NOT NULL DEFAULT 0.00
);